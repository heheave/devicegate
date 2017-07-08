package devicegate.netty.handler;

import devicegate.actor.message.AckMessage;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.conf.JsonField;
import devicegate.launch.MasterLaunch;
import devicegate.manager.MachineCacheInfo;
import devicegate.manager.MachineManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xiaoke on 17-5-28.
 */
public class MasterHTTPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = Logger.getLogger(MasterHTTPHandler.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final MasterLaunch master;

    public MasterHTTPHandler(MasterLaunch master) {
        this.master = master;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext cntx, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            sendError(cntx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (request.getMethod() == HttpMethod.GET) {
            final String uri = request.getUri();
            String sanitizedUri = sanitizeURI(uri);
            if (!"show".equals(sanitizedUri)) {
                sendError(cntx, HttpResponseStatus.FORBIDDEN);
            } else {
                responseShow(cntx, request);
            }
        } else if (request.getMethod() == HttpMethod.POST) {
            final String uri = request.getUri();
            String sanitizedUri = sanitizeURI(uri);
            if (!"ctrl".equals(sanitizedUri)) {
                sendError(cntx, HttpResponseStatus.FORBIDDEN);
            } else {
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
                Map<String, String> datas = new HashMap<String, String>();
                for (InterfaceHttpData ihd : postData) {
                    if (ihd.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attr = (Attribute) ihd;
                        String attrV;
                        try {
                            attrV = attr.getValue();
                            datas.put(attr.getName(), attrV);
                        } catch (IOException ioe) {
                            log.warn("Get attr: " + attr.getName() + " error", ioe);
                        }
                    }
                }
                CtrlRetTuple retTuple = deviceCtrl(datas);
                responseCtrl(cntx, retTuple);
            }
        } else {
            sendError(cntx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    private static final class CtrlRetTuple {
        public final String  did;

        public final JsonField.DeviceCtrl.CtrlState ctrlState;

        public CtrlRetTuple(String  did, JsonField.DeviceCtrl.CtrlState ctrlState) {
            this.did = did;
            this.ctrlState = ctrlState;
        }
    }

    private CtrlRetTuple deviceCtrl(Map<String, String> datas) {
        String did = datas.get(JsonField.DeviceCtrl.ID);
        if (did != null) {
            JSONObject jo = JSONObject.fromObject(datas);
            MachineCacheInfo mci = master.getMm().get(did);
            if (mci != null && mci.getIsa() != null) {
                try {
                    Msg ctrlMessage = MessageFactory.getMessage(Msg.TYPE.CTRL, jo);
                    Object ack = master.getMasterActor().sendToSlaveWithReply(ctrlMessage, mci.getIsa());
                    AckMessage ackMessage = (AckMessage)ack;
                    String ackInfo = ackMessage.getAckInfo();
                    if (ackInfo != null) {
                        log.warn("Ctrl device error" + ackInfo);
                        return new CtrlRetTuple(did, JsonField.DeviceCtrl.CtrlState.CTRL_FAILED);
                    }
                    return new CtrlRetTuple(did, JsonField.DeviceCtrl.CtrlState.CTRL_SUCCEEDED);
                } catch (Exception e) {
                    log.warn(String.format("Ctrl device: %s on machine(%s:%d) error",
                            did, mci.getIsa().getAddress().getCanonicalHostName(), mci.getIsa().getPort()), e);
                    return new CtrlRetTuple(did, JsonField.DeviceCtrl.CtrlState.UNCERTAIN);
                }
            } else {
                return new CtrlRetTuple(did, JsonField.DeviceCtrl.CtrlState.NO_DEVICE);
            }
        } else {
            return new CtrlRetTuple(null, JsonField.DeviceCtrl.CtrlState.DID_NULL);
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
        Map<String, List<String>> infos = master.getMm().showAllAddress();
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

    private void responseCtrl(ChannelHandlerContext cntx, CtrlRetTuple tuple) {
        FullHttpMessage response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/json; charset=UTF-8");
        JSONObject jo = new JSONObject();
        String did = tuple.did;
        if (did != null) {
            jo.put(JsonField.DeviceValue.ID, did);
        }
        jo.put(JsonField.DeviceCtrl.RET, tuple.ctrlState.desc());
        StringBuffer buf = new StringBuffer(jo.toString());
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