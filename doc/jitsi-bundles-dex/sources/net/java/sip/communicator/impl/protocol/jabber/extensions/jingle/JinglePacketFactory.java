package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.List;
import org.jivesoftware.smack.packet.IQ.Type;

public class JinglePacketFactory {
    public static JingleIQ createRinging(JingleIQ sessionInitiate) {
        return createSessionInfo(sessionInitiate.getTo(), sessionInitiate.getFrom(), sessionInitiate.getSID(), SessionInfoType.ringing);
    }

    public static JingleIQ createSessionInfo(String from, String to, String sid) {
        JingleIQ sessionInfo = new JingleIQ();
        sessionInfo.setFrom(from);
        sessionInfo.setTo(to);
        sessionInfo.setType(Type.SET);
        sessionInfo.setSID(sid);
        sessionInfo.setAction(JingleAction.SESSION_INFO);
        return sessionInfo;
    }

    public static JingleIQ createSessionInfo(String from, String to, String sid, SessionInfoType type) {
        JingleIQ ringing = createSessionInfo(from, to, sid);
        ringing.setSessionInfo(new SessionInfoPacketExtension(type));
        return ringing;
    }

    public static JingleIQ createBusy(String from, String to, String sid) {
        return createSessionTerminate(from, to, sid, Reason.BUSY, null);
    }

    public static JingleIQ createBye(String from, String to, String sid) {
        return createSessionTerminate(from, to, sid, Reason.SUCCESS, "Nice talking to you!");
    }

    public static JingleIQ createCancel(String from, String to, String sid) {
        return createSessionTerminate(from, to, sid, Reason.CANCEL, "Oops!");
    }

    public static JingleIQ createSessionTerminate(String from, String to, String sid, Reason reason, String reasonText) {
        JingleIQ terminate = new JingleIQ();
        terminate.setTo(to);
        terminate.setFrom(from);
        terminate.setType(Type.SET);
        terminate.setSID(sid);
        terminate.setAction(JingleAction.SESSION_TERMINATE);
        terminate.setReason(new ReasonPacketExtension(reason, reasonText, null));
        return terminate;
    }

    public static JingleIQ createSessionAccept(String from, String to, String sid, Iterable<ContentPacketExtension> contentList) {
        JingleIQ sessionAccept = new JingleIQ();
        sessionAccept.setTo(to);
        sessionAccept.setFrom(from);
        sessionAccept.setResponder(from);
        sessionAccept.setType(Type.SET);
        sessionAccept.setSID(sid);
        sessionAccept.setAction(JingleAction.SESSION_ACCEPT);
        for (ContentPacketExtension content : contentList) {
            sessionAccept.addContent(content);
        }
        return sessionAccept;
    }

    public static JingleIQ createDescriptionInfo(String from, String to, String sid, Iterable<ContentPacketExtension> contentList) {
        JingleIQ descriptionInfo = new JingleIQ();
        descriptionInfo.setTo(to);
        descriptionInfo.setFrom(from);
        descriptionInfo.setResponder(from);
        descriptionInfo.setType(Type.SET);
        descriptionInfo.setSID(sid);
        descriptionInfo.setAction(JingleAction.DESCRIPTION_INFO);
        for (ContentPacketExtension content : contentList) {
            descriptionInfo.addContent(content);
        }
        return descriptionInfo;
    }

    public static JingleIQ createSessionInitiate(String from, String to, String sid, List<ContentPacketExtension> contentList) {
        JingleIQ sessionInitiate = new JingleIQ();
        sessionInitiate.setTo(to);
        sessionInitiate.setFrom(from);
        sessionInitiate.setInitiator(from);
        sessionInitiate.setType(Type.SET);
        sessionInitiate.setSID(sid);
        sessionInitiate.setAction(JingleAction.SESSION_INITIATE);
        for (ContentPacketExtension content : contentList) {
            sessionInitiate.addContent(content);
        }
        return sessionInitiate;
    }

    public static JingleIQ createContentAdd(String from, String to, String sid, List<ContentPacketExtension> contentList) {
        JingleIQ contentAdd = new JingleIQ();
        contentAdd.setTo(to);
        contentAdd.setFrom(from);
        contentAdd.setType(Type.SET);
        contentAdd.setSID(sid);
        contentAdd.setAction(JingleAction.CONTENT_ADD);
        for (ContentPacketExtension content : contentList) {
            contentAdd.addContent(content);
        }
        return contentAdd;
    }

    public static JingleIQ createContentAccept(String from, String to, String sid, Iterable<ContentPacketExtension> contentList) {
        JingleIQ contentAccept = new JingleIQ();
        contentAccept.setTo(to);
        contentAccept.setFrom(from);
        contentAccept.setType(Type.SET);
        contentAccept.setSID(sid);
        contentAccept.setAction(JingleAction.CONTENT_ACCEPT);
        for (ContentPacketExtension content : contentList) {
            contentAccept.addContent(content);
        }
        return contentAccept;
    }

    public static JingleIQ createContentReject(String from, String to, String sid, Iterable<ContentPacketExtension> contentList) {
        JingleIQ contentReject = new JingleIQ();
        contentReject.setTo(to);
        contentReject.setFrom(from);
        contentReject.setType(Type.SET);
        contentReject.setSID(sid);
        contentReject.setAction(JingleAction.CONTENT_REJECT);
        if (contentList != null) {
            for (ContentPacketExtension content : contentList) {
                contentReject.addContent(content);
            }
        }
        return contentReject;
    }

    public static JingleIQ createContentModify(String from, String to, String sid, ContentPacketExtension content) {
        JingleIQ contentModify = new JingleIQ();
        contentModify.setTo(to);
        contentModify.setFrom(from);
        contentModify.setType(Type.SET);
        contentModify.setSID(sid);
        contentModify.setAction(JingleAction.CONTENT_MODIFY);
        contentModify.addContent(content);
        return contentModify;
    }

    public static JingleIQ createContentRemove(String from, String to, String sid, Iterable<ContentPacketExtension> contentList) {
        JingleIQ contentRemove = new JingleIQ();
        contentRemove.setTo(to);
        contentRemove.setFrom(from);
        contentRemove.setType(Type.SET);
        contentRemove.setSID(sid);
        contentRemove.setAction(JingleAction.CONTENT_REMOVE);
        for (ContentPacketExtension content : contentList) {
            contentRemove.addContent(content);
        }
        return contentRemove;
    }
}
