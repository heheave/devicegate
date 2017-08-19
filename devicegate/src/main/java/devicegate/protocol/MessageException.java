package devicegate.protocol;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-8-19.
 */
public class MessageException extends Exception {

    private final String reason;

    private final JSONObject jo;

    private final AttachInfo attachInfo;

    private final long timestamp;

    private boolean needTackle;

    public MessageException(String reason, JSONObject jo, AttachInfo attachInfo, boolean needReTackle) {
        this.jo = jo;
        this.reason = reason;
        this.attachInfo = attachInfo;
        this.timestamp = System.currentTimeMillis();
        this.needTackle = needReTackle;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JSONObject getJo() {
        return jo;
    }

    public AttachInfo getAttachInfo() {
        return attachInfo;
    }

    public boolean isNeedTackle() {
        return needTackle;
    }

    @Override
    public String toString() {
        return "MessageException{" +
                "reason='" + reason + '\'' +
                ", jo=" + jo +
                ", attachInfo=" + attachInfo +
                ", timestamp=" + timestamp +
                ", needTackle=" + needTackle +
                '}';
    }
}
