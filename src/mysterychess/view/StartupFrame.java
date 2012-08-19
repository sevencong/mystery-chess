package mysterychess.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.TitledBorder;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import mysterychess.model.ChessType;
import mysterychess.model.Match;
import mysterychess.model.Team.TeamColor;
import mysterychess.network.Chatter;
import mysterychess.network.RmiClient;
import mysterychess.network.RmiServer;
import mysterychess.util.PropertiesLoader;
import mysterychess.util.Util;

/**
 * Tin Bui-Huy
 */
public class StartupFrame extends JFrame {

    JPanel southPanel = new JPanel();
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    TitledBorder titledBorder1 = new TitledBorder("");
    ButtonGroup clientServerButtonGroup = new ButtonGroup();
    JRadioButton clientRadio = new JRadioButton();
    JRadioButton serverRadio = new JRadioButton();
    JLabel portLabel = new JLabel();
    JTextField portText = new JTextField();
    JLabel addressLabel = new JLabel();
    JComboBox addressCombo = new JComboBox();
    JCheckBox iMoveFirstCheck = new JCheckBox();
    JPanel northPanel = new JPanel() {

        // This is a work around to force the image to load to memory
        // before starting up the game.
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                Collection<Image> images = Util.getAllImages();
                int unit = this.getWidth() / images.size();
                int x = 0;
                int y = this.getWidth() / 2;
                for (Image im : images) {
                    g.drawImage(im,
                            x,
                            y,
                            unit,
                            unit,
                            null);
                    y += unit;
                }
            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).severe(e.getMessage());
            }
        }
    };
    JRadioButton chineseChess = new JRadioButton();
    JRadioButton mysteryChessChk = new JRadioButton();
    ButtonGroup chessTypeButtonGroup = new ButtonGroup();
    JLabel gameLimitTimeLabel = new JLabel();
    JTextField gameLimitTimeText = new JTextField();
    JLabel pieceMoveLimitTimeLabel = new JLabel();
    JTextField pieceMoveLimitTimeText = new JTextField();
    JLabel secLabel = new JLabel();
    JLabel minuteLabel = new JLabel();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel paddingPanel = new JPanel();
    private boolean started = false;

    public StartupFrame() {
        try {
            initComponent();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initComponent() throws Exception {
        final PropertiesLoader loader = new PropertiesLoader(Util.APPLICATION_NAME + ".inf");
        this.setIconImage(Util.getIconImage());
        this.setTitle(Util.getApplicationName() + " - " + Util.getVersion());
        titledBorder1 = new TitledBorder("Server or Client");
        this.getContentPane().setLayout(borderLayout1);
        okButton.setPreferredSize(new Dimension(70, 23));
        okButton.setText("OK");

        mainPanel.setLayout(gridBagLayout1);
        clientRadio.setSelected(true);
        clientRadio.setText("Client");
        clientRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addressCombo.setVisible(true);
                addressLabel.setVisible(true);
                iMoveFirstCheck.setVisible(false);

                mysteryChessChk.setVisible(false);
                chineseChess.setVisible(false);

                // Time options
                gameLimitTimeLabel.setVisible(false);
                gameLimitTimeText.setVisible(false);
                minuteLabel.setVisible(false);
                pieceMoveLimitTimeLabel.setVisible(false);
                pieceMoveLimitTimeText.setVisible(false);
                secLabel.setVisible(false);
            }
        });

        serverRadio.setSelected(true);
        serverRadio.setText("Server");
        serverRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addressCombo.setVisible(false);
                addressLabel.setVisible(false);
                iMoveFirstCheck.setVisible(true);

                mysteryChessChk.setVisible(true);
                chineseChess.setVisible(true);

                // Time options
                gameLimitTimeLabel.setVisible(true);
                gameLimitTimeText.setVisible(true);
                minuteLabel.setVisible(true);
                pieceMoveLimitTimeLabel.setVisible(true);
                pieceMoveLimitTimeText.setVisible(true);
                secLabel.setVisible(true);
            }
        });

        portLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        portLabel.setText("Port:");
        portText.setText(loader.getParameter("port", "4444"));
        addressLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        addressLabel.setText("Server address:");
        addressLabel.setVisible(false);
        addressCombo.setVisible(false);
        String str = loader.getParameter("server", "192.168.103.191");
        String[] addrs = str.split(";");
        for (String a : addrs) {
            addressCombo.addItem(a);
        }
        addressCombo.setEditable(true);
        addressCombo.setSelectedItem(addrs[0]);
        
        iMoveFirstCheck.setHorizontalAlignment(SwingConstants.TRAILING);
        iMoveFirstCheck.setSelected(true);
        iMoveFirstCheck.setText("I move first");
        chineseChess.setText("Chinese Chess");
        mysteryChessChk.setSelected(true);
        mysteryChessChk.setText("Mystery Chess");
        gameLimitTimeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        gameLimitTimeLabel.setText("Game limit time:");
        gameLimitTimeText.setText(String.valueOf(Util.GAME_EXPIRE_TIME / (60 * 1000)));
        gameLimitTimeText.setHorizontalAlignment(SwingConstants.LEFT);
        pieceMoveLimitTimeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        pieceMoveLimitTimeLabel.setText("Piece move limit time:");
        pieceMoveLimitTimeText.setText(String.valueOf(Util.PIECE_MOVE_EXPIRE_TIME / 1000));
        secLabel.setText("secs");
        minuteLabel.setText("mins");
        jLabel2.setText("jLabel2");
        southPanel.add(buttonPanel);
        buttonPanel.add(okButton);
        northPanel.setSize(new Dimension(100, 1));
        this.getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);
        this.getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
        clientServerButtonGroup.add(serverRadio);
        clientServerButtonGroup.add(clientRadio);
        chessTypeButtonGroup.add(chineseChess);
        chessTypeButtonGroup.add(mysteryChessChk);
        mainPanel.add(mysteryChessChk,
                new GridBagConstraints(4, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(10, 19, 0, 12), 9, 0));
        mainPanel.add(clientRadio, new GridBagConstraints(4, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(6, 19, 0, 13), 53, 0));
        mainPanel.add(portText, new GridBagConstraints(1, 1, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(9, 0, 0, 0), 242, 6));
        mainPanel.add(addressCombo, new GridBagConstraints(1, 2, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(8, 0, 0, 0), 242, 6));
        mainPanel.add(pieceMoveLimitTimeText,
                new GridBagConstraints(5, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(9, 0, 18, 0), 40, 7));
        mainPanel.add(gameLimitTimeText,
                new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(9, 0, 18, 0), 122, 7));
        mainPanel.add(secLabel, new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(11, 6, 18, 22), 9, 8));
        mainPanel.add(serverRadio, new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(6, 0, 0, 0), 77, 0));
        mainPanel.add(minuteLabel, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 7, 18, 8), 32, 10));
        mainPanel.add(chineseChess, new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(10, 0, 0, 6), 33, -1));
        mainPanel.add(pieceMoveLimitTimeLabel,
                new GridBagConstraints(3, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(10, 0, 18, 5), 15, 10));
        mainPanel.add(portLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(9, 15, 0, 0), 69, 11));
        mainPanel.add(addressLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(8, 25, 0, 0), 9, 11));
        mainPanel.add(iMoveFirstCheck,
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(9, 13, 0, 4), 17, 0));
        mainPanel.add(gameLimitTimeLabel,
                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(9, 15, 18, 0), 18, 12));
        mainPanel.add(paddingPanel, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 7, 0, 0), 19, 7));

        ActionListener enteredActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    String addrs = loader.getParameter("server", 
                            addressCombo.getSelectedItem().toString());
                    addrs = updateAddrs(addrs, addressCombo.getSelectedItem().toString());
                    loader.setParameter("server", addrs);
                    loader.setParameter("port", portText.getText());
                    loader.store();
                } catch (IOException ex) {
                    Logger.getLogger(StartupFrame.class.getName()).log(Level.WARNING,
                            "Fail to save application info file", ex);
                }
                startup();
            }
            
            private String updateAddrs(String currentAddresses, String newAddress) {
                final int MAX_ENTRY = 7;
                String[] s = currentAddresses.split(";");
                List<String> addrs = new ArrayList<String>();
                addrs.addAll(Arrays.asList(s));
                if (addrs.contains(newAddress)) {
                    addrs.remove(newAddress);
                }
                if (addrs.size() >= MAX_ENTRY) {
                    addrs.remove(addrs.size() - 1);
                }
                addrs.add(0, newAddress);
                StringBuilder st = new StringBuilder();
                for (String a : addrs) {
                    st.append(a);
                    st.append(";");
                }
                st.delete(st.length() - 1, st.length());
                return st.toString();
            }
        };
