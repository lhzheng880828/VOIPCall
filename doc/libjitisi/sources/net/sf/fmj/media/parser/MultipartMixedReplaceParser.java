package net.sf.fmj.media.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.BadHeaderException;
import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.Track;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import net.sf.fmj.media.AbstractDemultiplexer;
import net.sf.fmj.media.AbstractTrack;
import net.sf.fmj.media.format.GIFFormat;
import net.sf.fmj.media.format.PNGFormat;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Image;

public class MultipartMixedReplaceParser extends AbstractDemultiplexer {
    public static final String TIMESTAMP_KEY = "X-FMJ-Timestamp";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    private static final String[] supportedFrameContentTypes = new String[]{"image/jpeg", "image/gif", "image/png"};
    private PullDataSource source;
    private ContentDescriptor[] supportedInputContentDescriptors = new ContentDescriptor[]{new ContentDescriptor("multipart.x_mixed_replace")};
    private PullSourceStreamTrack[] tracks;

    private abstract class PullSourceStreamTrack extends AbstractTrack {
        public abstract void deallocate();

        private PullSourceStreamTrack() {
        }
    }

    private class VideoTrack extends PullSourceStreamTrack {
        private static final int MAX_LINE_LENGTH = 255;
        private final int MAX_IMAGE_SIZE = 1000000;
        private String boundary;
        private final VideoFormat format;
        private String frameContentType;
        private int framesRead;
        private byte[] pushbackBuffer;
        private int pushbackBufferLen;
        private int pushbackBufferOffset;
        private final PullSourceStream stream;

        private class MaxLengthExceededException extends IOException {
            public MaxLengthExceededException(String s) {
                super(s);
            }
        }

