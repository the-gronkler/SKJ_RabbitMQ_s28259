package Client;

import com.rabbitmq.client.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MessageHandler {
    public static final Map<String, String> SUBJECTS = Map.of(
            "Capitalise", "CL",
            "Reverse", "RV",
            "Get byte value", "BV"
    );
    private final static String QUEUE_NAME = "hello";

    private final ClientGUI ui;
    private final Channel channel;
    private final String replyQueueName;
    private final DefaultConsumer consumer;
    private final AMQP.BasicProperties props;

    public MessageHandler(ClientGUI ui) {
        this.ui = ui;
        // set up connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try  {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Set up reply queue
            replyQueueName = channel.queueDeclare().getQueue();
        }
        catch (IOException | TimeoutException e) {
            JOptionPane.showMessageDialog(ui,  "Failed to establish connection: " + e);
            throw new RuntimeException(e);
        }

        // Set up properties for reply-to and correlation ID
        props = new AMQP.BasicProperties.Builder()
                .replyTo(replyQueueName)
                .correlationId(java.util.UUID.randomUUID().toString())
                .build();

        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(
                    String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    byte[] body )
            {
                String response = new String(body, StandardCharsets.UTF_8);
                ui.displayMessage(ClientGUI.SERVER , response);
            }
        };
    }

    public void sendMessage(String message, String subjectCode){
        byte[] messageBytes = (subjectCode + message).getBytes(StandardCharsets.UTF_8);

        try {
            channel.basicPublish(
                    "", QUEUE_NAME, props, messageBytes
            );
            // Start consuming messages from the reply queue
            channel.basicConsume(replyQueueName, true, consumer);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(ui,  "Sending message failed: " + e);
            throw new RuntimeException(e);
        }
    }
}
