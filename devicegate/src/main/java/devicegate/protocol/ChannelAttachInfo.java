package devicegate.protocol;

import io.netty.channel.Channel;

/**
 * Created by xiaoke on 17-8-19.
 */

public class ChannelAttachInfo extends AttachInfo {

    private final Channel channel;

    private final boolean closed;

    private final boolean sync;

    public ChannelAttachInfo(Channel channel, boolean closed, boolean sync) {
        this.channel = channel;
        this.closed = closed;
        this.sync = sync;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Object get(int index) {
        throw new UnsupportedOperationException("Not support get by index");
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isClosed() {
        return  closed;
    }

    public boolean isSync() {
        return sync;
    }
}
