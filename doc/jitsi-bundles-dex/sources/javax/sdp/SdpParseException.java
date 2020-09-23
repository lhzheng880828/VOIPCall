package javax.sdp;

public class SdpParseException extends SdpException {
    private int charOffset;
    private int lineNumber;

    public SdpParseException(int lineNumber, int charOffset, String message, Throwable rootCause) {
        super(message, rootCause);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public SdpParseException(int lineNumber, int charOffset, String message) {
        super(message);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getCharOffset() {
        return this.charOffset;
    }

    public String getMessage() {
        return super.getMessage();
    }
}
