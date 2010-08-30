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

package org.pidster.tomcat.util.cli;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pidster
 * 
 */
public class CommandProcessor {

    private long count;

    private boolean interactive;

    private final LinkedList<CommandLine> history;

    /**
     * @param registry
     */
    public CommandProcessor() {
        this.count = 0;
        this.interactive = false;
        this.history = new LinkedList<CommandLine>();
    }

    /**
     * @param arguments
     * @return
     */
    public CommandLine parseArguments(String... arguments) {

        if ((arguments.length == 1) && (arguments[0].indexOf(' ') > -1)) {
            arguments = arguments[0].replaceAll("[\\s ]+", " ").split(" ");
        }

        // this.history.put(count, arguments);

        List<String> original = new LinkedList<String>();
        original.addAll(Arrays.asList(arguments));

        if (original.contains("--interactive")) {
            original.remove("--interactive");
            this.interactive = true;
        }

        CommandLine line = new CommandLine();

        for (String arg : original) {
            if (arg.startsWith("--") || arg.startsWith("-")) {
                line.setOption(arg);
            }
            else {
                line.addArgument(arg);
            }
        }

        history.add(line);

        return line;
    }

    /**
     * @return outcome
     */
    public boolean isExit() {

        String commandName = history.getLast().getCommandName();

        if ("exit".equalsIgnoreCase(commandName))
            return true;
        if ("quit".equalsIgnoreCase(commandName))
            return true;

        return false;
    }

    /**
     * @return
     */
    public boolean first() {
        count++;
        return (count <= 1);
    }

    /**
     * @return
     */
    public boolean isInteractive() {
        return this.interactive;
    }

}
