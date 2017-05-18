package devicegate;

import devicegate.conf.Configure;
import devicegate.launch.Launch;
import devicegate.launch.MasterLaunch;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.awt.*;

/**
 * Created by xiaoke on 17-5-16.
 */
public class GateMain {

    private static final Logger log = Logger.getLogger(GateMain.class);

    private static Launch launch;

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/file/log4j.properties");
        if (args.length != 1) {
            log.error("Should specify the launch mode: master/slave");
            System.exit(-1);
        } else {
            Configure conf = new Configure();
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
