package mysterychess.model;

import mysterychess.model.Team.TeamColor;
import mysterychess.util.Util;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mysterychess.model.Team.TeamPosition;
import mysterychess.network.dto.TableDto;
import mysterychess.view.ChessTable;

/**
 * TODO Split this class into 2 classes: a model and a controller.
 *
 * In MVC, Model is an observable.
 * View and Controller are observers.
 *
 * This class is a model for {@link ChessTable}.
 * 
 * @author Tin Bui-Huy
 */
public class Match {

    private boolean enabled = false;
    private Team whiteTeam; //White = Red - play first
    private Team blackTeam; // Black = Green
    private Team activeTeam;
    private Piece checkPiece;
    private ChessType type;
    private List<ModelActionListener> modelActionListeners = new CopyOnWriteArrayList<ModelActionListener>();
    private List<RemoteActionListener> remoteActionListeners = new CopyOnWriteArrayList<RemoteActionListener>();
    private List<ActionListener> pieceMovedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> dataChangedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> pieceCapturedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> checkmatedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> statusChangedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> newGameListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> messageListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> gameLoadedListeners = new CopyOnWriteArrayList<ActionListener>();
    private List<ActionListener> gameSavedListeners = new CopyOnWriteArrayList<ActionListener>();

    public ChessType getType() {
        return type;
    }

    /**
     * This constructor will be called with setData().
     */
    public Match() {
    }

    public Match(ChessType type, Team whiteTeam, Team blackTeam) {
        this.type = type;
        this.whiteTeam = whiteTeam;
        this.blackTeam = blackTeam;
        whiteTeam.setMatch(this);
        blackTeam.setMatch(this);
        activeTeam = whiteTeam;
        dataChanged(null);
    }

    public Match(ChessType type, Team.TeamColor bottomTeam) {
        this.type = type;
        Team bottom = null;
        Team top = null;
        if (type == ChessType.MYSTERY_CHESS) {
            bottom = Util.createMysteryBottomTeam(this);
            top = Util.createMysteryTopTeam(this);

        } else {
            bottom = Util.createNormalBottomTeam(this);
            top = Util.createNormalTopTeam(this);
        }

        if (bottomTeam == Team.TeamColor.BLACK) {
            whiteTeam = top;
            blackTeam = bottom;
        } else {
            whiteTeam = bottom;
            blackTeam = top;
        }
        whiteTeam.setColor(Team.TeamColor.WHITE);
        blackTeam.setColor(Team.TeamColor.BLACK);

        activeTeam = whiteTeam;
        dataChanged(null);
    }

    public void setData(ChessType type, Team whiteTeam, Team blackTeam, Team activeTeam) {
        this.type = type;
        this.whiteTeam = whiteTeam;
        this.blackTeam = blackTeam;
        whiteTeam.setMatch(this);
        blackTeam.setMatch(this);
        this.activeTeam = activeTeam;
        checkPiece = null;
        setEnabled(false);
        dataChanged(null);
    }

    public Team getTeam(TeamPosition position) {
        if (blackTeam.getPosition() == position) {
            return blackTeam;
        }
        return whiteTeam;
    }

    public Team getOtherTeam(Team t) {
        if (t == whiteTeam) {
            return blackTeam;
        }
        return whiteTeam;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        statusChanged();
    }

    public Piece getCheckPiece() {
        return checkPiece;
    }

    public Team getActiveTeam() {
        return activeTeam;
    }

    public Team getWhiteTeam() {
        return whiteTeam;
    }

    public Team getBlackTeam() {
        return blackTeam;
    }

    public Piece getPieceAt(Point position) {
        Piece p = whiteTeam.getPieceAt(position);
        if (p == null) {
            p = blackTeam.getPieceAt(position);
        }
        return p;
    }

    /**
     * The pieces at fromPosition and toPosition are exclusive.
     *
     * @param fromPosition
     * @param toPosition
     * @return
     */
    List<Piece> getPieces(Point fromPosition, Point toPosition) {
        if (fromPosition.x != toPosition.x
                && fromPosition.y != toPosition.y) {
            throw new IllegalArgumentException("Either x or y must be the same");
        }

        List<Piece> pieces = new ArrayList<Piece>();

        if (fromPosition.x == toPosition.x) {
            int y1 = (fromPosition.y < toPosition.y) ? fromPosition.y : toPosition.y;
            int y2 = (fromPosition.y == y1) ? toPosition.y : fromPosition.y;

            for (int i = y1 + 1; i < y2; i++) {
                Piece p = getPieceAt(new Point(fromPosition.x, i));
                if (p != null) {
                    pieces.add(p);
                }
            }
        } else {
            int x1 = (fromPosition.x < toPosition.x) ? fromPosition.x : toPosition.x;
            int x2 = (fromPosition.x == x1) ? toPosition.x : fromPosition.x;

            for (int i = x1 + 1; i < x2; i++) {
                Piece p = getPieceAt(new Point(i, fromPosition.y));
                if (p != null) {
                    pieces.add(p);
                }
            }
        }

        return pieces;
    }

