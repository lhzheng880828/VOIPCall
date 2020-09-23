package net.java.sip.communicator.impl.protocol.sip.xcap;

import java.io.IOException;
import java.net.URI;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.CommonPolicyParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.RulesetType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.ContentType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.DataType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.PresContentParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ListType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ResourceListsParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ResourceListsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps.XCapCapsParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps.XCapCapsType;
import net.java.sip.communicator.util.Base64;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.util.StringUtils;

public class XCapClientImpl extends BaseHttpXCapClient implements XCapClient {
    private boolean presContentSupported;
    private boolean presRulesSupported;
    private boolean resourceListsSupported;
    private XCapCapsType xCapCaps;

    public void connect(URI uri, Address userAddress, String username, String password) throws XCapException {
        super.connect(uri, userAddress, username, password);
        try {
            this.xCapCaps = loadXCapCaps();
            for (String namespace : this.xCapCaps.getNamespaces().getNamespace()) {
                if (ResourceListsClient.NAMESPACE.equals(namespace)) {
                    this.resourceListsSupported = true;
                }
                if (PresRulesClient.NAMESPACE.equals(namespace)) {
                    this.presRulesSupported = true;
                }
                if (PresContentClient.NAMESPACE.equals(namespace)) {
                    this.presContentSupported = true;
                }
            }
        } catch (XCapException e) {
            disconnect();
            throw e;
        }
    }

    public void disconnect() {
        super.disconnect();
        this.xCapCaps = null;
        this.resourceListsSupported = false;
    }

    public void putResourceLists(ResourceListsType resourceLists) throws XCapException {
        assertConnected();
        assertResourceListsSupported();
        XCapResourceId resourceId = new XCapResourceId(getResourceListsDocument());
        try {
            if (resourceLists.getList().size() == 0) {
                deleteResourceLists();
            } else {
                putResource(new XCapResource(resourceId, ResourceListsParser.toXml(resourceLists), ResourceListsClient.RESOURCE_LISTS_CONTENT_TYPE));
            }
        } catch (ParsingException e) {
            throw new XCapException("ResourceLists cannot be parsed", e);
        }
    }

    public ResourceListsType getResourceLists() throws XCapException {
        assertConnected();
        assertResourceListsSupported();
        try {
            String xml = getResource(new XCapResourceId(getResourceListsDocument()), ResourceListsClient.RESOURCE_LISTS_CONTENT_TYPE);
            if (xml == null) {
                return new ResourceListsType();
            }
            return ResourceListsParser.fromXml(xml);
        } catch (ParsingException e) {
            throw new XCapException("ResourceLists cannot be parsed", e);
        }
    }

    public void deleteResourceLists() throws XCapException {
        assertConnected();
        assertResourceListsSupported();
        deleteResource(new XCapResourceId(getResourceListsDocument()));
    }

    public ListType getList(String anchor) throws XCapException {
        assertConnected();
        assertResourceListsSupported();
        return null;
    }

    public XCapCapsType getXCapCaps() throws XCapException {
        assertConnected();
        return this.xCapCaps;
    }

    private XCapCapsType loadXCapCaps() throws XCapException {
        try {
            String xml = getResource(new XCapResourceId(getXCapCapsDocument()), XCapCapsClient.CONTENT_TYPE);
            if (xml != null) {
                return XCapCapsParser.fromXml(xml);
            }
            throw new XCapException("Server xcap-caps wasn't find");
        } catch (ParsingException e) {
            throw new XCapException("XCapCapsType cannot be parsed", e);
        }
    }

    public RulesetType getPresRules() throws XCapException {
        assertConnected();
        assertPresRulesSupported();
        try {
            String xml = getResource(new XCapResourceId(getPresRulesDocument()), PresRulesClient.CONTENT_TYPE);
            if (xml == null) {
                return new RulesetType();
            }
            return CommonPolicyParser.fromXml(xml);
        } catch (Exception e) {
            throw new XCapException("PresRules cannot be parsed", e);
        }
    }

