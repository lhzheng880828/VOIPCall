package org.jitsi.impl.neomedia.codec.audio.g729;

class PreProc {
    private float x0;
    private float x1;
    private float y1;
    private float y2;

    PreProc() {
    }

    /* access modifiers changed from: 0000 */
    public void init_pre_process() {
        this.x1 = 0.0f;
        this.x0 = 0.0f;
        this.y1 = 0.0f;
        this.y2 = 0.0f;
    }

    /* access modifiers changed from: 0000 */
    public void pre_process(float[] signal, int signal_offset, int lg) {
        float[] a140 = TabLd8k.a140;
        float[] b140 = TabLd8k.b140;
        int toIndex = lg + signal_offset;
        for (int i = signal_offset; i < toIndex; i++) {
            float x2 = this.x1;
            this.x1 = this.x0;
            this.x0 = signal[i];
            float y0 = ((((this.y1 * a140[1]) + (this.y2 * a140[2])) + (this.x0 * b140[0])) + (this.x1 * b140[1])) + (b140[2] * x2);
            signal[i] = y0;
            this.y2 = this.y1;
            this.y1 = y0;
        }
    }
}