    public void setActiveTeam(Team t) {
        activeTeam = t;
    }

    public void setInactiveTeam(Team t) {
        if (t == whiteTeam) {
            activeTeam = blackTeam;
        } else {
            activeTeam = whiteTeam;
        }
    }

    public void capturePieceAt(Point position, Team capturedByTeam) {
        Team myTeam = whiteTeam;
        Team otherTeam = blackTeam;
        if (capturedByTeam == whiteTeam) {
            myTeam = blackTeam;
            otherTeam = whiteTeam;
        }
        Piece p = myTeam.getPieceAt(position);
        myTeam.getPieces().remove(p);
        myTeam.getLostPieces().add(p);
        otherTeam.getCapturedPieces().add(p);
        pieceCaptured(p);
    }

    public void addPieceCapturedListener(ActionListener l) {
        pieceCapturedListeners.add(l);
    }

    public void addCheckmatedListener(ActionListener l) {
        checkmatedListeners.add(l);
    }

    public void addStatusChangedListener(ActionListener l) {
        statusChangedListeners.add(l);
    }

    public void addDataChangedListener(ActionListener l) {
        dataChangedListeners.add(l);
    }

    public void removeDataChangedListener(ActionListener l) {
        dataChangedListeners.remove(l);
    }

    public void addNewGameListener(ActionListener l) {
        newGameListeners.add(l);
    }

    public void removeNewGameListener(ActionListener l) {
        newGameListeners.remove(l);
    }

    public void addMessageListener(ActionListener l) {
        messageListeners.add(l);
    }

    public void removeMessageListener(ActionListener l) {
        messageListeners.remove(l);
    }

    public void addRemoteActionListeners(RemoteActionListener l) {
        remoteActionListeners.add(l);
    }

    public void removeRemoteActionListeners(RemoteActionListener l) {
        remoteActionListeners.remove(l);
    }

    public void addModelChangedListener(ModelActionListener l) {
        modelActionListeners.add(l);
    }

    public void removeModelChangedListeners(ModelActionListener l) {
        modelActionListeners.remove(l);
    }

    public void addGameLoadedListener(ActionListener l) {
        gameLoadedListeners.add(l);
    }

    public void removeGameLoadedListener(ActionListener pieceMovedListener) {
        gameLoadedListeners.remove(pieceMovedListener);
    }

    public void addGameSavedListener(ActionListener l) {
        gameSavedListeners.add(l);
    }

    public void removeGameSavedListener(ActionListener pieceMovedListener) {
        gameSavedListeners.remove(pieceMovedListener);
    }
    
    GameTracker gameTracker = new GameTracker();
    protected void pieceMoved(Point oldPos, Point newPos) {

        Line2D line = new Line2D.Float(oldPos, newPos);
        ActionEvent e = new ActionEvent(line, 1, null);
        for (ActionListener l : pieceMovedListeners) {
            l.actionPerformed(e);
        }

        for (ModelActionListener l : modelActionListeners) {
            l.pieceMoved(oldPos, newPos);
        }
    }

    /**
     * This event will be fired in either cases:
     * 1. When the game created. In this case source will be this match.
     * 2. When a piece moved.
     *
     * @param p the piece has just moved causing this event
     */
    protected void dataChanged(Piece p) {
        if(p == null) {
            gameTracker = new GameTracker();
        } else {
            GameTracker.MatchState s;
            
            // TODO change data model to support storing move line
            s = new GameTracker.MatchState(TableDto.toDtoTable(this), p.getPosition(), p.getPosition());
            gameTracker.addState(s);
        }
        
        // When a piece move p != null
        Object source = this;
        if (p != null) {
            source = p;
            determineCheck(p);
        }

        ActionEvent e = new ActionEvent(source, 1, null);
        for (ActionListener l : dataChangedListeners) {
            l.actionPerformed(e);
        }
    }

    private void pieceCaptured(Piece p) {
        ActionEvent e = new ActionEvent(p, 1, null);
        for (ActionListener l : pieceCapturedListeners) {
            l.actionPerformed(e);
        }
    }

    private void gameLoaded(TableDto table) {
        ActionEvent e = new ActionEvent(table, 1, null);
        for (ActionListener l : gameLoadedListeners) {
            l.actionPerformed(e);
        }
    }

