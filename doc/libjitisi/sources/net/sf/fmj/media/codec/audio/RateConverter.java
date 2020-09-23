package net.sf.fmj.media.codec.audio;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class RateConverter extends AudioCodec {
    public RateConverter() {
        this.inputFormats = new Format[]{new AudioFormat(AudioFormat.LINEAR)};
    }

    private int doByteCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio) {
        byte[] inData = (byte[]) in.getData();
        byte[] outData = validateByteArraySize(out, outLen);
        out.setData(outData);
        out.setFormat(this.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = Pa.LATENCY_UNSPECIFIED;
        int inPtr = inOffset;
        int outPtr = 0;
        int inEnd = inOffset + inLen;
        if (ratio == 1.0d) {
            System.arraycopy(inData, inOffset, outData, 0, inLen);
            return 0;
        }
        int i;
        int outPtr2;
        if (ratio > 1.0d) {
            while (inPtr <= inEnd - step && outPtr <= outLen - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    i++;
                }
                sum += ratio;
                while (sum > Pa.LATENCY_UNSPECIFIED) {
                    inPtr += step;
                    sum -= 1.0d;
                }
                outPtr = outPtr2;
            }
        } else {
            byte[] d = new byte[step];
            while (inPtr <= inEnd - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                    i++;
                }
                outPtr = outPtr2;
                while (true) {
                    sum += ratio;
                    if (sum >= 1.0d) {
                        break;
                    } else if (outPtr <= outLen - step) {
                        i = 0;
                        while (true) {
                            outPtr2 = outPtr;
                            if (i >= step) {
                                break;
                            }
                            outPtr = outPtr2 + 1;
                            outData[outPtr2] = d[i];
                            i++;
                        }
                        outPtr = outPtr2;
                    }
                }
                sum -= 1.0d;
                inPtr += step;
            }
        }
        return 0;
    }

    private int doIntCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio) {
        int[] inData = (int[]) in.getData();
        int[] outData = validateIntArraySize(out, outLen);
        out.setData(outData);
        out.setFormat(this.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = Pa.LATENCY_UNSPECIFIED;
        int inPtr = inOffset;
        int outPtr = 0;
        int inEnd = inOffset + inLen;
        if (ratio == 1.0d) {
            System.arraycopy(inData, inOffset, outData, 0, inLen);
            return 0;
        }
        int i;
        int outPtr2;
        if (ratio > 1.0d) {
            while (inPtr <= inEnd - step && outPtr <= outLen - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    i++;
                }
                sum += ratio;
                while (sum > Pa.LATENCY_UNSPECIFIED) {
                    inPtr += step;
                    sum -= 1.0d;
                }
                outPtr = outPtr2;
            }
        } else {
            int[] d = new int[step];
            while (inPtr <= inEnd - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                    i++;
                }
                outPtr = outPtr2;
                while (true) {
                    sum += ratio;
                    if (sum >= 1.0d) {
                        break;
                    } else if (outPtr <= outLen - step) {
                        i = 0;
                        while (true) {
                            outPtr2 = outPtr;
                            if (i >= step) {
                                break;
                            }
                            outPtr = outPtr2 + 1;
                            outData[outPtr2] = d[i];
                            i++;
                        }
                        outPtr = outPtr2;
                    }
                }
                sum -= 1.0d;
                inPtr += step;
            }
        }
        return 0;
    }

    private int doShortCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio) {
        short[] inData = (short[]) in.getData();
        short[] outData = validateShortArraySize(out, outLen);
        out.setData(outData);
        out.setFormat(this.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = Pa.LATENCY_UNSPECIFIED;
        int inPtr = inOffset;
        int outPtr = 0;
        int inEnd = inOffset + inLen;
        if (ratio == 1.0d) {
            System.arraycopy(inData, inOffset, outData, 0, inLen);
            return 0;
        }
        int i;
        int outPtr2;
        if (ratio > 1.0d) {
            while (inPtr <= inEnd - step && outPtr <= outLen - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    i++;
                }
                sum += ratio;
                while (sum > Pa.LATENCY_UNSPECIFIED) {
                    inPtr += step;
                    sum -= 1.0d;
                }
                outPtr = outPtr2;
            }
        } else {
            short[] d = new short[step];
            while (inPtr <= inEnd - step) {
                i = 0;
                while (true) {
                    outPtr2 = outPtr;
                    if (i >= step) {
                        break;
                    }
                    outPtr = outPtr2 + 1;
                    outData[outPtr2] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                    i++;
                }
                outPtr = outPtr2;
                while (true) {
                    sum += ratio;
                    if (sum >= 1.0d) {
                        break;
                    } else if (outPtr <= outLen - step) {
                        i = 0;
                        while (true) {
                            outPtr2 = outPtr;
                            if (i >= step) {
                                break;
                            }
                            outPtr = outPtr2 + 1;
                            outData[outPtr2] = d[i];
                            i++;
                        }
                        outPtr = outPtr2;
                    }
                }
                sum -= 1.0d;
                inPtr += step;
            }
        }
        return 0;
    }

    public String getName() {
        return "Rate Conversion";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return new Format[]{new AudioFormat(AudioFormat.LINEAR)};
        }
        if (input instanceof AudioFormat) {
            AudioFormat af = (AudioFormat) input;
            int ssize = af.getSampleSizeInBits();
            int chnl = af.getChannels();
            int endian = af.getEndian();
            int signed = af.getSigned();
            this.outputFormats = new Format[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 11025.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 16000.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 22050.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 32000.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 44100.0d, ssize, chnl, endian, signed), new AudioFormat(AudioFormat.LINEAR, 48000.0d, ssize, chnl, endian, signed)};
        } else {
            this.outputFormats = new Format[0];
        }
        return this.outputFormats;
    }

    public synchronized int process(Buffer in, Buffer out) {
        int i;
        if (!checkInputBuffer(in)) {
            i = 1;
        } else if (isEOM(in)) {
            propagateEOM(out);
            i = 0;
        } else {
            int step;
            int inOffset = in.getOffset();
            int inLen = in.getLength();
            double inRate = ((AudioFormat) this.inputFormat).getSampleRate();
            double outRate = ((AudioFormat) this.outputFormat).getSampleRate();
            int bsize = ((AudioFormat) this.inputFormat).getSampleSizeInBits() / 8;
            if (((AudioFormat) this.inputFormat).getChannels() == 2) {
                if (bsize == 2) {
                    step = 4;
                } else {
                    step = 2;
                }
            } else if (bsize == 2) {
                step = 2;
            } else {
                step = 1;
            }
            if (outRate == Pa.LATENCY_UNSPECIFIED || inRate == Pa.LATENCY_UNSPECIFIED) {
                i = 1;
            } else {
                double ratio = inRate / outRate;
                int outLen = (int) (((((double) (inLen - inOffset)) * outRate) / inRate) + 0.5d);
                switch (step) {
                    case 2:
                        if (outLen % 2 == 1) {
                            outLen++;
                            break;
                        }
                        break;
                    case 4:
                        if (outLen % 4 != 0) {
                            outLen = ((outLen / 4) + 1) << 2;
                            break;
                        }
                        break;
                }
                if (this.inputFormat.getDataType() == Format.byteArray) {
                    i = doByteCvrt(in, inLen, inOffset, out, outLen, step, ratio);
                } else if (this.inputFormat.getDataType() == Format.shortArray) {
                    i = doShortCvrt(in, inLen, inOffset, out, outLen, step, ratio);
                } else if (this.inputFormat.getDataType() == Format.intArray) {
                    i = doIntCvrt(in, inLen, inOffset, out, outLen, step, ratio);
                } else {
                    i = 1;
                }
            }
        }
        return i;
    }
}
