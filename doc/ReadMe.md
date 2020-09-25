# FMJ框架
用FMJ在本地模拟收发视频、音频
https://www.iteye.com/problems/74543

java媒体处理解决方案之FMJ
http://suwish.com/java-media-process-solutions-fmj.html


http://fmj-sf.net/


# FMJ RTP流建立时序图
logcat

audio流程
2020-09-22 14:32:28.423 28948-29019/org.jitsi D/Jitsi: [482] service.protocol.media.CallPeerMediaHandler.initStream().1194 Initializing audio stream for 45645 <45645@192.168.125.254>;status=Connected
2020-09-22 14:32:28.425 28948-29019/org.jitsi D/Jitsi: [482] service.protocol.media.MediaHandler.initStream().972 Reinitializing stream: org.jitsi.impl.neomedia.AudioMediaStreamImpl@29e477b
2020-09-22 14:32:28.428 28948-29019/org.jitsi I/Jitsi: [482] service.protocol.media.MediaHandler.registerDynamicPTsWithStream().1026 Dynamic PT map: 96=rtpmap:-1 opus/48000/2 fmtp:usedtx=1; 102=rtpmap:-1 speex/16000; 100=rtpmap:-1 speex/32000; 104=rtpmap:-1 speex/8000; 99=rtpmap:-1 H264/90000 fmtp:profile-level-id=4DE01f; 105=rtpmap:-1 H264/90000 fmtp:packetization-mode=1;profile-level-id=4DE01f; 101=rtpmap:-1 telephone-event/8000; 103=rtpmap:-1 iLBC/8000; 97=rtpmap:-1 SILK/24000; 98=rtpmap:-1 SILK/16000;
2020-09-22 14:32:28.429 28948-29019/org.jitsi I/Jitsi: [482] service.protocol.media.MediaHandler.registerDynamicPTsWithStream().1043 PT overrides []
2020-09-22 14:32:28.431 28948-29019/org.jitsi D/Jitsi: [482] org.jitsi.impl.neomedia.transform.zrtp.SecurityEventManager.debug() AUDIO_SESSION: ZRTP message: severity: Info, sub code: ZRTPEnabledByDefault, DH session: true, multi: 0

audio时序图
2020-09-22 15:39:34.193 30612-30682/org.jitsi I/lhzheng@grandstream.com: initStream
    java.lang.Exception: Call Stack Trace
        at net.java.sip.communicator.service.protocol.media.CallPeerMediaHandler.initStream(CallPeerMediaHandler.java:1195)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.doNonSynchronisedProcessAnswer(CallPeerMediaHandlerSipImpl.java:1608)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.processAnswer(CallPeerMediaHandlerSipImpl.java:1415)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.processAnswer(CallPeerMediaHandlerSipImpl.java:1389)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerSipImpl.processInviteOK(CallPeerSipImpl.java:848)
        at net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl.processInviteOK(OperationSetBasicTelephonySipImpl.java:874)
        at net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl.processResponse(OperationSetBasicTelephonySipImpl.java:497)
        at net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.processResponse(ProtocolProviderServiceSipImpl.java:925)
        at net.java.sip.communicator.impl.protocol.sip.SipStackSharing.processResponse(SipStackSharing.java:839)
        at android.gov.nist.javax.sip.EventScanner.deliverResponseEvent(EventScanner.java:317)
        at android.gov.nist.javax.sip.EventScanner.deliverEvent(EventScanner.java:148)
        at android.gov.nist.javax.sip.EventScanner.run(EventScanner.java:513)
        at java.lang.Thread.run(Thread.java:761)