    public void putPresRules(RulesetType presRules) throws XCapException {
        assertConnected();
        assertPresRulesSupported();
        try {
            putResource(new XCapResource(new XCapResourceId(getPresRulesDocument()), CommonPolicyParser.toXml(presRules), PresRulesClient.CONTENT_TYPE));
        } catch (ParsingException e) {
            throw new XCapException("PresRules cannot be parsed", e);
        }
    }

    public void deletePresRules() throws XCapException {
        assertConnected();
        assertResourceListsSupported();
        deleteResource(new XCapResourceId(getPresRulesDocument()));
    }

    public void putPresContent(ContentType content, String imageName) throws XCapException {
        assertConnected();
        assertPresContentSupported();
        try {
            putResource(new XCapResource(new XCapResourceId(getPresContentDocument(imageName)), PresContentParser.toXml(content), PresContentClient.CONTENT_TYPE));
        } catch (ParsingException e) {
            throw new XCapException("ContentType cannot be parsed", e);
        }
    }

    public ContentType getPresContent(String imageName) throws XCapException {
        assertConnected();
        assertPresContentSupported();
        try {
            XCapHttpResponse response = get(new XCapResourceId(getPresContentDocument(imageName)));
            int httpCode = response.getHttpCode();
            String contentType = response.getContentType();
            byte[] content = response.getContent();
            if (httpCode != Response.OK) {
                if (httpCode == Response.NOT_FOUND) {
                    return null;
                }
                throw new XCapException(String.format("Error %1s while getting %1s PresContent from XCAP server", new Object[]{Integer.valueOf(httpCode), resourceId.toString()}));
            } else if (!contentType.startsWith(PresContentClient.CONTENT_TYPE)) {
                throw new XCapException(String.format("XCAP server returns invalid PresContent content type: %1s", new Object[]{contentType}));
            } else if (content == null || content.length == 0) {
                throw new XCapException("XCAP server returns invalid PresContent content");
            } else {
                try {
                    return PresContentParser.fromXml(new String(content, "UTF-8"));
                } catch (ParsingException e) {
                    ContentType presContent = new ContentType();
                    DataType data = new DataType();
                    data.setValue(new String(Base64.encode(content)));
                    presContent.setData(data);
                    return presContent;
                }
            }
        } catch (IOException e2) {
            throw new XCapException(String.format("%1s resource cannot be read", new Object[]{resourceId.toString()}), e2);
        }
    }

    public void deletePresContent(String imageName) throws XCapException {
        assertConnected();
        assertPresContentSupported();
        deleteResource(new XCapResourceId(getPresContentDocument(imageName)));
    }

    public URI getPresContentImageUri(String imageName) {
        assertConnected();
        return getResourceURI(new XCapResourceId(getPresContentDocument(imageName)));
    }

    public byte[] getImage(URI imageUri) throws XCapException {
        assertConnected();
        XCapHttpResponse response = get(imageUri);
        int httpCode = response.getHttpCode();
        byte[] content = response.getContent();
        if (httpCode == Response.OK) {
            return content;
        }
        throw new XCapException(String.format("Error %1s while getting %2s image from the server", new Object[]{Integer.valueOf(httpCode), imageUri}));
    }

    /* access modifiers changed from: protected */
    public void assertResourceListsSupported() {
        if (!this.resourceListsSupported) {
            throw new IllegalStateException("XCAP server doesn't support resource-lists");
        }
    }

    /* access modifiers changed from: protected */
    public void assertPresRulesSupported() {
        if (!this.presRulesSupported) {
            throw new IllegalStateException("XCAP server doesn't support pres-rules");
        }
    }

    /* access modifiers changed from: protected */
    public void assertPresContentSupported() {
        if (!this.presContentSupported) {
            throw new IllegalStateException("XCAP server doesn't support pres-content");
        }
    }

