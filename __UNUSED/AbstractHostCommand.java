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

import java.util.HashMap;
import java.util.Map;
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
        @Option(name = "engine", single = 'e', setter = true, required = false, description = "Selects a specific engine"),
        @Option(name = "hostname", single = 'n', setter = true, required = true, description = "Selects a specific hostname")
})
public abstract class AbstractHostCommand extends StatusCommand {

    private final Map<String, String> commandMap;

    /**
     * 
     */
    protected AbstractHostCommand() {
        this.commandMap = new HashMap<String, String>();
        this.commandMap.put("start-host", "start");
        this.commandMap.put("stop-host", "stop");
        this.commandMap.put("findleaks", "findReloadedContextMemoryLeaks");
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
            String command = getConfig().getCommandName();

            if (!commandMap.containsKey(command)) {
                throw new CommandException("Unknown command " + command);
            }

            String method = commandMap.get(command);

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

            Object obj = invoke(hosts.first(), method, new Object[0],
                    new String[0]);

            handleMethodResult(obj);
        }
        catch (Exception e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }
    }

    /**
     * @param obj
     * @throws CommandException
     */
    protected void handleMethodResult(Object obj) throws CommandException {
        super.execute();
    }

}