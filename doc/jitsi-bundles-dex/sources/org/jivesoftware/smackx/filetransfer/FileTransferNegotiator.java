package org.jivesoftware.smackx.filetransfer;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.FileElement;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.FormField.Option;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.StreamInitiation;
import org.jivesoftware.smackx.packet.StreamInitiation.File;

public class FileTransferNegotiator {
    public static boolean IBB_ONLY = false;
    private static final String[] NAMESPACE = new String[]{"http://jabber.org/protocol/si/profile/file-transfer", FileElement.NAMESPACE};
    protected static final String STREAM_DATA_FIELD_NAME = "stream-method";
    private static final String STREAM_INIT_PREFIX = "jsi_";
    private static final Random randomGenerator = new Random();
    private static final Map<Connection, FileTransferNegotiator> transferObject = new ConcurrentHashMap();
    private final StreamNegotiator byteStreamTransferManager;
    private final Connection connection;
    private final StreamNegotiator inbandTransferManager;

    static {
        boolean z = true;
        if (System.getProperty("ibb") == null) {
            z = false;
        }
        IBB_ONLY = z;
    }

    public static FileTransferNegotiator getInstanceFor(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        } else if (!connection.isConnected()) {
            return null;
        } else {
            if (transferObject.containsKey(connection)) {
                return (FileTransferNegotiator) transferObject.get(connection);
            }
            FileTransferNegotiator transfer = new FileTransferNegotiator(connection);
            setServiceEnabled(connection, true);
            transferObject.put(connection, transfer);
            return transfer;
        }
    }

    public static void setServiceEnabled(Connection connection, boolean isEnabled) {
        ServiceDiscoveryManager manager = ServiceDiscoveryManager.getInstanceFor(connection);
        List<String> namespaces = new ArrayList();
        namespaces.addAll(Arrays.asList(NAMESPACE));
        namespaces.add(InBandBytestreamManager.NAMESPACE);
        if (!IBB_ONLY) {
            namespaces.add(Socks5BytestreamManager.NAMESPACE);
        }
        for (String namespace : namespaces) {
            if (!isEnabled) {
                manager.removeFeature(namespace);
            } else if (!manager.includesFeature(namespace)) {
                manager.addFeature(namespace);
            }
        }
    }

    public static boolean isServiceEnabled(Connection connection) {
        ServiceDiscoveryManager manager = ServiceDiscoveryManager.getInstanceFor(connection);
        List<String> namespaces = new ArrayList();
        namespaces.addAll(Arrays.asList(NAMESPACE));
        namespaces.add(InBandBytestreamManager.NAMESPACE);
        if (!IBB_ONLY) {
            namespaces.add(Socks5BytestreamManager.NAMESPACE);
        }
        for (String namespace : namespaces) {
            if (!manager.includesFeature(namespace)) {
                return false;
            }
        }
        return true;
    }

    public static IQ createIQ(String ID, String to, String from, Type type) {
        IQ iqPacket = new IQ() {
            public String getChildElementXML() {
                return null;
            }
        };
        iqPacket.setPacketID(ID);
        iqPacket.setTo(to);
        iqPacket.setFrom(from);
        iqPacket.setType(type);
        return iqPacket;
    }

    public static Collection<String> getSupportedProtocols() {
        List<String> protocols = new ArrayList();
        protocols.add(InBandBytestreamManager.NAMESPACE);
        if (!IBB_ONLY) {
            protocols.add(Socks5BytestreamManager.NAMESPACE);
        }
        return Collections.unmodifiableList(protocols);
    }

    private FileTransferNegotiator(Connection connection) {
        configureConnection(connection);
        this.connection = connection;
        this.byteStreamTransferManager = new Socks5TransferNegotiator(connection);
        this.inbandTransferManager = new IBBTransferNegotiator(connection);
    }

    private void configureConnection(final Connection connection) {
        connection.addConnectionListener(new ConnectionListener() {
            public void connectionClosed() {
                FileTransferNegotiator.this.cleanup(connection);
            }

            public void connectionClosedOnError(Exception e) {
                FileTransferNegotiator.this.cleanup(connection);
            }

            public void reconnectionFailed(Exception e) {
            }

            public void reconnectionSuccessful() {
            }

            public void reconnectingIn(int seconds) {
            }
        });
    }

    /* access modifiers changed from: private */
    public void cleanup(Connection connection) {
        if (transferObject.remove(connection) != null) {
            this.inbandTransferManager.cleanup();
        }
    }

    public StreamNegotiator selectStreamNegotiator(FileTransferRequest request) throws XMPPException {
        StreamInitiation si = request.getStreamInitiation();
        FormField streamMethodField = getStreamMethodField(si.getFeatureNegotiationForm());
        IQ iqPacket;
        if (streamMethodField == null) {
            String errorMessage = "No stream methods contained in packet.";
            XMPPError error = new XMPPError(Condition.bad_request, errorMessage);
            iqPacket = createIQ(si.getPacketID(), si.getFrom(), si.getTo(), Type.ERROR);
            iqPacket.setError(error);
            this.connection.sendPacket(iqPacket);
            throw new XMPPException(errorMessage, error);
        }
        try {
            return getNegotiator(streamMethodField);
        } catch (XMPPException e) {
            iqPacket = createIQ(si.getPacketID(), si.getFrom(), si.getTo(), Type.ERROR);
            iqPacket.setError(e.getXMPPError());
            this.connection.sendPacket(iqPacket);
            throw e;
        }
    }

    private FormField getStreamMethodField(DataForm form) {
        FormField field = null;
        Iterator<FormField> it = form.getFields();
        while (it.hasNext()) {
            field = (FormField) it.next();
            if (field.getVariable().equals(STREAM_DATA_FIELD_NAME)) {
                break;
            }
            field = null;
        }
        return field;
    }

    private StreamNegotiator getNegotiator(FormField field) throws XMPPException {
        boolean isByteStream = false;
        boolean isIBB = false;
        Iterator<Option> it = field.getOptions();
        while (it.hasNext()) {
            String variable = ((Option) it.next()).getValue();
            if (variable.equals(Socks5BytestreamManager.NAMESPACE) && !IBB_ONLY) {
                isByteStream = true;
            } else if (variable.equals(InBandBytestreamManager.NAMESPACE)) {
                isIBB = true;
            }
        }
        if (!isByteStream && !isIBB) {
            XMPPError error = new XMPPError(Condition.bad_request, "No acceptable transfer mechanism");
            throw new XMPPException(error.getMessage(), error);
        } else if (isByteStream && isIBB) {
            return new FaultTolerantNegotiator(this.connection, this.byteStreamTransferManager, this.inbandTransferManager);
        } else {
            if (isByteStream) {
                return this.byteStreamTransferManager;
            }
            return this.inbandTransferManager;
        }
    }

    public void rejectStream(StreamInitiation si) {
        XMPPError error = new XMPPError(Condition.forbidden, "Offer Declined");
        IQ iqPacket = createIQ(si.getPacketID(), si.getFrom(), si.getTo(), Type.ERROR);
        iqPacket.setError(error);
        this.connection.sendPacket(iqPacket);
    }

    public String getNextStreamID() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(STREAM_INIT_PREFIX);
        buffer.append(Math.abs(randomGenerator.nextLong()));
        return buffer.toString();
    }

    public StreamNegotiator negotiateOutgoingTransfer(String userID, String streamID, String fileName, long size, String desc, int responseTimeout) throws XMPPException {
        StreamInitiation si = new StreamInitiation();
        si.setSesssionID(streamID);
        si.setMimeType(URLConnection.guessContentTypeFromName(fileName));
        File siFile = new File(fileName, size);
        siFile.setDesc(desc);
        si.setFile(siFile);
        si.setFeatureNegotiationForm(createDefaultInitiationForm());
        si.setFrom(this.connection.getUser());
        si.setTo(userID);
        si.setType(Type.SET);
        PacketCollector collector = this.connection.createPacketCollector(new PacketIDFilter(si.getPacketID()));
        this.connection.sendPacket(si);
        Packet siResponse = collector.nextResult((long) responseTimeout);
        collector.cancel();
        if (!(siResponse instanceof IQ)) {
            return null;
        }
        IQ iqResponse = (IQ) siResponse;
        if (iqResponse.getType().equals(Type.RESULT)) {
            return getOutgoingNegotiator(getStreamMethodField(((StreamInitiation) siResponse).getFeatureNegotiationForm()));
        }
        if (iqResponse.getType().equals(Type.ERROR)) {
            throw new XMPPException(iqResponse.getError());
        }
        throw new XMPPException("File transfer response unreadable");
    }

    private StreamNegotiator getOutgoingNegotiator(FormField field) throws XMPPException {
        boolean isByteStream = false;
        boolean isIBB = false;
        Iterator<String> it = field.getValues();
        while (it.hasNext()) {
            String variable = (String) it.next();
            if (variable.equals(Socks5BytestreamManager.NAMESPACE) && !IBB_ONLY) {
                isByteStream = true;
            } else if (variable.equals(InBandBytestreamManager.NAMESPACE)) {
                isIBB = true;
            }
        }
        if (!isByteStream && !isIBB) {
            XMPPError error = new XMPPError(Condition.bad_request, "No acceptable transfer mechanism");
            throw new XMPPException(error.getMessage(), error);
        } else if (isByteStream && isIBB) {
            return new FaultTolerantNegotiator(this.connection, this.byteStreamTransferManager, this.inbandTransferManager);
        } else {
            if (isByteStream) {
                return this.byteStreamTransferManager;
            }
            return this.inbandTransferManager;
        }
    }

    private DataForm createDefaultInitiationForm() {
        DataForm form = new DataForm(Form.TYPE_FORM);
        FormField field = new FormField(STREAM_DATA_FIELD_NAME);
        field.setType(FormField.TYPE_LIST_SINGLE);
        if (!IBB_ONLY) {
            field.addOption(new Option(Socks5BytestreamManager.NAMESPACE));
        }
        field.addOption(new Option(InBandBytestreamManager.NAMESPACE));
        form.addField(field);
        return form;
    }
}
