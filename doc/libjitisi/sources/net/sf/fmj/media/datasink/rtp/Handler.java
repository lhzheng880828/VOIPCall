package net.sf.fmj.media.datasink.rtp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import net.sf.fmj.media.AbstractDataSink;
import net.sf.fmj.utility.LoggerSingleton;

public class Handler extends AbstractDataSink {
    private static final Logger logger = LoggerSingleton.logger;
    private ParsedRTPUrl parsedRTPUrl;
    private RTPManager rtpManager;
    private PushBufferDataSource source;
    private SendStream[] streams;

    public void close() {
        if (this.rtpManager != null) {
            this.rtpManager.dispose();
        }
        try {
            stop();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
        }
    }

    public String getContentType() {
        if (this.source != null) {
            return this.source.getContentType();
        }
        return null;
    }

    public Object getControl(String controlType) {
        logger.warning("TODO: getControl " + controlType);
        return null;
    }

    public Object[] getControls() {
        logger.warning("TODO: getControls");
        return new Object[0];
    }

    public void open() throws IOException, SecurityException {
        if (getOutputLocator() == null) {
            throw new IOException("Output locator not set");
        }
        try {
            this.parsedRTPUrl = RTPUrlParser.parse(getOutputLocator().toExternalForm());
            try {
                this.rtpManager = RTPManager.newInstance();
                this.rtpManager.initialize(new SessionAddress());
                RTPBonusFormatsMgr.addBonusFormats(this.rtpManager);
                int numStreams = this.source.getStreams().length;
                this.streams = new SendStream[numStreams];
                int numStreamsInUse = 0;
                for (int streamIndex = 0; streamIndex < numStreams; streamIndex++) {
                    String elementType;
                    Format format = this.source.getStreams()[streamIndex].getFormat();
                    if (format instanceof AudioFormat) {
                        elementType = ParsedRTPUrlElement.AUDIO;
                    } else if (format instanceof VideoFormat) {
                        elementType = ParsedRTPUrlElement.VIDEO;
                    } else {
                        logger.warning("Skipping unknown source stream format: " + format);
                    }
                    ParsedRTPUrlElement element = this.parsedRTPUrl.find(elementType);
                    if (element == null) {
                        logger.fine("Skipping source stream format not specified in URL: " + format);
                    } else {
                        this.rtpManager.addTarget(new SessionAddress(InetAddress.getByName(element.host), element.port, element.ttl));
                        this.streams[streamIndex] = this.rtpManager.createSendStream(this.source, streamIndex);
                        numStreamsInUse++;
                    }
                }
                if (numStreamsInUse <= 0) {
                    throw new IOException("No streams selected to be used");
                }
                this.source.connect();
            } catch (InvalidSessionAddressException e) {
                logger.log(Level.WARNING, "" + e, e);
                throw new IOException("" + e);
            } catch (UnsupportedFormatException e2) {
                logger.log(Level.WARNING, "" + e2, e2);
                throw new IOException("" + e2);
            }
        } catch (RTPUrlParserException e3) {
            logger.log(Level.WARNING, "" + e3, e3);
            throw new IOException("" + e3);
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        logger.finer("setSource: " + source);
        if (source instanceof PushBufferDataSource) {
            this.source = (PushBufferDataSource) source;
            return;
        }
        throw new IncompatibleSourceException();
    }

    public void start() throws IOException {
        this.source.start();
        for (int streamIndex = 0; streamIndex < this.streams.length; streamIndex++) {
            if (this.streams[streamIndex] != null) {
                this.streams[streamIndex].start();
            }
        }
    }

    public void stop() throws IOException {
        if (this.source != null) {
            this.source.stop();
        }
        if (this.streams != null) {
            for (int streamIndex = 0; streamIndex < this.streams.length; streamIndex++) {
                if (this.streams[streamIndex] != null) {
                    this.streams[streamIndex].stop();
                }
            }
        }
    }
}
