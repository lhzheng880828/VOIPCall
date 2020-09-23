package gov.nist.javax.sdp.fields;

import java.net.URL;
import javax.sdp.URI;
import org.jitsi.gov.nist.core.Separators;

public class URIField extends SDPField implements URI {
    private static final long serialVersionUID = -4322063343955734258L;
    protected URL url;
    protected String urlString;

    public URIField() {
        super(SDPFieldNames.URI_FIELD);
    }

    public String getURI() {
        return this.urlString;
    }

    public void setURI(String uri) {
        this.urlString = uri;
        this.url = null;
    }

    public URL get() {
        if (this.url != null) {
            return this.url;
        }
        try {
            this.url = new URL(this.urlString);
            return this.url;
        } catch (Exception e) {
            return null;
        }
    }

    public void set(URL uri) {
        this.url = uri;
        this.urlString = null;
    }

    public String encode() {
        if (this.urlString != null) {
            return SDPFieldNames.URI_FIELD + this.urlString + Separators.NEWLINE;
        }
        if (this.url != null) {
            return SDPFieldNames.URI_FIELD + this.url.toString() + Separators.NEWLINE;
        }
        return "";
    }
}
