package javax.media.datasink;

import javax.media.DataSink;

public class DataSinkErrorEvent extends DataSinkEvent {
    public DataSinkErrorEvent(DataSink from) {
        super(from);
    }

    public DataSinkErrorEvent(DataSink from, String reason) {
        super(from, reason);
    }
}
