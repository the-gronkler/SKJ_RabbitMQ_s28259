package server;

import java.io.IOException;


/**
 * Main class for the server application. Instantiates a {@link ServerMessageHandler} object,
 * and calls {@link ServerMessageHandler#beginProcessing()} to begin consuming messages from the queue
 */
public class ServerMain {
    /**
     * Main method for the server application. Instantiates a {@link ServerMessageHandler} object,
     * and calls {@link ServerMessageHandler#beginProcessing()} to begin consuming messages from the queue
     * @throws IOException If an error occurs with {@link ServerMessageHandler#beginProcessing()}
     */
    public static void main(String[] args) throws IOException {
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler();
        serverMessageHandler.beginProcessing();
    }
}
