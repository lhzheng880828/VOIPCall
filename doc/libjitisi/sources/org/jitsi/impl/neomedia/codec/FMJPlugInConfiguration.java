package org.jitsi.impl.neomedia.codec;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import javax.media.Codec;
import javax.media.PackageManager;
import javax.media.PlugInManager;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public class FMJPlugInConfiguration {
    private static final String[] CUSTOM_CODECS;
    private static final String[] CUSTOM_PACKAGES = new String[]{"org.jitsi.impl.neomedia.jmfext", "net.java.sip.communicator.impl.neomedia.jmfext", "net.sf.fmj"};
    private static boolean codecsRegistered = false;
    private static final Logger logger = Logger.getLogger(FMJPlugInConfiguration.class);
    private static boolean packagesRegistered = false;

    static {
        String str;
        String str2 = null;
        String[] strArr = new String[39];
        if (OSUtils.IS_ANDROID) {
            str = "org.jitsi.impl.neomedia.codec.video.AndroidEncoder";
        } else {
            str = null;
        }
        strArr[0] = str;
        if (OSUtils.IS_ANDROID) {
            str = "org.jitsi.impl.neomedia.codec.video.AndroidDecoder";
        } else {
            str = null;
        }
        strArr[1] = str;
        strArr[2] = "org.jitsi.impl.neomedia.codec.audio.alaw.DePacketizer";
        strArr[3] = "org.jitsi.impl.neomedia.codec.audio.alaw.JavaEncoder";
        strArr[4] = "org.jitsi.impl.neomedia.codec.audio.alaw.Packetizer";
        strArr[5] = "org.jitsi.impl.neomedia.codec.audio.ulaw.JavaDecoder";
        strArr[6] = "org.jitsi.impl.neomedia.codec.audio.ulaw.JavaEncoder";
        strArr[7] = "org.jitsi.impl.neomedia.codec.audio.ulaw.Packetizer";
        strArr[8] = "org.jitsi.impl.neomedia.codec.audio.opus.JNIDecoder";
        strArr[9] = "org.jitsi.impl.neomedia.codec.audio.opus.JNIEncoder";
        strArr[10] = "org.jitsi.impl.neomedia.codec.audio.speex.JNIDecoder";
        strArr[11] = "org.jitsi.impl.neomedia.codec.audio.speex.JNIEncoder";
        strArr[12] = "org.jitsi.impl.neomedia.codec.audio.speex.SpeexResampler";
        if (OSUtils.IS_ANDROID) {
            str = null;
        } else {
            str = "org.jitsi.impl.neomedia.codec.audio.mp3.JNIEncoder";
        }
        strArr[13] = str;
        strArr[14] = "org.jitsi.impl.neomedia.codec.audio.ilbc.JavaDecoder";
        strArr[15] = "org.jitsi.impl.neomedia.codec.audio.ilbc.JavaEncoder";
        strArr[16] = null;
        strArr[17] = null;
        strArr[18] = "net.java.sip.communicator.impl.neomedia.codec.audio.g722.JNIDecoder";
        strArr[19] = "net.java.sip.communicator.impl.neomedia.codec.audio.g722.JNIEncoder";
        strArr[20] = "org.jitsi.impl.neomedia.codec.audio.gsm.Decoder";
        strArr[21] = "org.jitsi.impl.neomedia.codec.audio.gsm.Encoder";
        strArr[22] = "org.jitsi.impl.neomedia.codec.audio.gsm.DePacketizer";
        strArr[23] = "org.jitsi.impl.neomedia.codec.audio.gsm.Packetizer";
        strArr[24] = "org.jitsi.impl.neomedia.codec.audio.silk.JavaDecoder";
        strArr[25] = "org.jitsi.impl.neomedia.codec.audio.silk.JavaEncoder";
        strArr[26] = "org.jitsi.impl.neomedia.codec.video.h263p.DePacketizer";
        strArr[27] = "org.jitsi.impl.neomedia.codec.video.h263p.JNIDecoder";
        strArr[28] = "org.jitsi.impl.neomedia.codec.video.h263p.JNIEncoder";
        strArr[29] = "org.jitsi.impl.neomedia.codec.video.h263p.Packetizer";
        strArr[30] = "org.jitsi.impl.neomedia.codec.video.h264.DePacketizer";
        strArr[31] = "org.jitsi.impl.neomedia.codec.video.h264.JNIDecoder";
        strArr[32] = "org.jitsi.impl.neomedia.codec.video.h264.JNIEncoder";
        strArr[33] = "org.jitsi.impl.neomedia.codec.video.h264.Packetizer";
        strArr[34] = "org.jitsi.impl.neomedia.codec.video.SwScale";
        strArr[35] = "org.jitsi.impl.neomedia.codec.video.vp8.Packetizer";
        strArr[36] = "org.jitsi.impl.neomedia.codec.video.vp8.DePacketizer";
        if (OSUtils.IS_ANDROID) {
            str = null;
        } else {
            str = "org.jitsi.impl.neomedia.codec.video.vp8.VPXEncoder";
        }
        strArr[37] = str;
        if (!OSUtils.IS_ANDROID) {
            str2 = "org.jitsi.impl.neomedia.codec.video.vp8.VPXDecoder";
        }
        strArr[38] = str2;
        CUSTOM_CODECS = strArr;
    }

    public static void registerCustomCodecs() {
        if (!codecsRegistered) {
            String className;
            Collection<String> hashSet = new HashSet(PlugInManager.getPlugInList(null, null, 2));
            boolean commit = false;
            PlugInManager.removePlugIn("com.sun.media.codec.video.colorspace.JavaRGBToYUV", 2);
            PlugInManager.removePlugIn("com.sun.media.codec.video.colorspace.JavaRGBConverter", 2);
            PlugInManager.removePlugIn("com.sun.media.codec.video.colorspace.RGBScaler", 2);
            PlugInManager.removePlugIn("com.sun.media.codec.video.vh263.NativeDecoder", 2);
            PlugInManager.removePlugIn("com.ibm.media.codec.video.h263.NativeEncoder", 2);
            String gsmCodecPackage = "com.ibm.media.codec.audio.gsm.";
            for (String gsmCodecClass : new String[]{"JavaDecoder", "JavaDecoder_ms", "JavaEncoder", "JavaEncoder_ms", "NativeDecoder", "NativeDecoder_ms", "NativeEncoder", "NativeEncoder_ms", "Packetizer"}) {
                PlugInManager.removePlugIn(gsmCodecPackage + gsmCodecClass, 2);
            }
            PlugInManager.removePlugIn("net.sf.fmj.media.codec.JavaSoundCodec", 2);
            for (String className2 : CUSTOM_CODECS) {
                if (className2 != null) {
                    if (!hashSet.contains(className2)) {
                        boolean registered;
                        commit = true;
                        Throwable exception = null;
                        try {
                            Codec codec = (Codec) Class.forName(className2).newInstance();
                            registered = PlugInManager.addPlugIn(className2, codec.getSupportedInputFormats(), codec.getSupportedOutputFormats(null), 2);
                        } catch (Throwable ex) {
                            registered = false;
                            exception = ex;
                        }
                        if (!registered) {
                            logger.warn("Codec " + className2 + " is NOT successfully registered", exception);
                        } else if (logger.isTraceEnabled()) {
                            logger.trace("Codec " + className2 + " is successfully registered");
                        }
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Codec " + className2 + " is already registered");
                    }
                }
            }
            Vector<String> codecs = PlugInManager.getPlugInList(null, null, 2);
            if (codecs != null) {
                boolean setPlugInList = false;
                for (int i = CUSTOM_CODECS.length - 1; i >= 0; i--) {
                    className2 = CUSTOM_CODECS[i];
                    if (className2 != null) {
                        int classNameIndex = codecs.indexOf(className2);
                        if (classNameIndex != -1) {
                            codecs.remove(classNameIndex);
                            codecs.add(0, className2);
                            setPlugInList = true;
                        }
                    }
                }
                if (setPlugInList) {
                    PlugInManager.setPlugInList(codecs, 2);
                }
            }
            if (commit && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
                try {
                    PlugInManager.commit();
                } catch (IOException ex2) {
                    logger.error("Cannot commit to PlugInManager", ex2);
                }
            }
            codecsRegistered = true;
        }
    }

    public static void registerCustomPackages() {
        if (!packagesRegistered) {
            Vector<String> packages = PackageManager.getProtocolPrefixList();
            boolean loggerIsDebugEnabled = logger.isDebugEnabled();
            for (int i = CUSTOM_PACKAGES.length - 1; i >= 0; i--) {
                String customPackage = CUSTOM_PACKAGES[i];
                if (!packages.contains(customPackage)) {
                    packages.add(0, customPackage);
                    if (loggerIsDebugEnabled) {
                        logger.debug("Adding package  : " + customPackage);
                    }
                }
            }
            PackageManager.setProtocolPrefixList(packages);
            PackageManager.commitProtocolPrefixList();
            if (loggerIsDebugEnabled) {
                logger.debug("Registering new protocol prefix list: " + packages);
            }
            packagesRegistered = true;
        }
    }
}
