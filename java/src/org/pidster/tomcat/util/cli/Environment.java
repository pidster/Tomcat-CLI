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

import java.io.Console;

/**
 * @author pidster
 * 
 */
public class Environment {

    private String prompt;

    private final Console console;

    /**
     * 
     */
    public Environment() {
        super();
        this.console = System.console();
        this.prompt = "> ";
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

}
