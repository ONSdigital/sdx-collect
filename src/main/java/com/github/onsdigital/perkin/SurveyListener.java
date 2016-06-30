package com.github.onsdigital.perkin;

import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class SurveyListener implements Runnable, RecoveryListener {

    private String host;
    private int port;
    private String host2;
    private int port2;
    private String queue;
    private String username;
    private String password;

    private TransformEngine transformer = TransformEngine.getInstance();

    public SurveyListener() {
        host = ConfigurationManager.get("RABBITMQ_HOST");
        port = ConfigurationManager.getInt("RABBITMQ_PORT");
        host2 = ConfigurationManager.get("RABBITMQ_HOST2");
        port2 = ConfigurationManager.getInt("RABBITMQ_PORT2");
        queue = ConfigurationManager.get("RABBITMQ_QUEUE");
        username = ConfigurationManager.get("RABBITMQ_DEFAULT_USER");
        password = ConfigurationManager.get("RABBITMQ_DEFAULT_PASS");
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        boolean notConnected = true;
        Connection connection = null;

        while (true) {

            if (notConnected) {
                log.info("QUEUE|CONNECTION|attempting to restart connection... ");

                try {
                    connection = startListening();
                    log.debug("QUEUE|CONNECTION|returned from startListening()");
                } catch (IOException | InterruptedException e) {
                    log.error("QUEUE|problem processing queue message: ", e);
                }

                if (connection == null) {
                    notConnected = true;
                } else {
                    notConnected = ! connection.isOpen();
                }
            }

            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    private Connection startListening() throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setConnectionTimeout(10 * 1000); //10 seconds
        Address[] addresses = {new Address(host, port), new Address(host2, port2)};
        factory.setAutomaticRecoveryEnabled(true);

        if (StringUtils.isNotBlank(username)) factory.setUsername(username);
        if (StringUtils.isNotBlank(password)) factory.setPassword(password);
        Connection connection = factory.newConnection(addresses);
        Channel channel = connection.createChannel();
        ((Recoverable) channel).addRecoveryListener(this);

        channel.queueDeclare(queue, false, false, false, null);

        String server = getQueueInfo(connection);
        log.info("QUEUE|CONNECTION|START|listening to queue: {} on server: {} username: {} password: {}", queue, server, username, ConfigurationManager.getSafe("RABBITMQ_DEFAULT_PASS"));


        Consumer consumer = new DefaultConsumer(channel) {
            public static final boolean REQUEUE = true;
            public static final boolean DONT_REQUEUE = false;

            int retry = 0;
            int maxRetry = 3;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                String server = getQueueInfo(channel.getConnection());
                log.info("QUEUE|MESSAGE|RECV|server: {} got message: {}", server, message);

                try {
                    transformer.transform(message);
                    log.info("QUEUE|MESSAGE|ACK|transform success, remove msg from queue. message: {}", message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    retry = 0;

                } catch (Throwable t) {
                    log.error("QUEUE|MESSAGE|ERROR|error during message processing", t);

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
                log.warn("QUEUE|CONNECTION|END|shutdown on queue: {} on host: {}", queue, host, e);
            }
        };

        boolean NO_AUTO_ACK = false;
        channel.basicConsume(queue, NO_AUTO_ACK, consumer);
        return connection;
    }

    public String test() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        Address[] addresses = {new Address(host), new Address(host2)};
        if (StringUtils.isNotBlank(username)) factory.setUsername(username);
        if (StringUtils.isNotBlank(password)) factory.setPassword(password);
        Connection connection = factory.newConnection(addresses);
        Channel channel = connection.createChannel();

        channel.queueDeclare(queue, false, false, false, null);

        String version = channel.getConnection().getServerProperties().get("version").toString();
        connection.close();

        return version;
    }

    @Override
    public void handleRecovery(Recoverable recoverable) {
        log.warn("QUEUE|CONNECTION|END|queue connection was recovered");
        log.info("QUEUE|CONNECTION|START|queue connection was recovered");
    }

    private String getQueueInfo(Connection connection) {
        return connection.getAddress().getHostName() + ":" + connection.getPort();
    }
}
