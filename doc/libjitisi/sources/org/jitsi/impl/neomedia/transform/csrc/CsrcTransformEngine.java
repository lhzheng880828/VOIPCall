package org.jitsi.impl.neomedia.transform.csrc;

import java.util.Map;
import java.util.Map.Entry;
import org.jitsi.impl.neomedia.AudioMediaStreamImpl;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.TransformEngine;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.RTPExtension;

public class CsrcTransformEngine extends SinglePacketTransformer implements TransformEngine {
    private MediaDirection csrcAudioLevelDirection = MediaDirection.INACTIVE;
    private CsrcAudioLevelDispatcher csrcAudioLevelDispatcher = null;
    private byte csrcAudioLevelExtID = (byte) -1;
    private byte[] extensionBuff = null;
    private int extensionBuffLen = 0;
    /* access modifiers changed from: private|final */
    public final MediaStreamImpl mediaStream;

    private class CsrcAudioLevelDispatcher implements Runnable {
        private boolean isRunning;
        private long[] lastReportedLevels;

        private CsrcAudioLevelDispatcher() {
            this.isRunning = false;
            this.lastReportedLevels = null;
        }

        public void addLevels(long[] levels) {
            synchronized (this) {
                this.lastReportedLevels = levels;
                notifyAll();
            }
        }

        public void run() {
            this.isRunning = true;
            if (CsrcTransformEngine.this.mediaStream instanceof AudioMediaStreamImpl) {
                AudioMediaStreamImpl audioStream = (AudioMediaStreamImpl) CsrcTransformEngine.this.mediaStream;
                while (this.isRunning) {
                    synchronized (this) {
                        if (this.lastReportedLevels == null) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }
                        } else {
                            long[] audioLevels = this.lastReportedLevels;
                            this.lastReportedLevels = null;
                            if (audioLevels != null) {
                                audioStream.audioLevelsReceived(audioLevels);
                            }
                        }
                    }
                }
            }
        }

        public void stop() {
            synchronized (this) {
                this.lastReportedLevels = null;
                this.isRunning = false;
                notifyAll();
            }
        }
    }

    public CsrcTransformEngine(MediaStreamImpl mediaStream) {
        this.mediaStream = mediaStream;
        Map<Byte, RTPExtension> activeRTPExtensions = this.mediaStream.getActiveRTPExtensions();
        if (activeRTPExtensions != null && !activeRTPExtensions.isEmpty()) {
            for (Entry<Byte, RTPExtension> e : activeRTPExtensions.entrySet()) {
                RTPExtension rtpExtension = (RTPExtension) e.getValue();
                if (RTPExtension.CSRC_AUDIO_LEVEL_URN.equals(rtpExtension.getURI().toString())) {
                    Byte extID = (Byte) e.getKey();
                    setCsrcAudioLevelExtensionID(extID == null ? (byte) -1 : extID.byteValue(), rtpExtension.getDirection());
                }
            }
        }
    }

    public void close() {
        if (this.csrcAudioLevelDispatcher != null) {
            this.csrcAudioLevelDispatcher.stop();
        }
    }

    private byte[] createLevelExtensionBuffer(long[] csrcList) {
        int buffLen = csrcList.length + 1;
        int padLen = 4 - (buffLen % 4);
        if (padLen == 4) {
            padLen = 0;
        }
        byte[] extensionBuff = getExtensionBuff(buffLen + padLen);
        extensionBuff[0] = (byte) ((this.csrcAudioLevelExtID << 4) | (csrcList.length - 1));
        int csrcOffset = 1;
        for (long csrc : csrcList) {
            extensionBuff[csrcOffset] = (byte) ((AudioMediaStreamImpl) this.mediaStream).getLastMeasuredAudioLevel(csrc);
            csrcOffset++;
        }
        return extensionBuff;
    }

    private byte[] getExtensionBuff(int ensureCapacity) {
        if (this.extensionBuff == null || this.extensionBuff.length < ensureCapacity) {
            this.extensionBuff = new byte[ensureCapacity];
        }
        this.extensionBuffLen = ensureCapacity;
        return this.extensionBuff;
    }

    public PacketTransformer getRTCPTransformer() {
        return null;
    }

    public PacketTransformer getRTPTransformer() {
        return this;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        if (this.csrcAudioLevelExtID > (byte) 0 && this.csrcAudioLevelDirection.allowsReceiving()) {
            long[] levels = pkt.extractCsrcAudioLevels(this.csrcAudioLevelExtID);
            if (levels != null) {
                if (this.csrcAudioLevelDispatcher == null) {
                    this.csrcAudioLevelDispatcher = new CsrcAudioLevelDispatcher();
                    new Thread(this.csrcAudioLevelDispatcher).start();
                }
                this.csrcAudioLevelDispatcher.addLevels(levels);
            }
        }
        return pkt;
    }

    public void setCsrcAudioLevelExtensionID(byte extID, MediaDirection dir) {
        this.csrcAudioLevelExtID = extID;
        this.csrcAudioLevelDirection = dir;
    }

    public synchronized RawPacket transform(RawPacket pkt) {
        if (!pkt.getExtensionBit()) {
            long[] csrcList = this.mediaStream.getLocalContributingSourceIDs();
            if (!(csrcList == null || csrcList.length == 0)) {
                pkt.setCsrcList(csrcList);
                if (this.csrcAudioLevelExtID > (byte) 0 && this.csrcAudioLevelDirection.allowsSending() && (this.mediaStream instanceof AudioMediaStreamImpl)) {
                    pkt.addExtension(createLevelExtensionBuffer(csrcList), this.extensionBuffLen);
                }
            }
        }
        return pkt;
    }
}
