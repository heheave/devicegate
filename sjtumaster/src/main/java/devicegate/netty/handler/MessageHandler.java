package devicegate.netty.handler;

import devicegate.slave.SlaveLaunch;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-6.
 */
public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final SlaveLaunch slaveLaunch;

    public MessageHandler(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jo = (JSONObject)msg;
        String id = jo.getString("id");
        if (id != null) {
            slaveLaunch.addChannel(id, ctx.channel());
            slaveLaunch.pushToKafka(jo);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
