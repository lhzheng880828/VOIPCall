package org.jitsi.impl.neomedia.codec.audio.gsm;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.AudioFormatCompleter;

public class Decoder extends AbstractCodec {
    private static final int GSM_BYTES = 33;
    private static final int PCM_BYTES = 320;
    private static final boolean TRACE = false;
    private Buffer innerBuffer = new Buffer();
    byte[] innerContent;
    private int innerDataLength = 0;
    protected Format[] outputFormats;

    public String getName() {
        return "GSM Decoder";
    }

    public Decoder() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, -1, 1, -1, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat("gsm", 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public Format setOutputFormat(Format format) {
        if (format instanceof AudioFormat) {
            return super.setOutputFormat(AudioFormatCompleter.complete((AudioFormat) format));
        }
        return null;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals("gsm") && ((inputCast.getSampleSizeInBits() == 8 || inputCast.getSampleSizeInBits() == -1) && ((inputCast.getChannels() == 1 || inputCast.getChannels() == -1) && ((inputCast.getSigned() == 1 || inputCast.getSigned() == -1) && ((inputCast.getFrameSizeInBits() == 264 || inputCast.getFrameSizeInBits() == -1) && (inputCast.getDataType() == null || inputCast.getDataType() == Format.byteArray)))))) {
                AudioFormat result = new AudioFormat(AudioFormat.LINEAR, inputCast.getSampleRate(), 16, 1, inputCast.getEndian(), 1, 16, -1.0d, Format.byteArray);
                return new Format[]{result};
            }
            return new Format[]{null};
        }
        return new Format[]{null};
    }

    public void open() {
    }

    public void close() {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        boolean bigEndian = true;
        byte[] inputContent = new byte[inputBuffer.getLength()];
        System.arraycopy(inputBuffer.getData(), inputBuffer.getOffset(), inputContent, 0, inputContent.length);
        byte[] mergedContent = mergeArrays((byte[]) this.innerBuffer.getData(), inputContent);
        this.innerBuffer.setData(mergedContent);
        this.innerBuffer.setLength(mergedContent.length);
        this.innerDataLength = this.innerBuffer.getLength();
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return false;
        } else if (!checkInputBuffer(inputBuffer)) {
            return 1;
        } else {
            if (isEOM(inputBuffer)) {
                propagateEOM(outputBuffer);
                return false;
            }
            boolean result;
            byte[] outputBufferData = (byte[]) outputBuffer.getData();
            if (outputBufferData == null || outputBufferData.length < (this.innerBuffer.getLength() * PCM_BYTES) / GSM_BYTES) {
                outputBuffer.setData(new byte[((this.innerBuffer.getLength() / GSM_BYTES) * PCM_BYTES)]);
            }
            if (this.innerBuffer.getLength() < GSM_BYTES) {
                result = true;
            } else {
                if (((AudioFormat) this.outputFormat).getEndian() != 1) {
                    bigEndian = false;
                }
                outputBufferData = new byte[((this.innerBuffer.getLength() / GSM_BYTES) * PCM_BYTES)];
                outputBuffer.setData(outputBufferData);
                outputBuffer.setLength((this.innerBuffer.getLength() / GSM_BYTES) * PCM_BYTES);
                GSMDecoderUtil.gsmDecode(bigEndian, (byte[]) this.innerBuffer.getData(), inputBuffer.getOffset(), this.innerBuffer.getLength(), outputBufferData);
                outputBuffer.setFormat(this.outputFormat);
                result = false;
                byte[] temp = new byte[(this.innerDataLength - ((this.innerDataLength / GSM_BYTES) * GSM_BYTES))];
                this.innerContent = (byte[]) this.innerBuffer.getData();
                System.arraycopy(this.innerContent, (this.innerDataLength / GSM_BYTES) * GSM_BYTES, temp, 0, temp.length);
                outputBuffer.setOffset(0);
                this.innerBuffer.setLength(temp.length);
                this.innerBuffer.setData(temp);
            }
            return result;
        }
    }

    private byte[] mergeArrays(byte[] arr1, byte[] arr2) {
        if (arr1 == null) {
            return arr2;
        }
        if (arr2 == null) {
            return arr1;
        }
        byte[] merged = new byte[(arr1.length + arr2.length)];
        System.arraycopy(arr1, 0, merged, 0, arr1.length);
        System.arraycopy(arr2, 0, merged, arr1.length, arr2.length);
        return merged;
    }

    public Format setInputFormat(Format arg0) {
        return super.setInputFormat(arg0);
    }
}
