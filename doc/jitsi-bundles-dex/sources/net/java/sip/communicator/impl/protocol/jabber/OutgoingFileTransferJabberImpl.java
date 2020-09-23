package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.FileElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailIQ;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.StreamInitiation;

public class OutgoingFileTransferJabberImpl extends AbstractFileTransfer implements PacketInterceptor {
    /* access modifiers changed from: private|final */
    public final File file;
    private final String id;
    private final OutgoingFileTransfer jabberTransfer;
    /* access modifiers changed from: private|final */
    public final Logger logger = Logger.getLogger(OutgoingFileTransferJabberImpl.class);
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl protocolProvider;
    private final Contact receiver;
    /* access modifiers changed from: private */
    public ThumbnailElement thumbnailElement;
    private final ThumbnailRequestListener thumbnailRequestListener = new ThumbnailRequestListener();

    private class ThumbnailRequestListener implements PacketListener {
        private ThumbnailRequestListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof ThumbnailIQ) {
                ThumbnailIQ thumbnailIQ = (ThumbnailIQ) packet;
                String thumbnailIQCid = thumbnailIQ.getCid();
                XMPPConnection connection = OutgoingFileTransferJabberImpl.this.protocolProvider.getConnection();
                if (thumbnailIQCid != null && thumbnailIQCid.equals(OutgoingFileTransferJabberImpl.this.thumbnailElement.getCid())) {
                    ThumbnailedFile thumbnailedFile = (ThumbnailedFile) OutgoingFileTransferJabberImpl.this.file;
                    ThumbnailIQ thumbnailResponse = new ThumbnailIQ(thumbnailIQ.getTo(), thumbnailIQ.getFrom(), thumbnailIQCid, thumbnailedFile.getThumbnailMimeType(), thumbnailedFile.getThumbnailData(), Type.RESULT);
                    if (OutgoingFileTransferJabberImpl.this.logger.isDebugEnabled()) {
                        OutgoingFileTransferJabberImpl.this.logger.debug("Send thumbnail response to the receiver: " + thumbnailResponse.toXML());
                    }
                    connection.sendPacket(thumbnailResponse);
                }
                if (connection != null) {
                    connection.removePacketListener(this);
                }
            }
        }
    }

    public OutgoingFileTransferJabberImpl(Contact receiver, File file, OutgoingFileTransfer jabberTransfer, ProtocolProviderServiceJabberImpl protocolProvider) {
        this.receiver = receiver;
        this.file = file;
        this.jabberTransfer = jabberTransfer;
        this.protocolProvider = protocolProvider;
        this.id = String.valueOf(System.currentTimeMillis()) + String.valueOf(hashCode());
        if ((file instanceof ThumbnailedFile) && ((ThumbnailedFile) file).getThumbnailData() != null && ((ThumbnailedFile) file).getThumbnailData().length > 0) {
            if (protocolProvider.isFeatureListSupported(protocolProvider.getFullJid(receiver), ThumbnailElement.NAMESPACE, ThumbnailIQ.NAMESPACE)) {
                protocolProvider.getConnection().addPacketInterceptor(this, new IQTypeFilter(Type.SET));
            }
        }
    }

    public void cancel() {
        this.jabberTransfer.cancel();
    }

    public long getTransferedBytes() {
        return this.jabberTransfer.getBytesSent();
    }

    public int getDirection() {
        return 2;
    }

    public File getLocalFile() {
        return this.file;
    }

    public Contact getContact() {
        return this.receiver;
    }

    public String getID() {
        return this.id;
    }

    public void removeThumbnailRequestListener() {
        this.protocolProvider.getConnection().removePacketListener(this.thumbnailRequestListener);
    }

    public void interceptPacket(Packet packet) {
        if ((packet instanceof StreamInitiation) && (this.file instanceof ThumbnailedFile)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("File transfer packet intercepted in order to add thumbnail.");
            }
            StreamInitiation fileTransferPacket = (StreamInitiation) packet;
            ThumbnailedFile thumbnailedFile = this.file;
            if (this.jabberTransfer.getStreamID().equals(fileTransferPacket.getSessionID())) {
                StreamInitiation.File file = fileTransferPacket.getFile();
                this.thumbnailElement = new ThumbnailElement(StringUtils.parseServer(fileTransferPacket.getTo()), thumbnailedFile.getThumbnailData(), thumbnailedFile.getThumbnailMimeType(), thumbnailedFile.getThumbnailWidth(), thumbnailedFile.getThumbnailHeight());
                fileTransferPacket.setFile(new FileElement(file, this.thumbnailElement));
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("The file transfer packet with thumbnail: " + fileTransferPacket.toXML());
                }
                if (this.protocolProvider.getConnection() != null) {
                    this.protocolProvider.getConnection().addPacketListener(this.thumbnailRequestListener, new AndFilter(new PacketTypeFilter(IQ.class), new IQTypeFilter(Type.GET)));
                }
            }
            this.protocolProvider.getConnection().removePacketInterceptor(this);
        }
    }
}
