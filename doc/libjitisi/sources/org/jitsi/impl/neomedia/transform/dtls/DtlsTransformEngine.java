package org.jitsi.impl.neomedia.transform.dtls;

import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SrtpControl.TransformEngine;

public class DtlsTransformEngine implements TransformEngine {
    private AbstractRTPConnector connector;
    private final DtlsControlImpl dtlsControl;
    private MediaType mediaType;
    private final DtlsPacketTransformer[] packetTransformers = new DtlsPacketTransformer[2];
    private Setup setup;

    public DtlsTransformEngine(DtlsControlImpl dtlsControl) {
        this.dtlsControl = dtlsControl;
    }

    public void cleanup() {
        for (int i = 0; i < this.packetTransformers.length; i++) {
            DtlsPacketTransformer packetTransformer = this.packetTransformers[i];
            if (packetTransformer != null) {
                packetTransformer.close();
                this.packetTransformers[i] = null;
            }
        }
        setConnector(null);
        setMediaType(null);
    }

    private DtlsPacketTransformer createPacketTransformer(int componentID) {
        DtlsPacketTransformer packetTransformer = new DtlsPacketTransformer(this, componentID);
        packetTransformer.setConnector(this.connector);
        packetTransformer.setSetup(this.setup);
        packetTransformer.setMediaType(this.mediaType);
        return packetTransformer;
    }

    /* access modifiers changed from: 0000 */
    public DtlsControlImpl getDtlsControl() {
        return this.dtlsControl;
    }

    private DtlsPacketTransformer getPacketTransformer(int componentID) {
        int index = componentID - 1;
        DtlsPacketTransformer packetTransformer = this.packetTransformers[index];
        if (packetTransformer == null) {
            packetTransformer = createPacketTransformer(componentID);
            if (packetTransformer != null) {
                this.packetTransformers[index] = packetTransformer;
            }
        }
        return packetTransformer;
    }

    public PacketTransformer getRTCPTransformer() {
        return getPacketTransformer(2);
    }

    public PacketTransformer getRTPTransformer() {
        return getPacketTransformer(1);
    }

    /* access modifiers changed from: 0000 */
    public void setConnector(AbstractRTPConnector connector) {
        if (this.connector != connector) {
            this.connector = connector;
            for (DtlsPacketTransformer packetTransformer : this.packetTransformers) {
                if (packetTransformer != null) {
                    packetTransformer.setConnector(this.connector);
                }
            }
        }
    }

    private void setMediaType(MediaType mediaType) {
        if (this.mediaType != mediaType) {
            this.mediaType = mediaType;
            for (DtlsPacketTransformer packetTransformer : this.packetTransformers) {
                if (packetTransformer != null) {
                    packetTransformer.setMediaType(this.mediaType);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void setSetup(Setup setup) {
        if (this.setup != setup) {
            this.setup = setup;
            for (DtlsPacketTransformer packetTransformer : this.packetTransformers) {
                if (packetTransformer != null) {
                    packetTransformer.setSetup(this.setup);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void start(MediaType mediaType) {
        setMediaType(mediaType);
    }
}
