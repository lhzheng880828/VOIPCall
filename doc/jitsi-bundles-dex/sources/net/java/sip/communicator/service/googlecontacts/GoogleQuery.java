package net.java.sip.communicator.service.googlecontacts;

import java.util.regex.Pattern;

public class GoogleQuery {
    private boolean cancelled = false;
    private Pattern query = null;

    public GoogleQuery(Pattern query) {
        this.query = query;
    }

    public Pattern getQueryPattern() {
        return this.query;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