    private void determineCheck(Piece pieceMoved) {
        // Determine check of the move team first
        // If it true --> this team loses the game
        Team checkTeam = whiteTeam;
        Piece general = blackTeam.getGeneral();
        if (pieceMoved.getTeam() == whiteTeam) {
            checkTeam = blackTeam;
            general = whiteTeam.getGeneral();
        }
        checkPiece = null;
        checkPiece = determineCheck(checkTeam, general);

		// TODO The Tran
		if (!canDefense()) {
			checkmated(pieceMoved.getTeam());
		} else {
			resetAttacker();
		}

        if (checkPiece != null) {
            checkmated(checkTeam);
        } else {
            checkTeam = blackTeam;
            general = whiteTeam.getGeneral();
            if (pieceMoved.getTeam() == whiteTeam) {
                checkTeam = whiteTeam;
                general = blackTeam.getGeneral();
            }
            checkPiece = determineCheck(checkTeam, general);
        }
    }

    private Piece determineCheck(Team team, Piece general) {
        for (Piece p : team.getPieces()) {
            if (p.canCapture(general)) {
                return p;
            }
        }
        Piece g = team.getGeneral();
        if (((General) g.getActualRole()).isGeneralsFaceToFace(general)) {
            return g;
        }
        return null;
    }

	private void resetAttacker() {
		for (Piece p : blackTeam.getPieces()) {
			p.setAttacker(false);
		}
		for (Piece p : whiteTeam.getPieces()) {
			p.setAttacker(false);
		}
	}

	private boolean canDefense() {
		resetAttacker(); 
		
		Team defenseTeam = getActiveTeam();
		for (Piece p : defenseTeam.getPieces()) {
			Role role = p.getCurrentRole();
			if (role == null) {
				role = p.getActualRole();
			}
			for (Point step : role.possibleSteps()) {
				if (role.isValidLogic(step)) {
					// we still have steps to defense
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Show WON dialog
	 * 
	 * @param wonTeam
	 */
    private void checkmated(Team wonTeam) {
        stop(wonTeam.getColor().getDisplayName() + " team has won the game!");
    }

    private void stop(String message) {
        this.setEnabled(false);
        ActionEvent e = new ActionEvent(message, 1, null);
        for (ActionListener l : checkmatedListeners) {
            l.actionPerformed(e);
        }
    }

    private void statusChanged() {
        ActionEvent e = new ActionEvent(enabled, 1, null);
        for (ActionListener l : statusChangedListeners) {
            l.actionPerformed(e);
        }
    }

    public void shutdown(boolean requestFromView) {
        setEnabled(false);
        if (requestFromView) {
            for (ModelActionListener l : modelActionListeners) {
                l.shutdownRequested();
            }
        } else {
            // Request from other side
            for (RemoteActionListener l : remoteActionListeners) {
                l.shutdownRequested();
            }
        }
    }

    public void resign(boolean requestFromView) {
        setEnabled(false);
        if (requestFromView) {
            for (ModelActionListener l : modelActionListeners) {
                l.resigned();
                stop("");
            }
        } else {
            stop("Other player has resigned");
        }
    }

//    public void error(String msg) {
//        ActionEvent e = new ActionEvent(msg, 1, "Error");
//        for (ActionListener l : messageListeners) {
//            l.actionPerformed(e);
//        }
//    }

    public void receivedMessage(String msg) {
        for (RemoteActionListener l : remoteActionListeners) {
            l.messageReceived(msg);
        }
    }

    public void sendMessage(String msg) {
        for (ModelActionListener l : modelActionListeners) {
            l.messageSent(msg);
        }
    }

    public void newGame(ChessType type, Team.TeamColor bottomTeam) {
        for (ModelActionListener l : modelActionListeners) {
            l.newGameCreated(type, bottomTeam);
        }
    }

    public void gameOver(String msg, boolean requestFromView) {
        setEnabled(false);
        if (requestFromView) {
            for (ModelActionListener l : modelActionListeners) {
                l.gameOver(msg);
            }
        }
        
        stop(msg);
    }

    public void loadGame(String fileName) {
        try {
            FileInputStream f = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(f);
            TableDto table = (TableDto) os.readObject();
            os.close();
            gameLoaded(table);
        } catch (Exception e) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, e.toString(), e);
        }
    }

    public void saveGame(String fileName) {
        try {
            FileOutputStream f = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(f);
            os.writeObject(TableDto.toDtoTable(this));
            os.flush();
            os.close();
        } catch (Exception e) {
            Logger.getLogger(Match.class.getName()).severe("Fail to save file");
        }
    }

    public Team getTeam(TeamColor teamColor) {
        if (blackTeam.getColor() == teamColor) {
            return blackTeam;
        }
        return whiteTeam;
    }

    public void receivedError(String msg) {
        for (RemoteActionListener l : remoteActionListeners) {
            l.errorReceived(msg);
        }
    }
}