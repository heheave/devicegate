package devicegate.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-7.
 */
public class OutEncoderHandler extends MessageToByteEncoder{

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        JSONObject jo = (JSONObject)o;
        if (jo != null) {
            byte[] msg = o.toString().getBytes();
            byteBuf.writeInt(msg.length);
            byteBuf.writeBytes(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
