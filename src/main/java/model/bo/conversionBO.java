package model.bo;

import model.dao.conversionDAO;
import model.bean.conversion;
import com.rabbitmq.client.*;
import utils.RabbitMQConnection;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class conversionBO {
    private conversionDAO conversionDAO = new conversionDAO();
    private final String QUEUE_NAME = "pdf_converter_queue";

    public boolean uploadAndQueue(conversion conv) throws Exception {
        int newId = conversionDAO.createConversion(conv);

        if (newId > 0) {
            try
            {
                JsonObject json = new JsonObject();
                json.addProperty("id", newId);
                json.addProperty("input_url", conv.getInputUrl());
                json.addProperty("input_filename", conv.getInputFilename());
                //Chuyen object thanh chuoi json
                String jsonMessage = json.toString();

                pushToRabbitMQ(jsonMessage);
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                conversionDAO.updateStatus(newId, "FAILED", "Queue Error: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public void updateConversionResult(int conversionId, String outputUrl, String outputPublicId) {
        conversionDAO.updateConversionResult(conversionId, outputUrl, outputPublicId);
    }

    public void updateStatus(int conversionId, String status, String errorMessage) {
        conversionDAO.updateStatus(conversionId, status, errorMessage);
    }

    public List<conversion> getUserHistory(int userId) {
        return conversionDAO.getHistoryByUserId(userId);
    }

    private void pushToRabbitMQ(String message) throws Exception {
        Connection conn = RabbitMQConnection.getConnection();
        try (Channel channel = conn.createChannel()){
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            //Gui jsonMessage
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent JSON to RabbitMQ: '" + message + "'");
        }
        catch (Exception e) {

        }
    }
}