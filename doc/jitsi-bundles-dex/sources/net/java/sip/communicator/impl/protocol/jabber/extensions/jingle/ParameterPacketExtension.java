package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class ParameterPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "parameter";
    public static final String NAME_ATTR_NAME = "name";
    public static final String VALUE_ATTR_NAME = "value";

    public ParameterPacketExtension() {
        super(null, ELEMENT_NAME);
    }

    public void setName(String name) {
        super.setAttribute("name", name);
    }

    public String getName() {
        return super.getAttributeAsString("name");
    }

    public void setValue(String value) {
        super.setAttribute(VALUE_ATTR_NAME, value);
    }

    public String getValue() {
        return super.getAttributeAsString(VALUE_ATTR_NAME);
    }
}
