package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror;

public class NoParentType extends BaseXCapError {
    protected String ancestor;

    NoParentType() {
        super(null);
    }

    public NoParentType(String phrase) {
        super(phrase);
    }

    public String getAncestor() {
        return this.ancestor;
    }

    /* access modifiers changed from: 0000 */
    public void setAncestor(String ancestor) {
        this.ancestor = ancestor;
    }
}
