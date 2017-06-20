package devicegate.netty;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.launch.MasterLaunch;
import devicegate.launch.SlaveLaunch;
import devicegate.netty.handler.MasterHTTPHandler;
import devicegate.netty.handler.ShowHandler;
import devicegate.netty.handler.SlaveInDecoderHandler;
import devicegate.netty.handler.SlaveMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterNettyServer {

    private static Logger log = Logger.getLogger(MasterNettyServer.class);

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workGroup;

    private final Configure conf;

    private final InetSocketAddress bindAddress;

    private final MasterLaunch master;

    private volatile boolean isRunning;

    public MasterNettyServer(MasterLaunch master) {
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        this.master = master;
        this.conf = master.getConf();
        String localHost = conf.getString(V.MASTER_HOST);
        int localPort = conf.getIntOrElse(V.NETTY_MASTER_SERVER_PORT, 9090);
        this.bindAddress = new InetSocketAddress(localHost, localPort);
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;
        final int HOAMaxCntLength = conf.getIntOrElse(V.MASTER_HOA_MAX_CONTEN_LENGTH, 65536);
        try {
            int SO_BACKLOG = conf.getIntOrElse(V.NETTY_SERVER_SO_BACKLOG, 100);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(this.bossGroup, this.workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, SO_BACKLOG)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(HOAMaxCntLength));
                            socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("http-handler", new MasterHTTPHandler(master));
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
