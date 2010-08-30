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

import java.io.Console;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author pidster
 * 
 */
public class Environment {

    private final SortedMap<String, String> options;

    private String prompt;

    private final Console console;

    /**
     * 
     */
    public Environment() {
        super();
        this.console = System.console();
        this.prompt = "> ";
        this.options = new TreeMap<String, String>();
    }

    /**
     * @return command line
     */
    public String[] readPrompt() {
        String line = console.readLine(prompt);

        line = line.replaceAll("[\\s\\ ]+", " ");

        if (line.isEmpty()) {
            return new String[0];
        }

        return line.split(" ");
    }

    /**
     * @param fmt
     * @param args
     */
    public void flush() {
        console.flush();
    }

    /**
     * @param fmt
     * @param args
     */
    public void sysout(String fmt, Object... args) {
        console.format(fmt, args);
    }

    /**
     * @param throwable
     */
    public void sysout(Throwable throwable) {
        throwable.printStackTrace(System.out);
    }

    /**
     * @param option
     */
    public void setOption(String option) {

        if (option.startsWith("--")) {
            option = option.substring(2);
        }

        if (option.indexOf(':') > -1) {
            String[] pair = option.split("\\:");
            this.options.put(pair[0], pair[1]);
        }
        else {
            this.options.put(option, "true");
        }
    }

    /**
     * @param option
     * @return outcome
     */
    public boolean isOptionSet(String option) {
        return this.options.containsKey(option);
    }

    /**
     * @param option
     * @return value
     */
    public String getOption(String option) {
        return this.options.get(option);
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

}
