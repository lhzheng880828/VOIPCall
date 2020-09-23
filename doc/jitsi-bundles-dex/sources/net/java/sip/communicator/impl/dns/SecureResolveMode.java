package net.java.sip.communicator.impl.dns;

public enum SecureResolveMode {
    IgnoreDnssec,
    SecureOnly,
    SecureOrUnsigned,
    WarnIfBogus,
    WarnIfBogusOrUnsigned
}
