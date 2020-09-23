package org.jivesoftware.smackx.provider;

import java.text.ParseException;
import java.util.Date;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.FileElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.StreamInitiation;
import org.jivesoftware.smackx.packet.StreamInitiation.File;

public class StreamInitiationProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean done = false;
        String id = parser.getAttributeValue("", "id");
        String mimeType = parser.getAttributeValue("", ThumbnailElement.MIME_TYPE);
        StreamInitiation initiation = new StreamInitiation();
        String name = null;
        String size = null;
        String hash = null;
        String date = null;
        String desc = null;
        boolean isRanged = false;
        DataForm form = null;
        DataFormProvider dataFormProvider = new DataFormProvider();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            String namespace = parser.getNamespace();
            if (eventType == 2) {
                if (elementName.equals("file")) {
                    name = parser.getAttributeValue("", "name");
                    size = parser.getAttributeValue("", "size");
                    hash = parser.getAttributeValue("", "hash");
                    date = parser.getAttributeValue("", "date");
                } else if (elementName.equals("desc")) {
                    desc = parser.nextText();
                } else if (elementName.equals("range")) {
                    isRanged = true;
                } else if (elementName.equals("x") && namespace.equals("jabber:x:data")) {
                    form = (DataForm) dataFormProvider.parseExtension(parser);
                }
            } else if (eventType == 3) {
                if (elementName.equals(FileElement.ELEMENT_NAME)) {
                    done = true;
                } else if (elementName.equals("file")) {
                    long fileSize = 0;
                    if (!(size == null || size.trim().length() == 0)) {
                        try {
                            fileSize = Long.parseLong(size);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    Date fileDate = new Date();
                    if (date != null) {
                        try {
                            fileDate = StringUtils.parseXEP0082Date(date);
                        } catch (ParseException e2) {
                        }
                    }
                    File file = new File(name, fileSize);
                    file.setHash(hash);
                    file.setDate(fileDate);
                    file.setDesc(desc);
                    file.setRanged(isRanged);
                    initiation.setFile(file);
                }
            }
        }
        initiation.setSesssionID(id);
        initiation.setMimeType(mimeType);
        initiation.setFeatureNegotiationForm(form);
        return initiation;
    }
}