        public VideoTrack(PullSourceStream stream) throws ResourceUnavailableException {
            super();
            this.stream = stream;
            Buffer buffer = new Buffer();
            readFrame(buffer);
            if (buffer.isDiscard() || buffer.isEOM()) {
                throw new ResourceUnavailableException("Unable to read first frame");
            }
            try {
                Image image = ImageIO.read(new ByteArrayInputStream((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength()));
                if (image == null) {
                    MultipartMixedReplaceParser.logger.log(Level.WARNING, "Failed to read image (ImageIO.read returned null).");
                    throw new ResourceUnavailableException();
                } else if (this.frameContentType.equals("image/jpeg")) {
                    this.format = new JPEGFormat(new Dimension(image.getWidth(null), image.getHeight(null)), -1, Format.byteArray, -1.0f, -1, -1);
                } else if (this.frameContentType.equals("image/gif")) {
                    this.format = new GIFFormat(new Dimension(image.getWidth(null), image.getHeight(null)), -1, Format.byteArray, -1.0f);
                } else if (this.frameContentType.equals("image/png")) {
                    this.format = new PNGFormat(new Dimension(image.getWidth(null), image.getHeight(null)), -1, Format.byteArray, -1.0f);
                } else {
                    throw new ResourceUnavailableException("Unsupported frame content type: " + this.frameContentType);
                }
            } catch (IOException e) {
                MultipartMixedReplaceParser.logger.log(Level.WARNING, "" + e, e);
                throw new ResourceUnavailableException("Error reading image: " + e);
            }
        }

        public boolean canSkipNanos() {
            return false;
        }

        public void deallocate() {
        }

        private int eatUntil(String boundary) throws IOException {
            int totalEaten = 0;
            byte[] boundaryBytes = boundary.getBytes();
            byte[] matchBuffer = new byte[boundaryBytes.length];
            int matchOffset = 0;
            while (read(matchBuffer, matchOffset, 1) >= 0) {
                totalEaten++;
                if (matchBuffer[matchOffset] == boundaryBytes[matchOffset]) {
                    if (matchOffset == boundaryBytes.length - 1) {
                        pushback(matchBuffer, matchOffset + 1);
                        totalEaten -= matchOffset + 1;
                        int i = totalEaten;
                        return totalEaten;
                    }
                    matchOffset++;
                } else if (matchOffset > 0) {
                    matchOffset = 0;
                }
            }
            return (totalEaten * -1) - 1;
        }

        public Time getDuration() {
            return Duration.DURATION_UNKNOWN;
        }

        public Format getFormat() {
            return this.format;
        }

        public Time mapFrameToTime(int frameNumber) {
            return TIME_UNKNOWN;
        }

        public int mapTimeToFrame(Time t) {
            return Integer.MAX_VALUE;
        }

        private boolean parseProperty(String line, Properties properties) {
            int index = line.indexOf(58);
            if (index < 0) {
                return false;
            }
            String key = line.substring(0, index).trim();
            properties.setProperty(key.toUpperCase(), line.substring(index + 1).trim());
            return true;
        }

        private void pushback(byte[] bytes, int len) {
            if (this.pushbackBufferLen == 0) {
                this.pushbackBuffer = bytes;
                this.pushbackBufferLen = len;
                this.pushbackBufferOffset = 0;
                return;
            }
            byte[] newPushbackBuffer = new byte[(this.pushbackBufferLen + len)];
            System.arraycopy(this.pushbackBuffer, 0, newPushbackBuffer, 0, this.pushbackBufferLen);
            System.arraycopy(bytes, 0, newPushbackBuffer, this.pushbackBufferLen, len);
            this.pushbackBuffer = newPushbackBuffer;
            this.pushbackBufferLen += len;
            this.pushbackBufferOffset = 0;
        }

        private int read(byte[] buffer, int offset, int length) throws IOException {
            if (this.pushbackBufferLen <= 0) {
                return this.stream.read(buffer, offset, length);
            }
            int lenToCopy = length < this.pushbackBufferLen ? length : this.pushbackBufferLen;
            System.arraycopy(this.pushbackBuffer, this.pushbackBufferOffset, buffer, offset, lenToCopy);
            this.pushbackBufferLen -= lenToCopy;
            this.pushbackBufferOffset += lenToCopy;
            return lenToCopy;
        }

        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void readFrame(javax.media.Buffer r18) {
            /*
            r17 = this;
        L_0x0000:
            r13 = 255; // 0xff float:3.57E-43 double:1.26E-321;
            r0 = r17;
            r8 = r0.readLine(r13);	 Catch:{ IOException -> 0x004d }
            if (r8 != 0) goto L_0x0017;
        L_0x000a:
            r13 = 1;
            r0 = r18;
            r0.setEOM(r13);	 Catch:{ IOException -> 0x004d }
            r13 = 0;
            r0 = r18;
            r0.setLength(r13);	 Catch:{ IOException -> 0x004d }
        L_0x0016:
            return;
        L_0x0017:
            r13 = r8.trim();	 Catch:{ IOException -> 0x004d }
            r14 = "";
            r13 = r13.equals(r14);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0000;
        L_0x0023:
            r0 = r17;
            r13 = r0.boundary;	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0054;
        L_0x0029:
            r13 = r8.trim();	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r0.boundary = r13;	 Catch:{ IOException -> 0x004d }
        L_0x0031:
            r9 = new java.util.Properties;	 Catch:{ IOException -> 0x004d }
            r9.<init>();	 Catch:{ IOException -> 0x004d }
        L_0x0036:
            r13 = 255; // 0xff float:3.57E-43 double:1.26E-321;
            r0 = r17;
            r8 = r0.readLine(r13);	 Catch:{ IOException -> 0x004d }
            if (r8 != 0) goto L_0x00f7;
        L_0x0040:
            r13 = 1;
            r0 = r18;
            r0.setEOM(r13);	 Catch:{ IOException -> 0x004d }
            r13 = 0;
            r0 = r18;
            r0.setLength(r13);	 Catch:{ IOException -> 0x004d }
            goto L_0x0016;
        L_0x004d:
            r6 = move-exception;
            r13 = new java.lang.RuntimeException;
            r13.<init>(r6);
            throw r13;
        L_0x0054:
            r13 = r8.trim();	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r14 = r0.boundary;	 Catch:{ IOException -> 0x004d }
            r13 = r13.equals(r14);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0031;
        L_0x0062:
            r13 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.logger;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Expected boundary (frame ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r15 = r0.framesRead;	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r15 = "): ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r15 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.toPrintable(r8);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.warning(r14);	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r13 = r0.boundary;	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r7 = r0.eatUntil(r13);	 Catch:{ IOException -> 0x004d }
            r14 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.logger;	 Catch:{ IOException -> 0x004d }
            r13 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r13.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Ignored bytes (eom after=";
            r15 = r13.append(r15);	 Catch:{ IOException -> 0x004d }
            if (r7 >= 0) goto L_0x00d5;
        L_0x00a9:
            r13 = 1;
        L_0x00aa:
            r13 = r15.append(r13);	 Catch:{ IOException -> 0x004d }
            r15 = "): ";
            r15 = r13.append(r15);	 Catch:{ IOException -> 0x004d }
            if (r7 >= 0) goto L_0x00d7;
        L_0x00b6:
            r13 = r7 * -1;
            r13 = r13 + -1;
        L_0x00ba:
            r13 = r15.append(r13);	 Catch:{ IOException -> 0x004d }
            r13 = r13.toString();	 Catch:{ IOException -> 0x004d }
            r14.info(r13);	 Catch:{ IOException -> 0x004d }
            if (r7 >= 0) goto L_0x00d9;
        L_0x00c7:
            r13 = 1;
            r0 = r18;
            r0.setEOM(r13);	 Catch:{ IOException -> 0x004d }
            r13 = 0;
            r0 = r18;
            r0.setLength(r13);	 Catch:{ IOException -> 0x004d }
            goto L_0x0016;
        L_0x00d5:
            r13 = 0;
            goto L_0x00aa;
        L_0x00d7:
            r13 = r7;
            goto L_0x00ba;
        L_0x00d9:
            r13 = 255; // 0xff float:3.57E-43 double:1.26E-321;
            r0 = r17;
            r8 = r0.readLine(r13);	 Catch:{ IOException -> 0x004d }
            r13 = r8.trim();	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r14 = r0.boundary;	 Catch:{ IOException -> 0x004d }
            r13 = r13.equals(r14);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0031;
        L_0x00ef:
            r13 = new java.lang.RuntimeException;	 Catch:{ IOException -> 0x004d }
            r14 = "No boundary found after eatUntil(boundary)";
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x00f7:
            r13 = r8.trim();	 Catch:{ IOException -> 0x004d }
            r14 = "";
            r13 = r13.equals(r14);	 Catch:{ IOException -> 0x004d }
            if (r13 == 0) goto L_0x0131;
        L_0x0103:
            r13 = "Content-Type";
            r13 = r13.toUpperCase();	 Catch:{ IOException -> 0x004d }
            r4 = r9.getProperty(r13);	 Catch:{ IOException -> 0x004d }
            if (r4 != 0) goto L_0x0156;
        L_0x010f:
            r13 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.logger;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Header properties: ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r9);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.warning(r14);	 Catch:{ IOException -> 0x004d }
            r13 = new java.io.IOException;	 Catch:{ IOException -> 0x004d }
            r14 = "Expected Content-Type in header";
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x0131:
            r0 = r17;
            r13 = r0.parseProperty(r8, r9);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0036;
        L_0x0139:
            r13 = new java.io.IOException;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Expected property: ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r15 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.toPrintable(r8);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x0156:
            r13 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.isSupportedFrameContentType(r4);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x0175;
        L_0x015c:
            r13 = new java.io.IOException;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Unsupported Content-Type: ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r4);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x0175:
            r0 = r17;
            r13 = r0.frameContentType;	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x01ba;
        L_0x017b:
            r0 = r17;
            r0.frameContentType = r4;	 Catch:{ IOException -> 0x004d }
        L_0x017f:
            r13 = "Content-Length";
            r13 = r13.toUpperCase();	 Catch:{ IOException -> 0x004d }
            r3 = r9.getProperty(r13);	 Catch:{ IOException -> 0x004d }
            if (r3 == 0) goto L_0x0205;
        L_0x018b:
            r2 = java.lang.Integer.parseInt(r3);	 Catch:{ NumberFormatException -> 0x01eb }
            r0 = r17;
            r5 = r0.readFully(r2);	 Catch:{ IOException -> 0x004d }
        L_0x0195:
            r13 = "X-FMJ-Timestamp";
            r13 = r13.toUpperCase();	 Catch:{ IOException -> 0x004d }
            r12 = r9.getProperty(r13);	 Catch:{ IOException -> 0x004d }
            if (r12 == 0) goto L_0x01aa;
        L_0x01a1:
            r10 = java.lang.Long.parseLong(r12);	 Catch:{ NumberFormatException -> 0x0210 }
            r0 = r18;
            r0.setTimeStamp(r10);	 Catch:{ NumberFormatException -> 0x0210 }
        L_0x01aa:
            if (r5 != 0) goto L_0x022f;
        L_0x01ac:
            r13 = 1;
            r0 = r18;
            r0.setEOM(r13);	 Catch:{ IOException -> 0x004d }
            r13 = 0;
            r0 = r18;
            r0.setLength(r13);	 Catch:{ IOException -> 0x004d }
            goto L_0x0016;
        L_0x01ba:
            r0 = r17;
            r13 = r0.frameContentType;	 Catch:{ IOException -> 0x004d }
            r13 = r4.equals(r13);	 Catch:{ IOException -> 0x004d }
            if (r13 != 0) goto L_0x017f;
        L_0x01c4:
            r13 = new java.io.IOException;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Content type changed during stream from ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r15 = r0.frameContentType;	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r15 = " to ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r4);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x01eb:
            r6 = move-exception;
            r13 = new java.io.IOException;	 Catch:{ IOException -> 0x004d }
            r14 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r14.<init>();	 Catch:{ IOException -> 0x004d }
            r15 = "Invalid content length: ";
            r14 = r14.append(r15);	 Catch:{ IOException -> 0x004d }
            r14 = r14.append(r3);	 Catch:{ IOException -> 0x004d }
            r14 = r14.toString();	 Catch:{ IOException -> 0x004d }
            r13.<init>(r14);	 Catch:{ IOException -> 0x004d }
            throw r13;	 Catch:{ IOException -> 0x004d }
        L_0x0205:
            r0 = r17;
            r13 = r0.boundary;	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r5 = r0.readUntil(r13);	 Catch:{ IOException -> 0x004d }
            goto L_0x0195;
        L_0x0210:
            r6 = move-exception;
            r13 = net.sf.fmj.media.parser.MultipartMixedReplaceParser.logger;	 Catch:{ IOException -> 0x004d }
            r14 = java.util.logging.Level.WARNING;	 Catch:{ IOException -> 0x004d }
            r15 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x004d }
            r15.<init>();	 Catch:{ IOException -> 0x004d }
            r16 = "";
            r15 = r15.append(r16);	 Catch:{ IOException -> 0x004d }
            r15 = r15.append(r6);	 Catch:{ IOException -> 0x004d }
            r15 = r15.toString();	 Catch:{ IOException -> 0x004d }
            r13.log(r14, r15, r6);	 Catch:{ IOException -> 0x004d }
            goto L_0x01aa;
        L_0x022f:
            r0 = r18;
            r0.setData(r5);	 Catch:{ IOException -> 0x004d }
            r13 = 0;
            r0 = r18;
            r0.setOffset(r13);	 Catch:{ IOException -> 0x004d }
            r13 = r5.length;	 Catch:{ IOException -> 0x004d }
            r0 = r18;
            r0.setLength(r13);	 Catch:{ IOException -> 0x004d }
            r0 = r17;
            r13 = r0.framesRead;	 Catch:{ IOException -> 0x004d }
            r13 = r13 + 1;
            r0 = r17;
            r0.framesRead = r13;	 Catch:{ IOException -> 0x004d }
            goto L_0x0016;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.parser.MultipartMixedReplaceParser$VideoTrack.readFrame(javax.media.Buffer):void");
        }

        private byte[] readFully(int bytes) throws IOException {
            byte[] buffer = new byte[bytes];
            int offset = 0;
            int length = bytes;
            while (true) {
                int lenRead = read(buffer, offset, length);
                if (lenRead < 0) {
                    return null;
                }
                if (lenRead == length) {
                    return buffer;
                }
                length -= lenRead;
                offset += lenRead;
            }
        }

        private String readLine(int max) throws IOException {
            byte[] buffer = new byte[max];
            int offset = 0;
            while (offset < max) {
                if (read(buffer, offset, 1) < 0) {
                    return null;
                }
                if (buffer[offset] == (byte) 10) {
                    if (offset > 0 && buffer[offset - 1] == (byte) 13) {
                        offset--;
                    }
                    return new String(buffer, 0, offset);
                }
                offset++;
            }
            throw new MaxLengthExceededException("No newline found in " + max + " bytes");
        }

        private byte[] readUntil(String boundary) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] boundaryBytes = boundary.getBytes();
            byte[] matchBuffer = new byte[boundaryBytes.length];
            int matchOffset = 0;
            while (os.size() < 1000000) {
                if (read(matchBuffer, matchOffset, 1) < 0) {
                    return null;
                }
                if (matchBuffer[matchOffset] == boundaryBytes[matchOffset]) {
                    if (matchOffset == boundaryBytes.length - 1) {
                        pushback(matchBuffer, matchOffset + 1);
                        return os.toByteArray();
                    }
                    matchOffset++;
                } else if (matchOffset > 0) {
                    os.write(matchBuffer, 0, matchOffset + 1);
                    matchOffset = 0;
                } else {
                    os.write(matchBuffer, 0, 1);
                }
            }
            throw new IOException("No boundary found in 1000000 bytes.");
        }

        public long skipNanos(long nanos) throws IOException {
            return 0;
        }
    }

    /* access modifiers changed from: private|static */
    public static String toPrintable(String line) {
        return toPrintable(line, 32);
    }

    private static String toPrintable(String line, int max) {
        StringBuilder b = new StringBuilder();
        int i = 0;
        while (i < line.length() && i < max) {
            char c = line.charAt(i);
            if (c < ' ' || c > '~') {
                b.append('.');
            } else {
                b.append(c);
            }
            i++;
        }
        return b.toString();
    }

    /* access modifiers changed from: private|static|final */
    public static final boolean isSupportedFrameContentType(String contentType) {
        for (String supported : supportedFrameContentTypes) {
            if (supported.equals(contentType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        if (this.tracks != null) {
            for (int i = 0; i < this.tracks.length; i++) {
                if (this.tracks[i] != null) {
                    this.tracks[i].deallocate();
                    this.tracks[i] = null;
                }
            }
            this.tracks = null;
        }
        super.close();
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors() {
        return this.supportedInputContentDescriptors;
    }

    public Track[] getTracks() throws IOException, BadHeaderException {
        return this.tracks;
    }

    public boolean isPositionable() {
        return false;
    }

    public boolean isRandomAccess() {
        return super.isRandomAccess();
    }

    public void open() throws ResourceUnavailableException {
        try {
            this.source.start();
            PullSourceStream[] streams = this.source.getStreams();
            this.tracks = new PullSourceStreamTrack[streams.length];
            for (int i = 0; i < streams.length; i++) {
                this.tracks[i] = new VideoTrack(streams[i]);
            }
            super.open();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new ResourceUnavailableException("" + e);
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        String protocol = source.getLocator().getProtocol();
        if (source instanceof PullDataSource) {
            this.source = (PullDataSource) source;
            return;
        }
        throw new IncompatibleSourceException();
    }

    public void start() throws IOException {
    }
}
