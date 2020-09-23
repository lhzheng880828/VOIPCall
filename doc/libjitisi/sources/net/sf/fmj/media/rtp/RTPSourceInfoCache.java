package net.sf.fmj.media.rtp;

import java.util.Hashtable;

public class RTPSourceInfoCache {
    Hashtable cache = new Hashtable(20);
    RTPSourceInfoCache main;
    public SSRCCache ssrccache;

    /* JADX WARNING: Missing block: B:15:0x002d, code skipped:
            return r1;
     */
    public net.sf.fmj.media.rtp.RTPSourceInfo get(java.lang.String r5, boolean r6) {
        /*
        r4 = this;
        r1 = 0;
        monitor-enter(r4);
        r3 = r4.cache;	 Catch:{ all -> 0x002e }
        r3 = r3.get(r5);	 Catch:{ all -> 0x002e }
        r0 = r3;
        r0 = (net.sf.fmj.media.rtp.RTPSourceInfo) r0;	 Catch:{ all -> 0x002e }
        r1 = r0;
        if (r1 != 0) goto L_0x0036;
    L_0x000e:
        if (r6 != 0) goto L_0x0036;
    L_0x0010:
        r2 = new net.sf.fmj.media.rtp.RTPRemoteSourceInfo;	 Catch:{ all -> 0x002e }
        r3 = r4.main;	 Catch:{ all -> 0x002e }
        r2.m861init(r5, r3);	 Catch:{ all -> 0x002e }
        r3 = r4.cache;	 Catch:{ all -> 0x0031 }
        r3.put(r5, r2);	 Catch:{ all -> 0x0031 }
    L_0x001c:
        if (r2 != 0) goto L_0x0034;
    L_0x001e:
        if (r6 == 0) goto L_0x0034;
    L_0x0020:
        r1 = new net.sf.fmj.media.rtp.RTPLocalSourceInfo;	 Catch:{ all -> 0x0031 }
        r3 = r4.main;	 Catch:{ all -> 0x0031 }
        r1.m845init(r5, r3);	 Catch:{ all -> 0x0031 }
        r3 = r4.cache;	 Catch:{ all -> 0x002e }
        r3.put(r5, r1);	 Catch:{ all -> 0x002e }
    L_0x002c:
        monitor-exit(r4);	 Catch:{ all -> 0x002e }
        return r1;
    L_0x002e:
        r3 = move-exception;
    L_0x002f:
        monitor-exit(r4);	 Catch:{ all -> 0x002e }
        throw r3;
    L_0x0031:
        r3 = move-exception;
        r1 = r2;
        goto L_0x002f;
    L_0x0034:
        r1 = r2;
        goto L_0x002c;
    L_0x0036:
        r2 = r1;
        goto L_0x001c;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.rtp.RTPSourceInfoCache.get(java.lang.String, boolean):net.sf.fmj.media.rtp.RTPSourceInfo");
    }

    public Hashtable getCacheTable() {
        return this.cache;
    }

    public RTPSourceInfoCache getMainCache() {
        if (this.main == null) {
            this.main = new RTPSourceInfoCache();
        }
        return this.main;
    }

    public void remove(String cname) {
        this.cache.remove(cname);
    }

    public void setMainCache(RTPSourceInfoCache main) {
        this.main = main;
    }

    public void setSSRCCache(SSRCCache ssrccache) {
        this.main.ssrccache = ssrccache;
    }
}
