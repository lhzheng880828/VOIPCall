package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;

public class RulesetType {
    private List<RuleType> rules;

    public List<RuleType> getRules() {
        if (this.rules == null) {
            this.rules = new ArrayList();
        }
        return this.rules;
    }
}
