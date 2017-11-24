package devicegate.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by xiaoke on 17-5-6.
 */
public class SlaveInDecoderHandler extends ByteToMessageDecoder {

    private int limit;

    private int curidx;

    private byte[] buf;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (buf == null) {
            if (byteBuf.readableBytes() < 4) {
                return;
            }
            limit = byteBuf.readInt();
            curidx = 0;
            buf = new byte[limit];
        }

        int readableBytes = byteBuf.readableBytes();
        int needReadBytes = readableBytes <= limit - curidx ? readableBytes : limit - curidx;
        byteBuf.readBytes(buf, curidx, needReadBytes);
        curidx += needReadBytes;
        if (curidx == limit) {
            ByteBuf byteBuf1 = Unpooled.wrappedBuffer(buf);
            buf = null;
            list.add(byteBuf1);
        }
    }

}
