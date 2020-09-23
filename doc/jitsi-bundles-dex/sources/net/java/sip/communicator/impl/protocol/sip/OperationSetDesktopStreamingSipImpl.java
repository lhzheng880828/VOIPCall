package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDesktopStreaming;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.VideoMediaFormat;

public class OperationSetDesktopStreamingSipImpl extends OperationSetVideoTelephonySipImpl implements OperationSetDesktopStreaming {
    protected Point origin = null;
    protected Dimension size = null;

    public OperationSetDesktopStreamingSipImpl(OperationSetBasicTelephonySipImpl basicTelephony) {
        super(basicTelephony);
    }

    public MediaUseCase getMediaUseCase() {
        return MediaUseCase.DESKTOP;
    }

    public Call createVideoCall(String uri, MediaDevice mediaDevice) throws OperationFailedException, ParseException {
        Address toAddress = ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(uri);
        CallSipImpl call = ((OperationSetBasicTelephonySipImpl) this.basicTelephony).createOutgoingCall();
        MediaUseCase useCase = getMediaUseCase();
        call.setVideoDevice(mediaDevice, useCase);
        call.setLocalVideoAllowed(true, useCase);
        call.invite(toAddress, null);
        this.origin = getOriginForMediaDevice(mediaDevice);
        return call;
    }

    public Call createVideoCall(Contact callee, MediaDevice mediaDevice) throws OperationFailedException {
        try {
            Address toAddress = ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(callee.getAddress());
            CallSipImpl call = ((OperationSetBasicTelephonySipImpl) this.basicTelephony).createOutgoingCall();
            MediaUseCase useCase = getMediaUseCase();
            call.setLocalVideoAllowed(true, useCase);
            call.setVideoDevice(mediaDevice, useCase);
            call.invite(toAddress, null);
            this.origin = getOriginForMediaDevice(mediaDevice);
            return call;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public Call createVideoCall(String uri) throws OperationFailedException, ParseException {
        CallSipImpl call = (CallSipImpl) super.createVideoCall(uri);
        MediaDevice device = call.getDefaultDevice(MediaType.VIDEO);
        this.size = ((VideoMediaFormat) device.getFormat()).getSize();
        this.origin = getOriginForMediaDevice(device);
        return call;
    }

    public Call createVideoCall(Contact callee) throws OperationFailedException {
        CallSipImpl call = (CallSipImpl) super.createVideoCall(callee);
        MediaDevice device = call.getDefaultDevice(MediaType.VIDEO);
        this.size = ((VideoMediaFormat) device.getFormat()).getSize();
        this.origin = getOriginForMediaDevice(device);
        return call;
    }

    public void setLocalVideoAllowed(Call call, boolean allowed) throws OperationFailedException {
        CallSipImpl callImpl = (CallSipImpl) call;
        MediaUseCase useCase = MediaUseCase.DESKTOP;
        callImpl.setLocalVideoAllowed(allowed, useCase);
        callImpl.setVideoDevice(null, useCase);
        MediaDevice device = callImpl.getDefaultDevice(MediaType.VIDEO);
        if (device.getFormat() != null) {
            this.size = ((VideoMediaFormat) device.getFormat()).getSize();
        }
        this.origin = getOriginForMediaDevice(device);
        callImpl.reInvite();
    }

    public boolean isLocalVideoAllowed(Call call) {
        return ((CallSipImpl) call).isLocalVideoAllowed(MediaUseCase.DESKTOP);
    }

    public void setLocalVideoAllowed(Call call, MediaDevice mediaDevice, boolean allowed) throws OperationFailedException {
        CallSipImpl sipCall = (CallSipImpl) call;
        MediaUseCase useCase = MediaUseCase.DESKTOP;
        sipCall.setVideoDevice(mediaDevice, useCase);
        sipCall.setLocalVideoAllowed(allowed, useCase);
        this.size = ((VideoMediaFormat) sipCall.getDefaultDevice(MediaType.VIDEO).getFormat()).getSize();
        this.origin = getOriginForMediaDevice(mediaDevice);
        sipCall.reInvite();
    }

    public boolean isPartialStreaming(Call call) {
        MediaDevice device = ((CallSipImpl) call).getDefaultDevice(MediaType.VIDEO);
        return device == null ? false : SipActivator.getMediaService().isPartialStreaming(device);
    }

    public void movePartialDesktopStreaming(Call call, int x, int y) {
        VideoMediaStream videoStream = (VideoMediaStream) ((CallPeerMediaHandlerSipImpl) ((CallPeerSipImpl) ((CallSipImpl) call).getCallPeers().next()).getMediaHandler()).getStream(MediaType.VIDEO);
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

    public Point getOrigin() {
        return this.origin;
    }

    protected static Point getOriginForMediaDevice(MediaDevice device) {
        return SipActivator.getMediaService().getOriginForDesktopStreamingDevice(device);
    }
}
