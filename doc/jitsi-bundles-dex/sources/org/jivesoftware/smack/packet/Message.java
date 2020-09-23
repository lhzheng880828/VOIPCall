package org.jivesoftware.smack.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.util.StringUtils;

public class Message extends Packet {
    private final Set<Body> bodies = new HashSet();
    private String language;
    private final Set<Subject> subjects = new HashSet();
    private String thread = null;
    private Type type = Type.normal;

    public static class Body {
        /* access modifiers changed from: private */
        public String language;
        /* access modifiers changed from: private */
        public String message;

        private Body(String language, String message) {
            if (language == null) {
                throw new NullPointerException("Language cannot be null.");
            } else if (message == null) {
                throw new NullPointerException("Message cannot be null.");
            } else {
                this.language = language;
                this.message = message;
            }
        }

        public String getLanguage() {
            return this.language;
        }

        public String getMessage() {
            return this.message;
        }

        public int hashCode() {
            return ((this.language.hashCode() + 31) * 31) + this.message.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Body other = (Body) obj;
            if (this.language.equals(other.language) && this.message.equals(other.message)) {
                return true;
            }
            return false;
        }
    }

    public static class Subject {
        /* access modifiers changed from: private */
        public String language;
        /* access modifiers changed from: private */
        public String subject;

        private Subject(String language, String subject) {
            if (language == null) {
                throw new NullPointerException("Language cannot be null.");
            } else if (subject == null) {
                throw new NullPointerException("Subject cannot be null.");
            } else {
                this.language = language;
                this.subject = subject;
            }
        }

        public String getLanguage() {
            return this.language;
        }

        public String getSubject() {
            return this.subject;
        }

        public int hashCode() {
            return ((this.language.hashCode() + 31) * 31) + this.subject.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Subject other = (Subject) obj;
            if (this.language.equals(other.language) && this.subject.equals(other.subject)) {
                return true;
            }
            return false;
        }
    }

    public enum Type {
        normal,
        chat,
        groupchat,
        headline,
        error;

