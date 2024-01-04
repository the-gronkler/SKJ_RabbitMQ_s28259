package client;

import javax.swing.*;

/**
 * Main class for the client application.
 * Instantiates a {@link ClientGUI} object on the AWT event dispatch thread
 */
public class ClientMain {
    /**
     * Main method for the client application.
     * Instantiates a {@link ClientGUI} object on the AWT event dispatch thread
     */
    public static void main(String[] args)  {
        SwingUtilities.invokeLater( ClientGUI::new);
    }
}
