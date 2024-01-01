package client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;

public class ClientGUI extends JFrame {
    public static final String
            CLIENT = "Client",
            SERVER = "Server";

    public static final Map<String, String>
            SUBJECT_MAP = Map.of(
            "Capitalise", "CL",
            "Reverse", "RV",
            "Get byte value", "BV"
    );
    public static final Set<String>
            SUBJECT_NAMES = SUBJECT_MAP.keySet();

    private final MessageHandler messageHandler;

    private final JTextPane chatPane;
    private final JComboBox<String> subjectComboBox;
    private final JTextField messageField;

    public ClientGUI( ){
        try {
            messageHandler = new MessageHandler(this);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null, "Message Handler creation failed: " + e);
            throw new RuntimeException(e);
        }

        // subject dropdown:
        subjectComboBox = new JComboBox<>();
        for( String subject : SUBJECT_NAMES )
            subjectComboBox.addItem(subject);

        //send button
        JButton sendButton = new JButton("send");
        sendButton.addActionListener( e -> sendMessage());
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER)
                sendButton.doClick();
            return false;
        });

        // message entry field
        messageField = new JTextField();

        // chat display
        chatPane = new JTextPane();
        chatPane.setEditable(false);

        // panels:
        JPanel subjectSelectPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout());

        subjectSelectPanel.add(subjectComboBox, BorderLayout.CENTER);
        subjectSelectPanel.add(new JLabel("Subject:"), BorderLayout.WEST);

        inputPanel.add(subjectSelectPanel, BorderLayout.NORTH);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        main.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);


        setTitle("Client App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(main);
        setVisible(true);
    }

    private void sendMessage(){
        String message =  messageField.getText();
        if (message.isEmpty())
            return;

        String subjectCode = SUBJECT_MAP.get(
                (String) subjectComboBox.getSelectedItem()
        );

        messageField.setText(null);

        messageHandler.sendMessage(subjectCode, message);
        this.displayMessage(CLIENT, message);
    }

    public void displayMessage(String sender, String message){
        Color color = switch (sender){
            case CLIENT -> Color.BLUE;
            case SERVER -> Color.MAGENTA;
            default -> Color.BLACK;
        };

        SimpleAttributeSet nameAttributes = new SimpleAttributeSet();
        StyleConstants.setBold(nameAttributes, true);
        StyleConstants.setForeground(nameAttributes, color);

        StyledDocument doc = chatPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), sender + ": ", nameAttributes);
            doc.insertString(doc.getLength(), message + "\n", new SimpleAttributeSet());
        }
        catch (BadLocationException e) {
            JOptionPane.showMessageDialog(this, "Message displaying failed: " + e);
            e.printStackTrace();
        }

        chatPane.setCaretPosition(doc.getLength());
    }
}


