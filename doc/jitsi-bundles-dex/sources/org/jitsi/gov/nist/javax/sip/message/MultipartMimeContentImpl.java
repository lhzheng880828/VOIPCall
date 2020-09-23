package org.jitsi.gov.nist.javax.sip.message;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.HeaderFactoryExt;
import org.jitsi.gov.nist.javax.sip.header.HeaderFactoryImpl;
import org.jitsi.javax.sip.header.ContentDispositionHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.Header;

public class MultipartMimeContentImpl implements MultipartMimeContent {
    public static String BOUNDARY = "boundary";
    private String boundary;
    private List<Content> contentList = new LinkedList();
    private ContentTypeHeader multipartMimeContentTypeHeader;

    public MultipartMimeContentImpl(ContentTypeHeader contentTypeHeader) {
        this.multipartMimeContentTypeHeader = contentTypeHeader;
        this.boundary = contentTypeHeader.getParameter(BOUNDARY);
    }

    public boolean add(Content content) {
        return this.contentList.add((ContentImpl) content);
    }

    public ContentTypeHeader getContentTypeHeader() {
        return this.multipartMimeContentTypeHeader;
    }

    public String toString() {
        StringBuilder StringBuilder = new StringBuilder();
        for (Content content : this.contentList) {
            StringBuilder.append(content.toString());
        }
        return StringBuilder.toString();
    }

    public void createContentList(String body) throws ParseException {
        try {
            HeaderFactoryExt headerFactory = new HeaderFactoryImpl();
            String delimiter = getContentTypeHeader().getParameter(BOUNDARY);
            ContentImpl content;
            if (delimiter == null) {
                this.contentList = new LinkedList();
                content = new ContentImpl(body, delimiter);
                content.setContentTypeHeader(getContentTypeHeader());
                this.contentList.add(content);
                return;
            }
            String[] split = body.split("--" + delimiter + Separators.NEWLINE);
            int len$ = split.length;
            int i$ = 0;
            while (i$ < len$) {
                String nextPart = split[i$];
                if (nextPart != null) {
                    StringBuilder stringBuilder = new StringBuilder(nextPart);
                    while (stringBuilder.length() > 0 && (stringBuilder.charAt(0) == 13 || stringBuilder.charAt(0) == 10)) {
                        stringBuilder.deleteCharAt(0);
                    }
                    if (stringBuilder.length() != 0) {
                        nextPart = stringBuilder.toString();
                        int position = nextPart.indexOf("\r\n\r\n");
                        int off = 4;
                        if (position == -1) {
                            position = nextPart.indexOf(Separators.RETURN);
                            off = 2;
                        }
                        if (position == -1) {
                            throw new ParseException("no content type header found in " + nextPart, 0);
                        }
                        String rest = nextPart.substring(position + off);
                        if (rest == null) {
                            throw new ParseException("No content [" + nextPart + "]", 0);
                        }
                        String headers = nextPart.substring(0, position);
                        content = new ContentImpl(rest, this.boundary);
                        for (String hdr : headers.split(Separators.NEWLINE)) {
                            Header header = headerFactory.createHeader(hdr);
                            if (header instanceof ContentTypeHeader) {
                                content.setContentTypeHeader((ContentTypeHeader) header);
                            } else if (header instanceof ContentDispositionHeader) {
                                content.setContentDispositionHeader((ContentDispositionHeader) header);
                            } else {
                                throw new ParseException("Unexpected header type " + header.getName(), 0);
                            }
                            this.contentList.add(content);
                        }
                        continue;
                    }
                    i$++;
                } else {
                    return;
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("Invalid Multipart mime format", 0);
        }
    }

    public Content getContentByType(String contentType, String contentSubtype) {
        Content retval = null;
        if (this.contentList == null) {
            return null;
        }
        for (Content content : this.contentList) {
            if (content.getContentTypeHeader().getContentType().equalsIgnoreCase(contentType) && content.getContentTypeHeader().getContentSubType().equalsIgnoreCase(contentSubtype)) {
                retval = content;
                break;
            }
        }
        return retval;
    }

    public void addContent(Content content) {
        add(content);
    }

    public Iterator<Content> getContents() {
        return this.contentList.iterator();
    }

    public int getContentCount() {
        return this.contentList.size();
    }
}
