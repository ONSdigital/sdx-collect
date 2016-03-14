package com.github.onsdigital.perkin;

import com.github.onsdigital.Configuration;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SurveyListener {

    protected static final String QUEUE_HOST = "RABBITMQ_HOST";
    protected static final String QUEUE_NAME = "RABBITMQ_QUEUE";
    protected static final String QUEUE_USERNAME = "RABBITMQ_DEFAULT_USER";
    protected static final String QUEUE_PASSWORD = "RABBITMQ_DEFAULT_PASS";

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
        //TODO: need to kickoff a new thread - otherwise if queue down, will never start
        //while (true) {

            try {
                startListening();
            } catch (IOException | InterruptedException e) {
                log.error("QUEUE|problem processing queue message: ", e);
            }

            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                //ignore
            }

           // log.info("QUEUE|CONNECTION|attempting to restart connection... ");
        //}
    }

    private void startListening() throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        if (username!=null) factory.setUsername(username);
        if (password!=null) factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queue, false, false, false, null);
        log.info("QUEUE|CONNECTION|START|listening to queue: {} on host: {} username: {}", queue, host, username);

        Consumer consumer = new DefaultConsumer(channel) {
            public static final boolean REQUEUE = true;
            public static final boolean DONT_REQUEUE = false;

            int retry = 0;
            int maxRetry = 3;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                log.info("QUEUE|MESSAGE|RECV|got message: {}", message);

                try {
                    if (transformer.transform(message)) {
                        log.info("QUEUE|MESSAGE|ACK|transform success, remove msg from queue. message: {}", message);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        retry = 0;
                    } else {
                        log.info("QUEUE|MESSAGE|REJECT|transform fail, reject, don't requeue. message: {}", message);
                        //TODO: place message on DLQ? if transform fails - should do this
                        //TODO: place message on DLQ? if publish fails - should retry
                        channel.basicReject(envelope.getDeliveryTag(), DONT_REQUEUE);
                    }

                } catch (Throwable t) {
                    log.error("QUEUE|MESSAGE|error during message processing", t);

                    //TODO: primitive for now - needs a delay?
                    if (++retry <= maxRetry) {
                        log.info("QUEUE|MESSAGE|RETRY|fail, reject ({} retries), requeue. message: {}", retry, message);
                        channel.basicReject(envelope.getDeliveryTag(), REQUEUE);
                    } else {
                        log.info("QUEUE|MESSAGE|FAIL|fail, reject ({} retries), DONT requeue. message: {}", retry, message);
                        channel.basicReject(envelope.getDeliveryTag(), DONT_REQUEUE);
                        retry = 0;
                    }
                }
            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException e) {
                log.warn("QUEUE|CONNECTION|END|handle shutdown - stopped listening to queue: {} on host: {}", queue, host, e);
            }
        };

        boolean NO_AUTO_ACK = false;
        channel.basicConsume(queue, NO_AUTO_ACK, consumer);
    }
}
