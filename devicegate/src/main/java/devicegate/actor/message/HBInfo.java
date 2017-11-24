package devicegate.actor.message;

import devicegate.conf.JsonField;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-11-13.
 */
public class HBInfo {

    private final float cpu;

    private final float mem;

    private final float io;

    private final float net;

    private final int msgNum;

    private final long msgBytes;

    private final int msgNumT;

    private final long msgBytesT;

    private final int cntNum;

    private final long time;

    public HBInfo(float cpu, float mem, float io, float net, int msgNum, long msgBytes, int msgNumT, long msgBytesT, int cntNum) {
        this.cpu = cpu;
        this.mem = mem;
        this.io = io;
        this.net = net;
        this.msgNum = msgNum;
        this.msgBytes = msgBytes;
        this.msgNumT = msgNumT;
        this.msgBytesT = msgBytesT;
        this.cntNum = cntNum;
        this.time = System.currentTimeMillis();
    }

    public HBInfo(JSONObject jo) {
        this.cpu = jo.containsKey(JsonField.MSG.CU) ? (float)jo.getDouble(JsonField.MSG.CU) : 0.0f;
        this.mem = jo.containsKey(JsonField.MSG.MU) ? (float)jo.getDouble(JsonField.MSG.MU) : 0.0f;
        this.io = jo.containsKey(JsonField.MSG.IU) ? (float)jo.getDouble(JsonField.MSG.IU) : 0.0f;
        this.net = jo.containsKey(JsonField.MSG.NU) ? (float)jo.getDouble(JsonField.MSG.NU) : 0.0f;
        this.msgNum = jo.containsKey(JsonField.MSG.MN) ? jo.getInt(JsonField.MSG.MN) : 0;
        this.msgBytes = jo.containsKey(JsonField.MSG.MB) ? jo.getLong(JsonField.MSG.MB) : 0L;
        this.msgNumT = jo.containsKey(JsonField.MSG.MNT) ? jo.getInt(JsonField.MSG.MNT) : 0;
        this.msgBytesT = jo.containsKey(JsonField.MSG.MBT) ? jo.getLong(JsonField.MSG.MBT) : 0L;
        this.cntNum = jo.containsKey(JsonField.MSG.CN) ? jo.getInt(JsonField.MSG.CN) : 0;
        this.time = jo.containsKey(JsonField.MSG.HT) ? jo.getLong(JsonField.MSG.HT) : 0L;
    }

    public float getCpu() {
        return cpu;
    }

    public float getMem() {
        return mem;
    }

    public float getIo() {
        return io;
    }

    public float getNet() {
        return net;
    }

    public long getMsgNum() {
        return msgNum;
    }

    public long getMsgBytes() {
        return msgBytes;
    }

    public long getCntNum() {
        return cntNum;
    }

    public long getTime() {
        return time;
    }

    public int getMsgNumT() {
        return msgNumT;
    }

    public long getMsgBytesT() {
        return msgBytesT;
    }
}
