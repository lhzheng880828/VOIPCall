package net.sf.fmj.media.codec.audio.gsm;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractCodec;

public class Encoder extends AbstractCodec {
    private static final int GSM_BYTES = 33;
    private static final int PCM_BYTES = 320;
    private static final boolean TRACE = false;
    private Buffer innerBuffer;
    byte[] innerContent;
    private int innerDataLength;
    private int inputDataLength;
    protected Format[] outputFormats;

    public Encoder() {
        this.innerBuffer = new Buffer();
        this.innerDataLength = 0;
        this.inputDataLength = 0;
        this.outputFormats = new Format[]{new AudioFormat("gsm", 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray)};
        this.inputFormats = new Format[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 1, 1, -1, -1.0d, Format.byteArray)};
    }

    public void close() {
    }

    public String getName() {
        return "GSM Encoder";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            AudioFormat result = new AudioFormat("gsm", inputCast.getSampleRate(), 8, 1, inputCast.getEndian(), 1, 264, inputCast.getFrameRate(), Format.byteArray);
            return new Format[]{result};
        }
        return new Format[]{null};
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

    public void open() {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        boolean bigEndian = true;
        byte[] inputContent = new byte[inputBuffer.getLength()];
        System.arraycopy(inputBuffer.getData(), inputBuffer.getOffset(), inputContent, 0, inputContent.length);
        byte[] mergedContent = mergeArrays((byte[]) this.innerBuffer.getData(), inputContent);
        this.innerBuffer.setData(mergedContent);
        this.innerBuffer.setLength(mergedContent.length);
        this.innerDataLength = this.innerBuffer.getLength();
        this.inputDataLength = inputBuffer.getLength();
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return false;
        }
        boolean result;
        byte[] outputBufferData = (byte[]) outputBuffer.getData();
        if (outputBufferData == null || outputBufferData.length < (this.innerDataLength * GSM_BYTES) / PCM_BYTES) {
            outputBuffer.setData(new byte[((this.innerDataLength / PCM_BYTES) * GSM_BYTES)]);
        }
        if (this.innerDataLength < PCM_BYTES) {
            result = true;
            System.out.println("Not filled");
        } else {
            if (((AudioFormat) this.outputFormat).getEndian() != 1) {
                bigEndian = false;
            }
            outputBufferData = new byte[((this.innerDataLength / PCM_BYTES) * GSM_BYTES)];
            outputBuffer.setData(outputBufferData);
            outputBuffer.setLength((this.innerDataLength / PCM_BYTES) * GSM_BYTES);
            GSMEncoderUtil.gsmEncode(bigEndian, (byte[]) this.innerBuffer.getData(), this.innerBuffer.getOffset(), this.innerDataLength, outputBufferData);
            outputBuffer.setFormat(this.outputFormat);
            outputBuffer.setData(outputBufferData);
            result = false;
            byte[] temp = new byte[(this.innerDataLength - ((this.innerDataLength / PCM_BYTES) * PCM_BYTES))];
            this.innerContent = (byte[]) this.innerBuffer.getData();
            System.arraycopy(this.innerContent, (this.innerDataLength / PCM_BYTES) * PCM_BYTES, temp, 0, temp.length);
            outputBuffer.setOffset(0);
            this.innerBuffer.setLength(temp.length);
            this.innerBuffer.setData(temp);
        }
        return result;
    }

    public Format setInputFormat(Format f) {
        return super.setInputFormat(f);
    }

    public Format setOutputFormat(Format f) {
        return super.setOutputFormat(f);
    }
}
