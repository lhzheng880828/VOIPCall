package org.jivesoftware.smack.packet;

import org.jitsi.gov.nist.core.Separators;

public class PrivacyItem {
    private boolean allow;
    private boolean filterIQ = false;
    private boolean filterMessage = false;
    private boolean filterPresence_in = false;
    private boolean filterPresence_out = false;
    private int order;
    private PrivacyRule rule;

    public static class PrivacyRule {
        public static final String SUBSCRIPTION_BOTH = "both";
        public static final String SUBSCRIPTION_FROM = "from";
        public static final String SUBSCRIPTION_NONE = "none";
        public static final String SUBSCRIPTION_TO = "to";
        private Type type;
        private String value;

        protected static PrivacyRule fromString(String value) {
            if (value == null) {
                return null;
            }
            PrivacyRule rule = new PrivacyRule();
            rule.setType(Type.valueOf(value.toLowerCase()));
            return rule;
        }

        public Type getType() {
            return this.type;
        }

        private void setType(Type type) {
            this.type = type;
        }

        public String getValue() {
            return this.value;
        }

        /* access modifiers changed from: protected */
        public void setValue(String value) {
            if (isSuscription()) {
                setSuscriptionValue(value);
            } else {
                this.value = value;
            }
        }

        private void setSuscriptionValue(String value) {
            String setValue;
            if (value == null) {
            }
            if (SUBSCRIPTION_BOTH.equalsIgnoreCase(value)) {
                setValue = SUBSCRIPTION_BOTH;
            } else if ("to".equalsIgnoreCase(value)) {
                setValue = "to";
            } else if ("from".equalsIgnoreCase(value)) {
                setValue = "from";
            } else if (SUBSCRIPTION_NONE.equalsIgnoreCase(value)) {
                setValue = SUBSCRIPTION_NONE;
            } else {
                setValue = null;
            }
            this.value = setValue;
        }

        public boolean isSuscription() {
            return getType() == Type.subscription;
        }
    }

    public enum Type {
        group,
        jid,
        subscription
    }

    public PrivacyItem(String type, boolean allow, int order) {
        setRule(PrivacyRule.fromString(type));
        setAllow(allow);
        setOrder(order);
    }

    public boolean isAllow() {
        return this.allow;
    }

    private void setAllow(boolean allow) {
        this.allow = allow;
    }

    public boolean isFilterIQ() {
        return this.filterIQ;
    }

    public void setFilterIQ(boolean filterIQ) {
        this.filterIQ = filterIQ;
    }

    public boolean isFilterMessage() {
        return this.filterMessage;
    }

    public void setFilterMessage(boolean filterMessage) {
        this.filterMessage = filterMessage;
    }

    public boolean isFilterPresence_in() {
        return this.filterPresence_in;
    }

    public void setFilterPresence_in(boolean filterPresence_in) {
        this.filterPresence_in = filterPresence_in;
    }

    public boolean isFilterPresence_out() {
        return this.filterPresence_out;
    }

    public void setFilterPresence_out(boolean filterPresence_out) {
        this.filterPresence_out = filterPresence_out;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setValue(String value) {
        if (getRule() != null || value != null) {
            getRule().setValue(value);
        }
    }

    public Type getType() {
        if (getRule() == null) {
            return null;
        }
        return getRule().getType();
    }

    public String getValue() {
        if (getRule() == null) {
            return null;
        }
        return getRule().getValue();
    }

    public boolean isFilterEverything() {
        return (isFilterIQ() || isFilterMessage() || isFilterPresence_in() || isFilterPresence_out()) ? false : true;
    }

    private PrivacyRule getRule() {
        return this.rule;
    }

    private void setRule(PrivacyRule rule) {
        this.rule = rule;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<item");
        if (isAllow()) {
            buf.append(" action=\"allow\"");
        } else {
            buf.append(" action=\"deny\"");
        }
        buf.append(" order=\"").append(getOrder()).append(Separators.DOUBLE_QUOTE);
        if (getType() != null) {
            buf.append(" type=\"").append(getType()).append(Separators.DOUBLE_QUOTE);
        }
        if (getValue() != null) {
            buf.append(" value=\"").append(getValue()).append(Separators.DOUBLE_QUOTE);
        }
        if (isFilterEverything()) {
            buf.append("/>");
        } else {
            buf.append(Separators.GREATER_THAN);
            if (isFilterIQ()) {
                buf.append("<iq/>");
            }
            if (isFilterMessage()) {
                buf.append("<message/>");
            }
            if (isFilterPresence_in()) {
                buf.append("<presence-in/>");
            }
            if (isFilterPresence_out()) {
                buf.append("<presence-out/>");
            }
            buf.append("</item>");
        }
        return buf.toString();
    }
}
