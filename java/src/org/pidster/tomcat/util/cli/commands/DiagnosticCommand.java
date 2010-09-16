/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.pidster.tomcat.util.cli.commands;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;

import org.pidster.tomcat.util.cli.commands.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "<options>", description = "Sample various properties of an instance, compare data and report potential problems")
@Descriptor(name = "diagnostic")
@Options({
        @Option(name = "samples", single = 'S', setter = true, value = "10", description = "Sets the number of samples"),
        @Option(name = "duration", single = 'D', setter = true, value = "1", description = "Sets the sample period") })
public class DiagnosticCommand extends AbstractJMXCommand {

    /**
     * @author pidster
     * 
     */
    private final class Collector implements Runnable {

        @Override
        public void run() {

            long start = System.currentTimeMillis();

            try {
                if (isVerbose()) {
                    log("tomcatcli.commands.diagnostic.collecting", initCounter.get());
                }
                else {
                    if ((initCounter.get() > 0) && ((initCounter.get() % 72) == 0))
                        System.out.println();

                    System.out.print(".");
                }

                collect();
                runCounter.incrementAndGet();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println();
            }

            long end = System.currentTimeMillis();

            System.out.println("collection: " + (end - start) + "ms");
        }

        /**
         * @throws IOException
         * 
         */
        private void collect() throws IOException {

            ObjectName system = query("java.lang:type=OperatingSystem").get(0);

            ObjectName global = query("Catalina:type=GlobalRequestProcessor,name=*").get(0);

            ObjectName executor = query("Catalina:type=Executor,name=*").get(0);

            OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            ThreadMXBean threads = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

            CompilationMXBean compilation = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.COMPILATION_MXBEAN_NAME, CompilationMXBean.class);

            ClassLoadingMXBean loading = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);

            // TODO build MBeans in one go, then utilise

            // TODO use j.u.c.ExecutorService
            // with 3 threads to ensure collection is timely

            Long freeSwapSpaceSize = attribute(system, "FreeSwapSpaceSize");
            Long totalSwapSpaceSize = attribute(system, "TotalSwapSpaceSize");

            Report r = new Report();
            r.loadAverage = os.getSystemLoadAverage();
            r.usedSwapSpaceSize = (totalSwapSpaceSize - freeSwapSpaceSize);

            // collect thread counts
            r.daemonThreadCount = threads.getDaemonThreadCount();
            r.peakThreadCount = threads.getPeakThreadCount();
            r.threadCount = threads.getThreadCount();
            r.totalStartedThreads = threads.getTotalStartedThreadCount();

            // collect blocked/locked threads
            if (threads.findDeadlockedThreads() != null) {
                r.countDeadLocked = threads.findDeadlockedThreads().length;
            }
            if (threads.findMonitorDeadlockedThreads() != null) {
                r.countMonitorLocked = threads.findMonitorDeadlockedThreads().length;
            }

            // collect global request processor counts
            // Assumes there's one in Service Catalina
            r.errorCount = attribute(global, "errorCount");
            r.requestCount = attribute(global, "requestCount");

            // TODO collect request processor counts
            // examine individual RPs?

            // collect executor pool stats, monitor peaking
            r.activeCount = attribute(executor, "activeCount");
            r.completedTaskCount = attribute(executor, "completedTaskCount");
            r.poolSize = attribute(executor, "poolSize");
            r.queueSize = attribute(executor, "queueSize");

            // TODO collect datasource pool stats, monitor peaking
            // Might need to be pool-specific

            // Monitor compilation
            r.totalCompilationTime = compilation.getTotalCompilationTime();

            // collect webapp classloader count
            r.loadedClassCount = loading.getLoadedClassCount();
            r.unloadedClassCount = loading.getUnloadedClassCount();
            r.totalLoadedClassCount = loading.getTotalLoadedClassCount();

            // TODO collect GC stats, monitor rate of minor / major GCs
            // GarbageCollectorMXBean gc =
            // ManagementFactory.newPlatformMXBeanProxy(getConnection(),
            // ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name="
            // +
            // name, GarbageCollectorMXBean.class);

