package org.rubycoder.gsm;

public class GSMDriver {
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005d A:{SYNTHETIC, Splitter:B:19:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005d A:{SYNTHETIC, Splitter:B:19:0x005d} */
    private static void decode(java.lang.String r14, java.lang.String r15) {
        /*
        r13 = 160; // 0xa0 float:2.24E-43 double:7.9E-322;
        r8 = new org.rubycoder.gsm.GSMDecoder;
        r8.m2788init();
        r11 = 33;
        r7 = new byte[r11];
        r10 = new int[r13];
        r1 = 0;
        r3 = 0;
        r11 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r9 = new byte[r11];
        r11 = "";
        r11 = r14.equalsIgnoreCase(r11);
        if (r11 != 0) goto L_0x0023;
    L_0x001b:
        r11 = "";
        r11 = r15.equalsIgnoreCase(r11);
        if (r11 == 0) goto L_0x002e;
    L_0x0023:
        r11 = java.lang.System.err;
        r12 = "Usage: GSMDriver inputfile outputfile\n";
        r11.print(r12);
        r11 = 1;
        java.lang.System.exit(r11);
    L_0x002e:
        r2 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x0047 }
        r2.<init>(r14);	 Catch:{ Exception -> 0x0047 }
        r4 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x00a2 }
        r4.<init>(r15);	 Catch:{ Exception -> 0x00a2 }
        r3 = r4;
        r1 = r2;
    L_0x003a:
        r11 = r1.read(r7);	 Catch:{ IOException -> 0x0054 }
        if (r11 > 0) goto L_0x005d;
    L_0x0040:
        r1.close();	 Catch:{ IOException -> 0x0099 }
        r3.close();	 Catch:{ IOException -> 0x0099 }
    L_0x0046:
        return;
    L_0x0047:
        r0 = move-exception;
    L_0x0048:
        r11 = java.lang.System.err;
        r12 = "file not found, or can't open.\n";
        r11.println(r12);
        r11 = 2;
        java.lang.System.exit(r11);
        goto L_0x003a;
    L_0x0054:
        r0 = move-exception;
        r11 = java.lang.System.err;
        r12 = "error reading inputArray";
        r11.println(r12);
        goto L_0x0040;
    L_0x005d:
        r8.decode(r7, r10);	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r5 = 0;
    L_0x0061:
        if (r5 >= r13) goto L_0x007c;
    L_0x0063:
        r6 = r5 << 1;
        r11 = r10[r5];	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r11 = r11 & 255;
        r11 = (byte) r11;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r9[r6] = r11;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r6 = r6 + 1;
        r11 = r10[r5];	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r12 = 65280; // 0xff00 float:9.1477E-41 double:3.22526E-319;
        r11 = r11 & r12;
        r11 = r11 >> 8;
        r11 = (byte) r11;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r9[r6] = r11;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r5 = r5 + 1;
        goto L_0x0061;
    L_0x007c:
        r11 = java.lang.System.out;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r12 = "-";
        r11.println(r12);	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r3.write(r9);	 Catch:{ IOException -> 0x0087 }
        goto L_0x003a;
    L_0x0087:
        r0 = move-exception;
        r11 = java.lang.System.err;	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        r12 = "error writing outputArray";
        r11.println(r12);	 Catch:{ InvalidGSMFrameException -> 0x0090 }
        goto L_0x0040;
    L_0x0090:
        r0 = move-exception;
        r11 = java.lang.System.err;
        r12 = "bad frame";
        r11.println(r12);
        goto L_0x0040;
    L_0x0099:
        r0 = move-exception;
        r11 = java.lang.System.err;
        r12 = "error closing files.";
        r11.println(r12);
        goto L_0x0046;
    L_0x00a2:
        r0 = move-exception;
        r1 = r2;
        goto L_0x0048;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.rubycoder.gsm.GSMDriver.decode(java.lang.String, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0042  */
    private static void encode(java.lang.String r14, java.lang.String r15) {
        /*
        r10 = new org.rubycoder.gsm.GSMEncoder;
        r10.m2792init();
        r12 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r9 = new byte[r12];
        r12 = 33;
        r11 = new byte[r12];
        r1 = 0;
        r3 = 0;
        r12 = 160; // 0xa0 float:2.24E-43 double:7.9E-322;
        r6 = new int[r12];
        r2 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x002c }
        r2.<init>(r14);	 Catch:{ Exception -> 0x002c }
        r4 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x007c }
        r4.<init>(r15);	 Catch:{ Exception -> 0x007c }
        r3 = r4;
        r1 = r2;
    L_0x001f:
        r12 = r1.read(r9);	 Catch:{ IOException -> 0x0039 }
        if (r12 > 0) goto L_0x0042;
    L_0x0025:
        r1.close();	 Catch:{ IOException -> 0x0073 }
        r3.close();	 Catch:{ IOException -> 0x0073 }
    L_0x002b:
        return;
    L_0x002c:
        r0 = move-exception;
    L_0x002d:
        r12 = java.lang.System.err;
        r13 = "file not found, or can't open.\n";
        r12.println(r13);
        r12 = 2;
        java.lang.System.exit(r12);
        goto L_0x001f;
    L_0x0039:
        r0 = move-exception;
        r12 = java.lang.System.err;
        r13 = "error reading inputArray";
        r12.println(r13);
        goto L_0x0025;
    L_0x0042:
        r5 = 0;
    L_0x0043:
        r12 = 160; // 0xa0 float:2.24E-43 double:7.9E-322;
        if (r5 >= r12) goto L_0x0063;
    L_0x0047:
        r7 = r5 << 1;
        r12 = r7 + 1;
        r12 = r9[r12];
        r6[r5] = r12;
        r12 = r6[r5];
        r12 = r12 << 8;
        r6[r5] = r12;
        r12 = r6[r5];
        r8 = r7 + 1;
        r13 = r9[r7];
        r13 = r13 & 255;
        r12 = r12 | r13;
        r6[r5] = r12;
        r5 = r5 + 1;
        goto L_0x0043;
    L_0x0063:
        r10.encode(r11, r6);
        r3.write(r11);	 Catch:{ IOException -> 0x006a }
        goto L_0x001f;
    L_0x006a:
        r0 = move-exception;
        r12 = java.lang.System.err;
        r13 = "error writing outputArray";
        r12.println(r13);
        goto L_0x0025;
    L_0x0073:
        r0 = move-exception;
        r12 = java.lang.System.err;
        r13 = "error closing files.";
        r12.println(r13);
        goto L_0x002b;
    L_0x007c:
        r0 = move-exception;
        r1 = r2;
        goto L_0x002d;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.rubycoder.gsm.GSMDriver.encode(java.lang.String, java.lang.String):void");
    }

    public static void main(String[] argv) {
        if (argv.length != 3) {
            System.err.print("Usage: GSMDriver d inputfile outputfile   -  decode from gsm file");
            System.err.print("       GSMDriver e inputfile outputfile   -  encode into gsm file");
            System.exit(2);
        }
        if (argv[0].equalsIgnoreCase("d")) {
            decode(argv[1], argv[2]);
        } else if (argv[0].equalsIgnoreCase("e")) {
            encode(argv[1], argv[2]);
        }
    }
}
