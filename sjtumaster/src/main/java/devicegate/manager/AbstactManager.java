package devicegate.manager;

import io.netty.channel.Channel;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaoke on 17-5-16.
 */
abstract public class AbstactManager<T> {

    protected final ConcurrentHashMap<String, T> idToChannel;

    public AbstactManager() {
        idToChannel = new ConcurrentHashMap<String, T>();
    }

    public T putIfAbsent(String id, T newValue) {
        return idToChannel.putIfAbsent(id, newValue);
    }

    public T put(String id, T newValue) {
        T oldValue = idToChannel.put(id, newValue);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public T get(String id) {
        return idToChannel.get(id);
    }

    public T remove(String id) {
        T oldValue = idToChannel.remove(id);
        if (oldValue != null) {
            afterRemoved(oldValue);
        }
        return oldValue;
    }

    public boolean containsKey(String id) {
        return idToChannel.containsKey(id);
    }

    public boolean isEmpty() {
        return idToChannel.isEmpty();
    }

    public void clear() {
        Collection<T> oldValues = idToChannel.values();
        idToChannel.clear();
        for (T oldValue : oldValues) {
            afterRemoved(oldValue);
        }
    }

    abstract void afterRemoved(T oldValue);
}
