package net.sf.fmj.media.parser;

import com.lti.utils.synchronization.CloseableThread;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/* compiled from: XmlMovieParser */
class XmlMovieSAXParserThread extends CloseableThread {
    private final XmlMovieSAXHandler handler;
    private final InputStream is;

    public XmlMovieSAXParserThread(XmlMovieSAXHandler handler, InputStream is) {
        this.handler = handler;
        this.is = is;
    }

    public void run() {
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(this.handler);
            parser.parse(new InputSource(this.is));
        } catch (SAXException e) {
            try {
                this.handler.postError(e);
            } catch (InterruptedException e2) {
                setClosed();
                return;
            } catch (Throwable th) {
                setClosed();
            }
        } catch (IOException e3) {
            this.handler.postError(e3);
        }
        setClosed();
    }
}
