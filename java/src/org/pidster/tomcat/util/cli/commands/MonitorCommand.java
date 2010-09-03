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
import java.lang.management.ThreadMXBean;

import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Monitor properties of a server in real time")
@Descriptor(name = "monitor")
public class MonitorCommand extends AbstractJMXCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.pidster.tomcat
     * .util.cli .Environment)
     */
    @Override
    public void execute() throws CommandException {

        if (getConfig().getArguments().size() > 0) {
            String action = getConfig().getArguments().get(0);
            log("Monitor: " + action);
        }
        else {
            log("Monitor: ???");
        }

        try {
            ThreadMXBean threads = ManagementFactory.newPlatformMXBeanProxy(
                    getConnection(), ManagementFactory.THREAD_MXBEAN_NAME,
                    ThreadMXBean.class);

        }
        catch (IOException e) {
            throw new CommandException(e.getMessage(), e.getCause());
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