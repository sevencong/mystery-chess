package mysterychess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import mysterychess.model.ChessTimer;
import mysterychess.model.GameTracker;
import mysterychess.model.Match;
import mysterychess.model.Piece;
import mysterychess.model.RemoteActionListener;
import mysterychess.model.Team;
import mysterychess.util.Util;

/**
 *
 * @author Tin Bui-Huy
 */
public class ChessPanel extends JPanel {

//    private final static int CHESS_TABLE_MARGIN = 3;
    private final static int VERTICAL_PANEL_WIDTH = 40;
    private final static int HORIZONTAL_PANEL_HEIGHT = 30;
    private Match match;
    private JPanel lostPanel;
    private JPanel capturedPanel;
    private JPanel northPanel;
    private JPanel southPanel;
    private JDialog aboutFrame;

    public ChessPanel(Match m) {
        this.match = m;
        initComponents();
        match.addRemoteActionListeners(new RemoteActionListener() {

            public void errorReceived(String message) {
                Util.showMessageConcurrently(ChessPanel.this, message);
            }

            public void messageReceived(String message) {
                Util.showMessageConcurrently(ChessPanel.this, message);
            }

            public void shutdownRequested() {
                setEnabled(false);
                Util.showMessageConcurrently(ChessPanel.this,
                        "Guest has stopped playing");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setLayout(new BorderLayout());
        add(new ChessTable(match), java.awt.BorderLayout.CENTER);

        lostPanel = new LostPiecesPanel(match);
        lostPanel.setSize(new Dimension(VERTICAL_PANEL_WIDTH, 100));
        capturedPanel = new CapturedPiecePanel(match);
        capturedPanel.setSize(new Dimension(VERTICAL_PANEL_WIDTH, 100));
        createNorthPanel();
        createSouthPanel();

        add(lostPanel, java.awt.BorderLayout.WEST);
        add(capturedPanel, java.awt.BorderLayout.EAST);
        add(northPanel, java.awt.BorderLayout.NORTH);
        add(southPanel, java.awt.BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 600));
    }

    private void createSouthPanel() {
        southPanel = new TimerPanel(match, match.getTeam(Team.TeamPosition.BOTTOM));
        southPanel.setSize(new Dimension(100, HORIZONTAL_PANEL_HEIGHT));
        new Thread((Runnable) southPanel).start();
    }

    private void createNorthPanel() {
        northPanel = new JPanel();
        northPanel.setSize(new Dimension(100, HORIZONTAL_PANEL_HEIGHT));
        northPanel.setLayout(new BorderLayout());

        // Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton newGameButton = new JButton("New");
//        JButton saveButton = new JButton("Save");
        JButton replayButton = new JButton("Playback");
        JButton aboutButton = new JButton("About");
//        saveButton.setEnabled(false);
//        replayButton.setEnabled(true);
        buttonPanel.add(newGameButton);
//        buttonPanel.add(saveButton);
        buttonPanel.add(replayButton);
        buttonPanel.add(aboutButton);
        buttonPanel.setSize(150, HORIZONTAL_PANEL_HEIGHT);
        newGameButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final NewGamePanel ng = new NewGamePanel();
                ng.setListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        Team.TeamColor bottomTeam = ng.isMoveFirst()
                                ? Team.TeamColor.WHITE : Team.TeamColor.BLACK;
                        match.newGame(ng.getChessType(), bottomTeam);
                    }
                });
                ng.setLocationRelativeTo(ChessPanel.this);
                ng.setVisible(true);
            }
        });

