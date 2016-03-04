package com.github.onsdigital.perkin;

import com.github.onsdigital.perkin.transform.Transformer;
import com.rabbitmq.client.*;

import java.io.IOException;

public class SurveyListener {

    private final static String QUEUE_HOST = "rabbit";
    private final static String QUEUE_NAME = "survey";

    private Transformer transformer = Transformer.getInstance();

    public void start() {
        try {
            startListening();
        } catch (IOException | InterruptedException e) {
            System.out.println("queue exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void startListening() throws java.io.IOException, java.lang.InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(QUEUE_HOST);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("queue ******** listening to queue: " + QUEUE_NAME + " on host: " + QUEUE_HOST);

        Consumer consumer = new DefaultConsumer(channel) {
            public static final boolean REQUEUE = true;
            public static final boolean DONT_REQUEUE = false;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("queue ******** received '" + message + "'");

                try {
                    //TODO: decide whether to leave message on queue or move to dead letter q
                    if (transformer.transform(message)) {
                        System.out.println("queue ******** success, ack '" + message + "'");
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } else {
                        System.out.println("queue ******** fail, reject, don't requeue '" + message + "'");

                        channel.basicReject(envelope.getDeliveryTag(), DONT_REQUEUE);
                    }

                } catch (Throwable t) {
                    System.out.println("ERROR queue ******** Throwable: " + t.toString());
                    t.printStackTrace();
                    System.out.println("queue ******** fail, reject, requeue '" + message + "'");
                    channel.basicReject(envelope.getDeliveryTag(), REQUEUE);
                }
            }
        };

        channel.basicConsume(QUEUE_NAME, true, consumer);
        System.out.println("queue ******** STOPPED listening to queue: " + QUEUE_NAME + " on host: " + QUEUE_HOST);
    }
}
