package mysterychess.network;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import mysterychess.model.ChessType;
import mysterychess.model.Match;
import mysterychess.model.Team.TeamColor;
import mysterychess.network.dto.TableDto;
import mysterychess.util.Util;

/**
 * <code>Server</code> is used to listens for connection from clients. Whenever
 * a new connection from client accepted, a new {@link RequestHandler} is
 * created to handle requests from this client.
 *
 * @author Tin Bui-Huy
 * @version 1.0, 11/09/09
 */
public class RmiServer extends AbstractHost //extends java.rmi.server.UnicastRemoteObject
        implements RemoteServer {

    /** Server listens for connections from clients on this port */
    private int port;
    private Match match;
    private final Chatter chater;
    private Registry registry;
    private ClientCallback client;

    /**
     * Constructs a server with a given database file and listen on a specified
     * port.
     *
     * @param databasePath the path of the data file
     * @param port         the port to listen on
     * @param validator    the validator of the database
     * @throws FileNotFoundException if the data file does not exist
     * @throws IOException           if problem happens while accessing
     *                               file or opening socket
     */
    public RmiServer(Match match, int port, Chatter chater) throws IOException {
        this.port = port;
        this.match = match;
        this.chater = chater;
        super.init(match, chater);
    }

    public void startup() {
        registry();
    }

    private void registry() {
        // We need to set the security manager to the RMISecurityManager
//        System.setSecurityManager(new RMISecurityManager());

        try {
            registry = LocateRegistry.createRegistry(port);
//            registry.rebind(Util.RMI_SERVER_NAME, this);
            UnicastRemoteObject.exportObject(((RemoteServer) this));
            registry.rebind(Util.RMI_SERVER_NAME, this);
        } catch (RemoteException e) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.ALL, "Remote exception" + e);
        }
    }

    public Chatter getChater() {
        return chater;
    }

    public Match getMatch() {
        return match;
    }

    public void registerClientCallback(ClientCallback callback, String clientVersion)
            throws RemoteException {
        if (!clientVersion.equals(Util.getVersion())) {
            String msg = "Version mismatch! "
                    + "Server version: " + Util.getVersion()
                    + ", client version: " + clientVersion;
            throw new RemoteException(msg);
        }
        boolean oldClientAlive = false;
        if (client != null) {
            try {
                oldClientAlive = client.isAlive();
            } catch (Exception ex) {
                oldClientAlive = false;
            }
        }
        if (oldClientAlive) {
            throw new RemoteException("Connection refused. Enough player");
        }
        client = callback;
    }

    public void deregisterClientCallback(ClientCallback callback) {
        client = null;
    }

    public void createTable(ChessType type, TeamColor clientTeam) {
        createNewGame(type, TeamColor.getOtherTeam(clientTeam));
    }

    protected void createNewGame(ChessType type, TeamColor bottomTeam) {
        try {
            Match m = new Match(type, bottomTeam);
            match.setData(type, m.getWhiteTeam(), m.getBlackTeam(), m.getWhiteTeam());
            if (client != null) { 
                client.setTable(TableDto.toDtoTable(match));
            }
        } catch (RemoteException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, "Remote Exception", ex);
        }
    }

    public TableDto joinGame(String clientName) throws RemoteException {
        match.receivedMessage("Guest has joined the game");
        return TableDto.toDtoTable(match);
    }

    public void setClientReady() throws RemoteException {
        ready = true;
        match.setEnabled(true);
    }

    public long getPieceMoveLimitTime() throws RemoteException {
        return Util.PIECE_MOVE_EXPIRE_TIME;
    }

    public long getGameLimitTime() throws RemoteException {
        return Util.GAME_EXPIRE_TIME;
    }

    public void setPieceMoveLimitTime(long time) throws RemoteException {
        Util.PIECE_MOVE_EXPIRE_TIME = time;
    }

    public void setGameLimitTime(long time) throws RemoteException {
        Util.GAME_EXPIRE_TIME = time;
    }

    /**
     * This is called remotely.
     *
     * @param tableDto
     */
    public void loadTable(TableDto tableDto) throws RemoteException {
        try {
            Match m = tableDto.toMatch();
            match.setData(m.getType(), m.getWhiteTeam(),
                    m.getBlackTeam(), m.getActiveTeam());
            match.setActiveTeam(match.getTeam(tableDto.activeTeam));

            // Revert the team for client
            tableDto.myTeam = TeamColor.getOtherTeam(tableDto.myTeam);
            client.setTable(tableDto);
        } catch (RemoteException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, "Remote Exception", ex);
        }
    }

    @Override
    public CommonRemote getOtherSide() {
        return client;
    }
    
    @Override
    public void removeOtherSide() {
        client = null;
    }

    /**
     * No transform needed for Server side,
     * since client already handle that.
     *
     * @param p
     * @return
     */
    @Override
    protected Point transform(Point p) {
        return p;
    }
}
