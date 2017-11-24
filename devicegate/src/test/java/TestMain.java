import devicegate.cache.cacheClient.LocalCacheService;
import devicegate.cache.de.DEDeserEntrier;
import devicegate.conf.V;
import org.apache.log4j.PropertyConfigurator;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-6-7.
 */
public class TestMain {
    public static void main(String[] args) {
        PropertyConfigurator.configure(V.LOG_PATH);
//        KafkaSendPermission p1 = new KafkaSendPermission("SWITCH", "send");
//        KafkaSendPermission p2 = new KafkaSendPermission("SWITCH", "SWITCH-213", "send");
//        System.out.println(p1.hashCode() + "-->" + p2.hashCode());
//        System.out.println(p1.equals(p2));
//        System.out.println(p2.equals(p1));
//        System.out.println(p1.implies(p2));
//        System.out.println(p2.implies(p1));
//        Socket socket = new Socket();
//        try {
//            socket.connect(new InetSocketAddress("localhost", 20000));
//            JSONObject jo = new JSONObject();
//            jo.put("type", "rdd");
//            jo.put("id", "DIGITL-ABC010");
//            //jo.put("query", "select * from deviceValue where did = 'DIGITL-ABC002' and ptimestamp % 1000000 < 1000");
//            //int a = 1000000;
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
////            long btime = -1;
////            long etime = -1;
////            try {
////                btime = sdf.parse("2017-06-23 10").getTime();
////                etime = sdf.parse("2017-06-23 12").getTime();
////            } catch (ParseException e) {
////                e.printStackTrace();
////            }
////            jo.put("btime", btime);
////            jo.put("etime", etime);
////            System.out.println(etime - btime);
////            System.out.println((etime - btime) / 10);
////            jo.put("interval", (etime - btime) / 1000);
////            jo.put("delta", 15 * 1000);
//            //jo.getInt("interval");
//            jo.put("desc", "pm2.5");
//            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//            byte[] write = jo.toString().getBytes();
//            dos.write(write, 0, write.length);
//            dos.flush();
//            socket.shutdownOutput();
//            DataInputStream dis = new DataInputStream(socket.getInputStream());
//            byte[] b = new byte[1024];
//            int rlen = 0;
//            while ((rlen = dis.read(b, 0, b.length)) != -1) {
//                System.out.println(new String(b, 0, rlen));
//            }
//            dis.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }




//        Map<String, String> map = new HashMap<String, String>();
//        map.put("deviceconf", "devicegate.cache.cacheHandler.DeviceConfHandler");
//        final CacheServerService css = new CacheServerService(
//                new InetSocketAddress("localhost", 8888),
//                "localhost",
//                5672,
//                "deviceconf111",
//                map,
//                new Configure()
//        );
//
//        try {
//            css.startService();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        final LocalCacheService lcs = new LocalCacheService(
                "deviceconf",
                new InetSocketAddress("localhost", 8888),
                "localhost",
                5672,
                "deviceconf",
                10,
                true,
                5000,
                new DEDeserEntrier()
        );

        try {
            lcs.startService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] queryKey = {
                "ECD0190524",
                "ECD0190525",
                "ECD0190526",
                "ECD0190527",
                "ECD0190528",
                "ECD0190529",
                "ECD0190530",
                "ECD0190531",
                "ECD0190532",
                "ECD0190533"
        };
        int id = 0;
        for (int i = 0; i < 10; i++) {
            System.out.println(lcs.get(queryKey[i]).getCreatedTime());
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(lcs.get(queryKey[i]).getCreatedTime());
        }

        //css.change("deviceconf", "5");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(lcs.get(queryKey[i]).getCreatedTime());
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(lcs.get(queryKey[i]).getCreatedTime());
        }

        lcs.stopService();

        //css.stopService();


        //RabbitProducer rp = new RabbitProducer();

    }
}
