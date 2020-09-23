package org.jitsi.service.neomedia;

public enum MediaUseCase {
    ANY("any"),
    CALL("call"),
    DESKTOP("desktop");
    
    private final String mediaUseCase;

    private MediaUseCase(String mediaUseCase) {
        this.mediaUseCase = mediaUseCase;
    }

    public String toString() {
        return this.mediaUseCase;
    }

    public static MediaUseCase parseString(String mediaUseCase) throws IllegalArgumentException {
        if (CALL.toString().equals(mediaUseCase)) {
            return CALL;
        }
        if (ANY.toString().equals(mediaUseCase)) {
            return ANY;
        }
        if (DESKTOP.toString().equals(mediaUseCase)) {
            return DESKTOP;
        }
        throw new IllegalArgumentException(mediaUseCase + " is not a currently supported MediaUseCase");
    }
}