//        portText.addActionListener(enteredActionListener);
//        pieceMoveLimitTimeText.addActionListener(enteredActionListener);
//        gameLimitTimeText.addActionListener(enteredActionListener);
//        addressCombo.addActionListener(enteredActionListener);
        okButton.addActionListener(enteredActionListener);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            super.processWindowEvent(e);
            System.exit(0);
        } else {
            super.processWindowEvent(e);
        }
    }

    private void startup() {
        if (started) {
            return;
        }
        try {
            final Match match;
            final Chatter chater = new Chatter();
            ChessType type = mysteryChessChk.isSelected()
                    ? ChessType.MYSTERY_CHESS
                    : ChessType.NORMAL_CHESS;
            TeamColor color = TeamColor.BLACK;
            if (iMoveFirstCheck.isSelected()) {
                color = TeamColor.WHITE;
            }
            int port = Integer.parseInt(portText.getText());
            final String title;

            if (serverRadio.isSelected()) {
                try {
                    // Time in minute
                    int gameLimitTime = Integer.parseInt(gameLimitTimeText.getText());
                    Util.GAME_EXPIRE_TIME = gameLimitTime * 60 * 1000;
                } catch (NumberFormatException e) {
                }
                try {
                    // Time in second
                    int pieceMoveLimitTime = Integer.parseInt(pieceMoveLimitTimeText.getText());
                    Util.PIECE_MOVE_EXPIRE_TIME = pieceMoveLimitTime * 1000;
                } catch (NumberFormatException e) {
                }

                title = "Mystery Chess Server - " + Util.getVersion();
                match = new Match(type, color);

                RmiServer s = new RmiServer(match, port, chater);
                s.startup();
            } else {
                title = "Mystery Chess Client - " + Util.getVersion();
                String serverAddress = addressCombo.getSelectedItem().toString();
                RmiClient client = new RmiClient(serverAddress, port, chater);
                match = client.startup();
            }
            if (match != null) {
                this.setVisible(false);
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        started = true;
                        new MainFrame(match, chater, title).setVisible(true);
                    }
                });
            }
        } catch (NumberFormatException numberFormatException) {
            JOptionPane.showMessageDialog(this, "Invalid port number");
        } catch (UnknownHostException ue) {
            Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, "Problem happen while connecting to server", ue);
            JOptionPane.showMessageDialog(this, "Unknown host");
        } catch (RemoteException e) {
            Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, "Problem happen while starting up", e);
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (IOException iOException) {
            Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, "Problem happen while starting up", iOException);
            JOptionPane.showMessageDialog(this, "Problem happen while starting up");
        }
    }
}