        public static Type fromString(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return normal;
            }
        }
    }

    public Message(String to) {
        setTo(to);
    }

    public Message(String to, Type type) {
        setTo(to);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        this.type = type;
    }

    public String getSubject() {
        return getSubject(null);
    }

    public String getSubject(String language) {
        Subject subject = getMessageSubject(language);
        return subject == null ? null : subject.subject;
    }

    private Subject getMessageSubject(String language) {
        language = determineLanguage(language);
        for (Subject subject : this.subjects) {
            if (language.equals(subject.language)) {
                return subject;
            }
        }
        return null;
    }

    public Collection<Subject> getSubjects() {
        return Collections.unmodifiableCollection(this.subjects);
    }

    public void setSubject(String subject) {
        if (subject == null) {
            removeSubject("");
        } else {
            addSubject(null, subject);
        }
    }

    public Subject addSubject(String language, String subject) {
        Subject messageSubject = new Subject(determineLanguage(language), subject);
        this.subjects.add(messageSubject);
        return messageSubject;
    }

    public boolean removeSubject(String language) {
        language = determineLanguage(language);
        for (Subject subject : this.subjects) {
            if (language.equals(subject.language)) {
                return this.subjects.remove(subject);
            }
        }
        return false;
    }

    public boolean removeSubject(Subject subject) {
        return this.subjects.remove(subject);
    }

    public Collection<String> getSubjectLanguages() {
        Subject defaultSubject = getMessageSubject(null);
        List<String> languages = new ArrayList();
        for (Subject subject : this.subjects) {
            if (!subject.equals(defaultSubject)) {
                languages.add(subject.language);
            }
        }
        return Collections.unmodifiableCollection(languages);
    }

    public String getBody() {
        return getBody(null);
    }

    public String getBody(String language) {
        Body body = getMessageBody(language);
        return body == null ? null : body.message;
    }

    private Body getMessageBody(String language) {
        language = determineLanguage(language);
        for (Body body : this.bodies) {
            if (language.equals(body.language)) {
                return body;
            }
        }
        return null;
    }

    public Collection<Body> getBodies() {
        return Collections.unmodifiableCollection(this.bodies);
    }

    public void setBody(String body) {
        if (body == null) {
            removeBody("");
        } else {
            addBody(null, body);
        }
    }

    public Body addBody(String language, String body) {
        Body messageBody = new Body(determineLanguage(language), body);
        this.bodies.add(messageBody);
        return messageBody;
    }

    public boolean removeBody(String language) {
        language = determineLanguage(language);
        for (Body body : this.bodies) {
            if (language.equals(body.language)) {
                return this.bodies.remove(body);
            }
        }
        return false;
    }

    public boolean removeBody(Body body) {
        return this.bodies.remove(body);
    }

    public Collection<String> getBodyLanguages() {
        Body defaultBody = getMessageBody(null);
        List<String> languages = new ArrayList();
        for (Body body : this.bodies) {
            if (!body.equals(defaultBody)) {
                languages.add(body.language);
            }
        }
        return Collections.unmodifiableCollection(languages);
    }

    public String getThread() {
        return this.thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    private String determineLanguage(String language) {
        if ("".equals(language)) {
            language = null;
        }
        if (language == null && this.language != null) {
            return this.language;
        }
        if (language == null) {
            return Packet.getDefaultLanguage();
        }
        return language;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<message");
        if (getXmlns() != null) {
            buf.append(" xmlns=\"").append(getXmlns()).append(Separators.DOUBLE_QUOTE);
        }
        if (this.language != null) {
            buf.append(" xml:lang=\"").append(getLanguage()).append(Separators.DOUBLE_QUOTE);
        }
        if (getPacketID() != null) {
            buf.append(" id=\"").append(getPacketID()).append(Separators.DOUBLE_QUOTE);
        }
        if (getTo() != null) {
            buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append(Separators.DOUBLE_QUOTE);
        }
        if (getFrom() != null) {
            buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append(Separators.DOUBLE_QUOTE);
        }
        if (this.type != Type.normal) {
            buf.append(" type=\"").append(this.type).append(Separators.DOUBLE_QUOTE);
        }
        buf.append(Separators.GREATER_THAN);
        Subject defaultSubject = getMessageSubject(null);
        if (defaultSubject != null) {
            buf.append("<subject>").append(StringUtils.escapeForXML(defaultSubject.subject)).append("</subject>");
        }
        for (Subject subject : getSubjects()) {
            if (!subject.equals(defaultSubject)) {
                buf.append("<subject xml:lang=\"").append(subject.language).append("\">");
                buf.append(StringUtils.escapeForXML(subject.subject));
                buf.append("</subject>");
            }
        }
        Body defaultBody = getMessageBody(null);
        if (defaultBody != null) {
            buf.append("<body>").append(StringUtils.escapeForXML(defaultBody.message)).append("</body>");
        }
        for (Body body : getBodies()) {
            if (!body.equals(defaultBody)) {
                buf.append("<body xml:lang=\"").append(body.getLanguage()).append("\">");
                buf.append(StringUtils.escapeForXML(body.getMessage()));
                buf.append("</body>");
            }
        }
        if (this.thread != null) {
            buf.append("<thread>").append(this.thread).append("</thread>");
        }
        if (this.type == Type.error) {
            XMPPError error = getError();
            if (error != null) {
                buf.append(error.toXML());
            }
        }
        buf.append(getExtensionsXML());
        buf.append("</message>");
        return buf.toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        if (!super.equals(message) || this.bodies.size() != message.bodies.size() || !this.bodies.containsAll(message.bodies)) {
            return false;
        }
        if (this.language != null) {
            if (!this.language.equals(message.language)) {
                return false;
            }
        } else if (message.language != null) {
            return false;
        }
        if (this.subjects.size() != message.subjects.size() || !this.subjects.containsAll(message.subjects)) {
            return false;
        }
        if (this.thread != null) {
            if (!this.thread.equals(message.thread)) {
                return false;
            }
        } else if (message.thread != null) {
            return false;
        }
        if (this.type != message.type) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int hashCode;
        int i = 0;
        if (this.type != null) {
            result = this.type.hashCode();
        } else {
            result = 0;
        }
        int hashCode2 = ((result * 31) + this.subjects.hashCode()) * 31;
        if (this.thread != null) {
            hashCode = this.thread.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode2 + hashCode) * 31;
        if (this.language != null) {
            i = this.language.hashCode();
        }
        return ((hashCode + i) * 31) + this.bodies.hashCode();
    }
}
