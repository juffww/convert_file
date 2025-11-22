package main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import utils.rabbitMQConnection;
import utils.imageKitConnection;
import utils.SecurityConfig;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WorkerMain {
    private static final String QUEUE_NAME = "pdf_converter_queue";
    private static final String SERVER_CALLBACK_URL = "http://localhost:8082/api/callback";
    
    // CẤU HÌNH ĐƯỜNG DẪN (Sửa lại nếu cần thiết)
    // Nếu chạy trên Windows nhớ dùng dấu "\\" thay vì "/"
    private static final String PYTHON_SCRIPT_PATH = "convert_script.py"; 
    private static final String VENV_PYTHON_PATH = "venv/bin/python"; // Trỏ vào python trong venv

    public static void main(String[] args) {
        System.out.println("=== WORKER PYTHON PDF2DOCX STARTED ===");
        try {
            // Kiểm tra môi trường trước khi chạy
            if (!new File(PYTHON_SCRIPT_PATH).exists()) {
                System.err.println("❌ Lỗi: Không tìm thấy file script tại: " + new File(PYTHON_SCRIPT_PATH).getAbsolutePath());
                return;
            }

            Connection conn = rabbitMQConnection.getConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1); // Chỉ nhận 1 job mỗi lần để tránh quá tải CPU

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String jsonMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [>] Nhận Job mới: " + jsonMessage);

                try {
                    // 1. Parse JSON
                    Gson gson = new Gson();
                    JsonObject job = gson.fromJson(jsonMessage, JsonObject.class);

                    int id = job.get("id").getAsInt();
                    String input_url = job.get("input_url").getAsString();
                    String file_name = job.get("input_filename").getAsString();

                    // 2. Xử lý Convert (Download -> Python -> Upload)
                    processConversion(id, input_url, file_name);

                    // 3. Xác nhận xong việc (Ack)
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Có thể thêm logic nack() nếu muốn retry
                }
            };
            
            System.out.println(" [*] Đang chờ tin nhắn...");
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

            // Giữ app luôn chạy
            Object lock = new Object();
            synchronized (lock) { lock.wait(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processConversion(int id, String input_url, String file_name) {
        File tempPdf = null;
        File tempDocx = null;

        try {
            // BƯỚC 1: Tải file PDF về ổ cứng (File tạm)
            System.out.println("Đang tải file từ: " + input_url);
            tempPdf = File.createTempFile("job_" + id + "_in", ".pdf");
            try (InputStream in = new URL(input_url).openStream()) {
                Files.copy(in, tempPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // BƯỚC 2: Chuẩn bị file Output tạm
            tempDocx = File.createTempFile("job_" + id + "_out", ".docx");

            // BƯỚC 3: Gọi Python Script để convert
            System.out.println("    ⚙️ Đang gọi Python để convert...");
            long startTime = System.currentTimeMillis();
            
            boolean success = runPythonConversion(tempPdf.getAbsolutePath(), tempDocx.getAbsolutePath());
            
            long duration = System.currentTimeMillis() - startTime;

            if (!success) {
                throw new Exception("Python Script báo lỗi hoặc convert thất bại.");
            }
            System.out.println("Convert xong trong " + duration + "ms");

            // BƯỚC 4: Đọc file DOCX lên RAM và Upload
            System.out.println("Đang upload kết quả lên ImageKit...");
            byte[] convertedBytes = Files.readAllBytes(tempDocx.toPath());
            
            // Tên file mới: bỏ đuôi .pdf cũ, thêm .docx
            String newFileName = file_name.replace(".pdf", "") + ".docx";
            
            FileCreateRequest fileCreateRequest = new FileCreateRequest(convertedBytes, newFileName);
            fileCreateRequest.setFolder("/docx_downloads/");
            fileCreateRequest.setUseUniqueFileName(true); // Để tránh trùng tên

            Result result = imageKitConnection.getInstance().upload(fileCreateRequest);
            
            System.out.println("    🎉 Hoàn tất! URL: " + result.getUrl());

            // BƯỚC 5: Báo cáo Server
            reportSuccess(id, result.getUrl(), result.getFileId());

        } catch (Exception e) {
            System.err.println("    ❌ Lỗi xử lý Job " + id + ": " + e.getMessage());
            e.printStackTrace();
            reportFail(id, "Lỗi Worker: " + e.getMessage());
        } finally {
            // BƯỚC 6: Dọn dẹp file tạm (Quan trọng!)
            if (tempPdf != null && tempPdf.exists()) tempPdf.delete();
            if (tempDocx != null && tempDocx.exists()) tempDocx.delete();
        }
    }

    // Hàm gọi ProcessBuilder (giống SimpleTest)
    private static boolean runPythonConversion(String inputPath, String outputPath) {
        try {
            // Command: venv/bin/python convert_script.py <input> <output>
            ProcessBuilder pb = new ProcessBuilder(VENV_PYTHON_PATH, PYTHON_SCRIPT_PATH, inputPath, outputPath);
            pb.redirectErrorStream(true); // Gộp luồng lỗi để in ra console

            Process process = pb.start();

            // Đọc log từ Python (để debug nếu lỗi)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Chỉ in các dòng INFO hoặc ERROR quan trọng
                    if(line.contains("ERROR") || line.contains("WARNING")) {
                         System.out.println("    🐍 [Py]: " + line);
                    }
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0; // Trả về true nếu exit code là 0 (thành công)
        } catch (Exception e) {
            System.err.println("Lỗi gọi ProcessBuilder: " + e.getMessage());
            return false;
        }
    }

    // --- Các hàm gửi Callback giữ nguyên ---
    private static void reportSuccess(int id, String output_url, String output_public_id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", "COMPLETED");
        jsonObject.addProperty("output_url", output_url);
        jsonObject.addProperty("output_public_id", output_public_id);
        sendCallback(jsonObject.toString());
    }

    private static void reportFail(int id, String error) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", "FAILED");
        jsonObject.addProperty("errorMessage", error);
        sendCallback(jsonObject.toString());
    }

    private static void sendCallback(String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_CALLBACK_URL))
                    .header("Content-Type", "application/json")
                    .header("X-Callback-Secret", SecurityConfig.CALLBACK_SECRET)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                 System.err.println("    ⚠️ Server trả về code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("    ⚠️ Không gọi được Callback tới Server: " + e.getMessage());
        }
    }
}