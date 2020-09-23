package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import net.sf.fmj.utility.LoggerSingleton;

public class InputStreamPushSourceStream implements PushSourceStream {
    private static final Logger logger = LoggerSingleton.logger;
    private boolean eos;
    private final InputStream is;
    private final ContentDescriptor outputContentDescriptor;
    private SourceTransferHandler transferHandler;

    public InputStreamPushSourceStream(ContentDescriptor outputContentDescriptor, InputStream is) {
        this.outputContentDescriptor = outputContentDescriptor;
        this.is = is;
    }

    public boolean endOfStream() {
        logger.finer(getClass().getSimpleName() + " endOfStream");
        return this.eos;
    }

    public ContentDescriptor getContentDescriptor() {
        logger.finer(getClass().getSimpleName() + " getContentDescriptor");
        return this.outputContentDescriptor;
    }

    public long getContentLength() {
        logger.finer(getClass().getSimpleName() + " getContentLength");
        return 0;
    }

    public Object getControl(String controlType) {
        logger.finer(getClass().getSimpleName() + " getControl");
        return null;
    }

    public Object[] getControls() {
        logger.finer(getClass().getSimpleName() + " getControls");
        return new Object[0];
    }

    public int getMinimumTransferSize() {
        logger.finer(getClass().getSimpleName() + " getMinimumTransferSize");
        return 0;
    }

    public SourceTransferHandler getTransferHandler() {
        return this.transferHandler;
    }

    public void notifyDataAvailable() {
        if (this.transferHandler != null) {
            this.transferHandler.transferData(this);
        }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int result = this.is.read(buffer, offset, length);
        if (result < 0) {
            this.eos = true;
        }
        return result;
    }

    public void setTransferHandler(SourceTransferHandler transferHandler) {
        logger.finer(getClass().getSimpleName() + " setTransferHandler");
        this.transferHandler = transferHandler;
    }
}
