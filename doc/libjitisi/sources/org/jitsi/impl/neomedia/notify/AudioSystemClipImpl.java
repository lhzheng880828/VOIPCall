package org.jitsi.impl.neomedia.notify;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Renderer;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.service.audionotifier.AbstractSCAudioClip;
import org.jitsi.service.audionotifier.AudioNotifierService;
import org.jitsi.util.Logger;

public class AudioSystemClipImpl extends AbstractSCAudioClip {
    private static final int DEFAULT_BUFFER_DATA_LENGTH = 8192;
    private static final long MIN_AUDIO_STREAM_DURATION = 200;
    private static final Logger logger = Logger.getLogger(AudioSystemClipImpl.class);
    private final AudioSystem audioSystem;
    private Buffer buffer;
    private byte[] bufferData;
    private final boolean playback;
    private Renderer renderer;

    public AudioSystemClipImpl(String url, AudioNotifierService audioNotifier, AudioSystem audioSystem, boolean playback) throws IOException {
        super(url, audioNotifier);
        this.audioSystem = audioSystem;
        this.playback = playback;
    }

    /* access modifiers changed from: protected */
    public void enterRunInPlayThread() {
        this.buffer = new Buffer();
        this.bufferData = new byte[8192];
        this.buffer.setData(this.bufferData);
        this.renderer = this.audioSystem.createRenderer(this.playback);
    }

    /* access modifiers changed from: protected */
    public void exitRunInPlayThread() {
        this.buffer = null;
        this.bufferData = null;
        this.renderer = null;
    }

