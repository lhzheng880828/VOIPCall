package net.java.sip.communicator.impl.protocol.sip.xcap;

public interface XCapClient extends HttpXCapClient, XCapCapsClient, ResourceListsClient, PresRulesClient, PresContentClient {
    boolean isPresContentSupported();

    boolean isPresRulesSupported();

    boolean isResourceListsSupported();
}
