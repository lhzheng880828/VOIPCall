package org.jitsi.impl.neomedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.format.ParameterizedVideoFormat;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.format.MediaFormat;

public class MediaUtils {
    public static final MediaFormat[] EMPTY_MEDIA_FORMATS = new MediaFormat[0];
    public static final int MAX_AUDIO_CHANNELS;
    public static final double MAX_AUDIO_SAMPLE_RATE;
    public static final int MAX_AUDIO_SAMPLE_SIZE_IN_BITS;
    private static final Map<String, String> jmfEncodingToEncodings = new HashMap();
    private static final Map<String, MediaFormat[]> rtpPayloadTypeStrToMediaFormats = new HashMap();
    private static final List<MediaFormat> rtpPayloadTypelessMediaFormats = new ArrayList();

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x01d7  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x02b0  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x02da  */
    /* JADX WARNING: Missing block: B:24:0x0199, code skipped:
            if (r41.getBoolean("net.java.sip.communicator.impl.neomedia.codec.video.h264.packetization-mode-1.enabled", true) != false) goto L_0x019b;
     */
    static {
        /*
        r2 = 0;
        r2 = new org.jitsi.service.neomedia.format.MediaFormat[r2];
        EMPTY_MEDIA_FORMATS = r2;
        r2 = new java.util.HashMap;
        r2.<init>();
        jmfEncodingToEncodings = r2;
        r2 = new java.util.ArrayList;
        r2.<init>();
        rtpPayloadTypelessMediaFormats = r2;
        r2 = new java.util.HashMap;
        r2.<init>();
        rtpPayloadTypeStrToMediaFormats = r2;
        r2 = 0;
        r3 = "PCMU";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "ULAW/rtp";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = org.jitsi.util.OSUtils.IS_LINUX32;
        if (r2 != 0) goto L_0x0037;
    L_0x0033:
        r2 = org.jitsi.util.OSUtils.IS_WINDOWS32;
        if (r2 == 0) goto L_0x0060;
    L_0x0037:
        r6 = new java.util.HashMap;
        r6.<init>();
        r2 = "annexa";
        r3 = "no";
        r6.put(r2, r3);
        r2 = "bitrate";
        r3 = "6.3";
        r6.put(r2, r3);
        r2 = 4;
        r3 = "G723";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "g723/rtp";
        r7 = 0;
        r8 = 1;
        r8 = new double[r8];
        r9 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r8[r9] = r12;
        addMediaFormats(r2, r3, r4, r5, r6, r7, r8);
    L_0x0060:
        r2 = 3;
        r3 = "GSM";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "gsm/rtp";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = 8;
        r3 = "PCMA";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "ALAW/rtp";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = -1;
        r3 = "iLBC";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "ilbc/rtp";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = -1;
        r3 = "speex";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "speex/rtp";
        r7 = 3;
        r7 = new double[r7];
        r7 = {4665518107723300864, 4670021707350671360, 4674525306978041856};
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = 9;
        r3 = "G722";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "g722/rtp";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r2 = -1;
        r3 = "telephone-event";
        r4 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r5 = "telephone-event";
        r7 = 1;
        r7 = new double[r7];
        r8 = 0;
        r12 = 4665518107723300864; // 0x40bf400000000000 float:0.0 double:8000.0;
        r7[r8] = r12;
        addMediaFormats(r2, r3, r4, r5, r7);
        r41 = org.jitsi.service.libjitsi.LibJitsi.getConfigurationService();
        r2 = "net.java.sip.communicator.impl.neomedia.codec.audio.silk.ADVERTISE_FEC";
        r3 = 0;
        r0 = r41;
        r37 = r0.getBoolean(r2, r3);
        r11 = new java.util.HashMap;
        r11.<init>();
        if (r37 == 0) goto L_0x00f6;
    L_0x00ef:
        r2 = "useinbandfec";
        r3 = "1";
        r11.put(r2, r3);
    L_0x00f6:
        r7 = -1;
        r8 = "SILK";
        r9 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r10 = "SILK/rtp";
        r12 = 0;
        r2 = 4;
        r13 = new double[r2];
        r13 = {4665518107723300864, 4667822684095119360, 4670021707350671360, 4672326283722489856};
        addMediaFormats(r7, r8, r9, r10, r11, r12, r13);
        r17 = new java.util.HashMap;
        r17.<init>();
        r2 = "net.java.sip.communicator.impl.neomedia.codec.audio.opus.encoder.FEC";
        r3 = 1;
        r0 = r41;
        r53 = r0.getBoolean(r2, r3);
        if (r53 != 0) goto L_0x0120;
    L_0x0117:
        r2 = "useinbandfec";
        r3 = "0";
        r0 = r17;
        r0.put(r2, r3);
    L_0x0120:
        r2 = "net.java.sip.communicator.impl.neomedia.codec.audio.opus.encoder.DTX";
        r3 = 1;
        r0 = r41;
        r52 = r0.getBoolean(r2, r3);
        if (r52 == 0) goto L_0x0134;
    L_0x012b:
        r2 = "usedtx";
        r3 = "1";
        r0 = r17;
        r0.put(r2, r3);
    L_0x0134:
        r12 = -1;
        r13 = "opus";
        r14 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r15 = "opus/rtp";
        r16 = 2;
        r18 = 0;
        r2 = 1;
        r0 = new double[r2];
        r19 = r0;
        r2 = 0;
        r4 = 4676829883349860352; // 0x40e7700000000000 float:0.0 double:48000.0;
        r19[r2] = r4;
        addMediaFormats(r12, r13, r14, r15, r16, r17, r18, r19);
        r22 = new java.util.HashMap;
        r22.<init>();
        r54 = "packetization-mode";
        r23 = new java.util.HashMap;
        r23.<init>();
        r59 = org.jitsi.impl.neomedia.device.ScreenDeviceImpl.getDefaultScreenDevice();
        if (r59 != 0) goto L_0x0293;
    L_0x0161:
        r55 = 0;
    L_0x0163:
        r2 = "imageattr";
        r3 = 0;
        r0 = r55;
        r3 = createImageAttr(r3, r0);
        r0 = r23;
        r0.put(r2, r3);
        if (r41 == 0) goto L_0x0185;
    L_0x0173:
        r2 = "net.java.sip.communicator.impl.neomedia.codec.video.h264.defaultProfile";
        r3 = "main";
        r0 = r41;
        r2 = r0.getString(r2, r3);
        r3 = "main";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x0299;
    L_0x0185:
        r2 = "profile-level-id";
        r3 = "4DE01f";
        r0 = r22;
        r0.put(r2, r3);
    L_0x018e:
        if (r41 == 0) goto L_0x019b;
    L_0x0190:
        r2 = "net.java.sip.communicator.impl.neomedia.codec.video.h264.packetization-mode-1.enabled";
        r3 = 1;
        r0 = r41;
        r2 = r0.getBoolean(r2, r3);
        if (r2 == 0) goto L_0x01b4;
    L_0x019b:
        r2 = "1";
        r0 = r22;
        r1 = r54;
        r0.put(r1, r2);
        r18 = -1;
        r19 = "H264";
        r20 = org.jitsi.service.neomedia.MediaType.VIDEO;
        r21 = "h264/rtp";
        r2 = 0;
        r0 = new double[r2];
        r24 = r0;
        addMediaFormats(r18, r19, r20, r21, r22, r23, r24);
    L_0x01b4:
        r0 = r22;
        r1 = r54;
        r0.remove(r1);
        r18 = -1;
        r19 = "H264";
        r20 = org.jitsi.service.neomedia.MediaType.VIDEO;
        r21 = "h264/rtp";
        r2 = 0;
        r0 = new double[r2];
        r24 = r0;
        addMediaFormats(r18, r19, r20, r21, r22, r23, r24);
        r28 = new java.util.HashMap;
        r28.<init>();
        r29 = new java.util.LinkedHashMap;
        r29.<init>();
        if (r55 == 0) goto L_0x0203;
    L_0x01d7:
        r2 = "CUSTOM";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r0 = r55;
        r4 = r0.width;
        r3 = r3.append(r4);
        r4 = ",";
        r3 = r3.append(r4);
        r0 = r55;
        r4 = r0.height;
        r3 = r3.append(r4);
        r4 = ",2";
        r3 = r3.append(r4);
        r3 = r3.toString();
        r0 = r28;
        r0.put(r2, r3);
    L_0x0203:
        r2 = "VGA";
        r3 = "2";
        r0 = r28;
        r0.put(r2, r3);
        r2 = "CIF";
        r3 = "1";
        r0 = r28;
        r0.put(r2, r3);
        r2 = "QCIF";
        r3 = "1";
        r0 = r28;
        r0.put(r2, r3);
        r24 = -1;
        r25 = "H263-1998";
        r26 = org.jitsi.service.neomedia.MediaType.VIDEO;
        r27 = "h263-1998/rtp";
        r2 = 0;
        r0 = new double[r2];
        r30 = r0;
        addMediaFormats(r24, r25, r26, r27, r28, r29, r30);
        r30 = -1;
        r31 = "VP8";
        r32 = org.jitsi.service.neomedia.MediaType.VIDEO;
        r33 = "VP8/rtp";
        r34 = 0;
        r35 = 0;
        r2 = 0;
        r0 = new double[r2];
        r36 = r0;
        addMediaFormats(r30, r31, r32, r33, r34, r35, r36);
        r40 = new java.util.ArrayList;
        r2 = rtpPayloadTypeStrToMediaFormats;
        r2 = r2.size();
        r3 = rtpPayloadTypelessMediaFormats;
        r3 = r3.size();
        r2 = r2 + r3;
        r0 = r40;
        r0.<init>(r2);
        r2 = rtpPayloadTypeStrToMediaFormats;
        r2 = r2.values();
        r43 = r2.iterator();
    L_0x0260:
        r2 = r43.hasNext();
        if (r2 == 0) goto L_0x02a4;
    L_0x0266:
        r51 = r43.next();
        r51 = (org.jitsi.service.neomedia.format.MediaFormat[]) r51;
        r38 = r51;
        r0 = r38;
        r0 = r0.length;
        r45 = r0;
        r44 = 0;
    L_0x0275:
        r0 = r44;
        r1 = r45;
        if (r0 >= r1) goto L_0x0260;
    L_0x027b:
        r50 = r38[r44];
        r2 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r3 = r50.getMediaType();
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x0290;
    L_0x0289:
        r0 = r40;
        r1 = r50;
        r0.add(r1);
    L_0x0290:
        r44 = r44 + 1;
        goto L_0x0275;
    L_0x0293:
        r55 = r59.getSize();
        goto L_0x0163;
    L_0x0299:
        r2 = "profile-level-id";
        r3 = "42E01f";
        r0 = r22;
        r0.put(r2, r3);
        goto L_0x018e;
    L_0x02a4:
        r2 = rtpPayloadTypelessMediaFormats;
        r43 = r2.iterator();
    L_0x02aa:
        r2 = r43.hasNext();
        if (r2 == 0) goto L_0x02ca;
    L_0x02b0:
        r50 = r43.next();
        r50 = (org.jitsi.service.neomedia.format.MediaFormat) r50;
        r2 = org.jitsi.service.neomedia.MediaType.AUDIO;
        r3 = r50.getMediaType();
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x02aa;
    L_0x02c2:
        r0 = r40;
        r1 = r50;
        r0.add(r1);
        goto L_0x02aa;
    L_0x02ca:
        r46 = -1;
        r48 = -4616189618054758400; // 0xbff0000000000000 float:0.0 double:-1.0;
        r47 = -1;
        r43 = r40.iterator();
    L_0x02d4:
        r2 = r43.hasNext();
        if (r2 == 0) goto L_0x031b;
    L_0x02da:
        r50 = r43.next();
        r50 = (org.jitsi.service.neomedia.format.MediaFormat) r50;
        r39 = r50;
        r39 = (org.jitsi.impl.neomedia.format.AudioMediaFormatImpl) r39;
        r42 = r39.getChannels();
        r2 = "opus";
        r3 = r39.getEncoding();
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x02f6;
    L_0x02f4:
        r42 = 1;
    L_0x02f6:
        r56 = r39.getClockRate();
        r2 = r39.getFormat();
        r2 = (javax.media.format.AudioFormat) r2;
        r58 = r2.getSampleSizeInBits();
        r0 = r46;
        r1 = r42;
        if (r0 >= r1) goto L_0x030c;
    L_0x030a:
        r46 = r42;
    L_0x030c:
        r2 = (r48 > r56 ? 1 : (r48 == r56 ? 0 : -1));
        if (r2 >= 0) goto L_0x0312;
    L_0x0310:
        r48 = r56;
    L_0x0312:
        r0 = r47;
        r1 = r58;
        if (r0 >= r1) goto L_0x02d4;
    L_0x0318:
        r47 = r58;
        goto L_0x02d4;
    L_0x031b:
        MAX_AUDIO_CHANNELS = r46;
        MAX_AUDIO_SAMPLE_RATE = r48;
        MAX_AUDIO_SAMPLE_SIZE_IN_BITS = r47;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.MediaUtils.m1819clinit():void");
    }

    private static void addMediaFormats(byte rtpPayloadType, String encoding, MediaType mediaType, String jmfEncoding, double... clockRates) {
        addMediaFormats(rtpPayloadType, encoding, mediaType, jmfEncoding, null, null, clockRates);
    }

    private static void addMediaFormats(byte rtpPayloadType, String encoding, MediaType mediaType, String jmfEncoding, Map<String, String> formatParameters, Map<String, String> advancedAttributes, double... clockRates) {
        addMediaFormats(rtpPayloadType, encoding, mediaType, jmfEncoding, 1, formatParameters, advancedAttributes, clockRates);
    }

    private static void addMediaFormats(byte rtpPayloadType, String encoding, MediaType mediaType, String jmfEncoding, int channels, Map<String, String> formatParameters, Map<String, String> advancedAttributes, double... clockRates) {
        int clockRateCount = clockRates.length;
        List<MediaFormat> arrayList = new ArrayList(clockRateCount);
        Format format;
        MediaFormat mediaFormat;
        double clockRate;
        if (clockRateCount > 0) {
            for (double clockRate2 : clockRates) {
                switch (mediaType) {
                    case AUDIO:
                        if (channels != 1) {
                            format = new AudioFormat(jmfEncoding, -1.0d, -1, channels);
                            break;
                        } else {
                            format = new AudioFormat(jmfEncoding);
                            break;
                        }
                    case VIDEO:
                        format = new ParameterizedVideoFormat(jmfEncoding, (Map) formatParameters);
                        break;
                    default:
                        throw new IllegalArgumentException("mediaType");
                }
                mediaFormat = MediaFormatImpl.createInstance(format, clockRate2, formatParameters, advancedAttributes);
                if (mediaFormat != null) {
                    arrayList.add(mediaFormat);
                }
            }
        } else {
            switch (mediaType) {
                case AUDIO:
                    Format audioFormat = new AudioFormat(jmfEncoding);
                    format = audioFormat;
                    clockRate2 = audioFormat.getSampleRate();
                    break;
                case VIDEO:
                    format = new ParameterizedVideoFormat(jmfEncoding, (Map) formatParameters);
                    clockRate2 = 90000.0d;
                    break;
                default:
                    throw new IllegalArgumentException("mediaType");
            }
            mediaFormat = MediaFormatImpl.createInstance(format, clockRate2, formatParameters, advancedAttributes);
            if (mediaFormat != null) {
                arrayList.add(mediaFormat);
            }
        }
        if (arrayList.size() > 0) {
            if ((byte) -1 == rtpPayloadType) {
                rtpPayloadTypelessMediaFormats.addAll(arrayList);
            } else {
                rtpPayloadTypeStrToMediaFormats.put(Byte.toString(rtpPayloadType), arrayList.toArray(EMPTY_MEDIA_FORMATS));
            }
            jmfEncodingToEncodings.put(((MediaFormatImpl) arrayList.get(0)).getJMFEncoding(), encoding);
        }
    }

    public static String createImageAttr(Dimension sendSize, Dimension maxRecvSize) {
        StringBuffer img = new StringBuffer();
        if (sendSize != null) {
            img.append("send [x=[0-");
            img.append((int) sendSize.getWidth());
            img.append("],y=[0-");
            img.append((int) sendSize.getHeight());
            img.append("]]");
        } else {
            img.append("send *");
        }
        if (maxRecvSize != null) {
            img.append(" recv [x=[0-");
            img.append((int) maxRecvSize.getWidth());
            img.append("],y=[0-");
            img.append((int) maxRecvSize.getHeight());
            img.append("]]");
        } else {
            img.append(" recv *");
        }
        return img.toString();
    }

    public static MediaFormat getMediaFormat(Format format) {
        double clockRate;
        if (format instanceof AudioFormat) {
            clockRate = ((AudioFormat) format).getSampleRate();
        } else if (format instanceof VideoFormat) {
            clockRate = 90000.0d;
        } else {
            clockRate = -1.0d;
        }
        byte rtpPayloadType = getRTPPayloadType(format.getEncoding(), clockRate);
        if ((byte) -1 != rtpPayloadType) {
            for (MediaFormat mediaFormat : getMediaFormats(rtpPayloadType)) {
                if (format.matches(((MediaFormatImpl) mediaFormat).getFormat())) {
                    return mediaFormat;
                }
            }
        }
        return null;
    }

    public static MediaFormat getMediaFormat(String encoding, double clockRate) {
        return getMediaFormat(encoding, clockRate, null);
    }

    public static MediaFormat getMediaFormat(String encoding, double clockRate, Map<String, String> fmtps) {
        for (MediaFormat format : getMediaFormats(encoding)) {
            if (format.getClockRate() == clockRate && format.formatParametersMatch(fmtps)) {
                return format;
            }
        }
        return null;
    }

    public static int getMediaFormatIndex(MediaFormat mediaFormat) {
        return rtpPayloadTypelessMediaFormats.indexOf(mediaFormat);
    }

    public static MediaFormat[] getMediaFormats(byte rtpPayloadType) {
        MediaFormat[] mediaFormats = (MediaFormat[]) rtpPayloadTypeStrToMediaFormats.get(Byte.toString(rtpPayloadType));
        return mediaFormats == null ? EMPTY_MEDIA_FORMATS : (MediaFormat[]) mediaFormats.clone();
    }

    public static MediaFormat[] getMediaFormats(MediaType mediaType) {
        List<MediaFormat> mediaFormats = new ArrayList();
        for (MediaFormat[] arr$ : rtpPayloadTypeStrToMediaFormats.values()) {
            for (MediaFormat format : (MediaFormat[]) r3.next()) {
                if (format.getMediaType().equals(mediaType)) {
                    mediaFormats.add(format);
                }
            }
        }
        for (MediaFormat format2 : rtpPayloadTypelessMediaFormats) {
            if (format2.getMediaType().equals(mediaType)) {
                mediaFormats.add(format2);
            }
        }
        return (MediaFormat[]) mediaFormats.toArray(EMPTY_MEDIA_FORMATS);
    }

    public static List<MediaFormat> getMediaFormats(String encoding) {
        String jmfEncoding = null;
        for (Entry<String, String> jmfEncodingToEncoding : jmfEncodingToEncodings.entrySet()) {
            if (((String) jmfEncodingToEncoding.getValue()).equals(encoding)) {
                jmfEncoding = (String) jmfEncodingToEncoding.getKey();
                break;
            }
        }
        List<MediaFormat> mediaFormats = new ArrayList();
        if (jmfEncoding != null) {
            for (MediaFormat[] arr$ : rtpPayloadTypeStrToMediaFormats.values()) {
                for (MediaFormat rtpPayloadTypeMediaFormat : (MediaFormat[]) i$.next()) {
                    if (((MediaFormatImpl) rtpPayloadTypeMediaFormat).getJMFEncoding().equals(jmfEncoding)) {
                        mediaFormats.add(rtpPayloadTypeMediaFormat);
                    }
                }
            }
            if (mediaFormats.size() < 1) {
                for (MediaFormat rtpPayloadTypelessMediaFormat : rtpPayloadTypelessMediaFormats) {
                    if (((MediaFormatImpl) rtpPayloadTypelessMediaFormat).getJMFEncoding().equals(jmfEncoding)) {
                        mediaFormats.add(rtpPayloadTypelessMediaFormat);
                    }
                }
            }
        }
        return mediaFormats;
    }

    public static byte getRTPPayloadType(String jmfEncoding, double clockRate) {
        if (jmfEncoding == null) {
            return (byte) -1;
        }
        if (jmfEncoding.equals(AudioFormat.ULAW_RTP)) {
            return (byte) 0;
        }
        if (jmfEncoding.equals("ALAW/rtp")) {
            return (byte) 8;
        }
        if (jmfEncoding.equals(AudioFormat.GSM_RTP)) {
            return (byte) 3;
        }
        if (jmfEncoding.equals(AudioFormat.G723_RTP)) {
            return (byte) 4;
        }
        if (jmfEncoding.equals(AudioFormat.DVI_RTP) && clockRate == 8000.0d) {
            return (byte) 5;
        }
        if (jmfEncoding.equals(AudioFormat.DVI_RTP) && clockRate == 16000.0d) {
            return (byte) 6;
        }
        if (jmfEncoding.equals(AudioFormat.ALAW)) {
            return (byte) 8;
        }
        if (jmfEncoding.equals(Constants.G722)) {
            return (byte) 9;
        }
        if (jmfEncoding.equals(Constants.G722_RTP)) {
            return (byte) 9;
        }
        if (jmfEncoding.equals("gsm")) {
            return (byte) 3;
        }
        if (jmfEncoding.equals(AudioFormat.GSM_RTP)) {
            return (byte) 3;
        }
        if (jmfEncoding.equals(AudioFormat.G728_RTP)) {
            return (byte) 15;
        }
        if (jmfEncoding.equals(AudioFormat.G729_RTP)) {
            return (byte) 18;
        }
        if (jmfEncoding.equals(VideoFormat.H263_RTP)) {
            return (byte) 34;
        }
        if (jmfEncoding.equals(VideoFormat.JPEG_RTP)) {
            return (byte) 26;
        }
        if (jmfEncoding.equals(VideoFormat.H261_RTP)) {
            return (byte) 31;
        }
        return (byte) -1;
    }

    public static String jmfEncodingToEncoding(String jmfEncoding) {
        return (String) jmfEncodingToEncodings.get(jmfEncoding);
    }
}
