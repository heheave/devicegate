package devicegate.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-5-7.
 */
public class OutEncoderHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String outStr = (String)msg;
        System.out.println(outStr.getClass().getName());
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(outStr.length());
        byteBuf.writeBytes(outStr.getBytes());
        ctx.writeAndFlush(byteBuf, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
