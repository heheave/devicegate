package devicegate.mqtt;

import devicegate.conf.Configure;
import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import io.netty.channel.ChannelFutureListener;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.security.AccessControlException;

/**
 * Created by xiaoke on 17-5-18.
 */
public class MqttMessageHandler implements MqttHandler{

    private final Logger log = Logger.getLogger(MqttMessageHandler.class);

    private final SlaveLaunch slaveLaunch;

    private final Configure conf;

    private final String topicFormat = "device_back_%s";

    public MqttMessageHandler(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
        this.conf = slaveLaunch.getConf();
    }

    public void messageIn(MqttProxyClient client, String topic, byte[] bytes) {
        if (bytes != null) {
            JSONObject jo = null;
            try {
                jo = JSONObject.fromObject(new String(bytes));
            } catch (Exception e) {
                log.error("Illegal message format", e);
            }
            if (jo != null) {
                String id = jo.getString(JsonField.DeviceValue.ID);
                if (id != null) {
                    String did = jo.getString(JsonField.DeviceValue.ID);
                    boolean cnt = jo.containsKey(JsonField.DeviceValue.CNT) ? jo.getBoolean(JsonField.DeviceValue.CNT) : false;
                    // check has already existed?
                    DeviceCacheInfo dci = slaveLaunch.getDm().get(did);
                    if (cnt) {
                        log.info("cnt message");
                        // required cnt and has already existed
                        boolean checked = false;
                        if (dci != null) {
                            log.info("cnt existed");
                            dci.updateTime();
                            dci.bindWithJson(jo);
                        } else {
                            // required cnt but not existed, do cnt
                            //check user and passwd
                            log.info("cnt not existed, check???");
                            checked = true;
                            if (checked) {
                                dci = slaveLaunch.addChannel(id, null);
                                if (dci != null) {
                                    dci.bindWithJson(jo);
                                }
                            } else {
                                checked = false;
                            }
                        }
                        if (checked) {
                            log.info("checked");
                            client.pub(String.format(topicFormat, did), conf.getStringOrElse(V.DEVICE_MSG_ACK, "CNT SUCCESS"));
                        } else {
                            log.info("unchecked");
                            client.pub(String.format(topicFormat, did), conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH"));
                        }
                    } else {
                        log.info("data message");
                        if (dci != null) {
                            log.info("session found");
                            dci.updateTime();
                            try {
                                slaveLaunch.pushToKafka(dci.decorateJson(jo));
                            } catch (AccessControlException e) {
                                client.pub(String.format(topicFormat, did), conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH"));
                                slaveLaunch.removeChannel(did);
                            }
                        } else {
                            log.info("session not found");
                            client.pub(String.format(topicFormat, did), conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH"));
                        }
                    }
                }
            }
        }
    }
}
