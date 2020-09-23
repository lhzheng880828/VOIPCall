package net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.util.Logger;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.StreamInitiation;
import org.jivesoftware.smackx.packet.StreamInitiation.File;
import org.jivesoftware.smackx.provider.DataFormProvider;

public class FileElement extends File implements IQProvider {
    private static final List<DateFormat> DATE_FORMATS = new ArrayList();
    public static final String ELEMENT_NAME = "si";
    public static final String NAMESPACE = "http://jabber.org/protocol/si";
    private static final Logger logger = Logger.getLogger(FileElement.class);
    private ThumbnailElement thumbnail;

    static {
        DATE_FORMATS.add(DelayInformation.XEP_0091_UTC_FORMAT);
        DateFormat fmt = new SimpleDateFormat("yyyyMd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMATS.add(fmt);
        fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMATS.add(fmt);
        fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMATS.add(fmt);
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }

    public FileElement() {
        this("", 0);
    }

    public FileElement(File baseFile, ThumbnailElement thumbnail) {
        this(baseFile.getName(), baseFile.getSize());
        this.thumbnail = thumbnail;
    }

    public FileElement(String name, long size) {
        super(name, size);
    }

    public String toXML() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\" ");
        if (getName() != null) {
            buffer.append("name=\"").append(StringUtils.escapeForXML(getName())).append("\" ");
        }
        if (getSize() > 0) {
            buffer.append("size=\"").append(getSize()).append("\" ");
        }
        if (getDate() != null) {
            buffer.append("date=\"").append(StringUtils.formatXEP0082Date(getDate())).append("\" ");
        }
        if (getHash() != null) {
            buffer.append("hash=\"").append(getHash()).append("\" ");
        }
        if ((getDesc() == null || getDesc().length() <= 0) && !isRanged() && this.thumbnail == null) {
            buffer.append("/>");
        } else {
            buffer.append(Separators.GREATER_THAN);
            if (getDesc() != null && getDesc().length() > 0) {
                buffer.append("<desc>").append(StringUtils.escapeForXML(getDesc())).append("</desc>");
            }
            if (isRanged()) {
                buffer.append("<range/>");
            }
            if (this.thumbnail != null) {
                buffer.append(this.thumbnail.toXML());
            }
            buffer.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        }
        return buffer.toString();
    }

    public ThumbnailElement getThumbnailElement() {
        return this.thumbnail;
    }

    public void setThumbnailElement(ThumbnailElement thumbnail) {
        this.thumbnail = thumbnail;
    }

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
        ThumbnailElement thumbnail = null;
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
                } else if (elementName.equals(ThumbnailElement.ELEMENT_NAME)) {
                    ThumbnailElement thumbnailElement = new ThumbnailElement(parser.getText());
                }
            } else if (eventType == 3) {
                if (elementName.equals(ELEMENT_NAME)) {
                    done = true;
                } else if (elementName.equals("file") && name != null) {
                    long fileSize = 0;
                    if (!(size == null || size.trim().length() == 0)) {
                        try {
                            fileSize = Long.parseLong(size);
                        } catch (NumberFormatException e) {
                            logger.warn("Received an invalid file size, continuing with fileSize set to 0", e);
                        }
                    }
                    FileElement file = new FileElement(name, fileSize);
                    file.setHash(hash);
                    if (date != null) {
                        boolean found = false;
                        if (date.matches(".*?T\\d+:\\d+:\\d+(\\.\\d+)?(\\+|-)\\d+:\\d+")) {
                            int timeZoneColon = date.lastIndexOf(Separators.COLON);
                            date = date.substring(0, timeZoneColon) + date.substring(timeZoneColon + 1, date.length());
                        }
                        for (DateFormat fmt : DATE_FORMATS) {
                            try {
                                file.setDate(fmt.parse(date));
                                found = true;
                                break;
                            } catch (ParseException e2) {
                            }
                        }
                        if (!found) {
                            logger.warn("Unknown dateformat on incoming file transfer: " + date);
                        }
                    }
                    if (thumbnail != null) {
                        file.setThumbnailElement(thumbnail);
                    }
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
