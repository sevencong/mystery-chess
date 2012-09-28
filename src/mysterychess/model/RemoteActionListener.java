package mysterychess.model;

/**
 * The listener which listens to the actions from remote host.
 *
 * @author Tin Bui-Huy
 */
public interface RemoteActionListener {

    public void errorReceived(String message);

    public void messageReceived(String message);

    public void shutdownRequested();

    public void pause();

    public void unpause();
}
