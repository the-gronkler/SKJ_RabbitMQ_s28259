package Client;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args)  {
        SwingUtilities.invokeLater( ClientGUI::new);
    }
}
