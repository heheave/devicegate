package devicegate.security;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiaoke on 17-6-20.
 */
public class DeviceCtrlPermission extends Permission{

    /**
     * Constructs a permission with the specified name.
     *
     * @param name name of the Permission object being created.
     */
    private final Set<String> magics;

    public DeviceCtrlPermission(String name, String action) {
        super(name);
        this.magics = new HashSet<String>();
        if (action != null) {
            String[] magicStrs = action.split(":");
            for (String mstr : magicStrs) {
                if (!mstr.trim().isEmpty()) {
                    magics.add(mstr.trim().toLowerCase());
                }
            }
        }
    }

    public DeviceCtrlPermission(String action) {
        this("DeviceCtrl", action);
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
    public boolean implies(Permission permission) {
        if (permission instanceof DeviceCtrlPermission) {
            DeviceCtrlPermission dcp = (DeviceCtrlPermission)permission;
            if (stringEquals(getName(), dcp.getName()) && dcp.magics.size() == 1 && magics.containsAll(dcp.magics)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceCtrlPermission) {
            DeviceCtrlPermission dcp = (DeviceCtrlPermission)obj;
            return stringEquals(getName(), dcp.getName())
                    && stringEquals(getActions(), dcp.getActions());
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
        StringBuffer sb = new StringBuffer();
        boolean isBegin = true;
        for (String m: magics) {
            if (isBegin) {
                isBegin = false;
            } else {
                sb.append(':');
            }
            sb.append(m);
        }
        return  sb.toString();
    }
}
