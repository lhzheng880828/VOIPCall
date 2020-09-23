package javax.media;

import java.io.Serializable;

public class Format implements Cloneable, Serializable {
    public static final int FALSE = 0;
    public static final int NOT_SPECIFIED = -1;
    public static final int TRUE = 1;
    public static final Class<?> byteArray = new byte[0].getClass();
    public static final Class<?> formatArray = new Format[0].getClass();
    public static final Class<?> intArray = new int[0].getClass();
    public static final Class<?> shortArray = new short[0].getClass();
    protected Class<?> clz;
    protected Class<?> dataType;
    protected String encoding;
    private long encodingCode;

    public Format(String encoding) {
        this.dataType = byteArray;
        this.clz = getClass();
        this.encodingCode = 0;
        this.encoding = encoding;
    }

    public Format(String encoding, Class<?> dataType) {
        this(encoding);
        this.dataType = dataType;
    }

    public Object clone() {
        Format f = new Format(this.encoding);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        this.dataType = f.dataType;
    }

    public boolean equals(Object format) {
        if (format == null || this.clz != ((Format) format).clz) {
            return false;
        }
        String otherEncoding = ((Format) format).encoding;
        return this.dataType == ((Format) format).dataType && (this.encoding == otherEncoding || !(this.encoding == null || otherEncoding == null || !isSameEncoding((Format) format)));
    }

    public Class<?> getDataType() {
        return this.dataType;
    }

    public String getEncoding() {
        return this.encoding;
    }

    private long getEncodingCode(String enc) {
        byte[] chars = enc.getBytes();
        long code = 0;
        for (int i = 0; i < enc.length(); i++) {
            byte b = chars[i];
            if (b > (byte) 96 && b < (byte) 123) {
                b = (byte) (b - 32);
            }
            b = (byte) (b - 32);
            if (b > (byte) 63) {
                return -1;
            }
            code = (code << 6) | ((long) b);
        }
        return code;
    }

    public Format intersects(Format other) {
        Format res;
        if (this.clz.isAssignableFrom(other.clz)) {
            res = (Format) other.clone();
        } else if (!other.clz.isAssignableFrom(this.clz)) {
            return null;
        } else {
            res = (Format) clone();
        }
        if (res.encoding == null) {
            res.encoding = this.encoding != null ? this.encoding : other.encoding;
        }
        if (res.dataType != null) {
            return res;
        }
        res.dataType = this.dataType != null ? this.dataType : other.dataType;
        return res;
    }

    public boolean isSameEncoding(Format other) {
        if (this.encoding == null || other == null || other.encoding == null) {
            return false;
        }
        if (this.encoding == other.encoding) {
            return true;
        }
        if (this.encodingCode <= 0 || other.encodingCode <= 0) {
            if (this.encoding.length() > 10) {
                return this.encoding.equalsIgnoreCase(other.encoding);
            }
            if (this.encodingCode == 0) {
                this.encodingCode = getEncodingCode(this.encoding);
            }
            if (this.encodingCode <= 0) {
                return this.encoding.equalsIgnoreCase(other.encoding);
            }
            if (other.encodingCode == 0) {
                return other.isSameEncoding(this);
            }
            if (this.encodingCode != other.encodingCode) {
                return false;
            }
            return true;
        } else if (this.encodingCode != other.encodingCode) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSameEncoding(String encoding) {
        if (this.encoding == null || encoding == null) {
            return false;
        }
        if (this.encoding == encoding) {
            return true;
        }
        if (this.encoding.length() > 10) {
            return this.encoding.equalsIgnoreCase(encoding);
        }
        if (this.encodingCode == 0) {
            this.encodingCode = getEncodingCode(this.encoding);
        }
        if (this.encodingCode < 0) {
            return this.encoding.equalsIgnoreCase(encoding);
        }
        if (this.encodingCode != getEncodingCode(encoding)) {
            return false;
        }
        return true;
    }

    public boolean matches(Format format) {
        if (format == null) {
            return false;
        }
        if (format.encoding != null && this.encoding != null && !isSameEncoding(format)) {
            return false;
        }
        if (format.dataType != null && this.dataType != null && format.dataType != this.dataType) {
            return false;
        }
        if (this.clz.isAssignableFrom(format.clz) || format.clz.isAssignableFrom(this.clz)) {
            return true;
        }
        return false;
    }

    public Format relax() {
        return (Format) clone();
    }

    public String toString() {
        return getEncoding();
    }
}
