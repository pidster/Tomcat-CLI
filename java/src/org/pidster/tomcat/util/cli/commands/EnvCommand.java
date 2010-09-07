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
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.TreeMap;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Dump environment")
@Descriptor(name = "env")
public class EnvCommand extends AbstractJMXCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.pidster.tomcat
     * .util.cli .Environment)
     */
    @Override
    public void execute() throws CommandException {

        try {
            StringBuilder s = new StringBuilder();

            RuntimeMXBean runtime = ManagementFactory.newPlatformMXBeanProxy(
                    getConnection(), ManagementFactory.RUNTIME_MXBEAN_NAME,
                    RuntimeMXBean.class);

            Map<String, String> properties = runtime.getSystemProperties();
            s.append("\nDumping System environment variables...\n");

            TreeMap<String, String> names = new TreeMap<String, String>(
                    properties);

            for (String name : names.keySet()) {
                s.append(" ");
                s.append(name);
                s.append("=");
                s.append(properties.get(name));
                s.append("\n");
            }

            log(s.toString());
        }
        catch (IOException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }
    }
}
