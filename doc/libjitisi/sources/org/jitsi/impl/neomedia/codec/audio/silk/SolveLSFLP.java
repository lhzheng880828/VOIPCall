package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class SolveLSFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!SolveLSFLP.class.desiredAssertionStatus());

    static void SKP_Silk_solve_LDL_FLP(float[] A, int A_offset, int M, float[] b, float[] x, int x_offset) {
        float[] L_tmp = new float[256];
        float[] T = new float[16];
        float[] Dinv = new float[16];
        if ($assertionsDisabled || M <= 16) {
            SKP_Silk_LDL_FLP(A, A_offset, M, L_tmp, Dinv);
            SKP_Silk_SolveWithLowerTriangularWdiagOnes_FLP(L_tmp, M, b, T);
            for (int i = 0; i < M; i++) {
                T[i] = T[i] * Dinv[i];
            }
            SKP_Silk_SolveWithUpperTriangularFromLowerWdiagOnes_FLP(L_tmp, M, T, x, x_offset);
            return;
        }
        throw new AssertionError();
    }

    static void SKP_Silk_SolveWithUpperTriangularFromLowerWdiagOnes_FLP(float[] L, int M, float[] b, float[] x, int x_offset) {
        for (int i = M - 1; i >= 0; i--) {
            float[] ptr1 = L;
            int ptr1_offset = i;
            float temp = 0.0f;
            for (int j = M - 1; j > i; j--) {
                temp += ptr1[(j * M) + ptr1_offset] * x[x_offset + j];
            }
            x[x_offset + i] = b[i] - temp;
        }
    }

    static void SKP_Silk_SolveWithLowerTriangularWdiagOnes_FLP(float[] L, int M, float[] b, float[] x) {
        for (int i = 0; i < M; i++) {
            float[] ptr1 = L;
            int ptr1_offset = i * M;
            float temp = 0.0f;
            for (int j = 0; j < i; j++) {
                temp += ptr1[ptr1_offset + j] * x[j];
            }
            x[i] = b[i] - temp;
        }
    }

    static void SKP_Silk_LDL_FLP(float[] A, int A_offset, int M, float[] L, float[] Dinv) {
        int err = 1;
        float[] v = new float[16];
        float[] D = new float[16];
        if ($assertionsDisabled || M <= 16) {
            double diag_min_value = (double) (5.0E-6f * (A[A_offset + 0] + A[((M * M) + A_offset) - 1]));
            for (int loop_count = 0; loop_count < M && err == 1; loop_count++) {
                err = 0;
                int j = 0;
                while (j < M) {
                    int i;
                    float[] ptr1 = L;
                    int ptr1_offset = (j * M) + 0;
                    double temp = (double) A[((j * M) + A_offset) + j];
                    for (i = 0; i < j; i++) {
                        v[i] = ptr1[ptr1_offset + i] * D[i];
                        temp -= (double) (ptr1[ptr1_offset + i] * v[i]);
                    }
                    if (temp < diag_min_value) {
                        temp = (((double) (loop_count + 1)) * diag_min_value) - temp;
                        for (i = 0; i < M; i++) {
                            int i2 = ((i * M) + A_offset) + i;
                            A[i2] = (float) (((double) A[i2]) + temp);
                        }
                        err = 1;
                    } else {
                        D[j] = (float) temp;
                        Dinv[j] = (float) (1.0d / temp);
                        L[(j * M) + j] = 1.0f;
                        ptr1 = A;
                        ptr1_offset = A_offset + (j * M);
                        float[] ptr2 = L;
                        int ptr2_offset = (j + 1) * M;
                        for (i = j + 1; i < M; i++) {
                            temp = Pa.LATENCY_UNSPECIFIED;
                            for (int k = 0; k < j; k++) {
                                temp += (double) (ptr2[ptr2_offset + k] * v[k]);
                            }
                            L[(i * M) + j] = (float) ((((double) ptr1[ptr1_offset + i]) - temp) * ((double) Dinv[j]));
                            ptr2_offset += M;
                        }
                        j++;
                    }
                }
            }
            if (!$assertionsDisabled && err != 0) {
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }
}