    private void putResource(XCapResource resource) throws XCapException {
        XCapHttpResponse response = put(resource);
        int httpCode = response.getHttpCode();
        if (httpCode != Response.OK && httpCode != 201) {
            String errorMessage;
            if (getXCapErrorMessage(response) != null) {
                errorMessage = String.format("Error %1s while putting %2s to XCAP server. %3s", new Object[]{Integer.valueOf(httpCode), resource.getId().toString(), getXCapErrorMessage(response)});
            } else {
                errorMessage = String.format("Error %1s while putting %2s to XCAP server", new Object[]{Integer.valueOf(httpCode), resource.getId().toString()});
            }
            throw new XCapException(errorMessage);
        }
    }

    private String getResource(XCapResourceId resourceId, String contentType) throws XCapException {
        try {
            XCapHttpResponse response = get(resourceId);
            int httpCode = response.getHttpCode();
            byte[] content = response.getContent();
            if (httpCode != Response.OK) {
                if (httpCode == Response.NOT_FOUND) {
                    return null;
                }
                String errorMessage;
                if (getXCapErrorMessage(response) != null) {
                    errorMessage = String.format("Error %1s while getting %2s from XCAP server. %3s", new Object[]{Integer.valueOf(httpCode), resourceId.toString(), getXCapErrorMessage(response)});
                } else {
                    errorMessage = String.format("Error %1s while getting %2s from XCAP server", new Object[]{Integer.valueOf(httpCode), resourceId.toString()});
                }
                if (httpCode == Response.UNAUTHORIZED || httpCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
                    String displayName = this.userAddress.getDisplayName();
                    if (StringUtils.isNullOrEmpty(displayName)) {
                        displayName = this.userAddress.toString();
                    }
                    BaseHttpXCapClient.showError(null, null, SipActivator.getResources().getI18NString("impl.protocol.sip.XCAP_ERROR_UNAUTHORIZED", new String[]{displayName}));
                }
                throw new XCapException(errorMessage);
            } else if (StringUtils.isNullOrEmpty(response.getContentType()) || content == null || content.length == 0) {
                return null;
            } else {
                if (response.getContentType().startsWith(contentType)) {
                    return new String(content, "UTF-8");
                }
                throw new XCapException(String.format("XCAP server returns invalid content type: %1s", new Object[]{response.getContentType()}));
            }
        } catch (IOException e) {
            throw new XCapException(String.format("%1s resource cannot be read", new Object[]{resourceId.toString()}), e);
        }
    }

    private void deleteResource(XCapResourceId resourceId) throws XCapException {
        XCapHttpResponse response = delete(resourceId);
        int httpCode = response.getHttpCode();
        if (httpCode != Response.OK && httpCode != Response.NOT_FOUND) {
            String errorMessage;
            if (getXCapErrorMessage(response) != null) {
                errorMessage = String.format("Error %1s while deleting %2s resource from XCAP server. %3s", new Object[]{Integer.valueOf(httpCode), resourceId.toString(), getXCapErrorMessage(response)});
            } else {
                errorMessage = String.format("Error %1s while deleting %2s resource from XCAP server", new Object[]{Integer.valueOf(httpCode), resourceId.toString()});
            }
            throw new XCapException(errorMessage);
        }
    }

    private String getResourceListsDocument() {
        return String.format(ResourceListsClient.DOCUMENT_FORMAT, new Object[]{this.userAddress.getURI().toString()});
    }

    private String getXCapCapsDocument() {
        return XCapCapsClient.DOCUMENT_FORMAT;
    }

    private String getPresRulesDocument() {
        return String.format(PresRulesClient.DOCUMENT_FORMAT, new Object[]{this.userAddress.getURI().toString()});
    }

    private String getPresContentDocument(String imageName) {
        return String.format(PresContentClient.DOCUMENT_FORMAT, new Object[]{this.userAddress.getURI().toString(), imageName});
    }

    public boolean isResourceListsSupported() {
        assertConnected();
        return this.resourceListsSupported;
    }

    public boolean isPresRulesSupported() {
        assertConnected();
        return this.presRulesSupported;
    }

    public boolean isPresContentSupported() {
        return this.presContentSupported;
    }
}
