package devicegate.netty.handler;

import devicegate.conf.Configure;
import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.security.AccessControlException;
import java.util.Map;
import java.util.Set;


/**
 * Created by xiaoke on 17-5-6.
 */
public class SlaveMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(SlaveMessageHandler.class);

    private final SlaveLaunch slaveLaunch;

    private final Configure conf;

    public SlaveMessageHandler(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
        this.conf = slaveLaunch.getConf();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jo = (JSONObject)msg;
        String id = jo.getString(JsonField.DeviceValue.ID);
        if (id != null) {
            String did = jo.getString(JsonField.DeviceValue.ID);
            boolean cnt = jo.containsKey(JsonField.DeviceValue.CNT) ? jo.getBoolean(JsonField.DeviceValue.CNT) : false;
            // check has already existed?
            DeviceCacheInfo dci = slaveLaunch.getDm().get(did);
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
                    checked = true;
                    if (checked) {
                        log.info("checked");
                        dci = slaveLaunch.addChannel(id, ctx.channel());
                        if (dci != null) {
                            dci.bindWithJson(jo);
                        }
                    } else {
                        log.info("unchecked");
                        checked = false;
                    }
                }
                if (checked) {
                    ctx.channel().writeAndFlush(conf.getStringOrElse(V.DEVICE_MSG_ACK, "CNT SUCCESS"));
                } else {
                    ctx.channel().writeAndFlush(conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH")).addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                log.info("data message");
                if (dci != null) {
                    log.info("session found");
                    dci.updateTime();
                    try {
                        slaveLaunch.pushToKafka(dci.decorateJson(jo));
                    } catch (AccessControlException e) {
                        ctx.channel().writeAndFlush(conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH")).addListener(ChannelFutureListener.CLOSE);
                    }
                } else {
                    log.info("session not found");
                    ctx.channel().writeAndFlush(conf.getStringOrElse(V.DEVICE_CNT_NOT_AUTH, "DEVICE NOT AUTH")).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //log.info("Channel is stopped");
        slaveLaunch.removeChannel(ctx.channel());
    }
}
