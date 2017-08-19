package devicegate.protocol;

/**
 * Created by xiaoke on 17-8-19.
 */
abstract public class AttachInfo {

    abstract public Object get();

    abstract public Object get(int index);

    public static AttachInfo constantAttachInfo(final Object val) {
        return new AttachInfo() {
            @Override
            public Object get() {
                return val;
            }
            @Override
            public Object get(int index) {
                throw new UnsupportedOperationException("Not support get by index");
            }
        };
    }

    public static AttachInfo contant2AttachInfo(final Object val1, final Object val2) {
        return new AttachInfo() {
            @Override
            public Object get() {
                return get(0);
            }
            @Override
            public Object get(int index) {
                return index == 0 ? val1 : val2;
            }
        };
    }

}
