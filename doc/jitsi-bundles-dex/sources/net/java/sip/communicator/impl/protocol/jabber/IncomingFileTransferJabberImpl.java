package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

public class IncomingFileTransferJabberImpl extends AbstractFileTransfer {
    private final File file;
    private final String id;
    private IncomingFileTransfer jabberTransfer;
    private final Contact sender;

    public IncomingFileTransferJabberImpl(String id, Contact sender, File file, IncomingFileTransfer jabberTransfer) {
        this.id = id;
        this.sender = sender;
        this.file = file;
        this.jabberTransfer = jabberTransfer;
    }

    public void cancel() {
        this.jabberTransfer.cancel();
    }

    public long getTransferedBytes() {
        return this.jabberTransfer.getAmountWritten();
    }

    public int getDirection() {
        return 1;
    }

    public Contact getContact() {
        return this.sender;
    }

    public String getID() {
        return this.id;
    }

    public File getLocalFile() {
        return this.file;
    }
}
