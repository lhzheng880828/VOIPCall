package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Date;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailIQ;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.IncomingFileTransferRequest;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.event.FileTransferRequestEvent;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

public class IncomingFileTransferRequestJabberImpl implements IncomingFileTransferRequest {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(IncomingFileTransferRequestJabberImpl.class);
    /* access modifiers changed from: private|final */
    public final OperationSetFileTransferJabberImpl fileTransferOpSet;
    private final FileTransferRequest fileTransferRequest;
    private String id;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    private Contact sender;
    /* access modifiers changed from: private */
    public byte[] thumbnail;
    /* access modifiers changed from: private */
    public String thumbnailCid;

    private class ThumbnailResponseListener implements PacketListener {
        private ThumbnailResponseListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof ThumbnailIQ) {
                if (IncomingFileTransferRequestJabberImpl.logger.isDebugEnabled()) {
                    IncomingFileTransferRequestJabberImpl.logger.debug("Thumbnail response received.");
                }
                ThumbnailIQ thumbnailResponse = (ThumbnailIQ) packet;
                if (thumbnailResponse.getCid() != null && thumbnailResponse.getCid().equals(IncomingFileTransferRequestJabberImpl.this.thumbnailCid)) {
                    IncomingFileTransferRequestJabberImpl.this.thumbnail = thumbnailResponse.getData();
                    IncomingFileTransferRequestJabberImpl.this.fileTransferOpSet.fireFileTransferRequest(new FileTransferRequestEvent(IncomingFileTransferRequestJabberImpl.this.fileTransferOpSet, IncomingFileTransferRequestJabberImpl.this, new Date()));
                }
                if (IncomingFileTransferRequestJabberImpl.this.jabberProvider.getConnection() != null) {
                    IncomingFileTransferRequestJabberImpl.this.jabberProvider.getConnection().removePacketListener(this);
                }
            }
        }
    }

    public IncomingFileTransferRequestJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider, OperationSetFileTransferJabberImpl fileTransferOpSet, FileTransferRequest fileTransferRequest) {
        this.jabberProvider = jabberProvider;
        this.fileTransferOpSet = fileTransferOpSet;
        this.fileTransferRequest = fileTransferRequest;
        String fromUserID = fileTransferRequest.getRequestor();
        this.sender = ((OperationSetPersistentPresenceJabberImpl) jabberProvider.getOperationSet(OperationSetPersistentPresence.class)).findContactByID(fromUserID);
        if (this.sender == null) {
            ChatRoom privateContactRoom = ((OperationSetMultiUserChatJabberImpl) jabberProvider.getOperationSet(OperationSetMultiUserChat.class)).getChatRoom(StringUtils.parseBareAddress(fromUserID));
            if (privateContactRoom != null) {
                this.sender = ((OperationSetPersistentPresenceJabberImpl) jabberProvider.getOperationSet(OperationSetPersistentPresence.class)).createVolatileContact(fromUserID, true);
                privateContactRoom.updatePrivateContactPresenceStatus(this.sender);
            }
        }
        this.id = String.valueOf(System.currentTimeMillis()) + String.valueOf(hashCode());
    }

    public Contact getSender() {
        return this.sender;
    }

    public String getFileDescription() {
        return this.fileTransferRequest.getDescription();
    }

    public String getFileName() {
        return this.fileTransferRequest.getFileName();
    }

    public long getFileSize() {
        return this.fileTransferRequest.getFileSize();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0039  */
    public net.java.sip.communicator.service.protocol.FileTransfer acceptFile(java.io.File r9) {
        /*
        r8 = this;
        r2 = 0;
        r5 = r8.fileTransferRequest;
        r4 = r5.accept();
        r3 = new net.java.sip.communicator.impl.protocol.jabber.IncomingFileTransferJabberImpl;	 Catch:{ XMPPException -> 0x0030 }
        r5 = r8.id;	 Catch:{ XMPPException -> 0x0030 }
        r6 = r8.sender;	 Catch:{ XMPPException -> 0x0030 }
        r3.m235init(r5, r6, r9, r4);	 Catch:{ XMPPException -> 0x0030 }
        r1 = new net.java.sip.communicator.service.protocol.event.FileTransferCreatedEvent;	 Catch:{ XMPPException -> 0x0041 }
        r5 = new java.util.Date;	 Catch:{ XMPPException -> 0x0041 }
        r5.<init>();	 Catch:{ XMPPException -> 0x0041 }
        r1.<init>(r3, r5);	 Catch:{ XMPPException -> 0x0041 }
        r5 = r8.fileTransferOpSet;	 Catch:{ XMPPException -> 0x0041 }
        r5.fireFileTransferCreated(r1);	 Catch:{ XMPPException -> 0x0041 }
        r4.recieveFile(r9);	 Catch:{ XMPPException -> 0x0041 }
        r5 = new net.java.sip.communicator.impl.protocol.jabber.OperationSetFileTransferJabberImpl$FileTransferProgressThread;	 Catch:{ XMPPException -> 0x0041 }
        r6 = r8.getFileSize();	 Catch:{ XMPPException -> 0x0041 }
        r5.m303init(r4, r3, r6);	 Catch:{ XMPPException -> 0x0041 }
        r5.start();	 Catch:{ XMPPException -> 0x0041 }
        r2 = r3;
    L_0x002f:
        return r2;
    L_0x0030:
        r0 = move-exception;
    L_0x0031:
        r5 = logger;
        r5 = r5.isDebugEnabled();
        if (r5 == 0) goto L_0x002f;
    L_0x0039:
        r5 = logger;
        r6 = "Receiving file failed.";
        r5.debug(r6, r0);
        goto L_0x002f;
    L_0x0041:
        r0 = move-exception;
        r2 = r3;
        goto L_0x0031;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.IncomingFileTransferRequestJabberImpl.acceptFile(java.io.File):net.java.sip.communicator.service.protocol.FileTransfer");
    }

    public void rejectFile() {
        this.fileTransferRequest.reject();
        this.fileTransferOpSet.fireFileTransferRequestRejected(new FileTransferRequestEvent(this.fileTransferOpSet, this, new Date()));
    }

    public String getID() {
        return this.id;
    }

    public byte[] getThumbnail() {
        return this.thumbnail;
    }

    public void createThumbnailListeners(String cid) {
        this.thumbnailCid = cid;
        if (this.jabberProvider.getConnection() != null) {
            this.jabberProvider.getConnection().addPacketListener(new ThumbnailResponseListener(), new AndFilter(new PacketTypeFilter(IQ.class), new IQTypeFilter(Type.RESULT)));
        }
    }
}
