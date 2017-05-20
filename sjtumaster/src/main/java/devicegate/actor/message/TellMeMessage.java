package devicegate.actor.message;

import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by xiaoke on 17-5-19.
 */
public class TellMeMessage extends Msg{

    public TellMeMessage() {
        super(TYPE.TELLME);
    }

    public void setIds(Collection<String> ids) {
        if (!ids.isEmpty()) {
            data.put("ids", JSONArray.fromObject(ids));
        }
    }

    public List<String> getIds() {
        if (data.containsKey("ids")) {
            JSONArray ids = JSONArray.fromObject(data.get("ids"));
            List<String> res = new ArrayList<String>(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                res.add((String)ids.get(i));
            }
            return res;
        }
        return null;
    }
}
