package net.java.sip.communicator.impl.protocol.sip.xcap;

import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ListType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ResourceListsType;

public interface ResourceListsClient {
    public static final String DOCUMENT_FORMAT = "resource-lists/users/%2s/index";
    public static final String ELEMENT_CONTENT_TYPE = "application/xcap-el+xml";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:xcap-caps";
    public static final String RESOURCE_LISTS_CONTENT_TYPE = "application/resource-lists+xml";

    void deleteResourceLists() throws XCapException;

    ListType getList(String str) throws XCapException;

    ResourceListsType getResourceLists() throws XCapException;

    void putResourceLists(ResourceListsType resourceListsType) throws XCapException;
}
