package devicegate.actor.message;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-16.
 */
public class AddIdMessage extends Message {

    public AddIdMessage() {
        super(TYPE.ADDID);
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
