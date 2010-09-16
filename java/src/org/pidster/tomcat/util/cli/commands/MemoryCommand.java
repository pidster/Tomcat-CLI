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
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.management.ObjectName;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Display memory, heap information")
@Descriptor(name = "memory")
public class MemoryCommand extends AbstractJMXCommand {

    /**
     * @author pidster
     * 
     */
    private final class ObjectNameComparator implements Comparator<ObjectName> {
        @Override
        public int compare(ObjectName o1, ObjectName o2) {
            String type1 = attribute(o1, "Type");
            String type2 = attribute(o2, "Type");
            return type2.compareTo(type1);
        }
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        try {

            ObjectName system = query("java.lang:type=OperatingSystem").get(0);

            log("tomcatcli.commands.memory.systemHeader");

            // long maxFileDescriptorCount = attribute(system,
            // "MaxFileDescriptorCount");
            // long processCpuTime = attribute(system, "ProcessCpuTime");

            Long freePhysicalMemorySize = attribute(system, "FreePhysicalMemorySize");
            Long totalPhysicalMemorySize = attribute(system, "TotalPhysicalMemorySize");

            Long committedVirtualMemorySize = attribute(system, "CommittedVirtualMemorySize");

            Long freeSwapSpaceSize = attribute(system, "FreeSwapSpaceSize");
            Long totalSwapSpaceSize = attribute(system, "TotalSwapSpaceSize");

            log(String.format(" Physical: %s / %s    Swap: %s / %s    Committed VM: %s", format(totalPhysicalMemorySize
                    - freePhysicalMemorySize), format(totalPhysicalMemorySize), format(totalSwapSpaceSize
                    - freeSwapSpaceSize), format(totalSwapSpaceSize), format(committedVirtualMemorySize)));

            MemoryMXBean memory = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

            log("");
            log("tomcatcli.commands.memory.overviewHeader");

            displayMemoryUsage("NON_HEAP", "", memory.getNonHeapMemoryUsage());
            displayMemoryUsage("HEAP", "", memory.getHeapMemoryUsage());

            log("");
            log("tomcatcli.commands.memory.segmentsHeader");

            List<ObjectName> pools = query("java.lang:type=MemoryPool,name=*");

            Collections.sort(pools, new ObjectNameComparator());

            for (ObjectName obj : pools) {
                String name = attribute(obj, "Name");
                String type = attribute(obj, "Type");

                MemoryPoolMXBean pool = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                        ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + name, MemoryPoolMXBean.class);

                if (pool.getUsage() != null)
                    displayMemoryUsage(type, name, pool.getUsage());

                if (getConfig().isOptionSet("debug") && (pool.getCollectionUsage() != null))
                    displayMemoryUsage(type, name + " (coll)", pool.getCollectionUsage());

                if (getConfig().isOptionSet("verbose") && (pool.getPeakUsage() != null))
                    displayMemoryUsage(type, name + " (peak)", pool.getPeakUsage());
            }

            log(""); // finish with a blank line

        }
        catch (IOException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }
    }

    /**
     * @param mu
     */
    private void displayMemoryUsage(String type, String name, MemoryUsage mu) {
        Object[] args = new Object[] { type, name, format(mu.getInit()), format(mu.getUsed()),
                perc(mu.getUsed(), mu.getMax()), format(mu.getCommitted()), perc(mu.getCommitted(), mu.getMax()),
                format(mu.getMax()) };

        log(String.format(" %-8s %-25s %9s %9s %-7s %9s %-7s %9s", args));
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
    private String format(double count) {

        DecimalFormat df = new DecimalFormat("###0.0");

        return df.format(count / (1024 * 1024)) + "M";
    }

}
