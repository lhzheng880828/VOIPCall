package net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.util.Logger;

public class GeolocationJabberUtils {
    private static final Logger logger = Logger.getLogger(GeolocationJabberUtils.class);

    public static Map<String, String> convertExtensionToMap(GeolocationPacketExtension geolocExt) {
        Map<String, String> geolocMap = new Hashtable();
        addFloatToMap(geolocMap, GeolocationPacketExtension.ALT, geolocExt.getAlt());
        addStringToMap(geolocMap, GeolocationPacketExtension.AREA, geolocExt.getArea());
        addFloatToMap(geolocMap, GeolocationPacketExtension.BEARING, geolocExt.getBearing());
        addStringToMap(geolocMap, GeolocationPacketExtension.BUILDING, geolocExt.getBuilding());
        addStringToMap(geolocMap, GeolocationPacketExtension.COUNTRY, geolocExt.getCountry());
        addStringToMap(geolocMap, GeolocationPacketExtension.DATUM, geolocExt.getDatum());
        addStringToMap(geolocMap, "description", geolocExt.getDescription());
        addFloatToMap(geolocMap, GeolocationPacketExtension.ERROR, geolocExt.getError());
        addStringToMap(geolocMap, GeolocationPacketExtension.FLOOR, geolocExt.getFloor());
        addFloatToMap(geolocMap, GeolocationPacketExtension.LAT, geolocExt.getLat());
        addStringToMap(geolocMap, GeolocationPacketExtension.LOCALITY, geolocExt.getLocality());
        addFloatToMap(geolocMap, GeolocationPacketExtension.LON, geolocExt.getLon());
        addStringToMap(geolocMap, GeolocationPacketExtension.POSTALCODE, geolocExt.getPostalCode());
        addStringToMap(geolocMap, GeolocationPacketExtension.REGION, geolocExt.getRegion());
        addStringToMap(geolocMap, GeolocationPacketExtension.ROOM, geolocExt.getRoom());
        addStringToMap(geolocMap, GeolocationPacketExtension.STREET, geolocExt.getStreet());
        addStringToMap(geolocMap, "text", geolocExt.getText());
        addStringToMap(geolocMap, GeolocationPacketExtension.TIMESTAMP, geolocExt.getTimestamp());
        return geolocMap;
    }

    private static void addFloatToMap(Map<String, String> map, String key, float value) {
        if (value != -1.0f) {
            map.put(key, new Float(value).toString());
        }
    }

    private static void addStringToMap(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public static GeolocationPacketExtension convertMapToExtension(Map<String, String> geolocation) {
        GeolocationPacketExtension geolocExt = new GeolocationPacketExtension();
        for (Entry<String, String> line : geolocation.entrySet()) {
            String curParam = (String) line.getKey();
            String curValue = (String) line.getValue();
            String setterFunction = "set" + (Character.toUpperCase(curParam.charAt(0)) + curParam.substring(1));
            try {
                try {
                    GeolocationPacketExtension.class.getMethod(setterFunction, new Class[]{String.class}).invoke(geolocExt, new Object[]{curValue});
                } catch (IllegalArgumentException exc) {
                    logger.error(exc);
                } catch (IllegalAccessException exc2) {
                    logger.error(exc2);
                } catch (InvocationTargetException exc3) {
                    logger.error(exc3);
                }
            } catch (SecurityException exc4) {
                logger.error(exc4);
            } catch (NoSuchMethodException exc5) {
                logger.error(exc5);
            }
        }
        return geolocExt;
    }
}