    /* access modifiers changed from: protected */
    public void exitRunOnceInPlayThread() {
        try {
            this.renderer.stop();
        } finally {
            this.renderer.close();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0210 A:{SYNTHETIC, Splitter:B:97:0x0210} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x03bd  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x03eb  */
    public boolean runOnceInPlayThread() {
        /*
        r44 = this;
        r7 = 0;
        r0 = r44;
        r0 = r0.audioSystem;	 Catch:{ IOException -> 0x0016 }
        r38 = r0;
        r0 = r44;
        r0 = r0.uri;	 Catch:{ IOException -> 0x0016 }
        r39 = r0;
        r7 = r38.getAudioInputStream(r39);	 Catch:{ IOException -> 0x0016 }
    L_0x0011:
        if (r7 != 0) goto L_0x003c;
    L_0x0013:
        r32 = 0;
    L_0x0015:
        return r32;
    L_0x0016:
        r19 = move-exception;
        r38 = logger;
        r39 = new java.lang.StringBuilder;
        r39.<init>();
        r40 = "Failed to get audio stream ";
        r39 = r39.append(r40);
        r0 = r44;
        r0 = r0.uri;
        r40 = r0;
        r39 = r39.append(r40);
        r39 = r39.toString();
        r0 = r38;
        r1 = r39;
        r2 = r19;
        r0.error(r1, r2);
        goto L_0x0011;
    L_0x003c:
        r25 = 0;
        r32 = 1;
        r10 = 0;
        r11 = 0;
        r26 = 0;
        r0 = r44;
        r0 = r0.audioSystem;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r0;
        r0 = r38;
        r10 = r0.getFormat(r7);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r23 = r10;
        if (r23 != 0) goto L_0x00cf;
    L_0x0054:
        r38 = 0;
        r7.close();	 Catch:{ IOException -> 0x042e }
    L_0x0059:
        if (r25 == 0) goto L_0x005e;
    L_0x005b:
        r25.close();
    L_0x005e:
        if (r32 == 0) goto L_0x00b6;
    L_0x0060:
        if (r10 == 0) goto L_0x00b6;
    L_0x0062:
        if (r11 <= 0) goto L_0x00b6;
    L_0x0064:
        r40 = 0;
        r39 = (r26 > r40 ? 1 : (r26 == r40 ? 0 : -1));
        if (r39 <= 0) goto L_0x00b6;
    L_0x006a:
        r39 = r44.isStarted();
        if (r39 == 0) goto L_0x00b6;
    L_0x0070:
        r0 = (long) r11;
        r40 = r0;
        r0 = r40;
        r40 = r10.computeDuration(r0);
        r42 = 999999; // 0xf423f float:1.401297E-39 double:4.94065E-318;
        r40 = r40 + r42;
        r42 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r8 = r40 / r42;
        r40 = 0;
        r39 = (r8 > r40 ? 1 : (r8 == r40 ? 0 : -1));
        if (r39 <= 0) goto L_0x00b6;
    L_0x0089:
        r40 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r8 = r8 + r40;
        r18 = 0;
        r0 = r44;
        r0 = r0.sync;
        r39 = r0;
        monitor-enter(r39);
    L_0x0096:
        r40 = r44.isStarted();	 Catch:{ all -> 0x00cc }
        if (r40 == 0) goto L_0x00ac;
    L_0x009c:
        r40 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x00cc }
        r36 = r40 - r26;
        r40 = (r36 > r8 ? 1 : (r36 == r8 ? 0 : -1));
        if (r40 >= 0) goto L_0x00ac;
    L_0x00a6:
        r40 = 0;
        r40 = (r36 > r40 ? 1 : (r36 == r40 ? 0 : -1));
        if (r40 > 0) goto L_0x00ba;
    L_0x00ac:
        monitor-exit(r39);	 Catch:{ all -> 0x00cc }
        if (r18 == 0) goto L_0x00b6;
    L_0x00af:
        r39 = java.lang.Thread.currentThread();
        r39.interrupt();
    L_0x00b6:
        r32 = r38;
        goto L_0x0015;
    L_0x00ba:
        r0 = r44;
        r0 = r0.sync;	 Catch:{ InterruptedException -> 0x00c8 }
        r40 = r0;
        r0 = r40;
        r1 = r36;
        r0.wait(r1);	 Catch:{ InterruptedException -> 0x00c8 }
        goto L_0x0096;
    L_0x00c8:
        r17 = move-exception;
        r18 = 1;
        goto L_0x0096;
    L_0x00cc:
        r38 = move-exception;
        monitor-exit(r39);	 Catch:{ all -> 0x00cc }
        throw r38;
    L_0x00cf:
        r30 = 0;
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r0;
        r0 = r38;
        r1 = r23;
        r38 = r0.setInputFormat(r1);	 Catch:{ ResourceUnavailableException -> 0x020d }
        if (r38 != 0) goto L_0x0141;
    L_0x00e1:
        r28 = new org.jitsi.impl.neomedia.codec.audio.speex.SpeexResampler;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r28.m2223init();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r30 = r23;
        r0 = r28;
        r1 = r30;
        r0.setInputFormat(r1);	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r0 = r28;
        r1 = r30;
        r35 = r0.getSupportedOutputFormats(r1);	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r38 = r0;
        r5 = r38.getSupportedInputFormats();	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r0 = r5.length;	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r20 = r0;
        r15 = 0;
        r16 = r15;
    L_0x0107:
        r0 = r16;
        r1 = r20;
        if (r0 >= r1) goto L_0x013f;
    L_0x010d:
        r33 = r5[r16];	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r6 = r35;
        r0 = r6.length;	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r21 = r0;
        r15 = 0;
    L_0x0115:
        r0 = r21;
        if (r15 >= r0) goto L_0x0137;
    L_0x0119:
        r34 = r6[r15];	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r38 = r33.matches(r34);	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        if (r38 == 0) goto L_0x013c;
    L_0x0121:
        r23 = r33;
        r0 = r28;
        r1 = r23;
        r0.setOutputFormat(r1);	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
        r38 = r0;
        r0 = r38;
        r1 = r23;
        r0.setInputFormat(r1);	 Catch:{ ResourceUnavailableException -> 0x043e, all -> 0x0439 }
    L_0x0137:
        r15 = r16 + 1;
        r16 = r15;
        goto L_0x0107;
    L_0x013c:
        r15 = r15 + 1;
        goto L_0x0115;
    L_0x013f:
        r25 = r28;
    L_0x0141:
        r0 = r44;
        r0 = r0.buffer;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r22 = r0;
        r22.setFormat(r23);	 Catch:{ ResourceUnavailableException -> 0x020d }
        if (r25 != 0) goto L_0x01ca;
    L_0x014c:
        r29 = 0;
    L_0x014e:
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = r0;
        r38.open();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = r0;
        r38.start();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
    L_0x0160:
        r38 = r44.isStarted();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        if (r38 == 0) goto L_0x02ec;
    L_0x0166:
        r0 = r44;
        r0 = r0.bufferData;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = r0;
        r0 = r38;
        r13 = r7.read(r0);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = -1;
        r0 = r38;
        if (r13 == r0) goto L_0x02ec;
    L_0x0178:
        r11 = r11 + r13;
        if (r25 != 0) goto L_0x029a;
    L_0x017b:
        r0 = r22;
        r0.setLength(r13);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = 0;
        r0 = r22;
        r1 = r38;
        r0.setOffset(r1);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
    L_0x0189:
        r38 = 0;
        r38 = (r26 > r38 ? 1 : (r26 == r38 ? 0 : -1));
        if (r38 != 0) goto L_0x0193;
    L_0x018f:
        r26 = java.lang.System.currentTimeMillis();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
    L_0x0193:
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = r0;
        r0 = r38;
        r1 = r22;
        r24 = r0.process(r1);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = 1;
        r0 = r24;
        r1 = r38;
        if (r0 != r1) goto L_0x034e;
    L_0x01a9:
        r38 = logger;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r39 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r39.<init>();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r40 = "Failed to render audio stream ";
        r39 = r39.append(r40);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r0 = r44;
        r0 = r0.uri;	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r40 = r0;
        r39 = r39.append(r40);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r39 = r39.toString();	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38.error(r39);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r32 = 0;
        goto L_0x0160;
    L_0x01ca:
        r29 = new javax.media.Buffer;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r29.m181init();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r12 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;
        r0 = r30;
        r0 = r0 instanceof javax.media.format.AudioFormat;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r0;
        if (r38 == 0) goto L_0x01ee;
    L_0x01d9:
        r0 = r30;
        r0 = (javax.media.format.AudioFormat) r0;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r4 = r0;
        r38 = r4.getSampleSizeInBits();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r38 / 8;
        r39 = r4.getChannels();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r14 = r38 * r39;
        r38 = r12 / r14;
        r12 = r38 * r14;
    L_0x01ee:
        r0 = new byte[r12];	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r0;
        r0 = r38;
        r1 = r44;
        r1.bufferData = r0;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r0 = r44;
        r0 = r0.bufferData;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r38 = r0;
        r0 = r29;
        r1 = r38;
        r0.setData(r1);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r29.setFormat(r30);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r25.open();	 Catch:{ ResourceUnavailableException -> 0x020d }
        goto L_0x014e;
    L_0x020d:
        r31 = move-exception;
    L_0x020e:
        if (r25 == 0) goto L_0x0238;
    L_0x0210:
        r38 = logger;	 Catch:{ all -> 0x03b7 }
        r39 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03b7 }
        r39.<init>();	 Catch:{ all -> 0x03b7 }
        r40 = "Failed to open ";
        r39 = r39.append(r40);	 Catch:{ all -> 0x03b7 }
        r40 = r25.getClass();	 Catch:{ all -> 0x03b7 }
        r40 = r40.getName();	 Catch:{ all -> 0x03b7 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x03b7 }
        r39 = r39.toString();	 Catch:{ all -> 0x03b7 }
        r0 = r38;
        r1 = r39;
        r2 = r31;
        r0.error(r1, r2);	 Catch:{ all -> 0x03b7 }
        r32 = 0;
    L_0x0238:
        r7.close();	 Catch:{ IOException -> 0x0434 }
    L_0x023b:
        if (r25 == 0) goto L_0x0240;
    L_0x023d:
        r25.close();
    L_0x0240:
        if (r32 == 0) goto L_0x0015;
    L_0x0242:
        if (r10 == 0) goto L_0x0015;
    L_0x0244:
        if (r11 <= 0) goto L_0x0015;
    L_0x0246:
        r38 = 0;
        r38 = (r26 > r38 ? 1 : (r26 == r38 ? 0 : -1));
        if (r38 <= 0) goto L_0x0015;
    L_0x024c:
        r38 = r44.isStarted();
        if (r38 == 0) goto L_0x0015;
    L_0x0252:
        r0 = (long) r11;
        r38 = r0;
        r0 = r38;
        r38 = r10.computeDuration(r0);
        r40 = 999999; // 0xf423f float:1.401297E-39 double:4.94065E-318;
        r38 = r38 + r40;
        r40 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r8 = r38 / r40;
        r38 = 0;
        r38 = (r8 > r38 ? 1 : (r8 == r38 ? 0 : -1));
        if (r38 <= 0) goto L_0x0015;
    L_0x026b:
        r38 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r8 = r8 + r38;
        r18 = 0;
        r0 = r44;
        r0 = r0.sync;
        r39 = r0;
        monitor-enter(r39);
    L_0x0278:
        r38 = r44.isStarted();	 Catch:{ all -> 0x03b4 }
        if (r38 == 0) goto L_0x028e;
    L_0x027e:
        r40 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x03b4 }
        r36 = r40 - r26;
        r38 = (r36 > r8 ? 1 : (r36 == r8 ? 0 : -1));
        if (r38 >= 0) goto L_0x028e;
    L_0x0288:
        r40 = 0;
        r38 = (r36 > r40 ? 1 : (r36 == r40 ? 0 : -1));
        if (r38 > 0) goto L_0x03a0;
    L_0x028e:
        monitor-exit(r39);	 Catch:{ all -> 0x03b4 }
        if (r18 == 0) goto L_0x0015;
    L_0x0291:
        r38 = java.lang.Thread.currentThread();
        r38.interrupt();
        goto L_0x0015;
    L_0x029a:
        r0 = r29;
        r0.setLength(r13);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = 0;
        r0 = r29;
        r1 = r38;
        r0.setOffset(r1);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = 0;
        r0 = r22;
        r1 = r38;
        r0.setLength(r1);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r38 = 0;
        r0 = r22;
        r1 = r38;
        r0.setOffset(r1);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        r0 = r25;
        r1 = r29;
        r2 = r22;
        r0.process(r1, r2);	 Catch:{ IOException -> 0x02c5, ResourceUnavailableException -> 0x035a }
        goto L_0x0189;
    L_0x02c5:
        r19 = move-exception;
        r38 = logger;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39 = new java.lang.StringBuilder;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39.<init>();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r40 = "Failed to read from audio stream ";
        r39 = r39.append(r40);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r0 = r44;
        r0 = r0.uri;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r40 = r0;
        r39 = r39.append(r40);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39 = r39.toString();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r0 = r38;
        r1 = r39;
        r2 = r19;
        r0.error(r1, r2);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r32 = 0;
    L_0x02ec:
        r7.close();	 Catch:{ IOException -> 0x0431 }
    L_0x02ef:
        if (r25 == 0) goto L_0x02f4;
    L_0x02f1:
        r25.close();
    L_0x02f4:
        if (r32 == 0) goto L_0x0015;
    L_0x02f6:
        if (r10 == 0) goto L_0x0015;
    L_0x02f8:
        if (r11 <= 0) goto L_0x0015;
    L_0x02fa:
        r38 = 0;
        r38 = (r26 > r38 ? 1 : (r26 == r38 ? 0 : -1));
        if (r38 <= 0) goto L_0x0015;
    L_0x0300:
        r38 = r44.isStarted();
        if (r38 == 0) goto L_0x0015;
    L_0x0306:
        r0 = (long) r11;
        r38 = r0;
        r0 = r38;
        r38 = r10.computeDuration(r0);
        r40 = 999999; // 0xf423f float:1.401297E-39 double:4.94065E-318;
        r38 = r38 + r40;
        r40 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r8 = r38 / r40;
        r38 = 0;
        r38 = (r8 > r38 ? 1 : (r8 == r38 ? 0 : -1));
        if (r38 <= 0) goto L_0x0015;
    L_0x031f:
        r38 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r8 = r8 + r38;
        r18 = 0;
        r0 = r44;
        r0 = r0.sync;
        r39 = r0;
        monitor-enter(r39);
    L_0x032c:
        r38 = r44.isStarted();	 Catch:{ all -> 0x039d }
        if (r38 == 0) goto L_0x0342;
    L_0x0332:
        r40 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x039d }
        r36 = r40 - r26;
        r38 = (r36 > r8 ? 1 : (r36 == r8 ? 0 : -1));
        if (r38 >= 0) goto L_0x0342;
    L_0x033c:
        r40 = 0;
        r38 = (r36 > r40 ? 1 : (r36 == r40 ? 0 : -1));
        if (r38 > 0) goto L_0x038b;
    L_0x0342:
        monitor-exit(r39);	 Catch:{ all -> 0x039d }
        if (r18 == 0) goto L_0x0015;
    L_0x0345:
        r38 = java.lang.Thread.currentThread();
        r38.interrupt();
        goto L_0x0015;
    L_0x034e:
        r38 = r24 & 2;
        r39 = 2;
        r0 = r38;
        r1 = r39;
        if (r0 == r1) goto L_0x0193;
    L_0x0358:
        goto L_0x0160;
    L_0x035a:
        r31 = move-exception;
        r38 = logger;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39 = new java.lang.StringBuilder;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39.<init>();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r40 = "Failed to open ";
        r39 = r39.append(r40);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r0 = r44;
        r0 = r0.renderer;	 Catch:{ ResourceUnavailableException -> 0x020d }
        r40 = r0;
        r40 = r40.getClass();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r40 = r40.getName();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39 = r39.append(r40);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r39 = r39.toString();	 Catch:{ ResourceUnavailableException -> 0x020d }
        r0 = r38;
        r1 = r39;
        r2 = r31;
        r0.error(r1, r2);	 Catch:{ ResourceUnavailableException -> 0x020d }
        r32 = 0;
        goto L_0x02ec;
    L_0x038b:
        r0 = r44;
        r0 = r0.sync;	 Catch:{ InterruptedException -> 0x0399 }
        r38 = r0;
        r0 = r38;
        r1 = r36;
        r0.wait(r1);	 Catch:{ InterruptedException -> 0x0399 }
        goto L_0x032c;
    L_0x0399:
        r17 = move-exception;
        r18 = 1;
        goto L_0x032c;
    L_0x039d:
        r38 = move-exception;
        monitor-exit(r39);	 Catch:{ all -> 0x039d }
        throw r38;
    L_0x03a0:
        r0 = r44;
        r0 = r0.sync;	 Catch:{ InterruptedException -> 0x03af }
        r38 = r0;
        r0 = r38;
        r1 = r36;
        r0.wait(r1);	 Catch:{ InterruptedException -> 0x03af }
        goto L_0x0278;
    L_0x03af:
        r17 = move-exception;
        r18 = 1;
        goto L_0x0278;
    L_0x03b4:
        r38 = move-exception;
        monitor-exit(r39);	 Catch:{ all -> 0x03b4 }
        throw r38;
    L_0x03b7:
        r38 = move-exception;
    L_0x03b8:
        r7.close();	 Catch:{ IOException -> 0x0437 }
    L_0x03bb:
        if (r25 == 0) goto L_0x03c0;
    L_0x03bd:
        r25.close();
    L_0x03c0:
        if (r32 == 0) goto L_0x0418;
    L_0x03c2:
        if (r10 == 0) goto L_0x0418;
    L_0x03c4:
        if (r11 <= 0) goto L_0x0418;
    L_0x03c6:
        r40 = 0;
        r39 = (r26 > r40 ? 1 : (r26 == r40 ? 0 : -1));
        if (r39 <= 0) goto L_0x0418;
    L_0x03cc:
        r39 = r44.isStarted();
        if (r39 == 0) goto L_0x0418;
    L_0x03d2:
        r0 = (long) r11;
        r40 = r0;
        r0 = r40;
        r40 = r10.computeDuration(r0);
        r42 = 999999; // 0xf423f float:1.401297E-39 double:4.94065E-318;
        r40 = r40 + r42;
        r42 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r8 = r40 / r42;
        r40 = 0;
        r39 = (r8 > r40 ? 1 : (r8 == r40 ? 0 : -1));
        if (r39 <= 0) goto L_0x0418;
    L_0x03eb:
        r40 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r8 = r8 + r40;
        r18 = 0;
        r0 = r44;
        r0 = r0.sync;
        r39 = r0;
        monitor-enter(r39);
    L_0x03f8:
        r40 = r44.isStarted();	 Catch:{ all -> 0x042b }
        if (r40 == 0) goto L_0x040e;
    L_0x03fe:
        r40 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x042b }
        r36 = r40 - r26;
        r40 = (r36 > r8 ? 1 : (r36 == r8 ? 0 : -1));
        if (r40 >= 0) goto L_0x040e;
    L_0x0408:
        r40 = 0;
        r40 = (r36 > r40 ? 1 : (r36 == r40 ? 0 : -1));
        if (r40 > 0) goto L_0x0419;
    L_0x040e:
        monitor-exit(r39);	 Catch:{ all -> 0x042b }
        if (r18 == 0) goto L_0x0418;
    L_0x0411:
        r39 = java.lang.Thread.currentThread();
        r39.interrupt();
    L_0x0418:
        throw r38;
    L_0x0419:
        r0 = r44;
        r0 = r0.sync;	 Catch:{ InterruptedException -> 0x0427 }
        r40 = r0;
        r0 = r40;
        r1 = r36;
        r0.wait(r1);	 Catch:{ InterruptedException -> 0x0427 }
        goto L_0x03f8;
    L_0x0427:
        r17 = move-exception;
        r18 = 1;
        goto L_0x03f8;
    L_0x042b:
        r38 = move-exception;
        monitor-exit(r39);	 Catch:{ all -> 0x042b }
        throw r38;
    L_0x042e:
        r39 = move-exception;
        goto L_0x0059;
    L_0x0431:
        r38 = move-exception;
        goto L_0x02ef;
    L_0x0434:
        r38 = move-exception;
        goto L_0x023b;
    L_0x0437:
        r39 = move-exception;
        goto L_0x03bb;
    L_0x0439:
        r38 = move-exception;
        r25 = r28;
        goto L_0x03b8;
    L_0x043e:
        r31 = move-exception;
        r25 = r28;
        goto L_0x020e;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.notify.AudioSystemClipImpl.runOnceInPlayThread():boolean");
    }
}
