package net.java.sip.communicator.impl.netaddr;

import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.service.sysactivity.SystemActivityChangeListener;
import net.java.sip.communicator.service.sysactivity.SystemActivityNotificationsService;
import net.java.sip.communicator.service.sysactivity.event.SystemActivityEvent;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NetworkConfigurationWatcher implements SystemActivityChangeListener, ServiceListener, Runnable {
    private static final int CHECK_INTERVAL = 3000;
    private static Logger logger = Logger.getLogger(NetworkConfigurationWatcher.class);
    private Map<String, List<InetAddress>> activeInterfaces = new HashMap();
    private NetworkEventDispatcher eventDispatcher = new NetworkEventDispatcher();
    private boolean isRunning = false;
    private SystemActivityNotificationsService systemActivityNotificationsService = null;

    NetworkConfigurationWatcher() {
        try {
            checkNetworkInterfaces(false, 0, true);
        } catch (SocketException e) {
            logger.error("Error checking network interfaces", e);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addNetworkConfigurationChangeListener(NetworkConfigurationChangeListener listener) {
        this.eventDispatcher.addNetworkConfigurationChangeListener(listener);
        initialFireEvents(listener);
        NetaddrActivator.getBundleContext().addServiceListener(this);
        if (this.systemActivityNotificationsService == null) {
            handleNewSystemActivityNotificationsService((SystemActivityNotificationsService) ServiceUtils.getService(NetaddrActivator.getBundleContext(), SystemActivityNotificationsService.class));
        }
    }

    private void initialFireEvents(NetworkConfigurationChangeListener listener) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) e.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    Enumeration<InetAddress> as = networkInterface.getInetAddresses();
                    boolean hasAddress = false;
                    while (as.hasMoreElements()) {
                        InetAddress inetAddress = (InetAddress) as.nextElement();
                        if (!inetAddress.isLinkLocalAddress()) {
                            hasAddress = true;
                            NetworkEventDispatcher.fireChangeEvent(new ChangeEvent(networkInterface.getName(), 3, inetAddress, false, true), listener);
                        }
                    }
                    if (hasAddress) {
                        NetworkEventDispatcher.fireChangeEvent(new ChangeEvent(networkInterface.getName(), 1, null, false, true), listener);
                    }
                }
            }
        } catch (SocketException e2) {
            logger.error("Error checking network interfaces", e2);
        }
    }

    private void handleNewSystemActivityNotificationsService(SystemActivityNotificationsService newService) {
        if (newService != null) {
            this.systemActivityNotificationsService = newService;
            if (this.systemActivityNotificationsService.isSupported(9)) {
                this.systemActivityNotificationsService.addSystemActivityChangeListener(this);
            } else if (!this.isRunning) {
                this.isRunning = true;
                Thread th = new Thread(this);
                th.setPriority(10);
                th.start();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeNetworkConfigurationChangeListener(NetworkConfigurationChangeListener listener) {
        this.eventDispatcher.removeNetworkConfigurationChangeListener(listener);
    }

    public void serviceChanged(ServiceEvent serviceEvent) {
        ServiceReference serviceRef = serviceEvent.getServiceReference();
        if (serviceRef.getBundle().getState() != 16) {
            Object sService = NetaddrActivator.getBundleContext().getService(serviceRef);
            if (sService instanceof SystemActivityNotificationsService) {
                switch (serviceEvent.getType()) {
                    case 1:
                        if (this.systemActivityNotificationsService == null) {
                            handleNewSystemActivityNotificationsService((SystemActivityNotificationsService) sService);
                            return;
                        }
                        return;
                    case 4:
                        ((SystemActivityNotificationsService) sService).removeSystemActivityChangeListener(this);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void stop() {
        if (this.isRunning) {
            synchronized (this) {
                this.isRunning = false;
                notifyAll();
            }
        }
        if (this.eventDispatcher != null) {
            this.eventDispatcher.stop();
        }
    }

    public void activityChanged(SystemActivityEvent event) {
        if (event.getEventID() == 0) {
            downAllInterfaces();
        } else if (event.getEventID() == 9) {
            try {
                checkNetworkInterfaces(true, 0, true);
            } catch (SocketException e) {
                logger.error("Error checking network interfaces", e);
            }
        } else if (event.getEventID() == 12) {
            try {
                this.eventDispatcher.fireChangeEvent(new ChangeEvent(event.getSource(), 4));
            } catch (Throwable th) {
                logger.error("Error dispatching dns change.");
            }
        }
    }

    private void downAllInterfaces() {
        for (Object niface : this.activeInterfaces.keySet()) {
            this.eventDispatcher.fireChangeEvent(new ChangeEvent(niface, 0, true));
        }
        this.activeInterfaces.clear();
    }

    private void checkNetworkInterfaces(boolean fireEvents, int waitBeforeFiringUpEvents, boolean printDebugInfo) throws SocketException {
        List<InetAddress> addresses;
        InetAddress addr;
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        Map<String, List<InetAddress>> currentActiveInterfaces = new HashMap();
        while (e.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) e.nextElement();
            if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                addresses = new ArrayList();
                Enumeration<InetAddress> as = networkInterface.getInetAddresses();
                while (as.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) as.nextElement();
                    if (!inetAddress.isLinkLocalAddress()) {
                        addresses.add(inetAddress);
                    }
                }
                if (addresses.size() > 0) {
                    currentActiveInterfaces.put(networkInterface.getName(), addresses);
                }
            }
        }
        if (logger.isInfoEnabled() && printDebugInfo) {
            for (Entry<String, List<InetAddress>> en : this.activeInterfaces.entrySet()) {
                logger.info("Previously Active " + ((String) en.getKey()) + ":" + en.getValue());
            }
            for (Entry<String, List<InetAddress>> en2 : currentActiveInterfaces.entrySet()) {
                logger.info("Currently Active " + ((String) en2.getKey()) + ":" + en2.getValue());
            }
        }
        List<String> arrayList = new ArrayList(this.activeInterfaces.keySet());
        List<String> currentActiveInterfacesSet = new ArrayList(currentActiveInterfaces.keySet());
        arrayList.removeAll(currentActiveInterfacesSet);
        for (int i = 0; i < arrayList.size(); i++) {
            String iface = (String) arrayList.get(i);
            if (!currentActiveInterfacesSet.contains(iface)) {
                if (fireEvents) {
                    this.eventDispatcher.fireChangeEvent(new ChangeEvent(iface, 0));
                }
                this.activeInterfaces.remove(iface);
            }
        }
        for (Entry<String, List<InetAddress>> entry : this.activeInterfaces.entrySet()) {
            Iterator<InetAddress> addrIter = ((List) entry.getValue()).iterator();
            while (addrIter.hasNext()) {
                addr = (InetAddress) addrIter.next();
                addresses = (List) currentActiveInterfaces.get(entry.getKey());
                if (!(addresses == null || addresses.contains(addr))) {
                    if (fireEvents) {
                        this.eventDispatcher.fireChangeEvent(new ChangeEvent(entry.getKey(), 2, addr));
                    }
                    addrIter.remove();
                }
            }
        }
        if (waitBeforeFiringUpEvents > 0 && currentActiveInterfaces.size() != 0) {
            synchronized (this) {
                try {
                    wait((long) waitBeforeFiringUpEvents);
                } catch (InterruptedException e2) {
                }
            }
        }
        for (Entry<String, List<InetAddress>> entry2 : currentActiveInterfaces.entrySet()) {
            for (InetAddress addr2 : (List<InetAddress>) entry2.getValue()) {
                addresses = (List) this.activeInterfaces.get(entry2.getKey());
                if (!(addresses == null || addresses.contains(addr2))) {
                    if (fireEvents) {
                        this.eventDispatcher.fireChangeEvent(new ChangeEvent(entry2.getKey(), 3, addr2));
                    }
                    addresses.add(addr2);
                }
            }
        }
        for (Object remove : this.activeInterfaces.keySet()) {
            currentActiveInterfaces.remove(remove);
        }
        for (Entry<String, List<InetAddress>> entry22 : currentActiveInterfaces.entrySet()) {
            for (InetAddress addr22 : (List<InetAddress>) entry22.getValue()) {
                if (fireEvents) {
                    this.eventDispatcher.fireChangeEvent(new ChangeEvent(entry22.getKey(), 3, addr22));
                }
            }
            if (fireEvents) {
                int wait = waitBeforeFiringUpEvents;
                if (wait == 0) {
                    wait = 500;
                }
                this.eventDispatcher.fireChangeEvent(new ChangeEvent(entry22.getKey(), 1), wait);
            }
            this.activeInterfaces.put(entry22.getKey(), entry22.getValue());
        }
    }

    public void run() {
        long last = 0;
        boolean isAfterStandby = false;
        while (this.isRunning) {
            long curr = System.currentTimeMillis();
            if (!(isAfterStandby || last == 0)) {
                isAfterStandby = (12000 + last) - curr < 0;
            }
            if (isAfterStandby) {
                downAllInterfaces();
                isAfterStandby = false;
                last = curr;
                synchronized (this) {
                    try {
                        wait(3000);
                    } catch (Exception e) {
                    }
                }
            } else {
                try {
                    boolean networkIsUP = this.activeInterfaces.size() > 0;
                    checkNetworkInterfaces(true, 1000, false);
                    if (!networkIsUP && this.activeInterfaces.size() > 0) {
                        isAfterStandby = false;
                    }
                    last = System.currentTimeMillis();
                } catch (SocketException e2) {
                    logger.error("Error checking network interfaces", e2);
                }
                synchronized (this) {
                    try {
                        wait(3000);
                    } catch (Exception e3) {
                    }
                }
            }
        }
    }
}
