package devicegate.manager;

import devicegate.conf.JsonField;
import devicegate.util.SessionIdGenUtil;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;

import java.util.Map;
import java.util.Properties;

/**
 * Created by xiaoke on 17-5-18.
 */
public class DeviceCacheInfo {

    // 0 is tcp
    // 1 is mqtt

    private final String did;

    private final int protocolType;

    private final long period;

    private final Properties prop;

    private volatile Channel channel;

    private volatile long timeversion;

    public DeviceCacheInfo(String did, Channel channel, long period) {
        this.did = did;
        this.channel = channel;
        this.period = period;
        this.timeversion = System.currentTimeMillis() + period;
        this.protocolType = (channel == null) ? 1 : 0;
        this.prop = new Properties();
    }

    public int protocol() {
        return protocolType;
    }

    public String getDid() {
        return did;
    }

    public Properties getProp() {
        return prop;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isExpired(long timeversion) {
        if (this.timeversion < timeversion) {
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public void updateTime(long timeversion) {
        this.timeversion = timeversion + period;
    }

    public void updateTime() {
        updateTime(System.currentTimeMillis());
    }

    public JSONObject decorateJson(JSONObject jo) {
        if (jo.containsKey(JsonField.DeviceValue.USER)) {
            jo.remove(JsonField.DeviceValue.USER);
        }
        if (jo.containsKey(JsonField.DeviceValue.PASSWD)) {
            jo.remove(JsonField.DeviceValue.PASSWD);
        }
        for (Map.Entry<Object, Object> entry: getProp().entrySet()) {
            jo.put(entry.getKey(), entry.getValue());
        }
        return jo;
    }

    public void bindWithJson(JSONObject jo) {
        if (jo.containsKey(JsonField.DeviceValue.ID)) jo.remove(JsonField.DeviceValue.ID);
        if (jo.containsKey(JsonField.DeviceValue.CNT)) jo.remove(JsonField.DeviceValue.CNT);
        if (jo.containsKey(JsonField.DeviceValue.USER)) jo.remove(JsonField.DeviceValue.USER);
        if (jo.containsKey(JsonField.DeviceValue.PASSWD)) jo.remove(JsonField.DeviceValue.PASSWD);
        for (Object key: jo.keySet()) {
            getProp().put(key, jo.get(key));
        }
    }

}
