package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Server {
    private final static String
            QUEUE_NAME = "queue_s28259",
            USERNAME = "client", 
            PASSWORD = "password";
    
    private Channel channel;

    private final static String ;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.establishConnection();
        server.beginListening();
    }

    private void establishConnection() {
        // Set up connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setUsername(USERNAME);
//        factory.setPassword(PASSWORD);

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
    }

    private void beginListening() throws IOException {
        // Set up a consumer to listen for messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

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

        System.out.println("Server is waiting for messages.");

        // Consume messages from the queue
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

        //keep the server running until manually interrupted
        //noinspection InfiniteLoopStatement,StatementWithEmptyBody
        while(true);
    }


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
