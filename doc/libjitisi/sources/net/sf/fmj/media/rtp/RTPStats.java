package net.sf.fmj.media.rtp;

import javax.media.rtp.ReceptionStats;

public class RTPStats implements ReceptionStats {
    public static final int ADUDROP = 9;
    public static final int ENCODE = 6;
    public static final int PAYLOAD = 5;
    public static final int PDUDROP = 8;
    public static final int PDUDUP = 4;
    public static final int PDUINVALID = 3;
    public static final int PDULOST = 0;
    public static final int PDUMISORD = 2;
    public static final int PDUPROCSD = 1;
    public static final int QSIZE = 7;
    private int ADUDrop = 0;
    private int PDUDrop = 0;
    private String encodeName;
    private int numDup = 0;
    private int numInvalid = 0;
    private int numLost = 0;
    private int numMisord = 0;
    private int numProc = 0;
    private int payload;
    private int qSize = 0;

    public int getADUDrop() {
        return this.ADUDrop;
    }

    public int getBufferSize() {
        return this.qSize;
    }

    public String getEncodingName() {
        return this.encodeName;
    }

    public int getPayloadType() {
        return this.payload;
    }

    public int getPDUDrop() {
        return this.PDUDrop;
    }

    public int getPDUDuplicate() {
        return this.numDup;
    }

    public int getPDUInvalid() {
        return this.numInvalid;
    }

    public int getPDUlost() {
        return this.numLost;
    }

    public int getPDUMisOrd() {
        return this.numMisord;
    }

    public int getPDUProcessed() {
        return this.numProc;
    }

    public String toString() {
        return "PDULost " + getPDUlost() + "\nPDUProcessed " + getPDUProcessed() + "\nPDUMisord " + getPDUMisOrd() + "\nPDUInvalid " + getPDUInvalid() + "\nPDUDuplicate " + getPDUDuplicate();
    }

    public synchronized void update(int which) {
        switch (which) {
            case 0:
                this.numLost++;
                break;
            case 1:
                this.numProc++;
                break;
            case 2:
                this.numMisord++;
                break;
            case 3:
                this.numInvalid++;
                break;
            case 4:
                this.numDup++;
                break;
            case 8:
                this.PDUDrop++;
                break;
        }
    }

    public synchronized void update(int which, int amount) {
        switch (which) {
            case 0:
                this.numLost += amount;
                break;
            case 5:
                this.payload = amount;
                break;
            case 7:
                this.qSize = amount;
                break;
            case 8:
                this.PDUDrop = amount;
                break;
            case 9:
                this.ADUDrop = amount;
                break;
        }
    }

    public synchronized void update(int which, String name) {
        if (which == 6) {
            this.encodeName = name;
        }
    }
}
