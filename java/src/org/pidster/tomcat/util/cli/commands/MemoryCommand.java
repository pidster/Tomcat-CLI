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
import java.lang.management.MemoryUsage;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;

import org.pidster.tomcat.util.cli.AbstractJMXCommand;
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
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        try {

            MemoryMXBean memory = ManagementFactory.newPlatformMXBeanProxy(
                    getConnection(), ManagementFactory.MEMORY_MXBEAN_NAME,
                    MemoryMXBean.class);

            log("JVM Memory:");
            log(" type ---------- init ---- used % ---- committed % ---------- max % ----");

            displayMemoryUsage("Heap", memory.getHeapMemoryUsage());
            displayMemoryUsage("Non-Heap", memory.getNonHeapMemoryUsage());

        }
        catch (IOException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }
    }

    /**
     * @param mu
     */
    private void displayMemoryUsage(String type, MemoryUsage mu) {
        String template = " %-10s %9s %9s %-6s %9s %-6s %9s %-6s";

        Object[] args = new Object[] {
                type, format(mu.getInit()), format(mu.getUsed()),
                perc(mu.getUsed(), mu.getInit()), format(mu.getCommitted()),
                perc(mu.getCommitted(), mu.getInit()), format(mu.getMax()),
                perc(mu.getMax(), mu.getInit())
        };

        log(template, args);
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
