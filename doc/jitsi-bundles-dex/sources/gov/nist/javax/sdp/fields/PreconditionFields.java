package gov.nist.javax.sdp.fields;

import java.io.Serializable;
import java.util.Vector;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ParameterNames;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;

public class PreconditionFields implements Serializable {
    public static final String[] DIRECTION = new String[]{PrivacyRule.SUBSCRIPTION_NONE, "send", "recv", "sendrecv"};
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_RECV = 2;
    public static final int DIRECTION_SEND = 1;
    public static final int DIRECTION_SENDRECV = 3;
    public static final String[] PRECONDITION = new String[]{"qos"};
    public static final int PRECONDITION_QOS = 0;
    public static final String[] STATUS = new String[]{"e2e", "local", "remote"};
    public static final int STATUS_E2E = 0;
    public static final int STATUS_LOCAL = 1;
    public static final int STATUS_REMOTE = 2;
    public static final String[] STRENGTH = new String[]{SubscriptionStateHeader.UNKNOWN, "failure", PrivacyRule.SUBSCRIPTION_NONE, ParameterNames.OPTIONAL, "mandatory"};
    public static final int STRENGTH_FAILURE = 1;
    public static final int STRENGTH_MANDATORY = 4;
    public static final int STRENGTH_NONE = 2;
    public static final int STRENGTH_OPTIONAL = 3;
    public static final int STRENGTH_UNKNOWN = 0;
    protected Vector preconditionAttributes = new Vector();

    public int getPreconditionSize() {
        if (this.preconditionAttributes != null) {
            return this.preconditionAttributes.size();
        }
        return -1;
    }

    public Vector getPreconditions() {
        return this.preconditionAttributes;
    }

    public void setPreconditions(Vector preconditions) throws SdpException {
        if (preconditions == null) {
            throw new SdpException("Precondition attributes are null");
        }
        this.preconditionAttributes = preconditions;
    }

