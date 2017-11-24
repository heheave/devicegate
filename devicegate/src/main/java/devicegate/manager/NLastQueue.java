package devicegate.manager;

/**
 * Created by xiaoke on 17-11-9.
 */
public class NLastQueue<T> {

    public interface CalculateOp<E> {
        E eval(E a, E b);
    }

    private final Object[] queue;

    private volatile int head;

    private final int N;

    public NLastQueue(int N) {
        this.N = N <= 0 ? 1 : N;
        this.queue = new Object[this.N];
        this.head = N - 1;
    }

    public void in(T t) {
        int hTmp = head;
        hTmp = (hTmp + 1) % N;
        queue[hTmp] = t;
        head = hTmp;
    }

    public T out() {
        int hTmp = head;
        T t = (T)queue[head];
        if (t != null) {
            hTmp = (hTmp - 1 + N) % N;
            head = hTmp;
        }
        return t;
    }

    public T tail() {
        int hTmp = head;
        if (queue[(hTmp + 1) % N] != null) {
            return (T)queue[(hTmp + 1) % N];
        } else if (queue[hTmp] != null) {
            return (T)queue[0];
        } else {
            return null;
        }
    }

    public T cur() {
        int hTmp = head;
        return (T)queue[hTmp];
    }

    public int size() {
        int hTmp = head;
        if (queue[(hTmp + 1) % N] != null) {
            return N;
        } else if (queue[hTmp] != null) {
            return hTmp + 1;
        } else {
            return 0;
        }
    }

    public T caculate(CalculateOp<T> op) {
        if (op == null) {
            return null;
        }
        if (N == 1) {
            return (T)queue[0];
        }
        T first = (T)queue[0];
        for (int i = 0; i < N; i++) {
            first = op.eval(first, (T)queue[i]);
        }
        return first;
    }

    public Object[] all() {
        int hTmp = head;
        int begin = 0;
        int size;
        if (queue[(hTmp + 1) % N] != null) {
            begin = (hTmp + 1) % N;
            size = N;
        } else if (queue[hTmp] != null) {
            begin = 0;
            size = hTmp + 1;
        } else {
            size = 0;
        }
        Object[] res = new Object[size];
        for (int i = 0; i < size; i++) {
            res[i] = queue[(begin + i) % N];
        }
        return res;
    }
}
