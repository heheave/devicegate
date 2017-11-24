package devicegate.manager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaoke on 17-5-16.
 */
abstract public class AbstactManager<K, T> {

    protected final ConcurrentHashMap<K, T> idToCacheObj;

    public AbstactManager() {
        idToCacheObj = new ConcurrentHashMap<K, T>();
    }

    public T putIfAbsent(K id, T newValue) {
        return idToCacheObj.putIfAbsent(id, newValue);
    }

    public T put(K id, T newValue) {
        T oldValue = idToCacheObj.put(id, newValue);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public T get(K id) {
        return idToCacheObj.get(id);
    }

    public T remove(K id) {
        T oldValue = idToCacheObj.remove(id);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public boolean containsKey(K id) {
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
