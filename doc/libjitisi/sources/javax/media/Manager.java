package javax.media;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.control.FormatControl;
import javax.media.control.TrackControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.SourceCloneable;
import javax.media.protocol.URLDataSource;
import net.sf.fmj.ejmf.toolkit.util.StateWaiter;
import net.sf.fmj.media.MergingCaptureDevicePullBufferDataSource;
import net.sf.fmj.media.MergingCaptureDevicePullDataSource;
import net.sf.fmj.media.MergingCaptureDevicePushBufferDataSource;
import net.sf.fmj.media.MergingCaptureDevicePushDataSource;
import net.sf.fmj.media.MergingPullBufferDataSource;
import net.sf.fmj.media.MergingPullDataSource;
import net.sf.fmj.media.MergingPushBufferDataSource;
import net.sf.fmj.media.MergingPushDataSource;
import net.sf.fmj.media.protocol.CloneableCaptureDevicePullBufferDataSource;
import net.sf.fmj.media.protocol.CloneableCaptureDevicePullDataSource;
import net.sf.fmj.media.protocol.CloneableCaptureDevicePushBufferDataSource;
import net.sf.fmj.media.protocol.CloneableCaptureDevicePushDataSource;
import net.sf.fmj.media.protocol.CloneablePullBufferDataSource;
import net.sf.fmj.media.protocol.CloneablePullDataSource;
import net.sf.fmj.media.protocol.CloneablePushBufferDataSource;
import net.sf.fmj.media.protocol.CloneablePushDataSource;
import net.sf.fmj.utility.LoggerSingleton;

public final class Manager {
    public static final int CACHING = 2;
    public static final String FMJ_TAG = "FMJ";
    public static final int LIGHTWEIGHT_RENDERER = 3;
    public static final int MAX_SECURITY = 1;
    public static final int PLUGIN_PLAYER = 4;
    public static final boolean RETHROW_IO_EXCEPTIONS = true;
    public static final String UNKNOWN_CONTENT_NAME = "unknown";
    private static final boolean USE_MEDIA_PREFIX = false;
    private static final Map<Integer, Object> hints = new HashMap();
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    private static TimeBase systemTimeBase = new SystemTimeBase();

    private static class BlockingRealizer implements ControllerListener {
        private volatile boolean busy = true;
        private volatile String cannotRealizeExceptionMessage;
        private final Controller controller;
        private volatile boolean realized = false;

        public BlockingRealizer(Controller controller) {
            this.controller = controller;
        }

        public synchronized void controllerUpdate(ControllerEvent event) {
            if (event instanceof RealizeCompleteEvent) {
                this.realized = true;
                this.busy = false;
                notify();
            } else if ((event instanceof StopEvent) || (event instanceof ControllerClosedEvent)) {
                if (event instanceof StopEvent) {
                    this.cannotRealizeExceptionMessage = "Cannot realize: received StopEvent: " + event;
                    Manager.logger.info(this.cannotRealizeExceptionMessage);
                } else {
                    this.cannotRealizeExceptionMessage = "Cannot realize: received ControllerClosedEvent: " + event + "; message: " + ((ControllerClosedEvent) event).getMessage();
                    Manager.logger.info(this.cannotRealizeExceptionMessage);
                }
                this.realized = false;
                this.busy = false;
                notify();
            }
        }

