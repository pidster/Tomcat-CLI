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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
     */
    private static class Report {

        public double loadAverage;

        public long usedSwapSpaceSize;

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

        // TODO make duration parsable, Ns Nm Nh, default 's'econds
        String duration = getConfig().getOptionValue("duration");

        long counter = 0;
        int maxSamples = Integer.parseInt(samples);

        if (maxSamples > 1000)
            maxSamples = 1000;

        int delay = (Integer.parseInt(duration) * 1000) / maxSamples;

        log("tomcatcli.commands.diagnostic.sampling", maxSamples, Integer.parseInt(duration));
        log("tomcatcli.commands.diagnostic.starting");

        while (counter < maxSamples) {

            try {
                if (isVerbose()) {
                    log("Collecting sample {0,number}...", counter);
                }
                else {
                    System.out.print(".");
                }

                collect();
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println();
            }

            counter++;
        }

        log("");
        log("{0,number} samples collected, now processing...", counter);

        process();

        report();
    }

    /**
     * @throws IOException
     * 
     */
    private void collect() throws IOException {

        ObjectName system = query("java.lang:type=OperatingSystem").get(0);

        Long freeSwapSpaceSize = attribute(system, "FreeSwapSpaceSize");
        Long totalSwapSpaceSize = attribute(system, "TotalSwapSpaceSize");

        OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        Report r = new Report();
        r.loadAverage = os.getSystemLoadAverage();
        r.usedSwapSpaceSize = (totalSwapSpaceSize - freeSwapSpaceSize);

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

        log(" run  load av  swap ---");

        double minLoad = 0;
        double maxLoad = 0;
        double totalLoad = 0;

        long minSwap = 0;
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