video流程
2020-09-22 14:32:28.448 28948-29019/org.jitsi D/Jitsi: [482] service.protocol.media.CallPeerMediaHandler.initStream().1194 Initializing video stream for 45645 <45645@192.168.125.254>;status=Connected
2020-09-22 14:32:28.449 28948-29019/org.jitsi D/Jitsi: [482] service.protocol.media.MediaHandler.initStream().972 Reinitializing stream: org.jitsi.impl.neomedia.VideoMediaStreamImpl@47a05d6
2020-09-22 14:32:28.451 28948-29019/org.jitsi I/Jitsi: [482] service.protocol.media.MediaHandler.registerDynamicPTsWithStream().1026 Dynamic PT map: 96=rtpmap:-1 opus/48000/2 fmtp:usedtx=1; 102=rtpmap:-1 speex/16000; 100=rtpmap:-1 speex/32000; 104=rtpmap:-1 speex/8000; 99=rtpmap:-1 H264/90000 fmtp:profile-level-id=4DE01f; 105=rtpmap:-1 H264/90000 fmtp:packetization-mode=1;profile-level-id=4DE01f; 101=rtpmap:-1 telephone-event/8000; 103=rtpmap:-1 iLBC/8000; 97=rtpmap:-1 SILK/24000; 98=rtpmap:-1 SILK/16000;
2020-09-22 14:32:28.453 28948-29019/org.jitsi I/Jitsi: [482] service.protocol.media.MediaHandler.registerDynamicPTsWithStream().1043 PT overrides []
2020-09-22 14:32:28.454 28948-29019/org.jitsi D/Jitsi: [482] org.jitsi.impl.neomedia.MediaStreamImpl.trace() Changing direction of stream 75105750 from:sendrecv to:recvonly
2020-09-22 14:32:28.456 28948-29019/org.jitsi D/Jitsi: [482] org.jitsi.impl.neomedia.device.MediaDeviceSession.trace() Stopped Processor with hashCode 27232714
2020-09-22 14:32:28.458 28948-29019/org.jitsi D/Jitsi: [482] org.jitsi.impl.neomedia.device.MediaDeviceSession.debug() processorFormat != format; processorFormat= `null`; format= `H264/RTP, 500x500, fmtps={packetization-mode=1,profile-level-id=4DE01f}`

video时序图

2020-09-22 15:39:34.318 30612-30682/org.jitsi I/lhzheng@grandstream.com: initStream
    java.lang.Exception: Call Stack Trace
        at net.java.sip.communicator.service.protocol.media.CallPeerMediaHandler.initStream(CallPeerMediaHandler.java:1195)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.doNonSynchronisedProcessAnswer(CallPeerMediaHandlerSipImpl.java:1608)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.processAnswer(CallPeerMediaHandlerSipImpl.java:1415)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerMediaHandlerSipImpl.processAnswer(CallPeerMediaHandlerSipImpl.java:1389)
        at net.java.sip.communicator.impl.protocol.sip.CallPeerSipImpl.processInviteOK(CallPeerSipImpl.java:848)
        at net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl.processInviteOK(OperationSetBasicTelephonySipImpl.java:874)
        at net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl.processResponse(OperationSetBasicTelephonySipImpl.java:497)
        at net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.processResponse(ProtocolProviderServiceSipImpl.java:925)
        at net.java.sip.communicator.impl.protocol.sip.SipStackSharing.processResponse(SipStackSharing.java:839)
        at android.gov.nist.javax.sip.EventScanner.deliverResponseEvent(EventScanner.java:317)
        at android.gov.nist.javax.sip.EventScanner.deliverEvent(EventScanner.java:148)
        at android.gov.nist.javax.sip.EventScanner.run(EventScanner.java:513)
        at java.lang.Thread.run(Thread.java:761)

# invite/200OK信令的SDP信息
本地发送Invite消息
    v=0
    o=78997-jitsi.org 0 0 IN IP4 192.168.122.100
    s=-
    c=IN IP4 192.168.122.100
    t=0 0
    m=audio 5000 RTP/AVP 96 97 98 9 100 102 0 8 103 3 104 101
    a=rtpmap:96 opus/48000/2
    a=fmtp:96 usedtx=1
    a=rtpmap:97 SILK/24000
    a=rtpmap:98 SILK/16000
    a=rtpmap:9 G722/8000
    a=rtpmap:100 speex/32000
    a=rtpmap:102 speex/16000
    a=rtpmap:0 PCMU/8000
    a=rtpmap:8 PCMA/8000
    a=rtpmap:103 iLBC/8000
    a=rtpmap:3 GSM/8000
    a=rtpmap:104 speex/8000
    a=rtpmap:101 telephone-event/8000
    a=extmap:1/recvonly urn:ietf:params:rtp-hdrext:csrc-audio-level
    a=rtcp-xr:voip-metrics
    m=video 5002 RTP/AVP 105 99
    a=recvonly
    a=rtpmap:105 H264/90000
    a=fmtp:105 packetization-mode=1;profile-level-id=4DE01f
    a=imageattr:105 send * recv *
    a=rtpmap:99 H264/90000
    a=fmtp:99 profile-level-id=4DE01f
    a=imageattr:99 send * recv *

