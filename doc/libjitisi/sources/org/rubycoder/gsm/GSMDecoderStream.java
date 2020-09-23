package org.rubycoder.gsm;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.io.InputStream;

class GSMDecoderStream extends InputStream {
    private static int[] lookup_table;
    private InputStream GSMStream;
    private final int[] buffer;
    private int buffer_index = 0;
    private int buffer_size = 0;
    private final byte[] gsm_frame;
    private int gsm_index = 0;
    private final GSMDecoder theDecoder;

    private static int[] create_lookup_table() {
        int[] res = new int[8192];
        for (int i = 0; i < 8192; i++) {
            res[i] = lin2mu((i - 4096) << 3) & UnsignedUtils.MAX_UBYTE;
        }
        return res;
    }

    private static byte lin2mu(int lin) {
        int s;
        lin >>= 2;
        if (lin < 0) {
            lin = -lin;
            s = 1;
        } else {
            s = 0;
        }
        for (int n = 0; n < 8; n++) {
            if (lin < (32 << (n + 1)) - 32) {
                int e = n;
                return (byte) ((((s << 7) | (e << 4)) | ((((lin - (32 << e)) + 32) >>> (e + 1)) & 15)) ^ UnsignedUtils.MAX_UBYTE);
            }
        }
        return (byte) (s << 8);
    }

    public GSMDecoderStream(InputStream is) {
        if (lookup_table == null) {
            lookup_table = create_lookup_table();
        }
        this.gsm_index = 0;
        this.GSMStream = is;
        this.buffer = new int[160];
        this.buffer_index = 0;
        this.buffer_size = 0;
        this.gsm_frame = new byte[33];
        this.theDecoder = new GSMDecoder();
    }

    public int available() throws IOException {
        try {
            return ((this.GSMStream.available() / 33) * 160) + (this.buffer_size - this.buffer_index);
        } catch (IOException e) {
            throw new IOException("Recieved IO Exception from source stream.");
        } catch (NullPointerException e2) {
            throw new IOException("Source stream not open.");
        }
    }

    public void close() {
        this.GSMStream = null;
    }

    public synchronized void mark(int size) {
    }

    public boolean markSupported() {
        return false;
    }

    public final int read() {
        if (this.buffer_index >= this.buffer_size) {
            try {
                if (this.GSMStream.read(this.gsm_frame) < 33) {
                    close();
                    return -1;
                }
                try {
                    this.theDecoder.decode(this.gsm_frame, this.buffer);
                    this.buffer_index = 0;
                    this.buffer_size = 160;
                } catch (InvalidGSMFrameException e) {
                    System.out.println("invalid frame");
                    close();
                    return -1;
                }
            } catch (IOException e2) {
                System.out.println("got io exception");
                close();
                return -1;
            } catch (NullPointerException e3) {
                return -1;
            }
        }
        int[] iArr = lookup_table;
        int[] iArr2 = this.buffer;
        int i = this.buffer_index;
        this.buffer_index = i + 1;
        return iArr[(iArr2[i] >> 3) + 4096];
    }

    public final int read(byte[] output) {
        return read(output, 0, output.length);
    }

    public final int read(byte[] output, int start, int length) {
        if (this.GSMStream == null) {
            return -1;
        }
        int i = start;
        while (i < length) {
            int val;
            if (this.buffer_index >= this.buffer_size) {
                this.gsm_index = 0;
                while (this.gsm_index < 33) {
                    try {
                        int read_count = this.GSMStream.read(this.gsm_frame, this.gsm_index, 33 - this.gsm_index);
                        if (read_count < 0) {
                            System.out.println("got eof");
                            close();
                            val = -1;
                            break;
                        }
                        this.gsm_index += read_count;
                    } catch (IOException e) {
                        System.out.println("got io exception");
                        close();
                        val = -1;
                    } catch (NullPointerException e2) {
                        val = -1;
                    }
                }
                try {
                    this.theDecoder.decode(this.gsm_frame, this.buffer);
                    this.buffer_index = 0;
                    this.buffer_size = 160;
                } catch (InvalidGSMFrameException e3) {
                    System.out.println("invalid frame");
                    close();
                    val = -1;
                }
            }
            int[] iArr = lookup_table;
            int[] iArr2 = this.buffer;
            int i2 = this.buffer_index;
            this.buffer_index = i2 + 1;
            val = iArr[(iArr2[i2] >> 3) + 4096];
            if (val < 0) {
                return i;
            }
            output[i] = (byte) val;
            i++;
        }
        return i;
    }

    public void reset() {
    }

    public void skip(int n) {
    }
}
