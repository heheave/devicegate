package devicegate.util;

/**
 * Created by xiaoke on 17-11-13.
 */
import org.apache.log4j.Logger;

import java.io.*;

/**
 * 采集内存使用率
 */
public class MemUsage implements ResourceUsage {

    private static final Logger log = Logger.getLogger(MemUsage.class);
    private static MemUsage INSTANCE = new MemUsage();

    private MemUsage(){

    }

    public static MemUsage getInstance(){
        return INSTANCE;
    }

    public float get() {
        //log.info("开始收集memory使用率");
        float memUsage = 0.0f;
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "cat /proc/meminfo";
            pro = r.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            int count = 0;
            long totalMem = 0, freeMem = 0;
            while((line=in.readLine()) != null){
                //log.info(line);
                String[] memInfo = line.split("\\s+");
                if(memInfo[0].startsWith("MemTotal")){
                    totalMem = Long.parseLong(memInfo[1]);
                }
                if(memInfo[0].startsWith("MemFree")){
                    freeMem = Long.parseLong(memInfo[1]);
                }
                memUsage = (float)Math.round((1 - (float)freeMem / (float)totalMem) * 100) / 100;
                //log.info("本节点内存使用率为: " + memUsage);
                if(++count == 2){
                    break;
                }
            }
            in.close();
            pro.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error("MemUsage发生InstantiationException. " + e.getMessage());
            log.error(sw.toString());
        }
        return memUsage;
    }

}
