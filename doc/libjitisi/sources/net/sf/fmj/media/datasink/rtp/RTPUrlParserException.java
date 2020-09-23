package net.sf.fmj.media.datasink.rtp;

public class RTPUrlParserException extends Exception {
    public RTPUrlParserException(String message) {
        super(message);
    }

    public RTPUrlParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public RTPUrlParserException(Throwable cause) {
        super(cause);
    }
}
