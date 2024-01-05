package client;

import javax.swing.*;
import java.awt.*;

/**
 * Main class for the client application.
 * Instantiates a {@link ClientGUI} object on the {@linkplain EventQueue AWT Event Dispatch Thread}
 */
public class ClientMain {
    /**
     * Main method for the client application.
     * Instantiates a {@link ClientGUI} object on the {@linkplain EventQueue AWT Event Dispatch Thread}.
     */
    public static void main(String[] args)  {
        SwingUtilities.invokeLater( ClientGUI::new);
    }
}
