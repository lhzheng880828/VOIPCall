package org.xmlpull.mxp1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.org.xmlpull.v1.XmlPullParserException;

public class MXParser implements XmlPullParser {
    protected static final String FEATURE_NAMES_INTERNED = "http://xmlpull.org/v1/doc/features.html#names-interned";
    protected static final String FEATURE_XML_ROUNDTRIP = "http://xmlpull.org/v1/doc/features.html#xml-roundtrip";
    protected static final int LOOKUP_MAX = 1024;
    protected static final char LOOKUP_MAX_CHAR = 'Ѐ';
    protected static final char[] NCODING = "ncoding".toCharArray();
    protected static final char[] NO = AuthorizationHeaderIms.NO.toCharArray();
    protected static final String PROPERTY_LOCATION = "http://xmlpull.org/v1/doc/properties.html#location";
    protected static final String PROPERTY_XMLDECL_CONTENT = "http://xmlpull.org/v1/doc/properties.html#xmldecl-content";
    protected static final String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";
    protected static final String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
    protected static final int READ_CHUNK_SIZE = 8192;
    protected static final char[] TANDALONE = "tandalone".toCharArray();
    private static final boolean TRACE_SIZING = false;
    protected static final char[] VERSION = "version".toCharArray();
    protected static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    protected static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    protected static final char[] YES = AuthorizationHeaderIms.YES.toCharArray();
    protected static boolean[] lookupNameChar = new boolean[LOOKUP_MAX];
    protected static boolean[] lookupNameStartChar = new boolean[LOOKUP_MAX];
    protected boolean allStringsInterned;
    protected int attributeCount;
    protected String[] attributeName;
    protected int[] attributeNameHash;
    protected String[] attributePrefix;
    protected String[] attributeUri;
    protected String[] attributeValue;
    protected char[] buf;
    protected int bufAbsoluteStart;
    protected int bufEnd;
    protected int bufLoadFactor = 95;
    protected int bufSoftLimit;
    protected int bufStart;
    protected char[] charRefOneCharBuf;
    protected int columnNumber;
    protected int depth;
    protected String[] elName;
    protected int[] elNamespaceCount;
    protected String[] elPrefix;
    protected char[][] elRawName;
    protected int[] elRawNameEnd;
    protected int[] elRawNameLine;
    protected String[] elUri;
    protected boolean emptyElementTag;
    protected int entityEnd;
    protected String[] entityName;
    protected char[][] entityNameBuf;
    protected int[] entityNameHash;
    protected String entityRefName;
    protected String[] entityReplacement;
    protected char[][] entityReplacementBuf;
    protected int eventType;
    protected String inputEncoding;
    protected InputStream inputStream;
    protected int lineNumber;
    protected String location;
    protected int namespaceEnd;
    protected String[] namespacePrefix;
    protected int[] namespacePrefixHash;
    protected String[] namespaceUri;
    protected boolean pastEndTag;
    protected char[] pc;
    protected int pcEnd;
    protected int pcStart;
    protected int pos;
    protected int posEnd;
    protected int posStart;
    protected boolean preventBufferCompaction;
    protected boolean processNamespaces;
    protected boolean reachedEnd;
    protected Reader reader;
    protected boolean roundtripSupported;
    protected boolean seenAmpersand;
    protected boolean seenDocdecl;
    protected boolean seenEndTag;
    protected boolean seenMarkup;
    protected boolean seenRoot;
    protected boolean seenStartTag;
    protected String text;
    protected boolean tokenize;
    protected boolean usePC;
    protected String xmlDeclContent;
    protected Boolean xmlDeclStandalone;
    protected String xmlDeclVersion;

    /* access modifiers changed from: protected */
    public void resetStringCache() {
    }

    /* access modifiers changed from: protected */
    public String newString(char[] cbuf, int off, int len) {
        return new String(cbuf, off, len);
    }

    /* access modifiers changed from: protected */
    public String newStringIntern(char[] cbuf, int off, int len) {
        return new String(cbuf, off, len).intern();
    }

