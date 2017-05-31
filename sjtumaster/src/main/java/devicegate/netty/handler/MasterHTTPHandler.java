package devicegate.netty.handler;

import devicegate.manager.MachineManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoke on 17-5-28.
 */
public class MasterHTTPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final MachineManager mm = MachineManager.getInstance();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    protected void channelRead0(ChannelHandlerContext cntx, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            sendError(cntx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (request.getMethod() != HttpMethod.GET) {
            sendError(cntx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.getUri();
        String sanitizedUri = sanitizeURI(uri);
        if (!sanitizedUri.equals("show")) {
            sendError(cntx, HttpResponseStatus.FORBIDDEN);
        } else {
            responseShow(cntx, request);
        }
    }

    private void responseShow(ChannelHandlerContext cntx, FullHttpRequest request) {
        FullHttpMessage response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuffer buf = new StringBuffer();
        buf.append("<!DOCTYPE html> \r\n");
        buf.append("<html><head><title>");
        buf.append("Devices on Machines");
        buf.append("</title></head><body>\r\n");
        buf.append("<h1>网关集群设备连接情况:</h1>\r\n");
        Map<String, List<String>> infos = mm.showAllAddress();
        for (Map.Entry<String, List<String>> entry: infos.entrySet()) {
            Iterator<String> iter = entry.getValue().iterator();
            buf.append("<table style='margin-top:30px;width:60%;' border='1' align='left'>\r\n");
            String timeVersion = iter.next();
            String time;
            try {
                Date d = new Date(Long.parseLong(timeVersion));
                time = sdf.format(d);
            } catch (Exception e) {
                time = null;
            }
            String title = "Slave(" + entry.getKey() + ")'s latest heartbeat time is: " + time;
            buf.append("<tr style='color:blue'><th colspan='2'>");
            buf.append(title);
            buf.append("</th></tr>");
            if (iter.hasNext()) {
                buf.append("<tr style='color:green'><th>设备号</th><th>协议</th></tr>");
                while (iter.hasNext()) {
                    String id = iter.next();
                    String[] tmp = id.split(",", 2);
                    buf.append("<tr style='color:green'><th>");
                    buf.append(tmp[0]);
                    buf.append("</th><th>");
                    buf.append(tmp[1]);
                    buf.append("</th></tr>");
                }
            } else {
                buf.append("<tr style='color:red'><th colspan='2'>");
                buf.append("No device is connecting !!!");
                buf.append("</th></tr>");
            }
            buf.append("</table><br/>\r\n");
            entry.getValue().clear();
        }
        buf.append("</body></html>\r\n");
        infos.clear();
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        cntx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String sanitizeURI(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e0) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error("Not supported encoding beyond uft-8 and iso-8859-1");
            }
        }

        return uri.substring(uri.lastIndexOf("/") + 1, uri.length());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}