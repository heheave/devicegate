package devicegate.util;

/**
 * Created by xiaoke on 17-11-13.
 */
import org.apache.log4j.Logger;

import java.io.*;

/**
 * 采集网络带宽使用率
 */
public class NetUsage implements ResourceUsage {

    private static Logger log = Logger.getLogger(NetUsage.class);
    private static NetUsage INSTANCE = new NetUsage();
    //private final static float TotalBandwidth = 1000;	//网口带宽,Mbps

    private volatile static long lastNetTime = -1;
    private volatile static long lastNetBytes = -1;

    private NetUsage(){

    }

    public static NetUsage getInstance(){
        return INSTANCE;
    }

    public float get() {
        //log.info("开始收集网络带宽使用率");
        float netUsage = 0.0f;
        Process pro1,pro2;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "cat /proc/net/dev";
            //第一次采集流量数据
            long startTime = System.currentTimeMillis();
            pro1 = r.exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String line = null;
            long outSize1 = 0;
            while((line=in1.readLine()) != null){
                line = line.trim();
                if(line.startsWith("eth0")){
                    //log.info(line);
                    String[] temp = line.substring(5).trim().split("\\s+");
                    //inSize1 = Long.parseLong(temp[0]);	//Receive bytes,单位为Byte
                    outSize1 = Long.parseLong(temp[8]);	//Transmit bytes,单位为Byte
                    break;
                }
            }
            in1.close();
            pro1.destroy();
            if (lastNetBytes <= 0) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    log.error("NetUsage休眠时发生InterruptedException. " + e.getMessage());
                    log.error(sw.toString());
                }
                //第二次采集流量数据
                long endTime = System.currentTimeMillis();
                pro2 = r.exec(command);
                BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
                long outSize2 = 0;
                while ((line = in2.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("eth0")) {
                        //log.info(line);
                        String[] temp = line.substring(5).trim().split("\\s+");
                        //inSize2 = Long.parseLong(temp[0]);
                        outSize2 = Long.parseLong(temp[8]);
                        break;
                    }
                }
                if (outSize1 != 0 && outSize2 != 0) {
                    float interval = (float) (endTime - startTime) / 1000;
                    //网口传输速度,单位为bps
                    float curRate = (float) (outSize2 - outSize1) * 8 / (1000000 * interval);
                    System.out.println("-----" + (outSize2 - outSize1));
                    netUsage = (float) Math.round(curRate * 100) / 100;
                    //log.info("本节点网口速度为: " + curRate + "Mbps");
                    //log.info("本节点网络带宽使用率为: " + netUsage);
                    lastNetTime = endTime;
                    lastNetBytes = outSize2;
                }
                in2.close();
                pro2.destroy();
            } else {
                long endTime = startTime;
                if (outSize1 != 0) {
                    float interval = (float) (endTime - lastNetTime) / 1000;
                    //网口传输速度,单位为bps
                    float curRate = (float) (outSize1 - lastNetBytes) * 8 / (1000000 * interval);
                    System.out.println("-----" + (outSize1 - lastNetBytes));
                    netUsage = (float) Math.round(curRate * 100) / 100;
                    lastNetTime = endTime;
                    lastNetBytes = outSize1;
                    //log.info("本节点网口速度为: " + curRate + "Mbps");
                    //log.info("本节点网络带宽使用率为: " + netUsage);
                }
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error("NetUsage发生InstantiationException. " + e.getMessage());
            log.error(sw.toString());
        }
        System.out.println(lastNetTime + ":" + lastNetBytes);
        return netUsage;
    }

}