//        saveButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                saveGame();
//            }
//        });

        replayButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                replayGame();
            }
        });

        aboutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showAbout();
            }
        });

        // Timer
        TimerPanel timerPanel = new TimerPanel(match, match.getTeam(Team.TeamPosition.TOP));
        northPanel.add(buttonPanel, BorderLayout.WEST);
        northPanel.add(timerPanel, BorderLayout.CENTER);
        new Thread(timerPanel).start();
    }

    private void saveGame() {
        String fileName = selectFile(false);
        match.saveGame(fileName);
    }

    private void loadGame() {
        String fileName = selectFile(true);
        match.loadGame(fileName);
    }
    
    private void replayGame() {
        
        FileInputStream fi = null;
        try {
            String fileName = selectFile(true);
            if (fileName == null) {
                return;
            }
            fi = new FileInputStream(fileName);
            ObjectInputStream oi = new ObjectInputStream(fi);
            List<GameTracker.MatchState> states = (List<GameTracker.MatchState>) oi.readObject();
            ReplayDialog d = new ReplayDialog(JOptionPane.getFrameForComponent(this), states);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fail to load file. File may not in correct format", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChessPanel.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        } finally {
            try {
                if (fi != null) {
                    fi.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ChessPanel.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            }
        }
    }


    private void showAbout() {
        if (aboutFrame == null) {
            aboutFrame = new AboutDialog(JOptionPane.getFrameForComponent(this));
        }
        aboutFrame.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
        aboutFrame.setVisible(true);
    }

    private String selectFile(boolean open) {
        try {
            JFileChooser fc = new JFileChooser(Util.DEFAULT_BASE_DIRECTORY);
            fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().toLowerCase().endsWith(".mchess");
                }

                @Override
                public String getDescription() {
                    return "*.mchess";

                }
            });

            fc.setCurrentDirectory(new java.io.File("."));
            if (fc.showDialog(this, open ? "Open" : "Save") == fc.APPROVE_OPTION) {
                String fileName = fc.getSelectedFile().getPath();
                if (!fileName.endsWith(".mchess")) {
                    fileName = fileName + ".mchess";
                }
                return fileName;
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * Time of the given team is over.
     */
    protected void timeOver(Team team) {
        Team myTeam = match.getTeam(Team.TeamPosition.BOTTOM);
        if (team.equals(myTeam)) {
            Team wonTeam = match.getTeam(Team.TeamPosition.TOP);
            String msg = "Time is over. "
                    + wonTeam.getColor().getDisplayName()
                    + " team has won the game!";
            match.gameOver(msg, true);
        }
    }

    private class TimerPanel extends JPanel implements Runnable {

        private JLabel timeLabel = new JLabel();
        private ChessTimer timer = new ChessTimer();
        private Team team;
        private Match match;
        private boolean gameStopped = false;
        private boolean pieceMoveTimeWarned = false;
        private JLabel pieceMoveTimeWarnLabel = new JLabel(
                new ImageIcon(Util.loadImage("alarm.gif").getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
        private JLabel gameTimeWarnLabel = new JLabel(
                new ImageIcon(Util.loadImage("monkey.gif").getScaledInstance(30, 30, Image.SCALE_DEFAULT)));

        public TimerPanel(Match match, Team team) {
            this.team = team;
            this.match = match;
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            pieceMoveTimeWarnLabel.setVisible(false);
            gameTimeWarnLabel.setVisible(false);
            add(gameTimeWarnLabel);
            add(timeLabel);
            add(pieceMoveTimeWarnLabel);

            match.addDataChangedListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() != null) {
                        if (e.getSource() instanceof Piece) {
                            if (!timer.isGameStarted()) {
                                timer.startGame();
                            }
                            Piece p = (Piece) e.getSource();
                            if (p.getTeam() != TimerPanel.this.team) {
                                timer.startMovePiece();
                            } else {
                                timer.stopMovePiece();
                            }
                        } else {
                            if (e.getSource() instanceof Match) {
                                // New game created
                                TimerPanel.this.team = TimerPanel.this.match.getTeam(TimerPanel.this.team.getPosition());
                                timer.reset();
                                gameStopped = false;
                                pieceMoveTimeWarned = false;
                                setGameTimeWarn(false);
                            }
                        }
                    }
                }
            });
            match.addCheckmatedListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    gameStopped = true;
                    pieceMoveTimeWarned = false;
                    setGameTimeWarn(false);
                }
            });

            match.addRemoteActionListeners(new RemoteActionListener() {

                public void errorReceived(String message) {
                    // Do nothing
                }

                public void messageReceived(String message) {
                    // Do nothing
                }

                public void shutdownRequested() {
                    gameStopped = true;
                    pieceMoveTimeWarned = false;
                }
            });
        }

        private void pieceMoveTimeWarn() {
            if (!pieceMoveTimeWarned) {
                Thread t = new Thread() {

                    public void run() {
                        try {
                            pieceMoveTimeWarnLabel.setVisible(true);
                            sleep(3000);
                            pieceMoveTimeWarnLabel.setVisible(false);
                        } catch (InterruptedException ex) {
                        }
                    }
                };
                t.start();
                pieceMoveTimeWarned = true;
            }
        }

        private void setGameTimeWarn(boolean warn) {
            gameTimeWarnLabel.setVisible(warn);
        }

        public void run() {

            while (true) {
                try {
                    if (timer.isRunning()) {
                        timeLabel.setForeground(Color.blue);
                    } else {
                        timeLabel.setForeground(Color.black);
                    }
                    if (!gameStopped) {
                        timeLabel.setText(timer.toString());
                        if (timer.getPieceMoveTimeLeft() < 15 * 1000 // 15 seconds
                                || timer.getGameTimeLeft() < 3 * 60 * 1000) { // 2 minutes
                            timeLabel.setForeground(Color.red);
                            if (timer.getPieceMoveTimeLeft() < 15 * 1000) { // 15 seconds
                                pieceMoveTimeWarn();
                            } else {
                                pieceMoveTimeWarned = false;
                            }

                            if (timer.getGameTimeLeft() < 3 * 60 * 1000) {
                                setGameTimeWarn(true);
                            }
                        }

                        // Allow 5 seconds extra
                        if (timer.getPieceMoveTimeLeft() < -4 * 1000
                                || timer.getGameTimeLeft() < -4 * 1000) {
                            timeOver(team);
                        }
                    }
                    // Update GUI after every 1 second
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChessPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
