package org.jitsi.gov.nist.javax.sip.stack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;

public class CallAnalyzer {
    static int count = 0;
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(CallAnalyzer.class);
    private MetricReferenceMap metricStatisticsMap = new MetricReferenceMap();
    private SipStackImpl stack;
    /* access modifiers changed from: private */
    public Map<Thread, HashMap<MetricReference, Object>> threadMap = new WeakHashMap();
    private Timer timer = new Timer();

    public static class MetricAnalysisConfiguration {
        protected Long checkingInterval;
        protected Long minimumDumpInterval;
        protected Long stuckTimeBeforeDump;

        public MetricAnalysisConfiguration(Long checkingInterval, Long minDumpInterval, Long stuckTimerBeforeDump) {
            this.checkingInterval = checkingInterval;
            this.minimumDumpInterval = minDumpInterval;
            this.stuckTimeBeforeDump = stuckTimerBeforeDump;
        }

        public MetricAnalysisConfiguration(int checkingInterval, int minDumpInterval, int stuckTimerBeforeDump) {
            this.checkingInterval = new Long((long) checkingInterval);
            this.minimumDumpInterval = new Long((long) minDumpInterval);
            this.stuckTimeBeforeDump = new Long((long) stuckTimerBeforeDump);
        }
    }

    public static class MetricReference {
        public String name;

        public MetricReference(String name) {
            this.name = name;
        }

        public boolean equals(Object other) {
            if (other instanceof MetricReference) {
                return ((MetricReference) other).name.equals(this.name);
            }
            return false;
        }

        public int hashCode() {
            return this.name.hashCode();
        }
    }

    public static class MetricReferenceMap extends WeakHashMap<MetricReference, TImeMetricInfo> {
        private static final long serialVersionUID = 393231609328924828L;

        public TImeMetricInfo get(Object key) {
            if (super.get(key) == null) {
                super.put((MetricReference) key, new TImeMetricInfo());
            }
            return (TImeMetricInfo) super.get(key);
        }
    }

    public static class StackTrace {
        public int delta;
        public String trace;

        public StackTrace(int delta, String trace) {
            this.delta = delta;
            this.trace = trace;
        }
    }

    public static class TImeMetricInfo {
        public Long averageTime = new Long(1);
        protected MetricAnalysisConfiguration config = new MetricAnalysisConfiguration(5000, 5000, 5000);
        public Long lastLoggedEventTime = new Long(0);
        public Long numberOfEvents = new Long(0);
        protected TimerTask task;
        public Long totalTime = new Long(0);
    }

    public static class ThreadInfo {
        public Object data;
        public LinkedList<StackTrace> stackTraces = new LinkedList();
    }

    public CallAnalyzer(SipStackImpl stack) {
        this.stack = stack;
    }

    public void configure(MetricReference ref, MetricAnalysisConfiguration config) {
        this.metricStatisticsMap.get((Object) ref).config = config;
        if (isAnalyssStarted(ref)) {
            startAnalysis(ref);
        }
    }

    public boolean isAnalyssStarted(MetricReference ref) {
        return this.metricStatisticsMap.get((Object) ref).task != null;
    }

    public TImeMetricInfo getMetricStats(MetricReference ref) {
        return this.metricStatisticsMap.get((Object) ref);
    }

    public void resetStats(MetricReference metricReference) {
        TImeMetricInfo info = this.metricStatisticsMap.get((Object) metricReference);
        info.totalTime = new Long(0);
        info.numberOfEvents = new Long(0);
        info.averageTime = new Long(1);
        info.lastLoggedEventTime = new Long(0);
    }

    public void stopAnalysis(MetricReference metricReference) {
        TImeMetricInfo statInfo = this.metricStatisticsMap.get((Object) metricReference);
        if (statInfo.task != null) {
            statInfo.task.cancel();
            statInfo.task = null;
        }
    }

