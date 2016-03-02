package com.github.onsdigital.perkin.helpers;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Tx {

    private final static String QUEUE_HOST = "rabbit";
    private final static String QUEUE_NAME = "survey";

    public static void sendMessage(String message) throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(QUEUE_HOST);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }
}
