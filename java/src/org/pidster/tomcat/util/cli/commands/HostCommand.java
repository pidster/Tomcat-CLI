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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "start|stop|findleaks <options>", description = "Execute host functions")
@Descriptor(name = "host")
@Options({
        @Option(name = "engine", single = 'E', setter = true, required = false, description = "Selects a specific engine"),
        @Option(name = "hostname", single = 'H', setter = true, required = true, description = "Selects a specific hostname")
})
public class HostCommand extends AbstractJMXCommand {

    private final Map<String, String> commandMap;

    /**
     * 
     */
    public HostCommand() {
        this.commandMap = new HashMap<String, String>();
        this.commandMap.put("start", "start");
        this.commandMap.put("stop", "stop");
        this.commandMap.put("findleaks", "findReloadedContextMemoryLeaks");
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        try {
            if (getConfig().getArguments().isEmpty()) {
                throw new CommandException(
                        "Sub-command required: start|stop|findleaks");
            }

            String command = getConfig().getArguments().get(0);
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
            List<ObjectName> hosts = query(host, null, null);

            if (hosts.size() != 1) {
                log("hosts query:" + s.toString());
                throw new CommandException("Expected one Host, found: "
                        + hosts.size());
            }

            Object obj = invoke(hosts.get(0), method, new Object[0],
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

        if (obj == null)
            return;

        if (!obj.getClass().isArray())
            return;

        if (Array.getLength(obj) == 0) {
            log("No leaking apps found.");
            return;
        }

        List<String> leaks = new ArrayList<String>();
        for (int i = 0; i < Array.getLength(obj); i++) {
            Object o = Array.get(obj, i);
            if (!String.class.isInstance(o))
                continue;

            leaks.add(String.class.cast(o));
        }

        log("Found %s leaking apps...", Array.getLength(obj));

        for (String leak : leaks) {
            log("Leaking app name: %s", leak);
        }
    }

}
