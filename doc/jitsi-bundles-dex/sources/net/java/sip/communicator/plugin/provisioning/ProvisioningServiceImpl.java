package net.java.sip.communicator.plugin.provisioning;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import net.java.sip.communicator.service.provisioning.ProvisioningService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.OrderedProperties;
import org.apache.http.NameValuePair;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.util.StringUtils;

public class ProvisioningServiceImpl implements ProvisioningService {
    private static final String PROPERTY_PROVISIONING_MANDATORY = "net.java.sip.communicator.plugin.provisioning.MANDATORY";
    static final String PROPERTY_PROVISIONING_PASSWORD = "net.java.sip.communicator.plugin.provisioning.auth";
    private static final String PROPERTY_PROVISIONING_URL = "net.java.sip.communicator.plugin.provisioning.URL";
    static final String PROPERTY_PROVISIONING_USERNAME = "net.java.sip.communicator.plugin.provisioning.auth.USERNAME";
    private static final String PROVISIONING_ALLOW_PREFIX_PROP = "provisioning.ALLOW_PREFIX";
    private static final String PROVISIONING_ENFORCE_PREFIX_PROP = "provisioning.ENFORCE_PREFIX";
    private static final String PROVISIONING_METHOD_PROP = "net.java.sip.communicator.plugin.provisioning.METHOD";
    public static final String PROVISIONING_UUID_PROP = "net.java.sip.communicator.UUID";
    private static final Logger logger = Logger.getLogger(ProvisioningServiceImpl.class);
    private static String provPassword = null;
    private static String provUsername = null;
    private List<String> allowedPrefixes = new ArrayList();

