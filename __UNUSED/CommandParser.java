/* *  Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.tomcat.util.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pidster
 * 
 */
public class CommandParser {

    private final Map<String, Command> registry;

    private final Environment environment;

    private String commandName;

    /**
     * @param original
     */
    public CommandParser(Map<String, Command> registry, String[] original) {

        this.registry = registry;
        this.environment = new Environment();

        List<String> arguments = new ArrayList<String>();

        for (String arg : original) {
            if (arg.startsWith("--")) {
                environment.setOption(arg);
            }
            if (arg.startsWith("-")) {
                environment.setOption(arg);
            }
            else {
                arguments.add(arg);
            }
        }

        if (arguments.size() > 0) {
            this.commandName = arguments.get(0);
        }
    }

    /**
     * @return interactive
     */
    public boolean isInteractive() {

        if (environment.isOptionSet("interactive")) {
            return Boolean.parseBoolean(environment.getOption("interactive"));
        }

        return false;
    }

    /**
     * @return verbose?
     */
    public boolean isVerbose() {

        if (environment.isOptionSet("verbose")) {
            return Boolean.parseBoolean(environment.getOption("verbose"));
        }

        return false;
    }

    /**
     * @return
     */
    public boolean isExit() {

        if ("exit".equalsIgnoreCase(commandName))
            return true;
        if ("quit".equalsIgnoreCase(commandName))
            return true;

        return false;
    }

    /**
     * @return command
     */
    public boolean hasCommand() {
        return (commandName != null);
    }

    /**
     * @return outcome
     */
    public boolean foundCommand() {
        return registry.containsKey(commandName);
    }

    /**
     * @return command
     */
    public Command getCommand() {
        Command c = registry.get(commandName);

        return c;
    }

    /**
     * @return name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * @return
     */
    public Environment getEnvironment() {
        return environment;
    }
}
