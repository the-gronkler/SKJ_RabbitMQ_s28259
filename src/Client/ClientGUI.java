package Client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ClientGUI extends JFrame {
    public static final String
            CLIENT = "Client",
            SERVER = "Server";


    private final MessageHandler messageHandler;

    private final JTextPane chatPane;
    private final JComboBox<String> subjectComboBox;
    private final JTextField messageField;

    public ClientGUI( ){
        try {
            messageHandler = new MessageHandler(this);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Message Handler creation failed: " + e);
            throw new RuntimeException(e);
        }

        // subject dropdown:
        subjectComboBox = new JComboBox<>();
        for( String subject : MessageHandler.SUBJECTS.keySet() )
            subjectComboBox.addItem(subject);



        //send button
        JButton sendButton = new JButton("send");
        sendButton.addActionListener( e -> sendMessage());

        // message entry field
        messageField = new JTextField();
        messageField.addActionListener(e -> sendButton.doClick());

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER)
                sendButton.doClick();
            return false;  // Allow the event to be dispatched to its normal recipient
        });

        // chat display
        chatPane = new JTextPane();
        chatPane.setEditable(false);

        // panels:
        JPanel subjectSelectPanel = new JPanel(new BorderLayout());
        JPanel sendPanel = new JPanel(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout());

        subjectSelectPanel.add(subjectComboBox, BorderLayout.CENTER);
        subjectSelectPanel.add(new JLabel("Subject:"), BorderLayout.WEST);

        sendPanel.add(subjectSelectPanel, BorderLayout.NORTH);
        sendPanel.add(messageField, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);

        main.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        main.add(sendPanel, BorderLayout.SOUTH);


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

        String subjectCode = MessageHandler.SUBJECTS.get(
                (String) subjectComboBox.getSelectedItem()
        );
        messageField.setText(null);


        messageHandler.sendMessage(message, subjectCode);

        this.displayMessage(CLIENT, message);
    }

    public void displayMessage(String sender, String message){
        Color color;
        switch (sender){
            case "Client" -> color = Color.BLUE;
            case "Server" -> color = Color.MAGENTA;
            default -> color = Color.BLACK;
        }

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


