package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror;

public abstract class BaseXCapError implements XCapError {
    private String phrase;

    public BaseXCapError(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return this.phrase;
    }

    /* access modifiers changed from: 0000 */
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
