package org.jitsi.impl.neomedia.codec.audio.g729;

class QuaGain {
    private final float[] past_qua_en = new float[]{-14.0f, -14.0f, -14.0f, -14.0f};

    QuaGain() {
    }

    /* access modifiers changed from: 0000 */
    public int qua_gain(float[] code, float[] g_coeff, int l_subfr, FloatReference gain_pit, FloatReference gain_code, int tameflag) {
        float g_code;
        float[][] gbk1 = TabLd8k.gbk1;
        float[][] gbk2 = TabLd8k.gbk2;
        int[] map1 = TabLd8k.map1;
        int[] map2 = TabLd8k.map2;
        int index1 = 0;
        int index2 = 0;
        float[] best_gain = new float[2];
        float gcode0 = Gainpred.gain_predict(this.past_qua_en, code, l_subfr);
        float tmp = -1.0f / (((4.0f * g_coeff[0]) * g_coeff[2]) - (g_coeff[4] * g_coeff[4]));
        best_gain[0] = (((2.0f * g_coeff[2]) * g_coeff[1]) - (g_coeff[3] * g_coeff[4])) * tmp;
        best_gain[1] = (((2.0f * g_coeff[0]) * g_coeff[3]) - (g_coeff[1] * g_coeff[4])) * tmp;
        if (tameflag == 1 && best_gain[0] > 0.94f) {
            best_gain[0] = 0.94f;
        }
        IntReference cand1Ref = new IntReference();
        IntReference cand2Ref = new IntReference();
        gbk_presel(best_gain, cand1Ref, cand2Ref, gcode0);
        int cand1 = cand1Ref.value;
        int cand2 = cand2Ref.value;
        float dist_min = 1.0E38f;
        int i;
        int j;
        float g_pitch;
        float dist;
        if (tameflag == 1) {
            for (i = 0; i < 4; i++) {
                for (j = 0; j < 8; j++) {
                    g_pitch = gbk1[cand1 + i][0] + gbk2[cand2 + j][0];
                    if (g_pitch < 0.9999f) {
                        g_code = gcode0 * (gbk1[cand1 + i][1] + gbk2[cand2 + j][1]);
                        dist = (((((g_pitch * g_pitch) * g_coeff[0]) + (g_coeff[1] * g_pitch)) + ((g_code * g_code) * g_coeff[2])) + (g_coeff[3] * g_code)) + ((g_pitch * g_code) * g_coeff[4]);
                        if (dist < dist_min) {
                            dist_min = dist;
                            index1 = cand1 + i;
                            index2 = cand2 + j;
                        }
                    }
                }
            }
        } else {
            for (i = 0; i < 4; i++) {
                for (j = 0; j < 8; j++) {
                    g_pitch = gbk1[cand1 + i][0] + gbk2[cand2 + j][0];
                    g_code = gcode0 * (gbk1[cand1 + i][1] + gbk2[cand2 + j][1]);
                    dist = (((((g_pitch * g_pitch) * g_coeff[0]) + (g_coeff[1] * g_pitch)) + ((g_code * g_code) * g_coeff[2])) + (g_coeff[3] * g_code)) + ((g_pitch * g_code) * g_coeff[4]);
                    if (dist < dist_min) {
                        dist_min = dist;
                        index1 = cand1 + i;
                        index2 = cand2 + j;
                    }
                }
            }
        }
        gain_pit.value = gbk1[index1][0] + gbk2[index2][0];
        g_code = gbk1[index1][1] + gbk2[index2][1];
        gain_code.value = g_code * gcode0;
        Gainpred.gain_update(this.past_qua_en, g_code);
        return (map1[index1] * 16) + map2[index2];
    }

    private void gbk_presel(float[] best_gain, IntReference cand1, IntReference cand2, float gcode0) {
        float[][] coef = TabLd8k.coef;
        float[] thr1 = TabLd8k.thr1;
        float[] thr2 = TabLd8k.thr2;
        int _cand1 = cand1.value;
        int _cand2 = cand2.value;
        float x = (best_gain[1] - (((coef[0][0] * best_gain[0]) + coef[1][1]) * gcode0)) * -0.032623f;
        float y = (((coef[1][0] * ((-coef[0][1]) + (best_gain[0] * coef[0][0]))) * gcode0) - (coef[0][0] * best_gain[1])) * -0.032623f;
        if (gcode0 <= 0.0f) {
            _cand1 = 0;
            while (y < thr1[_cand1] * gcode0) {
                _cand1++;
                if (_cand1 >= 4) {
                    break;
                }
            }
            _cand2 = 0;
            while (x < thr2[_cand2] * gcode0) {
                _cand2++;
                if (_cand2 >= 8) {
                    break;
                }
            }
        }
        _cand1 = 0;
        while (y > thr1[_cand1] * gcode0) {
            _cand1++;
            if (_cand1 >= 4) {
                break;
            }
        }
        _cand2 = 0;
        while (x > thr2[_cand2] * gcode0) {
            _cand2++;
            if (_cand2 >= 8) {
                break;
            }
        }
        cand1.value = _cand1;
        cand2.value = _cand2;
    }
}
