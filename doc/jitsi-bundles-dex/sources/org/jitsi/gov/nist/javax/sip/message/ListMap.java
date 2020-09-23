package org.jitsi.gov.nist.javax.sip.message;

import java.util.HashMap;
import java.util.Map;
import org.jitsi.gov.nist.javax.sip.header.Accept;
import org.jitsi.gov.nist.javax.sip.header.AcceptEncoding;
import org.jitsi.gov.nist.javax.sip.header.AcceptEncodingList;
import org.jitsi.gov.nist.javax.sip.header.AcceptLanguage;
import org.jitsi.gov.nist.javax.sip.header.AcceptLanguageList;
import org.jitsi.gov.nist.javax.sip.header.AcceptList;
import org.jitsi.gov.nist.javax.sip.header.AlertInfo;
import org.jitsi.gov.nist.javax.sip.header.AlertInfoList;
import org.jitsi.gov.nist.javax.sip.header.Allow;
import org.jitsi.gov.nist.javax.sip.header.AllowList;
import org.jitsi.gov.nist.javax.sip.header.Authorization;
import org.jitsi.gov.nist.javax.sip.header.AuthorizationList;
import org.jitsi.gov.nist.javax.sip.header.CallInfo;
import org.jitsi.gov.nist.javax.sip.header.CallInfoList;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.ContactList;
import org.jitsi.gov.nist.javax.sip.header.ContentEncoding;
import org.jitsi.gov.nist.javax.sip.header.ContentEncodingList;
import org.jitsi.gov.nist.javax.sip.header.ContentLanguage;
import org.jitsi.gov.nist.javax.sip.header.ContentLanguageList;
import org.jitsi.gov.nist.javax.sip.header.ErrorInfo;
import org.jitsi.gov.nist.javax.sip.header.ErrorInfoList;
import org.jitsi.gov.nist.javax.sip.header.ExtensionHeaderImpl;
import org.jitsi.gov.nist.javax.sip.header.ExtensionHeaderList;
import org.jitsi.gov.nist.javax.sip.header.InReplyTo;
import org.jitsi.gov.nist.javax.sip.header.InReplyToList;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthenticate;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthenticateList;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthorization;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthorizationList;
import org.jitsi.gov.nist.javax.sip.header.ProxyRequire;
import org.jitsi.gov.nist.javax.sip.header.ProxyRequireList;
import org.jitsi.gov.nist.javax.sip.header.RecordRoute;
import org.jitsi.gov.nist.javax.sip.header.RecordRouteList;
import org.jitsi.gov.nist.javax.sip.header.Require;
import org.jitsi.gov.nist.javax.sip.header.RequireList;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.SIPHeaderList;
import org.jitsi.gov.nist.javax.sip.header.Supported;
import org.jitsi.gov.nist.javax.sip.header.SupportedList;
import org.jitsi.gov.nist.javax.sip.header.Unsupported;
import org.jitsi.gov.nist.javax.sip.header.UnsupportedList;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.header.ViaList;
import org.jitsi.gov.nist.javax.sip.header.WWWAuthenticate;
import org.jitsi.gov.nist.javax.sip.header.WWWAuthenticateList;
import org.jitsi.gov.nist.javax.sip.header.Warning;
import org.jitsi.gov.nist.javax.sip.header.WarningList;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedIdentity;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedIdentityList;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssociatedURI;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssociatedURIList;
import org.jitsi.gov.nist.javax.sip.header.ims.PMediaAuthorization;
import org.jitsi.gov.nist.javax.sip.header.ims.PMediaAuthorizationList;
import org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkID;
import org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkIDList;
import org.jitsi.gov.nist.javax.sip.header.ims.Path;
import org.jitsi.gov.nist.javax.sip.header.ims.PathList;
import org.jitsi.gov.nist.javax.sip.header.ims.Privacy;
import org.jitsi.gov.nist.javax.sip.header.ims.PrivacyList;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityClient;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityClientList;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityServer;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityServerList;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityVerify;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityVerifyList;
import org.jitsi.gov.nist.javax.sip.header.ims.ServiceRoute;
import org.jitsi.gov.nist.javax.sip.header.ims.ServiceRouteList;

