package net.sf.fmj.media;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.Time;

public abstract class BasicModule implements Module, StateTransistor {
    protected BasicController controller;
    protected Registry inputConnectors = new Registry();
    protected InputConnector[] inputConnectorsArray;
    protected ModuleListener moduleListener;
    protected String name = null;
    protected Registry outputConnectors = new Registry();
    protected OutputConnector[] outputConnectorsArray;
    protected boolean prefetchFailed = false;
    protected int protocol = 0;
    protected boolean resetted = false;

    class Registry extends Hashtable {
        Connector def = null;

        Registry() {
        }

        /* access modifiers changed from: 0000 */
        public Object get(String name) {
            if (name == null) {
                return this.def;
            }
            return super.get(name);
        }

        /* access modifiers changed from: 0000 */
        public Connector[] getConnectors() {
            Enumeration connectorsEnum = elements();
            Connector[] connectorsArray = new Connector[size()];
            for (int i = 0; i < size(); i++) {
                connectorsArray[i] = (Connector) connectorsEnum.nextElement();
            }
            return connectorsArray;
        }

        /* access modifiers changed from: 0000 */
        public String[] getNames() {
            Enumeration namesEnum = keys();
            String[] namesArray = new String[size()];
            for (int i = 0; i < size(); i++) {
                namesArray[i] = (String) namesEnum.nextElement();
            }
            return namesArray;
        }

        /* access modifiers changed from: 0000 */
        public void put(String name, Connector connector) {
            if (containsKey(name)) {
                throw new RuntimeException("Connector '" + name + "' already exists in Module '" + BasicModule.this.getClass().getName() + "::" + name + "'");
            }
            if (this.def == null) {
                this.def = connector;
            }
            super.put(name, connector);
        }
    }

    public abstract void process();

    public void abortPrefetch() {
    }

    public void abortRealize() {
    }

    public boolean canRun() {
        for (InputConnector isValidBufferAvailable : this.inputConnectorsArray) {
            if (!isValidBufferAvailable.isValidBufferAvailable()) {
                return false;
            }
        }
        for (OutputConnector isEmptyBufferAvailable : this.outputConnectorsArray) {
            if (!isEmptyBufferAvailable.isEmptyBufferAvailable()) {
                return false;
            }
        }
        return true;
    }

    public void connectorPushed(InputConnector inputConnector) {
        process();
    }

    public void doClose() {
    }

    public void doDealloc() {
    }

    public void doFailedPrefetch() {
    }

    public void doFailedRealize() {
    }

    public boolean doPrefetch() {
        this.resetted = false;
        return true;
    }

    public boolean doRealize() {
        return true;
    }

    public void doSetMediaTime(Time t) {
    }

    public float doSetRate(float r) {
        return r;
    }

    public void doStart() {
        this.resetted = false;
    }

    public void doStop() {
    }

    /* access modifiers changed from: protected */
    public void error() {
        throw new RuntimeException(getClass().getName() + " error");
    }

    public Object getControl(String s) {
        return null;
    }

    public final BasicController getController() {
        return this.controller;
    }

    public Object[] getControls() {
        return null;
    }

    public InputConnector getInputConnector(String connectorName) {
        return (InputConnector) this.inputConnectors.get(connectorName);
    }

    public String[] getInputConnectorNames() {
        return this.inputConnectors.getNames();
    }

    public long getLatency() {
        return ((PlaybackEngine) this.controller).getLatency();
    }

    public long getMediaNanoseconds() {
        return this.controller.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        return this.controller.getMediaTime();
    }

    public final String getName() {
        return this.name;
    }

    public OutputConnector getOutputConnector(String connectorName) {
        return (OutputConnector) this.outputConnectors.get(connectorName);
    }

    public String[] getOutputConnectorNames() {
        return this.outputConnectors.getNames();
    }

    public int getProtocol() {
        return this.protocol;
    }

    public final int getState() {
        return this.controller.getState();
    }

    public final boolean isInterrupted() {
        return this.controller == null ? false : this.controller.isInterrupted();
    }

    public boolean isThreaded() {
        return true;
    }

    public boolean prefetchFailed() {
        return this.prefetchFailed;
    }

    public void registerInputConnector(String name, InputConnector inputConnector) {
        this.inputConnectors.put(name, inputConnector);
        inputConnector.setModule(this);
    }

    public void registerOutputConnector(String name, OutputConnector outputConnector) {
        this.outputConnectors.put(name, outputConnector);
        outputConnector.setModule(this);
    }

    public void reset() {
        this.resetted = true;
    }

    public final void setController(BasicController c) {
        this.controller = c;
    }

    public void setFormat(Connector connector, Format format) {
    }

    public void setModuleListener(ModuleListener listener) {
        this.moduleListener = listener;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
        Connector[] connectors = this.inputConnectors.getConnectors();
        for (Connector protocol2 : connectors) {
            protocol2.setProtocol(protocol);
        }
        connectors = this.outputConnectors.getConnectors();
        for (Connector protocol22 : connectors) {
            protocol22.setProtocol(protocol);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyBuffer(Buffer buffer) {
        if (buffer.isDiscard()) {
            return true;
        }
        Object data = buffer.getData();
        if (buffer.getLength() < 0) {
            System.err.println("warning: data length shouldn't be negative: " + buffer.getLength());
        }
        if (data == null) {
            System.err.println("warning: data buffer is null");
            if (buffer.getLength() != 0) {
                System.err.println("buffer advertized length = " + buffer.getLength() + " but data buffer is null!");
                return false;
            }
        } else if (data instanceof byte[]) {
            if (buffer.getLength() > ((byte[]) data).length) {
                System.err.println("buffer advertized length = " + buffer.getLength() + " but actual length = " + ((byte[]) data).length);
                return false;
            }
        } else if ((data instanceof int[]) && buffer.getLength() > ((int[]) data).length) {
            System.err.println("buffer advertized length = " + buffer.getLength() + " but actual length = " + ((int[]) data).length);
            return false;
        }
        return true;
    }
}
