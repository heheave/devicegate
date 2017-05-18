package devicegate.launch;

/**
 * Created by xiaoke on 17-5-17.
 */
public interface Launch {

    void launch() throws Exception;

    void shutdown();

    /**
     * show the launcher's state
     * 0 is inited
     * 1 is running
     * 2 is stopped
     * @return launcher's state
     */
    int state();
}
