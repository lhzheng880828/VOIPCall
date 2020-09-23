package org.dhcp4java;

import java.net.DatagramPacket;
import java.util.logging.Level;
import java.util.logging.Logger;

/* compiled from: DHCPCoreServer */
class DHCPServletDispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(DHCPServletDispatcher.class.getName().toLowerCase());
    private final DatagramPacket dispatchPacket;
    private final DHCPServlet dispatchServlet;
    private final DHCPCoreServer server;

    public DHCPServletDispatcher(DHCPCoreServer server, DHCPServlet servlet, DatagramPacket req) {
        this.server = server;
        this.dispatchServlet = servlet;
        this.dispatchPacket = req;
    }

    public void run() {
        try {
            this.server.sendResponse(this.dispatchServlet.serviceDatagram(this.dispatchPacket));
        } catch (Exception e) {
            logger.log(Level.FINE, "Exception in dispatcher", e);
        }
    }
}
