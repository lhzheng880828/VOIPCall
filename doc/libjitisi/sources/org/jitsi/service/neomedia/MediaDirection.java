package org.jitsi.service.neomedia;

public enum MediaDirection {
    INACTIVE("inactive"),
    SENDONLY("sendonly"),
    RECVONLY("recvonly"),
    SENDRECV("sendrecv");
    
    private final String directionName;

    private MediaDirection(String directionName) {
        this.directionName = directionName;
    }

    public String toString() {
        return this.directionName;
    }

    public MediaDirection and(MediaDirection direction) {
        if (this == SENDRECV) {
            return direction;
        }
        if (this == SENDONLY) {
            if (direction == SENDONLY || direction == SENDRECV) {
                return SENDONLY;
            }
            return INACTIVE;
        } else if (this != RECVONLY) {
            return INACTIVE;
        } else {
            if (direction == RECVONLY || direction == SENDRECV) {
                return RECVONLY;
            }
            return INACTIVE;
        }
    }

    public MediaDirection or(MediaDirection direction) {
        if (this == SENDRECV) {
            return this;
        }
        if (this == SENDONLY) {
            if (direction.allowsReceiving()) {
                return SENDRECV;
            }
            return this;
        } else if (this != RECVONLY) {
            return direction;
        } else {
            if (direction.allowsSending()) {
                return SENDRECV;
            }
            return this;
        }
    }

    public MediaDirection getReverseDirection() {
        switch (this) {
            case SENDRECV:
                return SENDRECV;
            case SENDONLY:
                return RECVONLY;
            case RECVONLY:
                return SENDONLY;
            default:
                return INACTIVE;
        }
    }

    public MediaDirection getDirectionForAnswer(MediaDirection remotePartyDir) {
        return and(remotePartyDir.getReverseDirection());
    }

    public boolean allowsSending() {
        return this == SENDONLY || this == SENDRECV;
    }

    public boolean allowsReceiving() {
        return this == RECVONLY || this == SENDRECV;
    }

    public static MediaDirection parseString(String mediaDirectionStr) throws IllegalArgumentException {
        for (MediaDirection value : values()) {
            if (value.toString().equals(mediaDirectionStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(mediaDirectionStr + " is not a valid media direction");
    }
}