        public void realize() throws CannotRealizeException, InterruptedException {
            this.controller.addControllerListener(this);
            this.controller.realize();
            while (this.busy) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    this.controller.removeControllerListener(this);
                    throw e;
                }
            }
            this.controller.removeControllerListener(this);
            if (!this.realized) {
                throw new CannotRealizeException(this.cannotRealizeExceptionMessage);
            }
        }
    }

    static {
        hints.put(new Integer(1), Boolean.FALSE);
        hints.put(new Integer(2), Boolean.TRUE);
        hints.put(new Integer(3), Boolean.FALSE);
        hints.put(new Integer(4), Boolean.FALSE);
    }

    private static void blockingRealize(Controller controller) throws CannotRealizeException {
        try {
            new BlockingRealizer(controller).realize();
        } catch (InterruptedException e) {
            throw new CannotRealizeException("Interrupted");
        }
    }

    public static DataSource createCloneableDataSource(DataSource source) {
        if (source instanceof SourceCloneable) {
            return source;
        }
        if (source instanceof PushBufferDataSource) {
            if (source instanceof CaptureDevice) {
                return new CloneableCaptureDevicePushBufferDataSource((PushBufferDataSource) source);
            }
            return new CloneablePushBufferDataSource((PushBufferDataSource) source);
        } else if (source instanceof PullBufferDataSource) {
            if (source instanceof CaptureDevice) {
                return new CloneableCaptureDevicePullBufferDataSource((PullBufferDataSource) source);
            }
            return new CloneablePullBufferDataSource((PullBufferDataSource) source);
        } else if (source instanceof PushDataSource) {
            if (source instanceof CaptureDevice) {
                return new CloneableCaptureDevicePushDataSource((PushDataSource) source);
            }
            return new CloneablePushDataSource((PushDataSource) source);
        } else if (!(source instanceof PullDataSource)) {
            throw new IllegalArgumentException("Unknown or unsupported DataSource type: " + source);
        } else if (source instanceof CaptureDevice) {
            return new CloneableCaptureDevicePullDataSource((PullDataSource) source);
        } else {
            return new CloneablePullDataSource((PullDataSource) source);
        }
    }

    public static DataSink createDataSink(DataSource datasource, MediaLocator destLocator) throws NoDataSinkException {
        String protocol = destLocator.getProtocol();
        Iterator it = getDataSinkClassList(protocol).iterator();
        while (it.hasNext()) {
            try {
                Class<?> handlerClass = Class.forName((String) it.next());
                if (DataSink.class.isAssignableFrom(handlerClass) || DataSinkProxy.class.isAssignableFrom(handlerClass)) {
                    MediaHandler handler = (MediaHandler) handlerClass.newInstance();
                    handler.setSource(datasource);
                    if (handler instanceof DataSink) {
                        DataSink dataSink = (DataSink) handler;
                        dataSink.setOutputLocator(destLocator);
                        return dataSink;
                    } else if (handler instanceof DataSinkProxy) {
                        DataSinkProxy mediaProxy = (DataSinkProxy) handler;
                        Iterator i$ = getDataSinkClassList(protocol + "." + toPackageFriendly(mediaProxy.getContentType(destLocator))).iterator();
                        while (i$.hasNext()) {
                            try {
                                Class<?> handlerClass2 = Class.forName((String) i$.next());
                                if (DataSink.class.isAssignableFrom(handlerClass2)) {
                                    MediaHandler handler2 = (MediaHandler) handlerClass2.newInstance();
                                    handler2.setSource(mediaProxy.getDataSource());
                                    if (handler2 instanceof DataSink) {
                                        ((DataSink) handler2).setOutputLocator(destLocator);
                                        return (DataSink) handler2;
                                    }
                                } else {
                                    continue;
                                }
                            } catch (ClassNotFoundException e) {
                                logger.finer("createDataSink: " + e);
                            } catch (IncompatibleSourceException e2) {
                                logger.fine("createDataSink(" + datasource + ", " + destLocator + "), proxy=" + mediaProxy.getDataSource() + ": " + e2);
                            } catch (NoClassDefFoundError e3) {
                                logger.log(Level.FINE, "" + e3, e3);
                            } catch (Exception e4) {
                                logger.log(Level.FINE, "" + e4, e4);
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            } catch (ClassNotFoundException e5) {
                logger.finer("createDataSink: " + e5);
            } catch (IncompatibleSourceException e22) {
                logger.fine("createDataSink(" + datasource + ", " + destLocator + "): " + e22);
            } catch (NoClassDefFoundError e32) {
                logger.log(Level.FINE, "" + e32, e32);
            } catch (Exception e42) {
                logger.log(Level.FINE, "" + e42, e42);
            }
        }
        throw new NoDataSinkException();
    }

    private static DataSink createDataSink(DataSource datasource, String protocol) throws NoDataSinkException {
        Iterator i$ = getDataSinkClassList(protocol).iterator();
        while (i$.hasNext()) {
            try {
                Class<?> handlerClass = Class.forName((String) i$.next());
                if (DataSink.class.isAssignableFrom(handlerClass)) {
                    MediaHandler handler = (MediaHandler) handlerClass.newInstance();
                    handler.setSource(datasource);
                    if (handler instanceof DataSink) {
                        return (DataSink) handler;
                    }
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                logger.finer("createDataSink: " + e);
            } catch (IncompatibleSourceException e2) {
                logger.fine("createDataSink(" + datasource + ", " + protocol + "): " + e2);
            } catch (NoClassDefFoundError e3) {
                logger.log(Level.FINE, "" + e3, e3);
            } catch (Exception e4) {
                logger.log(Level.FINE, "" + e4, e4);
            }
        }
        throw new NoDataSinkException();
    }

    public static DataSource createDataSource(URL sourceURL) throws IOException, NoDataSourceException {
        return createDataSource(new MediaLocator(sourceURL));
    }

    public static DataSource createDataSource(MediaLocator sourceLocator) throws IOException, NoDataSourceException {
        Iterator i$ = getDataSourceList(sourceLocator.getProtocol()).iterator();
        while (i$.hasNext()) {
            try {
                DataSource dataSource = (DataSource) Class.forName((String) i$.next()).newInstance();
                dataSource.setLocator(sourceLocator);
                dataSource.connect();
                return dataSource;
            } catch (ClassNotFoundException e) {
                logger.finer("createDataSource: " + e);
            } catch (IOException e2) {
                logger.log(Level.FINE, "" + e2, e2);
                throw e2;
            } catch (NoClassDefFoundError e3) {
                logger.log(Level.FINE, "" + e3, e3);
            } catch (Exception e4) {
                logger.log(Level.FINE, "" + e4, e4);
            }
        }
        try {
            URLDataSource dataSource2 = new URLDataSource(sourceLocator.getURL());
            dataSource2.connect();
            return dataSource2;
        } catch (Exception e42) {
            logger.log(Level.WARNING, "" + e42, e42);
            throw new NoDataSourceException();
        }
    }

    public static DataSource createMergingDataSource(DataSource[] sources) throws IncompatibleSourceException {
        boolean allPushBufferDataSource = true;
        boolean allPullBufferDataSource = true;
        boolean allPushDataSource = true;
        boolean allPullDataSource = true;
        boolean allCaptureDevice = true;
        for (DataSource source : sources) {
            if (!(source instanceof PushBufferDataSource)) {
                allPushBufferDataSource = false;
            }
            if (!(source instanceof PullBufferDataSource)) {
                allPullBufferDataSource = false;
            }
            if (!(source instanceof PushDataSource)) {
                allPushDataSource = false;
            }
            if (!(source instanceof PullDataSource)) {
                allPullDataSource = false;
            }
            if (!(source instanceof CaptureDevice)) {
                allCaptureDevice = false;
            }
        }
        if (allPushBufferDataSource) {
            List<PushBufferDataSource> sourcesCast = new ArrayList();
            for (DataSource source2 : sources) {
                sourcesCast.add((PushBufferDataSource) source2);
            }
            if (allCaptureDevice) {
                return new MergingCaptureDevicePushBufferDataSource(sourcesCast);
            }
            return new MergingPushBufferDataSource(sourcesCast);
        } else if (allPullBufferDataSource) {
            List<PullBufferDataSource> sourcesCast2 = new ArrayList();
            for (DataSource source22 : sources) {
                sourcesCast2.add((PullBufferDataSource) source22);
            }
            if (allCaptureDevice) {
                return new MergingCaptureDevicePullBufferDataSource(sourcesCast2);
            }
            return new MergingPullBufferDataSource(sourcesCast2);
        } else if (allPushDataSource) {
            List<PushDataSource> sourcesCast3 = new ArrayList();
            for (DataSource source222 : sources) {
                sourcesCast3.add((PushDataSource) source222);
            }
            if (allCaptureDevice) {
                return new MergingCaptureDevicePushDataSource(sourcesCast3);
            }
            return new MergingPushDataSource(sourcesCast3);
        } else if (allPullDataSource) {
            List<PullDataSource> sourcesCast4 = new ArrayList();
            for (DataSource source2222 : sources) {
                sourcesCast4.add((PullDataSource) source2222);
            }
            if (allCaptureDevice) {
                return new MergingCaptureDevicePullDataSource(sourcesCast4);
            }
            return new MergingPullDataSource(sourcesCast4);
        } else {
            throw new IncompatibleSourceException();
        }
    }

    public static Player createPlayer(DataSource source) throws IOException, NoPlayerException {
        try {
            return createPlayer(source, source.getContentType());
        } catch (NoPlayerException e) {
        } catch (IOException e2) {
            logger.log(Level.FINE, "" + e2, e2);
            throw e2;
        } catch (Exception e3) {
            logger.log(Level.FINER, "" + e3, e3);
        }
        return createPlayer(source, UNKNOWN_CONTENT_NAME);
    }

    private static Player createPlayer(DataSource source, String contentType) throws IOException, NoPlayerException {
        List<String> classFoundHandlersTried = new ArrayList();
        Iterator i$ = getHandlerClassList(contentType).iterator();
        while (i$.hasNext()) {
            String handlerClassName = (String) i$.next();
            try {
                Class<?> handlerClass = Class.forName(handlerClassName);
                if (Player.class.isAssignableFrom(handlerClass) || MediaProxy.class.isAssignableFrom(handlerClass)) {
                    MediaHandler handler = (MediaHandler) handlerClass.newInstance();
                    handler.setSource(source);
                    if (handler instanceof Player) {
                        logger.info("Using player: " + handler.getClass().getName());
                        return (Player) handler;
                    } else if (handler instanceof MediaProxy) {
                        return createPlayer(((MediaProxy) handler).getDataSource());
                    } else {
                        logger.fine("Not Player, and not MediaProxy: " + handler.getClass().getName());
                        classFoundHandlersTried.add(handlerClassName);
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.finer("createPlayer: " + e);
            } catch (IncompatibleSourceException e2) {
                classFoundHandlersTried.add(handlerClassName);
                logger.fine("createPlayer(" + source + ", " + contentType + "): " + e2);
            } catch (IOException e3) {
                classFoundHandlersTried.add(handlerClassName);
                logger.log(Level.FINE, "" + e3, e3);
                throw e3;
            } catch (NoPlayerException e4) {
                classFoundHandlersTried.add(handlerClassName);
            } catch (NoClassDefFoundError e5) {
                classFoundHandlersTried.add(handlerClassName);
                logger.log(Level.FINE, "" + e5, e5);
            } catch (Exception e6) {
                classFoundHandlersTried.add(handlerClassName);
                logger.log(Level.FINE, "" + e6, e6);
            }
        }
        StringBuilder b = new StringBuilder();
        b.append("Tried handlers:");
        for (int i = 0; i < classFoundHandlersTried.size(); i++) {
            b.append(10);
            b.append((String) classFoundHandlersTried.get(i));
        }
        throw new NoPlayerException("No player found for " + source.getLocator() + " - " + b.toString());
    }

    public static Player createPlayer(URL sourceURL) throws IOException, NoPlayerException {
        return createPlayer(new MediaLocator(sourceURL));
    }

    public static Player createPlayer(MediaLocator sourceLocator) throws IOException, NoPlayerException {
        DataSource dataSource;
        Iterator i$ = getDataSourceList(sourceLocator.getProtocol()).iterator();
        while (i$.hasNext()) {
            try {
                dataSource = (DataSource) Class.forName((String) i$.next()).newInstance();
                dataSource.setLocator(sourceLocator);
                dataSource.connect();
                return createPlayer(dataSource);
            } catch (NoPlayerException e) {
            } catch (ClassNotFoundException e2) {
                logger.finer("createPlayer: " + e2);
            } catch (IOException e3) {
                logger.log(Level.FINE, "" + e3, e3);
                throw e3;
            } catch (NoClassDefFoundError e4) {
                logger.log(Level.FINE, "" + e4, e4);
            } catch (Exception e5) {
                logger.log(Level.FINE, "" + e5, e5);
            }
        }
        try {
            dataSource = new URLDataSource(sourceLocator.getURL());
            dataSource.connect();
            return createPlayer(dataSource);
        } catch (Exception e52) {
            logger.log(Level.WARNING, "" + e52, e52);
            throw new NoPlayerException();
        }
    }

    public static Processor createProcessor(DataSource source) throws IOException, NoProcessorException {
        try {
            return createProcessor(source, source.getContentType());
        } catch (IOException e) {
            logger.log(Level.FINE, "" + e, e);
            throw e;
        } catch (NoProcessorException e2) {
            return createProcessor(source, UNKNOWN_CONTENT_NAME);
        } catch (Exception e3) {
            logger.log(Level.FINE, "" + e3, e3);
            return createProcessor(source, UNKNOWN_CONTENT_NAME);
        }
    }

    private static Processor createProcessor(DataSource source, String contentType) throws IOException, NoProcessorException {
        Iterator i$ = getProcessorClassList(contentType).iterator();
        while (i$.hasNext()) {
            try {
                Class<?> handlerClass = Class.forName((String) i$.next());
                if (Processor.class.isAssignableFrom(handlerClass) || MediaProxy.class.isAssignableFrom(handlerClass)) {
                    MediaHandler handler = (MediaHandler) handlerClass.newInstance();
                    handler.setSource(source);
                    if (handler instanceof Processor) {
                        return (Processor) handler;
                    }
                    if (handler instanceof MediaProxy) {
                        return createProcessor(((MediaProxy) handler).getDataSource());
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.finer("createProcessor: " + e);
            } catch (IncompatibleSourceException e2) {
                logger.fine("createProcessor(" + source + ", " + contentType + "): " + e2);
            } catch (NoProcessorException e3) {
            } catch (IOException e4) {
                logger.log(Level.FINE, "" + e4, e4);
                throw e4;
            } catch (NoClassDefFoundError e5) {
                logger.log(Level.FINE, "" + e5, e5);
            } catch (Exception e6) {
                logger.log(Level.FINE, "" + e6, e6);
            }
        }
        throw new NoProcessorException();
    }

    public static Processor createProcessor(URL sourceURL) throws IOException, NoProcessorException {
        return createProcessor(new MediaLocator(sourceURL));
    }

    public static Processor createProcessor(MediaLocator sourceLocator) throws IOException, NoProcessorException {
        DataSource dataSource;
        Iterator i$ = getDataSourceList(sourceLocator.getProtocol()).iterator();
        while (i$.hasNext()) {
            try {
                dataSource = (DataSource) Class.forName((String) i$.next()).newInstance();
                dataSource.setLocator(sourceLocator);
                dataSource.connect();
                return createProcessor(dataSource);
            } catch (ClassNotFoundException e) {
                logger.finer("createProcessor: " + e);
            } catch (IOException e2) {
                logger.log(Level.FINE, "" + e2, e2);
                throw e2;
            } catch (NoProcessorException e3) {
            } catch (NoClassDefFoundError e4) {
                logger.log(Level.FINE, "" + e4, e4);
            } catch (Exception e5) {
                logger.log(Level.FINE, "" + e5, e5);
            }
        }
        try {
            dataSource = new URLDataSource(sourceLocator.getURL());
            dataSource.connect();
            return createProcessor(dataSource);
        } catch (Exception e52) {
            logger.log(Level.WARNING, "" + e52, e52);
            throw new NoProcessorException();
        }
    }

    public static Player createRealizedPlayer(DataSource source) throws IOException, NoPlayerException, CannotRealizeException {
        Player player = createPlayer(source);
        blockingRealize(player);
        return player;
    }

    public static Player createRealizedPlayer(URL sourceURL) throws IOException, NoPlayerException, CannotRealizeException {
        Player player = createPlayer(sourceURL);
        blockingRealize(player);
        return player;
    }

    public static Player createRealizedPlayer(MediaLocator ml) throws IOException, NoPlayerException, CannotRealizeException {
        Player player = createPlayer(ml);
        blockingRealize(player);
        return player;
    }

    public static Processor createRealizedProcessor(ProcessorModel model) throws IOException, NoProcessorException, CannotRealizeException {
        Processor processor;
        if (model.getInputDataSource() != null) {
            processor = createProcessor(model.getInputDataSource());
        } else {
            processor = createProcessor(model.getInputLocator());
        }
        StateWaiter stateWaiter = new StateWaiter(processor);
        if (stateWaiter.blockingConfigure()) {
            Format[] outputFormats;
            int i;
            if (model.getContentDescriptor() != null) {
                processor.setContentDescriptor(model.getContentDescriptor());
            }
            int numTracks = model.getTrackCount(Integer.MAX_VALUE);
            if (numTracks > 0) {
                outputFormats = new Format[numTracks];
                for (i = 0; i < outputFormats.length; i++) {
                    outputFormats[i] = model.getOutputTrackFormat(i);
                }
            } else {
                outputFormats = null;
            }
            if (outputFormats != null && outputFormats.length > 0) {
                int j;
                TrackControl[] trackControl = processor.getTrackControls();
                boolean[] trackConfigured = new boolean[trackControl.length];
                boolean[] outputFormatUsed = new boolean[outputFormats.length];
                for (j = 0; j < outputFormats.length; j++) {
                    Format outputFormat = outputFormats[j];
                    if (outputFormat != null) {
                        for (i = 0; i < trackControl.length; i++) {
                            if (!trackConfigured[i]) {
                                if (!(trackControl[i] instanceof FormatControl)) {
                                    logger.warning("Disabling track " + i + "; trackControl is not a FormatControl: " + trackControl[i]);
                                    trackControl[i].setEnabled(false);
                                    trackConfigured[i] = true;
                                } else if (trackControl[i].setFormat(outputFormat) == null) {
                                    logger.fine("Track " + i + "; does not accept " + outputFormat);
                                } else {
                                    logger.fine("Using track " + i + "; accepted " + outputFormat);
                                    trackConfigured[i] = true;
                                    outputFormatUsed[j] = true;
                                }
                            }
                        }
                    }
                }
                for (j = 0; j < outputFormats.length; j++) {
                    if (outputFormats[j] == null) {
                        for (i = 0; i < trackControl.length; i++) {
                            if (!trackConfigured[i]) {
                                logger.fine("Using track " + i + "; for unspecified format");
                                trackConfigured[i] = true;
                                outputFormatUsed[j] = true;
                            }
                        }
                    }
                }
                for (i = 0; i < trackControl.length; i++) {
                    if (!trackConfigured[i]) {
                        logger.info("Disabling track " + i + "; no format set.");
                        trackControl[i].setEnabled(false);
                    }
                }
                j = 0;
                while (j < outputFormats.length) {
                    if (outputFormatUsed[j]) {
                        j++;
                    } else {
                        throw new CannotRealizeException("No tracks found that are compatible with format " + outputFormats[j]);
                    }
                }
            }
            if (stateWaiter.blockingRealize()) {
                return processor;
            }
            throw new CannotRealizeException("Failed to realize");
        }
        throw new CannotRealizeException("Failed to configure");
    }

    public static String getCacheDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public static Vector<String> getClassList(String contentName, Vector packages, String component2, String className) {
        Vector<String> result = new Vector();
        Iterator i$ = packages.iterator();
        while (i$.hasNext()) {
            result.add(i$.next() + ".media." + component2 + "." + contentName + "." + className);
        }
        return result;
    }

    public static Vector<String> getDataSinkClassList(String contentName) {
        return getClassList(toPackageFriendly(contentName), PackageManager.getContentPrefixList(), "datasink", "Handler");
    }

    public static Vector<String> getDataSourceList(String protocolName) {
        return getClassList(protocolName, PackageManager.getProtocolPrefixList(), "protocol", "DataSource");
    }

    public static Vector<String> getHandlerClassList(String contentName) {
        return getClassList(toPackageFriendly(contentName), PackageManager.getContentPrefixList(), "content", "Handler");
    }

    public static Object getHint(int hint) {
        return hints.get(Integer.valueOf(hint));
    }

    public static Vector<String> getProcessorClassList(String contentName) {
        return getClassList(toPackageFriendly(contentName), PackageManager.getContentPrefixList(), "processor", "Handler");
    }

    public static TimeBase getSystemTimeBase() {
        return systemTimeBase;
    }

    public static String getVersion() {
        try {
            Properties p = new Properties();
            p.load(Manager.class.getResourceAsStream("/fmj.build.properties"));
            String s = p.getProperty("build");
            if (!(s == null || s.equals(""))) {
                return "FMJ " + s.trim();
            }
        } catch (Exception e) {
        }
        return "FMJ non-release x.x";
    }

    public static void setHint(int hint, Object value) {
        hints.put(Integer.valueOf(hint), value);
    }

    private static char toPackageFriendly(char c) {
        if (c >= 'a' && c <= 'z') {
            return c;
        }
        if (c >= 'A' && c <= 'Z') {
            return c;
        }
        if ((c >= '0' && c <= '9') || c == '.') {
            return c;
        }
        if (c == '/') {
            return '.';
        }
        return '_';
    }

    private static String toPackageFriendly(String contentName) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < contentName.length(); i++) {
            b.append(toPackageFriendly(contentName.charAt(i)));
        }
        return b.toString();
    }
}
