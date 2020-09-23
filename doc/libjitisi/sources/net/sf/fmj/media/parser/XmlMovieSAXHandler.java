package net.sf.fmj.media.parser;

import com.lti.utils.StringUtils;
import com.lti.utils.synchronization.ProducerConsumerQueue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.media.Buffer;
import javax.media.Format;
import net.sf.fmj.utility.FormatArgUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/* compiled from: XmlMovieParser */
class XmlMovieSAXHandler extends DefaultHandler {
    private static final int AWAIT_BUFFER = 10;
    private static final int AWAIT_DATA = 11;
    private static final int INIT = 0;
    private static final int READ_DATA = 12;
    private Buffer currentBuffer;
    private StringBuilder currentDataChars;
    private int currentTrack = -1;
    private final Map<Integer, Format> formatsMap = new HashMap();
    private final Map<Integer, ProducerConsumerQueue> qBuffers = new HashMap();
    private final ProducerConsumerQueue qMeta = new ProducerConsumerQueue();
    private int state = 0;

    XmlMovieSAXHandler() {
    }

    private static int getIntAttr(Attributes atts, String qName) throws SAXException {
        if (atts.getIndex(qName) >= 0) {
            return getIntAttr(atts, qName, 0);
        }
        throw new SAXException("Missing attribute: " + qName);
    }

    private static int getIntAttr(Attributes atts, String qName, int defaultResult) throws SAXException {
        int index = atts.getIndex(qName);
        if (index < 0) {
            return defaultResult;
        }
        String s = atts.getValue(index);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new SAXException("Expected integer: " + s, e);
        }
    }

    private static long getLongAttr(Attributes atts, String qName) throws SAXException {
        if (atts.getIndex(qName) >= 0) {
            return getLongAttr(atts, qName, 0);
        }
        throw new SAXException("Missing attribute: " + qName);
    }

    private static long getLongAttr(Attributes atts, String qName, long defaultResult) throws SAXException {
        int index = atts.getIndex(qName);
        if (index < 0) {
            return defaultResult;
        }
        String s = atts.getValue(index);
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new SAXException("Expected long: " + s, e);
        }
    }

    private static String getStringAttr(Attributes atts, String qName) throws SAXException {
        if (atts.getIndex(qName) >= 0) {
            return getStringAttr(atts, qName, null);
        }
        throw new SAXException("Missing attribute: " + qName);
    }

    private static String getStringAttr(Attributes atts, String qName, String defaultResult) throws SAXException {
        int index = atts.getIndex(qName);
        if (index < 0) {
            return defaultResult;
        }
        return atts.getValue(index);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.state == 12) {
            this.currentDataChars.append(new String(ch, start, length));
            return;
        }
        String s = new String(ch, start, length).trim();
        if (s.length() > 0) {
            throw new SAXException("characters unexpected, state=" + this.state + " chars=" + s);
        }
    }

    public void endDocument() throws SAXException {
        if (this.qBuffers != null) {
            for (ProducerConsumerQueue q : this.qBuffers.values()) {
                if (q != null) {
                    Buffer eomBuffer = new Buffer();
                    eomBuffer.setEOM(true);
                    try {
                        q.put(eomBuffer);
                    } catch (InterruptedException e) {
                        throw new SAXException(e);
                    }
                }
            }
        }
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        if (localName.equals("Tracks")) {
            if (this.formatsMap.size() == 0) {
                throw new SAXException("No tracks");
            }
            try {
                Format[] formatsArray = new Format[this.formatsMap.size()];
                for (int i = 0; i < formatsArray.length; i++) {
                    Format format = (Format) this.formatsMap.get(Integer.valueOf(i));
                    if (format == null) {
                        throw new SAXException("Expected format for track " + i);
                    }
                    formatsArray[i] = format;
                }
                this.qMeta.put(formatsArray);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (localName.equals("Data")) {
            byte[] data = StringUtils.hexStringToByteArray(this.currentDataChars.toString());
            this.currentBuffer.setData(data);
            this.currentBuffer.setOffset(0);
            this.currentBuffer.setLength(data.length);
            try {
                ((ProducerConsumerQueue) this.qBuffers.get(Integer.valueOf(this.currentTrack))).put(this.currentBuffer);
                this.currentBuffer = null;
                this.currentTrack = -1;
                this.currentDataChars = null;
                this.state = 10;
            } catch (InterruptedException e2) {
                throw new SAXException(e2);
            }
        }
    }

    public void postError(Exception e) throws InterruptedException {
        if (this.qMeta != null) {
            this.qMeta.put(e);
        }
        if (this.qBuffers != null) {
            for (ProducerConsumerQueue q : this.qBuffers.values()) {
                if (q != null) {
                    q.put(e);
                }
            }
        }
    }

    public Buffer readBuffer(int track) throws SAXException, IOException, InterruptedException {
        Object o = ((ProducerConsumerQueue) this.qBuffers.get(Integer.valueOf(track))).get();
        if (o instanceof Buffer) {
            return (Buffer) o;
        }
        if (o instanceof SAXException) {
            throw ((SAXException) o);
        } else if (o instanceof IOException) {
            throw ((IOException) o);
        } else {
            throw new RuntimeException("Unknown object in queue: " + o);
        }
    }

    public Format[] readTracksInfo() throws SAXException, IOException, InterruptedException {
        Object o = this.qMeta.get();
        if (o instanceof Format[]) {
            return (Format[]) o;
        }
        if (o instanceof SAXException) {
            throw ((SAXException) o);
        } else if (o instanceof IOException) {
            throw ((IOException) o);
        } else {
            throw new RuntimeException("Unknown object in queue: " + o);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if (localName.equals("XmlMovie")) {
                if (!atts.getValue(atts.getIndex("version")).equals("1.0")) {
                    throw new SAXException("Expection XmlMovie version 1.0");
                }
            } else if (localName.equals("Track")) {
                int index = getIntAttr(atts, "index");
                this.formatsMap.put(Integer.valueOf(index), FormatArgUtils.parse(getStringAttr(atts, "format")));
                this.qBuffers.put(Integer.valueOf(index), new ProducerConsumerQueue());
            } else if (localName.equals("Buffer")) {
                this.currentTrack = getIntAttr(atts, "track");
                long sequenceNumber = getLongAttr(atts, "sequenceNumber", Buffer.SEQUENCE_UNKNOWN);
                long timeStamp = getLongAttr(atts, "timeStamp");
                long duration = getLongAttr(atts, "duration", -1);
                int flags = getIntAttr(atts, "flags", 0);
                String formatStr = getStringAttr(atts, "format", null);
                Format format = formatStr == null ? (Format) this.formatsMap.get(Integer.valueOf(this.currentTrack)) : FormatArgUtils.parse(formatStr);
                Buffer buffer = new Buffer();
                buffer.setSequenceNumber(sequenceNumber);
                buffer.setTimeStamp(timeStamp);
                buffer.setDuration(duration);
                buffer.setFlags(flags);
                buffer.setFormat(format);
                this.currentBuffer = buffer;
                this.currentDataChars = new StringBuilder();
                this.state = 11;
            } else if (!localName.equals("Data")) {
            } else {
                if (this.state != 11) {
                    throw new SAXException("Not expecting Data element");
                }
                this.state = 12;
            }
        } catch (SAXException e) {
            throw e;
        } catch (Exception e2) {
            throw new SAXException(e2);
        }
    }
}
