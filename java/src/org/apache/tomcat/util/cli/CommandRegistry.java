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

package org.apache.tomcat.util.cli;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author pidster
 * 
 */
public class CommandRegistry {

    private final Map<String, Command> commands;

    private final Map<String, Option[]> commandOptions;

    /**
     * 
     */
    public CommandRegistry() {
        this.commands = new TreeMap<String, Command>();
        this.commandOptions = new TreeMap<String, Option[]>();
    }

    /**
     * @param command
     */
    public void register(Command command) {

        Class<?> c = command.getClass();
        if (!c.isAnnotationPresent(Descriptor.class))
            return;

        Descriptor d = c.getAnnotation(Descriptor.class);

        if (commands.containsKey(d.name()))
            return;

        register(d.name(), command);
    }

    /**
     * @param name
     * @param command
     */
    void register(String name, Command command) {

        Class<?> c = command.getClass();
        if (!c.isAnnotationPresent(Descriptor.class))
            return;

        Descriptor d = c.getAnnotation(Descriptor.class);

        commands.put(name, command);
        commandOptions.put(name, d.options());

    }

    /**
     * @param name
     * @return outcome
     */
    public boolean isRegistered(String name) {
        if (name == null) {
            return false;
        }
        return commands.containsKey(name);
    }

    /**
     * @param name
     * @return outcome
     */
    public boolean hasOption(String name, String option) {

        if ((name == null) || (name.isEmpty()))
            return false;

        if ((option == null) || (option.isEmpty()))
            return false;

        if (!commandOptions.containsKey(name))
            return false;

        Option[] options = commandOptions.get(name);

        for (Option o : options) {
            if (o.trigger() == option.charAt(0))
                return true;

            if (o.extended().equalsIgnoreCase(option))
                return true;
        }

        return false;
    }

    /**
     * @return
     */
    public Collection<Command> commands() {
        return this.commands.values();
    }

    /**
     * @param commandName
     * @return command
     */
    public Command get(String commandName) {
        return this.commands.get(commandName);
    }

}
