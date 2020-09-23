package org.jitsi.service.protocol;

public class OperationFailedException extends Exception {
    public static final int AUTHENTICATION_CANCELED = 15;
    public static final int AUTHENTICATION_FAILED = 401;
    public static final int CHAT_ROOM_NOT_JOINED = 14;
    public static final int CONTACT_GROUP_ALREADY_EXISTS = 6;
    public static final int FORBIDDEN = 403;
    public static final int GENERAL_ERROR = 1;
    public static final int IDENTIFICATION_CONFLICT = 10;
    public static final int ILLEGAL_ARGUMENT = 11;
    public static final int INTERNAL_ERROR = 4;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int INVALID_ACCOUNT_PROPERTIES = 7;
    public static final int NETWORK_FAILURE = 2;
    public static final int NOT_ENOUGH_PRIVILEGES = 12;
    public static final int NOT_FOUND = 404;
    public static final int NOT_SUPPORTED_OPERATION = 18;
    public static final int OPERATION_CANCELED = 16;
    public static final int PROVIDER_NOT_REGISTERED = 3;
    public static final int REGISTRATION_REQUIRED = 13;
    public static final int SERVER_NOT_SPECIFIED = 17;
    public static final int SUBSCRIPTION_ALREADY_EXISTS = 5;
    private static final long serialVersionUID = 0;
    private final int errorCode;

    public OperationFailedException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OperationFailedException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
