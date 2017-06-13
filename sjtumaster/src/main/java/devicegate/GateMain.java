package devicegate;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.launch.Launch;
import devicegate.launch.MasterLaunch;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.DocumentException;

import java.awt.*;

/**
 * Created by xiaoke on 17-5-16.
 */
public class GateMain {

    private static final Logger log = Logger.getLogger(GateMain.class);

    private static Launch launch;

    public static void main(String[] args) {
        PropertyConfigurator.configure(V.LOG_PATH);
        if (args.length != 1) {
            log.error("Should specify the launch mode: master/slave");
            System.exit(-1);
        } else {
            Configure conf = new Configure();
            try {
                conf.readFromXml();
            } catch (DocumentException e) {
                log.error("Could not find var.xml", e);
                System.exit(-1);
            }
            if ("master".compareToIgnoreCase(args[0]) == 0) {
                log.info("Launcher mode is master");
                launch = new MasterLaunch(conf);
            } else {
                log.info("Launcher mode is slave");
                launch = new SlaveLaunch(conf);
            }
        }

        if (launch != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        launch.launch();
                        log.info("Succeeded to launch");
                    }catch (Exception e) {
                        log.error("Failed to launch", e);
                        System.exit(-1);
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (launch != null) {
                        launch.shutdown();
                    }
                    log.info("Succeeded to shutdown");
                }
            });
        }

    }
}
