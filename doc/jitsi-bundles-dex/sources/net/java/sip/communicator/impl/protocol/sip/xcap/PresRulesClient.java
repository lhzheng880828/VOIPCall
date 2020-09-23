package net.java.sip.communicator.impl.protocol.sip.xcap;

import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.RulesetType;

public interface PresRulesClient {
    public static final String CONTENT_TYPE = "application/auth-policy+xml";
    public static final String DOCUMENT_FORMAT = "pres-rules/users/%2s/presrules";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:pres-rules";

    void deletePresRules() throws XCapException;

    RulesetType getPresRules() throws XCapException;

    void putPresRules(RulesetType rulesetType) throws XCapException;
}
