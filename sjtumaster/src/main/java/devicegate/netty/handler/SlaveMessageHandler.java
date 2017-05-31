package devicegate.netty.handler;

import devicegate.conf.JsonField;
import devicegate.launch.SlaveLaunch;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;


/**
 * Created by xiaoke on 17-5-6.
 */
public class SlaveMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(SlaveMessageHandler.class);

    private final SlaveLaunch slaveLaunch;

    public SlaveMessageHandler(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jo = (JSONObject)msg;
        String id = jo.getString(JsonField.DeviceValue.ID);
        if (id != null) {
            slaveLaunch.addChannel(id, ctx.channel());
            slaveLaunch.pushToKafka(jo);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        slaveLaunch.removeChannel(ctx.channel());
    }
}