    /* access modifiers changed from: protected */
    public void ensureElementsCapacity() {
        int elStackSize;
        if (this.elName != null) {
            elStackSize = this.elName.length;
        } else {
            elStackSize = 0;
        }
        if (this.depth + 1 >= elStackSize) {
            int newSize = (this.depth >= 7 ? this.depth * 2 : 8) + 2;
            boolean needsCopying = elStackSize > 0 ? true : TRACE_SIZING;
            String[] arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elName, 0, arr, 0, elStackSize);
            }
            this.elName = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elPrefix, 0, arr, 0, elStackSize);
            }
            this.elPrefix = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elUri, 0, arr, 0, elStackSize);
            }
            this.elUri = arr;
            int[] iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elNamespaceCount, 0, iarr, 0, elStackSize);
            } else {
                iarr[0] = 0;
            }
            this.elNamespaceCount = iarr;
            iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elRawNameEnd, 0, iarr, 0, elStackSize);
            }
            this.elRawNameEnd = iarr;
            iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elRawNameLine, 0, iarr, 0, elStackSize);
            }
            this.elRawNameLine = iarr;
            char[][] carr = new char[newSize][];
            if (needsCopying) {
                System.arraycopy(this.elRawName, 0, carr, 0, elStackSize);
            }
            this.elRawName = carr;
        }
    }

    /* access modifiers changed from: protected */
    public void ensureAttributesCapacity(int size) {
        int attrPosSize;
        if (this.attributeName != null) {
            attrPosSize = this.attributeName.length;
        } else {
            attrPosSize = 0;
        }
        if (size >= attrPosSize) {
            int newSize = size > 7 ? size * 2 : 8;
            boolean needsCopying = attrPosSize > 0 ? true : TRACE_SIZING;
            String[] arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeName, 0, arr, 0, attrPosSize);
            }
            this.attributeName = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributePrefix, 0, arr, 0, attrPosSize);
            }
            this.attributePrefix = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeUri, 0, arr, 0, attrPosSize);
            }
            this.attributeUri = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeValue, 0, arr, 0, attrPosSize);
            }
            this.attributeValue = arr;
            if (!this.allStringsInterned) {
                int[] iarr = new int[newSize];
                if (needsCopying) {
                    System.arraycopy(this.attributeNameHash, 0, iarr, 0, attrPosSize);
                }
                this.attributeNameHash = iarr;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void ensureNamespacesCapacity(int size) {
        int namespaceSize;
        if (this.namespacePrefix != null) {
            namespaceSize = this.namespacePrefix.length;
        } else {
            namespaceSize = 0;
        }
        if (size >= namespaceSize) {
            int newSize = size > 7 ? size * 2 : 8;
            String[] newNamespacePrefix = new String[newSize];
            String[] newNamespaceUri = new String[newSize];
            if (this.namespacePrefix != null) {
                System.arraycopy(this.namespacePrefix, 0, newNamespacePrefix, 0, this.namespaceEnd);
                System.arraycopy(this.namespaceUri, 0, newNamespaceUri, 0, this.namespaceEnd);
            }
            this.namespacePrefix = newNamespacePrefix;
            this.namespaceUri = newNamespaceUri;
            if (!this.allStringsInterned) {
                int[] newNamespacePrefixHash = new int[newSize];
                if (this.namespacePrefixHash != null) {
                    System.arraycopy(this.namespacePrefixHash, 0, newNamespacePrefixHash, 0, this.namespaceEnd);
                }
                this.namespacePrefixHash = newNamespacePrefixHash;
            }
        }
    }

    protected static final int fastHash(char[] ch, int off, int len) {
        if (len == 0) {
            return 0;
        }
        int hash = (ch[off] << 7) + ch[(off + len) - 1];
        if (len > 16) {
            hash = (hash << 7) + ch[(len / 4) + off];
        }
        if (len > 8) {
            return (hash << 7) + ch[(len / 2) + off];
        }
        return hash;
    }

    /* access modifiers changed from: protected */
    public void ensureEntityCapacity() {
        int entitySize;
        if (this.entityReplacementBuf != null) {
            entitySize = this.entityReplacementBuf.length;
        } else {
            entitySize = 0;
        }
        if (this.entityEnd >= entitySize) {
            int newSize = this.entityEnd > 7 ? this.entityEnd * 2 : 8;
            String[] newEntityName = new String[newSize];
            char[][] newEntityNameBuf = new char[newSize][];
            String[] newEntityReplacement = new String[newSize];
            char[][] newEntityReplacementBuf = new char[newSize][];
            if (this.entityName != null) {
                System.arraycopy(this.entityName, 0, newEntityName, 0, this.entityEnd);
                System.arraycopy(this.entityNameBuf, 0, newEntityNameBuf, 0, this.entityEnd);
                System.arraycopy(this.entityReplacement, 0, newEntityReplacement, 0, this.entityEnd);
                System.arraycopy(this.entityReplacementBuf, 0, newEntityReplacementBuf, 0, this.entityEnd);
            }
            this.entityName = newEntityName;
            this.entityNameBuf = newEntityNameBuf;
            this.entityReplacement = newEntityReplacement;
            this.entityReplacementBuf = newEntityReplacementBuf;
            if (!this.allStringsInterned) {
                int[] newEntityNameHash = new int[newSize];
                if (this.entityNameHash != null) {
                    System.arraycopy(this.entityNameHash, 0, newEntityNameHash, 0, this.entityEnd);
                }
                this.entityNameHash = newEntityNameHash;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.location = null;
        this.lineNumber = 1;
        this.columnNumber = 0;
        this.seenRoot = TRACE_SIZING;
        this.reachedEnd = TRACE_SIZING;
        this.eventType = 0;
        this.emptyElementTag = TRACE_SIZING;
        this.depth = 0;
        this.attributeCount = 0;
        this.namespaceEnd = 0;
        this.entityEnd = 0;
        this.reader = null;
        this.inputEncoding = null;
        this.preventBufferCompaction = TRACE_SIZING;
        this.bufAbsoluteStart = 0;
        this.bufStart = 0;
        this.bufEnd = 0;
        this.posEnd = 0;
        this.posStart = 0;
        this.pos = 0;
        this.pcStart = 0;
        this.pcEnd = 0;
        this.usePC = TRACE_SIZING;
        this.seenStartTag = TRACE_SIZING;
        this.seenEndTag = TRACE_SIZING;
        this.pastEndTag = TRACE_SIZING;
        this.seenAmpersand = TRACE_SIZING;
        this.seenMarkup = TRACE_SIZING;
        this.seenDocdecl = TRACE_SIZING;
        this.xmlDeclVersion = null;
        this.xmlDeclStandalone = null;
        this.xmlDeclContent = null;
        resetStringCache();
    }

    public MXParser() {
        int i = READ_CHUNK_SIZE;
        this.buf = new char[(Runtime.getRuntime().freeMemory() > 1000000 ? READ_CHUNK_SIZE : 256)];
        this.bufSoftLimit = (this.bufLoadFactor * this.buf.length) / 100;
        if (Runtime.getRuntime().freeMemory() <= 1000000) {
            i = 64;
        }
        this.pc = new char[i];
        this.charRefOneCharBuf = new char[1];
    }

    public void setFeature(String name, boolean state) throws XmlPullParserException {
        if (name == null) {
            throw new IllegalArgumentException("feature name should not be null");
        } else if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(name)) {
            if (this.eventType != 0) {
                throw new XmlPullParserException("namespace processing feature can only be changed before parsing", this, null);
            }
            this.processNamespaces = state;
        } else if (FEATURE_NAMES_INTERNED.equals(name)) {
            if (state) {
                throw new XmlPullParserException("interning names in this implementation is not supported");
            }
        } else if (XmlPullParser.FEATURE_PROCESS_DOCDECL.equals(name)) {
            if (state) {
                throw new XmlPullParserException("processing DOCDECL is not supported");
            }
        } else if (FEATURE_XML_ROUNDTRIP.equals(name)) {
            this.roundtripSupported = state;
        } else {
            throw new XmlPullParserException(new StringBuffer().append("unsupported feature ").append(name).toString());
        }
    }

    public boolean getFeature(String name) {
        if (name == null) {
            throw new IllegalArgumentException("feature name should not be null");
        } else if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(name)) {
            return this.processNamespaces;
        } else {
            if (FEATURE_NAMES_INTERNED.equals(name) || XmlPullParser.FEATURE_PROCESS_DOCDECL.equals(name) || !FEATURE_XML_ROUNDTRIP.equals(name)) {
                return TRACE_SIZING;
            }
            return this.roundtripSupported;
        }
    }

    public void setProperty(String name, Object value) throws XmlPullParserException {
        if (PROPERTY_LOCATION.equals(name)) {
            this.location = (String) value;
            return;
        }
        throw new XmlPullParserException(new StringBuffer().append("unsupported property: '").append(name).append(Separators.QUOTE).toString());
    }

    public Object getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("property name should not be null");
        } else if (PROPERTY_XMLDECL_VERSION.equals(name)) {
            return this.xmlDeclVersion;
        } else {
            if (PROPERTY_XMLDECL_STANDALONE.equals(name)) {
                return this.xmlDeclStandalone;
            }
            if (PROPERTY_XMLDECL_CONTENT.equals(name)) {
                return this.xmlDeclContent;
            }
            if (PROPERTY_LOCATION.equals(name)) {
                return this.location;
            }
            return null;
        }
    }

    public void setInput(Reader in) throws XmlPullParserException {
        reset();
        this.reader = in;
    }

    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        if (inputStream == null) {
            throw new IllegalArgumentException("input stream can not be null");
        }
        Reader reader;
        this.inputStream = inputStream;
        if (inputEncoding != null) {
            try {
                reader = new InputStreamReader(inputStream, inputEncoding);
            } catch (UnsupportedEncodingException une) {
                throw new XmlPullParserException(new StringBuffer().append("could not create reader for encoding ").append(inputEncoding).append(" : ").append(une).toString(), this, une);
            }
        }
        reader = new InputStreamReader(inputStream, "UTF-8");
        setInput(reader);
        this.inputEncoding = inputEncoding;
    }

    public String getInputEncoding() {
        return this.inputEncoding;
    }

    public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
        ensureEntityCapacity();
        this.entityName[this.entityEnd] = newString(entityName.toCharArray(), 0, entityName.length());
        this.entityNameBuf[this.entityEnd] = entityName.toCharArray();
        this.entityReplacement[this.entityEnd] = replacementText;
        this.entityReplacementBuf[this.entityEnd] = replacementText.toCharArray();
        if (!this.allStringsInterned) {
            this.entityNameHash[this.entityEnd] = fastHash(this.entityNameBuf[this.entityEnd], 0, this.entityNameBuf[this.entityEnd].length);
        }
        this.entityEnd++;
    }

    public int getNamespaceCount(int depth) throws XmlPullParserException {
        if (!this.processNamespaces || depth == 0) {
            return 0;
        }
        if (depth >= 0 && depth <= this.depth) {
            return this.elNamespaceCount[depth];
        }
        throw new IllegalArgumentException(new StringBuffer().append("allowed namespace depth 0..").append(this.depth).append(" not ").append(depth).toString());
    }

    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        if (pos < this.namespaceEnd) {
            return this.namespacePrefix[pos];
        }
        throw new XmlPullParserException(new StringBuffer().append("position ").append(pos).append(" exceeded number of available namespaces ").append(this.namespaceEnd).toString());
    }

    public String getNamespaceUri(int pos) throws XmlPullParserException {
        if (pos < this.namespaceEnd) {
            return this.namespaceUri[pos];
        }
        throw new XmlPullParserException(new StringBuffer().append("position ").append(pos).append(" exceeded number of available namespaces ").append(this.namespaceEnd).toString());
    }

    public String getNamespace(String prefix) {
        int i;
        if (prefix != null) {
            for (i = this.namespaceEnd - 1; i >= 0; i--) {
                if (prefix.equals(this.namespacePrefix[i])) {
                    return this.namespaceUri[i];
                }
            }
            if ("xml".equals(prefix)) {
                return XML_URI;
            }
            if ("xmlns".equals(prefix)) {
                return XMLNS_URI;
            }
        }
        for (i = this.namespaceEnd - 1; i >= 0; i--) {
            if (this.namespacePrefix[i] == null) {
                return this.namespaceUri[i];
            }
        }
        return null;
    }

    public int getDepth() {
        return this.depth;
    }

    private static int findFragment(int bufMinPos, char[] b, int start, int end) {
        if (start < bufMinPos) {
            start = bufMinPos;
            if (start > end) {
                start = end;
            }
            return start;
        }
        if (end - start > 65) {
            start = end - 10;
        }
        int i = start + 1;
        while (true) {
            i--;
            if (i <= bufMinPos || end - i > 65) {
                return i;
            }
            if (b[i] == '<' && start - i > 10) {
                return i;
            }
        }
    }

    public String getPositionDescription() {
        String fragment = null;
        if (this.posStart <= this.pos) {
            int start = findFragment(0, this.buf, this.posStart, this.pos);
            if (start < this.pos) {
                fragment = new String(this.buf, start, this.pos - start);
            }
            if (this.bufAbsoluteStart > 0 || start > 0) {
                fragment = new StringBuffer().append("...").append(fragment).toString();
            }
        }
        return new StringBuffer().append(Separators.SP).append(XmlPullParser.TYPES[this.eventType]).append(fragment != null ? new StringBuffer().append(" seen ").append(printable(fragment)).append("...").toString() : "").append(Separators.SP).append(this.location != null ? this.location : "").append(Separators.AT).append(getLineNumber()).append(Separators.COLON).append(getColumnNumber()).toString();
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (this.eventType == 4 || this.eventType == 5) {
            int i;
            if (this.usePC) {
                for (i = this.pcStart; i < this.pcEnd; i++) {
                    if (!isS(this.pc[i])) {
                        return TRACE_SIZING;
                    }
                }
                return true;
            }
            for (i = this.posStart; i < this.posEnd; i++) {
                if (!isS(this.buf[i])) {
                    return TRACE_SIZING;
                }
            }
            return true;
        } else if (this.eventType == 7) {
            return true;
        } else {
            throw new XmlPullParserException("no content available to check for white spaces");
        }
    }

    public String getText() {
        if (this.eventType == 0 || this.eventType == 1) {
            return null;
        }
        if (this.eventType == 6) {
            return this.text;
        }
        if (this.text == null) {
            if (!this.usePC || this.eventType == 2 || this.eventType == 3) {
                this.text = new String(this.buf, this.posStart, this.posEnd - this.posStart);
            } else {
                this.text = new String(this.pc, this.pcStart, this.pcEnd - this.pcStart);
            }
        }
        return this.text;
    }

    public char[] getTextCharacters(int[] holderForStartAndLength) {
        if (this.eventType == 4) {
            if (this.usePC) {
                holderForStartAndLength[0] = this.pcStart;
                holderForStartAndLength[1] = this.pcEnd - this.pcStart;
                return this.pc;
            }
            holderForStartAndLength[0] = this.posStart;
            holderForStartAndLength[1] = this.posEnd - this.posStart;
            return this.buf;
        } else if (this.eventType == 2 || this.eventType == 3 || this.eventType == 5 || this.eventType == 9 || this.eventType == 6 || this.eventType == 8 || this.eventType == 7 || this.eventType == 10) {
            holderForStartAndLength[0] = this.posStart;
            holderForStartAndLength[1] = this.posEnd - this.posStart;
            return this.buf;
        } else if (this.eventType == 0 || this.eventType == 1) {
            holderForStartAndLength[1] = -1;
            holderForStartAndLength[0] = -1;
            return null;
        } else {
            throw new IllegalArgumentException(new StringBuffer().append("unknown text eventType: ").append(this.eventType).toString());
        }
    }

    public String getNamespace() {
        if (this.eventType == 2) {
            return this.processNamespaces ? this.elUri[this.depth] : "";
        } else {
            if (this.eventType == 3) {
                return this.processNamespaces ? this.elUri[this.depth] : "";
            } else {
                return null;
            }
        }
    }

    public String getName() {
        if (this.eventType == 2) {
            return this.elName[this.depth];
        }
        if (this.eventType == 3) {
            return this.elName[this.depth];
        }
        if (this.eventType != 6) {
            return null;
        }
        if (this.entityRefName == null) {
            this.entityRefName = newString(this.buf, this.posStart, this.posEnd - this.posStart);
        }
        return this.entityRefName;
    }

    public String getPrefix() {
        if (this.eventType == 2) {
            return this.elPrefix[this.depth];
        }
        if (this.eventType == 3) {
            return this.elPrefix[this.depth];
        }
        return null;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.eventType == 2) {
            return this.emptyElementTag;
        }
        throw new XmlPullParserException("parser must be on START_TAG to check for empty element", this, null);
    }

    public int getAttributeCount() {
        if (this.eventType != 2) {
            return -1;
        }
        return this.attributeCount;
    }

    public String getAttributeNamespace(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (!this.processNamespaces) {
            return "";
        } else {
            if (index >= 0 && index < this.attributeCount) {
                return this.attributeUri[index];
            }
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public String getAttributeName(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (index >= 0 && index < this.attributeCount) {
            return this.attributeName[index];
        } else {
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public String getAttributePrefix(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (!this.processNamespaces) {
            return null;
        } else {
            if (index >= 0 && index < this.attributeCount) {
                return this.attributePrefix[index];
            }
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public String getAttributeType(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (index >= 0 && index < this.attributeCount) {
            return "CDATA";
        } else {
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public boolean isAttributeDefault(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (index >= 0 && index < this.attributeCount) {
            return TRACE_SIZING;
        } else {
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public String getAttributeValue(int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        } else if (index >= 0 && index < this.attributeCount) {
            return this.attributeValue[index];
        } else {
            throw new IndexOutOfBoundsException(new StringBuffer().append("attribute position must be 0..").append(this.attributeCount - 1).append(" and not ").append(index).toString());
        }
    }

    public String getAttributeValue(String namespace, String name) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException(new StringBuffer().append("only START_TAG can have attributes").append(getPositionDescription()).toString());
        } else if (name == null) {
            throw new IllegalArgumentException("attribute name can not be null");
        } else {
            int i;
            if (this.processNamespaces) {
                if (namespace == null) {
                    namespace = "";
                }
                i = 0;
                while (i < this.attributeCount) {
                    if ((namespace == this.attributeUri[i] || namespace.equals(this.attributeUri[i])) && name.equals(this.attributeName[i])) {
                        return this.attributeValue[i];
                    }
                    i++;
                }
            } else {
                if (namespace != null && namespace.length() == 0) {
                    namespace = null;
                }
                if (namespace != null) {
                    throw new IllegalArgumentException("when namespaces processing is disabled attribute namespace must be null");
                }
                for (i = 0; i < this.attributeCount; i++) {
                    if (name.equals(this.attributeName[i])) {
                        return this.attributeValue[i];
                    }
                }
            }
            return null;
        }
    }

    public int getEventType() throws XmlPullParserException {
        return this.eventType;
    }

    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (!this.processNamespaces && namespace != null) {
            throw new XmlPullParserException(new StringBuffer().append("processing namespaces must be enabled on parser (or factory) to have possible namespaces declared on elements").append(" (position:").append(getPositionDescription()).append(Separators.RPAREN).toString());
        } else if (type != getEventType() || ((namespace != null && !namespace.equals(getNamespace())) || (name != null && !name.equals(getName())))) {
            StringBuffer append = new StringBuffer().append("expected event ").append(XmlPullParser.TYPES[type]).append(name != null ? new StringBuffer().append(" with name '").append(name).append(Separators.QUOTE).toString() : "");
            String str = (namespace == null || name == null) ? "" : " and";
            append = append.append(str).append(namespace != null ? new StringBuffer().append(" with namespace '").append(namespace).append(Separators.QUOTE).toString() : "").append(" but got").append(type != getEventType() ? new StringBuffer().append(Separators.SP).append(XmlPullParser.TYPES[getEventType()]).toString() : "");
            str = (name == null || getName() == null || name.equals(getName())) ? "" : new StringBuffer().append(" name '").append(getName()).append(Separators.QUOTE).toString();
            append = append.append(str);
            str = (namespace == null || name == null || getName() == null || name.equals(getName()) || getNamespace() == null || namespace.equals(getNamespace())) ? "" : " and";
            append = append.append(str);
            str = (namespace == null || getNamespace() == null || namespace.equals(getNamespace())) ? "" : new StringBuffer().append(" namespace '").append(getNamespace()).append(Separators.QUOTE).toString();
            throw new XmlPullParserException(append.append(str).append(" (position:").append(getPositionDescription()).append(Separators.RPAREN).toString());
        }
    }

    public void skipSubTree() throws XmlPullParserException, IOException {
        require(2, null, null);
        int level = 1;
        while (level > 0) {
            int eventType = next();
            if (eventType == 3) {
                level--;
            } else if (eventType == 2) {
                level++;
            }
        }
    }

    public String nextText() throws XmlPullParserException, IOException {
        if (getEventType() != 2) {
            throw new XmlPullParserException("parser must be on START_TAG to read next text", this, null);
        }
        int eventType = next();
        if (eventType == 4) {
            String text = getText();
            if (next() == 3) {
                return text;
            }
            throw new XmlPullParserException(new StringBuffer().append("TEXT must be immediately followed by END_TAG and not ").append(XmlPullParser.TYPES[getEventType()]).toString(), this, null);
        } else if (eventType == 3) {
            return "";
        } else {
            throw new XmlPullParserException("parser must be on START_TAG or TEXT to read text", this, null);
        }
    }

    public int nextTag() throws XmlPullParserException, IOException {
        next();
        if (this.eventType == 4 && isWhitespace()) {
            next();
        }
        if (this.eventType == 2 || this.eventType == 3) {
            return this.eventType;
        }
        throw new XmlPullParserException(new StringBuffer().append("expected START_TAG or END_TAG not ").append(XmlPullParser.TYPES[getEventType()]).toString(), this, null);
    }

    public int next() throws XmlPullParserException, IOException {
        this.tokenize = TRACE_SIZING;
        return nextImpl();
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.tokenize = true;
        return nextImpl();
    }

    /* access modifiers changed from: protected */
    public int nextImpl() throws XmlPullParserException, IOException {
        this.text = null;
        this.pcStart = 0;
        this.pcEnd = 0;
        this.usePC = TRACE_SIZING;
        this.bufStart = this.posEnd;
        if (this.pastEndTag) {
            this.pastEndTag = TRACE_SIZING;
            this.depth--;
            this.namespaceEnd = this.elNamespaceCount[this.depth];
        }
        if (this.emptyElementTag) {
            this.emptyElementTag = TRACE_SIZING;
            this.pastEndTag = true;
            this.eventType = 3;
            return 3;
        } else if (this.depth > 0) {
            int parseStartTag;
            if (this.seenStartTag) {
                this.seenStartTag = TRACE_SIZING;
                parseStartTag = parseStartTag();
                this.eventType = parseStartTag;
                return parseStartTag;
            } else if (this.seenEndTag) {
                this.seenEndTag = TRACE_SIZING;
                parseStartTag = parseEndTag();
                this.eventType = parseStartTag;
                return parseStartTag;
            } else {
                char ch;
                if (this.seenMarkup) {
                    this.seenMarkup = TRACE_SIZING;
                    ch = '<';
                } else if (this.seenAmpersand) {
                    this.seenAmpersand = TRACE_SIZING;
                    ch = '&';
                } else {
                    ch = more();
                }
                this.posStart = this.pos - 1;
                boolean hadCharData = TRACE_SIZING;
                boolean needsMerging = TRACE_SIZING;
                while (true) {
                    char[] cArr;
                    int i;
                    if (ch == '<') {
                        if (hadCharData && this.tokenize) {
                            this.seenMarkup = true;
                            this.eventType = 4;
                            return 4;
                        }
                        ch = more();
                        if (ch == '/') {
                            if (this.tokenize || !hadCharData) {
                                parseStartTag = parseEndTag();
                                this.eventType = parseStartTag;
                                return parseStartTag;
                            }
                            this.seenEndTag = true;
                            this.eventType = 4;
                            return 4;
                        } else if (ch == '!') {
                            ch = more();
                            if (ch == '-') {
                                parseComment();
                                if (this.tokenize) {
                                    this.eventType = 9;
                                    return 9;
                                } else if (this.usePC || !hadCharData) {
                                    this.posStart = this.pos;
                                } else {
                                    needsMerging = true;
                                }
                            } else if (ch == '[') {
                                parseCDSect(hadCharData);
                                if (this.tokenize) {
                                    this.eventType = 5;
                                    return 5;
                                }
                                if (this.posEnd - this.posStart > 0) {
                                    hadCharData = true;
                                    if (!this.usePC) {
                                        needsMerging = true;
                                    }
                                }
                            } else {
                                throw new XmlPullParserException(new StringBuffer().append("unexpected character in markup ").append(printable(ch)).toString(), this, null);
                            }
                        } else if (ch == '?') {
                            parsePI();
                            if (this.tokenize) {
                                this.eventType = 8;
                                return 8;
                            } else if (this.usePC || !hadCharData) {
                                this.posStart = this.pos;
                            } else {
                                needsMerging = true;
                            }
                        } else if (!isNameStartChar(ch)) {
                            throw new XmlPullParserException(new StringBuffer().append("unexpected character in markup ").append(printable(ch)).toString(), this, null);
                        } else if (this.tokenize || !hadCharData) {
                            parseStartTag = parseStartTag();
                            this.eventType = parseStartTag;
                            return parseStartTag;
                        } else {
                            this.seenStartTag = true;
                            this.eventType = 4;
                            return 4;
                        }
                    } else if (ch != '&') {
                        if (needsMerging) {
                            joinPC();
                            needsMerging = TRACE_SIZING;
                        }
                        hadCharData = true;
                        boolean normalizedCR = TRACE_SIZING;
                        boolean normalizeInput = (this.tokenize && this.roundtripSupported) ? TRACE_SIZING : true;
                        boolean seenBracket = TRACE_SIZING;
                        int seenBracketBracket = 0;
                        do {
                            if (ch == ']') {
                                if (seenBracket) {
                                    seenBracketBracket = 1;
                                } else {
                                    seenBracket = true;
                                }
                            } else if (seenBracketBracket != 0 && ch == '>') {
                                throw new XmlPullParserException("characters ]]> are not allowed in content", this, null);
                            } else if (seenBracket) {
                                seenBracket = TRACE_SIZING;
                                seenBracketBracket = 0;
                            }
                            if (normalizeInput) {
                                if (ch == 13) {
                                    normalizedCR = true;
                                    this.posEnd = this.pos - 1;
                                    if (!this.usePC) {
                                        if (this.posEnd > this.posStart) {
                                            joinPC();
                                        } else {
                                            this.usePC = true;
                                            this.pcEnd = 0;
                                            this.pcStart = 0;
                                        }
                                    }
                                    if (this.pcEnd >= this.pc.length) {
                                        ensurePC(this.pcEnd);
                                    }
                                    cArr = this.pc;
                                    i = this.pcEnd;
                                    this.pcEnd = i + 1;
                                    cArr[i] = 10;
                                } else if (ch == 10) {
                                    if (!normalizedCR && this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            ensurePC(this.pcEnd);
                                        }
                                        cArr = this.pc;
                                        i = this.pcEnd;
                                        this.pcEnd = i + 1;
                                        cArr[i] = 10;
                                    }
                                    normalizedCR = TRACE_SIZING;
                                } else {
                                    if (this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            ensurePC(this.pcEnd);
                                        }
                                        cArr = this.pc;
                                        i = this.pcEnd;
                                        this.pcEnd = i + 1;
                                        cArr[i] = ch;
                                    }
                                    normalizedCR = TRACE_SIZING;
                                }
                            }
                            ch = more();
                            if (ch == '<') {
                                break;
                            }
                        } while (ch != '&');
                        this.posEnd = this.pos - 1;
                    } else if (this.tokenize && hadCharData) {
                        this.seenAmpersand = true;
                        this.eventType = 4;
                        return 4;
                    } else {
                        int oldStart = this.posStart + this.bufAbsoluteStart;
                        int oldEnd = this.posEnd + this.bufAbsoluteStart;
                        char[] resolvedEntity = parseEntityRef();
                        if (this.tokenize) {
                            this.eventType = 6;
                            return 6;
                        } else if (resolvedEntity == null) {
                            if (this.entityRefName == null) {
                                this.entityRefName = newString(this.buf, this.posStart, this.posEnd - this.posStart);
                            }
                            throw new XmlPullParserException(new StringBuffer().append("could not resolve entity named '").append(printable(this.entityRefName)).append(Separators.QUOTE).toString(), this, null);
                        } else {
                            this.posStart = oldStart - this.bufAbsoluteStart;
                            this.posEnd = oldEnd - this.bufAbsoluteStart;
                            if (!this.usePC) {
                                if (hadCharData) {
                                    joinPC();
                                    needsMerging = TRACE_SIZING;
                                } else {
                                    this.usePC = true;
                                    this.pcEnd = 0;
                                    this.pcStart = 0;
                                }
                            }
                            for (char c : resolvedEntity) {
                                if (this.pcEnd >= this.pc.length) {
                                    ensurePC(this.pcEnd);
                                }
                                cArr = this.pc;
                                i = this.pcEnd;
                                this.pcEnd = i + 1;
                                cArr[i] = c;
                            }
                            hadCharData = true;
                        }
                    }
                    ch = more();
                }
            }
        } else if (this.seenRoot) {
            return parseEpilog();
        } else {
            return parseProlog();
        }
    }

    /* access modifiers changed from: protected */
    public int parseProlog() throws XmlPullParserException, IOException {
        char ch;
        if (this.seenMarkup) {
            ch = this.buf[this.pos - 1];
        } else {
            ch = more();
        }
        if (this.eventType == 0) {
            if (ch == 65534) {
                throw new XmlPullParserException("first character in input was UNICODE noncharacter (0xFFFE)- input requires int swapping", this, null);
            } else if (ch == 65279) {
                ch = more();
            }
        }
        this.seenMarkup = TRACE_SIZING;
        boolean gotS = TRACE_SIZING;
        this.posStart = this.pos - 1;
        boolean normalizeIgnorableWS = (!this.tokenize || this.roundtripSupported) ? TRACE_SIZING : true;
        boolean normalizedCR = TRACE_SIZING;
        while (true) {
            if (ch == '<') {
                if (gotS && this.tokenize) {
                    this.posEnd = this.pos - 1;
                    this.seenMarkup = true;
                    this.eventType = 7;
                    return 7;
                }
                ch = more();
                if (ch == '?') {
                    if (!parsePI()) {
                        this.posStart = this.pos;
                        gotS = TRACE_SIZING;
                    } else if (this.tokenize) {
                        this.eventType = 8;
                        return 8;
                    }
                } else if (ch == '!') {
                    ch = more();
                    if (ch == 'D') {
                        if (this.seenDocdecl) {
                            throw new XmlPullParserException("only one docdecl allowed in XML document", this, null);
                        }
                        this.seenDocdecl = true;
                        parseDocdecl();
                        if (this.tokenize) {
                            this.eventType = 10;
                            return 10;
                        }
                    } else if (ch == '-') {
                        parseComment();
                        if (this.tokenize) {
                            this.eventType = 9;
                            return 9;
                        }
                    } else {
                        throw new XmlPullParserException(new StringBuffer().append("unexpected markup <!").append(printable(ch)).toString(), this, null);
                    }
                } else if (ch == '/') {
                    throw new XmlPullParserException(new StringBuffer().append("expected start tag name and not ").append(printable(ch)).toString(), this, null);
                } else if (isNameStartChar(ch)) {
                    this.seenRoot = true;
                    return parseStartTag();
                } else {
                    throw new XmlPullParserException(new StringBuffer().append("expected start tag name and not ").append(printable(ch)).toString(), this, null);
                }
            } else if (isS(ch)) {
                gotS = true;
                if (normalizeIgnorableWS) {
                    char[] cArr;
                    int i;
                    if (ch == 13) {
                        normalizedCR = true;
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcEnd = 0;
                                this.pcStart = 0;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        cArr = this.pc;
                        i = this.pcEnd;
                        this.pcEnd = i + 1;
                        cArr[i] = 10;
                    } else if (ch == 10) {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = 10;
                        }
                        normalizedCR = TRACE_SIZING;
                    } else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = ch;
                        }
                        normalizedCR = TRACE_SIZING;
                    }
                }
            } else {
                throw new XmlPullParserException(new StringBuffer().append("only whitespace content allowed before start tag and not ").append(printable(ch)).toString(), this, null);
            }
            ch = more();
        }
    }

    /* access modifiers changed from: protected */
    public int parseEpilog() throws XmlPullParserException, IOException {
        boolean normalizeIgnorableWS = TRACE_SIZING;
        if (this.eventType == 1) {
            throw new XmlPullParserException("already reached end of XML input", this, null);
        } else if (this.reachedEnd) {
            this.eventType = 1;
            return 1;
        } else {
            boolean gotS = TRACE_SIZING;
            if (this.tokenize && !this.roundtripSupported) {
                normalizeIgnorableWS = true;
            }
            boolean normalizedCR = TRACE_SIZING;
            try {
                char ch;
                if (this.seenMarkup) {
                    ch = this.buf[this.pos - 1];
                } else {
                    ch = more();
                }
                this.seenMarkup = TRACE_SIZING;
                this.posStart = this.pos - 1;
                if (!this.reachedEnd) {
                    do {
                        if (ch == '<') {
                            if (!gotS || !this.tokenize) {
                                ch = more();
                                if (this.reachedEnd) {
                                    break;
                                } else if (ch == '?') {
                                    parsePI();
                                    if (this.tokenize) {
                                        this.eventType = 8;
                                        return 8;
                                    }
                                } else if (ch == '!') {
                                    ch = more();
                                    if (this.reachedEnd) {
                                        break;
                                    } else if (ch == 'D') {
                                        parseDocdecl();
                                        if (this.tokenize) {
                                            this.eventType = 10;
                                            return 10;
                                        }
                                    } else if (ch == '-') {
                                        parseComment();
                                        if (this.tokenize) {
                                            this.eventType = 9;
                                            return 9;
                                        }
                                    } else {
                                        throw new XmlPullParserException(new StringBuffer().append("unexpected markup <!").append(printable(ch)).toString(), this, null);
                                    }
                                } else if (ch == '/') {
                                    throw new XmlPullParserException(new StringBuffer().append("end tag not allowed in epilog but got ").append(printable(ch)).toString(), this, null);
                                } else if (isNameStartChar(ch)) {
                                    throw new XmlPullParserException(new StringBuffer().append("start tag not allowed in epilog but got ").append(printable(ch)).toString(), this, null);
                                } else {
                                    throw new XmlPullParserException(new StringBuffer().append("in epilog expected ignorable content and not ").append(printable(ch)).toString(), this, null);
                                }
                            }
                            this.posEnd = this.pos - 1;
                            this.seenMarkup = true;
                            this.eventType = 7;
                            return 7;
                        } else if (isS(ch)) {
                            gotS = true;
                            if (normalizeIgnorableWS) {
                                char[] cArr;
                                int i;
                                if (ch == 13) {
                                    normalizedCR = true;
                                    if (!this.usePC) {
                                        this.posEnd = this.pos - 1;
                                        if (this.posEnd > this.posStart) {
                                            joinPC();
                                        } else {
                                            this.usePC = true;
                                            this.pcEnd = 0;
                                            this.pcStart = 0;
                                        }
                                    }
                                    if (this.pcEnd >= this.pc.length) {
                                        ensurePC(this.pcEnd);
                                    }
                                    cArr = this.pc;
                                    i = this.pcEnd;
                                    this.pcEnd = i + 1;
                                    cArr[i] = 10;
                                } else if (ch == 10) {
                                    if (!normalizedCR && this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            ensurePC(this.pcEnd);
                                        }
                                        cArr = this.pc;
                                        i = this.pcEnd;
                                        this.pcEnd = i + 1;
                                        cArr[i] = 10;
                                    }
                                    normalizedCR = TRACE_SIZING;
                                } else {
                                    if (this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            ensurePC(this.pcEnd);
                                        }
                                        cArr = this.pc;
                                        i = this.pcEnd;
                                        this.pcEnd = i + 1;
                                        cArr[i] = ch;
                                    }
                                    normalizedCR = TRACE_SIZING;
                                }
                            }
                        } else {
                            throw new XmlPullParserException(new StringBuffer().append("in epilog non whitespace content is not allowed but got ").append(printable(ch)).toString(), this, null);
                        }
                        ch = more();
                    } while (!this.reachedEnd);
                }
            } catch (EOFException e) {
                this.reachedEnd = true;
            }
            if (!this.reachedEnd) {
                throw new XmlPullParserException("internal error in parseEpilog");
            } else if (this.tokenize && gotS) {
                this.posEnd = this.pos;
                this.eventType = 7;
                return 7;
            } else {
                this.eventType = 1;
                return 1;
            }
        }
    }

    public int parseEndTag() throws XmlPullParserException, IOException {
        char ch = more();
        if (isNameStartChar(ch)) {
            this.posStart = this.pos - 3;
            int nameStart = (this.pos - 1) + this.bufAbsoluteStart;
            do {
                ch = more();
            } while (isNameChar(ch));
            int off = nameStart - this.bufAbsoluteStart;
            int len = (this.pos - 1) - off;
            char[] cbuf = this.elRawName[this.depth];
            if (this.elRawNameEnd[this.depth] != len) {
                throw new XmlPullParserException(new StringBuffer().append("end tag name </").append(new String(this.buf, off, len)).append("> must match start tag name <").append(new String(cbuf, 0, this.elRawNameEnd[this.depth])).append(Separators.GREATER_THAN).append(" from line ").append(this.elRawNameLine[this.depth]).toString(), this, null);
            }
            int i = 0;
            int off2 = off;
            while (i < len) {
                off = off2 + 1;
                if (this.buf[off2] != cbuf[i]) {
                    throw new XmlPullParserException(new StringBuffer().append("end tag name </").append(new String(this.buf, (off - i) - 1, len)).append("> must be the same as start tag <").append(new String(cbuf, 0, len)).append(Separators.GREATER_THAN).append(" from line ").append(this.elRawNameLine[this.depth]).toString(), this, null);
                }
                i++;
                off2 = off;
            }
            while (isS(ch)) {
                ch = more();
            }
            if (ch != '>') {
                throw new XmlPullParserException(new StringBuffer().append("expected > to finish end tag not ").append(printable(ch)).append(" from line ").append(this.elRawNameLine[this.depth]).toString(), this, null);
            }
            this.posEnd = this.pos;
            this.pastEndTag = true;
            this.eventType = 3;
            return 3;
        }
        throw new XmlPullParserException(new StringBuffer().append("expected name start and not ").append(printable(ch)).toString(), this, null);
    }

    public int parseStartTag() throws XmlPullParserException, IOException {
        this.depth++;
        this.posStart = this.pos - 2;
        this.emptyElementTag = TRACE_SIZING;
        this.attributeCount = 0;
        int nameStart = (this.pos - 1) + this.bufAbsoluteStart;
        int colonPos = -1;
        if (this.buf[this.pos - 1] == ':' && this.processNamespaces) {
            throw new XmlPullParserException("when namespaces processing enabled colon can not be at element name start", this, null);
        }
        while (true) {
            char ch = more();
            if (!isNameChar(ch)) {
                ensureElementsCapacity();
                int elLen = (this.pos - 1) - (nameStart - this.bufAbsoluteStart);
                if (this.elRawName[this.depth] == null || this.elRawName[this.depth].length < elLen) {
                    this.elRawName[this.depth] = new char[(elLen * 2)];
                }
                System.arraycopy(this.buf, nameStart - this.bufAbsoluteStart, this.elRawName[this.depth], 0, elLen);
                this.elRawNameEnd[this.depth] = elLen;
                this.elRawNameLine[this.depth] = this.lineNumber;
                String prefix = null;
                if (!this.processNamespaces) {
                    this.elName[this.depth] = newString(this.buf, nameStart - this.bufAbsoluteStart, elLen);
                } else if (colonPos != -1) {
                    String[] strArr = this.elPrefix;
                    int i = this.depth;
                    prefix = newString(this.buf, nameStart - this.bufAbsoluteStart, colonPos - nameStart);
                    strArr[i] = prefix;
                    this.elName[this.depth] = newString(this.buf, (colonPos + 1) - this.bufAbsoluteStart, (this.pos - 2) - (colonPos - this.bufAbsoluteStart));
                } else {
                    prefix = null;
                    this.elPrefix[this.depth] = null;
                    this.elName[this.depth] = newString(this.buf, nameStart - this.bufAbsoluteStart, elLen);
                }
                while (true) {
                    if (!isS(ch)) {
                        if (ch == '>') {
                            break;
                        } else if (ch == '/') {
                            if (this.emptyElementTag) {
                                throw new XmlPullParserException("repeated / in tag declaration", this, null);
                            }
                            this.emptyElementTag = true;
                            ch = more();
                            if (ch != '>') {
                                throw new XmlPullParserException(new StringBuffer().append("expected > to end empty tag not ").append(printable(ch)).toString(), this, null);
                            }
                        } else if (isNameStartChar(ch)) {
                            ch = parseAttribute();
                            ch = more();
                        } else {
                            throw new XmlPullParserException(new StringBuffer().append("start tag unexpected character ").append(printable(ch)).toString(), this, null);
                        }
                    }
                    ch = more();
                }
                int i2;
                int j;
                String attr1;
                if (this.processNamespaces) {
                    String uri = getNamespace(prefix);
                    if (uri == null) {
                        if (prefix == null) {
                            uri = "";
                        } else {
                            throw new XmlPullParserException(new StringBuffer().append("could not determine namespace bound to element prefix ").append(prefix).toString(), this, null);
                        }
                    }
                    this.elUri[this.depth] = uri;
                    for (i2 = 0; i2 < this.attributeCount; i2++) {
                        String attrPrefix = this.attributePrefix[i2];
                        if (attrPrefix != null) {
                            String attrUri = getNamespace(attrPrefix);
                            if (attrUri == null) {
                                throw new XmlPullParserException(new StringBuffer().append("could not determine namespace bound to attribute prefix ").append(attrPrefix).toString(), this, null);
                            }
                            this.attributeUri[i2] = attrUri;
                        } else {
                            this.attributeUri[i2] = "";
                        }
                    }
                    i2 = 1;
                    while (i2 < this.attributeCount) {
                        j = 0;
                        while (j < i2) {
                            if (this.attributeUri[j] == this.attributeUri[i2] && ((this.allStringsInterned && this.attributeName[j].equals(this.attributeName[i2])) || (!this.allStringsInterned && this.attributeNameHash[j] == this.attributeNameHash[i2] && this.attributeName[j].equals(this.attributeName[i2])))) {
                                attr1 = this.attributeName[j];
                                if (this.attributeUri[j] != null) {
                                    attr1 = new StringBuffer().append(this.attributeUri[j]).append(Separators.COLON).append(attr1).toString();
                                }
                                String attr2 = this.attributeName[i2];
                                if (this.attributeUri[i2] != null) {
                                    attr2 = new StringBuffer().append(this.attributeUri[i2]).append(Separators.COLON).append(attr2).toString();
                                }
                                throw new XmlPullParserException(new StringBuffer().append("duplicated attributes ").append(attr1).append(" and ").append(attr2).toString(), this, null);
                            }
                            j++;
                        }
                        i2++;
                    }
                } else {
                    i2 = 1;
                    while (i2 < this.attributeCount) {
                        j = 0;
                        while (j < i2) {
                            if ((this.allStringsInterned && this.attributeName[j].equals(this.attributeName[i2])) || (!this.allStringsInterned && this.attributeNameHash[j] == this.attributeNameHash[i2] && this.attributeName[j].equals(this.attributeName[i2]))) {
                                attr1 = this.attributeName[j];
                                throw new XmlPullParserException(new StringBuffer().append("duplicated attributes ").append(attr1).append(" and ").append(this.attributeName[i2]).toString(), this, null);
                            }
                            j++;
                        }
                        i2++;
                    }
                }
                this.elNamespaceCount[this.depth] = this.namespaceEnd;
                this.posEnd = this.pos;
                this.eventType = 2;
                return 2;
            } else if (ch == ':' && this.processNamespaces) {
                if (colonPos != -1) {
                    throw new XmlPullParserException("only one colon is allowed in name of element when namespaces are enabled", this, null);
                }
                colonPos = (this.pos - 1) + this.bufAbsoluteStart;
            }
        }
    }

    /* access modifiers changed from: protected */
    public char parseAttribute() throws XmlPullParserException, IOException {
        int prevPosStart = this.posStart + this.bufAbsoluteStart;
        int nameStart = (this.pos - 1) + this.bufAbsoluteStart;
        int colonPos = -1;
        char ch = this.buf[this.pos - 1];
        if (ch == ':' && this.processNamespaces) {
            throw new XmlPullParserException("when namespaces processing enabled colon can not be at attribute name start", this, null);
        }
        int i;
        boolean startsWithXmlns = (this.processNamespaces && ch == 'x') ? true : TRACE_SIZING;
        int xmlnsPos = 0;
        ch = more();
        while (isNameChar(ch)) {
            if (this.processNamespaces) {
                if (startsWithXmlns && xmlnsPos < 5) {
                    xmlnsPos++;
                    if (xmlnsPos == 1) {
                        if (ch != 'm') {
                            startsWithXmlns = TRACE_SIZING;
                        }
                    } else if (xmlnsPos == 2) {
                        if (ch != 'l') {
                            startsWithXmlns = TRACE_SIZING;
                        }
                    } else if (xmlnsPos == 3) {
                        if (ch != 'n') {
                            startsWithXmlns = TRACE_SIZING;
                        }
                    } else if (xmlnsPos == 4) {
                        if (ch != 's') {
                            startsWithXmlns = TRACE_SIZING;
                        }
                    } else if (xmlnsPos == 5 && ch != ':') {
                        throw new XmlPullParserException("after xmlns in attribute name must be colonwhen namespaces are enabled", this, null);
                    }
                }
                if (ch != ':') {
                    continue;
                } else if (colonPos != -1) {
                    throw new XmlPullParserException("only one colon is allowed in attribute name when namespaces are enabled", this, null);
                } else {
                    colonPos = (this.pos - 1) + this.bufAbsoluteStart;
                }
            }
            ch = more();
        }
        ensureAttributesCapacity(this.attributeCount);
        String name = null;
        String[] strArr;
        if (this.processNamespaces) {
            if (xmlnsPos < 4) {
                startsWithXmlns = TRACE_SIZING;
            }
            int nameLen;
            if (!startsWithXmlns) {
                if (colonPos != -1) {
                    this.attributePrefix[this.attributeCount] = newString(this.buf, nameStart - this.bufAbsoluteStart, colonPos - nameStart);
                    nameLen = (this.pos - 2) - (colonPos - this.bufAbsoluteStart);
                    strArr = this.attributeName;
                    i = this.attributeCount;
                    name = newString(this.buf, (colonPos - this.bufAbsoluteStart) + 1, nameLen);
                    strArr[i] = name;
                } else {
                    this.attributePrefix[this.attributeCount] = null;
                    strArr = this.attributeName;
                    i = this.attributeCount;
                    name = newString(this.buf, nameStart - this.bufAbsoluteStart, (this.pos - 1) - (nameStart - this.bufAbsoluteStart));
                    strArr[i] = name;
                }
                if (!this.allStringsInterned) {
                    this.attributeNameHash[this.attributeCount] = name.hashCode();
                }
            } else if (colonPos != -1) {
                nameLen = (this.pos - 2) - (colonPos - this.bufAbsoluteStart);
                if (nameLen == 0) {
                    throw new XmlPullParserException("namespace prefix is required after xmlns:  when namespaces are enabled", this, null);
                }
                name = newString(this.buf, (colonPos - this.bufAbsoluteStart) + 1, nameLen);
            }
        } else {
            strArr = this.attributeName;
            i = this.attributeCount;
            name = newString(this.buf, nameStart - this.bufAbsoluteStart, (this.pos - 1) - (nameStart - this.bufAbsoluteStart));
            strArr[i] = name;
            if (!this.allStringsInterned) {
                this.attributeNameHash[this.attributeCount] = name.hashCode();
            }
        }
        while (isS(ch)) {
            ch = more();
        }
        if (ch != '=') {
            throw new XmlPullParserException("expected = after attribute name", this, null);
        }
        ch = more();
        while (isS(ch)) {
            ch = more();
        }
        char delimit = ch;
        if (delimit == '\"' || delimit == '\'') {
            boolean normalizedCR = TRACE_SIZING;
            this.usePC = TRACE_SIZING;
            this.pcStart = this.pcEnd;
            this.posStart = this.pos;
            while (true) {
                ch = more();
                int i2;
                if (ch == delimit) {
                    if (this.processNamespaces && startsWithXmlns) {
                        String ns;
                        if (this.usePC) {
                            ns = newStringIntern(this.pc, this.pcStart, this.pcEnd - this.pcStart);
                        } else {
                            ns = newStringIntern(this.buf, this.posStart, (this.pos - 1) - this.posStart);
                        }
                        ensureNamespacesCapacity(this.namespaceEnd);
                        int prefixHash = -1;
                        if (colonPos == -1) {
                            this.namespacePrefix[this.namespaceEnd] = null;
                            if (!this.allStringsInterned) {
                                prefixHash = -1;
                                this.namespacePrefixHash[this.namespaceEnd] = -1;
                            }
                        } else if (ns.length() == 0) {
                            throw new XmlPullParserException("non-default namespace can not be declared to be empty string", this, null);
                        } else {
                            this.namespacePrefix[this.namespaceEnd] = name;
                            if (!this.allStringsInterned) {
                                int[] iArr = this.namespacePrefixHash;
                                i = this.namespaceEnd;
                                prefixHash = name.hashCode();
                                iArr[i] = prefixHash;
                            }
                        }
                        this.namespaceUri[this.namespaceEnd] = ns;
                        int startNs = this.elNamespaceCount[this.depth - 1];
                        i2 = this.namespaceEnd - 1;
                        while (i2 >= startNs) {
                            if (((this.allStringsInterned || name == null) && this.namespacePrefix[i2] == name) || (!this.allStringsInterned && name != null && this.namespacePrefixHash[i2] == prefixHash && name.equals(this.namespacePrefix[i2]))) {
                                String s;
                                if (name == null) {
                                    s = "default";
                                } else {
                                    s = new StringBuffer().append(Separators.QUOTE).append(name).append(Separators.QUOTE).toString();
                                }
                                throw new XmlPullParserException(new StringBuffer().append("duplicated namespace declaration for ").append(s).append(" prefix").toString(), this, null);
                            }
                            i2--;
                        }
                        this.namespaceEnd++;
                    } else {
                        if (this.usePC) {
                            this.attributeValue[this.attributeCount] = new String(this.pc, this.pcStart, this.pcEnd - this.pcStart);
                        } else {
                            this.attributeValue[this.attributeCount] = new String(this.buf, this.posStart, (this.pos - 1) - this.posStart);
                        }
                        this.attributeCount++;
                    }
                    this.posStart = prevPosStart - this.bufAbsoluteStart;
                    return ch;
                } else if (ch == '<') {
                    throw new XmlPullParserException("markup not allowed inside attribute value - illegal < ", this, null);
                } else {
                    char[] cArr;
                    if (ch == '&') {
                        this.posEnd = this.pos - 1;
                        if (!this.usePC) {
                            if (this.posEnd > this.posStart ? true : TRACE_SIZING) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcEnd = 0;
                                this.pcStart = 0;
                            }
                        }
                        char[] resolvedEntity = parseEntityRef();
                        if (resolvedEntity == null) {
                            if (this.entityRefName == null) {
                                this.entityRefName = newString(this.buf, this.posStart, this.posEnd - this.posStart);
                            }
                            throw new XmlPullParserException(new StringBuffer().append("could not resolve entity named '").append(printable(this.entityRefName)).append(Separators.QUOTE).toString(), this, null);
                        }
                        for (char c : resolvedEntity) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = c;
                        }
                    } else if (ch == 9 || ch == 10 || ch == 13) {
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcStart = 0;
                                this.pcEnd = 0;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        if (!(ch == 10 && normalizedCR)) {
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = ' ';
                        }
                    } else if (this.usePC) {
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        cArr = this.pc;
                        i = this.pcEnd;
                        this.pcEnd = i + 1;
                        cArr[i] = ch;
                    }
                    normalizedCR = ch == 13 ? true : TRACE_SIZING;
                }
            }
        } else {
            throw new XmlPullParserException(new StringBuffer().append("attribute value must start with quotation or apostrophe not ").append(printable(delimit)).toString(), this, null);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006a  */
    public char[] parseEntityRef() throws org.jitsi.org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        r11 = this;
        r10 = 116; // 0x74 float:1.63E-43 double:5.73E-322;
        r9 = 97;
        r8 = 59;
        r4 = 0;
        r7 = 0;
        r11.entityRefName = r4;
        r5 = r11.pos;
        r11.posStart = r5;
        r0 = r11.more();
        r5 = 35;
        if (r0 != r5) goto L_0x00b9;
    L_0x0016:
        r1 = 0;
        r0 = r11.more();
        r5 = 120; // 0x78 float:1.68E-43 double:5.93E-322;
        if (r0 != r5) goto L_0x0087;
    L_0x001f:
        r0 = r11.more();
        r5 = 48;
        if (r0 < r5) goto L_0x0032;
    L_0x0027:
        r5 = 57;
        if (r0 > r5) goto L_0x0032;
    L_0x002b:
        r5 = r1 * 16;
        r6 = r0 + -48;
        r5 = r5 + r6;
        r1 = (char) r5;
        goto L_0x001f;
    L_0x0032:
        if (r0 < r9) goto L_0x003f;
    L_0x0034:
        r5 = 102; // 0x66 float:1.43E-43 double:5.04E-322;
        if (r0 > r5) goto L_0x003f;
    L_0x0038:
        r5 = r1 * 16;
        r6 = r0 + -87;
        r5 = r5 + r6;
        r1 = (char) r5;
        goto L_0x001f;
    L_0x003f:
        r5 = 65;
        if (r0 < r5) goto L_0x004e;
    L_0x0043:
        r5 = 70;
        if (r0 > r5) goto L_0x004e;
    L_0x0047:
        r5 = r1 * 16;
        r6 = r0 + -55;
        r5 = r5 + r6;
        r1 = (char) r5;
        goto L_0x001f;
    L_0x004e:
        if (r0 != r8) goto L_0x006a;
    L_0x0050:
        r4 = r11.pos;
        r4 = r4 + -1;
        r11.posEnd = r4;
        r4 = r11.charRefOneCharBuf;
        r4[r7] = r1;
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x0067;
    L_0x005e:
        r4 = r11.charRefOneCharBuf;
        r5 = 1;
        r4 = r11.newString(r4, r7, r5);
        r11.text = r4;
    L_0x0067:
        r3 = r11.charRefOneCharBuf;
    L_0x0069:
        return r3;
    L_0x006a:
        r5 = new org.jitsi.org.xmlpull.v1.XmlPullParserException;
        r6 = new java.lang.StringBuffer;
        r6.<init>();
        r7 = "character reference (with hex value) may not contain ";
        r6 = r6.append(r7);
        r7 = r11.printable(r0);
        r6 = r6.append(r7);
        r6 = r6.toString();
        r5.m1677init(r6, r11, r4);
        throw r5;
    L_0x0087:
        r5 = 48;
        if (r0 < r5) goto L_0x009a;
    L_0x008b:
        r5 = 57;
        if (r0 > r5) goto L_0x009a;
    L_0x008f:
        r5 = r1 * 10;
        r6 = r0 + -48;
        r5 = r5 + r6;
        r1 = (char) r5;
        r0 = r11.more();
        goto L_0x0087;
    L_0x009a:
        if (r0 == r8) goto L_0x0050;
    L_0x009c:
        r5 = new org.jitsi.org.xmlpull.v1.XmlPullParserException;
        r6 = new java.lang.StringBuffer;
        r6.<init>();
        r7 = "character reference (with decimal value) may not contain ";
        r6 = r6.append(r7);
        r7 = r11.printable(r0);
        r6 = r6.append(r7);
        r6 = r6.toString();
        r5.m1677init(r6, r11, r4);
        throw r5;
    L_0x00b9:
        r5 = r11.isNameStartChar(r0);
        if (r5 != 0) goto L_0x00e2;
    L_0x00bf:
        r5 = new org.jitsi.org.xmlpull.v1.XmlPullParserException;
        r6 = new java.lang.StringBuffer;
        r6.<init>();
        r7 = "entity reference names can not start with character '";
        r6 = r6.append(r7);
        r7 = r11.printable(r0);
        r6 = r6.append(r7);
        r7 = "'";
        r6 = r6.append(r7);
        r6 = r6.toString();
        r5.m1677init(r6, r11, r4);
        throw r5;
    L_0x00e2:
        r0 = r11.more();
        if (r0 != r8) goto L_0x011d;
    L_0x00e8:
        r5 = r11.pos;
        r5 = r5 + -1;
        r11.posEnd = r5;
        r5 = r11.posEnd;
        r6 = r11.posStart;
        r2 = r5 - r6;
        r5 = 2;
        if (r2 != r5) goto L_0x0146;
    L_0x00f7:
        r5 = r11.buf;
        r6 = r11.posStart;
        r5 = r5[r6];
        r6 = 108; // 0x6c float:1.51E-43 double:5.34E-322;
        if (r5 != r6) goto L_0x0146;
    L_0x0101:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 1;
        r5 = r5[r6];
        if (r5 != r10) goto L_0x0146;
    L_0x010b:
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x0113;
    L_0x010f:
        r4 = "<";
        r11.text = r4;
    L_0x0113:
        r4 = r11.charRefOneCharBuf;
        r5 = 60;
        r4[r7] = r5;
        r3 = r11.charRefOneCharBuf;
        goto L_0x0069;
    L_0x011d:
        r5 = r11.isNameChar(r0);
        if (r5 != 0) goto L_0x00e2;
    L_0x0123:
        r5 = new org.jitsi.org.xmlpull.v1.XmlPullParserException;
        r6 = new java.lang.StringBuffer;
        r6.<init>();
        r7 = "entity reference name can not contain character ";
        r6 = r6.append(r7);
        r7 = r11.printable(r0);
        r6 = r6.append(r7);
        r7 = "'";
        r6 = r6.append(r7);
        r6 = r6.toString();
        r5.m1677init(r6, r11, r4);
        throw r5;
    L_0x0146:
        r5 = 3;
        if (r2 != r5) goto L_0x017b;
    L_0x0149:
        r5 = r11.buf;
        r6 = r11.posStart;
        r5 = r5[r6];
        if (r5 != r9) goto L_0x017b;
    L_0x0151:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 1;
        r5 = r5[r6];
        r6 = 109; // 0x6d float:1.53E-43 double:5.4E-322;
        if (r5 != r6) goto L_0x017b;
    L_0x015d:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 2;
        r5 = r5[r6];
        r6 = 112; // 0x70 float:1.57E-43 double:5.53E-322;
        if (r5 != r6) goto L_0x017b;
    L_0x0169:
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x0171;
    L_0x016d:
        r4 = "&";
        r11.text = r4;
    L_0x0171:
        r4 = r11.charRefOneCharBuf;
        r5 = 38;
        r4[r7] = r5;
        r3 = r11.charRefOneCharBuf;
        goto L_0x0069;
    L_0x017b:
        r5 = 2;
        if (r2 != r5) goto L_0x01a4;
    L_0x017e:
        r5 = r11.buf;
        r6 = r11.posStart;
        r5 = r5[r6];
        r6 = 103; // 0x67 float:1.44E-43 double:5.1E-322;
        if (r5 != r6) goto L_0x01a4;
    L_0x0188:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 1;
        r5 = r5[r6];
        if (r5 != r10) goto L_0x01a4;
    L_0x0192:
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x019a;
    L_0x0196:
        r4 = ">";
        r11.text = r4;
    L_0x019a:
        r4 = r11.charRefOneCharBuf;
        r5 = 62;
        r4[r7] = r5;
        r3 = r11.charRefOneCharBuf;
        goto L_0x0069;
    L_0x01a4:
        r5 = 4;
        if (r2 != r5) goto L_0x01e5;
    L_0x01a7:
        r5 = r11.buf;
        r6 = r11.posStart;
        r5 = r5[r6];
        if (r5 != r9) goto L_0x01e5;
    L_0x01af:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 1;
        r5 = r5[r6];
        r6 = 112; // 0x70 float:1.57E-43 double:5.53E-322;
        if (r5 != r6) goto L_0x01e5;
    L_0x01bb:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 2;
        r5 = r5[r6];
        r6 = 111; // 0x6f float:1.56E-43 double:5.5E-322;
        if (r5 != r6) goto L_0x01e5;
    L_0x01c7:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 3;
        r5 = r5[r6];
        r6 = 115; // 0x73 float:1.61E-43 double:5.7E-322;
        if (r5 != r6) goto L_0x01e5;
    L_0x01d3:
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x01db;
    L_0x01d7:
        r4 = "'";
        r11.text = r4;
    L_0x01db:
        r4 = r11.charRefOneCharBuf;
        r5 = 39;
        r4[r7] = r5;
        r3 = r11.charRefOneCharBuf;
        goto L_0x0069;
    L_0x01e5:
        r5 = 4;
        if (r2 != r5) goto L_0x0226;
    L_0x01e8:
        r5 = r11.buf;
        r6 = r11.posStart;
        r5 = r5[r6];
        r6 = 113; // 0x71 float:1.58E-43 double:5.6E-322;
        if (r5 != r6) goto L_0x0226;
    L_0x01f2:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 1;
        r5 = r5[r6];
        r6 = 117; // 0x75 float:1.64E-43 double:5.8E-322;
        if (r5 != r6) goto L_0x0226;
    L_0x01fe:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 2;
        r5 = r5[r6];
        r6 = 111; // 0x6f float:1.56E-43 double:5.5E-322;
        if (r5 != r6) goto L_0x0226;
    L_0x020a:
        r5 = r11.buf;
        r6 = r11.posStart;
        r6 = r6 + 3;
        r5 = r5[r6];
        if (r5 != r10) goto L_0x0226;
    L_0x0214:
        r4 = r11.tokenize;
        if (r4 == 0) goto L_0x021c;
    L_0x0218:
        r4 = "\"";
        r11.text = r4;
    L_0x021c:
        r4 = r11.charRefOneCharBuf;
        r5 = 34;
        r4[r7] = r5;
        r3 = r11.charRefOneCharBuf;
        goto L_0x0069;
    L_0x0226:
        r3 = r11.lookuEntityReplacement(r2);
        if (r3 != 0) goto L_0x0069;
    L_0x022c:
        r5 = r11.tokenize;
        if (r5 == 0) goto L_0x0232;
    L_0x0230:
        r11.text = r4;
    L_0x0232:
        r3 = r4;
        goto L_0x0069;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xmlpull.mxp1.MXParser.parseEntityRef():char[]");
    }

    /* access modifiers changed from: protected */
    public char[] lookuEntityReplacement(int entitNameLen) throws XmlPullParserException, IOException {
        int i;
        if (this.allStringsInterned) {
            this.entityRefName = newString(this.buf, this.posStart, this.posEnd - this.posStart);
            for (i = this.entityEnd - 1; i >= 0; i--) {
                if (this.entityRefName == this.entityName[i]) {
                    if (this.tokenize) {
                        this.text = this.entityReplacement[i];
                    }
                    return this.entityReplacementBuf[i];
                }
            }
        } else {
            int hash = fastHash(this.buf, this.posStart, this.posEnd - this.posStart);
            i = this.entityEnd - 1;
            while (i >= 0) {
                if (hash == this.entityNameHash[i] && entitNameLen == this.entityNameBuf[i].length) {
                    char[] entityBuf = this.entityNameBuf[i];
                    int j = 0;
                    while (j < entitNameLen) {
                        if (this.buf[this.posStart + j] == entityBuf[j]) {
                            j++;
                        }
                    }
                    if (this.tokenize) {
                        this.text = this.entityReplacement[i];
                    }
                    return this.entityReplacementBuf[i];
                }
                i--;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void parseComment() throws XmlPullParserException, IOException {
        boolean normalizeIgnorableWS = true;
        if (more() != '-') {
            throw new XmlPullParserException("expected <!-- for comment start", this, null);
        }
        if (this.tokenize) {
            this.posStart = this.pos;
        }
        int curLine = this.lineNumber;
        int curColumn = this.columnNumber;
        try {
            if (!this.tokenize || this.roundtripSupported) {
                normalizeIgnorableWS = TRACE_SIZING;
            }
            boolean normalizedCR = TRACE_SIZING;
            boolean seenDash = TRACE_SIZING;
            boolean seenDashDash = TRACE_SIZING;
            while (true) {
                char ch = more();
                if (!seenDashDash || ch == '>') {
                    if (ch == '-') {
                        if (seenDash) {
                            seenDashDash = true;
                            seenDash = TRACE_SIZING;
                        } else {
                            seenDash = true;
                        }
                    } else if (ch != '>') {
                        seenDash = TRACE_SIZING;
                    } else if (!seenDashDash) {
                        seenDashDash = TRACE_SIZING;
                        seenDash = TRACE_SIZING;
                    } else if (this.tokenize) {
                        this.posEnd = this.pos - 3;
                        if (this.usePC) {
                            this.pcEnd -= 2;
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                    if (normalizeIgnorableWS) {
                        char[] cArr;
                        int i;
                        if (ch == 13) {
                            normalizedCR = true;
                            if (!this.usePC) {
                                this.posEnd = this.pos - 1;
                                if (this.posEnd > this.posStart) {
                                    joinPC();
                                } else {
                                    this.usePC = true;
                                    this.pcEnd = 0;
                                    this.pcStart = 0;
                                }
                            }
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = 10;
                        } else if (ch == 10) {
                            if (!normalizedCR && this.usePC) {
                                if (this.pcEnd >= this.pc.length) {
                                    ensurePC(this.pcEnd);
                                }
                                cArr = this.pc;
                                i = this.pcEnd;
                                this.pcEnd = i + 1;
                                cArr[i] = 10;
                            }
                            normalizedCR = TRACE_SIZING;
                        } else {
                            if (this.usePC) {
                                if (this.pcEnd >= this.pc.length) {
                                    ensurePC(this.pcEnd);
                                }
                                cArr = this.pc;
                                i = this.pcEnd;
                                this.pcEnd = i + 1;
                                cArr[i] = ch;
                            }
                            normalizedCR = TRACE_SIZING;
                        }
                    }
                } else {
                    throw new XmlPullParserException(new StringBuffer().append("in comment after two dashes (--) next character must be > not ").append(printable(ch)).toString(), this, null);
                }
            }
        } catch (EOFException ex) {
            throw new XmlPullParserException(new StringBuffer().append("comment started on line ").append(curLine).append(" and column ").append(curColumn).append(" was not closed").toString(), this, ex);
        }
    }

    /* access modifiers changed from: protected */
    public boolean parsePI() throws XmlPullParserException, IOException {
        if (this.tokenize) {
            this.posStart = this.pos;
        }
        int curLine = this.lineNumber;
        int curColumn = this.columnNumber;
        int piTargetStart = this.pos + this.bufAbsoluteStart;
        int piTargetEnd = -1;
        boolean normalizeIgnorableWS = (!this.tokenize || this.roundtripSupported) ? TRACE_SIZING : true;
        boolean normalizedCR = TRACE_SIZING;
        boolean seenQ = TRACE_SIZING;
        try {
            char ch = more();
            if (isS(ch)) {
                throw new XmlPullParserException("processing instruction PITarget must be exactly after <? and not white space character", this, null);
            }
            while (true) {
                if (ch == '?') {
                    seenQ = true;
                } else if (ch != '>') {
                    if (piTargetEnd == -1) {
                        if (isS(ch)) {
                            piTargetEnd = (this.pos - 1) + this.bufAbsoluteStart;
                            if (piTargetEnd - piTargetStart == 3 && ((this.buf[piTargetStart] == 'x' || this.buf[piTargetStart] == 'X') && ((this.buf[piTargetStart + 1] == 'm' || this.buf[piTargetStart + 1] == 'M') && (this.buf[piTargetStart + 2] == 'l' || this.buf[piTargetStart + 2] == 'L')))) {
                            }
                        }
                    }
                    seenQ = TRACE_SIZING;
                } else if (seenQ) {
                    if (piTargetEnd == -1) {
                        piTargetEnd = (this.pos - 2) + this.bufAbsoluteStart;
                    }
                    piTargetStart -= this.bufAbsoluteStart;
                    piTargetEnd -= this.bufAbsoluteStart;
                    if (this.tokenize) {
                        this.posEnd = this.pos - 2;
                        if (normalizeIgnorableWS) {
                            this.pcEnd--;
                        }
                    }
                    return true;
                } else {
                    seenQ = TRACE_SIZING;
                }
                if (normalizeIgnorableWS) {
                    char[] cArr;
                    int i;
                    if (ch == 13) {
                        normalizedCR = true;
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcEnd = 0;
                                this.pcStart = 0;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        cArr = this.pc;
                        i = this.pcEnd;
                        this.pcEnd = i + 1;
                        cArr[i] = 10;
                    } else if (ch == 10) {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = 10;
                        }
                        normalizedCR = TRACE_SIZING;
                    } else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = ch;
                        }
                        normalizedCR = TRACE_SIZING;
                    }
                }
                ch = more();
            }
            if (piTargetStart > 3) {
                throw new XmlPullParserException("processing instruction can not have PITarget with reserveld xml name", this, null);
            } else if (this.buf[piTargetStart] == 'x' || this.buf[piTargetStart + 1] == 'm' || this.buf[piTargetStart + 2] == 'l') {
                parseXmlDecl(ch);
                if (this.tokenize) {
                    this.posEnd = this.pos - 2;
                }
                int off = (piTargetStart - this.bufAbsoluteStart) + 3;
                this.xmlDeclContent = newString(this.buf, off, (this.pos - 2) - off);
                return TRACE_SIZING;
            } else {
                throw new XmlPullParserException("XMLDecl must have xml name in lowercase", this, null);
            }
        } catch (EOFException ex) {
            throw new XmlPullParserException(new StringBuffer().append("processing instruction started on line ").append(curLine).append(" and column ").append(curColumn).append(" was not closed").toString(), this, ex);
        }
    }

    static {
        char ch;
        setNameStart(':');
        for (ch = 'A'; ch <= 'Z'; ch = (char) (ch + 1)) {
            setNameStart(ch);
        }
        setNameStart('_');
        for (ch = 'a'; ch <= 'z'; ch = (char) (ch + 1)) {
            setNameStart(ch);
        }
        for (ch = 192; ch <= 767; ch = (char) (ch + 1)) {
            setNameStart(ch);
        }
        for (ch = 880; ch <= 893; ch = (char) (ch + 1)) {
            setNameStart(ch);
        }
        for (ch = 895; ch < LOOKUP_MAX_CHAR; ch = (char) (ch + 1)) {
            setNameStart(ch);
        }
        setName('-');
        setName('.');
        for (ch = '0'; ch <= '9'; ch = (char) (ch + 1)) {
            setName(ch);
        }
        setName(183);
        for (ch = 768; ch <= 879; ch = (char) (ch + 1)) {
            setName(ch);
        }
    }

    /* access modifiers changed from: protected */
    public void parseXmlDecl(char ch) throws XmlPullParserException, IOException {
        this.preventBufferCompaction = true;
        this.bufStart = 0;
        ch = skipS(requireInput(skipS(ch), VERSION));
        if (ch != '=') {
            throw new XmlPullParserException(new StringBuffer().append("expected equals sign (=) after version and not ").append(printable(ch)).toString(), this, null);
        }
        ch = skipS(more());
        if (ch == '\'' || ch == '\"') {
            char quotChar = ch;
            int versionStart = this.pos;
            ch = more();
            while (ch != quotChar) {
                if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && !((ch >= '0' && ch <= '9') || ch == '_' || ch == '.' || ch == ':' || ch == '-'))) {
                    throw new XmlPullParserException(new StringBuffer().append("<?xml version value expected to be in ([a-zA-Z0-9_.:] | '-') not ").append(printable(ch)).toString(), this, null);
                }
                ch = more();
            }
            parseXmlDeclWithVersion(versionStart, this.pos - 1);
            this.preventBufferCompaction = TRACE_SIZING;
            return;
        }
        throw new XmlPullParserException(new StringBuffer().append("expected apostrophe (') or quotation mark (\") after version and not ").append(printable(ch)).toString(), this, null);
    }

    /* access modifiers changed from: protected */
    public void parseXmlDeclWithVersion(int versionStart, int versionEnd) throws XmlPullParserException, IOException {
        String oldEncoding = this.inputEncoding;
        if (versionEnd - versionStart == 3 && this.buf[versionStart] == '1' && this.buf[versionStart + 1] == '.' && this.buf[versionStart + 2] == '0') {
            char quotChar;
            this.xmlDeclVersion = newString(this.buf, versionStart, versionEnd - versionStart);
            char ch = skipS(more());
            if (ch == 'e') {
                ch = skipS(requireInput(more(), NCODING));
                if (ch != '=') {
                    throw new XmlPullParserException(new StringBuffer().append("expected equals sign (=) after encoding and not ").append(printable(ch)).toString(), this, null);
                }
                ch = skipS(more());
                if (ch == '\'' || ch == '\"') {
                    quotChar = ch;
                    int encodingStart = this.pos;
                    ch = more();
                    if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                        throw new XmlPullParserException(new StringBuffer().append("<?xml encoding name expected to start with [A-Za-z] not ").append(printable(ch)).toString(), this, null);
                    }
                    ch = more();
                    while (ch != quotChar) {
                        if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && !((ch >= '0' && ch <= '9') || ch == '.' || ch == '_' || ch == '-'))) {
                            throw new XmlPullParserException(new StringBuffer().append("<?xml encoding value expected to be in ([A-Za-z0-9._] | '-') not ").append(printable(ch)).toString(), this, null);
                        }
                        ch = more();
                    }
                    this.inputEncoding = newString(this.buf, encodingStart, (this.pos - 1) - encodingStart);
                    ch = more();
                } else {
                    throw new XmlPullParserException(new StringBuffer().append("expected apostrophe (') or quotation mark (\") after encoding and not ").append(printable(ch)).toString(), this, null);
                }
            }
            ch = skipS(ch);
            if (ch == 's') {
                ch = skipS(requireInput(more(), TANDALONE));
                if (ch != '=') {
                    throw new XmlPullParserException(new StringBuffer().append("expected equals sign (=) after standalone and not ").append(printable(ch)).toString(), this, null);
                }
                ch = skipS(more());
                if (ch == '\'' || ch == '\"') {
                    quotChar = ch;
                    int standaloneStart = this.pos;
                    ch = more();
                    if (ch == 'y') {
                        ch = requireInput(ch, YES);
                        this.xmlDeclStandalone = new Boolean(true);
                    } else if (ch == 'n') {
                        ch = requireInput(ch, NO);
                        this.xmlDeclStandalone = new Boolean(TRACE_SIZING);
                    } else {
                        throw new XmlPullParserException(new StringBuffer().append("expected 'yes' or 'no' after standalone and not ").append(printable(ch)).toString(), this, null);
                    }
                    if (ch != quotChar) {
                        throw new XmlPullParserException(new StringBuffer().append("expected ").append(quotChar).append(" after standalone value not ").append(printable(ch)).toString(), this, null);
                    }
                    ch = more();
                } else {
                    throw new XmlPullParserException(new StringBuffer().append("expected apostrophe (') or quotation mark (\") after encoding and not ").append(printable(ch)).toString(), this, null);
                }
            }
            ch = skipS(ch);
            if (ch != '?') {
                throw new XmlPullParserException(new StringBuffer().append("expected ?> as last part of <?xml not ").append(printable(ch)).toString(), this, null);
            }
            ch = more();
            if (ch != '>') {
                throw new XmlPullParserException(new StringBuffer().append("expected ?> as last part of <?xml not ").append(printable(ch)).toString(), this, null);
            }
            return;
        }
        throw new XmlPullParserException(new StringBuffer().append("only 1.0 is supported as <?xml version not '").append(printable(new String(this.buf, versionStart, versionEnd - versionStart))).append(Separators.QUOTE).toString(), this, null);
    }

    /* access modifiers changed from: protected */
    public void parseDocdecl() throws XmlPullParserException, IOException {
        if (more() != 'O') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else if (more() != 'C') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else if (more() != 'T') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else if (more() != 'Y') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else if (more() != 'P') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else if (more() != 'E') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        } else {
            this.posStart = this.pos;
            int bracketLevel = 0;
            boolean normalizeIgnorableWS = (!this.tokenize || this.roundtripSupported) ? TRACE_SIZING : true;
            boolean normalizedCR = TRACE_SIZING;
            while (true) {
                char ch = more();
                if (ch == '[') {
                    bracketLevel++;
                }
                if (ch == ']') {
                    bracketLevel--;
                }
                if (ch == '>' && bracketLevel == 0) {
                    this.posEnd = this.pos - 1;
                    return;
                } else if (normalizeIgnorableWS) {
                    char[] cArr;
                    int i;
                    if (ch == 13) {
                        normalizedCR = true;
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcEnd = 0;
                                this.pcStart = 0;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        cArr = this.pc;
                        i = this.pcEnd;
                        this.pcEnd = i + 1;
                        cArr[i] = 10;
                    } else if (ch == 10) {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = 10;
                        }
                        normalizedCR = TRACE_SIZING;
                    } else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = ch;
                        }
                        normalizedCR = TRACE_SIZING;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseCDSect(boolean hadCharData) throws XmlPullParserException, IOException {
        boolean normalizeInput = TRACE_SIZING;
        if (more() != 'C') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        } else if (more() != 'D') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        } else if (more() != 'A') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        } else if (more() != 'T') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        } else if (more() != 'A') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        } else if (more() != '[') {
            throw new XmlPullParserException("expected <![CDATA[ for comment start", this, null);
        } else {
            int cdStart = this.pos + this.bufAbsoluteStart;
            int curLine = this.lineNumber;
            int curColumn = this.columnNumber;
            if (!(this.tokenize && this.roundtripSupported)) {
                normalizeInput = true;
            }
            if (normalizeInput && hadCharData) {
                try {
                    if (!this.usePC) {
                        if (this.posEnd > this.posStart) {
                            joinPC();
                        } else {
                            this.usePC = true;
                            this.pcEnd = 0;
                            this.pcStart = 0;
                        }
                    }
                } catch (EOFException ex) {
                    throw new XmlPullParserException(new StringBuffer().append("CDATA section started on line ").append(curLine).append(" and column ").append(curColumn).append(" was not closed").toString(), this, ex);
                }
            }
            boolean seenBracket = TRACE_SIZING;
            boolean seenBracketBracket = TRACE_SIZING;
            boolean normalizedCR = TRACE_SIZING;
            while (true) {
                char ch = more();
                if (ch == ']') {
                    if (seenBracket) {
                        seenBracketBracket = true;
                    } else {
                        seenBracket = true;
                    }
                } else if (ch == '>') {
                    if (seenBracket && seenBracketBracket) {
                        break;
                    }
                    seenBracketBracket = TRACE_SIZING;
                    seenBracket = TRACE_SIZING;
                } else if (seenBracket) {
                    seenBracket = TRACE_SIZING;
                }
                if (normalizeInput) {
                    char[] cArr;
                    int i;
                    if (ch == 13) {
                        normalizedCR = true;
                        this.posStart = cdStart - this.bufAbsoluteStart;
                        this.posEnd = this.pos - 1;
                        if (!this.usePC) {
                            if (this.posEnd > this.posStart) {
                                joinPC();
                            } else {
                                this.usePC = true;
                                this.pcEnd = 0;
                                this.pcStart = 0;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            ensurePC(this.pcEnd);
                        }
                        cArr = this.pc;
                        i = this.pcEnd;
                        this.pcEnd = i + 1;
                        cArr[i] = 10;
                    } else if (ch == 10) {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = 10;
                        }
                        normalizedCR = TRACE_SIZING;
                    } else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                ensurePC(this.pcEnd);
                            }
                            cArr = this.pc;
                            i = this.pcEnd;
                            this.pcEnd = i + 1;
                            cArr[i] = ch;
                        }
                        normalizedCR = TRACE_SIZING;
                    }
                }
            }
            if (normalizeInput && this.usePC) {
                this.pcEnd -= 2;
            }
            this.posStart = cdStart - this.bufAbsoluteStart;
            this.posEnd = this.pos - 3;
        }
    }

    /* access modifiers changed from: protected */
    public void fillBuf() throws IOException, XmlPullParserException {
        if (this.reader == null) {
            throw new XmlPullParserException("reader must be set before parsing is started");
        }
        if (this.bufEnd > this.bufSoftLimit) {
            boolean compact = this.bufStart > this.bufSoftLimit ? true : TRACE_SIZING;
            boolean expand = TRACE_SIZING;
            if (this.preventBufferCompaction) {
                compact = TRACE_SIZING;
                expand = true;
            } else if (!compact) {
                if (this.bufStart < this.buf.length / 2) {
                    expand = true;
                } else {
                    compact = true;
                }
            }
            if (compact) {
                System.arraycopy(this.buf, this.bufStart, this.buf, 0, this.bufEnd - this.bufStart);
            } else if (expand) {
                char[] newBuf = new char[(this.buf.length * 2)];
                System.arraycopy(this.buf, this.bufStart, newBuf, 0, this.bufEnd - this.bufStart);
                this.buf = newBuf;
                if (this.bufLoadFactor > 0) {
                    this.bufSoftLimit = (int) ((((long) this.bufLoadFactor) * ((long) this.buf.length)) / 100);
                }
            } else {
                throw new XmlPullParserException("internal error in fillBuffer()");
            }
            this.bufEnd -= this.bufStart;
            this.pos -= this.bufStart;
            this.posStart -= this.bufStart;
            this.posEnd -= this.bufStart;
            this.bufAbsoluteStart += this.bufStart;
            this.bufStart = 0;
        }
        int ret = this.reader.read(this.buf, this.bufEnd, this.buf.length - this.bufEnd > READ_CHUNK_SIZE ? READ_CHUNK_SIZE : this.buf.length - this.bufEnd);
        if (ret > 0) {
            this.bufEnd += ret;
        } else if (ret != -1) {
            throw new IOException(new StringBuffer().append("error reading input, returned ").append(ret).toString());
        } else if (this.bufAbsoluteStart == 0 && this.pos == 0) {
            throw new EOFException("input contained no data");
        } else if (this.seenRoot && this.depth == 0) {
            this.reachedEnd = true;
        } else {
            StringBuffer expectedTagStack = new StringBuffer();
            if (this.depth > 0) {
                int i;
                expectedTagStack.append(" - expected end tag");
                if (this.depth > 1) {
                    expectedTagStack.append("s");
                }
                expectedTagStack.append(Separators.SP);
                for (i = this.depth; i > 0; i--) {
                    expectedTagStack.append("</").append(new String(this.elRawName[i], 0, this.elRawNameEnd[i])).append('>');
                }
                expectedTagStack.append(" to close");
                for (i = this.depth; i > 0; i--) {
                    if (i != this.depth) {
                        expectedTagStack.append(" and");
                    }
                    expectedTagStack.append(new StringBuffer().append(" start tag <").append(new String(this.elRawName[i], 0, this.elRawNameEnd[i])).append(Separators.GREATER_THAN).toString());
                    expectedTagStack.append(new StringBuffer().append(" from line ").append(this.elRawNameLine[i]).toString());
                }
                expectedTagStack.append(", parser stopped on");
            }
            throw new EOFException(new StringBuffer().append("no more data available").append(expectedTagStack.toString()).append(getPositionDescription()).toString());
        }
    }

    /* access modifiers changed from: protected */
    public char more() throws IOException, XmlPullParserException {
        if (this.pos >= this.bufEnd) {
            fillBuf();
            if (this.reachedEnd) {
                return 65535;
            }
        }
        char[] cArr = this.buf;
        int i = this.pos;
        this.pos = i + 1;
        char ch = cArr[i];
        if (ch == 10) {
            this.lineNumber++;
            this.columnNumber = 1;
            return ch;
        }
        this.columnNumber++;
        return ch;
    }

    /* access modifiers changed from: protected */
    public void ensurePC(int end) {
        char[] newPC = new char[(end > READ_CHUNK_SIZE ? end * 2 : 16384)];
        System.arraycopy(this.pc, 0, newPC, 0, this.pcEnd);
        this.pc = newPC;
    }

    /* access modifiers changed from: protected */
    public void joinPC() {
        int len = this.posEnd - this.posStart;
        int newEnd = (this.pcEnd + len) + 1;
        if (newEnd >= this.pc.length) {
            ensurePC(newEnd);
        }
        System.arraycopy(this.buf, this.posStart, this.pc, this.pcEnd, len);
        this.pcEnd += len;
        this.usePC = true;
    }

    /* access modifiers changed from: protected */
    public char requireInput(char ch, char[] input) throws XmlPullParserException, IOException {
        for (int i = 0; i < input.length; i++) {
            if (ch != input[i]) {
                throw new XmlPullParserException(new StringBuffer().append("expected ").append(printable(input[i])).append(" in ").append(new String(input)).append(" and not ").append(printable(ch)).toString(), this, null);
            }
            ch = more();
        }
        return ch;
    }

    /* access modifiers changed from: protected */
    public char requireNextS() throws XmlPullParserException, IOException {
        char ch = more();
        if (isS(ch)) {
            return skipS(ch);
        }
        throw new XmlPullParserException(new StringBuffer().append("white space is required and not ").append(printable(ch)).toString(), this, null);
    }

    /* access modifiers changed from: protected */
    public char skipS(char ch) throws XmlPullParserException, IOException {
        while (isS(ch)) {
            ch = more();
        }
        return ch;
    }

    private static final void setName(char ch) {
        lookupNameChar[ch] = true;
    }

    private static final void setNameStart(char ch) {
        lookupNameStartChar[ch] = true;
        setName(ch);
    }

    /* access modifiers changed from: protected */
    public boolean isNameStartChar(char ch) {
        return ((ch >= LOOKUP_MAX_CHAR || !lookupNameStartChar[ch]) && ((ch < LOOKUP_MAX_CHAR || ch > 8231) && ((ch < 8234 || ch > 8591) && (ch < 10240 || ch > 65519)))) ? TRACE_SIZING : true;
    }

    /* access modifiers changed from: protected */
    public boolean isNameChar(char ch) {
        return ((ch >= LOOKUP_MAX_CHAR || !lookupNameChar[ch]) && ((ch < LOOKUP_MAX_CHAR || ch > 8231) && ((ch < 8234 || ch > 8591) && (ch < 10240 || ch > 65519)))) ? TRACE_SIZING : true;
    }

    /* access modifiers changed from: protected */
    public boolean isS(char ch) {
        return (ch == ' ' || ch == 10 || ch == 13 || ch == 9) ? true : TRACE_SIZING;
    }

    /* access modifiers changed from: protected */
    public String printable(char ch) {
        if (ch == 10) {
            return "\\n";
        }
        if (ch == 13) {
            return "\\r";
        }
        if (ch == 9) {
            return "\\t";
        }
        if (ch == '\'') {
            return "\\'";
        }
        if (ch > 127 || ch < ' ') {
            return new StringBuffer().append("\\u").append(Integer.toHexString(ch)).toString();
        }
        return new StringBuffer().append("").append(ch).toString();
    }

    /* access modifiers changed from: protected */
    public String printable(String s) {
        if (s == null) {
            return null;
        }
        int sLen = s.length();
        StringBuffer buf = new StringBuffer(sLen + 10);
        for (int i = 0; i < sLen; i++) {
            buf.append(printable(s.charAt(i)));
        }
        return buf.toString();
    }
}
