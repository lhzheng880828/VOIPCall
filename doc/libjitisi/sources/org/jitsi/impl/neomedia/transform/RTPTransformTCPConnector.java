package org.jitsi.impl.neomedia.transform;

import java.io.IOException;
import org.jitsi.impl.neomedia.RTPConnectorTCPImpl;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.util.Logger;

public class RTPTransformTCPConnector extends RTPConnectorTCPImpl {
    private static final Logger logger = Logger.getLogger(RTPTransformUDPConnector.class);
    private TransformEngine engine;

    public RTPTransformTCPConnector(StreamConnector connector) {
        super(connector);
    }

    /* access modifiers changed from: protected */
    public TransformTCPInputStream createControlInputStream() throws IOException {
        TransformTCPInputStream controlInputStream = new TransformTCPInputStream(getControlSocket());
        controlInputStream.setTransformer(getRTCPTransformer());
        return controlInputStream;
    }

    /* access modifiers changed from: protected */
    public TransformTCPOutputStream createControlOutputStream() throws IOException {
        TransformTCPOutputStream controlOutputStream = new TransformTCPOutputStream(getControlSocket());
        controlOutputStream.setTransformer(getRTCPTransformer());
        return controlOutputStream;
    }

    /* access modifiers changed from: protected */
    public TransformTCPInputStream createDataInputStream() throws IOException {
        TransformTCPInputStream dataInputStream = new TransformTCPInputStream(getDataSocket());
        dataInputStream.setTransformer(getRTPTransformer());
        return dataInputStream;
    }

    /* access modifiers changed from: protected */
    public TransformTCPOutputStream createDataOutputStream() throws IOException {
        TransformTCPOutputStream dataOutputStream = new TransformTCPOutputStream(getDataSocket());
        dataOutputStream.setTransformer(getRTPTransformer());
        return dataOutputStream;
    }

    public TransformEngine getEngine() {
        return this.engine;
    }

    private PacketTransformer getRTCPTransformer() {
        TransformEngine engine = getEngine();
        return engine == null ? null : engine.getRTCPTransformer();
    }

    private PacketTransformer getRTPTransformer() {
        TransformEngine engine = getEngine();
        return engine == null ? null : engine.getRTPTransformer();
    }

    public void setEngine(TransformEngine engine) {
        if (this.engine != engine) {
            TransformTCPInputStream controlInputStream;
            TransformTCPOutputStream controlOutputStream;
            TransformTCPInputStream dataInputStream;
            TransformTCPOutputStream dataOutputStream;
            this.engine = engine;
            try {
                controlInputStream = (TransformTCPInputStream) getControlInputStream(false);
            } catch (IOException ioex) {
                logger.error("The impossible happened", ioex);
                controlInputStream = null;
            }
            if (controlInputStream != null) {
                controlInputStream.setTransformer(getRTCPTransformer());
            }
            try {
                controlOutputStream = (TransformTCPOutputStream) getControlOutputStream(false);
            } catch (IOException ioex2) {
                logger.error("The impossible happened", ioex2);
                controlOutputStream = null;
            }
            if (controlOutputStream != null) {
                controlOutputStream.setTransformer(getRTCPTransformer());
            }
            try {
                dataInputStream = (TransformTCPInputStream) getDataInputStream(false);
            } catch (IOException ioex22) {
                logger.error("The impossible happened", ioex22);
                dataInputStream = null;
            }
            if (dataInputStream != null) {
                dataInputStream.setTransformer(getRTPTransformer());
            }
            try {
                dataOutputStream = (TransformTCPOutputStream) getDataOutputStream(false);
            } catch (IOException ioex222) {
                logger.error("The impossible happened", ioex222);
                dataOutputStream = null;
            }
            if (dataOutputStream != null) {
                dataOutputStream.setTransformer(getRTPTransformer());
            }
        }
    }
}
