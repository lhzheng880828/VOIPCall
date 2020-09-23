package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror;

import java.util.ArrayList;
import java.util.List;

public class UniquenessFailureType extends BaseXCapError {
    protected List<ExistsType> exists;

    public static class ExistsType {
        protected List<String> altValue;
        protected String field;

        public List<String> getAltValue() {
            if (this.altValue == null) {
                this.altValue = new ArrayList();
            }
            return this.altValue;
        }

        public String getField() {
            return this.field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    UniquenessFailureType() {
        super(null);
    }

    public UniquenessFailureType(String phrase) {
        super(phrase);
    }

    public List<ExistsType> getExists() {
        if (this.exists == null) {
            this.exists = new ArrayList();
        }
        return this.exists;
    }
}