    public ProvisioningServiceImpl() {
        String uuid = (String) ProvisioningActivator.getConfigurationService().getProperty(PROVISIONING_UUID_PROP);
        if (uuid == null || uuid.equals("")) {
            ProvisioningActivator.getConfigurationService().setProperty(PROVISIONING_UUID_PROP, UUID.randomUUID().toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public void start(String url) {
        if (url == null) {
            url = getProvisioningUri();
        }
        if (!StringUtils.isNullOrEmpty(url)) {
            InputStream data = retrieveConfigurationFile(url);
            if (data != null) {
                ProvisioningActivator.getConfigurationService().setProperty(PROPERTY_PROVISIONING_URL, url);
                updateConfiguration(data);
            }
        }
    }

    public String getProvisioningMethod() {
        String provMethod = ProvisioningActivator.getConfigurationService().getString(PROVISIONING_METHOD_PROP);
        if (provMethod == null || provMethod.length() <= 0) {
            provMethod = ProvisioningActivator.getResourceService().getSettingsString("plugin.provisioning.DEFAULT_PROVISIONING_METHOD");
            if (provMethod != null && provMethod.length() > 0) {
                setProvisioningMethod(provMethod);
            }
        }
        return provMethod;
    }

    public void setProvisioningMethod(String provisioningMethod) {
        ProvisioningActivator.getConfigurationService().setProperty(PROVISIONING_METHOD_PROP, provisioningMethod);
    }

    public String getProvisioningUri() {
        String provUri = ProvisioningActivator.getConfigurationService().getString(PROPERTY_PROVISIONING_URL);
        if (provUri == null || provUri.length() <= 0) {
            provUri = ProvisioningActivator.getResourceService().getSettingsString("plugin.provisioning.DEFAULT_PROVISIONING_URI");
            if (provUri != null && provUri.length() > 0) {
                setProvisioningUri(provUri);
            }
        }
        return provUri;
    }

    public void setProvisioningUri(String uri) {
        ProvisioningActivator.getConfigurationService().setProperty(PROPERTY_PROVISIONING_URL, uri);
    }

    public String getProvisioningUsername() {
        return provUsername;
    }

    public String getProvisioningPassword() {
        return provPassword;
    }

    private InputStream retrieveConfigurationFile(String url) {
        return retrieveConfigurationFile(url, null);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:40:0x0145, code skipped:
            if (r56.indexOf("${resy}") != -1) goto L_0x0147;
     */
    public java.io.InputStream retrieveConfigurationFile(java.lang.String r56, java.util.List<org.apache.http.NameValuePair> r57) {
        /*
        r55 = this;
        r12 = 0;
        r13 = 0;
        r49 = new java.net.URL;	 Catch:{ Exception -> 0x004d }
        r0 = r49;
        r1 = r56;
        r0.<init>(r1);	 Catch:{ Exception -> 0x004d }
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getNetworkAddressManagerService();	 Catch:{ Exception -> 0x004d }
        r5 = r49.getHost();	 Catch:{ Exception -> 0x004d }
        r5 = java.net.InetAddress.getByName(r5);	 Catch:{ Exception -> 0x004d }
        r34 = r4.getLocalHost(r5);	 Catch:{ Exception -> 0x004d }
        r4 = "\\$\\{env\\.([^\\}]*)\\}";
        r39 = java.util.regex.Pattern.compile(r4);	 Catch:{ Exception -> 0x004d }
        r0 = r39;
        r1 = r56;
        r37 = r0.matcher(r1);	 Catch:{ Exception -> 0x004d }
        r46 = new java.lang.StringBuffer;	 Catch:{ Exception -> 0x004d }
        r46.<init>();	 Catch:{ Exception -> 0x004d }
    L_0x002e:
        r4 = r37.find();	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x0062;
    L_0x0034:
        r4 = 1;
        r0 = r37;
        r4 = r0.group(r4);	 Catch:{ Exception -> 0x004d }
        r52 = java.lang.System.getenv(r4);	 Catch:{ Exception -> 0x004d }
        if (r52 == 0) goto L_0x002e;
    L_0x0041:
        r4 = java.util.regex.Matcher.quoteReplacement(r52);	 Catch:{ Exception -> 0x004d }
        r0 = r37;
        r1 = r46;
        r0.appendReplacement(r1, r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x002e;
    L_0x004d:
        r18 = move-exception;
        r4 = logger;
        r4 = r4.isInfoEnabled();
        if (r4 == 0) goto L_0x005f;
    L_0x0056:
        r4 = logger;
        r5 = "Error retrieving provisioning file!";
        r0 = r18;
        r4.info(r5, r0);
    L_0x005f:
        r41 = 0;
    L_0x0061:
        return r41;
    L_0x0062:
        r0 = r37;
        r1 = r46;
        r0.appendTail(r1);	 Catch:{ Exception -> 0x004d }
        r56 = r46.toString();	 Catch:{ Exception -> 0x004d }
        r4 = "\\$\\{system\\.([^\\}]*)\\}";
        r39 = java.util.regex.Pattern.compile(r4);	 Catch:{ Exception -> 0x004d }
        r0 = r39;
        r1 = r56;
        r37 = r0.matcher(r1);	 Catch:{ Exception -> 0x004d }
        r46 = new java.lang.StringBuffer;	 Catch:{ Exception -> 0x004d }
        r46.<init>();	 Catch:{ Exception -> 0x004d }
    L_0x0080:
        r4 = r37.find();	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x009f;
    L_0x0086:
        r4 = 1;
        r0 = r37;
        r4 = r0.group(r4);	 Catch:{ Exception -> 0x004d }
        r52 = java.lang.System.getProperty(r4);	 Catch:{ Exception -> 0x004d }
        if (r52 == 0) goto L_0x0080;
    L_0x0093:
        r4 = java.util.regex.Matcher.quoteReplacement(r52);	 Catch:{ Exception -> 0x004d }
        r0 = r37;
        r1 = r46;
        r0.appendReplacement(r1, r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x0080;
    L_0x009f:
        r0 = r37;
        r1 = r46;
        r0.appendTail(r1);	 Catch:{ Exception -> 0x004d }
        r56 = r46.toString();	 Catch:{ Exception -> 0x004d }
        r4 = "${home.location}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x00c5;
    L_0x00b5:
        r4 = "${home.location}";
        r5 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getConfigurationService();	 Catch:{ Exception -> 0x004d }
        r5 = r5.getScHomeDirLocation();	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x00c5:
        r4 = "${home.name}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x00e0;
    L_0x00d0:
        r4 = "${home.name}";
        r5 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getConfigurationService();	 Catch:{ Exception -> 0x004d }
        r5 = r5.getScHomeDirName();	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x00e0:
        r4 = "${uuid}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x00ff;
    L_0x00eb:
        r5 = "${uuid}";
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getConfigurationService();	 Catch:{ Exception -> 0x004d }
        r6 = "net.java.sip.communicator.UUID";
        r4 = r4.getProperty(r6);	 Catch:{ Exception -> 0x004d }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r5, r4);	 Catch:{ Exception -> 0x004d }
    L_0x00ff:
        r4 = "${osname}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0118;
    L_0x010a:
        r4 = "${osname}";
        r5 = "os.name";
        r5 = java.lang.System.getProperty(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x0118:
        r4 = "${arch}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0131;
    L_0x0123:
        r4 = "${arch}";
        r5 = "os.arch";
        r5 = java.lang.System.getProperty(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x0131:
        r4 = "${resx}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 != r5) goto L_0x0147;
    L_0x013c:
        r4 = "${resy}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0181;
    L_0x0147:
        r47 = net.java.sip.communicator.plugin.desktoputil.ScreenInformation.getScreenBounds();	 Catch:{ Exception -> 0x004d }
        r4 = "${resx}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0166;
    L_0x0156:
        r4 = "${resx}";
        r0 = r47;
        r5 = r0.width;	 Catch:{ Exception -> 0x004d }
        r5 = java.lang.String.valueOf(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x0166:
        r4 = "${resy}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0181;
    L_0x0171:
        r4 = "${resy}";
        r0 = r47;
        r5 = r0.height;	 Catch:{ Exception -> 0x004d }
        r5 = java.lang.String.valueOf(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x0181:
        r4 = "${build}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x019a;
    L_0x018c:
        r4 = "${build}";
        r5 = "sip-communicator.version";
        r5 = java.lang.System.getProperty(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x019a:
        r4 = "${locale}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x01bd;
    L_0x01a5:
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getConfigurationService();	 Catch:{ Exception -> 0x004d }
        r5 = "net.java.sip.communicator.service.resources.DefaultLocale";
        r36 = r4.getString(r5);	 Catch:{ Exception -> 0x004d }
        if (r36 != 0) goto L_0x01b3;
    L_0x01b1:
        r36 = "";
    L_0x01b3:
        r4 = "${locale}";
        r0 = r56;
        r1 = r36;
        r56 = r0.replace(r4, r1);	 Catch:{ Exception -> 0x004d }
    L_0x01bd:
        r4 = "${ipaddr}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x01d4;
    L_0x01c8:
        r4 = "${ipaddr}";
        r5 = r34.getHostAddress();	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x01d4:
        r4 = "${hostname}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x01f3;
    L_0x01df:
        r4 = org.jitsi.util.OSUtils.IS_WINDOWS;	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x0281;
    L_0x01e3:
        r4 = "COMPUTERNAME";
        r38 = java.lang.System.getenv(r4);	 Catch:{ Exception -> 0x004d }
    L_0x01e9:
        r4 = "${hostname}";
        r0 = r56;
        r1 = r38;
        r56 = r0.replace(r4, r1);	 Catch:{ Exception -> 0x004d }
    L_0x01f3:
        r4 = "${hwaddr}";
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x02aa;
    L_0x01fe:
        if (r34 == 0) goto L_0x02aa;
    L_0x0200:
        r20 = java.net.NetworkInterface.getNetworkInterfaces();	 Catch:{ Exception -> 0x004d }
    L_0x0204:
        r4 = r20.hasMoreElements();	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x02aa;
    L_0x020a:
        r31 = r20.nextElement();	 Catch:{ Exception -> 0x004d }
        r31 = (java.net.NetworkInterface) r31;	 Catch:{ Exception -> 0x004d }
        r21 = r31.getInetAddresses();	 Catch:{ Exception -> 0x004d }
    L_0x0214:
        r4 = r21.hasMoreElements();	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x0204;
    L_0x021a:
        r33 = r21.nextElement();	 Catch:{ Exception -> 0x004d }
        r33 = (java.net.InetAddress) r33;	 Catch:{ Exception -> 0x004d }
        r4 = r33.equals(r34);	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x0214;
    L_0x0226:
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getNetworkAddressManagerService();	 Catch:{ Exception -> 0x004d }
        r0 = r31;
        r28 = r4.getHardwareAddress(r0);	 Catch:{ Exception -> 0x004d }
        if (r28 == 0) goto L_0x0214;
    L_0x0232:
        r16 = new java.lang.StringBuffer;	 Catch:{ Exception -> 0x004d }
        r16.<init>();	 Catch:{ Exception -> 0x004d }
        r14 = r28;
        r0 = r14.length;	 Catch:{ Exception -> 0x004d }
        r35 = r0;
        r30 = 0;
    L_0x023e:
        r0 = r30;
        r1 = r35;
        if (r0 >= r1) goto L_0x0291;
    L_0x0244:
        r26 = r14[r30];	 Catch:{ Exception -> 0x004d }
        if (r26 < 0) goto L_0x0287;
    L_0x0248:
        r27 = r26;
    L_0x024a:
        r48 = new java.lang.String;	 Catch:{ Exception -> 0x004d }
        r4 = 15;
        r0 = r27;
        if (r0 > r4) goto L_0x028e;
    L_0x0252:
        r4 = "0";
    L_0x0254:
        r0 = r48;
        r0.<init>(r4);	 Catch:{ Exception -> 0x004d }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004d }
        r4.<init>();	 Catch:{ Exception -> 0x004d }
        r0 = r48;
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x004d }
        r5 = java.lang.Integer.toHexString(r27);	 Catch:{ Exception -> 0x004d }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x004d }
        r48 = r4.toString();	 Catch:{ Exception -> 0x004d }
        r0 = r16;
        r1 = r48;
        r0.append(r1);	 Catch:{ Exception -> 0x004d }
        r4 = ":";
        r0 = r16;
        r0.append(r4);	 Catch:{ Exception -> 0x004d }
        r30 = r30 + 1;
        goto L_0x023e;
    L_0x0281:
        r38 = r34.getHostName();	 Catch:{ Exception -> 0x004d }
        goto L_0x01e9;
    L_0x0287:
        r0 = r26;
        r0 = r0 + 256;
        r27 = r0;
        goto L_0x024a;
    L_0x028e:
        r4 = "";
        goto L_0x0254;
    L_0x0291:
        r4 = r16.length();	 Catch:{ Exception -> 0x004d }
        r4 = r4 + -1;
        r0 = r16;
        r0.deleteCharAt(r4);	 Catch:{ Exception -> 0x004d }
        r4 = "${hwaddr}";
        r5 = r16.toString();	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x004d }
        goto L_0x0204;
    L_0x02aa:
        r4 = "?";
        r0 = r56;
        r4 = r0.contains(r4);	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x02e9;
    L_0x02b4:
        r4 = 63;
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r4 = r4 + 1;
        r5 = r56.length();	 Catch:{ Exception -> 0x004d }
        if (r4 == r5) goto L_0x02da;
    L_0x02c4:
        r4 = 63;
        r0 = r56;
        r4 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r4 = r4 + 1;
        r0 = r56;
        r12 = r0.substring(r4);	 Catch:{ Exception -> 0x004d }
        r4 = "&";
        r13 = r12.split(r4);	 Catch:{ Exception -> 0x004d }
    L_0x02da:
        r4 = 0;
        r5 = 63;
        r0 = r56;
        r5 = r0.indexOf(r5);	 Catch:{ Exception -> 0x004d }
        r0 = r56;
        r56 = r0.substring(r4, r5);	 Catch:{ Exception -> 0x004d }
    L_0x02e9:
        r7 = 0;
        r8 = 0;
        r9 = -1;
        r10 = -1;
        if (r13 == 0) goto L_0x03cf;
    L_0x02ef:
        r4 = r13.length;	 Catch:{ Exception -> 0x004d }
        if (r4 <= 0) goto L_0x03cf;
    L_0x02f2:
        r7 = new java.util.ArrayList;	 Catch:{ Exception -> 0x004d }
        r4 = r13.length;	 Catch:{ Exception -> 0x004d }
        r7.<init>(r4);	 Catch:{ Exception -> 0x004d }
        r8 = new java.util.ArrayList;	 Catch:{ Exception -> 0x004d }
        r4 = r13.length;	 Catch:{ Exception -> 0x004d }
        r8.<init>(r4);	 Catch:{ Exception -> 0x004d }
        r51 = "${username}";
        r40 = "${password}";
        r29 = 0;
    L_0x0304:
        r4 = r13.length;	 Catch:{ Exception -> 0x004d }
        r0 = r29;
        if (r0 >= r4) goto L_0x03cf;
    L_0x0309:
        r45 = r13[r29];	 Catch:{ Exception -> 0x004d }
        r4 = "=";
        r0 = r45;
        r22 = r0.indexOf(r4);	 Catch:{ Exception -> 0x004d }
        r17 = 0;
        r4 = -1;
        r0 = r22;
        if (r0 <= r4) goto L_0x0323;
    L_0x031a:
        r4 = 0;
        r0 = r45;
        r1 = r22;
        r17 = r0.substring(r4, r1);	 Catch:{ Exception -> 0x004d }
    L_0x0323:
        r0 = r57;
        r1 = r17;
        r43 = getParamValue(r0, r1);	 Catch:{ Exception -> 0x004d }
        r0 = r45;
        r1 = r51;
        r4 = r0.indexOf(r1);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0369;
    L_0x0336:
        if (r43 == 0) goto L_0x035e;
    L_0x0338:
        r0 = r45;
        r1 = r51;
        r2 = r43;
        r45 = r0.replace(r1, r2);	 Catch:{ Exception -> 0x004d }
    L_0x0342:
        r9 = r7.size();	 Catch:{ Exception -> 0x004d }
    L_0x0346:
        r4 = -1;
        r0 = r22;
        if (r0 <= r4) goto L_0x0390;
    L_0x034b:
        r0 = r17;
        r7.add(r0);	 Catch:{ Exception -> 0x004d }
        r4 = r22 + 1;
        r0 = r45;
        r4 = r0.substring(r4);	 Catch:{ Exception -> 0x004d }
        r8.add(r4);	 Catch:{ Exception -> 0x004d }
    L_0x035b:
        r29 = r29 + 1;
        goto L_0x0304;
    L_0x035e:
        r4 = "";
        r0 = r45;
        r1 = r51;
        r45 = r0.replace(r1, r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x0342;
    L_0x0369:
        r0 = r45;
        r1 = r40;
        r4 = r0.indexOf(r1);	 Catch:{ Exception -> 0x004d }
        r5 = -1;
        if (r4 == r5) goto L_0x0346;
    L_0x0374:
        if (r43 == 0) goto L_0x0385;
    L_0x0376:
        r0 = r45;
        r1 = r40;
        r2 = r43;
        r45 = r0.replace(r1, r2);	 Catch:{ Exception -> 0x004d }
    L_0x0380:
        r10 = r7.size();	 Catch:{ Exception -> 0x004d }
        goto L_0x0346;
    L_0x0385:
        r4 = "";
        r0 = r45;
        r1 = r40;
        r45 = r0.replace(r1, r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x0380;
    L_0x0390:
        r4 = logger;	 Catch:{ Exception -> 0x004d }
        r4 = r4.isInfoEnabled();	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x03c4;
    L_0x0398:
        r4 = logger;	 Catch:{ Exception -> 0x004d }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004d }
        r5.<init>();	 Catch:{ Exception -> 0x004d }
        r6 = "Invalid provisioning request parameter: \"";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x004d }
        r0 = r45;
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x004d }
        r6 = "\", is replaced by \"";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x004d }
        r0 = r45;
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x004d }
        r6 = "=\"";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x004d }
        r5 = r5.toString();	 Catch:{ Exception -> 0x004d }
        r4.info(r5);	 Catch:{ Exception -> 0x004d }
    L_0x03c4:
        r0 = r45;
        r7.add(r0);	 Catch:{ Exception -> 0x004d }
        r4 = "";
        r8.add(r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x035b;
    L_0x03cf:
        r44 = 0;
        r24 = 0;
        r5 = "net.java.sip.communicator.plugin.provisioning.auth.USERNAME";
        r6 = "net.java.sip.communicator.plugin.provisioning.auth";
        r11 = new net.java.sip.communicator.plugin.provisioning.ProvisioningServiceImpl$1;	 Catch:{ Throwable -> 0x0450 }
        r0 = r55;
        r11.m965init();	 Catch:{ Throwable -> 0x0450 }
        r4 = r56;
        r44 = net.java.sip.communicator.service.httputil.HttpUtils.postForm(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ Throwable -> 0x0450 }
    L_0x03e4:
        if (r44 != 0) goto L_0x0488;
    L_0x03e6:
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getConfigurationService();	 Catch:{ Exception -> 0x004d }
        r5 = "net.java.sip.communicator.plugin.provisioning.MANDATORY";
        r6 = 0;
        r4 = r4.getBoolean(r5, r6);	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x0484;
    L_0x03f3:
        if (r24 == 0) goto L_0x045d;
    L_0x03f5:
        r23 = r24.getLocalizedMessage();	 Catch:{ Exception -> 0x004d }
    L_0x03f9:
        r19 = new net.java.sip.communicator.plugin.desktoputil.ErrorDialog;	 Catch:{ Exception -> 0x004d }
        r4 = 0;
        r5 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getResourceService();	 Catch:{ Exception -> 0x004d }
        r6 = "plugin.provisioning.PROV_FAILED";
        r5 = r5.getI18NString(r6);	 Catch:{ Exception -> 0x004d }
        r6 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.getResourceService();	 Catch:{ Exception -> 0x004d }
        r11 = "plugin.provisioning.PROV_FAILED_MSG";
        r53 = 1;
        r0 = r53;
        r0 = new java.lang.String[r0];	 Catch:{ Exception -> 0x004d }
        r53 = r0;
        r54 = 0;
        r53[r54] = r23;	 Catch:{ Exception -> 0x004d }
        r0 = r53;
        r6 = r6.getI18NString(r11, r0);	 Catch:{ Exception -> 0x004d }
        r0 = r19;
        r1 = r24;
        r0.<init>(r4, r5, r6, r1);	 Catch:{ Exception -> 0x004d }
        r4 = 1;
        r0 = r19;
        r0.setModal(r4);	 Catch:{ Exception -> 0x004d }
        r19.showDialog();	 Catch:{ Exception -> 0x004d }
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.bundleContext;	 Catch:{ Exception -> 0x004d }
        r14 = r4.getBundles();	 Catch:{ Exception -> 0x004d }
        r0 = r14.length;	 Catch:{ Exception -> 0x004d }
        r35 = r0;
        r30 = 0;
    L_0x0439:
        r0 = r30;
        r1 = r35;
        if (r0 >= r1) goto L_0x0484;
    L_0x043f:
        r15 = r14[r30];	 Catch:{ Exception -> 0x004d }
        r4 = net.java.sip.communicator.plugin.provisioning.ProvisioningActivator.bundleContext;	 Catch:{ BundleException -> 0x0464 }
        r5 = r15.getBundleContext();	 Catch:{ BundleException -> 0x0464 }
        r4 = r4.equals(r5);	 Catch:{ BundleException -> 0x0464 }
        if (r4 == 0) goto L_0x0460;
    L_0x044d:
        r30 = r30 + 1;
        goto L_0x0439;
    L_0x0450:
        r48 = move-exception;
        r4 = logger;	 Catch:{ Exception -> 0x004d }
        r5 = "Error posting form";
        r0 = r48;
        r4.error(r5, r0);	 Catch:{ Exception -> 0x004d }
        r24 = r48;
        goto L_0x03e4;
    L_0x045d:
        r23 = "";
        goto L_0x03f9;
    L_0x0460:
        r15.stop();	 Catch:{ BundleException -> 0x0464 }
        goto L_0x044d;
    L_0x0464:
        r25 = move-exception;
        r4 = logger;	 Catch:{ Exception -> 0x004d }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004d }
        r5.<init>();	 Catch:{ Exception -> 0x004d }
        r6 = "Failed to being gentle stop ";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x004d }
        r6 = r15.getLocation();	 Catch:{ Exception -> 0x004d }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x004d }
        r5 = r5.toString();	 Catch:{ Exception -> 0x004d }
        r0 = r25;
        r4.error(r5, r0);	 Catch:{ Exception -> 0x004d }
        goto L_0x044d;
    L_0x0484:
        r41 = 0;
        goto L_0x0061;
    L_0x0488:
        r50 = r44.getCredentials();	 Catch:{ Exception -> 0x004d }
        r4 = 0;
        r4 = r50[r4];	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x04a0;
    L_0x0491:
        r4 = 1;
        r4 = r50[r4];	 Catch:{ Exception -> 0x004d }
        if (r4 == 0) goto L_0x04a0;
    L_0x0496:
        r4 = 0;
        r4 = r50[r4];	 Catch:{ Exception -> 0x004d }
        provUsername = r4;	 Catch:{ Exception -> 0x004d }
        r4 = 1;
        r4 = r50[r4];	 Catch:{ Exception -> 0x004d }
        provPassword = r4;	 Catch:{ Exception -> 0x004d }
    L_0x04a0:
        r32 = r44.getContent();	 Catch:{ Exception -> 0x004d }
        r4 = org.jitsi.util.OSUtils.IS_ANDROID;	 Catch:{ Exception -> 0x004d }
        if (r4 != 0) goto L_0x04c6;
    L_0x04a8:
        r41 = new javax.swing.ProgressMonitorInputStream;	 Catch:{ Exception -> 0x004d }
        r4 = 0;
        r5 = r49.toString();	 Catch:{ Exception -> 0x004d }
        r0 = r41;
        r1 = r32;
        r0.<init>(r4, r5, r1);	 Catch:{ Exception -> 0x004d }
        r42 = r41.getProgressMonitor();	 Catch:{ Exception -> 0x004d }
        r4 = r44.getContentLength();	 Catch:{ Exception -> 0x004d }
        r4 = (int) r4;	 Catch:{ Exception -> 0x004d }
        r0 = r42;
        r0.setMaximum(r4);	 Catch:{ Exception -> 0x004d }
        goto L_0x0061;
    L_0x04c6:
        r41 = r32;
        goto L_0x0061;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.plugin.provisioning.ProvisioningServiceImpl.retrieveConfigurationFile(java.lang.String, java.util.List):java.io.InputStream");
    }

    private static String getParamValue(List<NameValuePair> parameters, String paramName) {
        if (parameters == null || paramName == null) {
            return null;
        }
        for (NameValuePair nv : parameters) {
            if (nv.getName().equals(paramName)) {
                return nv.getValue();
            }
        }
        return null;
    }

    private void updateConfiguration(InputStream data) {
        Throwable th;
        Properties fileProps = new OrderedProperties();
        InputStream in = null;
        try {
            InputStream in2 = new BufferedInputStream(data);
            try {
                fileProps.load(in2);
                for (Entry<Object, Object> entry : fileProps.entrySet()) {
                    String key = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (key.trim().length() != 0) {
                        if (key.equals(PROVISIONING_ALLOW_PREFIX_PROP)) {
                            for (String s : ((String) value).split("\\|")) {
                                this.allowedPrefixes.add(s);
                            }
                        } else if (key.equals(PROVISIONING_ENFORCE_PREFIX_PROP)) {
                            checkEnforcePrefix((String) value);
                        } else if (isPrefixAllowed(key)) {
                            processProperty(key, value);
                        }
                    }
                }
                try {
                    ProvisioningActivator.getConfigurationService().storeConfiguration();
                    ProvisioningActivator.getConfigurationService().reloadConfiguration();
                } catch (Exception e) {
                    logger.error("Cannot reload configuration");
                }
                try {
                    in2.close();
                    in = in2;
                } catch (IOException e2) {
                    in = in2;
                }
            } catch (IOException e3) {
                in = in2;
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                try {
                    in.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            try {
                logger.warn("Error during load of provisioning file");
                try {
                    in.close();
                } catch (IOException e6) {
                }
            } catch (Throwable th3) {
                th = th3;
                in.close();
                throw th;
            }
        }
    }

    private boolean isPrefixAllowed(String key) {
        if (this.allowedPrefixes.size() <= 0) {
            return true;
        }
        for (String s : this.allowedPrefixes) {
            if (key.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private void processProperty(String key, Object value) {
        if ((value instanceof String) && value.equals("${null}")) {
            ProvisioningActivator.getConfigurationService().removeProperty(key);
            if (logger.isInfoEnabled()) {
                logger.info(key + Separators.EQUALS + value);
            }
        } else if (key.endsWith(".PASSWORD")) {
            ProvisioningActivator.getCredentialsStorageService().storePassword(key.substring(0, key.lastIndexOf(Separators.DOT)), (String) value);
            if (logger.isInfoEnabled()) {
                logger.info(key + "=<password hidden>");
            }
        } else {
            ProvisioningActivator.getConfigurationService().setProperty(key, value);
            if (logger.isInfoEnabled()) {
                logger.info(key + Separators.EQUALS + value);
            }
        }
    }

    private void checkEnforcePrefix(String enforcePrefix) {
        ConfigurationService config = ProvisioningActivator.getConfigurationService();
        if (enforcePrefix != null) {
            String[] prefixes = enforcePrefix.split("\\|");
            for (String key : config.getAllPropertyNames()) {
                boolean isValid = false;
                for (String k : prefixes) {
                    if (key.startsWith(k)) {
                        isValid = true;
                        break;
                    }
                }
                if (!isValid) {
                    config.removeProperty(key);
                }
            }
        }
    }
}
