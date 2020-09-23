package org.jitsi.impl.neomedia.jmfext.media.rtp;

import net.sf.fmj.media.rtp.GenerateSSRCCause;
import org.jitsi.service.neomedia.SSRCFactory;

public class RTPSessionMgr extends net.sf.fmj.media.rtp.RTPSessionMgr {
    private SSRCFactory ssrcFactory;

    public SSRCFactory getSSRCFactory() {
        return this.ssrcFactory;
    }

    /* access modifiers changed from: protected */
    public long generateSSRC(GenerateSSRCCause cause) {
        SSRCFactory ssrcFactory = getSSRCFactory();
        if (ssrcFactory == null) {
            return super.generateSSRC(cause);
        }
        return ssrcFactory.generateSSRC(cause == null ? null : cause.name());
    }

    public void setSSRCFactory(SSRCFactory ssrcFactory) {
        this.ssrcFactory = ssrcFactory;
    }
}
