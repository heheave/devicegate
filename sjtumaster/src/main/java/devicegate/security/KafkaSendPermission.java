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

    private static final String SEPARATOR = ":";

    private final String action;

    public KafkaSendPermission(String name, String action) {
        super(name);
        //System.out.println("name:" + name + ",action: " + action);
        this.action = action;
    }

    public KafkaSendPermission(String name, String did, String act) {
        this(name, String.format("%s%s%s", did, SEPARATOR, act));
    }

    @Override
    public boolean implies(Permission permission) {
        if (permission instanceof KafkaSendPermission) {
            KafkaSendPermission ksp = (KafkaSendPermission)permission;
            String toCheckedName = ksp.getName();
            //System.out.println(ksp.getName() + ">>" + ksp.getActions());
            String[] toCheckedAction = ksp.getActions().split(SEPARATOR, 2);
            //System.out.println(toCheckedName + ">>" + toCheckedAction[0] + ">>" + toCheckedAction[1]);
            //System.out.println(getName() + ">>" + getActions());
            return toCheckedName.endsWith(getName())
                    && toCheckedAction[0].startsWith(getName())
                    && toCheckedAction[1].equals(action);
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
        return action;
    }
}
