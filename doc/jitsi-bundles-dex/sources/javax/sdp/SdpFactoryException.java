package javax.sdp;

public class SdpFactoryException extends SdpException {
    protected Exception ex;

    public SdpFactoryException(String msg) {
        super(msg);
    }

    public SdpFactoryException(Exception ex) {
        super(ex.getMessage());
        this.ex = ex;
    }

    public SdpFactoryException(String msg, Exception ex) {
        super(msg);
        this.ex = ex;
    }

    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        }
        if (this.ex != null) {
            return this.ex.getMessage();
        }
        return null;
    }

    public Exception getException() {
        return this.ex;
    }
}
