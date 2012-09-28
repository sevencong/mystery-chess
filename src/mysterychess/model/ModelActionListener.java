package mysterychess.model;

import java.awt.Point;
import mysterychess.network.dto.TableDto;

/**
 * The listener which listens to the actions in model.
 *
 * @author Tin Bui-Huy
 */
public interface ModelActionListener {

    public void pieceMoved(Point from, Point to);

    public void newGameCreated(ChessType type, Team.TeamColor bottomTeam);

    public void gameLoaded(TableDto t);

    public void messageSent(String msg);

    public void errorSent(String msg);

    public void shutdownRequested();

    public void resigned();

    public void gameOver(String msg);
    
    public void pause();
    
    public void unpause();
    
}
