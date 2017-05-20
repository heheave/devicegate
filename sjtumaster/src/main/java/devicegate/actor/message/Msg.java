package devicegate.actor.message;

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
        data.put("id", id);
    }

    public String getId() {
        if(data.containsKey("id")) {
            return data.getString("id");
        } else {
            return null;
        }
    }

    public void setAddress(InetSocketAddress isa) {
        if (isa != null){
            data.put("host", isa.getAddress().getCanonicalHostName());
            data.put("port", isa.getPort());
        }
    }

    public InetSocketAddress getAddress() {
        if (data.containsKey("host")) {
            String host = data.getString("host");
            int port = data.getInt("port");
            return new InetSocketAddress(host, port);
        } else {
            return null;
        }
    }
}
