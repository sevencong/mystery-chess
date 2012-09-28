/*
 * MainFrame.java
 *
 * Created on Mar 10, 2011, 8:51:45 PM
 */
package mysterychess.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import mysterychess.model.Match;
import mysterychess.network.Chatter;
import mysterychess.util.Util;

/**
 * 
 * @author Administrator
 */
public class MainFrame extends javax.swing.JFrame {

    private Match match;
    private JPanel chatPanel;
    private ChessPanel chessPanel;
    private final Chatter chatter;

    /** Creates new form MainFrame */
    public MainFrame(Match m, Chatter chatter, String title) {
        super(title);
        this.match = m;
        this.chatter = chatter;
        initComponents();
        pack();
    }

    private void initComponents() {
        this.setIconImage(Util.getIconImage());
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        chatPanel = new ChatPanel(chatter);

        JSplitPane splitPane = new JSplitPane();
        contentPane.add(splitPane, java.awt.BorderLayout.CENTER);
        chessPanel = new ChessPanel(match);
        splitPane.setLeftComponent(chessPanel);
        splitPane.setRightComponent(chatPanel);

        this.setPreferredSize(new Dimension(730, 600));
        splitPane.setDividerLocation(550);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.80);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            chessPanel.saveGame(false);
            super.processWindowEvent(e);
            match.shutdown(true);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Sleep interrupted", ex);
            }
            System.exit(0);
        } else {
            super.processWindowEvent(e);
        }
    }
}
