package org.jitsi.org.xmlpull.v1;

import org.jitsi.gov.nist.core.Separators;

public class XmlPullParserException extends Exception {
    protected int column;
    protected Throwable detail;
    protected int row;

    public XmlPullParserException(String s) {
        super(s);
        this.row = -1;
        this.column = -1;
    }

    public XmlPullParserException(String msg, XmlPullParser parser, Throwable chain) {
        String str;
        StringBuffer stringBuffer = new StringBuffer();
        if (msg == null) {
            str = "";
        } else {
            str = new StringBuffer().append(msg).append(Separators.SP).toString();
        }
        stringBuffer = stringBuffer.append(str);
        if (parser == null) {
            str = "";
        } else {
            str = new StringBuffer().append("(position:").append(parser.getPositionDescription()).append(") ").toString();
        }
        stringBuffer = stringBuffer.append(str);
        if (chain == null) {
            str = "";
        } else {
            str = new StringBuffer().append("caused by: ").append(chain).toString();
        }
        super(stringBuffer.append(str).toString());
        this.row = -1;
        this.column = -1;
        if (parser != null) {
            this.row = parser.getLineNumber();
            this.column = parser.getColumnNumber();
        }
        this.detail = chain;
    }

    public Throwable getDetail() {
        return this.detail;
    }

    public int getLineNumber() {
        return this.row;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public void printStackTrace() {
        if (this.detail == null) {
            super.printStackTrace();
            return;
        }
        synchronized (System.err) {
            System.err.println(new StringBuffer().append(super.getMessage()).append("; nested exception is:").toString());
            this.detail.printStackTrace();
        }
    }
}
