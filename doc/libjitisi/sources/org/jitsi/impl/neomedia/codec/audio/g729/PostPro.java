package org.jitsi.impl.neomedia.codec.audio.g729;

class PostPro {
    private float x0;
    private float x1;
    private float y1;
    private float y2;

    PostPro() {
    }

    /* access modifiers changed from: 0000 */
    public void init_post_process() {
        this.x1 = 0.0f;
        this.x0 = 0.0f;
        this.y1 = 0.0f;
        this.y2 = 0.0f;
    }

    /* access modifiers changed from: 0000 */
    public void post_process(float[] signal, int lg) {
        float[] a100 = TabLd8k.a100;
        float[] b100 = TabLd8k.b100;
        for (int i = 0; i < lg; i++) {
            float x2 = this.x1;
            this.x1 = this.x0;
            this.x0 = signal[i];
            float y0 = ((((this.y1 * a100[1]) + (this.y2 * a100[2])) + (this.x0 * b100[0])) + (this.x1 * b100[1])) + (b100[2] * x2);
            signal[i] = y0;
            this.y2 = this.y1;
            this.y1 = y0;
        }
    }
}