public class ListMap {
    private static Map<Class<?>, Class<?>> headerListTable;
    private static boolean initialized;

    static {
        initializeListMap();
    }

    private static void initializeListMap() {
        headerListTable = new HashMap(34);
        headerListTable.put(ExtensionHeaderImpl.class, ExtensionHeaderList.class);
        headerListTable.put(Contact.class, ContactList.class);
        headerListTable.put(ContentEncoding.class, ContentEncodingList.class);
        headerListTable.put(Via.class, ViaList.class);
        headerListTable.put(WWWAuthenticate.class, WWWAuthenticateList.class);
        headerListTable.put(Accept.class, AcceptList.class);
        headerListTable.put(AcceptEncoding.class, AcceptEncodingList.class);
        headerListTable.put(AcceptLanguage.class, AcceptLanguageList.class);
        headerListTable.put(ProxyRequire.class, ProxyRequireList.class);
        headerListTable.put(Route.class, RouteList.class);
        headerListTable.put(Require.class, RequireList.class);
        headerListTable.put(Warning.class, WarningList.class);
        headerListTable.put(Unsupported.class, UnsupportedList.class);
        headerListTable.put(AlertInfo.class, AlertInfoList.class);
        headerListTable.put(CallInfo.class, CallInfoList.class);
        headerListTable.put(ProxyAuthenticate.class, ProxyAuthenticateList.class);
        headerListTable.put(ProxyAuthorization.class, ProxyAuthorizationList.class);
        headerListTable.put(Authorization.class, AuthorizationList.class);
        headerListTable.put(Allow.class, AllowList.class);
        headerListTable.put(RecordRoute.class, RecordRouteList.class);
        headerListTable.put(ContentLanguage.class, ContentLanguageList.class);
        headerListTable.put(ErrorInfo.class, ErrorInfoList.class);
        headerListTable.put(Supported.class, SupportedList.class);
        headerListTable.put(InReplyTo.class, InReplyToList.class);
        headerListTable.put(PAssociatedURI.class, PAssociatedURIList.class);
        headerListTable.put(PMediaAuthorization.class, PMediaAuthorizationList.class);
        headerListTable.put(Path.class, PathList.class);
        headerListTable.put(Privacy.class, PrivacyList.class);
        headerListTable.put(ServiceRoute.class, ServiceRouteList.class);
        headerListTable.put(PVisitedNetworkID.class, PVisitedNetworkIDList.class);
        headerListTable.put(SecurityClient.class, SecurityClientList.class);
        headerListTable.put(SecurityServer.class, SecurityServerList.class);
        headerListTable.put(SecurityVerify.class, SecurityVerifyList.class);
        headerListTable.put(PAssertedIdentity.class, PAssertedIdentityList.class);
        initialized = true;
    }

    public static boolean hasList(SIPHeader sipHeader) {
        if (sipHeader instanceof SIPHeaderList) {
            return false;
        }
        if (headerListTable.get(sipHeader.getClass()) != null) {
            return true;
        }
        return false;
    }

    public static boolean hasList(Class<?> sipHdrClass) {
        if (!initialized) {
            initializeListMap();
        }
        return headerListTable.get(sipHdrClass) != null;
    }

    public static Class<?> getListClass(Class<?> sipHdrClass) {
        if (!initialized) {
            initializeListMap();
        }
        return (Class) headerListTable.get(sipHdrClass);
    }

    public static SIPHeaderList<SIPHeader> getList(SIPHeader sipHeader) {
        if (!initialized) {
            initializeListMap();
        }
        try {
            SIPHeaderList<SIPHeader> shl = (SIPHeaderList) ((Class) headerListTable.get(sipHeader.getClass())).newInstance();
            shl.setHeaderName(sipHeader.getName());
            return shl;
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex2) {
            ex2.printStackTrace();
        }
        return null;
    }
}
