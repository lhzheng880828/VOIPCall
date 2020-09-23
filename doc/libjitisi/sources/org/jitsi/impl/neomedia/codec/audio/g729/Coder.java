package org.jitsi.impl.neomedia.codec.audio.g729;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Coder extends Ld8k {
    private final CodLd8k codLd8k = new CodLd8k();
    private final PreProc preProc = new PreProc();
    private final int[] prm = new int[11];

    Coder() {
        this.preProc.init_pre_process();
        this.codLd8k.init_coder_ld8k();
    }

    public static void main(String[] args) throws IOException {
        short[] sp16 = new short[80];
        short[] serial = new short[82];
        System.out.printf("\n", new Object[0]);
        System.out.printf("************  ITU G.729  8 Kbit/S SPEECH CODER  **************\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        System.out.printf("----------------- Floating point C simulation ----------------\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        System.out.printf("------------ Version 1.01 (Release 2, November 2006) --------\n", new Object[0]);
        System.out.printf("\n", new Object[0]);
        if (args.length != 2) {
            System.out.printf("Usage : coder  speech_file  bitstream_file \n", new Object[0]);
            System.out.printf("\n", new Object[0]);
            System.out.printf("Format for speech_file:\n", new Object[0]);
            System.out.printf("  Speech is read form a binary file of 16 bits data.\n", new Object[0]);
            System.out.printf("\n", new Object[0]);
            System.out.printf("Format for bitstream_file:\n", new Object[0]);
            System.out.printf("  One word (2-bytes) to indicate erasure.\n", new Object[0]);
            System.out.printf("  One word (2 bytes) to indicate bit rate\n", new Object[0]);
            System.out.printf("  80 words (2-bytes) containing 80 bits.\n", new Object[0]);
            System.out.printf("\n", new Object[0]);
            System.exit(1);
        }
        try {
            InputStream f_speech = new FileInputStream(args[0]);
            System.out.printf(" Input speech file     :  %s\n", new Object[]{args[0]});
            try {
                OutputStream f_serial = new FileOutputStream(args[1]);
                System.out.printf(" Output bitstream file :  %s\n", new Object[]{args[1]});
                Coder coder = new Coder();
                int frame = 0;
                while (Util.fread(sp16, 80, f_speech) == 80) {
                    frame++;
                    System.out.printf(" Frame: %d\r", new Object[]{Integer.valueOf(frame)});
                    coder.process(sp16, serial);
                    Util.fwrite(serial, 82, f_serial);
                }
                f_serial.close();
                f_speech.close();
            } catch (IOException ex) {
                System.out.printf("Coder - Error opening file  %s !!\n", new Object[]{args[1]});
                System.exit(0);
                throw ex;
            }
        } catch (IOException ex2) {
            System.out.printf("Codder - Error opening file  %s !!\n", new Object[]{args[0]});
            System.exit(0);
            throw ex2;
        }
    }

    /* access modifiers changed from: 0000 */
    public void process(short[] sp16, short[] serial) {
        float[] new_speech = this.codLd8k.new_speech;
        int new_speech_offset = this.codLd8k.new_speech_offset;
        for (int i = 0; i < 80; i++) {
            new_speech[new_speech_offset + i] = (float) sp16[i];
        }
        this.preProc.pre_process(new_speech, new_speech_offset, 80);
        this.codLd8k.coder_ld8k(this.prm);
        Bits.prm2bits_ld8k(this.prm, serial);
    }
}
