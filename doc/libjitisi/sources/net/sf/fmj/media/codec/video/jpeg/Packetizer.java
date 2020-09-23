package net.sf.fmj.media.codec.video.jpeg;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.Owned;
import javax.media.control.FormatControl;
import javax.media.control.QualityControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractPacketizer;
import net.sf.fmj.media.util.BufferToImage;
import net.sf.fmj.utility.ArrayUtility;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Packetizer extends AbstractPacketizer {
    private static final int PACKET_SIZE = 1000;
    private static final int RTP_JPEG_RESTART = 64;
    private static final Logger logger = Logger.getLogger(Packetizer.class.getName());
    private BufferToImage bufferToImage;
    private int[] chromaQtable = RFC2035.jpeg_chroma_quantizer_normal;
    private VideoFormat currentFormat;
    private int currentQuality;
    private int dri = 0;
    private ImageWriter encoder;
    /* access modifiers changed from: private */
    public JPEGHuffmanTable[] huffmanACTables;
    /* access modifiers changed from: private */
    public JPEGHuffmanTable[] huffmanDCTables;
    /* access modifiers changed from: private */
    public Buffer imageBuffer;
    /* access modifiers changed from: private */
    public Graphics imageGraphics;
    int j = 0;
    private int[] lumaQtable = RFC2035.jpeg_luma_quantizer_normal;
    /* access modifiers changed from: private */
    public BufferedImage offscreenImage;
    private ByteArrayOutputStream os = new ByteArrayOutputStream();
    private MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(this.os);
    /* access modifiers changed from: private */
    public Format outputVideoFormat;
    /* access modifiers changed from: private */
    public JPEGImageWriteParam param;
    /* access modifiers changed from: private */
    public JPEGQTable[] qtable;
    /* access modifiers changed from: private */
    public int quality = 75;
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};
    private final Format[] supportedOutputFormats;
    private Buffer temporary = new Buffer();
    private byte type = (byte) 1;
    private byte typeSpecific = (byte) 0;

    private class FC implements FormatControl, Owned {
        private FC() {
        }

        public Component getControlComponent() {
            return null;
        }

        public Format getFormat() {
            return Packetizer.this.outputVideoFormat;
        }

        public Object getOwner() {
            return Packetizer.this;
        }

        public Format[] getSupportedFormats() {
            return null;
        }

        public boolean isEnabled() {
            return true;
        }

        public void setEnabled(boolean enabled) {
        }

        public Format setFormat(Format format) {
            Packetizer.this.outputVideoFormat = format;
            synchronized (Packetizer.this.imageBuffer) {
                Packetizer.this.offscreenImage = null;
                Packetizer.this.imageGraphics = null;
            }
            return Packetizer.this.outputVideoFormat;
        }
    }

    class JPEGQualityControl implements QualityControl, Owned {
        JPEGQualityControl() {
        }

        public Component getControlComponent() {
            return null;
        }

        public Object getOwner() {
            return Packetizer.this;
        }

        public float getPreferredQuality() {
            return 0.75f;
        }

        public float getQuality() {
            return ((float) Packetizer.this.quality) / 100.0f;
        }

        public boolean isTemporalSpatialTradeoffSupported() {
            return true;
        }

        public float setQuality(float newQuality) {
            Packetizer.this.quality = Math.round(100.0f * newQuality);
            if (Packetizer.this.quality > 99) {
                Packetizer.this.quality = 99;
            } else if (Packetizer.this.quality < 1) {
                Packetizer.this.quality = 1;
            }
            Packetizer.this.qtable = Packetizer.this.createQTable(Packetizer.this.quality);
            Packetizer.this.param.setEncodeTables(Packetizer.this.qtable, Packetizer.this.huffmanDCTables, Packetizer.this.huffmanACTables);
            return (float) Packetizer.this.quality;
        }
    }

    private static Node createDri(Node n, int interval) {
        IIOMetadataNode dri = new IIOMetadataNode("dri");
        dri.setAttribute("interval", Integer.toString(interval));
        NodeList nl = n.getChildNodes();
        nl.item(1).insertBefore(dri, nl.item(1).getFirstChild());
        return n;
    }

    private static Node find(Node n, String s) {
        String[] names = s.split("/");
        String[] current = names[0].split(":");
        if (names == null) {
            return null;
        }
        if (names.length == 1) {
            return n;
        }
        String newS = "";
        int i = 1;
        while (i < names.length) {
            newS = newS + names[i] + (i == names.length + -1 ? "" : "/");
            i++;
        }
        if (n.getNodeName().equalsIgnoreCase(current[0]) && (current.length <= 1 || current[1].equalsIgnoreCase(n.getNodeValue()))) {
            return find(n, newS);
        }
        for (i = 0; i < n.getChildNodes().getLength(); i++) {
            Node child = n.getChildNodes().item(i);
            if (child.getNodeName().equalsIgnoreCase(names[0]) && (current.length <= 1 || current[1].equalsIgnoreCase(n.getNodeValue()))) {
                return find(child, newS);
            }
        }
        return null;
    }

    private static void outputMetadata(Node node, String delimiter) {
        System.out.println(delimiter + node.getNodeName());
        delimiter = "  " + delimiter;
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.hasChildNodes()) {
                outputMetadata(n, delimiter);
            }
            System.out.println(delimiter + n.getNodeName());
            if (list.item(i).hasAttributes()) {
                NamedNodeMap nnm = list.item(i).getAttributes();
                String ndel = "  " + delimiter + "-A:";
                for (int j = 0; j < nnm.getLength(); j++) {
                    System.out.println(ndel + nnm.item(j).getNodeName() + ":" + nnm.item(j).getNodeValue());
                }
            }
        }
    }

    private static Node setSamplingFactor(Node n, int hSampleFactor, int vSampleFactor) {
        Node lookingfor = find(n.getChildNodes().item(1), "markerSequence/sof/componentSpec/HsamplingFactor:1");
        lookingfor.getAttributes().getNamedItem("HsamplingFactor").setNodeValue(Integer.toString(hSampleFactor));
        lookingfor.getAttributes().getNamedItem("VsamplingFactor").setNodeValue(Integer.toString(vSampleFactor));
        return n;
    }

    public Packetizer() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f);
        this.supportedOutputFormats = formatArr;
        this.imageBuffer = new Buffer();
        this.inputFormats = this.supportedInputFormats;
        addControl(new JPEGQualityControl());
        addControl(new FC());
    }

    public void close() {
        try {
            this.out.close();
            this.os.close();
            this.encoder.dispose();
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "close", e.getCause());
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

    /* access modifiers changed from: private */
    public JPEGQTable[] createQTable(int q) {
        byte[] lumQ = new byte[64];
        byte[] chmQ = new byte[64];
        RFC2035.MakeTables(q, lumQ, chmQ, RFC2035.jpeg_luma_quantizer_normal, RFC2035.jpeg_chroma_quantizer_normal);
        JPEGQTable qtable_luma = new JPEGQTable(ArrayUtility.byteArrayToIntArray(lumQ));
        JPEGQTable qtable_chroma = new JPEGQTable(ArrayUtility.byteArrayToIntArray(chmQ));
        return new JPEGQTable[]{qtable_luma, qtable_chroma};
    }

    /* access modifiers changed from: protected */
    public int doBuildPacketHeader(Buffer inputBuffer, byte[] packetBuffer) {
        VideoFormat format = this.inputFormat;
        int width = format.getSize().width;
        int height = format.getSize().height;
        if (this.currentFormat != null) {
            width = this.currentFormat.getSize().width;
            height = this.currentFormat.getSize().height;
        }
        byte[] bytes = new JpegRTPHeader(this.typeSpecific, inputBuffer.getOffset(), this.type, (byte) this.currentQuality, (byte) (width / 8), (byte) (height / 8)).toBytes();
        System.arraycopy(bytes, 0, packetBuffer, 0, bytes.length);
        return 0 + bytes.length;
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

    public String getName() {
        return "JPEG/RTP Packetizer";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, inputCast.getSize(), -1, Format.byteArray, -1.0f)};
    }

    public void open() {
        setPacketSize(PACKET_SIZE);
        setDoNotSpanInputBuffers(true);
        this.temporary.setOffset(0);
        this.encoder = (ImageWriter) ImageIO.getImageWritersByFormatName("JPEG").next();
        this.param = new JPEGImageWriteParam(null);
        this.huffmanACTables = createACHuffmanTables();
        this.huffmanDCTables = createDCHuffmanTables();
        this.qtable = createQTable(this.quality);
        this.param.setEncodeTables(this.qtable, this.huffmanDCTables, this.huffmanACTables);
        try {
            this.encoder.setOutput(this.out);
            this.encoder.prepareWriteSequence(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int process(Buffer input, Buffer output) {
        if (!checkInputBuffer(input)) {
            return 1;
        }
        if (isEOM(input)) {
            propagateEOM(output);
            return 0;
        }
        BufferedImage image = (BufferedImage) this.bufferToImage.createImage(input);
        try {
            if (this.temporary.getLength() == 0) {
                this.currentQuality = this.quality;
                this.currentFormat = (VideoFormat) this.outputVideoFormat;
                if (this.currentFormat != null && (input.getFormat() instanceof RGBFormat)) {
                    int width = this.currentFormat.getSize().width;
                    int height = this.currentFormat.getSize().height;
                    synchronized (this.imageBuffer) {
                        if (this.offscreenImage == null) {
                            Object tempData = new byte[((width * height) * 3)];
                            Format videoFormat = (RGBFormat) input.getFormat();
                            Format vf = (RGBFormat) new RGBFormat(new Dimension(width, height), -1, null, -1.0f, -1, -1, -1, -1, -1, videoFormat.getPixelStride() * width, -1, -1).intersects(videoFormat);
                            this.imageBuffer.setData(tempData);
                            this.imageBuffer.setLength(tempData.length);
                            this.imageBuffer.setFormat(vf);
                            this.offscreenImage = (BufferedImage) this.bufferToImage.createImage(this.imageBuffer);
                            this.imageGraphics = this.offscreenImage.getGraphics();
                        }
                    }
                    this.imageGraphics.drawImage(image, 0, 0, width, height, null);
                    image = this.offscreenImage;
                }
                this.os.reset();
                this.param.setCompressionMode(2);
                this.param.setCompressionQuality(((float) this.currentQuality) / 100.0f);
                this.encoder.write(null, new IIOImage(image, null, null), this.param);
                byte[] ba = JpegStripper.removeHeaders(this.os.toByteArray());
                this.temporary.setData(ba);
                this.temporary.setLength(ba.length);
            }
            int result = super.process(this.temporary, output);
            if (result != 0) {
                return result;
            }
            this.temporary.setOffset(0);
            output.setFlags(output.getFlags() | 2048);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            output.setDiscard(true);
            output.setLength(0);
            return 1;
        }
    }

    public Format setInputFormat(Format format) {
        if (((VideoFormat) format).getSize() == null) {
            return null;
        }
        this.bufferToImage = new BufferToImage((VideoFormat) format);
        return super.setInputFormat(format);
    }

    private void setType(int typeValue) {
        this.type = (byte) ((this.dri != 0 ? 64 : 0) | typeValue);
    }
}