    public void setPreconditionCurr(String precondCurrValue) throws SdpException {
        if (precondCurrValue == null) {
            throw new SdpException("The Precondition \"curr\" attribute value is null");
        } else if (this.preconditionAttributes == null) {
            throw new SdpException("The Precondition Attributes is null");
        } else {
            try {
                String[] attributes = precondCurrValue.split(Separators.SP);
                setPreconditionCurr(attributes[1], attributes[2]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new SdpException("Error spliting the \"curr\" attribute into words", ex);
            }
        }
    }

    public void setPreconditionCurr(String status, String directionTag) throws SdpException {
        if (status == null) {
            throw new SdpException("The status-type is null");
        } else if (directionTag == null) {
            throw new SdpException("The direction-tag is null");
        } else if (this.preconditionAttributes == null) {
            throw new SdpException("Precondition Attributes is null");
        } else {
            int i = 0;
            while (i < this.preconditionAttributes.size()) {
                AttributeField af = (AttributeField) this.preconditionAttributes.elementAt(i);
                if (af.getAttribute().getName().equals("curr") && af.getValue().indexOf(status) != -1) {
                    if (af.getValue().indexOf(directionTag) != -1) {
                        break;
                    }
                    af.setValue("qos " + status + Separators.SP + directionTag);
                    this.preconditionAttributes.setElementAt(af, i);
                }
                i++;
            }
            if (i == this.preconditionAttributes.size()) {
                NameValue nv = new NameValue("curr", "qos " + status + Separators.SP + directionTag);
                AttributeField newAF = new AttributeField();
                newAF.setAttribute(nv);
                this.preconditionAttributes.add(newAF);
            }
        }
    }

    public void setPreconditionDes(String precondDesValue) throws SdpException {
        if (precondDesValue == null) {
            throw new SdpException("The Precondition \"des\" attribute value is null");
        } else if (this.preconditionAttributes == null) {
            throw new SdpException("The Precondition Attributes is null");
        } else {
            try {
                String[] attributes = precondDesValue.split(Separators.SP);
                setPreconditionDes(attributes[1], attributes[2], attributes[3]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new SdpException("Error spliting the \"des\" attribute into words", ex);
            }
        }
    }

    public void setPreconditionDes(String strength, String status, String direction) throws SdpException {
        if (strength == null) {
            throw new SdpException("The strength-tag is null");
        } else if (status == null) {
            throw new SdpException("The status-type is null");
        } else if (direction == null) {
            throw new SdpException("The direction-tag is null");
        } else if (this.preconditionAttributes == null) {
            throw new SdpException("Precondition Attributes is null");
        } else {
            int i = 0;
            while (i < this.preconditionAttributes.size()) {
                AttributeField af = (AttributeField) this.preconditionAttributes.elementAt(i);
                if (af.getAttribute().getName().equals("des") && af.getValue().indexOf(status) != -1) {
                    af.setValue("qos " + strength + Separators.SP + status + Separators.SP + direction);
                    this.preconditionAttributes.setElementAt(af, i);
                }
                i++;
            }
            if (i == this.preconditionAttributes.size()) {
                NameValue nv = new NameValue("des", "qos " + strength + Separators.SP + status + Separators.SP + direction);
                AttributeField newAF = new AttributeField();
                newAF.setAttribute(nv);
                this.preconditionAttributes.add(newAF);
            }
        }
    }

    public void setPreconditionConfirmStatus(String precondConfValue) throws SdpException {
        if (precondConfValue == null || precondConfValue.length() == 0) {
            throw new SdpException("The Precondition \"conf\" attribute value is null");
        } else if (this.preconditionAttributes == null) {
            throw new SdpException("The Precondition Attributes is null");
        } else {
            try {
                String[] attributes = precondConfValue.split(Separators.SP);
                setPreconditionConfirmStatus(attributes[1], attributes[2]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new SdpException("Error spliting the \"conf\" attribute into words", ex);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0093  */
    public void setPreconditionConfirmStatus(java.lang.String r8, java.lang.String r9) throws javax.sdp.SdpException {
        /*
        r7 = this;
        r6 = -1;
        if (r8 == 0) goto L_0x0009;
    L_0x0003:
        r4 = r9.length();
        if (r4 != 0) goto L_0x0011;
    L_0x0009:
        r4 = new javax.sdp.SdpException;
        r5 = "The status-type is null";
        r4.m103init(r5);
        throw r4;
    L_0x0011:
        if (r9 == 0) goto L_0x0019;
    L_0x0013:
        r4 = r9.length();
        if (r4 != 0) goto L_0x0021;
    L_0x0019:
        r4 = new javax.sdp.SdpException;
        r5 = "The direction-tag is null";
        r4.m103init(r5);
        throw r4;
    L_0x0021:
        r4 = r7.preconditionAttributes;
        if (r4 != 0) goto L_0x002d;
    L_0x0025:
        r4 = new javax.sdp.SdpException;
        r5 = "Precondition Attributes is null";
        r4.m103init(r5);
        throw r4;
    L_0x002d:
        r1 = 0;
        r1 = 0;
    L_0x002f:
        r4 = r7.preconditionAttributes;
        r4 = r4.size();
        if (r1 >= r4) goto L_0x008b;
    L_0x0037:
        r4 = r7.preconditionAttributes;
        r0 = r4.elementAt(r1);
        r0 = (gov.nist.javax.sdp.fields.AttributeField) r0;
        r4 = r0.getAttribute();
        r4 = r4.getName();
        r5 = "conf";
        r4 = r4.equals(r5);
        if (r4 != 0) goto L_0x0052;
    L_0x004f:
        r1 = r1 + 1;
        goto L_0x002f;
    L_0x0052:
        r4 = r0.getValue();
        r4 = r4.indexOf(r8);
        if (r4 == r6) goto L_0x004f;
    L_0x005c:
        r4 = r0.getValue();
        r4 = r4.indexOf(r9);
        if (r4 != r6) goto L_0x008b;
    L_0x0066:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "qos ";
        r4 = r4.append(r5);
        r4 = r4.append(r8);
        r5 = " ";
        r4 = r4.append(r5);
        r4 = r4.append(r9);
        r4 = r4.toString();
        r0.setValue(r4);
        r4 = r7.preconditionAttributes;
        r4.setElementAt(r0, r1);
    L_0x008b:
        r4 = r7.preconditionAttributes;
        r4 = r4.size();
        if (r1 != r4) goto L_0x00c4;
    L_0x0093:
        r3 = new org.jitsi.gov.nist.core.NameValue;
        r4 = "conf";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "qos ";
        r5 = r5.append(r6);
        r5 = r5.append(r8);
        r6 = " ";
        r5 = r5.append(r6);
        r5 = r5.append(r9);
        r5 = r5.toString();
        r3.m1039init(r4, r5);
        r2 = new gov.nist.javax.sdp.fields.AttributeField;
        r2.m37init();
        r2.setAttribute(r3);
        r4 = r7.preconditionAttributes;
        r4.add(r2);
    L_0x00c4:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sdp.fields.PreconditionFields.setPreconditionConfirmStatus(java.lang.String, java.lang.String):void");
    }

    public Vector getPreconditionCurr(String status) throws SdpException, SdpParseException {
        if (status == null) {
            throw new SdpException("The status-type is null");
        } else if (this.preconditionAttributes == null) {
            return null;
        } else {
            Vector vCurr = new Vector();
            for (int i = 0; i < this.preconditionAttributes.size(); i++) {
                AttributeField af = (AttributeField) this.preconditionAttributes.elementAt(i);
                if (af.getAttribute().getName().equals("curr") && af.getValue().indexOf(status) != -1) {
                    vCurr.addElement(af);
                }
            }
            if (vCurr.size() == 0) {
                return null;
            }
            return vCurr;
        }
    }

    public Vector getPreconditionDes(String status) throws SdpException, SdpParseException {
        if (status == null) {
            throw new SdpException("The status-type is null");
        } else if (this.preconditionAttributes == null) {
            return null;
        } else {
            Vector vCurr = new Vector();
            for (int i = 0; i < this.preconditionAttributes.size(); i++) {
                AttributeField af = (AttributeField) this.preconditionAttributes.elementAt(i);
                if (af.getAttribute().getName().equals("des") && af.getValue().indexOf(status) != -1) {
                    vCurr.addElement(af);
                }
            }
            if (vCurr.size() == 0) {
                return null;
            }
            return vCurr;
        }
    }

    public Vector getPreconditionConfirmStatus() throws SdpException {
        if (this.preconditionAttributes == null) {
            return null;
        }
        Vector vCurr = new Vector();
        for (int i = 0; i < this.preconditionAttributes.size(); i++) {
            AttributeField af = (AttributeField) this.preconditionAttributes.elementAt(i);
            if (af.getAttribute().getName().equals("conf")) {
                vCurr.addElement(af);
            }
        }
        if (vCurr.size() == 0) {
            return null;
        }
        return vCurr;
    }
}
