package org.xmpp.jnodes.smack;

import org.jivesoftware.smack.provider.IQProvider;

public class JingleNodesProvider implements IQProvider {
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
    public org.xmpp.jnodes.smack.JingleChannelIQ parseIQ(org.jitsi.org.xmlpull.v1.XmlPullParser r14) throws java.lang.Exception {
        /*
        r13 = this;
        r12 = 0;
        r5 = 0;
        r0 = 0;
        r6 = r5;
    L_0x0004:
        if (r0 != 0) goto L_0x0081;
    L_0x0006:
        r3 = r14.getEventType();
        r2 = r14.getName();
        r7 = r14.getNamespace();
        r11 = 2;
        if (r3 != r11) goto L_0x007b;
    L_0x0015:
        r11 = "channel";
        r11 = r2.equals(r11);
        if (r11 == 0) goto L_0x0089;
    L_0x001d:
        r11 = "http://jabber.org/protocol/jinglenodes#channel";
        r11 = r7.equals(r11);
        if (r11 == 0) goto L_0x0089;
    L_0x0025:
        r11 = "protocol";
        r10 = r14.getAttributeValue(r12, r11);
        r11 = "localport";
        r8 = r14.getAttributeValue(r12, r11);
        r11 = "remoteport";
        r9 = r14.getAttributeValue(r12, r11);
        r11 = "host";
        r4 = r14.getAttributeValue(r12, r11);
        r5 = new org.xmpp.jnodes.smack.JingleChannelIQ;	 Catch:{ IllegalFormatException -> 0x006f, NumberFormatException -> 0x0075 }
        r5.m2566init();	 Catch:{ IllegalFormatException -> 0x006f, NumberFormatException -> 0x0075 }
        if (r10 != 0) goto L_0x0046;
    L_0x0044:
        r10 = "udp";
    L_0x0046:
        r5.setProtocol(r10);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
        if (r4 == 0) goto L_0x004e;
    L_0x004b:
        r5.setHost(r4);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
    L_0x004e:
        if (r8 == 0) goto L_0x005b;
    L_0x0050:
        r11 = java.lang.Integer.valueOf(r8);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
        r11 = r11.intValue();	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
        r5.setLocalport(r11);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
    L_0x005b:
        if (r9 == 0) goto L_0x0068;
    L_0x005d:
        r11 = java.lang.Integer.valueOf(r9);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
        r11 = r11.intValue();	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
        r5.setRemoteport(r11);	 Catch:{ IllegalFormatException -> 0x0084, NumberFormatException -> 0x0082 }
    L_0x0068:
        if (r0 != 0) goto L_0x0086;
    L_0x006a:
        r14.next();
        r6 = r5;
        goto L_0x0004;
    L_0x006f:
        r1 = move-exception;
        r5 = r6;
    L_0x0071:
        r1.printStackTrace();
        goto L_0x0068;
    L_0x0075:
        r1 = move-exception;
        r5 = r6;
    L_0x0077:
        r1.printStackTrace();
        goto L_0x0068;
    L_0x007b:
        r11 = 3;
        if (r3 != r11) goto L_0x0089;
    L_0x007e:
        r0 = 1;
        r5 = r6;
        goto L_0x0068;
    L_0x0081:
        return r6;
    L_0x0082:
        r1 = move-exception;
        goto L_0x0077;
    L_0x0084:
        r1 = move-exception;
        goto L_0x0071;
    L_0x0086:
        r6 = r5;
        goto L_0x0004;
    L_0x0089:
        r5 = r6;
        goto L_0x0068;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xmpp.jnodes.smack.JingleNodesProvider.parseIQ(org.jitsi.org.xmlpull.v1.XmlPullParser):org.xmpp.jnodes.smack.JingleChannelIQ");
    }
}
