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
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "deadlocked | monitorlocked | id <id>", description = "Get data about threads")
@Descriptor(name = "threads")
public class ThreadsCommand extends AbstractJMXCommand {

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        try {
            ThreadMXBean threads = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

            if (threads.isThreadContentionMonitoringSupported()) {
                if (!threads.isThreadContentionMonitoringEnabled())
                    threads.setThreadContentionMonitoringEnabled(true);
            }

            if (threads.isThreadCpuTimeSupported()) {
                if (!threads.isThreadCpuTimeEnabled())
                    threads.setThreadCpuTimeEnabled(true);
            }

            log(String.format("Thread count:%d peak:%d daemon:%d started:%d", threads.getThreadCount(),
                    threads.getPeakThreadCount(), threads.getDaemonThreadCount(), threads.getTotalStartedThreadCount()));

            List<String> arguments = getConfig().getArguments();

            if (arguments.contains("deadlocked")) {
                displayDeadLocked(threads);
            }
            else if (arguments.contains("monitorlocked")) {
                displayMonitorLocked(threads);
            }
            else if (arguments.contains("id") && (arguments.size() > 1)) {
                String threadId = arguments.get(1);
                ThreadInfo info = threads.getThreadInfo(Integer.parseInt(threadId), Integer.MAX_VALUE);
                displayThreadId(info);
            }
            else {
                displayAllThreads(threads);
            }

        }
        catch (IOException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }

    }

    /**
     * @param threads
     */
    private void displayAllThreads(ThreadMXBean threads) {
        ThreadInfo[] allThreads = threads.getThreadInfo(threads.getAllThreadIds());

        sort(allThreads);

        log(String.format("Listing %d threads...", allThreads.length));
        logThreadInfo(allThreads);
    }

    /**
     * @param threads
     */
    private void displayDeadLocked(ThreadMXBean threads) {
        long[] deadlockedThreadIds = threads.findDeadlockedThreads();
        if (deadlockedThreadIds != null) {
            ThreadInfo[] deadlockedThreads = threads.getThreadInfo(deadlockedThreadIds);
            sort(deadlockedThreads);

            log("Deadlocked threads: " + deadlockedThreads.length);
            if (deadlockedThreads.length > 0) {
                logThreadInfo(deadlockedThreads);
            }
        }
        else {
            log("INFO: No deadlocked threads.");
        }
    }

    /**
     * @param threads
     */
    private void displayMonitorLocked(ThreadMXBean threads) {
        long[] monitorDeadlockedThreadIds = threads.findMonitorDeadlockedThreads();

        if (monitorDeadlockedThreadIds != null) {

            ThreadInfo[] monitorlockedThreads = threads.getThreadInfo(monitorDeadlockedThreadIds);
            sort(monitorlockedThreads);

            log("Monitor deadlocked threads: " + monitorlockedThreads.length);
            if (monitorlockedThreads.length > 0) {
                logThreadInfo(monitorlockedThreads);
            }
        }
        else {
            log("INFO: No monitor deadlocked threads.");
        }
    }

    /**
     * @param info
     */
    private void displayThreadId(ThreadInfo info) {
        log("Display thread: ");
        logThreadInfo(info);

        // TODO display monitors
        // MonitorInfo[] monitors = info.getLockedMonitors();

        // TODO display sync locks
        // LockInfo[] synchronizers = info.getLockedSynchronizers();

        if (info.getLockOwnerId() > -1) {
            log(String.format(" LOCK: %s %s %s", info.getLockName(), info.getLockOwnerId(), info.getLockOwnerName()));
        }

        StackTraceElement[] elements = info.getStackTrace();
        log(String.format("\nStackTrace: %s", info.getThreadName()));
        for (StackTraceElement ste : elements) {
            log(String.format("\tat %s.%s(%s:%s)", ste.getClassName(), ste.getMethodName(), ste.getFileName(),
                    ste.getLineNumber()));
        }
    }

    /**
     * @param infos
     * @return
     */
    private void sort(ThreadInfo[] infos) {
        Arrays.sort(infos, new Comparator<ThreadInfo>() {
            @Override
            public int compare(ThreadInfo one, ThreadInfo two) {
                return one.getThreadName().compareTo(two.getThreadName());
            }
        });
    }

    /**
     * @param info
     */
    private void logThreadInfo(ThreadInfo... infos) {
        log(" id -- state --------- waited -- wtime ---- blocked - btime ---- thread name -----------------------------------------------");
        for (ThreadInfo info : infos) {
            log(String.format(" %-5d %-15s %-9s %-10s %-9s %-10s %-60s", info.getThreadId(), info.getThreadState(),
                    info.getWaitedCount(), info.getWaitedTime(), info.getBlockedCount(), info.getBlockedTime(),
                    info.getThreadName()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.AbstractCommand#cleanup()
     */
    @Override
    public void cleanup() {
        super.cleanup();
    }

}
