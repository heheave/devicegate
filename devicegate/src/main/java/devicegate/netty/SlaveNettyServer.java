package devicegate.netty;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.netty.handler.SlaveOutEncoderHandler;
import devicegate.netty.handler.SlaveInDecoderHandler;
import devicegate.netty.handler.SlaveMessageHandler;
import devicegate.launch.SlaveLaunch;
import devicegate.protocol.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveNettyServer extends MessageServer{

    private static Logger log = Logger.getLogger(SlaveNettyServer.class);

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workGroup;

    private final Configure conf;

    private final InetSocketAddress bindAddress;

    private volatile MessageHandler nettyHandler;

    private volatile boolean isRunning;

    public SlaveNettyServer(Configure conf) {
        super(conf);
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        this.conf = conf;
        String localHost = conf.getString(V.SLAVE_HOST);
        int localPort = conf.getIntOrElse(V.NETTY_SLAVE_SERVER_PORT, 10000);
        this.bindAddress = new InetSocketAddress(localHost, localPort);
        this.isRunning = false;
    }

    public void start() {
        assert nettyHandler != null : "NettyMessageHandler shouldn't be null";
        isRunning = true;
        try {
            int SO_BACKLOG = conf.getIntOrElse(V.NETTY_SERVER_SO_BACKLOG, 100);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(this.bossGroup, this.workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, SO_BACKLOG)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new SlaveInDecoderHandler());
                            socketChannel.pipeline().addLast((ChannelInboundHandlerAdapter)nettyHandler);
                            socketChannel.pipeline().addLast(new SlaveOutEncoderHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(bindAddress).sync();
            future.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                public void operationComplete(Future<? super Void> future) throws Exception {
                    log.info("Netty server has been shutdown");
                }
            });
            log.info("Netty netty has been started");
        } catch (Exception e) {
            log.error("Start netty server error!!!", e);
        }
    }

    public void stop() {
        isRunning = false;
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void setMessageHandler(MessageHandler mi) {
        nettyHandler = mi;
    }

    @Override
    public void sendMessage(JSONObject jo, AttachInfo attachInfo) throws MessageException {
        if (jo == null || attachInfo == null) {
            throw new MessageException("NullPointerException", jo, attachInfo, false);
        }
        Object getValue = attachInfo.get();
        boolean isClosed = false;
        boolean isSync = false;
        Channel channel;
        if (getValue != null) {
            channel = (Channel)getValue;
        } else {
            channel = ((ChannelAttachInfo)attachInfo).getChannel();
            isClosed = ((ChannelAttachInfo)attachInfo).isClosed();
            isSync = ((ChannelAttachInfo)attachInfo).isSync();
        }
        if (channel != null) {
            try {
                ChannelFuture cf = channel.writeAndFlush(jo.toString());
                if (isClosed) {
                    cf.addListener(ChannelFutureListener.CLOSE);
                }
                if (isSync) {
                    cf.sync();
                }
                if (nettyHandler != null) {
                    nettyHandler.messageOutHandler(jo, attachInfo);
                }
            } catch (Exception e) {
                throw new MessageException("Send message error" + e.getMessage(), jo, attachInfo, false);
            }
        }
    }

    public InetSocketAddress getBindAddress() {
        if (isRunning) {
            return bindAddress;
        } else {
            return null;
        }
    }
}
