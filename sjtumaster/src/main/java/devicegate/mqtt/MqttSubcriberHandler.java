package devicegate.mqtt;

import devicegate.launch.SlaveLaunch;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-5-18.
 */
public class MqttSubcriberHandler implements MqttHandler{

    private final Logger log = Logger.getLogger(MqttSubcriberHandler.class);

    private final SlaveLaunch slaveLaunch;

    public MqttSubcriberHandler(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
    }

    public void messageIn(String topic, byte[] bytes) {
        if (bytes != null) {
            JSONObject msg = null;
            try {
                msg = JSONObject.fromObject(new String(bytes));
            } catch (Exception e) {
                log.error("Illegal message format", e);
            }
            if (msg != null) {
                String id = msg.getString("id");
                if (id != null) {
                    slaveLaunch.addChannel(id, null);
                    slaveLaunch.pushToKafka(msg);
                }
            }
        } else {
            log.warn("Message is null");
        }
    }
}
