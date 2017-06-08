package devicegate.netty;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.netty.handler.OutEncoderHandler;
import devicegate.netty.handler.SlaveInDecoderHandler;
import devicegate.netty.handler.SlaveMessageHandler;
import devicegate.netty.handler.ShowHandler;
import devicegate.launch.SlaveLaunch;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveNettyServer {

    private static Logger log = Logger.getLogger(SlaveNettyServer.class);

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workGroup;

    private final Configure conf;

    private final InetSocketAddress bindAddress;

    private final SlaveLaunch belongsToLaunch;

    private volatile boolean isRunning;

    public SlaveNettyServer(SlaveLaunch slaveLaunch, Configure conf) {
        this.belongsToLaunch = slaveLaunch;
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        this.conf = conf;
        String localHost = conf.getString(V.SLAVE_HOST);
        int localPort = conf.getIntOrElse(V.NETTY_SLAVE_SERVER_PORT, 10000);
        this.bindAddress = new InetSocketAddress(localHost, localPort);
        this.isRunning = false;
    }

    public void start() {
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
                            socketChannel.pipeline().addLast(new SlaveMessageHandler(belongsToLaunch));
                            socketChannel.pipeline().addLast(new OutEncoderHandler());
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

    public InetSocketAddress getBindAddress() {
        if (isRunning) {
            return bindAddress;
        } else {
            return null;
        }
    }
}
