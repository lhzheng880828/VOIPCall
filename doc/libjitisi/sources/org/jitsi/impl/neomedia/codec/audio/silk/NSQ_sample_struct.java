package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: NSQDelDec */
class NSQ_sample_struct implements Cloneable {
    int LF_AR_Q12;
    int LPC_exc_Q16;
    int Q_Q10;
    int RD_Q10;
    int sLTP_shp_Q10;
    int xq_Q14;

    NSQ_sample_struct() {
    }

    public Object clone() {
        NSQ_sample_struct clone = null;
        try {
            return (NSQ_sample_struct) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return clone;
        }
    }
}
