package net.sf.fmj.media.renderer.video;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayInputStream;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.stream.ImageInputStream;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.Owned;
import javax.media.control.FrameRateControl;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;
import net.sf.fmj.media.AbstractVideoRenderer;
import net.sf.fmj.media.codec.video.jpeg.RFC2035;
import net.sf.fmj.utility.ArrayUtility;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class JPEGRTPRenderer extends AbstractVideoRenderer implements VideoRenderer {
    private static final boolean TRACE = true;
    private static final Logger logger = LoggerSingleton.logger;
    private JVideoComponent component = new JVideoComponent();
    private JPEGRTPFrame currentFrame;
    /* access modifiers changed from: private */
    public ImageReader decoder;
    /* access modifiers changed from: private */
    public float frameRate = -1.0f;
    private int framesProcessed;
    private JPEGHuffmanTable[] huffmanACTables;
    private JPEGHuffmanTable[] huffmanDCTables;
    /* access modifiers changed from: private */
    public BufferedImage itsImage;
    private long lastTimestamp;
    private JPEGImageReadParam param;
    private JPEGQTable[] qtable;
    /* access modifiers changed from: private */
    public int quality = -1;
    private final Format[] supportedInputFormats = new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f)};

    private class JPEGRTPFrame {
        public int count;
        public int dataLength;
        public JPEGRTPFrame firstItem;
        public long fragmentOffset;
        public boolean hasRTPMarker;
        public Buffer itemData;
        byte[] jpegHeader = new byte[]{(byte) -1, (byte) -40, (byte) -1, (byte) -32, (byte) 0, (byte) 16, (byte) 74, (byte) 70, (byte) 73, (byte) 70, (byte) 0, (byte) 1, (byte) 2, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) -1, (byte) -64, (byte) 0, (byte) 17, (byte) 8, (byte) 0, (byte) -112, (byte) 0, (byte) -80, (byte) 3, (byte) 1, (byte) 34, (byte) 0, (byte) 2, (byte) 17, (byte) 1, (byte) 3, (byte) 17, (byte) 1, (byte) -1, (byte) -38, (byte) 0, (byte) 12, (byte) 3, (byte) 1, (byte) 0, (byte) 2, (byte) 17, (byte) 3, (byte) 17, (byte) 0, (byte) 63, (byte) 0};
        public JPEGRTPFrame nextItem;
        public long timestamp;

        public JPEGRTPFrame(Buffer buffer) {
            this.itemData = buffer;
        }

        public JPEGRTPFrame(long timestamp) {
            this.timestamp = timestamp;
        }

        public void add(Buffer buffer) {
            JPEGRTPFrame aNewItem = new JPEGRTPFrame((Buffer) buffer.clone());
            if ((buffer.getFlags() & 2048) > 0) {
                this.hasRTPMarker = true;
            }
            this.count++;
            if (this.firstItem == null) {
                this.firstItem = aNewItem;
            } else {
                JPEGRTPFrame aItem = this.firstItem;
                while (aItem.nextItem != null) {
                    aItem = aItem.nextItem;
                }
                aItem.nextItem = aNewItem;
            }
            byte[] data = (byte[]) buffer.getData();
            aNewItem.fragmentOffset = 0;
            for (int i = 0; i < 3; i++) {
                aNewItem.fragmentOffset <<= 8;
                aNewItem.fragmentOffset += (long) (data[i + 13] & UnsignedUtils.MAX_UBYTE);
            }
        }

        public void clear(long timestamp) {
            this.firstItem = null;
            this.timestamp = timestamp;
            this.hasRTPMarker = false;
            this.count = 0;
            this.dataLength = 0;
        }

        public byte[] getData() {
            byte[] frame = new byte[((this.jpegHeader.length + this.dataLength) + 2)];
            System.arraycopy(this.jpegHeader, 0, frame, 0, this.jpegHeader.length);
            JPEGRTPFrame aItem = this.firstItem;
            long expectedFragmentOffset = 0;
            int offset = this.jpegHeader.length;
            while (aItem != null) {
                aItem = this.firstItem;
                while (aItem != null && aItem.fragmentOffset != expectedFragmentOffset) {
                    aItem = aItem.nextItem;
                }
                if (aItem != null) {
                    int len = aItem.itemData.getLength() - 8;
                    System.arraycopy((byte[]) aItem.itemData.getData(), aItem.itemData.getOffset() + 8, frame, offset, len);
                    offset += len;
                    expectedFragmentOffset += (long) len;
                }
            }
            byte[] packetData = (byte[]) this.firstItem.itemData.getData();
            int width = packetData[this.firstItem.itemData.getOffset() + 6] << 3;
            int height = packetData[this.firstItem.itemData.getOffset() + 7] << 3;
            frame[25] = (byte) ((height >> 8) & UnsignedUtils.MAX_UBYTE);
            frame[26] = (byte) (height & UnsignedUtils.MAX_UBYTE);
            frame[27] = (byte) ((width >> 8) & UnsignedUtils.MAX_UBYTE);
            frame[28] = (byte) (width & UnsignedUtils.MAX_UBYTE);
            frame[frame.length - 2] = (byte) -1;
            frame[frame.length - 1] = (byte) -39;
            int q = packetData[this.firstItem.itemData.getOffset() + 5];
            if (JPEGRTPRenderer.this.decoder == null) {
                JPEGRTPRenderer.this.initDecoder(q);
            }
            if (!(JPEGRTPRenderer.this.quality == -1 || q == JPEGRTPRenderer.this.quality)) {
                JPEGRTPRenderer.this.initDecoder(q);
            }
            JPEGRTPRenderer.this.quality = q;
            if (JPEGRTPRenderer.this.itsImage == null) {
                JPEGRTPRenderer.this.itsImage = new BufferedImage(width, height, 1);
            }
            return frame;
        }

        public boolean isComplete() {
            if (!this.hasRTPMarker) {
                return false;
            }
            JPEGRTPFrame aItem = this.firstItem;
            long expectedFragmentOffset = 0;
            this.dataLength = 0;
            while (aItem != null) {
                aItem = this.firstItem;
                while (aItem != null && aItem.fragmentOffset != expectedFragmentOffset) {
                    aItem = aItem.nextItem;
                }
                if (aItem != null) {
                    int len = aItem.itemData.getLength() - 8;
                    this.dataLength += len;
                    if ((aItem.itemData.getFlags() & 2048) > 0) {
                        return true;
                    }
                    expectedFragmentOffset += (long) len;
                }
            }
            return false;
        }
    }

    private class VideoFrameRateControl implements FrameRateControl, Owned {
        private VideoFrameRateControl() {
        }

        public Component getControlComponent() {
            return null;
        }

        public float getFrameRate() {
            return JPEGRTPRenderer.this.frameRate;
        }

        public float getMaxSupportedFrameRate() {
            return -1.0f;
        }

        public Object getOwner() {
            return JPEGRTPRenderer.this;
        }

        public float getPreferredFrameRate() {
            return -1.0f;
        }

        public float setFrameRate(float newFrameRate) {
            return -1.0f;
        }
    }

    public JPEGRTPRenderer() {
        addControl(this);
        addControl(new VideoFrameRateControl());
    }

    public void close() {
        if (this.decoder != null) {
            this.decoder.dispose();
        }
    }

    private JPEGHuffmanTable[] createACHuffmanTables() {
        JPEGHuffmanTable acChm = new JPEGHuffmanTable(RFC2035.chm_ac_codelens, RFC2035.chm_ac_symbols);
        return new JPEGHuffmanTable[]{new JPEGHuffmanTable(RFC2035.lum_ac_codelens, RFC2035.lum_ac_symbols), acChm};
    }

    private JPEGHuffmanTable[] createDCHuffmanTables() {
        JPEGHuffmanTable dcChm = new JPEGHuffmanTable(RFC2035.chm_dc_codelens, RFC2035.chm_dc_symbols);
        return new JPEGHuffmanTable[]{new JPEGHuffmanTable(RFC2035.lum_dc_codelens, RFC2035.lum_dc_symbols), dcChm};
    }

    private JPEGQTable[] createQTable(int q) {
        byte[] lumQ = new byte[64];
        byte[] chmQ = new byte[64];
        RFC2035.MakeTables(q, lumQ, chmQ, RFC2035.jpeg_luma_quantizer_normal, RFC2035.jpeg_chroma_quantizer_normal);
        JPEGQTable qtable_luma = new JPEGQTable(ArrayUtility.byteArrayToIntArray(lumQ));
        JPEGQTable qtable_chroma = new JPEGQTable(ArrayUtility.byteArrayToIntArray(chmQ));
        return new JPEGQTable[]{qtable_luma, qtable_chroma};
    }

    public int doProcess(Buffer buffer) {
        long timestamp = buffer.getTimeStamp();
        if (this.currentFrame == null) {
            this.currentFrame = new JPEGRTPFrame(timestamp);
        }
        if (timestamp < this.currentFrame.timestamp) {
            logger.fine("JPEGRTPRenderer: dropping packet ts=" + timestamp);
        } else if (timestamp > this.currentFrame.timestamp) {
            logger.fine("JPEGRTPRenderer: dropping current frame ts=" + this.currentFrame.timestamp + ", got new packet ts=" + timestamp);
            this.currentFrame.clear(timestamp);
            this.currentFrame.add(buffer);
        } else {
            this.currentFrame.add(buffer);
        }
        if (this.currentFrame.isComplete()) {
            byte[] data = this.currentFrame.getData();
            this.currentFrame = null;
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ImageInputStream stream = ImageIO.createImageInputStream(in);
                this.decoder.setInput(stream, false, false);
                this.param.setDestination(this.itsImage);
                this.decoder.read(0, this.param);
                this.component.setImage(this.itsImage);
                stream.close();
                in.close();
                long currentTimestamp = System.nanoTime();
                if (-1 == this.lastTimestamp) {
                    this.lastTimestamp = currentTimestamp;
                }
                this.framesProcessed++;
                if (currentTimestamp - this.lastTimestamp > 1000000000) {
                    this.frameRate = ((float) this.framesProcessed) * (1000.0f / (((float) (currentTimestamp - this.lastTimestamp)) / 1000000.0f));
                    this.framesProcessed = 0;
                    this.lastTimestamp = currentTimestamp;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    private void dump(byte[] data, int length) {
        int index = 0;
        while (index < length) {
            String aString = "";
            int i = 0;
            while (true) {
                int index2 = index;
                if (i >= 16) {
                    index = index2;
                    break;
                }
                index = index2 + 1;
                String s = Integer.toHexString(data[index2] & UnsignedUtils.MAX_UBYTE);
                StringBuilder append = new StringBuilder().append(aString);
                if (s.length() < 2) {
                    s = "0" + s;
                }
                aString = append.append(s).toString() + " ";
                if (index >= length) {
                    break;
                }
                i++;
            }
            System.out.println(aString);
        }
        System.out.println(" ");
    }

    public Component getComponent() {
        return this.component;
    }

    public String getName() {
        return "JPEG/RTP Renderer";
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    /* access modifiers changed from: private */
    public void initDecoder(int q) {
        if (this.decoder != null) {
            this.decoder.dispose();
        }
        this.decoder = (ImageReader) ImageIO.getImageReadersByFormatName("JPEG").next();
        this.param = new JPEGImageReadParam();
        this.huffmanACTables = createACHuffmanTables();
        this.huffmanDCTables = createDCHuffmanTables();
        this.qtable = createQTable(q);
        this.param.setDecodeTables(this.qtable, this.huffmanDCTables, this.huffmanACTables);
    }

    public Format setInputFormat(Format format) {
        VideoFormat chosenFormat = (VideoFormat) super.setInputFormat(format);
        if (chosenFormat != null) {
            getComponent().setPreferredSize(chosenFormat.getSize());
        }
        return chosenFormat;
    }
}