    public void startAnalysis(final MetricReference metricReference) {
        stopAnalysis(metricReference);
        resetStats(metricReference);
        final TImeMetricInfo statInfo = this.metricStatisticsMap.get((Object) metricReference);
        statInfo.task = new TimerTask() {
            public void run() {
                try {
                    if (System.currentTimeMillis() - statInfo.lastLoggedEventTime.longValue() > statInfo.config.minimumDumpInterval.longValue()) {
                        for (Entry<Thread, HashMap<MetricReference, Object>> info : CallAnalyzer.this.threadMap.entrySet()) {
                            Long entryTime = (Long) ((HashMap) info.getValue()).get(metricReference);
                            if (!entryTime.equals(Long.valueOf(Long.MIN_VALUE))) {
                                Long delta = Long.valueOf(System.currentTimeMillis() - entryTime.longValue());
                                if (CallAnalyzer.logger != null && delta.longValue() > statInfo.config.stuckTimeBeforeDump.longValue()) {
                                    StackLogger access$100 = CallAnalyzer.logger;
                                    StringBuilder append = new StringBuilder().append("Offending thread:\n");
                                    String currentStack = CallAnalyzer.this.getCurrentStack((Thread) info.getKey());
                                    access$100.logWarning(append.append(currentStack).toString());
                                    StringBuilder sb = new StringBuilder();
                                    Thread[] threads = new Thread[5000];
                                    int count = Thread.enumerate(threads);
                                    for (int q = 0; q < count; q++) {
                                        long threadStuck = 0;
                                        if (((HashMap) CallAnalyzer.this.threadMap.get(threads[q])) != null) {
                                            Long stamp = (Long) ((HashMap) CallAnalyzer.this.threadMap.get(threads[q])).get(metricReference);
                                            if (stamp != null) {
                                                threadStuck = System.currentTimeMillis() - stamp.longValue();
                                            }
                                            if (stamp.longValue() != Long.MIN_VALUE) {
                                                sb.append("->Stuck time:" + threadStuck + Separators.SP + CallAnalyzer.this.getCurrentStack(threads[q]));
                                            }
                                        }
                                    }
                                    CallAnalyzer.logger.logWarning(sb.toString());
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        };
        this.timer.scheduleAtFixedRate(statInfo.task, statInfo.config.checkingInterval.longValue(), statInfo.config.checkingInterval.longValue());
    }

    public void stop() {
        this.timer.cancel();
        this.timer = null;
    }

    public Long getTime(Thread threadId, MetricReference metricReference) {
        return (Long) getAttributes(threadId).get(metricReference);
    }

    public void setObject(Thread threadId, MetricReference objectName, Object object) {
        getAttributes(threadId).put(objectName, object);
    }

    public Object getObject(Thread threadId, String objectName) {
        return getAttributes(threadId).get(objectName);
    }

    public synchronized HashMap<MetricReference, Object> getAttributes(Thread threadId) {
        HashMap<MetricReference, Object> threadLocal;
        threadLocal = (HashMap) this.threadMap.get(threadId);
        if (threadLocal == null) {
            threadLocal = new HashMap();
            this.threadMap.put(threadId, threadLocal);
        }
        return threadLocal;
    }

    public void enter(MetricReference metricReference) {
        enter(Thread.currentThread(), metricReference);
    }

    public void leave(MetricReference metricReference) {
        leave(Thread.currentThread(), metricReference);
    }

    public void enter(Thread threadId, MetricReference metricReference) {
        getAttributes(threadId).put(metricReference, Long.valueOf(System.currentTimeMillis()));
    }

    public void leave(Thread threadId, MetricReference metricReference) {
        TImeMetricInfo info = this.metricStatisticsMap.get((Object) metricReference);
        HashMap<MetricReference, Object> attribs = getAttributes(threadId);
        info.totalTime = Long.valueOf(info.totalTime.longValue() + (System.currentTimeMillis() - ((Long) attribs.get(metricReference)).longValue()));
        Long l = info.numberOfEvents;
        info.numberOfEvents = Long.valueOf(info.numberOfEvents.longValue() + 1);
        info.averageTime = Long.valueOf(info.totalTime.longValue() / info.numberOfEvents.longValue());
        attribs.put(metricReference, Long.valueOf(Long.MIN_VALUE));
    }

    public String getCurrentStack(Thread thread) {
        StringBuilder sb = new StringBuilder();
        sb.append(Separators.RETURN + thread.getName() + Separators.SP + thread.getId() + Separators.SP + thread.getState().toString() + Separators.RETURN);
        for (StackTraceElement el : thread.getStackTrace()) {
            sb.append(Separators.SP + el.toString() + Separators.RETURN);
        }
        return sb.toString();
    }

    public String getThreadDump() {
        StringBuilder sb = new StringBuilder();
        Thread[] threads = new Thread[5000];
        int count = Thread.enumerate(threads);
        for (int q = 0; q < count; q++) {
            sb.append(getCurrentStack(threads[q]));
        }
        return sb.toString();
    }

    public int getNumberOfThreads() {
        return this.threadMap.size();
    }

    public static void main(String[] arg) throws InterruptedException {
        ExecutorService ex = Executors.newFixedThreadPool(1000);
        CallAnalyzer tp = new CallAnalyzer();
        final MetricReference sec = new MetricReference("sec");
        MetricReference se1c = new MetricReference("se111c");
        tp.configure(sec, new MetricAnalysisConfiguration(500, 500, 500));
        tp.startAnalysis(sec);
        tp.startAnalysis(se1c);
        Runnable r = new Runnable(tp) {
            final /* synthetic */ CallAnalyzer val$tp;

            public void run() {
                this.val$tp.enter(sec);
                try {
                    int i = CallAnalyzer.count + 1;
                    CallAnalyzer.count = i;
                    if (i % MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT == 0) {
                        System.out.println("Avg " + this.val$tp.getMetricStats(sec).averageTime);
                        Thread.sleep(1000);
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.val$tp.leave(sec);
            }
        };
        for (int q = 0; q < 2000000; q++) {
            ex.execute(r);
        }
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        ex.shutdown();
        ex.awaitTermination(200, TimeUnit.SECONDS);
        ex.shutdownNow();
        System.gc();
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        Thread.sleep(5000);
        System.gc();
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        System.gc();
        Thread.sleep(5000);
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        System.gc();
        Thread.sleep(5000);
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        Thread.sleep(5000);
        System.gc();
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        System.gc();
        Thread.sleep(5000);
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        System.gc();
        Thread.sleep(5000);
        System.out.println("size:" + tp.threadMap.size() + Separators.SP + tp.metricStatisticsMap.size());
        System.gc();
        if (tp.threadMap.size() > 0) {
            throw new RuntimeException("Should be zero by this point. Leak.");
        }
    }
}
