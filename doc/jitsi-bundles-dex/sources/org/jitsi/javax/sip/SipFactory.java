package org.jitsi.javax.sip;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.address.AddressFactory;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.message.MessageFactory;

public class SipFactory {
    private static SipFactory myFactory = null;
    private AddressFactory addressFactory = null;
    private HeaderFactory headerFactory = null;
    private MessageFactory messageFactory = null;
    private String pathName = "gov.nist";
    private Hashtable sipStackByName = new Hashtable();
    private final LinkedList sipStackList = new LinkedList();

    public static synchronized SipFactory getInstance() {
        SipFactory sipFactory;
        synchronized (SipFactory.class) {
            if (myFactory == null) {
                myFactory = new SipFactory();
            }
            sipFactory = myFactory;
        }
        return sipFactory;
    }

    public synchronized SipStack createSipStack(Properties properties) throws PeerUnavailableException {
        SipStack mySipStack;
        String ipAddress = properties.getProperty("org.jitsi.javax.sip.IP_ADDRESS");
        String name = properties.getProperty("org.jitsi.javax.sip.STACK_NAME");
        if (name == null) {
            throw new PeerUnavailableException("Missing javax.sip.STACK_NAME property");
        } else if (ipAddress == null) {
            mySipStack = (SipStack) this.sipStackByName.get(name);
            if (mySipStack == null) {
                mySipStack = createStack(properties);
            }
        } else {
            for (int i = 0; i < this.sipStackList.size(); i++) {
                if (((SipStack) this.sipStackList.get(i)).getIPAddress().equals(ipAddress)) {
                    mySipStack = (SipStack) this.sipStackList.get(i);
                    break;
                }
            }
            mySipStack = createStack(properties);
        }
        return mySipStack;
    }

    public MessageFactory createMessageFactory() throws PeerUnavailableException {
        if (this.messageFactory == null) {
            this.messageFactory = (MessageFactory) createSipFactory("javax.sip.message.MessageFactoryImpl");
        }
        return this.messageFactory;
    }

    public HeaderFactory createHeaderFactory() throws PeerUnavailableException {
        if (this.headerFactory == null) {
            this.headerFactory = (HeaderFactory) createSipFactory("javax.sip.header.HeaderFactoryImpl");
        }
        return this.headerFactory;
    }

    public AddressFactory createAddressFactory() throws PeerUnavailableException {
        if (this.addressFactory == null) {
            this.addressFactory = (AddressFactory) createSipFactory("javax.sip.address.AddressFactoryImpl");
        }
        return this.addressFactory;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getPathName() {
        return this.pathName;
    }

    public void resetFactory() {
        this.sipStackList.clear();
        this.messageFactory = null;
        this.headerFactory = null;
        this.addressFactory = null;
        this.sipStackByName = new Hashtable();
        this.pathName = "gov.nist";
    }

    private Object createSipFactory(String objectClassName) throws PeerUnavailableException {
        if (objectClassName == null) {
            throw new NullPointerException();
        }
        try {
            return Class.forName(getPathName() + Separators.DOT + objectClassName).newInstance();
        } catch (Exception e) {
            throw new PeerUnavailableException("The Peer Factory: " + getPathName() + Separators.DOT + objectClassName + " could not be instantiated. Ensure the Path Name has been set.", e);
        }
    }

    private SipStack createStack(Properties properties) throws PeerUnavailableException {
        try {
            SipStack sipStack = (SipStack) Class.forName(getPathName() + ".javax.sip.SipStackImpl").getConstructor(new Class[]{Class.forName("java.util.Properties")}).newInstance(new Object[]{properties});
            this.sipStackList.add(sipStack);
            this.sipStackByName.put(properties.getProperty("org.jitsi.javax.sip.STACK_NAME"), sipStack);
            return sipStack;
        } catch (Exception e) {
            throw new PeerUnavailableException("The Peer SIP Stack: " + getPathName() + ".javax.sip.SipStackImpl" + " could not be instantiated. Ensure the Path Name has been set.", e);
        }
    }

    private SipFactory() {
    }
}
