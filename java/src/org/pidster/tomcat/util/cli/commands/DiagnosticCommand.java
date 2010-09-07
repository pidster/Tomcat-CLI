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
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

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
     */
    private static class Report {

        public double loadAverage;

        private Report() {
            super();
        }

    }

    private final List<Report> reports;

    /**
     * 
     */
    public DiagnosticCommand() {
        reports = new ArrayList<Report>();
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        String samples = getConfig().getOptionValue("samples");
        String duration = getConfig().getOptionValue("duration");

        log("Will take %s samples, duration %s minutes", samples, duration);
        log("Diagnostic starting, please wait...");

        int maxSamples = Integer.parseInt(samples);
        int counter = 0;
        int delay = (Integer.parseInt(duration) * 60 * 1000) / maxSamples;

        while (counter < maxSamples) {

            try {
                log("Collecting sample %d...", counter);
                collect();
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            counter++;
        }

        log("%d samples collected, now processing...", counter);

        process();

        report();
    }

    /**
     * @throws IOException
     * 
     */
    private void collect() throws IOException {

        OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        Report r = new Report();
        // TODO collect system load average
        r.loadAverage = os.getSystemLoadAverage();

        // // TODO collect GC stats, monitor rate of minor / major GCs
        // GarbageCollectorMXBean gc =
        // ManagementFactory.newPlatformMXBeanProxy(getConnection(),
        // ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" +
        // name, GarbageCollectorMXBean.class);

        // TODO collect thread error counts

        // TODO collect executor pool stats, monitor peaking

        // TODO collect datasource pool stats, monitor peaking

        // TODO collect webapp classloader count

        this.reports.add(r);
    }

    /**
     * 
     */
    private void process() {
        // TODO find memory leaks from webapp reloads

    }

    /**
     * 
     */
    private void report() {
        log("Report:");

        log(" load av ");
        for (Report r : reports) {
            log(" %s", r.loadAverage);
        }
    }

}
