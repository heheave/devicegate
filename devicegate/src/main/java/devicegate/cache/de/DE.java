package devicegate.cache.de;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-11-23.
 */
public class DE {

    private final int uid;
    private final int aid;
    private final int sid;
    private final int pid;
    private final String pcalculate;
    private final String poutunit;
    private final String pavg;

    public DE(JSONObject jo) {
        uid = jo.containsKey("uid") ? 0: jo.getInt("uid");
        aid = jo.containsKey("aid") ? 0: jo.getInt("aid");
        sid = jo.containsKey("sid") ? 0: jo.getInt("sid");
        pid = jo.containsKey("pid") ? 0: jo.getInt("pid");
        pcalculate = jo.containsKey("pcalculate") ? null: jo.getString("pcalculate");
        poutunit = jo.containsKey("poutunit") ? null: jo.getString("poutunit");
        pavg = jo.containsKey("pavg") ? null: jo.getString("pavg");
    }

    public int getUid() {
        return uid;
    }

    public int getAid() {
        return aid;
    }

    public int getSid() {
        return sid;
    }

    public int getPid() {
        return pid;
    }

    public String getPcalculate() {
        return pcalculate;
    }

    public String getPoutunit() {
        return poutunit;
    }

    public String getPavg() {
        return pavg;
    }

}
