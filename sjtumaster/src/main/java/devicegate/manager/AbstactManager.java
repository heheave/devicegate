package devicegate.manager;

import io.netty.channel.Channel;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaoke on 17-5-16.
 */
abstract public class AbstactManager<T> {

    protected final ConcurrentHashMap<String, T> idToCacheObj;

    public AbstactManager() {
        idToCacheObj = new ConcurrentHashMap<String, T>();
    }

    public T putIfAbsent(String id, T newValue) {
        return idToCacheObj.putIfAbsent(id, newValue);
    }

    public T put(String id, T newValue) {
        T oldValue = idToCacheObj.put(id, newValue);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public T get(String id) {
        return idToCacheObj.get(id);
    }

    public T remove(String id) {
        T oldValue = idToCacheObj.remove(id);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public boolean containsKey(String id) {
        return idToCacheObj.containsKey(id);
    }

    public boolean isEmpty() {
        return idToCacheObj.isEmpty();
    }

    public void clear() {
        Collection<T> oldValues = idToCacheObj.values();
        idToCacheObj.clear();
        for (T oldValue : oldValues) {
            afterRemoved(oldValue);
        }
    }

    abstract void afterRemoved(T oldValue);
}
