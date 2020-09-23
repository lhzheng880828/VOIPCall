package net.sf.fmj.media.multiplexer;

import javax.media.Format;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.media.rtp.FormatInfo;
import net.sf.fmj.media.rtp.RTPSessionMgr;

public class RTPSyncBufferMux extends RawSyncBufferMux {
    FormatInfo rtpFormats;

    public RTPSyncBufferMux() {
        this.rtpFormats = new FormatInfo();
        this.supported = new ContentDescriptor[1];
        this.supported[0] = new ContentDescriptor(ContentDescriptor.RAW_RTP);
        this.monoIncrTime = true;
    }

    public String getName() {
        return "RTP Sync Buffer Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID) {
        if (RTPSessionMgr.formatSupported(input)) {
            return super.setInputFormat(input, trackID);
        }
        return null;
    }
}
