package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Server-side component of the RabbitMQ project.
 * <p>
 * This class establishes a connection to <b>RabbitMQ Server</b>, sets up a message queue, and begins listening for incoming messages.
 * Messages are processed based on their subject code, and a response is sent back to the client.
 * The server runs indefinitely, waiting for messages to arrive.
 */
public class Server {
    /**
     * Constant representing the name of the message queue.
     */
    private final static String
            QUEUE_NAME = "queue_s28259";

    private final Channel channel;
    private final DeliverCallback deliverCallback;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.beginProcessing();
    }

    /**
     * Constructs a new instance of the {@code Server} class.
     * <p>
     * Sets up a connection to the RabbitMQ server, creates a channel, and declares the message queue.
     * Also defines {@link Server#deliverCallback}  which uses {@link Server#processMessage(String)} to generate a response.
     */
    public Server(){
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
     * Messages are processed asynchronously using {@link Server#deliverCallback}
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
     * If the subject code is not recognized, returns a message indicating that the subject was not found.
     *
     * @param message The incoming message to be processed.
     * @return The processed response based on the subject code.
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
                return "subject not found";
            }
        }
    }
}
