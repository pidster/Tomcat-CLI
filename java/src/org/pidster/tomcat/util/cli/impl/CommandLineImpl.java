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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pidster.tomcat.util.cli.CommandLine;

/**
 * @author pidster
 * 
 */
public class CommandLineImpl implements Serializable, CommandLine {

    private static final long serialVersionUID = 1L;

    private final List<String> arguments;

    private final List<String> options;

    private String prompt;

    /**
     * 
     */
    public CommandLineImpl() {
        this.prompt = "> ";
        this.arguments = new ArrayList<String>();
        this.options = new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#getPrompt()
     */
    @Override
    public String getPrompt() {
        return prompt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#setPrompt(java.lang.String)
     */
    @Override
    public void setPrompt(String prompt) {

        if (prompt == null || prompt.isEmpty()) {
            prompt = "> ";
        }

        this.prompt = prompt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#getOptions()
     */
    @Override
    public List<String> getOptions() {
        return this.options;
    }

    /**
     * @param option
     */
    public void addOption(String option) {
        System.out.println("option: " + option);
        this.options.add(option);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#getCommandName()
     */
    @Override
    public final String getCommandName() {
        if (arguments.size() == 0) {
            return null;
        }
        return arguments.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#hasCommand()
     */
    @Override
    public boolean hasCommand() {
        return (arguments.size() >= 1);
    }

    /**
     * @param argument
     */
    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandLine#getArguments()
     */
    @Override
    public final List<String> getArguments() {
        if (arguments.size() > 1) {
            return arguments.subList(1, arguments.size() - 1);
        }
        return Collections.emptyList();
    }

}
