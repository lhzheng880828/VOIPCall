package net.java.sip.communicator.service.netaddr.event;

import java.net.InetAddress;
import java.util.EventObject;

public class ChangeEvent extends EventObject {
    public static final int ADDRESS_DOWN = 2;
    public static final int ADDRESS_UP = 3;
    public static final int DNS_CHANGE = 4;
    public static final int IFACE_DOWN = 0;
    public static final int IFACE_UP = 1;
    private static final long serialVersionUID = 0;
    private InetAddress address;
    private boolean initial;
    private boolean standby;
    private int type;

    public ChangeEvent(Object source, int type, InetAddress address, boolean standby, boolean initial) {
        super(source);
        this.type = -1;
        this.standby = false;
        this.type = type;
        this.address = address;
        this.standby = standby;
        this.initial = initial;
    }

    public ChangeEvent(Object source, int type, InetAddress address) {
        this(source, type, address, false, false);
    }

    public ChangeEvent(Object source, int type) {
        this(source, type, null, false, false);
    }

    public ChangeEvent(Object source, int type, boolean standby) {
        this(source, type, null, standby, false);
    }

    public int getType() {
        return this.type;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public boolean isStandby() {
        return this.standby;
    }

    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("ChangeEvent ");
        switch (this.type) {
            case 0:
                buff.append("Interface down");
                break;
            case 1:
                buff.append("Interface up");
                break;
            case 2:
                buff.append("Address down");
                break;
            case 3:
                buff.append("Address up");
                break;
            case 4:
                buff.append("Dns has changed");
                break;
        }
        buff.append(", standby=" + this.standby).append(", source=" + this.source).append(", address=" + this.address).append(", isInitial=" + this.initial);
        return buff.toString();
    }

    public boolean isInitial() {
        return this.initial;
    }
}
