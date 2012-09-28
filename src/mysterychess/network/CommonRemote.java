package mysterychess.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import mysterychess.network.dto.MoveDto;

/**
 *
 * @author Tin Bui-Huy
 */
public interface CommonRemote extends Remote {

    public void pieceMoved(MoveDto d) throws RemoteException;

    public void message(String string) throws RemoteException;

    public void error(String msg) throws RemoteException;

    public void chat(String msg) throws RemoteException;

    public void resigned() throws RemoteException;

    public void gameOver(String msg) throws RemoteException;

    public void pause() throws RemoteException;

    public void unpause() throws RemoteException;
}
