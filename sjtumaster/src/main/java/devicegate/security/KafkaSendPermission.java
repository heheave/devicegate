package devicegate.security;

import java.security.Permission;

/**
 * Created by xiaoke on 17-6-10.
 */
public class KafkaSendPermission extends Permission{


    /**
     * Constructs a permission with the specified name.
     *
     * @param name name of the Permission object being created.
     */

    private final String checkInfo;

    private final String action;

    public KafkaSendPermission(String checkType, String action, String checkInfo) {
        super(checkType);
        this.action = action;
        this.checkInfo = checkInfo;
    }

    public KafkaSendPermission(String checkType, String action) {
        this(checkType, action, null);
    }

    private boolean check(String checkType, String checkInfo) {
        return SecurityInfoCache.getInstance().contains(checkType, checkInfo);
    }

    @Override
    public boolean implies(Permission permission) {
        if (permission instanceof KafkaSendPermission) {
            KafkaSendPermission ksp = (KafkaSendPermission)permission;
            String kspAction = ksp.action;
            String checkType = ksp.getName();
            String checkInfo = ksp.checkInfo;
            return stringEquals(action, kspAction) && checkType != null
                    && checkInfo != null && check(checkType, checkInfo);
        } else {
            return false;
        }
    }

    private boolean stringEquals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        } else if (str1 != null) {
            return str1.equals(str2);
        } else {
            return str2.equals(str1);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KafkaSendPermission) {
            KafkaSendPermission ksp = (KafkaSendPermission)obj;
            return stringEquals(getName(), ksp.getName())
                    && stringEquals(getActions(), ksp.getActions());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int res = 17;
        res = res * 31 + (getName() == null ? 0 : getName().hashCode());
        res = res * 31 +(getActions() == null ? 0 : getActions().hashCode());
        return res;
    }

    @Override
    public String getActions() {
        return "Check type: " + action;
    }
}
