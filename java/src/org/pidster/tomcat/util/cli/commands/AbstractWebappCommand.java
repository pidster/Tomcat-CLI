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

import java.util.SortedSet;

import javax.management.ObjectName;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;

/**
 * @author pidster
 * 
 */
@Options({
    @Option(name = "webapp", single = 'a', setter = true, required = true, description = "Selects a specific application context")
})
public abstract class AbstractWebappCommand extends StatusCommand {

    private final String command;

    /**
     * @param command
     */
    public AbstractWebappCommand(String command) {
        this.command = command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.apache.tomcat.
     * util.cli .Environment)
     */
    @Override
    public void execute() throws CommandException {

        try {

            // required, so should already be checked
            String docBase = getConfig().getOptionValue("webapp");

            StringBuilder s = new StringBuilder();

            String engine = "*";
            if (getConfig().isOptionSet("engine"))
                engine = getConfig().getOptionValue("engine");

            String hostname = "*";
            if (getConfig().isOptionSet("hostname"))
                hostname = getConfig().getOptionValue("hostname");

            s.append(engine);
            s.append(":type=Host,host=");
            s.append(hostname);

            ObjectName host = ObjectName.getInstance(s.toString());
            SortedSet<ObjectName> hosts = query(host, null);

            if (hosts.size() != 1) {
                log("hosts query:" + s.toString());
                throw new CommandException("Expected one Host, found: "
                        + hosts.size());
            }

            ObjectName[] children = (ObjectName[]) attribute(hosts.first(),
                    "children");

            for (ObjectName webapp : children) {
                String appDocBase = (String) attribute(webapp, "docBase");
                if (docBase.equals(appDocBase)) {
                    invoke(webapp, command, new Object[0], new String[0]);
                }
            }

            super.execute();
        }
        catch (Exception e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }

    }
}
