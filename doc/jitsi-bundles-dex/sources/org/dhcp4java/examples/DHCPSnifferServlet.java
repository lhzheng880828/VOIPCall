package org.dhcp4java.examples;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

public class DHCPSnifferServlet extends DHCPServlet {
    private static final Logger logger = Logger.getLogger("org.dhcp4java.examples.dhcpsnifferservlet");

    public DHCPPacket service(DHCPPacket request) {
        logger.info(request.toString());
        return null;
    }

    public static void main(String[] args) {
        try {
            new Thread(DHCPCoreServer.initServer(new DHCPSnifferServlet(), null)).start();
        } catch (DHCPServerInitException e) {
            logger.log(Level.SEVERE, "Server init", e);
        }
    }
}
