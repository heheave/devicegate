package devicegate.protocol;

/**
 * Created by xiaoke on 17-8-19.
 */
public class AuthRet {

    private final boolean passed;

    private final Exception e;

    private AuthRet(boolean passed, Exception e) {
        this.passed = passed;
        this.e = e;
    }

    public boolean isAuthorized() {
        return this.passed;
    }

    public String faildReason() {
        if (!isAuthorized()) {
            return e == null ? "No Faild Reason" : e.getMessage();
        } else {
            return "Auth Passed";
        }
    }

    public Throwable cause() {
        return isAuthorized() ? null : e;
    }

    public static final AuthRet AUTH_PASSED = AuthRet.apply(true, null);

    public static AuthRet apply(boolean passed, Exception e) {
        return new AuthRet(passed, e);
    }
}
