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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.CommandRegistry;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;

/**
 * @author pidster
 * 
 */
public class CommandRegistryImpl implements CommandRegistry {

    private final Map<String, Command> commands;

    private final Map<Command, Option[]> commandOptions;

    /**
     * 
     */
    public CommandRegistryImpl() {
        this.commands = new HashMap<String, Command>();
        this.commandOptions = new HashMap<Command, Option[]>();
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
        commandOptions.put(command, d.options());

    }

    /* (non-Javadoc)
     * @see org.pidster.tomcat.util.cli.CommandRegistry#isRegistered(java.lang.String)
     */
    @Override
    public boolean isRegistered(String name) {
        if (name == null) {
            return false;
        }
        return commands.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.pidster.tomcat.util.cli.CommandRegistry#getViableOptions(org.pidster.tomcat.util.cli.Command)
     */
    @Override
    public Option[] getViableOptions(Command command) {
        return commandOptions.get(command);
    }

    /* (non-Javadoc)
     * @see org.pidster.tomcat.util.cli.CommandRegistry#commands()
     */
    @Override
    public Collection<Command> commands() {
        return this.commands.values();
    }

    /* (non-Javadoc)
     * @see org.pidster.tomcat.util.cli.CommandRegistry#get(java.lang.String)
     */
    @Override
    public Command get(String commandName) {
        return this.commands.get(commandName);
    }

    /* (non-Javadoc)
     * @see org.pidster.tomcat.util.cli.CommandRegistry#getOptions()
     */
    @Override
    public Map<Command, Option[]> getOptions() {
        return this.commandOptions;
    }

}
