package mysterychess.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mysterychess.model.GameTracker;
import mysterychess.model.GameTracker.MatchState;
import mysterychess.model.Piece;
import mysterychess.model.Team.TeamColor;
import mysterychess.network.dto.PieceDto;
import mysterychess.util.Util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2012</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ReplayDialog
        extends JDialog {

    BorderLayout borderLayout1 = new BorderLayout();
    ReplayPanel replayPanel = new ReplayPanel();
    JPanel jPanel2 = new JPanel();
    JButton playButton = new JButton();
    JSlider progressSlider = new JSlider();
    JPanel jPanel3 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JSlider speedSlider = new JSlider();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel stepLabel = new JLabel();
    java.util.List<GameTracker.MatchState> states;
    int currentStateIndex = -1;
    boolean playing = false;
    int speed = 15;
    final static int DEFAULT_DELAY = 1000 * 15; // milli seconds
    PlayThread player;

    private ReplayDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            initiate();
            // pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ReplayDialog(Frame parent, java.util.List<GameTracker.MatchState> states) {
        this(parent, "Playback", false);
        this.states = states;
        setProgressBoundaries();
        play();
    }

    private void initiate() throws Exception {
        setSize(600, 600);
        this.getContentPane().setLayout(borderLayout4);
        replayPanel.setLayout(null);
        jPanel2.setLayout(borderLayout3);
        playButton.setBorder(BorderFactory.createRaisedBevelBorder());
        playButton.setMinimumSize(new Dimension(40, 21));
        playButton.setPreferredSize(new Dimension(40, 21));
        playButton.setIcon(null);
        playButton.setText(">");
        progressSlider.setBorder(BorderFactory.createEtchedBorder());
        progressSlider.setPreferredSize(new Dimension(300, 4));
        jPanel3.setLayout(borderLayout2);
        jLabel1.setText("    Speed:");
        speedSlider.setBorder(BorderFactory.createEtchedBorder());
        speedSlider.setPreferredSize(new Dimension(160, 8));
        stepLabel.setBorder(BorderFactory.createEtchedBorder());
        stepLabel.setMaximumSize(new Dimension(100, 4));
        stepLabel.setMinimumSize(new Dimension(40, 4));
        stepLabel.setPreferredSize(new Dimension(60, 4));
        jPanel3.add(jLabel1, java.awt.BorderLayout.CENTER);
        jPanel3.add(speedSlider, java.awt.BorderLayout.EAST);
        jPanel3.add(stepLabel, java.awt.BorderLayout.WEST);
        jPanel2.add(playButton, java.awt.BorderLayout.WEST);
        jPanel2.add(progressSlider, java.awt.BorderLayout.CENTER);
        jPanel2.add(jPanel3, java.awt.BorderLayout.EAST);
        this.getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(replayPanel, java.awt.BorderLayout.CENTER);

        speedSlider.setMaximum(30);
        speedSlider.setMinimum(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setValue(speed);
//        Hashtable<Integer, String> labels = new Hashtable<Integer, String>();
//        labels.put(1, "1x");
//        labels.put(5, "5x");
//        labels.put(10, "10x");
//        labels.put(15, "15x");
//        labels.put(20, "20x");
//        speedSlider.setLabelTable(labels);

        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                speed = speedSlider.getValue();
                if (speed > 15) {
                    speed = speed * speed / 10;
                }
            }
        });

        progressSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeCurrentState(progressSlider.getValue());
                showState(progressSlider.getValue());
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playButton.getText().equals(">")) {
                    play();
                } else {
                    pause();
                }
            }
        });
    }

    synchronized private void changeCurrentState(int newStateIndex) {
        currentStateIndex = newStateIndex;
    }

    private void play() {
        playButton.setText("||");
        playing = true;
        if (currentStateIndex >= states.size() - 1) {
            changeCurrentState(0);
        }
        player = new PlayThread();
        player.start();
    }

    private void pause() {
        playButton.setText(">");
        playing = false;
    }

    private void setProgressBoundaries() {
        progressSlider.setMaximum(0);
        progressSlider.setMaximum(states.size() - 1);
        progressSlider.setMinorTickSpacing(1);
        progressSlider.setMajorTickSpacing(5);

        stepLabel.setText("1/" + states.size());
        progressSlider.setValue(0);

        replayPanel.setState(states.get(0));
    }

    synchronized private void showState(int stateIndex) {
        replayPanel.setState(states.get(stateIndex));
        progressSlider.setValue(stateIndex);
        stepLabel.setText((stateIndex + 1) + "/" + states.size());
    }

    class PlayThread extends Thread {

        final static int SLEEP_UNIT = 100;

        @Override
        public void run() {
            if (currentStateIndex == -1) {
                changeCurrentState(0);
            }

            STOP:
            while (currentStateIndex < states.size()) {

                showState(currentStateIndex);

                changeCurrentState(currentStateIndex + 1);

                int totalSleepTime = DEFAULT_DELAY / speed;
                int sleptTime = 0;
                while (sleptTime < totalSleepTime) {
                    if (!playing) {
                        break STOP;
                    }
                    try {
                        sleep(SLEEP_UNIT);
                        sleptTime += SLEEP_UNIT;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReplayDialog.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                    }
                }
            }
            playing = false;
            playButton.setText(">");
        }
    }

    class ReplayPanel extends JPanel {

        private int xMargin = 0;
        private int yMargin = 0;
        private float unit = 0;
//    private Piece latestMovedPiece;
        private float PIECE_OVER_CELL_RATIO = 0.9f;
        private MatchState state;

        public ReplayPanel() {
        }

        public void setState(MatchState state) {
            this.state = state;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            calculateUnit();
            drawTable(g);
            drawPieces(g);
        }

        private void calculateUnit() {
            final int STATIC_MARGIN = 3;

            float unit1 = (getWidth() - 2 * STATIC_MARGIN) / (Util.MAX_X + 1 + 2); // plus 2: captured and lost columns 
            float unit2 = (getHeight() - 2 * STATIC_MARGIN) / (Util.MAX_Y + 1);
            unit = (unit1 < unit2 ? unit1 : unit2);

            xMargin = (int) (getWidth() - unit * (Util.MAX_X)) / 2;
            yMargin = (int) (getHeight() - unit * (Util.MAX_Y)) / 2;
        }

        private void drawTable(Graphics g) {
            drawBorder(g);
            drawCells(g);
            decorate(g);
            drawPalaces(g);
        }

        private void drawBorder(Graphics g) {
            int delta = 2;
            int xm = xMargin - delta;
            int ym = yMargin - delta;
            int width = Math.round(Util.MAX_X * unit) + delta * 2;
            int heigth = Math.round(Util.MAX_Y * unit) + delta * 2;
            for (int i = 0; i < 3; i++) {
                g.drawRect(xm - i, ym - i,
                        width + i * 2,
                        heigth + i * 2);
            }
        }

        private void drawCells(Graphics g) {
            // Rows
            for (int i = 0; i <= Util.MAX_Y; i++) {
                g.drawLine(xMargin,
                        Math.round(yMargin + i * unit),
                        Math.round(xMargin + Util.MAX_X * unit),
                        Math.round(yMargin + i * unit));
            }

            // Columns
            for (int i = 0; i <= Util.MAX_X; i++) {
                g.drawLine(Math.round(xMargin + i * unit),
                        yMargin,
                        Math.round(xMargin + i * unit),
                        Math.round(yMargin + 4 * unit));

                g.drawLine(Math.round(xMargin + i * unit),
                        Math.round(yMargin + 5 * unit),
                        Math.round(xMargin + i * unit),
                        Math.round(yMargin + Util.MAX_Y * unit));
            }

        }

        private void decorate(Graphics g) {
            drawMark(g, 1, 2);
            drawMark(g, 1, 7);
            drawMark(g, 7, 2);
            drawMark(g, 7, 7);
            for (int i = 0; i <= 8; i += 2) {
                drawMark(g, i, 3);
                drawMark(g, i, 6);
            }
        }

        private void drawMark(Graphics g, int x, int y) {
            int len = Math.round(unit / 6);
            int d = Math.round(unit / 15);
            int ax = Math.round(x * unit) + xMargin;
            int ay = Math.round(y * unit) + yMargin;

            for (int i = -1; i <= 1; i++) {
                if (i == 0) {
                    continue;
                }
                for (int j = -1; j <= 1; j++) {
                    if (j == 0) {
                        continue;
                    }
                    if (x == 0 && i < 0) {
                        continue;
                    }
                    if (x == 8 && i > 0) {
                        continue;
                    }
                    int x1 = ax + d * i;
                    int y1 = ay + d * j;
                    g.drawLine(x1, y1, x1 + len * i, y1);
                    g.drawLine(x1, y1, x1, y1 + len * j);
                }
            }
        }

        private void drawPalaces(Graphics g) {
            // 3,0 - 5,2
            drawLine(g, 3, 0, 5, 2);

            // 3,2 - 5, 0
            drawLine(g, 3, 2, 5, 0);

            // 3,7 - 5,9
            drawLine(g, 3, 7, 5, 9);

            // 3,9 - 5,7
            drawLine(g, 3, 9, 5, 7);
        }

        private void drawLine(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(Math.round(x1 * unit + xMargin),
                    Math.round(y1 * unit + yMargin),
                    Math.round(x2 * unit + xMargin),
                    Math.round(y2 * unit + yMargin));
        }

        private void drawPieces(Graphics g) {
            int imageSize = Math.round(unit * PIECE_OVER_CELL_RATIO);
            drawPieces(g, state.getTable().blackTeam, TeamColor.BLACK, imageSize);
            drawPieces(g, state.getTable().whiteTeam, TeamColor.WHITE, imageSize);
            drawCapturedPieces(g, state.getCapturedPieces(), 
                    state.getTable().myTeam == TeamColor.WHITE ? TeamColor.BLACK 
                    : TeamColor.WHITE);
            drawLostPieces(g, state.getLostPieces(), state.getTable().myTeam);

//        if (match.getCheckPiece() != null) {
//            drawSquare(g, toScreenCoordinate(
//                    match.getCheckPiece().getPosition()), imageSize, Color.RED);
//
//            Piece general = null;
//            if (match.getCheckPiece().getTeam() == match.getBlackTeam()) {
//                general = match.getWhiteTeam().getGeneral();
//            } else {
//                general = match.getBlackTeam().getGeneral();
//            }
//
//            drawSquare(g, toScreenCoordinate(general.getPosition()), imageSize, Color.RED);
//        }
        }

   protected void drawLostPieces(Graphics g, PieceDto[] pieces, TeamColor color) {
        int u = (int)(unit/1.55f);
        int y = yMargin - (int) unit/2 + 4;
        int xM = Math.round(xMargin - unit - u*3f/4);
        for (PieceDto piece : pieces) {
            drawLostPiece(g, piece, color, new Point(xM, y), u);
            y += u;
        }
    }

     private void drawLostPiece(Graphics g, PieceDto piece, TeamColor color, Point p, int unit) {
        try {
            int imageSize = unit;
            g.drawImage(Util.getRetiredImage(piece, false, color),
                    p.x,
                    p.y,
                    imageSize,
                    imageSize,
                    null);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).severe(e.getMessage());
        }
    }
     
      protected void drawCapturedPieces(Graphics g, PieceDto[] pieces, TeamColor color) {
        int u = (int)(unit/1.55f);
        int y = getHeight() - yMargin - (int) unit/2 + u/2 - 4;
        int xM = Math.round(getWidth() - xMargin + u * 5f/4);
        for (PieceDto piece : pieces) {
            drawCapturedPiece(g, piece, color, new Point(xM, y), u);
            y -= u;
        }
    }

    private void drawCapturedPiece(Graphics g, PieceDto piece, TeamColor color, Point p, int unit) {
        try {
            int imageSize = unit;
            g.drawImage(Util.getRetiredImage(piece, true, color),
                    p.x,
                    p.y,
                    imageSize,
                    imageSize,
                    null);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).severe(e.getMessage());
        }
    }
     ///
     
        private void drawPieces(Graphics g, PieceDto[] pieces, TeamColor color, int size) {
            for (PieceDto p : pieces) {
                drawPiece(g, p, color, size);
//                if (p.isAttacker()) {
//                	drawSquare(g, toScreenCoordinate(p.getPosition()), size, Color.RED);
//                }
            }
        }

        private void drawPiece(Graphics g, PieceDto piece, TeamColor color, int size) {
            Point p = toScreenCoordinate(piece.pos);
            drawPiece(g, piece, color, p, size);
        }

        private void drawPiece(Graphics g, PieceDto piece, TeamColor color, Point p, int size) {
            try {
                g.drawImage(Util.getImage(piece, color),
                        p.x - size / 2,
                        p.y - size / 2,
                        size,//piece.getImage().getWidth(null),
                        size,//piece.getImage().getHeight(null),
                        null);

                if (piece.pos.equals(state.getLastMoveTo())) {
                    drawSquare(g, p, size, Color.BLUE);
//                    drawMove(g, toScreenCoordinate(state.getLastMoveFrom()), 
//                            toScreenCoordinate(state.getLastMoveTo()));
                }

            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString(), e);
            }
        }

        public Point toScreenCoordinate(Point p) {
            return new Point(Math.round(p.x * unit) + xMargin, Math.round(p.y * unit) + yMargin);
        }

        public Point toChessTableCoordinate(Point p) {
            return new Point(Math.round((p.x - xMargin) / unit), Math.round((p.y - yMargin) / unit));
        }

        private void drawSquare(Graphics g, Point p, int size, Color color) {
            Color c = g.getColor();
            g.setColor(color);
            g.drawRect(p.x - size / 2, p.y - size / 2, size, size);
            g.setColor(c);
        }

        private void drawMove(Graphics g, Point lastMoveFrom, Point lastMoveTo) {
            Color c = g.getColor();
            g.setColor(Color.ORANGE);
            g.drawLine(lastMoveFrom.x, lastMoveFrom.y, lastMoveTo.x, lastMoveTo.y);
            g.drawLine(lastMoveFrom.x + 1, lastMoveFrom.y + 1, lastMoveTo.x + 1, lastMoveTo.y + 1);

            int x = lastMoveFrom.x;
            int y = lastMoveFrom.y;
            int r = 8;
            g.fillOval(x / 2, y / 2, r, r);
            g.setColor(c);
        }
    }
}