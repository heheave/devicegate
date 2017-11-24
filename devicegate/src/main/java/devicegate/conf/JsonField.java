package devicegate.conf;

/**
 * Created by xiaoke on 17-5-24.
 */
public class JsonField {

    public static final class MSG{
        public static final String ID = "id";
        public static final String TELLINFO = "info";
        public static final String HOST = "addr";
        public static final String PROT = "port";
        public static final String PTC = "ptc";
        public static final String ISRET = "ret";
        public static final String ACKINFO = "ackinfo";



        public static final String CU = "cpu";
        public static final String MU = "mem";
        public static final String IU = "io";
        public static final String NU = "net";
        public static final String MN = "msgNum";
        public static final String MB = "msgBytes";
        public static final String MNT = "msgNumT";
        public static final String MBT = "msgBytesT";
        public static final String DT = "dt";
        public static final String CN = "cntNum";
        public static final String HT = "time";
    }

    public static final class DeviceValue {
        public static final String CNT = "cnt";
        public static final String USER = "user";
        public static final String PASSWD = "passwd";
        public static final String ID = "id";
        public static final String APP = "app";
        public static final String DTYPE = "dtype";
        public static final String DESC = "desc";
        public static final String COM = "com";
        public static final String LOC = "loc";
        public static final String PORTNUM = "portnum";
        public static final String DTIMESTAMP = "dtimestamp";
        public static final String PTIMESTAMP = "ptimestamp";
        public static final String MTYPE = "mtype";
        public static final String VALUES = "values";

        public static final String VALID="valid";
        public static final String UNIT ="unit";
        public static final String VALUE="value";
    }

    public static final class DeviceCtrl {
        public static final String ID = "did";
        public static final String TYPE = "type";
        public static final String MODE = "mode";
        public static final String PORT = "port";
        public static final String VALUE = "value";
        public static final String MAGIC = "magic";
        public static final String BACK = "back";
        public static final String RET = "stat";

        public enum CtrlState{

            DID_NULL("did is null"),
            CTRL_SUCCEEDED("ctrl succeeded"),
            CTRL_FAILED("ctrl failed"),
            NO_DEVICE("no device info"),
            UNCERTAIN("ctrl result uncertain");

            private String desc;

            CtrlState(String desc) {
                this.desc = desc;
            }

            public String desc() {
                return desc;
            }
        }
    }
}
