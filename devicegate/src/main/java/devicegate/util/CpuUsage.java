package devicegate.util;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by xiaoke on 17-11-13.
 */
public class CpuUsage implements ResourceUsage {

    private static final Logger log = Logger.getLogger(CpuUsage.class);
    private static CpuUsage INSTANCE = new CpuUsage();

    private CpuUsage(){

    }

    public static CpuUsage getInstance(){
        return INSTANCE;
    }

    public float get() {
        //log.info("开始收集cpu使用率");
        float cpuUsage = 0;
        Process pro1,pro2;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "cat /proc/stat";
            //第一次采集CPU时间
            //long startTime = System.currentTimeMillis();
            pro1 = r.exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String line = null;
            long idleCpuTime1 = 0, totalCpuTime1 = 0;	//分别为系统启动后空闲的CPU时间和总的CPU时间
            while((line = in1.readLine()) != null){
                if(line.startsWith("cpu")){
                    line = line.trim();
                    //log.info(line);
                    String[] temp = line.split("\\s+");
                    idleCpuTime1 = Long.parseLong(temp[4]);
                    for(String s : temp){
                        if(!s.equals("cpu")){
                            totalCpuTime1 += Long.parseLong(s);
                        }
                    }
                    //log.info("IdleCpuTime: " + idleCpuTime1 + ", " + "TotalCpuTime" + totalCpuTime1);
                    break;
                }
            }
            in1.close();
            pro1.destroy();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log.error("CpuUsage休眠时发生InterruptedException. " + e.getMessage());
                log.error(sw.toString());
            }
            //第二次采集CPU时间
            //long endTime = System.currentTimeMillis();
            pro2 = r.exec(command);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            long idleCpuTime2 = 0, totalCpuTime2 = 0;	//分别为系统启动后空闲的CPU时间和总的CPU时间
            while((line=in2.readLine()) != null){
                if(line.startsWith("cpu")){
                    line = line.trim();
                    //log.info(line);
                    String[] temp = line.split("\\s+");
                    idleCpuTime2 = Long.parseLong(temp[4]);
                    for(String s : temp){
                        if(!s.equals("cpu")){
                            totalCpuTime2 += Long.parseLong(s);
                        }
                    }
                    //log.info("IdleCpuTime: " + idleCpuTime2 + ", " + "TotalCpuTime" + totalCpuTime2);
                    break;
                }
            }
            if(idleCpuTime1 != 0 && totalCpuTime1 !=0 && idleCpuTime2 != 0 && totalCpuTime2 != 0){
                cpuUsage = (float)Math.round((1 -
                        (float)(idleCpuTime2 - idleCpuTime1)
                                / (float)(totalCpuTime2 - totalCpuTime1)) * 100) / 100;
                //log.info("本节点CPU使用率为: " + cpuUsage);
            }
            in2.close();
            pro2.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error("CpuUsage发生InstantiationException. " + e.getMessage());
            log.error(sw.toString());
        }
        return cpuUsage;
    }
}
