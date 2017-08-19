package devicegate.mqtt;

import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import devicegate.protocol.*;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.security.AccessControlException;

/**
 * Created by xiaoke on 17-5-18.
 */

public class MqttMessageHandler implements MessageHandler{

    private final Logger log = Logger.getLogger(MqttMessageHandler.class);

    private final ProtocolManager pm;

    private final String topicFormat = "MQTT_BACK_%s";

    public MqttMessageHandler(ProtocolManager pm) {
        this.pm = pm;
    }

    public void messageInHandler(JSONObject jo, AttachInfo attachInfo) {
        if (jo != null) {
            SlaveLaunch slaveLaunch = pm.slaveLaunch();
            String id = jo.getString(JsonField.DeviceValue.ID);
            if (id != null) {
                final String did = jo.getString(JsonField.DeviceValue.ID);
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
                        // check user and passwd
                        log.info("cnt not existed, check???");
                        AuthRet authRet = pm.authorize(jo, ProtocolManager.AuthType.CNT);
                        if (authRet.isAuthorized()) {
                            checked = true;
                            dci = slaveLaunch.getDm().addChannel(id, null);
                            if (dci != null) {
                                dci.bindWithJson(jo);
                            }
                        } else {
                            checked = false;
                        }
                    }
                    if (checked) {
                        log.info("checked");
                        JSONObject backInfo = backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_MSG_ACK, "CNT SUCCESS"));
                        pm.messageOut(backInfo, AttachInfo.constantAttachInfo(String.format(topicFormat, did)));
                    } else {
                        log.info("unchecked");
                        JSONObject backInfo = backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH"));
                        pm.messageOut(backInfo, AttachInfo.constantAttachInfo(String.format(topicFormat, did)));
                    }
                } else {
                    log.info("data message");
                    if (dci != null) {
                        log.info("session found");
                        dci.updateTime();
                        try {
                            slaveLaunch.getKafkaSender().pushToKafka(dci.decorateJson(jo));
                        } catch (AccessControlException e) {
                            slaveLaunch.getDm().removeChannel(did);
                        }
                    } else {
                        log.info("session not found");
                        JSONObject backInfo = backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH"));
                        pm.messageOut(backInfo, AttachInfo.constantAttachInfo(String.format(topicFormat, did)));
                    }
                }
            }
        }
    }

    private JSONObject backInfoWrap(String info) {
        return JSONObject.fromObject("{'back':'" + info + "'}");
    }

    public void messageOutHandler(JSONObject jo, AttachInfo attachInfo) {
        log.info("Message " + jo + " send to device on topic " + attachInfo.get());
    }
}
