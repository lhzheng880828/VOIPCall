package javax.media;

public class Buffer {
    public static final int FLAG_BUF_OVERFLOWN = 8192;
    public static final int FLAG_BUF_UNDERFLOWN = 16384;
    public static final int FLAG_DISCARD = 2;
    public static final int FLAG_EOM = 1;
    public static final int FLAG_FLUSH = 512;
    public static final int FLAG_KEY_FRAME = 16;
    public static final int FLAG_LIVE_DATA = 32768;
    public static final int FLAG_NO_DROP = 32;
    public static final int FLAG_NO_SYNC = 96;
    public static final int FLAG_NO_WAIT = 64;
    public static final int FLAG_RELATIVE_TIME = 256;
    public static final int FLAG_RTP_MARKER = 2048;
    public static final int FLAG_RTP_TIME = 4096;
    public static final int FLAG_SID = 8;
    public static final int FLAG_SILENCE = 4;
    public static final int FLAG_SKIP_FEC = 65536;
    public static final int FLAG_SYSTEM_MARKER = 1024;
    public static final int FLAG_SYSTEM_TIME = 128;
    public static final long SEQUENCE_UNKNOWN = 9223372036854775806L;
    public static final long TIME_UNKNOWN = -1;
    protected Object data = null;
    protected long duration = -1;
    protected int flags = 0;
    protected Format format = null;
    protected Object header = null;
    protected int length = 0;
    protected int offset = 0;
    protected long sequenceNumber = SEQUENCE_UNKNOWN;
    protected long timeStamp = -1;

    public Object clone() {
        Buffer buf = new Buffer();
        Object data = getData();
        if (data != null) {
            if (data instanceof byte[]) {
                buf.data = ((byte[]) data).clone();
            } else if (data instanceof int[]) {
                buf.data = ((int[]) data).clone();
            } else if (data instanceof short[]) {
                buf.data = ((short[]) data).clone();
            } else {
                buf.data = data;
            }
        }
        if (this.header != null) {
            if (this.header instanceof byte[]) {
                buf.header = ((byte[]) this.header).clone();
            } else if (this.header instanceof int[]) {
                buf.header = ((int[]) this.header).clone();
            } else if (this.header instanceof short[]) {
                buf.header = ((short[]) this.header).clone();
            } else {
                buf.header = this.header;
            }
        }
        buf.format = this.format;
        buf.length = this.length;
        buf.offset = this.offset;
        buf.timeStamp = this.timeStamp;
        buf.duration = this.duration;
        buf.sequenceNumber = this.sequenceNumber;
        buf.flags = this.flags;
        return buf;
    }

    public void copy(Buffer buffer) {
        copy(buffer, false);
    }

    public void copy(Buffer buffer, boolean swapData) {
        if (swapData) {
            Object temp = this.data;
            this.data = buffer.data;
            buffer.data = temp;
        } else {
            this.data = buffer.data;
        }
        this.header = buffer.header;
        this.format = buffer.format;
        this.length = buffer.length;
        this.offset = buffer.offset;
        this.timeStamp = buffer.timeStamp;
        this.duration = buffer.duration;
        this.sequenceNumber = buffer.sequenceNumber;
        this.flags = buffer.flags;
    }

    public Object getData() {
        return this.data;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getFlags() {
        return this.flags;
    }

    public Format getFormat() {
        return this.format;
    }

    public Object getHeader() {
        return this.header;
    }

    public int getLength() {
        return this.length;
    }

    public int getOffset() {
        return this.offset;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public boolean isDiscard() {
        return (this.flags & 2) != 0;
    }

    public boolean isEOM() {
        return (this.flags & 1) != 0;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setDiscard(boolean discard) {
        if (discard) {
            this.flags |= 2;
        } else {
            this.flags &= -3;
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setEOM(boolean eom) {
        if (eom) {
            this.flags |= 1;
        } else {
            this.flags &= -2;
        }
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setSequenceNumber(long number) {
        this.sequenceNumber = number;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
