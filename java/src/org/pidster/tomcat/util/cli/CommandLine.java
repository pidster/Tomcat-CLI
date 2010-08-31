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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pidster
 * 
 */
public class CommandLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> arguments;

    private final List<String> options;

    private String prompt;

    /**
     * 
     */
    public CommandLine() {
        this.prompt = "> ";
        this.arguments = new ArrayList<String>();
        this.options = new ArrayList<String>();
    }

    /**
     * @return the prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @param the
     *            prompt to set
     */
    public void setPrompt(String prompt) {

        if (prompt == null || prompt.isEmpty()) {
            prompt = "> ";
        }

        this.prompt = prompt;
    }

    // /**
    // * @return is verbose
    // */
    // public boolean isVerbose() {
    // return isOptionSet("verbose");
    // }
    //
    // /**
    // * @param option
    // */
    // public void setOption(String option) {
    //
    // if (option.startsWith("--")) {
    // option = option.substring(2);
    // }
    //
    // else if (option.startsWith("-")) {
    // option = option.substring(1);
    // }
    //
    // if (option.indexOf(':') > -1) {
    // String[] pair = option.split("\\:");
    // this.options.put(pair[0], pair[1]);
    // }
    // else {
    // this.options.put(option, "true");
    // }
    // }
    //
    // /**
    // * @param option
    // * @return outcome
    // */
    // public boolean isOptionSet(String option) {
    // return this.options.containsKey(option);
    // }

    /**
     * @param options
     * @return value
     */
    public List<String> getOptions() {
        return this.options;
    }

    /**
     * @param option
     */
    public void addOption(String option) {
        this.options.add(option);
    }

    /**
     * @return the commandName
     */
    public final String getCommandName() {
        return arguments.get(0);
    }

    /**
     * @return has command
     */
    public boolean hasCommand() {

        if (arguments.size() > 0) {
            return true;
        }

        return false;
    }

    // /**
    // * @return options
    // */
    // public SortedMap<String, String> getOptions() {
    // return options;
    // }

    /**
     * @param argument
     */
    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    /**
     * @return the arguments
     */
    public final List<String> getArguments() {
        if (arguments.size() > 1) {
            return arguments.subList(1, arguments.size() - 1);
        }
        return Collections.emptyList();
    }

}
