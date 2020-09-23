package net.sf.fmj.media.multiplexer;

import java.io.PipedInputStream;

public class BigPipedInputStream extends PipedInputStream {
    public BigPipedInputStream(int size) {
        this.buffer = new byte[size];
    }
}
