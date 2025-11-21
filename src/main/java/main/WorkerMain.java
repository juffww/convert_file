package main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import io.imagekit.sdk.exceptions.*;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import utils.rabbitMQConnection;
import utils.imageKitConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import utils.SecurityConfig;

/*
* 1. Worker now get JSON from rabbitMQ
* 2. Download file from input_url
* 3. Conver
* 4. Upload to ImageKit
* 5. Call API localhost: 8082/.../api/callback
* */
public class WorkerMain {
    private static final String QUEUE_NAME = "pdf_converter_queue";
    // url to call api
    private static final String SERVER_CALLBACK_URL = "http://localhost:8082/api/callback";

    public static void main(String[] args)
    {
        System.out.println("Start independent worker");
        try
        {
            Connection conn = rabbitMQConnection.getConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String jsonMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [>] Nhận Job: " + jsonMessage);

                try
                {
                    //1. Parse json to get data
                    Gson gson = new Gson();
                    JsonObject job = gson.fromJson(jsonMessage, JsonObject.class);

                    int id = job.get("id").getAsInt();
                    String input_url =  job.get("input_url").getAsString();
                    String file_name =  job.get("input_filename").getAsString();

                    //2. Convert (Download -> convert -> upload)
                    processConversion(id, input_url, file_name);

                    //3. Confirm done with rabbitMQ
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            };
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

            Object lock = new Object();
            synchronized (lock) {lock.wait();}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void processConversion(int id, String input_url, String file_name)
    {
        try
        {
            System.out.println("    ---> Donwloading from " + input_url);
            //Download file from ImageKit
            InputStream fileStream = new URL(input_url).openStream();
            byte[] fileBytes = fileStream.readAllBytes();
            //Convert
            System.out.println("    --> Đang convert...");
            Thread.sleep(3000);
            byte[] convertedBytes = fileBytes;

            //Upload result (file docx) to ImageKit
            System.out.println("    --> Đang upload kết quả...");
            FileCreateRequest fileCreateRequest = new FileCreateRequest(convertedBytes, "converted_" + file_name + ".pdf");
            fileCreateRequest.setFolder("/docx_downloads/");

            Result result = imageKitConnection.getInstance().upload(fileCreateRequest);

            //Call api to server web
            reportSuccess(id, result.getUrl(), result.getFileId());
        } catch (Exception e) {
            System.err.println("    [!] Lỗi xử lý conversion: " + e.getMessage());
            e.printStackTrace();
            reportFail(id, e.getMessage());
        }
    }

    private static void reportSuccess(int id, String output_url, String output_public_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", "COMPLETED");
        jsonObject.addProperty("output_url", output_url);
        jsonObject.addProperty("output_public_id", output_public_id);

        sendCallback(jsonObject.toString());
    }

    private static void reportFail(int id, String error)
    {
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
            System.out.println(" [V] Báo cáo Server: " + response.statusCode());
        } catch (Exception e) {
            System.err.println(" [!] Lỗi gọi API Callback: " + e.getMessage());
        }
    }
}