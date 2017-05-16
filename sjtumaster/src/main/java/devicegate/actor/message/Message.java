package devicegate.actor.message;

import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xiaoke on 17-5-16.
 */
abstract class Message implements Serializable{

    enum TYPE{
        ACK, ADDID, RMID
    }

    protected final TYPE type;

    protected JSONObject data;

    public Message(TYPE type, JSONObject jo) {
        this.type = type;
        this.data = jo;
    }

    public Message(TYPE type) {
        this(type, new JSONObject());
    }

    public TYPE type() {
        return type;
    }

    public JSONObject data() {
        return data;
    }

    public void setData(JSONObject jo) {
        data.clear();
        for(Object entry: jo.entrySet()) {
            Map.Entry<Object, Object> e = (Map.Entry<Object, Object>)entry;
            data.put(e.getKey(), e.getValue());
        }
    }

    public void setData(String joStr) {
        if (joStr != null) {
            try {
                setData(JSONObject.fromObject(joStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setData(byte[] bytes) {
        if (bytes != null) {
            setData(new String(bytes));
        }
    }
}
