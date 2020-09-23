package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.dvcs.Data;

public abstract class DVCSRequestData {
    protected Data data;

    protected DVCSRequestData(Data data) {
        this.data = data;
    }

    public Data toASN1Structure() {
        return this.data;
    }
}
