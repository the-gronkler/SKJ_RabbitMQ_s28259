package client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * GUI class for the client application.
 * <p>
 * When instantiated, it provides a graphical interface in the form of a chat application.
 * Allows the user to select a subject, type a message, and send it.
 * <p>
 * When the Enter key or the Send button are pressed, {@link MessageHandler#sendMessage(String, String)}
 * is called on its {@code messageHandler} field, passing the two-letter code of the subject and the contents of the message.
 */
public class ClientGUI extends JFrame {
    /**
     * Constant used for passing into the {@link ClientGUI#displayMessage(String, String)}
     * method as the {@code sender} parameter
     **/
    public static final String
            CLIENT = "Client",
            SERVER = "Server";

    /**
     * Constant dictionary with:
     * <ul>
     *     <li>Key - subject description</li>
     *     <li>Value - two-letter subject code</li>
     * </ul>
     **/
    public static final Map<String, String>
            SUBJECT_MAP = Map.of(
            "Capitalise", "CL",
            "Reverse", "RV",
            "Get byte value", "BV"
    );

    /**
     * Constant set representing subject descriptions. Points to {@link ClientGUI#SUBJECT_MAP}.keySet()
     */
    public static final Set<String>
            SUBJECT_NAMES = SUBJECT_MAP.keySet();

    /**
     *  {@link MessageHandler} for this class. Handles communication with the server
     */
    private final MessageHandler messageHandler;
    /**
     *  Chat message display
     **/
    private final JTextPane chatPane;
    /**
     *  Dropdown menu for selecting subject
     **/
    private final JComboBox<String> subjectComboBox;
    /**
     *  Text field for entering message
     **/
    private final JTextField messageField;

    /**
     * Constructor for the {@link ClientGUI} class.
     * <p>
     * Creates a {@link MessageHandler} instance and a window with the UI.
     * <p>
     * Strongly recommended to call this method on the AWT event dispatching thread
     * managed by {@link EventQueue}, as per the {@link javax.swing} threading policy.
     */
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

    /**
     * Gets the text from {@link #messageField} and the subject code for the currently selected subject,
     * then invokes {@link MessageHandler#sendMessage(String, String)} with these parameters.
     * <p>
     * If {@link #messageField} is empty, nothing will happen.
     */
    private void sendMessage(){
        String message =  messageField.getText();
        if (message.isEmpty())
            return;

        String subjectCode = SUBJECT_MAP.get(
                (String) subjectComboBox.getSelectedItem()
        );

        messageField.setText(null);

        try {
            messageHandler.sendMessage(subjectCode, message);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this,  "Sending message failed: " + e);
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater( () -> this.displayMessage(CLIENT, message) );
    }

    /**
     * Displays a message with the passed parameters in the {@code chatPane} in the format
     * "<b>sender:</b> message"
     * <p>
     * Also recommended to call this method on the AWT event dispatching thread
     * managed by {@link EventQueue}, as per the {@link javax.swing} threading policy.
     * @param sender author of the message. Recommended to use constants
     *      {@link ClientGUI#SERVER} and {@link ClientGUI#CLIENT} for best results
     * @param message message to be displayed
     */
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


