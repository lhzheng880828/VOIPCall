package org.xmpp.jnodes.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelDatagramChannel {
    /* access modifiers changed from: private|static|final */
    public static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    /* access modifiers changed from: private|static|final */
    public static final Object obj = new Object();
    /* access modifiers changed from: private|static */
    public static Selector selector;
    protected final DatagramChannel channel;
    /* access modifiers changed from: private */
    public DatagramListener datagramListener;

    private static void init() {
        try {
            selector = Selector.open();
            while (!selector.isOpen()) {
                Thread.yield();
            }
            executorService.submit(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            synchronized (SelDatagramChannel.obj) {
                            }
                            if (SelDatagramChannel.selector.select() == 0) {
                                Thread.sleep(50);
                                Thread.yield();
                            } else {
                                Iterator i = SelDatagramChannel.selector.selectedKeys().iterator();
                                while (i.hasNext()) {
                                    SelectionKey key = (SelectionKey) i.next();
                                    i.remove();
                                    DatagramChannel c = (DatagramChannel) key.channel();
                                    if (key.isValid() && key.isReadable()) {
                                        final SelDatagramChannel sdc = (SelDatagramChannel) key.attachment();
                                        if (sdc == null) {
                                            c.receive(ByteBuffer.allocate(0));
                                        } else {
                                            final SocketAddress clientAddress;
                                            final ByteBuffer b = ByteBuffer.allocateDirect(1450);
                                            synchronized (sdc) {
                                                clientAddress = sdc.channel.receive(b);
                                            }
                                            if (clientAddress != null) {
                                                if (sdc.datagramListener != null) {
                                                    SelDatagramChannel.executorService.submit(new Runnable() {
                                                        public void run() {
                                                            sdc.datagramListener.datagramReceived(sdc, b, clientAddress);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SelDatagramChannel(DatagramChannel channel, DatagramListener datagramListener) {
        this.channel = channel;
        this.datagramListener = datagramListener;
    }

    public static SelDatagramChannel open(DatagramListener datagramListener, SocketAddress localAddress) throws IOException {
        synchronized (executorService) {
            if (selector == null) {
                init();
            }
        }
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(false);
        dc.socket().bind(localAddress);
        SelDatagramChannel c = new SelDatagramChannel(dc, datagramListener);
        synchronized (obj) {
            selector.wakeup();
            dc.register(selector, 1, c);
        }
        return c;
    }

    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        return this.channel.send(src, target);
    }

    public void close() throws IOException {
        SelectionKey k = this.channel.keyFor(selector);
        if (k != null) {
            synchronized (obj) {
                selector.wakeup();
                k.cancel();
            }
        }
        synchronized (this) {
            this.channel.close();
        }
    }

    public void setDatagramListener(DatagramListener listener) {
        this.datagramListener = listener;
    }
}
