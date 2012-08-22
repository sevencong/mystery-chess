package mysterychess.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import mysterychess.model.Match;
import mysterychess.model.Piece;
import mysterychess.model.Team.TeamPosition;
import mysterychess.util.Util;

/**
 * This is a view of {@link Match}.
 * 
 * @author Tin Bui-Huy
 */
public class ChessTable extends JPanel {

    private int xMargin = 0;
    private int yMargin = 0;
    private float unit = 0;
    private Match match;
    private Piece selectedPiece;
    private Piece latestMovedPiece;
    private float PIECE_OVER_CELL_RATIO = 0.9f;

    public ChessTable(Match match) {
        this.match = match;
        addListeners();
        setEnabled(match.isEnabled());
    }

    private void addListeners() {
        match.addDataChangedListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() != null && e.getSource() instanceof Piece) {
                    latestMovedPiece = (Piece) e.getSource();
                }
                repaint(200);
            }
        });

        match.addCheckmatedListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() != null) {
                    setEnabled(false);
                    String msg = (String) e.getSource();
                    if (msg != null && !msg.equals("")) {
                        Util.showMessageConcurrently(ChessTable.this, msg);
                    }
                }
            }
        });

        match.addStatusChangedListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() != null && e.getSource() instanceof Boolean) {
                    boolean enabled = (Boolean) e.getSource();
                    if (isEnabled()) {
                        if (!enabled) {
                            selectedPiece = null;
                        }
                    }
                    setEnabled(enabled);
                }
            }
        });

        match.addMessageListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), e.getSource());
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (selectedPiece == null) {
                    return;
                }
                mouseReleasedHandler(e);
            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                mouseDraggedHandler(e);
            }

            public void mouseMoved(MouseEvent e) {
//                repaint();
            }
        });
    }

    protected void mouseReleasedHandler(MouseEvent e) {
        Point p = toChessTableCoordinate(e.getPoint());
        latestMovedPiece = selectedPiece;
        selectedPiece.move(p, false);
        selectedPiece = null;
        repaint();
    }

    protected void mouseDraggedHandler(MouseEvent e) {
        if (!isEnabled()) {
            return;
        }
        if (selectedPiece == null) {
            if (match.getActiveTeam().getPosition() == TeamPosition.BOTTOM) {
                selectedPiece = getPieceAt(e.getPoint());
            }
        }

        if (selectedPiece == null) {
            return;
        }
        latestMovedPiece = null;

        repaint();
    }

    private void drawSquare(Graphics g, Point p, int size, Color color) {
        Color c = g.getColor();
        g.setColor(color);
        g.drawRect(p.x - size / 2, p.y - size / 2, size, size);
        g.setColor(c);
    }

    private Piece getPieceAt(Point screenPoint) {
        Point chessPoint = toChessTableCoordinate(screenPoint);
//        Point screenValidPoint = toScreenCoordinate(chessPoint);

//        if (!isInImage(chessPoint, screenValidPoint)) {
//            return null;
//        }
        return match.getActiveTeam().getPieceAt(chessPoint);
    }

//    private boolean isInImage(Point chessPoint, Point screenValidPoint) {
//        float xRatio = (chessPoint.x < screenValidPoint.x) ? ((float) chessPoint.x) / screenValidPoint.x : ((float) screenValidPoint.x) / chessPoint.x;
//        if (xRatio > PIECE_OVER_CELL_RATIO) {
//            return false;
//        }
//        float yRatio = (chessPoint.y < screenValidPoint.y) ? ((float) chessPoint.y) / screenValidPoint.y : ((float) screenValidPoint.y) / chessPoint.y;
//        if (yRatio > PIECE_OVER_CELL_RATIO) {
//            return false;
//        }
//        return true;
//    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculateUnit();
        drawTable(g);
        drawPieces(g);
    }

    private void calculateUnit() {
        final int STATIC_MARGIN = 3;

        float unit1 = (getWidth() - 2 * STATIC_MARGIN) / (Util.MAX_X + 1);
        float unit2 = (getHeight() - 2 * STATIC_MARGIN) / (Util.MAX_Y + 1);
        unit = (unit1 < unit2 ? unit1 : unit2);
        
        xMargin = (int)(getWidth() - unit*(Util.MAX_X))/2;
        yMargin = (int)(getHeight() - unit * (Util.MAX_Y))/2;
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
            if (i == 0) continue;
            for (int j = -1; j <= 1; j++) {
                if (j == 0) continue;
                if (x == 0 && i < 0) continue;
                if (x == 8 && i > 0) continue;
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

        drawPieces(g, match.getBlackTeam().getPieces(), imageSize);
        drawPieces(g, match.getWhiteTeam().getPieces(), imageSize);
        if (match.getCheckPiece() != null) {
            drawSquare(g, toScreenCoordinate(
                    match.getCheckPiece().getPosition()), imageSize, Color.RED);

            Piece general = null;
            if (match.getCheckPiece().getTeam() == match.getBlackTeam()) {
                general = match.getWhiteTeam().getGeneral();
            } else {
                general = match.getBlackTeam().getGeneral();
            }

            drawSquare(g, toScreenCoordinate(general.getPosition()), imageSize, Color.RED);
        }
    }

    private void drawPieces(Graphics g, List<Piece> pieces, int size) {
        for (Piece p : pieces) {
            if (p != selectedPiece) {
                drawPiece(g, p, size);
                if (p.isAttacker()) {
                	drawSquare(g, toScreenCoordinate(p.getPosition()), size, Color.RED);
                }
            }
        }
        if (selectedPiece != null) {
            Point p = getMousePosition();
            if (p != null) {
                drawPiece(g, selectedPiece, p, size);
            }
        }
    }

    private void drawPiece(Graphics g, Piece piece, int size) {
        Point p = toScreenCoordinate(piece.getPosition());
        drawPiece(g, piece, p, size);
    }

    private void drawPiece(Graphics g, Piece piece, Point p, int size) {
        if (p == null) {
            return;
        }
        
        try {
            g.drawImage(Util.getImage(piece),
                    p.x - size / 2,
                    p.y - size / 2,
                    size,//piece.getImage().getWidth(null),
                    size,//piece.getImage().getHeight(null),
                    null);

            if (piece == latestMovedPiece) {
                drawSquare(g, p, size, Color.BLUE);
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
}
