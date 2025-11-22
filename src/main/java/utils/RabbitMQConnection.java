package utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnection {
    private static final String CLOUD_AMQP_URL = "amqps://nymrwcow:dhBMcLKNSyP6SPin8ytNmrbsPJ3kFqcd@possum.lmq.cloudamqp.com/nymrwcow";

    private static Connection connection;

    public static synchronized Connection getConnection() throws Exception {
        if(connection == null || !connection.isOpen()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(CLOUD_AMQP_URL);
            factory.setConnectionTimeout(30000);

            connection =  factory.newConnection();
            System.out.println("Ket noi rabbitMQ moi");
        }

        return connection;
    }
}
