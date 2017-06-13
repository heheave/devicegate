import devicegate.security.KafkaSendPermission;
import devicegate.util.SessionIdGenUtil;

/**
 * Created by xiaoke on 17-6-7.
 */
public class TestMain {
    public static void main(String[] args) {
        KafkaSendPermission p1 = new KafkaSendPermission("SWITCH", "send");
        KafkaSendPermission p2 = new KafkaSendPermission("SWITCH", "SWITCH-213", "send");
        System.out.println(p1.hashCode() + "-->" + p2.hashCode());
        System.out.println(p1.equals(p2));
        System.out.println(p2.equals(p1));
        System.out.println(p1.implies(p2));
        System.out.println(p2.implies(p1));
    }
}
