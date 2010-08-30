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

package org.apache.tomcat.util.cli.commands;

import java.util.Collection;

import org.apache.tomcat.util.cli.ACommand;
import org.apache.tomcat.util.cli.Command;
import org.apache.tomcat.util.cli.Descriptor;
import org.apache.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "<command>", description = "Display help")
@Descriptor(name = "help")
public class HelpCommand extends ACommand {

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
     * @see
     * org.apache.tomcat.util.cli.ACommand#execute(org.apache.tomcat.util.cli
     * .Environment)
     */
    @Override
    public void execute() {

        StringBuilder s = new StringBuilder();

        for (Command command : this.commands) {
            s.append("Syntax: ");

            Descriptor descriptor = command.getClass().getAnnotation(Descriptor.class);
            s.append(descriptor.name());

            if (command.getClass().isAnnotationPresent(Usage.class)) {
                Usage usage = command.getClass().getAnnotation(Usage.class);
                s.append(" ");
                s.append(usage.syntax());
                s.append("\n\t");
                s.append(usage.description());
            }
            s.append("\n");
        }

        s.append("\n");

        getEnvironment().sysout(s.toString());
    }
}
