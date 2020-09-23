package org.jitsi.impl.neomedia.transform;

import java.io.IOException;
import org.jitsi.impl.neomedia.RTPConnectorUDPImpl;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.util.Logger;

public class RTPTransformUDPConnector extends RTPConnectorUDPImpl {
    private static final Logger logger = Logger.getLogger(RTPTransformUDPConnector.class);
    private TransformEngine engine;

    public RTPTransformUDPConnector(StreamConnector connector) {
        super(connector);
    }

    /* access modifiers changed from: protected */
    public TransformUDPInputStream createControlInputStream() throws IOException {
        TransformUDPInputStream controlInputStream = new ControlTransformInputStream(getControlSocket());
        controlInputStream.setTransformer(getRTCPTransformer());
        return controlInputStream;
    }

    /* access modifiers changed from: protected */
    public TransformUDPOutputStream createControlOutputStream() throws IOException {
        TransformUDPOutputStream controlOutputStream = new TransformUDPOutputStream(getControlSocket());
        controlOutputStream.setTransformer(getRTCPTransformer());
        return controlOutputStream;
    }

    /* access modifiers changed from: protected */
    public TransformUDPInputStream createDataInputStream() throws IOException {
        TransformUDPInputStream dataInputStream = new TransformUDPInputStream(getDataSocket());
        dataInputStream.setTransformer(getRTPTransformer());
        return dataInputStream;
    }

    /* access modifiers changed from: protected */
    public TransformUDPOutputStream createDataOutputStream() throws IOException {
        TransformUDPOutputStream dataOutputStream = new TransformUDPOutputStream(getDataSocket());
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
            TransformUDPInputStream controlInputStream;
            TransformUDPOutputStream controlOutputStream;
            TransformUDPInputStream dataInputStream;
            TransformUDPOutputStream dataOutputStream;
            this.engine = engine;
            try {
                controlInputStream = (TransformUDPInputStream) getControlInputStream(false);
            } catch (IOException ioex) {
                logger.error("The impossible happened", ioex);
                controlInputStream = null;
            }
            if (controlInputStream != null) {
                controlInputStream.setTransformer(getRTCPTransformer());
            }
            try {
                controlOutputStream = (TransformUDPOutputStream) getControlOutputStream(false);
            } catch (IOException ioex2) {
                logger.error("The impossible happened", ioex2);
                controlOutputStream = null;
            }
            if (controlOutputStream != null) {
                controlOutputStream.setTransformer(getRTCPTransformer());
            }
            try {
                dataInputStream = (TransformUDPInputStream) getDataInputStream(false);
            } catch (IOException ioex22) {
                logger.error("The impossible happened", ioex22);
                dataInputStream = null;
            }
            if (dataInputStream != null) {
                dataInputStream.setTransformer(getRTPTransformer());
            }
            try {
                dataOutputStream = (TransformUDPOutputStream) getDataOutputStream(false);
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
