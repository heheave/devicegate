package devicegate.manager;

import io.netty.channel.Channel;

/**
 * Created by xiaoke on 17-5-18.
 */
public class DeviceCacheInfo {

    private final long period;

    // 0 is tcp
    // 1 is mqtt
    private final int cacheType;

    private volatile Channel channel;

    private volatile long timeversion;

    public DeviceCacheInfo(Channel channel, long timeversion, long period) {
        this.channel = channel;
        this.timeversion = timeversion;
        this.period = period;
        this.cacheType = (channel == null) ? 1 : 0;
    }

    public DeviceCacheInfo(Channel channel, long period) {
        this(channel, System.currentTimeMillis(), period);
    }

    public DeviceCacheInfo(long period) {
        this(null, period);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isExpired(long timeversion) {
        if (this.timeversion + period < timeversion) {
            return true;
        }
        return false;
    }

    public int protocol() {
        return cacheType;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public void updateTime(long timeversion) {
        this.timeversion = timeversion;
    }

    public void updateTime() {
        updateTime(System.currentTimeMillis());
    }
}