invite后对端回的200K消息
    v=0
    o=45645 8000 8000 IN IP4 192.168.122.105
    s=SIP Call
    c=IN IP4 192.168.122.105
    t=0 0
    m=audio 50040 RTP/AVP 0 8 9 101
    a=sendrecv
    a=rtcp:50041 IN IP4 192.168.122.105
    a=rtpmap:0 PCMU/8000
    a=ptime:20
    a=rtpmap:8 PCMA/8000
    a=rtpmap:9 G722/8000
    a=rtpmap:101 telephone-event/8000
    a=fmtp:101 0-15
    m=video 50042 RTP/AVP 105
    a=sendonly
    a=rtcp:50043 IN IP4 192.168.122.105
    a=rtpmap:105 H264/90000
    a=fmtp:105 profile-level-id=4DE01f; packetization-mode=1
    a=imageattr:105 send [x=1920,y=1080] recv [x=1920,y=1080]
    a=content:main
    a=label:11

# 媒体渲染处理时序图

RTP流渲染流程, 本地是发起方
CallPeerMediaHandlerSipImpl.doNonSynchronisedProcessAnswer
	CallPeerMediaHandler.initStream
		MediaHandler.initStream
			MediaHandler.configureStream
				MediaServiceImpl.createMediaStream(4 params)
					VideoMediaStreamImpl.init
						MediaStreamImpl.setDevice (MediaStreamImpl.update)
							MediaDeviceSession.addReceiveStream
								MediaDeviceSession.addPlaybackDataSource
									MediaDeviceSession.createPlayer

RTP渲染流程，本地接听
ReceivedCallActivity.(CallButtonClickedListener)answerCall
	ReceivedCallActivity.answerCall
		CallManager.answerCall(2 params)
			CallManager.answerCall(3 params)
				AnswerCallThread.init.run
					ProtocolProviderServiceSipImpl.getOperationSet
						OperationSetVideoTelephonySipImpl.answerVideoCallPeer
							CallPeerSipImpl.answer
								CallPeerMediaHandlerSipImpl.processOffer
									CallPeerMediaHandlerSipImpl.processFirstOffer(processUpdateOffer)
										CallPeerMediaHandlerSipImpl.createMediaDescriptionsForAnswer
											CallPeerMediaHandlerSipImpl.initStream
											
											
本地接听，处理对端发送来的INVITE消息
CallPeerMediaHandlerSipImpl.processFirstOffer
    CallPeerMediaHandlerSipImpl.createMediaDescriptionsForAnswer
    
    
通话界面，摄像头按钮切换调用流程，实际需要重新发一个invite请求给对端, 调用栈如下
startedDirectionChanged:2096, MediaDeviceSession (org.jitsi.impl.neomedia.device)
startedDirectionChanged:1882, VideoMediaDeviceSession (org.jitsi.impl.neomedia.device)
stop:2183, MediaDeviceSession (org.jitsi.impl.neomedia.device)
stop:2249, MediaStreamImpl (org.jitsi.impl.neomedia)
setDirection:1836, MediaStreamImpl (org.jitsi.impl.neomedia)
configureStream:717, MediaHandler (net.java.sip.communicator.service.protocol.media)
initStream:976, MediaHandler (net.java.sip.communicator.service.protocol.media)
initStream:1216, CallPeerMediaHandler (net.java.sip.communicator.service.protocol.media)
doNonSynchronisedProcessAnswer:1623, CallPeerMediaHandlerSipImpl (net.java.sip.communicator.impl.protocol.sip)
processAnswer:1430, CallPeerMediaHandlerSipImpl (net.java.sip.communicator.impl.protocol.sip)
processAnswer:1404, CallPeerMediaHandlerSipImpl (net.java.sip.communicator.impl.protocol.sip)
processInviteOK:848, CallPeerSipImpl (net.java.sip.communicator.impl.protocol.sip)
processInviteOK:874, OperationSetBasicTelephonySipImpl (net.java.sip.communicator.impl.protocol.sip)
processResponse:497, OperationSetBasicTelephonySipImpl (net.java.sip.communicator.impl.protocol.sip)
processResponse:925, ProtocolProviderServiceSipImpl (net.java.sip.communicator.impl.protocol.sip)
processResponse:839, SipStackSharing (net.java.sip.communicator.impl.protocol.sip)
deliverResponseEvent:317, EventScanner (android.gov.nist.javax.sip)
deliverEvent:148, EventScanner (android.gov.nist.javax.sip)
run:513, EventScanner (android.gov.nist.javax.sip)
run:761, Thread (java.lang)


远端画面相关事件在MediaStreamImpl.update 处理 ， 切换视频时，经常出现远端画面消失，是由于  update中处理到了TimeoutEvent将Player
dispose掉导致，但是不处理导致界面卡死。 