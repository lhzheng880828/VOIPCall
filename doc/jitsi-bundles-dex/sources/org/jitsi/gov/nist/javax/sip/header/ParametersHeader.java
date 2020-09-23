package org.jitsi.gov.nist.javax.sip.header;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Iterator;
import org.jitsi.gov.nist.core.DuplicateNameValueList;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.NameValueList;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.javax.sip.header.Parameters;

public abstract class ParametersHeader extends SIPHeader implements Parameters, ParametersExt, Serializable {
    protected DuplicateNameValueList duplicates;
    protected NameValueList parameters;

    public abstract StringBuilder encodeBody(StringBuilder stringBuilder);

    protected ParametersHeader() {
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }

    protected ParametersHeader(String hdrName) {
        super(hdrName);
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }

    protected ParametersHeader(String hdrName, boolean sync) {
        super(hdrName);
        this.parameters = new NameValueList(sync);
        this.duplicates = new DuplicateNameValueList();
    }

    public String getParameter(String name) {
        return this.parameters.getParameter(name);
    }

    public String getParameter(String name, boolean stripQuotes) {
        return this.parameters.getParameter(name, stripQuotes);
    }

    public Object getParameterValue(String name) {
        return this.parameters.getValue(name);
    }

    public Iterator<String> getParameterNames() {
        return this.parameters.getNames();
    }

    public boolean hasParameters() {
        return (this.parameters == null || this.parameters.isEmpty()) ? false : true;
    }

    public void removeParameter(String name) {
        this.parameters.delete(name);
    }

    public void setParameter(String name, String value) throws ParseException {
        NameValue nv = this.parameters.getNameValue(name);
        if (nv != null) {
            nv.setValueAsObject(value);
            return;
        }
        this.parameters.set(new NameValue(name, value));
    }

    public void setQuotedParameter(String name, String value) throws ParseException {
        NameValue nv = this.parameters.getNameValue(name);
        if (nv != null) {
            nv.setValueAsObject(value);
            nv.setQuotedValue();
            return;
        }
        nv = new NameValue(name, value);
        nv.setQuotedValue();
        this.parameters.set(nv);
    }

    /* access modifiers changed from: protected */
    public void setParameter(String name, int value) {
        this.parameters.set(name, Integer.valueOf(value));
    }

    /* access modifiers changed from: protected */
    public void setParameter(String name, boolean value) {
        this.parameters.set(name, Boolean.valueOf(value));
    }

    /* access modifiers changed from: protected */
    public void setParameter(String name, float value) {
        Float val = Float.valueOf(value);
        NameValue nv = this.parameters.getNameValue(name);
        if (nv != null) {
            nv.setValueAsObject(val);
            return;
        }
        this.parameters.set(new NameValue(name, val));
    }

    /* access modifiers changed from: protected */
    public void setParameter(String name, Object value) {
        this.parameters.set(name, value);
    }

    public boolean hasParameter(String parameterName) {
        return this.parameters.hasNameValue(parameterName);
    }

    public void removeParameters() {
        this.parameters = new NameValueList();
    }

    public NameValueList getParameters() {
        return this.parameters;
    }

    public void setParameter(NameValue nameValue) {
        this.parameters.set(nameValue);
    }

    public void setParameters(NameValueList parameters) {
        this.parameters = parameters;
    }

    /* access modifiers changed from: protected */
    public int getParameterAsInt(String parameterName) {
        if (getParameterValue(parameterName) == null) {
            return -1;
        }
        try {
            if (getParameterValue(parameterName) instanceof String) {
                return Integer.parseInt(getParameter(parameterName));
            }
            return ((Integer) getParameterValue(parameterName)).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int getParameterAsHexInt(String parameterName) {
        if (getParameterValue(parameterName) == null) {
            return -1;
        }
        try {
            if (getParameterValue(parameterName) instanceof String) {
                return Integer.parseInt(getParameter(parameterName), 16);
            }
            return ((Integer) getParameterValue(parameterName)).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public float getParameterAsFloat(String parameterName) {
        if (getParameterValue(parameterName) == null) {
            return -1.0f;
        }
        try {
            if (getParameterValue(parameterName) instanceof String) {
                return Float.parseFloat(getParameter(parameterName));
            }
            return ((Float) getParameterValue(parameterName)).floatValue();
        } catch (NumberFormatException e) {
            return -1.0f;
        }
    }

    /* access modifiers changed from: protected */
    public long getParameterAsLong(String parameterName) {
        if (getParameterValue(parameterName) == null) {
            return -1;
        }
        try {
            if (getParameterValue(parameterName) instanceof String) {
                return Long.parseLong(getParameter(parameterName));
            }
            return ((Long) getParameterValue(parameterName)).longValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public GenericURI getParameterAsURI(String parameterName) {
        Object val = getParameterValue(parameterName);
        if (val instanceof GenericURI) {
            return (GenericURI) val;
        }
        try {
            return new GenericURI((String) val);
        } catch (ParseException e) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean getParameterAsBoolean(String parameterName) {
        Object val = getParameterValue(parameterName);
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        }
        if (val instanceof String) {
            return Boolean.valueOf((String) val).booleanValue();
        }
        return false;
    }

    public NameValue getNameValue(String parameterName) {
        return this.parameters.getNameValue(parameterName);
    }

    public Object clone() {
        ParametersHeader retval = (ParametersHeader) super.clone();
        if (this.parameters != null) {
            retval.parameters = (NameValueList) this.parameters.clone();
        }
        return retval;
    }

    public void setMultiParameter(String name, String value) {
        NameValue nv = new NameValue();
        nv.setName(name);
        nv.setValue(value);
        this.duplicates.set(nv);
    }

    public void setMultiParameter(NameValue nameValue) {
        this.duplicates.set(nameValue);
    }

    public String getMultiParameter(String name) {
        return this.duplicates.getParameter(name);
    }

    public DuplicateNameValueList getMultiParameters() {
        return this.duplicates;
    }

    public Object getMultiParameterValue(String name) {
        return this.duplicates.getValue(name);
    }

    public Iterator<String> getMultiParameterNames() {
        return this.duplicates.getNames();
    }

    public boolean hasMultiParameters() {
        return (this.duplicates == null || this.duplicates.isEmpty()) ? false : true;
    }

    public void removeMultiParameter(String name) {
        this.duplicates.delete(name);
    }

    public boolean hasMultiParameter(String parameterName) {
        return this.duplicates.hasNameValue(parameterName);
    }

    public void removeMultiParameters() {
        this.duplicates = new DuplicateNameValueList();
    }

    /* access modifiers changed from: protected|final */
    public final boolean equalParameters(Parameters other) {
        if (this == other) {
            return true;
        }
        String pname;
        String p1;
        String p2;
        Iterator i = getParameterNames();
        while (i.hasNext()) {
            pname = (String) i.next();
            p1 = getParameter(pname);
            p2 = other.getParameter(pname);
            if (((p2 == null ? 1 : 0) ^ (p1 == null ? 1 : 0)) != 0) {
                return false;
            }
            if (p1 != null && !p1.equalsIgnoreCase(p2)) {
                return false;
            }
        }
        i = other.getParameterNames();
        while (i.hasNext()) {
            pname = (String) i.next();
            p1 = other.getParameter(pname);
            p2 = getParameter(pname);
            if (((p2 == null ? 1 : 0) ^ (p1 == null ? 1 : 0)) != 0) {
                return false;
            }
            if (p1 != null && !p1.equalsIgnoreCase(p2)) {
                return false;
            }
        }
        return true;
    }
}
