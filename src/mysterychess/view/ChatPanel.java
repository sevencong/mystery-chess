/**
 * 
 */
package mysterychess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import mysterychess.network.Chatter;
import mysterychess.util.Util;

/**
 * @author The Tran
 * 
 */
public class ChatPanel extends JPanel {

    /**
     * 
     */
    private final static Color CHAT_HIGHLIGHT_COLOR = new Color(0x4054cc80,
            true);
    private final static Color CHAT_BACKGROUND_COLOR = new Color(0xffffff);

    private static final long serialVersionUID = 1L;
    private static final String NEWLINE = System.getProperty("line.separator");

    public static final String REGULAR = "regular";
    public static final String ITALIC = "italic";
    public static final String BOLD = "bold";
    public static final String ANIMATION = "animation";

    /**
     * The text pane that supports custom styled (bold, italic, image...)
     */
    private JTextPane outputPane;
    private JTextField inputText = new JTextField();
    private JButton tipButton = new JButton("Tip");

    private final Chatter chatter;
    private final Action tipAction = new TipAction();
    private final TipPanel tipPanel = new TipPanel();

    public ChatPanel(Chatter chatter) {
        this.chatter = chatter;

        setLayout(new BorderLayout());
        outputPane = createTextPane();
        JPanel inputPanel = new JPanel();
        inputPanel.setSize(new Dimension(100, 20));
        inputPanel.setLayout(new BorderLayout(0, 0));
        inputPanel.add(inputText, BorderLayout.CENTER);
        tipButton.setAction(tipAction);
        inputPanel.add(tipButton, BorderLayout.EAST);

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setAlignmentY(CENTER_ALIGNMENT);
        outputPanel.setSize(new Dimension(100, 100));
        JScrollPane areaScrollPane = new JScrollPane(outputPane);
        areaScrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputPanel.add(areaScrollPane);
        outputPanel.add(tipPanel);

        add(outputPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();

        textPane.setAutoscrolls(true);
        textPane.setEditable(false);
        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);

        return textPane;
    }

    private void appendString(String text, String style) {
        StyledDocument doc = outputPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, doc.getStyle(style));
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert text into the chat pane.");
        }
    }

    private void appendAnimationGif(int gifCode) {
        StyledDocument doc = outputPane.getStyledDocument();
        try {
            setAnimationGif(doc, "anim/" + gifCode + ".gif");
            doc.insertString(doc.getLength(), " ", doc.getStyle(ANIMATION));
        } catch (BadLocationException ble) {
            System.err
                    .println("Couldn't insert animation gif into the chat pane.");
        }
    }

    private String processInput(String input) {
        input = input.replace(":))", ":2:");
        input = input.replace(":((", ":1:");
        input = input.replace(":]", ":11:");
        input = input.replace(":\")", ":5:");
        input = input.replace(">\"<", ":9:");
        input = input.replace(";))", ":14:");
        input = input.replace(":)", ":20:");
        return input;
    }

    private void updateTextPane(String input) {
        input = processInput(input);

        int j = -1;
        do {
            j = input.indexOf(":");
            if (j == -1) {
                appendString(input, REGULAR);
            } else {
                if (j > 0) {
                    String plainText = input.substring(0, j);
                    appendString(plainText, REGULAR);
                }
                // extract animation code
                int k = -1;
                if (j + 1 < input.length()) {
                    k = input.indexOf(":", j + 1);
                    if (k != -1) {
                        String code = input.substring(j + 1, k);
                        int gifCode = 20;
                        try {
                            gifCode = Integer.parseInt(code);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        appendAnimationGif(gifCode);
                    }
                }
                if (k == -1) {
                    input = input.substring(j);
                    appendString(input, REGULAR);
                    break;
                } else {
                    input = input.substring(k + 1);
                }
            }
        } while (j != -1);
        outputPane.setCaretPosition(outputPane.getStyledDocument().getLength());
    }

    private void addListeners() {
        inputText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String text = inputText.getText().trim();
                if (text.length() > 0) {
                    appendString(NEWLINE + "You: ", BOLD);
                    updateTextPane(inputText.getText());

                    chatter.messageTyped(inputText.getText());
                    inputText.setText("");
                }
            }
        });

        chatter.addMessageReceivedListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String msg = (String) e.getSource();
                appendString(NEWLINE + "Guest: ", BOLD);
                updateTextPane(msg);
                showMessageReceivedIcon();
            }

            private void showMessageReceivedIcon() {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            outputPane.setBackground(CHAT_HIGHLIGHT_COLOR);
                            sleep(300);
                            outputPane.setBackground(CHAT_BACKGROUND_COLOR);
                        } catch (InterruptedException ex) {
                        }
                    }
                };
                t.start();
            }
        });

    }

    protected void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle(REGULAR, def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle(ITALIC, regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle(BOLD, regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle(ANIMATION, regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
    }

    private void setAnimationGif(StyledDocument doc, String path) {
        Style s = doc.getStyle(ANIMATION);
        ImageIcon icon = Util.createImageIcon(path, "");
        if (icon != null) {
            StyleConstants.setIcon(s, icon);
        }
    }

    private class TipPanel extends JPanel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public TipPanel() {
            setLayout(new GridLayout(0, 3));

            for (int i = 1; i <= 20; i++) {
                add(createButton(i));
            }
            setVisible(false);
        }

        public void toggleVisibility() {
            setVisible(!isVisible());
            if (!isVisible()) {
                inputText.requestFocus();
            }
        }

        private JButton createButton(final int gifId) {
//            JButton b = new JButton(Util.createImageIcon("anim/" + gifId + ".gif", ""));
            JButton b = new JButton(new ImageIcon(Util.loadImage("anim/" + gifId + ".gif")
                    .getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
            b.setMaximumSize(new Dimension(30, 30));
            b.setBackground(Color.white);
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String textToInsert = ":" + gifId + ":";
                    int p = inputText.getCaretPosition();
                    String text = inputText.getText();
                    String newText = text.substring(0, p) + textToInsert + text.substring(p, text.length());
                    inputText.setText(newText);
                    super.mouseClicked(e);
                }
            });
            return b;
        }
    }

    private class TipAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public TipAction() {
            putValue(NAME, "Tip");
            putValue(SHORT_DESCRIPTION, "Emotion animation tips");
        }

        public void actionPerformed(ActionEvent e) {
            tipPanel.toggleVisibility();
        }
    }
}
