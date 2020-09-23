package org.jitsi.impl.neomedia.codec.audio.g729;

class Filter {
    Filter() {
    }

    static void convolve(float[] x, int x_offset, float[] h, float[] y, int l) {
        for (int n = 0; n < l; n++) {
            float temp = 0.0f;
            for (int i = 0; i <= n; i++) {
                temp += x[x_offset + i] * h[n - i];
            }
            y[n] = temp;
        }
    }

    static void syn_filt(float[] a, int a_offset, float[] x, int x_offset, float[] y, int y_offset, int l, float[] mem, int mem_offset, int update) {
        int yy;
        float[] yy_b = new float[50];
        int i = 0;
        int yy2 = 0;
        int mem_offset2 = mem_offset;
        while (i < 10) {
            yy = yy2 + 1;
            mem_offset = mem_offset2 + 1;
            yy_b[yy2] = mem[mem_offset2];
            i++;
            yy2 = yy;
            mem_offset2 = mem_offset;
        }
        i = 0;
        while (true) {
            int y_offset2 = y_offset;
            int x_offset2 = x_offset;
            if (i >= l) {
                break;
            }
            int py = yy2;
            int pa = 0;
            x_offset = x_offset2 + 1;
            float s = x[x_offset2];
            for (int j = 0; j < 10; j++) {
                pa++;
                py--;
                s -= a[a_offset + pa] * yy_b[py];
            }
            yy = yy2 + 1;
            yy_b[yy2] = s;
            y_offset = y_offset2 + 1;
            y[y_offset2] = s;
            i++;
            yy2 = yy;
        }
        if (update != 0) {
            yy = yy2;
            mem_offset = mem_offset2;
            for (i = 0; i < 10; i++) {
                mem_offset--;
                yy--;
                mem[mem_offset] = yy_b[yy];
            }
            return;
        }
        mem_offset = mem_offset2;
    }

    static void residu(float[] a, int a_offset, float[] x, int x_offset, float[] y, int y_offset, int l) {
        for (int i = 0; i < l; i++) {
            float s = x[x_offset + i];
            for (int j = 1; j <= 10; j++) {
                s += a[a_offset + j] * x[(x_offset + i) - j];
            }
            y[y_offset + i] = s;
        }
    }
}
