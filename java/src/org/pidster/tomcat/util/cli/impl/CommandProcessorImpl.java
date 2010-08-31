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

package org.pidster.tomcat.util.cli.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.pidster.tomcat.util.cli.CommandLine;
import org.pidster.tomcat.util.cli.CommandParser;

/**
 * @author pidster
 * 
 */
public class CommandProcessorImpl implements CommandParser {

    private long count;

    private boolean interactive;

    private final LinkedList<CommandLine> history;

    /**
     * @param registry
     */
    public CommandProcessorImpl() {
        this.count = 0;
        this.interactive = false;
        this.history = new LinkedList<CommandLine>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.CommandParser#parseArguments(java.lang
     * .String)
     */
    @Override
    public CommandLine parseArguments(String... argArray) {

        if ((argArray.length == 1) && (argArray[0].indexOf(' ') > -1)) {
            argArray = argArray[0].replaceAll("[\\s ]+", " ").split(" ");
        }

        // this.history.put(count, arguments);

        List<String> arguments = new LinkedList<String>();
        arguments.addAll(Arrays.asList(argArray));

        if (arguments.contains("--interactive")) {
            arguments.remove("--interactive");
            this.interactive = true;
        }

        CommandLineImpl line = new CommandLineImpl();

        for (String arg : arguments) {
            if (arg.startsWith("-")) {
                line.addOption(arg);
            }
            else {
                line.addArgument(arg);
            }
        }

        history.add(line);

        return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandParser#isExit()
     */
    @Override
    public boolean isExit() {

        if (history.size() < 1)
            return false;

        String commandName = history.getLast().getCommandName();

        if ("exit".equalsIgnoreCase(commandName))
            return true;
        if ("quit".equalsIgnoreCase(commandName))
            return true;

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandParser#first()
     */
    @Override
    public boolean first() {
        count++;
        return (count <= 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandParser#isInteractive()
     */
    @Override
    public boolean isInteractive() {
        return this.interactive;
    }

}
