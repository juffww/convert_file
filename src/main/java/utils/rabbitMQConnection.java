package utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnection {
    private static final String CLOUD_AMQP_URL = "amqps://nymrwcow:dhBMcLKNSyP6SPin8ytNmrbsPJ3kFqcd@possum.lmq.cloudamqp.com/nymrwcow";

    public static Connection getConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(CLOUD_AMQP_URL);
        factory.setConnectionTimeout(30000);
        return factory.newConnection();
    }
}
