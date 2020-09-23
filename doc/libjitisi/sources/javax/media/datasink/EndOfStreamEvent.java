package javax.media.datasink;

import javax.media.DataSink;

public class EndOfStreamEvent extends DataSinkEvent {
    public EndOfStreamEvent(DataSink from) {
        super(from);
    }

    public EndOfStreamEvent(DataSink from, String reason) {
        super(from, reason);
    }
}
