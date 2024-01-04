package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Message handler for the server-side component of the RabbitMQ project.
 * <p>
 * This class establishes a connection to <b>RabbitMQ Server</b>, sets up a message queue, and begins listening for incoming messages.
 * Messages are processed based on their subject code, and a response is sent back to the client.
 * The server runs indefinitely, waiting for messages to arrive.
 * <p>
 * Note that this class requires a running instance of
 * <a href="https://www.rabbitmq.com/download.html">RabbitMQ Server</a>
 */
public class ServerMessageHandler {
    /**
     * Constant representing the name of the message queue.
     */
    private final static String
            QUEUE_NAME = "queue_s28259";

    /**
     * The communication channel with <b>RabbitMQ Server</b>.
     * <p>
     * This channel is used to declare and interact with the message queue.
     */
    private final Channel channel;
    /**
     * Callback function for handling incoming messages asynchronously.
     * <p>
     * The {@link DeliverCallback} is triggered when a message is delivered to the server.
     * It processes the message using the {@link ServerMessageHandler#processMessage(String)} method,
     * generates a response, and publishes the response back to the client.
     */
    private final DeliverCallback deliverCallback;


    /**
     * Constructs a new instance of the {@code Server} class.
     * <p>
     * Sets up a connection to <b>RabbitMQ Server</b>, creates a channel, and declares the message queue.
     * Also instantiates {@link ServerMessageHandler#deliverCallback}
     * which uses {@link ServerMessageHandler#processMessage(String)} to generate a response.
     */
    public ServerMessageHandler(){
        // Set up connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            this.channel = channel;

            System.out.println("Connection established.");
        }
        catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        // Set up a consumer to listen for messages
        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received message: " + message);

            String response = processMessage(message);
            channel.basicPublish(
                    "",
                    delivery.getProperties().getReplyTo(),
                    null,
                    response.getBytes(StandardCharsets.UTF_8)
            );
        };
    }

    /**
     * Begins processing incoming messages from the message queue.
     * <p>
     * Messages are processed asynchronously using {@link ServerMessageHandler#deliverCallback}
     *
     * @throws IOException If an error occurs while waiting for messages.
     */
    public void beginProcessing() throws IOException {
        System.out.println("Server is waiting for messages.");

        // Consume messages from the queue
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

        //keep the server running until manually interrupted
        //noinspection InfiniteLoopStatement,StatementWithEmptyBody
        while(true);
    }

    /**
     * Processes an incoming message based on its subject code.
     * <p>
     * Supported subject codes:
     * <ul>
     *     <li>"CL" - Capitalizes the content of the message.</li>
     *     <li>"RV" - Reverses the content of the message.</li>
     *     <li>"BV" - Concatenates, space-separated, the byte values of every character in the message</li>
     * </ul>
     * If the subject code is not recognized, returns a string indicating that in plain text.
     *
     * @param message The message to be processed, consisting of subject code concatenated with the content of the message.
     * @return The response to the message.
     */
    public static String processMessage(String message) {
        String subjectCode = message.substring(0, 2);
        String content = message.substring(2);

        switch (subjectCode){
            case "CL" -> {
                return content.toUpperCase();
            }
            case "RV" -> {
                StringBuilder sb = new StringBuilder();
                char[] arr = content.toCharArray();
                for(int i = arr.length - 1; i >= 0; i-- )
                    sb.append(arr[i]);
                return sb.toString();
            }
            case "BV" -> {
                StringBuilder sb = new StringBuilder();
                byte[] arr = content.getBytes(StandardCharsets.UTF_8);
                for(byte b : arr)
                    sb.append(b).append(" ");
                return sb.toString();

            }
            default -> {
                return "subject not recognised :(";
            }
        }
    }
}
