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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.pidster.tomcat.util.cli.AbstractCommand;
import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "<command>", description = "Display help")
@Descriptor(name = "help")
public class HelpCommand extends AbstractCommand {

    private final Collection<Command> commands;

    /**
     * @param commands
     * 
     */
    public HelpCommand(Collection<Command> commands) {
        this.commands = commands;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Command#execute()
     */
    @Override
    public void execute() throws CommandException {

        StringBuilder s = new StringBuilder();

        List<String> arguments = getConfig().getArguments();

        if (arguments.size() != 2) {
            s.append("Synopis:\n");
            s.append("\t./tomcat-cli.sh <command> <options>\n");
            s.append("\tjava -jar tomcat-cli.jar <command> <options>\n");
            s.append("\n");
            s.append("Registered commands:\n");
            s.append("\t");
            for (Command command : this.commands) {
                Descriptor descriptor = command.getClass().getAnnotation(
                        Descriptor.class);
                s.append(descriptor.name());
                s.append(" ");
            }
            s.append("\n\n");
            s.append("Description:\n");
            s.append("\tTBC");
            s.append("\n\n");

            log(s.toString());
            return;
        }

        String commandName = arguments.get(1);

        for (Command command : this.commands) {

            Descriptor descriptor = command.getClass().getAnnotation(
                    Descriptor.class);

            if (!descriptor.name().equalsIgnoreCase(commandName)) {
                continue;
            }

            Usage usage = command.getClass().getAnnotation(Usage.class);

            s.append("Synopis:\n");

            s.append("\t./tomcat-cli.sh ");
            s.append(descriptor.name());
            s.append(" ");
            s.append(usage.syntax());
            s.append("\n");
            s.append("\tjava -jar tomcat-cli.jar ");
            s.append(descriptor.name());
            s.append(" ");
            s.append(usage.syntax());
            s.append("\n\n");

            s.append("Options:\n");

            List<Option> options = new LinkedList<Option>();

            Class<?> c = command.getClass();
            while (!Object.class.equals(c)) {

                if (c.isAnnotationPresent(Options.class)) {
                    Options o = c.getAnnotation(Options.class);
                    options.addAll(Arrays.asList(o.value()));
                }

                c = c.getSuperclass();
            }

            for (Option o : options) {
                if (!o.setter())
                    continue;
                s.append(String.format("\t-%-5s --%-18s %s", o.single()
                        + ":val", o.name() + ":val", o.description()));
                s.append("\n");
            }

            s.append("\n");

            for (Option o : options) {
                if (o.setter())
                    continue;
                s.append(String.format("\t-%s --%-22s %s", o.single(),
                        o.name(), o.description()));
                s.append("\n");
            }

            s.append("\n");
            s.append("Description:\n");
            s.append("\t");
            s.append(usage.description());
            s.append("\n\n");

        }

        log(s.toString());
    }
}
