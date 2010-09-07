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

import java.util.Map;

import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.CommandConfig;
import org.pidster.tomcat.util.cli.CommandLine;
import org.pidster.tomcat.util.cli.CommandParser;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.OptionParser;
import org.pidster.tomcat.util.cli.ConsoleUI;
import org.pidster.tomcat.util.cli.commands.HelpCommand;
import org.pidster.tomcat.util.cli.util.StringManager;

/**
 * @author pidster
 */
public class ConsoleUIImpl implements ConsoleUI {

    private static final StringManager sm = StringManager.getManager(Constants.PACKAGE_NAME);

    private final CommandParser commandParser;

    private final CommandRegistry registry;

    private final OptionParser optionParser;

    private final EnvironmentImpl environment;

    /**
     * 
     */
    public ConsoleUIImpl() {
        this.registry = new CommandRegistry();
        this.commandParser = new CommandParserImpl();
        this.environment = new EnvironmentImpl();
        this.optionParser = new OptionParserImpl(registry.getOptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void register(Iterable<Command> commands) {

        for (Command command : commands) {
            this.registry.register(command);
        }

        // Deliberately override any other help commands
        this.registry.register("help", new HelpCommand(registry.getCommands()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void process(String[] arguments) {

        CommandLine line = commandParser.parseArguments(arguments);
        boolean debug = commandParser.isDebug();
        boolean interactive = commandParser.isInteractive();

        // Is there a more elegant solution?
        // If it's the first time, or we're interactive
        while (commandParser.first() || interactive) {

            // check this first, just in case
            if (commandParser.isExit())
                break;

            // if there's no command and we're not interactive
            if (!line.hasCommand() && (!interactive)) {
                environment.sysout(sm.getString("tomcat.cli.usage"));
                break;
            }

            // if we have a command and we didn't match it
            if (line.hasCommand() && !registry.isRegistered(line.getCommandName())) {
                environment.sysout(sm.getString("tomcat.cli.commandNotFound", line.getCommandName()));
            }

            // if we found a command
            else if (registry.isRegistered(line.getCommandName())) {

                Command command = registry.get(line.getCommandName());

                try {

                    Map<Option, String> options = optionParser.activeOptions(line.getOptions(), command);

                    CommandConfig config = new CommandConfigImpl(environment, line.getCommandName(),
                            line.getArguments(), options);

                    command.configure(config);

                    command.execute();
                }
                catch (Throwable t) {

                    if (debug)
                        t.printStackTrace();

                    environment.sysout(t.getMessage());
                }
                finally {
                    command.cleanup();
                }
            }

            // Update the command line, if we're still running
            if (interactive) {
                String[] src = environment.readPrompt();
                String[] dst = new String[src.length + arguments.length];

                System.arraycopy(src, 0, dst, 0, src.length);
                System.arraycopy(arguments, 0, dst, src.length, arguments.length);

                line = commandParser.parseArguments(dst);
            }
        }
    }
}
