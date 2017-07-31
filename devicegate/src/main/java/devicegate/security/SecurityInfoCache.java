package devicegate.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xiaoke on 17-7-22.
 */
public class SecurityInfoCache {

    private Map<String, Set<String>> infoCache;

    private volatile static SecurityInfoCache instance;

    public static SecurityInfoCache getInstance() {
        if (instance == null) {
            synchronized (SecurityInfoCache.class) {
                if (instance == null) {
                    instance = new SecurityInfoCache();
                }
            }
        }
        return instance;
    }

    private SecurityInfoCache() {
        infoCache = new HashMap<String, Set<String>>();
    }

    public boolean contains(String type, String info) {
        synchronized (infoCache) {
            Set<String> infoSet = infoCache.get(type);
            if (infoSet != null) {
                return infoSet.contains(info);
            }
            return false;
        }
    }

    public void addType(String type, Set<String> infos) {
        synchronized (infoCache) {
            Set<String> infoSet = infoCache.get(type);
            if (infoSet != null) {
                infoSet.addAll(infos);
            } else {
                infoCache.put(type, infos);
            }
        }
    }

    public void addEntry(String type, String info) {
        synchronized (infoCache) {
            Set<String> infoSet = infoCache.get(type);
            if (infoSet != null) {
                infoSet.add(info);
            } else {
                Set<String> newInfoSet = new HashSet<String>();
                newInfoSet.add(info);
                infoCache.put(type, newInfoSet);
            }
        }
    }

    public void removeType(String type) {
        synchronized (infoCache) {
            infoCache.remove(type);
        }
    }

    public void removeEntry(String type, String info) {
        synchronized (infoCache) {
            Set<String> infoSet = infoCache.get(type);
            if (infoSet != null) {
                infoSet.remove(info);
            }
        }
    }

    public void clearType(String type) {
        removeType(type);
    }

    public void clear() {
        synchronized (infoCache) {
            infoCache.clear();
        }
    }

    public int sizeType() {
        synchronized (infoCache) {
            return infoCache.size();
        }
    }

    public int sizeEntry(String type) {
        synchronized (infoCache) {
            Set<String> infoSet = infoCache.get(type);
            if (infoSet != null) {
                return infoSet.size();
            }
            return 0;
        }
    }
}
