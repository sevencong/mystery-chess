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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
	private final static String ANIMATION_QUOTE_STRING = "\\";
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
		outputPanel.setLayout(new BorderLayout());
        outputPanel.setAlignmentY(CENTER_ALIGNMENT);
        outputPanel.setSize(new Dimension(100, 100));
        JScrollPane areaScrollPane = new JScrollPane(outputPane);
        areaScrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputPanel.add(areaScrollPane, BorderLayout.CENTER);
		outputPanel.add(tipPanel, BorderLayout.SOUTH);

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

	private void appendAnimationGif(String gifCode) {
        StyledDocument doc = outputPane.getStyledDocument();
        try {
            setAnimationGif(doc, "anim/" + gifCode + ".gif");
            doc.insertString(doc.getLength(), " ", doc.getStyle(ANIMATION));
        } catch (BadLocationException ble) {
            System.err
                    .println("Couldn't insert animation gif into the chat pane.");
        }
    }

	/**
	 * Returns a list of keys sorted by length.
	 */
	private List<String> getSortedAnimationKeys() {
		ArrayList<String> result = new ArrayList<String>();
		for (String key : animationMap.keySet()) {
			int pos = result.size();
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).length() < key.length()) {
					pos = i;
					break;
				}
			}
			result.add(pos, key);
			System.out.println("Keys: " + result);
		}
		return result;
	}

    private String processInput(String input) {

		for (String key : getSortedAnimationKeys()) {
			input = input.replace(key, ANIMATION_QUOTE_STRING + animationMap.get(key) + ANIMATION_QUOTE_STRING);
		}

        return input;
    }

	private final static Map<String, String> animationMap = new HashMap<String, String>();

	static {

		animationMap.put(":))", "big_laugh");
		animationMap.put(":D", "big_laugh");
		animationMap.put(":((", "cry");
		animationMap.put(">\"<", "thinking");
		animationMap.put(";))", "tongue");
		animationMap.put(":}", "angry");
		animationMap.put(":(", "sad");
		animationMap.put(";)", "happy");
		animationMap.put(":]", "frown");
		animationMap.put(":)", "smile");
		animationMap.put(":z)", "sleeping");
	}

    private void updateTextPane(String input) {
        input = processInput(input);

        int j = -1;
        do {
			j = input.indexOf(ANIMATION_QUOTE_STRING);
            if (j == -1) {
                appendString(input, REGULAR);
            } else {
                if (j > 0) {
                    String plainText = input.substring(0, j);
                    appendString(plainText, REGULAR);
                }
				// Extract animation code
                int k = -1;
                if (j + 1 < input.length()) {
					k = input.indexOf(ANIMATION_QUOTE_STRING, j + 1);
                    if (k != -1) {
                        String code = input.substring(j + 1, k);
						appendAnimationGif(code);
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
			boolean messageReceivedIconShown = false;
			private synchronized void showMessageReceivedIcon() {
				if (messageReceivedIconShown) {
					return;
				}

				messageReceivedIconShown = true;
                Thread t = new Thread() {
                    public void run() {
                        try {
                            outputPane.setBackground(CHAT_HIGHLIGHT_COLOR);
							sleep(70);
                            outputPane.setBackground(CHAT_BACKGROUND_COLOR);
							messageReceivedIconShown = false;
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

		// private static final int IMAGE_COUNT = 20;
		// private static final int IMAGE_SIZE = 30;

        public TipPanel() {
			setLayout(new GridLayout(5, 0, 0, 0));

			for (String name : animationMap.keySet()) {
				add(createButton(name));
            }
			setAutoscrolls(true);
            setVisible(false);
        }

        public void toggleVisibility() {
            setVisible(!isVisible());
            if (!isVisible()) {
                inputText.requestFocus();
            }
        }

		private JButton createButton(final String emotionKey) {
			JButton b = new JButton(new ImageIcon(Util.loadImage(
					"anim/" + animationMap.get(emotionKey) + ".gif")
					.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
			b.setPreferredSize(new Dimension(36, 36));
            b.setBackground(Color.white);
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
					String textToInsert = emotionKey;
                    int p = inputText.getCaretPosition();
                    String text = inputText.getText();
					String newText = text.substring(0, p) + textToInsert
							+ text.substring(p, text.length());
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
