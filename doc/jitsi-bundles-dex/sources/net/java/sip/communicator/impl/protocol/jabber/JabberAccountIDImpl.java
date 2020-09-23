package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.StunServerDescriptor;
import net.java.sip.communicator.service.protocol.jabber.JabberAccountID;
import org.jitsi.gov.nist.core.Separators;

public class JabberAccountIDImpl extends JabberAccountID {
    JabberAccountIDImpl(String id, Map<String, String> accountProperties) {
        super(id, accountProperties);
    }

    public List<StunServerDescriptor> getStunServers() {
        Map<String, String> accountProperties = getAccountProperties();
        List<StunServerDescriptor> serList = new ArrayList();
        for (int i = 0; i < 100; i++) {
            StunServerDescriptor stunServer = StunServerDescriptor.loadDescriptor(accountProperties, "STUN" + i);
            if (stunServer == null) {
                break;
            }
            String password = loadStunPassword("STUN" + i);
            if (password != null) {
                stunServer.setPassword(password);
            }
            serList.add(stunServer);
        }
        return serList;
    }

    private String loadStunPassword(String namePrefix) {
        String className = ProtocolProviderServiceJabberImpl.class.getName();
        String accountPrefix = ProtocolProviderFactory.findAccountPrefix(JabberActivator.bundleContext, this, className.substring(0, className.lastIndexOf(46)));
        try {
            return JabberActivator.getCredentialsStorageService().loadPassword(accountPrefix + Separators.DOT + namePrefix);
        } catch (Exception e) {
            return null;
        }
    }
}
