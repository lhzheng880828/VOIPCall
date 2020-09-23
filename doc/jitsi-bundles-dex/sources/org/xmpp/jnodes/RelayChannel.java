package org.xmpp.jnodes;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.xmpp.jnodes.nio.DatagramListener;
import org.xmpp.jnodes.nio.SelDatagramChannel;

public class RelayChannel {
    private final SocketAddress addressA;
    private final SocketAddress addressB;
    private Object attachment;
    /* access modifiers changed from: private|final */
    public final SelDatagramChannel channelA = SelDatagramChannel.open(null, this.addressA);
    /* access modifiers changed from: private|final */
    public final SelDatagramChannel channelA_;
    /* access modifiers changed from: private|final */
    public final SelDatagramChannel channelB = SelDatagramChannel.open(null, this.addressB);
    /* access modifiers changed from: private|final */
    public final SelDatagramChannel channelB_;
    private final String ip;
    /* access modifiers changed from: private */
    public SocketAddress lastReceivedA;
    /* access modifiers changed from: private */
    public SocketAddress lastReceivedA_;
    /* access modifiers changed from: private */
    public SocketAddress lastReceivedB;
    /* access modifiers changed from: private */
    public SocketAddress lastReceivedB_;
    /* access modifiers changed from: private */
    public long lastReceivedTimeA;
    /* access modifiers changed from: private */
    public long lastReceivedTimeB;
    private final int portA;
    private final int portB;

    public static RelayChannel createLocalRelayChannel(String host, int minPort, int maxPort) throws IOException {
        int range = maxPort - minPort;
        IOException be = null;
        int t = 0;
        while (t < 50) {
            try {
                int a = Math.round((float) ((int) (Math.random() * ((double) range)))) + minPort;
                if (a % 2 != 0) {
                    a++;
                }
                return new RelayChannel(host, a);
            } catch (IOException | BindException e) {
                be = e;
                t++;
            }
        }
        throw be;
    }

    public RelayChannel(String host, int portA) throws IOException {
        int portB = portA + 2;
        this.addressA = new InetSocketAddress(host, portA);
        this.addressB = new InetSocketAddress(host, portB);
        this.channelA.setDatagramListener(new DatagramListener() {
            public void datagramReceived(SelDatagramChannel channel, ByteBuffer buffer, SocketAddress address) {
                RelayChannel.this.lastReceivedA = address;
                RelayChannel.this.lastReceivedTimeA = System.currentTimeMillis();
                if (RelayChannel.this.lastReceivedB != null) {
                    try {
                        buffer.flip();
                        RelayChannel.this.channelB.send(buffer, RelayChannel.this.lastReceivedB);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.channelB.setDatagramListener(new DatagramListener() {
            public void datagramReceived(SelDatagramChannel channel, ByteBuffer buffer, SocketAddress address) {
                RelayChannel.this.lastReceivedB = address;
                RelayChannel.this.lastReceivedTimeB = System.currentTimeMillis();
                if (RelayChannel.this.lastReceivedA != null) {
                    try {
                        buffer.flip();
                        RelayChannel.this.channelA.send(buffer, RelayChannel.this.lastReceivedA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.portA = portA;
        this.portB = portB;
        SocketAddress addressA_ = new InetSocketAddress(host, portA + 1);
        SocketAddress addressB_ = new InetSocketAddress(host, portB + 1);
        this.channelA_ = SelDatagramChannel.open(null, addressA_);
        this.channelB_ = SelDatagramChannel.open(null, addressB_);
        this.channelA_.setDatagramListener(new DatagramListener() {
            public void datagramReceived(SelDatagramChannel channel, ByteBuffer buffer, SocketAddress address) {
                RelayChannel.this.lastReceivedA_ = address;
                if (RelayChannel.this.lastReceivedB_ != null) {
                    try {
                        buffer.flip();
                        RelayChannel.this.channelB_.send(buffer, RelayChannel.this.lastReceivedB_);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.channelB_.setDatagramListener(new DatagramListener() {
            public void datagramReceived(SelDatagramChannel channel, ByteBuffer buffer, SocketAddress address) {
                RelayChannel.this.lastReceivedB_ = address;
                if (RelayChannel.this.lastReceivedA_ != null) {
                    try {
                        buffer.flip();
                        RelayChannel.this.channelA_.send(buffer, RelayChannel.this.lastReceivedA_);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.ip = host;
    }

    public SocketAddress getAddressB() {
        return this.addressB;
    }

    public SocketAddress getAddressA() {
        return this.addressA;
    }

    public int getPortA() {
        return this.portA;
    }

    public int getPortB() {
        return this.portB;
    }

    public String getIp() {
        return this.ip;
    }

    public long getLastReceivedTimeA() {
        return this.lastReceivedTimeA;
    }

    public long getLastReceivedTimeB() {
        return this.lastReceivedTimeB;
    }

    public Object getAttachment() {
        return this.attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public void close() {
        try {
            this.channelA.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.channelB.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            this.channelA_.close();
        } catch (IOException e22) {
            e22.printStackTrace();
        }
        try {
            this.channelB_.close();
        } catch (IOException e222) {
            e222.printStackTrace();
        }
    }

    public SelDatagramChannel getChannelA() {
        return this.channelA;
    }

    public SelDatagramChannel getChannelB() {
        return this.channelB;
    }

    public SelDatagramChannel getChannelA_() {
        return this.channelA_;
    }

    public SelDatagramChannel getChannelB_() {
        return this.channelB_;
    }
}
