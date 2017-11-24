package devicegate.manager;

import devicegate.conf.JsonField;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xiaoke on 17-5-18.
 */
public class DeviceCacheInfo {

    public enum Protocol {
        TCP, MQTT
    }
    // 0 is tcp
    // 1 is mqtt

    private final String did;

    private final Protocol protocolType;

    private final long period;

    private final Properties prop;

    private volatile Channel channel;

    private final long firstTimestamp;

    private final NLastQueue<Long> lastNTimestamp;

    private final AtomicInteger msgNum = new AtomicInteger(0);

    private final AtomicLong msgBytes = new AtomicLong(0L);

    private volatile JSONObject cacheJo;

    private final NLastQueue.CalculateOp<Long> timestampSumOp = new NLastQueue.CalculateOp<Long>() {
        public Long eval(Long a, Long b) {
            return (a == null ? 0 : a) + (b == null ? 0 : b);
        }
    };

    public DeviceCacheInfo(String did, Channel channel, long period) {
        this.did = did;
        this.channel = channel;
        this.period = period;
        long timeNow = System.currentTimeMillis();
        this.firstTimestamp = timeNow;
        this.lastNTimestamp = new NLastQueue<Long>(3);
        this.lastNTimestamp.in(timeNow);
        this.protocolType = (channel == null) ? Protocol.MQTT : Protocol.TCP;
        this.prop = new Properties();
    }

    public Protocol protocol() {
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

    public long getAvgInterval() {
        int size = lastNTimestamp.size();
        if (size <= 1) {
            return 0;
        } else {
            long cur = lastNTimestamp.cur();
            long tail = lastNTimestamp.tail();
            return (cur - tail) / (size - 1);
        }
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isExpired(long timeversion) {
        long latestTime = lastNTimestamp.cur();
        if (latestTime + period < timeversion) {
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public void updateTime(long timeversion) {
        this.lastNTimestamp.in(timeversion);
    }

    public void updateTime() {
        updateTime(System.currentTimeMillis());
    }

    public void msgStaticUp(JSONObject jo) {
        this.updateTime();
        this.cacheJo = jo;
        this.msgNum.addAndGet(1);
        this.msgBytes.addAndGet(jo.toString().length());
    }

    public JSONObject decorateJson(JSONObject jo) {
        if (jo.containsKey(JsonField.DeviceValue.USER)) {
            jo.remove(JsonField.DeviceValue.USER);
        }
        if (jo.containsKey(JsonField.DeviceValue.PASSWD)) {
            jo.remove(JsonField.DeviceValue.PASSWD);
        }
        jo.put(JsonField.DeviceValue.PTIMESTAMP, System.currentTimeMillis());
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

    public JSONObject packageInfo() {
        JSONObject jo = new JSONObject();
        jo.put("protocolType", protocol().name());
        jo.put("firstTime", firstTimestamp);
        jo.put("lastTime", lastNTimestamp.cur());
        jo.put("interval", getAvgInterval());
        jo.put("msgNum", msgNum.get());
        jo.put("msgByte", msgBytes.get());
        JSONObject tmp = cacheJo;
        Map<String, String> msgAttr = new HashMap<String, String>();
        if (tmp != null) {
            for (Object key: tmp.keySet()) {
                String keyStr = (String)key;
                if (JsonField.DeviceValue.PTIMESTAMP.equals(keyStr)) {
                    continue;
                }
                Object value = tmp.get(keyStr);
                if (value instanceof Number) {
                    msgAttr.put(keyStr, "NUMBER");
                } else {
                    msgAttr.put(keyStr, value.getClass().getSimpleName().toUpperCase());
                }
            }
        }
        jo.put("msgAttr", msgAttr);
        return jo;
    }

}
