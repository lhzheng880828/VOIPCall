package net.java.sip.communicator.impl.protocol.sip.xcap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror.XCapError;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror.XCapErrorParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.utils.StreamUtils;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.httputil.HttpUtils;
import net.java.sip.communicator.util.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.message.Response;
import org.osgi.framework.ServiceReference;

public abstract class BaseHttpXCapClient implements HttpXCapClient {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String XCAP_ERROR_CONTENT_TYPE = "application/xcap-error+xml";
    private static final Logger logger = Logger.getLogger(BaseHttpXCapClient.class);
    private CertificateService certificateVerification;
    private boolean connected;
    private String password;
    protected URI uri;
    protected Address userAddress;
    private String username;

    private class XCapCredentialsProvider implements CredentialsProvider {
        private Credentials credentials;

        private XCapCredentialsProvider() {
        }

        public void setCredentials(AuthScope authscope, Credentials credentials) {
            this.credentials = credentials;
        }

        public Credentials getCredentials(AuthScope authscope) {
            return this.credentials;
        }

        public void clear() {
            this.credentials = null;
        }
    }

    public BaseHttpXCapClient() {
        ServiceReference guiVerifyReference = SipActivator.getBundleContext().getServiceReference(CertificateService.class.getName());
        if (guiVerifyReference != null) {
            this.certificateVerification = (CertificateService) SipActivator.getBundleContext().getService(guiVerifyReference);
        }
    }

    public void connect(URI uri, Address userAddress, String username, String password) throws XCapException {
        if (userAddress.getURI().isSipURI()) {
            this.uri = uri;
            this.userAddress = (Address) userAddress.clone();
            this.username = username;
            if (password == null) {
                password = "";
            }
            this.password = password;
            this.connected = true;
            return;
        }
        throw new IllegalArgumentException("Address must contains SipUri");
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void disconnect() {
        this.uri = null;
        this.userAddress = null;
        this.password = null;
        this.connected = false;
    }

    public XCapHttpResponse get(XCapResourceId resourceId) throws XCapException {
        return get(getResourceURI(resourceId));
    }

    /* access modifiers changed from: protected */
    public XCapHttpResponse get(URI uri) throws XCapException {
        DefaultHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpGet getMethod = new HttpGet(uri);
            getMethod.setHeader("Connection", "close");
            XCapHttpResponse result = createResponse(httpClient.execute(getMethod));
            if (logger.isDebugEnabled()) {
                String contenString;
                byte[] contentBytes = result.getContent();
                if (contentBytes == null || result.getContentType() == null || result.getContentType().startsWith(PresContentClient.CONTENT_TYPE)) {
                    contenString = "";
                } else {
                    contenString = new String(contentBytes);
                }
                logger.debug(String.format("Getting resource %1s from the server content:%2s", new Object[]{uri.toString(), contenString}));
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            return result;
        } catch (UnknownHostException uhe) {
            showError(uhe, null, null);
            disconnect();
            throw new XCapException(uhe.getMessage(), uhe);
        } catch (IOException e) {
            String errorMessage = SipActivator.getResources().getI18NString("impl.protocol.sip.XCAP_ERROR_RESOURCE_ERR", new String[]{uri.toString(), this.userAddress.getDisplayName()});
            showError(e, null, errorMessage);
            throw new XCapException(errorMessage, e);
        } catch (Throwable th) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    static void showError(Exception ex, String title, String message) {
        if (title == null) {
            try {
                title = SipActivator.getResources().getI18NString("impl.protocol.sip.XCAP_ERROR_TITLE");
            } catch (Throwable t) {
                logger.error("Error for error dialog", t);
                return;
            }
        }
        if (message == null) {
            message = title + Separators.RETURN + ex.getClass().getName() + ": " + ex.getLocalizedMessage();
        }
        if (SipActivator.getUIService() != null) {
            SipActivator.getUIService().getPopupDialog().showMessagePopupDialog(message, title, 0);
        }
    }

    public XCapHttpResponse put(XCapResource resource) throws XCapException {
        DefaultHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpPut putMethod = new HttpPut(getResourceURI(resource.getId()));
            putMethod.setHeader("Connection", "close");
            StringEntity stringEntity = new StringEntity(resource.getContent());
            stringEntity.setContentType(resource.getContentType());
            stringEntity.setContentEncoding("UTF-8");
            putMethod.setEntity(stringEntity);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Puting resource %1s to the server %2s", new Object[]{resource.getId().toString(), resource.getContent()}));
            }
            XCapHttpResponse createResponse = createResponse(httpClient.execute(putMethod));
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            return createResponse;
        } catch (IOException e) {
            throw new XCapException(String.format("%1s resource cannot be put", new Object[]{resource.getId().toString()}), e);
        } catch (Throwable th) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public XCapHttpResponse delete(XCapResourceId resourceId) throws XCapException {
        assertConnected();
        DefaultHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpDelete deleteMethod = new HttpDelete(getResourceURI(resourceId));
            deleteMethod.setHeader("Connection", "close");
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Deleting resource %1s from the server", new Object[]{resourceId.toString()}));
            }
            XCapHttpResponse createResponse = createResponse(httpClient.execute(deleteMethod));
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            return createResponse;
        } catch (IOException e) {
            throw new XCapException(String.format("%1s resource cannot be deleted", new Object[]{resourceId.toString()}), e);
        } catch (Throwable th) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public String getUserName() {
        return this.username;
    }

