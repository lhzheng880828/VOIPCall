package org.jitsi.impl.neomedia.codec.audio.g729;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Decoder extends Ld8k {
    private final float[] Az_dec = new float[22];
    private final DecLd8k decLd8k = new DecLd8k();
    private final int[] parm = new int[12];
    private final PostPro postPro = new PostPro();
    private final Postfil postfil = new Postfil();
    private final float[] pst_out = new float[80];
    private final float[] synth = this.synth_buf;
    private final float[] synth_buf = new float[90];
    private int synth_offset = 10;
    private int voicing;

    Decoder() {
        this.decLd8k.init_decod_ld8k();
        this.postfil.init_post_filter();
        this.postPro.init_post_process();
        this.voicing = 60;
    }

    private static void floats2shorts(float[] floats, short[] shorts) {
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            if (f >= 0.0f) {
                f += 0.5f;
            } else {
                f -= 0.5f;
            }
            if (f > 32767.0f) {
                f = 32767.0f;
            }
            if (f < -32768.0f) {
                f = -32768.0f;
            }
            shorts[i] = (short) ((int) f);
        }
    }

    public static void main(String[] args) throws IOException {
        short[] serial = new short[82];
        short[] sp16 = new short[80];
        System.out.printf("\n", new Object[0]);
        System.out.printf("**************    G.729  8 KBIT/S SPEECH DECODER    ************\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        System.out.printf("----------------- Floating point C simulation ----------------\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        System.out.printf("------------ Version 1.01 (Release 2, November 2006) --------\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        if (args.length != 2) {
            System.out.printf("Usage :Decoder bitstream_file  outputspeech_file\n", new Object[0]);
            System.out.printf("\n", new Object[0]);
            System.out.printf("Format for bitstream_file:\n", new Object[0]);
            System.out.printf("  One (2-byte) synchronization word \n", new Object[0]);
            System.out.printf("  One (2-byte) size word,\n", new Object[0]);
            System.out.printf("  80 words (2-byte) containing 80 bits.\n", new Object[0]);
            System.out.printf("\n", new Object[0]);
            System.out.printf("Format for outputspeech_file:\n", new Object[0]);
            System.out.printf("  Synthesis is written to a binary file of 16 bits data.\n", new Object[0]);
            System.exit(1);
        }
        try {
            InputStream f_serial = new FileInputStream(args[0]);
            try {
                OutputStream f_syn = new FileOutputStream(args[1]);
                System.out.printf("Input bitstream file  :   %s\n", new Object[]{args[0]});
                System.out.printf("Synthesis speech file :   %s\n", new Object[]{args[1]});
                Decoder decoder = new Decoder();
                int frame = 0;
                while (Util.fread(serial, 82, f_serial) == 82) {
                    frame++;
                    System.out.printf(" Frame: %d\r", new Object[]{Integer.valueOf(frame)});
                    decoder.process(serial, sp16);
                    Util.fwrite(sp16, 80, f_syn);
                }
                f_syn.close();
                f_serial.close();
            } catch (IOException ex) {
                System.out.printf("Decoder - Error opening file  %s !!\n", new Object[]{args[1]});
                System.exit(0);
                throw ex;
            }
        } catch (IOException ex2) {
            System.out.printf("Decoder - Error opening file  %s !!\n", new Object[]{args[0]});
            System.exit(0);
            throw ex2;
        }
    }

    /* access modifiers changed from: 0000 */
    public void process(short[] serial, short[] sp16) {
        int i;
        Bits.bits2prm_ld8k(serial, 2, this.parm, 1);
        this.parm[0] = 0;
        for (i = 2; i < 82; i++) {
            if (serial[i] == (short) 0) {
                this.parm[0] = 1;
            }
        }
        this.parm[4] = PParity.check_parity_pitch(this.parm[3], this.parm[4]);
        int t0_first = this.decLd8k.decod_ld8k(this.parm, this.voicing, this.synth, this.synth_offset, this.Az_dec);
        this.voicing = 0;
        float[] ptr_Az = this.Az_dec;
        int ptr_Az_offset = 0;
        for (i = 0; i < 80; i += 40) {
            int sf_voic = this.postfil.post(t0_first, this.synth, this.synth_offset + i, ptr_Az, ptr_Az_offset, this.pst_out, i);
            if (sf_voic != 0) {
                this.voicing = sf_voic;
            }
            ptr_Az_offset += 11;
        }
        Util.copy(this.synth_buf, 80, this.synth_buf, 10);
        this.postPro.post_process(this.pst_out, 80);
        floats2shorts(this.pst_out, sp16);
    }
}
