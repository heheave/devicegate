package devicegate.manager;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-6-19.
 */
public class MachineCacheInfo {

    private final InetSocketAddress isa;

    private final String ptc;

    public MachineCacheInfo(InetSocketAddress isa, String ptc) {
        this.isa = isa;
        this.ptc = ptc;
    }

    public InetSocketAddress getIsa() {
        return isa;
    }

    public String getPtc() {
        return ptc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MachineCacheInfo di = (MachineCacheInfo) o;

        if (isa != null ? !isa.equals(di.isa) : di.isa != null) return false;
        return ptc != null ? ptc.equals(di.ptc) : di.ptc == null;

    }

    @Override
    public int hashCode() {
        int result = isa != null ? isa.hashCode() : 0;
        result = 31 * result + (ptc != null ? ptc.hashCode() : 0);
        return result;
    }
}
