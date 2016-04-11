package com.github.onsdigital.perkin;

import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class SurveyListener implements Runnable {

    private String host;
    private String queue;
    private String username;
    private String password;

    private TransformEngine transformer = TransformEngine.getInstance();

    public SurveyListener() {
        host = ConfigurationManager.get("RABBITMQ_HOST");
        queue = ConfigurationManager.get("RABBITMQ_QUEUE");
        username = ConfigurationManager.get("RABBITMQ_DEFAULT_USER");
        password = ConfigurationManager.get("RABBITMQ_DEFAULT_PASS");
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {

            try {
                startListening();
            } catch (IOException | InterruptedException e) {
                log.error("QUEUE|problem processing queue message: ", e);
            }

            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                //ignore
            }

            log.info("QUEUE|CONNECTION|attempting to restart connection... ");
        }
    }

    private void startListening() throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setConnectionTimeout(10 * 1000); //10 seconds
        factory.setHost(host);
        if (StringUtils.isNotBlank(username)) factory.setUsername(username);
        if (StringUtils.isNotBlank(password)) factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queue, false, false, false, null);
        log.info("QUEUE|CONNECTION|START|listening to queue: {} on host: {} username: {} password: {}", queue, host, username, ConfigurationManager.getSafe("RABBITMQ_DEFAULT_PASS"));

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
                    transformer.transform(message);
                    log.info("QUEUE|MESSAGE|ACK|transform success, remove msg from queue. message: {}", message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    retry = 0;

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

    public String test() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        if (StringUtils.isNotBlank(username)) factory.setUsername(username);
        if (StringUtils.isNotBlank(password)) factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queue, false, false, false, null);

        String version = channel.getConnection().getServerProperties().get("version").toString();
        channel.close();

        return version;
    }
}
