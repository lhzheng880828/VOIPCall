package net.sf.fmj.media.datasink.rtp;

public class ParsedRTPUrl {
    public final ParsedRTPUrlElement[] elements;

    public ParsedRTPUrl(ParsedRTPUrlElement e) {
        this(new ParsedRTPUrlElement[]{e});
    }

    public ParsedRTPUrl(ParsedRTPUrlElement e, ParsedRTPUrlElement e2) {
        this(new ParsedRTPUrlElement[]{e, e2});
    }

    public ParsedRTPUrl(ParsedRTPUrlElement[] elements) {
        this.elements = elements;
    }

    public ParsedRTPUrlElement find(String type) {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i].type.equals(type)) {
                return this.elements[i];
            }
        }
        return null;
    }

    public String toString() {
        if (this.elements == null) {
            return "null";
        }
        StringBuffer b = new StringBuffer();
        b.append("rtp://");
        for (int i = 0; i < this.elements.length; i++) {
            if (i > 0) {
                b.append("&");
            }
            b.append(this.elements[i]);
        }
        return b.toString();
    }
}
