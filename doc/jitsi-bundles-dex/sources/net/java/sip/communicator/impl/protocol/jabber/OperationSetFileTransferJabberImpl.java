package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.FileElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailIQ;
import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSetFileTransfer;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.FileTransferCreatedEvent;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;
import net.java.sip.communicator.service.protocol.event.FileTransferRequestEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.StreamInitiation;
import org.jivesoftware.smackx.packet.StreamInitiation.File;

public class OperationSetFileTransferJabberImpl implements OperationSetFileTransfer {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetFileTransferJabberImpl.class);
    private Vector<FileTransferListener> fileTransferListeners = new Vector();
    /* access modifiers changed from: private */
    public FileTransferRequestListener fileTransferRequestListener;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    /* access modifiers changed from: private */
    public FileTransferManager manager = null;
    /* access modifiers changed from: private */
    public OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;

    protected static class FileTransferProgressThread extends Thread {
        private final AbstractFileTransfer fileTransfer;
        private long initialFileSize;
        private final FileTransfer jabberTransfer;

        public FileTransferProgressThread(FileTransfer jabberTransfer, AbstractFileTransfer transfer, long initialFileSize) {
            this.jabberTransfer = jabberTransfer;
            this.fileTransfer = transfer;
            this.initialFileSize = initialFileSize;
        }

        public FileTransferProgressThread(FileTransfer jabberTransfer, AbstractFileTransfer transfer) {
            this.jabberTransfer = jabberTransfer;
            this.fileTransfer = transfer;
        }

        public void run() {
            int status;
            long progress;
            String statusReason = "";
            while (true) {
                try {
                    Thread.sleep(10);
                    status = OperationSetFileTransferJabberImpl.parseJabberStatus(this.jabberTransfer.getStatus());
                    progress = this.fileTransfer.getTransferedBytes();
                    if (status != 2 && status != 0 && status != 1 && status != 3) {
                        this.fileTransfer.fireStatusChangeEvent(status, "Status changed");
                        this.fileTransfer.fireProgressChangeEvent(System.currentTimeMillis(), progress);
                    }
                } catch (InterruptedException e) {
                    if (OperationSetFileTransferJabberImpl.logger.isDebugEnabled()) {
                        OperationSetFileTransferJabberImpl.logger.debug("Unable to sleep thread.", e);
                    }
                }
            }
            if (this.fileTransfer instanceof OutgoingFileTransferJabberImpl) {
                ((OutgoingFileTransferJabberImpl) this.fileTransfer).removeThumbnailRequestListener();
            }
            if (status == 0 && this.fileTransfer.getStatus() == 6) {
                this.fileTransfer.fireStatusChangeEvent(4, "Status changed");
                this.fileTransfer.fireProgressChangeEvent(System.currentTimeMillis(), progress);
            }
            if (this.jabberTransfer.getError() != null) {
                OperationSetFileTransferJabberImpl.logger.error("An error occured while transfering file: " + this.jabberTransfer.getError().getMessage());
            }
            if (this.jabberTransfer.getException() != null) {
                OperationSetFileTransferJabberImpl.logger.error("An exception occured while transfering file: ", this.jabberTransfer.getException());
                if (this.jabberTransfer.getException() instanceof XMPPException) {
                    XMPPError error = ((XMPPException) this.jabberTransfer.getException()).getXMPPError();
                    if (error != null && (error.getCode() == Response.NOT_ACCEPTABLE || error.getCode() == Response.FORBIDDEN)) {
                        status = 3;
                    }
                }
                statusReason = this.jabberTransfer.getException().getMessage();
            }
            if (this.initialFileSize > 0 && status == 0 && this.fileTransfer.getTransferedBytes() < this.initialFileSize) {
                status = 1;
            }
            this.fileTransfer.fireStatusChangeEvent(status, statusReason);
            this.fileTransfer.fireProgressChangeEvent(System.currentTimeMillis(), progress);
        }
    }

    private class FileTransferRequestListener implements PacketListener {
        private FileTransferRequestListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof StreamInitiation) {
                if (OperationSetFileTransferJabberImpl.logger.isDebugEnabled()) {
                    OperationSetFileTransferJabberImpl.logger.debug("Incoming Jabber file transfer request.");
                }
                StreamInitiation streamInitiation = (StreamInitiation) packet;
                IncomingFileTransferRequestJabberImpl incomingFileTransferRequest = new IncomingFileTransferRequestJabberImpl(OperationSetFileTransferJabberImpl.this.jabberProvider, OperationSetFileTransferJabberImpl.this, new FileTransferRequest(OperationSetFileTransferJabberImpl.this.manager, streamInitiation));
                File file = streamInitiation.getFile();
                boolean isThumbnailedFile = false;
                if (file instanceof FileElement) {
                    ThumbnailElement thumbnailElement = ((FileElement) file).getThumbnailElement();
                    if (thumbnailElement != null) {
                        isThumbnailedFile = true;
                        incomingFileTransferRequest.createThumbnailListeners(thumbnailElement.getCid());
                        ThumbnailIQ thumbnailRequest = new ThumbnailIQ(streamInitiation.getTo(), streamInitiation.getFrom(), thumbnailElement.getCid(), Type.GET);
                        if (OperationSetFileTransferJabberImpl.logger.isDebugEnabled()) {
                            OperationSetFileTransferJabberImpl.logger.debug("Sending thumbnail request:" + thumbnailRequest.toXML());
                        }
                        OperationSetFileTransferJabberImpl.this.jabberProvider.getConnection().sendPacket(thumbnailRequest);
                    }
                }
                if (!isThumbnailedFile) {
                    OperationSetFileTransferJabberImpl.this.fireFileTransferRequest(new FileTransferRequestEvent(OperationSetFileTransferJabberImpl.this, incomingFileTransferRequest, new Date()));
                }
            }
        }
    }

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetFileTransferJabberImpl.logger.isDebugEnabled()) {
                OperationSetFileTransferJabberImpl.logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetFileTransferJabberImpl.this.opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) OperationSetFileTransferJabberImpl.this.jabberProvider.getOperationSet(OperationSetPersistentPresence.class);
                OperationSetFileTransferJabberImpl.this.manager = new FileTransferManager(OperationSetFileTransferJabberImpl.this.jabberProvider.getConnection());
                OperationSetFileTransferJabberImpl.this.fileTransferRequestListener = new FileTransferRequestListener();
                ProviderManager.getInstance().addIQProvider(FileElement.ELEMENT_NAME, FileElement.NAMESPACE, new FileElement());
                ProviderManager.getInstance().addIQProvider("data", ThumbnailIQ.NAMESPACE, new ThumbnailIQ());
                OperationSetFileTransferJabberImpl.this.jabberProvider.getConnection().addPacketListener(OperationSetFileTransferJabberImpl.this.fileTransferRequestListener, new AndFilter(new PacketTypeFilter(StreamInitiation.class), new IQTypeFilter(Type.SET)));
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED) {
                if (!(OperationSetFileTransferJabberImpl.this.fileTransferRequestListener == null || OperationSetFileTransferJabberImpl.this.jabberProvider.getConnection() == null)) {
                    OperationSetFileTransferJabberImpl.this.jabberProvider.getConnection().removePacketListener(OperationSetFileTransferJabberImpl.this.fileTransferRequestListener);
                }
                if (ProviderManager.getInstance() != null) {
                    ProviderManager.getInstance().removeIQProvider(FileElement.ELEMENT_NAME, FileElement.NAMESPACE);
                    ProviderManager.getInstance().removeIQProvider("data", ThumbnailIQ.NAMESPACE);
                }
                OperationSetFileTransferJabberImpl.this.fileTransferRequestListener = null;
                OperationSetFileTransferJabberImpl.this.manager = null;
            }
        }
    }

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                FileTransferNegotiator.getInstanceFor(connection);
            }
        });
    }

    public OperationSetFileTransferJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.jabberProvider = provider;
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());
        FileTransferNegotiator.IBB_ONLY = true;
    }

    public net.java.sip.communicator.service.protocol.FileTransfer sendFile(Contact toContact, java.io.File file) throws IllegalStateException, IllegalArgumentException, OperationNotSupportedException {
        XMPPException e;
        OutgoingFileTransferJabberImpl outgoingTransfer = null;
        try {
            assertConnected();
            if (file.length() > getMaximumFileLength()) {
                throw new IllegalArgumentException("File length exceeds the allowed one for this protocol");
            }
            String fullJid = null;
            if (((OperationSetMultiUserChat) this.jabberProvider.getOperationSet(OperationSetMultiUserChat.class)).isPrivateMessagingContact(toContact.getAddress())) {
                fullJid = toContact.getAddress();
            } else {
                Iterator<Presence> iter = this.jabberProvider.getConnection().getRoster().getPresences(toContact.getAddress());
                int bestPriority = -1;
                PresenceStatus jabberStatus = null;
                while (iter.hasNext()) {
                    Presence presence = (Presence) iter.next();
                    if (this.jabberProvider.isFeatureListSupported(presence.getFrom(), FileElement.NAMESPACE, "http://jabber.org/protocol/si/profile/file-transfer")) {
                        int priority = presence.getPriority() == Integer.MIN_VALUE ? 0 : presence.getPriority();
                        if (priority > bestPriority) {
                            bestPriority = priority;
                            fullJid = presence.getFrom();
                            jabberStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.jabberProvider);
                        } else if (priority == bestPriority && jabberStatus != null) {
                            PresenceStatus tempStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.jabberProvider);
                            if (tempStatus.compareTo(jabberStatus) > 0) {
                                fullJid = presence.getFrom();
                                jabberStatus = tempStatus;
                            }
                        }
                    }
                }
            }
            if (fullJid == null) {
                throw new OperationNotSupportedException("Contact client or server does not support file transfers.");
            }
            OutgoingFileTransfer transfer = this.manager.createOutgoingFileTransfer(fullJid);
            OutgoingFileTransferJabberImpl outgoingTransfer2 = new OutgoingFileTransferJabberImpl(toContact, file, transfer, this.jabberProvider);
            try {
                fireFileTransferCreated(new FileTransferCreatedEvent(outgoingTransfer2, new Date()));
                transfer.sendFile(file, "Sending file");
                new FileTransferProgressThread(transfer, outgoingTransfer2).start();
                return outgoingTransfer2;
            } catch (XMPPException e2) {
                e = e2;
                outgoingTransfer = outgoingTransfer2;
                logger.error("Failed to send file.", e);
                return outgoingTransfer;
            }
        } catch (XMPPException e3) {
            e = e3;
            logger.error("Failed to send file.", e);
            return outgoingTransfer;
        }
    }

    public net.java.sip.communicator.service.protocol.FileTransfer sendFile(Contact toContact, Contact fromContact, String remotePath, String localPath) throws IllegalStateException, IllegalArgumentException, OperationNotSupportedException {
        return sendFile(toContact, new java.io.File(localPath));
    }

    public void addFileTransferListener(FileTransferListener listener) {
        synchronized (this.fileTransferListeners) {
            if (!this.fileTransferListeners.contains(listener)) {
                this.fileTransferListeners.add(listener);
            }
        }
    }

    public void removeFileTransferListener(FileTransferListener listener) {
        synchronized (this.fileTransferListeners) {
            this.fileTransferListeners.remove(listener);
        }
    }

    private void assertConnected() throws IllegalStateException {
        if (this.jabberProvider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the service before being able to send a file.");
        } else if (!this.jabberProvider.isRegistered()) {
            if (this.opSetPersPresence.getPresenceStatus().isOnline()) {
                this.opSetPersPresence.fireProviderStatusChangeEvent(this.opSetPersPresence.getPresenceStatus(), this.jabberProvider.getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE));
            }
            throw new IllegalStateException("The provider must be signed on the service before being able to send a file.");
        }
    }

    public long getMaximumFileLength() {
        return 2147483648L;
    }

    /* access modifiers changed from: 0000 */
    public void fireFileTransferRequest(FileTransferRequestEvent event) {
        Iterator<FileTransferListener> listeners;
        synchronized (this.fileTransferListeners) {
            listeners = new ArrayList(this.fileTransferListeners).iterator();
        }
        while (listeners.hasNext()) {
            ((FileTransferListener) listeners.next()).fileTransferRequestReceived(event);
        }
    }

    /* access modifiers changed from: 0000 */
    public void fireFileTransferRequestRejected(FileTransferRequestEvent event) {
        Iterator<FileTransferListener> listeners;
        synchronized (this.fileTransferListeners) {
            listeners = new ArrayList(this.fileTransferListeners).iterator();
        }
        while (listeners.hasNext()) {
            ((FileTransferListener) listeners.next()).fileTransferRequestRejected(event);
        }
    }

    /* access modifiers changed from: 0000 */
    public void fireFileTransferCreated(FileTransferCreatedEvent event) {
        Iterator<FileTransferListener> listeners;
        synchronized (this.fileTransferListeners) {
            listeners = new ArrayList(this.fileTransferListeners).iterator();
        }
        while (listeners.hasNext()) {
            ((FileTransferListener) listeners.next()).fileTransferCreated(event);
        }
    }

    /* access modifiers changed from: private|static */
    public static int parseJabberStatus(Status jabberStatus) {
        if (jabberStatus.equals(Status.complete)) {
            return 0;
        }
        if (jabberStatus.equals(Status.cancelled)) {
            return 1;
        }
        if (jabberStatus.equals(Status.in_progress) || jabberStatus.equals(Status.negotiated)) {
            return 4;
        }
        if (jabberStatus.equals(Status.error)) {
            return 2;
        }
        if (jabberStatus.equals(Status.refused)) {
            return 3;
        }
        if (jabberStatus.equals(Status.negotiating_transfer) || jabberStatus.equals(Status.negotiating_stream)) {
            return 6;
        }
        return 5;
    }
}
