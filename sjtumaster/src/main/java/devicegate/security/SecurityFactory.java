package devicegate.security;

/**
 * Created by xiaoke on 17-7-22.
 */
public class SecurityFactory {

    public static KafkaSendPermission getKafkaSendPermission(String checkType, String action, String checkInfo) {
        return new KafkaSendPermission(checkType, action, checkInfo);
    }

}
