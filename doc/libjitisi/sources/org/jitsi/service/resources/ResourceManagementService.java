package org.jitsi.service.resources;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import org.jitsi.android.util.javax.swing.ImageIcon;

public interface ResourceManagementService {
    public static final String DEFAULT_LOCALE_CONFIG = "net.java.sip.communicator.service.resources.DefaultLocale";

    Iterator<Locale> getAvailableLocales();

    int getColor(String str);

    String getColorString(String str);

    String getI18NString(String str);

    String getI18NString(String str, Locale locale);

    String getI18NString(String str, String[] strArr);

    String getI18NString(String str, String[] strArr, Locale locale);

    char getI18nMnemonic(String str);

    char getI18nMnemonic(String str, Locale locale);

    ImageIcon getImage(String str);

    byte[] getImageInBytes(String str);

    InputStream getImageInputStream(String str);

    InputStream getImageInputStreamForPath(String str);

    String getImagePath(String str);

    URL getImageURL(String str);

    URL getImageURLForPath(String str);

    InputStream getSettingsInputStream(String str);

    InputStream getSettingsInputStream(String str, Class<?> cls);

    int getSettingsInt(String str);

    String getSettingsString(String str);

    URL getSettingsURL(String str);

    String getSoundPath(String str);

    URL getSoundURL(String str);

    URL getSoundURLForPath(String str);

    File prepareSkinBundleFromZip(File file) throws Exception;
}
