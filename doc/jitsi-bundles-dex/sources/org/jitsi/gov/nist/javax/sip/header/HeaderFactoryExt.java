package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.extensions.JoinHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReferencesHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReferredByHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReplacesHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAccessNetworkInfoHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedServiceHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssociatedURIHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PCalledPartyIDHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PMediaAuthorizationHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PProfileKeyHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PServedUserHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PUserDatabaseHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PathHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PrivacyHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityClientHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityServerHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityVerifyHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderFactory;

public interface HeaderFactoryExt extends HeaderFactory {
    PChargingVectorHeader createChargingVectorHeader(String str) throws ParseException;

    Header createHeader(String str) throws ParseException;

    JoinHeader createJoinHeader(String str, String str2, String str3) throws ParseException;

    PAccessNetworkInfoHeader createPAccessNetworkInfoHeader();

    PAssertedIdentityHeader createPAssertedIdentityHeader(Address address) throws NullPointerException, ParseException;

    PAssertedServiceHeader createPAssertedServiceHeader();

    PAssociatedURIHeader createPAssociatedURIHeader(Address address);

    PCalledPartyIDHeader createPCalledPartyIDHeader(Address address);

    PChargingFunctionAddressesHeader createPChargingFunctionAddressesHeader();

    PMediaAuthorizationHeader createPMediaAuthorizationHeader(String str) throws InvalidArgumentException, ParseException;

    PPreferredIdentityHeader createPPreferredIdentityHeader(Address address);

    PPreferredServiceHeader createPPreferredServiceHeader();

    PProfileKeyHeader createPProfileKeyHeader(Address address);

    PServedUserHeader createPServedUserHeader(Address address);

    PUserDatabaseHeader createPUserDatabaseHeader(String str);

    PVisitedNetworkIDHeader createPVisitedNetworkIDHeader();

    PathHeader createPathHeader(Address address);

    PrivacyHeader createPrivacyHeader(String str);

    ReferencesHeader createReferencesHeader(String str, String str2) throws ParseException;

    ReferredByHeader createReferredByHeader(Address address);

    ReplacesHeader createReplacesHeader(String str, String str2, String str3) throws ParseException;

    SipRequestLine createRequestLine(String str) throws ParseException;

    SecurityClientHeader createSecurityClientHeader();

    SecurityServerHeader createSecurityServerHeader();

    SecurityVerifyHeader createSecurityVerifyHeader();

    ServiceRouteHeader createServiceRouteHeader(Address address);

    SessionExpiresHeader createSessionExpiresHeader(int i) throws InvalidArgumentException;

    SipStatusLine createStatusLine(String str) throws ParseException;
}
