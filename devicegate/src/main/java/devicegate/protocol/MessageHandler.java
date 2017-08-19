package devicegate.protocol;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-8-19.
 */
public interface MessageHandler {

    void messageInHandler(JSONObject jo, AttachInfo attachInfo);

    void messageOutHandler(JSONObject jo, AttachInfo attachInfo);

}
