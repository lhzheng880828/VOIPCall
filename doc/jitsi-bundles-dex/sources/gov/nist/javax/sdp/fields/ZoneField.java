package gov.nist.javax.sdp.fields;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.TimeZoneAdjustment;
import org.jitsi.gov.nist.core.Separators;

public class ZoneField extends SDPField implements TimeZoneAdjustment {
    protected SDPObjectList zoneAdjustments = new SDPObjectList();

    public ZoneField() {
        super(SDPFieldNames.ZONE_FIELD);
    }

    public void addZoneAdjustment(ZoneAdjustment za) {
        this.zoneAdjustments.add(za);
    }

    public SDPObjectList getZoneAdjustments() {
        return this.zoneAdjustments;
    }

    public String encode() {
        StringBuilder retval = new StringBuilder(SDPFieldNames.ZONE_FIELD);
        ListIterator li = this.zoneAdjustments.listIterator();
        int num = 0;
        while (li.hasNext()) {
            ZoneAdjustment za = (ZoneAdjustment) li.next();
            if (num > 0) {
                retval.append(Separators.SP);
            }
            retval.append(za.encode());
            num++;
        }
        retval.append(Separators.NEWLINE);
        return retval.toString();
    }

    public Hashtable getZoneAdjustments(boolean create) throws SdpParseException {
        Hashtable result = new Hashtable();
        SDPObjectList zoneAdjustments = getZoneAdjustments();
        if (zoneAdjustments != null) {
            while (true) {
                ZoneAdjustment zone = (ZoneAdjustment) zoneAdjustments.next();
                if (zone == null) {
                    return result;
                }
                result.put(new Date(zone.getTime()), Integer.valueOf(Long.valueOf(zone.getTime()).toString()));
            }
        } else if (create) {
            return new Hashtable();
        } else {
            return null;
        }
    }

    public void setZoneAdjustments(Hashtable map) throws SdpException {
        if (map == null) {
            throw new SdpException("The map is null");
        }
        Enumeration e = map.keys();
        while (e.hasMoreElements()) {
            Date o = e.nextElement();
            if (o instanceof Date) {
                Date date = o;
                ZoneAdjustment zone = new ZoneAdjustment();
                zone.setTime(date.getTime());
                addZoneAdjustment(zone);
            } else {
                throw new SdpException("The map is not well-formated ");
            }
        }
    }

    public void setTypedTime(boolean typedTime) {
    }

    public boolean getTypedTime() {
        return false;
    }

    public Object clone() {
        ZoneField retval = (ZoneField) super.clone();
        if (this.zoneAdjustments != null) {
            retval.zoneAdjustments = (SDPObjectList) this.zoneAdjustments.clone();
        }
        return retval;
    }
}
