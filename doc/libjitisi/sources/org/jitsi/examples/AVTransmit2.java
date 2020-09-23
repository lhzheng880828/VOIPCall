package org.jitsi.examples;

import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;

public class AVTransmit2 {
    private static final String[][] ARGS;
    private static final String LOCAL_PORT_BASE_ARG_NAME = "--local-port-base=";
    private static final String REMOTE_HOST_ARG_NAME = "--remote-host=";
    private static final String REMOTE_PORT_BASE_ARG_NAME = "--remote-port-base=";
    private int localPortBase;
    private MediaStream[] mediaStreams;
    private InetAddress remoteAddr;
    private int remotePortBase;

    private AVTransmit2(String localPortBase, String remoteHost, String remotePortBase) throws Exception {
        this.localPortBase = localPortBase == null ? -1 : Integer.valueOf(localPortBase).intValue();
        this.remoteAddr = InetAddress.getByName(remoteHost);
        this.remotePortBase = Integer.valueOf(remotePortBase).intValue();
    }

    private String start() throws Exception {
        MediaStream mediaStream;
        MediaType[] mediaTypes = MediaType.values();
        MediaService mediaService = LibJitsi.getMediaService();
        int localPort = this.localPortBase;
        int remotePort = this.remotePortBase;
        this.mediaStreams = new MediaStream[mediaTypes.length];
        MediaType[] arr$ = mediaTypes;
        int len$ = arr$.length;
        int i$ = 0;
        int remotePort2 = remotePort;
        int localPort2 = localPort;
        while (i$ < len$) {
            String encoding;
            double clockRate;
            byte dynamicRTPPayloadType;
            StreamConnector connector;
            MediaType mediaType = arr$[i$];
            MediaDevice device = mediaService.getDefaultDevice(mediaType, MediaUseCase.CALL);
            mediaStream = mediaService.createMediaStream(device);
            mediaStream.setDirection(MediaDirection.SENDONLY);
            switch (device.getMediaType()) {
                case AUDIO:
                    encoding = "PCMU";
                    clockRate = 8000.0d;
                    dynamicRTPPayloadType = (byte) -1;
                    break;
                case VIDEO:
                    encoding = "H264";
                    clockRate = -1.0d;
                    dynamicRTPPayloadType = (byte) 99;
                    break;
                default:
                    encoding = null;
                    clockRate = -1.0d;
                    dynamicRTPPayloadType = (byte) -1;
                    break;
            }
            if (encoding != null) {
                MediaFormat format = mediaService.getFormatFactory().createMediaFormat(encoding, clockRate);
                if (dynamicRTPPayloadType != (byte) -1) {
                    mediaStream.addDynamicRTPPayloadType(dynamicRTPPayloadType, format);
                }
                mediaStream.setFormat(format);
            }
            if (this.localPortBase == -1) {
                connector = new DefaultStreamConnector();
            } else {
                localPort = localPort2 + 1;
                int localRTPPort = localPort2;
                localPort2 = localPort + 1;
                connector = new DefaultStreamConnector(new DatagramSocket(localRTPPort), new DatagramSocket(localPort));
            }
            localPort = localPort2;
            mediaStream.setConnector(connector);
            remotePort = remotePort2 + 1;
            int remoteRTPPort = remotePort2;
            remotePort2 = remotePort + 1;
            mediaStream.setTarget(new MediaStreamTarget(new InetSocketAddress(this.remoteAddr, remoteRTPPort), new InetSocketAddress(this.remoteAddr, remotePort)));
            mediaStream.setName(mediaType.toString());
            this.mediaStreams[mediaType.ordinal()] = mediaStream;
            i$++;
            localPort2 = localPort;
        }
        for (MediaStream mediaStream2 : this.mediaStreams) {
            if (mediaStream2 != null) {
                mediaStream2.start();
            }
        }
        return null;
    }

    private void stop() {
        if (this.mediaStreams != null) {
            for (int i = 0; i < this.mediaStreams.length; i++) {
                MediaStream mediaStream = this.mediaStreams[i];
                if (mediaStream != null) {
                    try {
                        mediaStream.stop();
                    } finally {
                        mediaStream.close();
                        this.mediaStreams[i] = null;
                    }
                }
            }
            this.mediaStreams = null;
        }
    }

    static {
        r0 = new String[3][];
        r0[0] = new String[]{LOCAL_PORT_BASE_ARG_NAME, "The port which is the source of the transmission i.e. from which the media is to be transmitted. The specified value will be used as the port to transmit the audio RTP from, the next port after it will be used to transmit the audio RTCP from. Respectively, the subsequent ports will be used to transmit the video RTP and RTCP from."};
        r0[1] = new String[]{REMOTE_HOST_ARG_NAME, "The name of the host which is the target of the transmission i.e. to which the media is to be transmitted"};
        r0[2] = new String[]{REMOTE_PORT_BASE_ARG_NAME, "The port which is the target of the transmission i.e. to which the media is to be transmitted. The specified value will be used as the port to transmit the audio RTP to the next port after it will be used to transmit the audio RTCP to. Respectively, the subsequent ports will be used to transmit the video RTP and RTCP to."};
        ARGS = r0;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            prUsage();
            return;
        }
        Map<String, String> argMap = parseCommandLineArgs(args);
        LibJitsi.start();
        try {
            AVTransmit2 at = new AVTransmit2((String) argMap.get(LOCAL_PORT_BASE_ARG_NAME), (String) argMap.get(REMOTE_HOST_ARG_NAME), (String) argMap.get(REMOTE_PORT_BASE_ARG_NAME));
            String result = at.start();
            if (result == null) {
                System.err.println("Start transmission for 60 seconds...");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
                at.stop();
                System.err.println("...transmission ended.");
            } else {
                System.err.println("Error : " + result);
            }
            LibJitsi.stop();
        } catch (Throwable th) {
            LibJitsi.stop();
        }
    }

    static Map<String, String> parseCommandLineArgs(String[] args) {
        Map<String, String> argMap = new HashMap();
        for (String arg : args) {
            String key;
            String value;
            int keyEndIndex = arg.indexOf(61);
            if (keyEndIndex == -1) {
                key = arg;
                value = null;
            } else {
                key = arg.substring(0, keyEndIndex + 1);
                value = arg.substring(keyEndIndex + 1);
            }
            argMap.put(key, value);
        }
        return argMap;
    }

    private static void prUsage() {
        PrintStream err = System.err;
        err.println("Usage: " + AVTransmit2.class.getName() + " <args>");
        err.println("Valid args:");
        for (String[] arg : ARGS) {
            err.println("  " + arg[0] + " " + arg[1]);
        }
    }
}