            reports.add(r);
        }

    }

    /**
     * @author pidster
     */
    private static class Report {

        double loadAverage = 0;

        Long usedSwapSpaceSize = 0L;

        Integer countDeadLocked = 0;

        Integer countMonitorLocked = 0;

        Integer daemonThreadCount = 0;

        Integer peakThreadCount = 0;

        Integer threadCount = 0;

        Long totalStartedThreads = 0L;

        Integer loadedClassCount = 0;

        Long unloadedClassCount = 0L;

        Long totalLoadedClassCount = 0L;

        Long totalCompilationTime = 0L;

        Integer errorCount = 0;

        Integer requestCount = 0;

        Integer activeCount = 0;

        Long completedTaskCount = 0L;

        Integer poolSize = 0;

        Integer queueSize = 0;

        private Report() {
            super();
        }

    }

    private final AtomicLong initCounter;

    private final AtomicLong runCounter;

    private volatile List<Report> reports;

    private final ExecutorService service;

    /**
     * 
     */
    public DiagnosticCommand() {
        initCounter = new AtomicLong(0);
        runCounter = new AtomicLong(0);
        reports = new ArrayList<Report>();

        ThreadFactory factory = new ThreadFactory() {

            private final AtomicLong serial = new AtomicLong(0);

            private final ThreadGroup group = new ThreadGroup("CollectorGroup");

            @Override
            public Thread newThread(Runnable r) {

                Thread t = new Thread(group, r, "collector-" + serial.incrementAndGet());

                t.setDaemon(true);

                return t;
            }
        };

        service = Executors.newCachedThreadPool(factory);
        // service = Executors.newFixedThreadPool(2, factory);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        String samples = getConfig().getOptionValue("samples");

        // TODO make duration parsable, Ns Nm Nh, default 's'econds
        String duration = getConfig().getOptionValue("duration");

        TimeUnit unit = TimeUnit.SECONDS;

        int maxSamples = Integer.parseInt(samples);

        if (maxSamples > 1000)
            maxSamples = 1000;

        int delay = Integer.parseInt(duration) / maxSamples;

        if (delay < 1) {
            delay = 1;
        }

        log("tomcatcli.commands.diagnostic.sampling", maxSamples, Integer.parseInt(duration), unit.toString()
                .toLowerCase());
        log("tomcatcli.commands.diagnostic.starting");

        try {
            long start = System.currentTimeMillis();

            Collector collector = new Collector();

            while (initCounter.get() < maxSamples) {
                service.execute(collector);
                Thread.sleep(delay * 1000);
                initCounter.incrementAndGet();
            }

            while (runCounter.get() < maxSamples) {
                Thread.sleep(1000);
            }

            service.shutdown();

            log("");
            log("tomcatcli.commands.diagnostic.processing", maxSamples);

            analyze();

            report();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void analyze() {
        // TODO find memory leaks from webapp reloads
        // TODO find blocked, locked threads - follow object graph to find cause
        // TODO Detect resource pool contention
    }

    /**
     * 
     */
    private void report() {
        log("tomcatcli.commands.diagnostic.report");

        log("tomcatcli.commands.diagnostic.reportHeader");

        double minLoad = reports.get(0).loadAverage;
        double maxLoad = 0;
        double totalLoad = 0;

        long minSwap = reports.get(0).usedSwapSpaceSize;
        long maxSwap = 0;
        long totalSwap = 0;

        int index = 0;
        for (Report r : reports) {

            if (minLoad > r.loadAverage)
                minLoad = r.loadAverage;

            if (maxLoad < r.loadAverage)
                maxLoad = r.loadAverage;

            totalLoad += r.loadAverage;

            if (minSwap > r.usedSwapSpaceSize)
                minSwap = r.usedSwapSpaceSize;

            if (maxSwap < r.usedSwapSpaceSize)
                maxSwap = r.usedSwapSpaceSize;

            totalSwap += r.usedSwapSpaceSize;

            log(String.format(" %-4s %-8s %-8s", index, format(r.loadAverage), formatMegs(r.usedSwapSpaceSize)));
            index++;
        }

        log("");
        log(String.format(" min  %-8s %-8s", format(minLoad), formatMegs(minSwap)));
        log(String.format(" max  %-8s %-8s", format(maxLoad), formatMegs(maxSwap)));
        log(String
                .format(" avg  %-8s %-8s", format(totalLoad / reports.size()), formatMegs(totalSwap / reports.size())));

        log(String.format(" var  %-8s %-8s", format(maxLoad - minLoad), formatMegs(maxSwap - minSwap)));

        log("");

        if ((totalLoad / reports.size()) > 2) {
            // Log warning
            log("ALERT!  Load is above normal");
        }

        if ((totalSwap / reports.size()) > 1024 * 32) {
            // Log warning
            log("ALERT!  Swap is above 32k, swap usage is bad for webservers");
        }

    }

    /**
     * @param fraction
     * @param maximum
     * @return formatted
     */
    private String perc(double fraction, double maximum) {

        DecimalFormat df = new DecimalFormat("###0.0");

        return df.format((fraction * 100) / maximum) + "%";
    }

    /**
     * @param count
     * @return formatted
     */
    private String formatMegs(double count) {

        DecimalFormat df = new DecimalFormat("###0.0");

        return df.format(count / (1024 * 1024)) + "M";
    }

    /**
     * @param count
     * @return formatted
     */
    private String format(double count) {

        DecimalFormat df = new DecimalFormat("###0.00");

        return df.format(count);
    }

}
