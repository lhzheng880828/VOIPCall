package net.java.sip.communicator.impl.protocol.jabber;

import java.text.ParseException;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDesktopStreaming;
import net.java.sip.communicator.service.protocol.media.MediaAwareCall;
import net.java.sip.communicator.service.protocol.media.ProtocolMediaActivator;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.VideoMediaFormat;

public class OperationSetDesktopStreamingJabberImpl extends OperationSetVideoTelephonyJabberImpl implements OperationSetDesktopStreaming {
    protected Point origin = null;
    protected Dimension size = null;

    public OperationSetDesktopStreamingJabberImpl(OperationSetBasicTelephonyJabberImpl basicTelephony) {
        super(basicTelephony);
    }

    public MediaUseCase getMediaUseCase() {
        return MediaUseCase.DESKTOP;
    }

    public Call createVideoCall(String uri, MediaDevice mediaDevice) throws OperationFailedException, ParseException {
        return createOutgoingVideoCall(uri, mediaDevice);
    }

    public Call createVideoCall(Contact callee, MediaDevice mediaDevice) throws OperationFailedException {
        return createOutgoingVideoCall(callee.getAddress(), mediaDevice);
    }

    public Call createVideoCall(String uri) throws OperationFailedException {
        Call call = createOutgoingVideoCall(uri);
        MediaDevice device = ((MediaAwareCall) call).getDefaultDevice(MediaType.VIDEO);
        this.size = ((VideoMediaFormat) device.getFormat()).getSize();
        this.origin = getOriginForMediaDevice(device);
        return call;
    }

    public Call createVideoCall(Contact callee) throws OperationFailedException {
        Call call = createOutgoingVideoCall(callee.getAddress());
        MediaDevice device = ((MediaAwareCall) call).getDefaultDevice(MediaType.VIDEO);
        this.size = ((VideoMediaFormat) device.getFormat()).getSize();
        this.origin = getOriginForMediaDevice(device);
        return call;
    }

    public void setLocalVideoAllowed(Call call, boolean allowed) throws OperationFailedException {
        setLocalVideoAllowed(call, null, allowed);
    }

    public void setLocalVideoAllowed(Call call, MediaDevice mediaDevice, boolean allowed) throws OperationFailedException {
        AbstractCallJabberGTalkImpl<?> callImpl = (AbstractCallJabberGTalkImpl) call;
        MediaUseCase useCase = getMediaUseCase();
        if (mediaDevice == null) {
            mediaDevice = ProtocolMediaActivator.getMediaService().getDefaultDevice(MediaType.VIDEO, useCase);
        }
        callImpl.setVideoDevice(mediaDevice, useCase);
        callImpl.setLocalVideoAllowed(allowed, useCase);
        MediaFormat mediaDeviceFormat = mediaDevice.getFormat();
        this.size = mediaDeviceFormat == null ? null : ((VideoMediaFormat) mediaDeviceFormat).getSize();
        callImpl.modifyVideoContent();
        this.origin = getOriginForMediaDevice(mediaDevice);
    }

    public boolean isLocalVideoAllowed(Call call) {
        return ((MediaAwareCall) call).isLocalVideoAllowed(MediaUseCase.DESKTOP);
    }

    /* access modifiers changed from: protected */
    public Call createOutgoingVideoCall(String calleeAddress, MediaDevice videoDevice) throws OperationFailedException {
        if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection() == null) {
            throw new OperationFailedException("Failed to create OutgoingJingleSession.\nwe don't have a valid XMPPConnection.", 4);
        }
        CallJabberImpl call = new CallJabberImpl((OperationSetBasicTelephonyJabberImpl) this.basicTelephony);
        MediaUseCase useCase = getMediaUseCase();
        if (videoDevice != null) {
            call.setVideoDevice(videoDevice, useCase);
        }
        call.setLocalVideoAllowed(true, useCase);
        ((OperationSetBasicTelephonyJabberImpl) this.basicTelephony).createOutgoingCall(call, calleeAddress);
        this.origin = getOriginForMediaDevice(videoDevice);
        return call;
    }

    public boolean isPartialStreaming(Call call) {
        MediaDevice device = ((MediaAwareCall) call).getDefaultDevice(MediaType.VIDEO);
        return device == null ? false : JabberActivator.getMediaService().isPartialStreaming(device);
    }

    public void movePartialDesktopStreaming(Call call, int x, int y) {
        VideoMediaStream videoStream = (VideoMediaStream) ((AbstractCallPeerMediaHandlerJabberGTalkImpl) ((AbstractCallPeerJabberGTalkImpl) ((AbstractCallJabberGTalkImpl) call).getCallPeers().next()).getMediaHandler()).getStream(MediaType.VIDEO);
        if (videoStream != null) {
            videoStream.movePartialDesktopStreaming(x, y);
            if (this.origin != null) {
                this.origin.x = x;
                this.origin.y = y;
                return;
            }
            this.origin = new Point(x, y);
        }
    }

    protected static Point getOriginForMediaDevice(MediaDevice device) {
        return JabberActivator.getMediaService().getOriginForDesktopStreamingDevice(device);
    }
}
