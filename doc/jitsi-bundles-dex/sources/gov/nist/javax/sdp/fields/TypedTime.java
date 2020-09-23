package gov.nist.javax.sdp.fields;

public class TypedTime extends SDPObject {
    int time;
    String unit;

    public String encode() {
        String retval = "" + Integer.toString(this.time);
        if (this.unit != null) {
            return retval + this.unit;
        }
        return retval;
    }

    public void setTime(int t) {
        this.time = t;
    }

    public int getTime() {
        return this.time;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String u) {
        this.unit = u;
    }
}
