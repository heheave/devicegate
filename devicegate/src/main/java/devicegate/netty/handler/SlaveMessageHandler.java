package devicegate.netty.handler;

import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import devicegate.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.security.AccessControlException;

/**
 * Created by xiaoke on 17-5-6.
 */
@ChannelHandler.Sharable
public class SlaveMessageHandler extends ChannelInboundHandlerAdapter implements MessageHandler{

    private static final Logger log = Logger.getLogger(SlaveMessageHandler.class);

    private final ProtocolManager pm;

    public SlaveMessageHandler(ProtocolManager pm) {
        this.pm = pm;
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jo = (JSONObject)msg;
        messageInHandler(jo, AttachInfo.constantAttachInfo(ctx.channel()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //log.info("Channel is stopped");
        pm.slaveLaunch().getDm().removeChannel(ctx.channel());
    }

    public void messageInHandler(JSONObject jo, AttachInfo attachInfo) {
        String id = jo.getString(JsonField.DeviceValue.ID);
        SlaveLaunch slaveLaunch = pm.slaveLaunch();
        if (id != null) {
            String did = jo.getString(JsonField.DeviceValue.ID);
            boolean cnt = jo.containsKey(JsonField.DeviceValue.CNT) ? jo.getBoolean(JsonField.DeviceValue.CNT) : false;
            // check has already existed?
            DeviceCacheInfo dci = slaveLaunch.getDm().get(did);
            final Channel channel = (Channel) attachInfo.get();
            if (cnt) {
                log.info("cnt message");
                // required cnt and has already existed
                boolean checked;
                if (dci != null) {
                    log.info("cnt existed");
                    dci.updateTime();
                    dci.bindWithJson(jo);
                    checked = true;
                } else {
                    // required cnt but not existed, do cnt
                    //check user and passwd
                    log.info("cnt not existed, check???");
                    AuthRet authRet = pm.authorize(jo, ProtocolManager.AuthType.CNT);
                    if (authRet.isAuthorized()) {
                        checked = true;
                        dci = slaveLaunch.getDm().addChannel(id, channel);
                        if (dci != null) {
                            dci.bindWithJson(jo);
                        }
                    } else {
                        checked = false;
                    }
                }
                if (checked) {
                    pm.messageOut(pm.backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_MSG_ACK, "CNT SUCCESS")), attachInfo);
                } else {
                    pm.messageOut(pm.backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_NOT_AUTH, "DEVICE NOT AUTH")),
                            new ChannelAttachInfo(channel, true, false));
                }
            } else {
                log.info("data message");
                AuthRet authRet = pm.authorize(jo, ProtocolManager.AuthType.IN);
                if (dci != null && authRet.isAuthorized()) {
                    log.info("session found");
                    dci.updateTime();
                    try {
                        pm.pushToKafka(dci.decorateJson(jo));
                    } catch (AccessControlException e) {
                        pm.messageOut(pm.backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_NOT_AUTH, "DEVICE NOT AUTH")),
                                new ChannelAttachInfo(channel, true, false));
                    }
                } else {
                    log.info("session not found or error cause by " + authRet.faildReason());
                    pm.messageOut(pm.backInfoWrap(slaveLaunch.getConf().getStringOrElse(V.DEVICE_NOT_AUTH, "DEVICE NOT AUTH")),
                            new ChannelAttachInfo(channel, true, false));
                }
            }
        }
    }

//    private JSONObject backInfoWrap(String info) {
//        return JSONObject.fromObject("{'back':'" + info + "'}");
//    }

    public void messageOutHandler(JSONObject jo, AttachInfo attachInfo) {
        log.info("Message " + jo + " send to device through channel " + attachInfo.get());
    }
}
