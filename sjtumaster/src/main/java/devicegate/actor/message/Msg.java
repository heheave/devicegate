package devicegate.actor.message;

import devicegate.conf.JsonField;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xiaoke on 17-5-16.
 */
public abstract class Msg implements Serializable{

    public enum TYPE {
        HB, ACK, TELLME, ADDID, RMID, STASLV, STPSLV
    }

    protected final TYPE type;

    protected JSONObject data;

    public Msg(TYPE type, JSONObject jo) {
        this.type = type;
        this.data = jo;
    }

    public Msg(TYPE type) {
        this(type, new JSONObject());
    }

    public TYPE type() {
        return type;
    }

    public JSONObject data() {
        return data;
    }

    public void setData(JSONObject jo) {
        if (jo != null) {
            data.clear();
            @SuppressWarnings("unchecked")
            Iterator<Object> iter = jo.keys();
            while (iter.hasNext()) {
                Object key = iter.next();
                data.put(key, jo.get(key));
            }
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

    public void setId(String id) {
        data.put(JsonField.MSG.ID, id);
    }

    public String getId() {
        if(data.containsKey(JsonField.MSG.ID)) {
            return data.getString(JsonField.MSG.ID);
        } else {
            return null;
        }
    }

    public void setAddress(InetSocketAddress isa) {
        if (isa != null){
            data.put(JsonField.MSG.HOST, isa.getAddress().getCanonicalHostName());
            data.put(JsonField.MSG.PROT, isa.getPort());
        }
    }

    public InetSocketAddress getAddress() {
        if (data.containsKey(JsonField.MSG.HOST)) {
            String host = data.getString(JsonField.MSG.HOST);
            int port = data.getInt(JsonField.MSG.PROT);
            return new InetSocketAddress(host, port);
        } else {
            return null;
        }
    }
}
