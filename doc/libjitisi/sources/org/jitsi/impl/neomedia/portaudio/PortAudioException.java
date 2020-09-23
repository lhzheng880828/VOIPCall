package org.jitsi.impl.neomedia.portaudio;

import org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId;

public class PortAudioException extends Exception {
    private static final long serialVersionUID = 0;
    private final long errorCode;
    private final HostApiTypeId hostApiType;

    public PortAudioException(String message) {
        this(message, 0, -1);
    }

    public PortAudioException(String message, long errorCode, int hostApiType) {
        super(message);
        this.errorCode = errorCode;
        this.hostApiType = hostApiType < 0 ? null : HostApiTypeId.valueOf(hostApiType);
    }

    public long getErrorCode() {
        return this.errorCode;
    }

    public HostApiTypeId getHostApiType() {
        return this.hostApiType;
    }

    public String toString() {
        String s = super.toString();
        long errorCode = getErrorCode();
        String errorCodeStr = errorCode == 0 ? null : Long.toString(errorCode);
        HostApiTypeId hostApiType = getHostApiType();
        String hostApiTypeStr = hostApiType == null ? null : hostApiType.toString();
        if (errorCodeStr == null && hostApiTypeStr == null) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        sb.append(": ");
        if (errorCodeStr != null) {
            sb.append("errorCode= ");
            sb.append(errorCodeStr);
            sb.append(';');
        }
        if (hostApiTypeStr != null) {
            if (errorCodeStr != null) {
                sb.append(' ');
            }
            sb.append("hostApiType= ");
            sb.append(hostApiTypeStr);
            sb.append(';');
        }
        return sb.toString();
    }
}
