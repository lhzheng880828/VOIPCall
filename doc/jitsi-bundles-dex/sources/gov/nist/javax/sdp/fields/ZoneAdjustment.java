package gov.nist.javax.sdp.fields;

import org.jitsi.gov.nist.core.Separators;

public class ZoneAdjustment extends SDPObject {
    protected TypedTime offset;
    protected String sign;
    protected long time;

    public void setTime(long t) {
        this.time = t;
    }

    public long getTime() {
        return this.time;
    }

    public TypedTime getOffset() {
        return this.offset;
    }

    public void setOffset(TypedTime off) {
        this.offset = off;
    }

    public void setSign(String s) {
        this.sign = s;
    }

    public String encode() {
        String retval = Long.toString(this.time) + Separators.SP;
        if (this.sign != null) {
            retval = retval + this.sign;
        }
        return retval + this.offset.encode();
    }

    public Object clone() {
        ZoneAdjustment retval = (ZoneAdjustment) super.clone();
        if (this.offset != null) {
            retval.offset = (TypedTime) this.offset.clone();
        }
        return retval;
    }
}
