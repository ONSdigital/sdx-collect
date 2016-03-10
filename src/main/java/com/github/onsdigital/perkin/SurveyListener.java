package com.github.onsdigital.perkin;

import com.github.onsdigital.Configuration;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.rabbitmq.client.*;

import java.io.IOException;

public class SurveyListener {

    protected static final String QUEUE_HOST = "queue.host";
    protected static final String QUEUE_NAME = "queue.name";
    protected static final String QUEUE_USERNAME = "queue.username";
    protected static final String QUEUE_PASSWORD = "queue.password";

    private String host = "rabbit";
    private String queue = "survey";
    private String username = null;
    private String password = null;

    private TransformEngine transformer = TransformEngine.getInstance();

    /**
     * Updates configured values if environment variables have been set.
     */
    public SurveyListener() {
        host = Configuration.get(QUEUE_HOST, host);
        queue = Configuration.get(QUEUE_NAME, queue);
        username = Configuration.get(QUEUE_USERNAME, username);
        password = Configuration.get(QUEUE_PASSWORD, password);
    }

    public void start() {
        //TODO: need to kickoff a new thread i think
        //while (true) {

            try {
                startListening();
            } catch (IOException | InterruptedException e) {
                System.out.println("queue exception: " + e.toString());
                e.printStackTrace();
            }

            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                //ignore
            }

           // System.out.println("queue *** attempting to restart connection... ");
        //}
    }

    private void startListening() throws java.io.IOException, java.lang.InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        if (username!=null) factory.setUsername(username);
        if (password!=null) factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queue, false, false, false, null);
        System.out.println("queue ******** listening to queue: " + queue + " on host: " + host);

        Consumer consumer = new DefaultConsumer(channel) {
            public static final boolean REQUEUE = true;
            public static final boolean DONT_REQUEUE = false;

            int retry = 0;
            int maxRetry = 3;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("queue ******** received '" + message + "'");

                try {
                    //TODO: decide whether to leave message on queue or move to dead letter q
                    if (transformer.transform(message)) {
                        System.out.println("queue ******** success, ack '" + message + "'");
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        retry = 0;
                    } else {
                        System.out.println("queue ******** fail, reject, don't requeue '" + message + "'");

                        channel.basicReject(envelope.getDeliveryTag(), DONT_REQUEUE);
                    }

                } catch (Throwable t) {
                    System.out.println("ERROR queue ******** Throwable: " + t.toString());
                    t.printStackTrace();
                    System.out.println("queue ******** fail, reject, requeue '" + message + "'");


                    //TODO: primitive for now - needs a delay?
                    if (++retry <= maxRetry) {
                        System.out.println("queue ******** fail, reject (" + retry + " retries), requeue '" + message + "'");
                        channel.basicReject(envelope.getDeliveryTag(), REQUEUE);
                    } else {
                        System.out.println("queue ******** fail, reject (" + retry + " retries), DONT requeue '" + message + "'");
                        channel.basicReject(envelope.getDeliveryTag(), DONT_REQUEUE);
                        retry = 0;
                    }
                }
            }
        };

        boolean NO_AUTO_ACK = false;
        channel.basicConsume(queue, NO_AUTO_ACK, consumer);
        System.out.println("queue ******** STOPPED listening to queue: " + queue + " on host: " + host);
    }
}