    public URI getUri() {
        return this.uri;
    }

    /* access modifiers changed from: protected */
    public void assertConnected() {
        if (!this.connected) {
            throw new IllegalStateException("User is not connected to the server");
        }
    }

    /* access modifiers changed from: protected */
    public URI getResourceURI(XCapResourceId resourceId) {
        try {
            return new URI(this.uri.toString() + Separators.SLASH + resourceId);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid XCAP resource identifier", e);
        }
    }

    private DefaultHttpClient createHttpClient() throws IOException {
        XCapCredentialsProvider credentialsProvider = new XCapCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUserName(), this.password));
        return HttpUtils.getHttpClient(null, null, this.uri.getHost(), credentialsProvider);
    }

    private XCapHttpResponse createResponse(HttpResponse response) throws IOException {
        XCapHttpResponse xcapHttpResponse = new XCapHttpResponse();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == Response.OK || statusCode == 201 || statusCode == 409) {
            String contentType = getSingleHeaderValue(response, "Content-Type");
            byte[] content = StreamUtils.read(response.getEntity().getContent());
            String eTag = getSingleHeaderValue(response, HEADER_ETAG);
            xcapHttpResponse.setContentType(contentType);
            xcapHttpResponse.setContent(content);
            xcapHttpResponse.setETag(eTag);
        }
        xcapHttpResponse.setHttpCode(statusCode);
        return xcapHttpResponse;
    }

    private static String readResponse(HttpResponse response) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity.getContentLength() == 0) {
            return "";
        }
        return new String(StreamUtils.read(responseEntity.getContent()), "UTF-8");
    }

    protected static String getSingleHeaderValue(HttpResponse response, String headerName) {
        Header[] headers = response.getHeaders(headerName);
        if (headers == null || headers.length <= 0) {
            return null;
        }
        return headers[0].getValue();
    }

    /* access modifiers changed from: protected */
    public String getXCapErrorMessage(XCapHttpResponse response) {
        int httpCode = response.getHttpCode();
        String contentType = response.getContentType();
        if (httpCode != 409 || contentType == null) {
            return null;
        }
        try {
            if (!contentType.startsWith(XCAP_ERROR_CONTENT_TYPE)) {
                return null;
            }
            XCapError error = XCapErrorParser.fromXml(new String(response.getContent())).getError();
            if (error != null) {
                return error.getPhrase();
            }
            return null;
        } catch (ParsingException e) {
            logger.error("XCapError cannot be parsed.");
            return null;
        }
    }
}
