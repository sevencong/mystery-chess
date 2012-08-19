package mysterychess.model;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import mysterychess.model.Team.TeamColor;

/**
 *
 * @author Tin Bui-Huy
 */
public abstract class Role implements Serializable {

    Piece myPiece;
    PieceName name;

    public Role(PieceName name) {
        this.name = name;
    }

    public void setMyPiece(Piece myPiece) {
        this.myPiece = myPiece;
    }

    public PieceName getName() {
        return name;
    }

    public void setName(PieceName name) {
        this.name = name;
    }

    public boolean move(Point toPosition, boolean validated) {
        Point oldPos = myPiece.getPosition();
        if (!isValidLogic(toPosition)) {
			return false;
		}
        if (validated) {
            Piece captured = myPiece.getTeam().getMatch().getPieceAt(toPosition);
            if (captured != null) {
                capturePieceAt(toPosition);
            } else {
                moveTo(toPosition);
            }
            return true;
        } else {
            if (!isValidPosition(toPosition)) {
                return false;
            }

            boolean moved = move(toPosition);
            if (moved) {
                myPiece.getTeam().getMatch().pieceMoved(oldPos, toPosition);
            }
            return moved;
        }
    }

    public abstract boolean move(Point toPosition);

    protected void moveTo(Point position) {
        changePosition(position);
    }

    private void changePosition(Point position) {
        myPiece.setPosition(position);
        if (!myPiece.isTurnedUp()) {
            turnUp();
        }
    }

    private void turnUp() {
        myPiece.setTurnedUp(true);
        myPiece.setCurrentRole(myPiece.getActualRole());
        myPiece.setActualRole(null);
    }

    protected void capturePieceAt(Point position) {
        myPiece.getTeam().getMatch().capturePieceAt(
                position, myPiece.getTeam());
        changePosition(position);
    }

    /**
     * Checks if the position is out of the table.
     * 
     * @param position
     * @return
     */
    protected boolean isValidPosition(Point position) {
        if (position.x < 0 || position.y < 0
                || position.x > 8 || position.y > 9) {
            return false;
        }
        return true;
    }

    public boolean canCapture(Piece p) {
        return isPossiblePoint(p.getPosition());
    }

    protected abstract boolean isPossiblePoint(Point position);
    
    /**
	 * Get all possible steps of the role The Tran
	 * 
	 * @return Empty list if no possible steps found
	 */
	public abstract List<Point> possibleSteps();
	
    /**
	 * My team's piece is at the toPosition.<br/>
	 * We can not attack my team, of course
	 * 
	 * @param toPosition
	 * @return
	 */
	protected boolean isDuplicated(Point toPosition) {
		Piece capturedPie = myPiece.getTeam().getPieceAt(toPosition);
		return capturedPie != null;
	}
	
    /**
	 * The team can not make the move if its general is captured
	 * 
	 * @param newPosition
	 * @return
	 */
    public boolean isValidLogic(Point newPosition) {
    	Match m = myPiece.getTeam().getMatch();
		TeamColor myColor = myPiece.getTeam().getColor();
		Team otherTeam = (myColor == TeamColor.WHITE) ? m.getBlackTeam() : m.getWhiteTeam();
		
		Piece general = myPiece.getTeam().getGeneral();
		Point old = myPiece.getPosition();
		myPiece.setPosition(newPosition);
		
		Piece dummy = otherTeam.getPieceAt(newPosition);
		if (dummy != null) {
			dummy.setEnable(false);
		}
		
		boolean valid = true;
		for (Piece p : otherTeam.getPieces()) {
			if (p.isEnable() && p.canCapture(general)) {
				myPiece.setPosition(old);
				if (dummy != null) {
					dummy.setEnable(true);
				}
				p.setAttacker(true);
				valid = false;
			}
		}
		myPiece.setPosition(old);
		if (dummy != null) {
			dummy.setEnable(true);
		}
		return valid;
    }
}
