package simulation.device;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-15.
 */
public interface Device {

    enum TYPE {
        SWITCH, DIGITL, ANALOG
    }

    String id();

    String mtype();

    String dtype();

    String desc();

    String company();

    Location location();

    long dtimestamp();

    JSONObject toJson();
}
