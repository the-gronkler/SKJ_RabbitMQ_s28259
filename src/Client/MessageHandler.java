package client;

import com.rabbitmq.client.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


/**
 * Handles communication with the <b>RabbitMQ Server</b>, for sending and receiving messages.
 * <p>
 * This class establishes a connection to the <b>RabbitMQ Server</b>, sets up a message queue, and creates a reply queue.
 * It provides methods for sending messages and handling incoming responses asynchronously.
 */
public class MessageHandler {
    /**
     * Constant representing the name of the message queue.
     */
    public static final String
            QUEUE_NAME = "queue_s28259";


    /**
     * The communication channel with <b>RabbitMQ Server</b>.
     */
    private final Channel channel;
    /**
     * The name of the reply queue used for receiving responses from the server.
     */
    private final String replyQueueName;
    /**
     * Consumer for handling incoming messages.
     * Calls {@link ClientGUI#displayMessage(String, String)}
     * with parameters:
     * <ul>
     *     <li>sender - {@link ClientGUI#SERVER}</li>
     *     <li>message - {@link String} constructed from the recieved byte array</li>
     * </ul>
     */
    private final DefaultConsumer consumer;
    /**
     * Properties for outgoing messages.
     * Contains the correlation ID and the name of the reply queue.
     */
    private final AMQP.BasicProperties props;

    /**
     * Constructs a new instance of the {@code MessageHandler} class.
     *
     * @param ui The {@link ClientGUI} instance associated with this message handler.
     *           Used for displaying server responses in the GUI.
     */
    public MessageHandler(ClientGUI ui) {
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
                SwingUtilities.invokeLater( () -> ui.displayMessage(ClientGUI.SERVER, response) );
            }
        };
    }

    /**
     * Sends a message to the RabbitMQ server with the specified subject code and message content.
     * The method also starts consuming messages from the reply queue asynchronously.
     *
     * @param subjectCode The two-letter code representing the message subject.
     * @param message     The content of the message.
     * @throws IOException If an error occurs while sending the message.
     */
    public void sendMessage(String subjectCode, String message) throws IOException {
        byte[] messageBytes = (subjectCode + message).getBytes(StandardCharsets.UTF_8);

        channel.basicPublish("", QUEUE_NAME, props, messageBytes );

        // Start consuming messages from reply queue
        channel.basicConsume(replyQueueName, true, consumer);

    }
}
