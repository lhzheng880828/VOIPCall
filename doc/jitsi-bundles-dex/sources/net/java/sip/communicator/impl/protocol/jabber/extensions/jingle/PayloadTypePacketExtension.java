package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class PayloadTypePacketExtension extends AbstractPacketExtension {
    public static final String CHANNELS_ATTR_NAME = "channels";
    public static final String CLOCKRATE_ATTR_NAME = "clockrate";
    public static final String ELEMENT_NAME = "payload-type";
    public static final String ID_ATTR_NAME = "id";
    public static final String MAXPTIME_ATTR_NAME = "maxptime";
    public static final String NAME_ATTR_NAME = "name";
    public static final String PTIME_ATTR_NAME = "ptime";

    public PayloadTypePacketExtension() {
        super(null, ELEMENT_NAME);
    }

    public void setChannels(int channels) {
        super.setAttribute(CHANNELS_ATTR_NAME, Integer.valueOf(channels));
    }

    public int getChannels() {
        return getAttributeAsInt(CHANNELS_ATTR_NAME, 1);
    }

    public void setClockrate(int clockrate) {
        super.setAttribute(CLOCKRATE_ATTR_NAME, Integer.valueOf(clockrate));
    }

    public int getClockrate() {
        return getAttributeAsInt(CLOCKRATE_ATTR_NAME);
    }

    public void setId(int id) {
        super.setAttribute("id", Integer.valueOf(id));
    }

    public int getID() {
        return getAttributeAsInt("id");
    }

    public void setMaxptime(int maxptime) {
        setAttribute(MAXPTIME_ATTR_NAME, Integer.valueOf(maxptime));
    }

    public int getMaxptime() {
        return getAttributeAsInt(MAXPTIME_ATTR_NAME);
    }

    public void setPtime(int ptime) {
        super.setAttribute(PTIME_ATTR_NAME, Integer.valueOf(ptime));
    }

    public int getPtime() {
        return getAttributeAsInt(PTIME_ATTR_NAME);
    }

    public void setName(String name) {
        setAttribute("name", name);
    }

    public String getName() {
        return getAttributeAsString("name");
    }

    public void addParameter(ParameterPacketExtension parameter) {
        addChildExtension(parameter);
    }

    public List<ParameterPacketExtension> getParameters() {
        return getChildExtensionsOfType(ParameterPacketExtension.class);
    }
}
