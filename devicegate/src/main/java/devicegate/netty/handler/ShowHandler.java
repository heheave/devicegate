package devicegate.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-5-6.
 */
public class ShowHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(ShowHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("receive message: " + msg);
        ctx.fireChannelRead(msg);
    }
}
