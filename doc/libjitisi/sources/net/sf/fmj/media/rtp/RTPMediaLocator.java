package net.sf.fmj.media.rtp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import javax.media.MediaLocator;
import net.sf.fmj.media.datasink.rtp.ParsedRTPUrlElement;

public class RTPMediaLocator extends MediaLocator {
    public static final int PORT_UNDEFINED = -1;
    public static final int SSRC_UNDEFINED = 0;
    public static final int TTL_UNDEFINED = 1;
    String address = "";
    String contentType = "";
    int port = -1;
    long ssrc = 0;
    int ttl = 1;
    private boolean valid = true;

    public RTPMediaLocator(String locatorString) throws MalformedURLException {
        super(locatorString);
        parseLocator(locatorString);
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getSessionAddress() {
        return this.address;
    }

    public int getSessionPort() {
        return this.port;
    }

    public long getSSRC() {
        return this.ssrc;
    }

    public int getTTL() {
        return this.ttl;
    }

    public boolean isValid() {
        return this.valid;
    }

    private void parseLocator(String locatorString) throws MalformedURLException {
        String remainder = getRemainder();
        int colonIndex = remainder.indexOf(":");
        int slashIndex = remainder.indexOf("/", 2);
        int nextcolonIndex = -1;
        int nextslashIndex = -1;
        if (colonIndex != -1) {
            nextcolonIndex = remainder.indexOf(":", colonIndex + 1);
        }
        if (slashIndex != -1) {
            nextslashIndex = remainder.indexOf("/", slashIndex + 1);
        }
        if (colonIndex != -1) {
            this.address = remainder.substring(2, colonIndex);
        }
        try {
            InetAddress Iaddr = InetAddress.getByName(this.address);
            if (colonIndex == -1 || slashIndex == -1) {
                throw new MalformedURLException("RTP MediaLocator is Invalid. Must be of form rtp://addr:port/content/ttl");
            }
            String portstr = "";
            if (nextcolonIndex == -1) {
                portstr = remainder.substring(colonIndex + 1, slashIndex);
            } else {
                portstr = remainder.substring(colonIndex + 1, nextcolonIndex);
            }
            try {
                this.port = Integer.valueOf(portstr).intValue();
                if (nextcolonIndex != -1) {
                    try {
                        this.ssrc = Long.valueOf(remainder.substring(nextcolonIndex + 1, slashIndex)).longValue();
                    } catch (NumberFormatException e) {
                    }
                }
                if (slashIndex != -1) {
                    if (nextslashIndex == -1) {
                        this.contentType = remainder.substring(slashIndex + 1, remainder.length());
                    } else {
                        this.contentType = remainder.substring(slashIndex + 1, nextslashIndex);
                    }
                    if (this.contentType.equals(ParsedRTPUrlElement.AUDIO) || this.contentType.equals(ParsedRTPUrlElement.VIDEO)) {
                        this.contentType = "rtp/" + this.contentType;
                    } else {
                        throw new MalformedURLException("Content Type in URL must be audio or video ");
                    }
                }
                if (nextslashIndex != -1) {
                    try {
                        this.ttl = Integer.valueOf(remainder.substring(nextslashIndex + 1, remainder.length())).intValue();
                    } catch (NumberFormatException e2) {
                    }
                }
            } catch (NumberFormatException e3) {
                throw new MalformedURLException("RTP MediaLocator Port must be a valid integer");
            }
        } catch (UnknownHostException e4) {
            throw new MalformedURLException("Valid RTP Session Address must be given");
        }
    }
}
