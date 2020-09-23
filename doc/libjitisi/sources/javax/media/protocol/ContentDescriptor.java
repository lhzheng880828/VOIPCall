package javax.media.protocol;

import javax.media.Format;

public class ContentDescriptor extends Format {
    public static final String CONTENT_UNKNOWN = "UnknownContent";
    public static final String MIXED = "application.mixed-data";
    public static final String RAW = "raw";
    public static final String RAW_RTP = "raw.rtp";

    private static final boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static final boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    private static final boolean isUpperAlpha(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static final String mimeTypeToPackageName(String mimeType) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < mimeType.length(); i++) {
            char c = mimeType.charAt(i);
            if (c == '/' || c == '.') {
                b.append('.');
            } else if (!isAlpha(c) && !isNumeric(c)) {
                b.append('_');
            } else if (isUpperAlpha(c)) {
                b.append(Character.toLowerCase(c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    public ContentDescriptor(String cdName) {
        super(cdName);
        this.dataType = byteArray;
    }

    public String getContentType() {
        return super.getEncoding();
    }

    public String toString() {
        return getContentType();
    }
}
