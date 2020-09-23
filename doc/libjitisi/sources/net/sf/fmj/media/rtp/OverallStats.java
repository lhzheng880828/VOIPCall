package net.sf.fmj.media.rtp;

import javax.media.rtp.GlobalReceptionStats;

public class OverallStats implements GlobalReceptionStats {
    public static final int BADRTCPPACKET = 13;
    public static final int BADRTPPACKET = 2;
    public static final int BYTESRECD = 1;
    public static final int LOCALCOLL = 3;
    public static final int MALFORMEDBYE = 17;
    public static final int MALFORMEDRR = 15;
    public static final int MALFORMEDSDES = 16;
    public static final int MALFORMEDSR = 18;
    public static final int PACKETRECD = 0;
    public static final int PACKETSLOOPED = 5;
    public static final int REMOTECOLL = 4;
    public static final int RTCPRECD = 11;
    public static final int SRRECD = 12;
    public static final int TRANSMITFAILED = 6;
    public static final int UNKNOWNTYPE = 14;
    private int numBadRTCPPkts = 0;
    private int numBadRTPPkts = 0;
    private int numBytes = 0;
    private int numLocalColl = 0;
    private int numMalformedBye = 0;
    private int numMalformedRR = 0;
    private int numMalformedSDES = 0;
    private int numMalformedSR = 0;
    private int numPackets = 0;
    private int numPktsLooped = 0;
    private int numRTCPRecd = 0;
    private int numRemoteColl = 0;
    private int numSRRecd = 0;
    private int numTransmitFailed = 0;
    private int numUnknownTypes = 0;

    public int getBadRTCPPkts() {
        return this.numBadRTCPPkts;
    }

    public int getBadRTPkts() {
        return this.numBadRTPPkts;
    }

    public int getBytesRecd() {
        return this.numBytes;
    }

    public int getLocalColls() {
        return this.numLocalColl;
    }

    public int getMalformedBye() {
        return this.numMalformedBye;
    }

    public int getMalformedRR() {
        return this.numMalformedRR;
    }

    public int getMalformedSDES() {
        return this.numMalformedSDES;
    }

    public int getMalformedSR() {
        return this.numMalformedSR;
    }

    public int getPacketsLooped() {
        return this.numPktsLooped;
    }

    public int getPacketsRecd() {
        return this.numPackets;
    }

    public int getRemoteColls() {
        return this.numRemoteColl;
    }

    public int getRTCPRecd() {
        return this.numRTCPRecd;
    }

    public int getSRRecd() {
        return this.numSRRecd;
    }

    public int getTransmitFailed() {
        return this.numTransmitFailed;
    }

    public int getUnknownTypes() {
        return this.numUnknownTypes;
    }

    public String toString() {
        return "Packets Recd " + getPacketsRecd() + "\nBytes Recd " + getBytesRecd() + "\ngetBadRTP " + getBadRTPkts() + "\nLocalColl " + getLocalColls() + "\nRemoteColl " + getRemoteColls() + "\nPacketsLooped " + getPacketsLooped() + "\ngetTransmitFailed " + getTransmitFailed() + "\nRTCPRecd " + getTransmitFailed() + "\nSRRecd " + getSRRecd() + "\nBadRTCPPkts " + getBadRTCPPkts() + "\nUnknown " + getUnknownTypes() + "\nMalformedRR " + getMalformedRR() + "\nMalformedSDES " + getMalformedSDES() + "\nMalformedBye " + getMalformedBye() + "\nMalformedSR " + getMalformedSR();
    }

    public synchronized void update(int which, int num) {
        switch (which) {
            case 0:
                this.numPackets += num;
                break;
            case 1:
                this.numBytes += num;
                break;
            case 2:
                this.numBadRTPPkts += num;
                break;
            case 3:
                this.numLocalColl += num;
                break;
            case 4:
                this.numRemoteColl += num;
                break;
            case 5:
                this.numPktsLooped += num;
                break;
            case 6:
                this.numTransmitFailed += num;
                break;
            case 11:
                this.numRTCPRecd += num;
                break;
            case 12:
                this.numSRRecd += num;
                break;
            case 13:
                this.numBadRTPPkts += num;
                break;
            case 14:
                this.numUnknownTypes += num;
                break;
            case 15:
                this.numMalformedRR += num;
                break;
            case 16:
                this.numMalformedSDES += num;
                break;
            case 17:
                this.numMalformedBye += num;
                break;
            case 18:
                this.numMalformedSR += num;
                break;
        }
    }
}
