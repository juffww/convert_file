package model.bo;

import model.dao.conversionDAO;
import model.bean.conversion;
import com.rabbitmq.client.*;

import java.util.List;

public class conversionBO {
    private conversionDAO conversionDAO = new conversionDAO();
    private final String QUEUE_NAME = "pdf_convert_queue";

    public boolean uploadAndQueue(conversion conv) {
        int newId = conversionDAO.createConversion(conv);

        if (newId > 0) {
            // 2. Nếu lưu DB thành công -> Đẩy ID vào RabbitMQ
            try {
                pushToRabbitMQ(String.valueOf(newId));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                // Nếu lỗi RabbitMQ, có thể update status DB thành FAILED
                conversionDAO.updateStatus(newId, "FAILED", "Queue Error");
                return false;
            }
        }
        return false;
    }

    public List<conversion> getUserHistory(int userId) {
        return conversionDAO.getHistoryByUserId(userId);
    }

    // Hàm private để đẩy message vào Queue
    private void pushToRabbitMQ(String message) throws Exception {
        // Đoạn code này cần thư viện amqp-client-5.x.x.jar
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // factory.setUsername("guest");
        // factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
        */
        System.out.println("Giả lập: Đã đẩy ID " + message + " vào hàng đợi RabbitMQ");
    }
}