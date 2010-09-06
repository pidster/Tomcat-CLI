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

import java.io.Console;

import org.pidster.tomcat.util.cli.Environment;

/**
 * @author pidster
 * 
 */
public class EnvironmentImpl implements Environment {

    private static final String DEFAULT_PROMPT = "> ";

    private final Console console;

    private String prompt;

    /**
     * 
     */
    public EnvironmentImpl() {
        super();
        this.console = System.console();
        this.prompt = DEFAULT_PROMPT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Environment#sysout(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void sysout(String fmt, Object... args) {

        try {
            if (!fmt.endsWith("\n")) {
                fmt += "\n";
            }

            if (args != null && args.length > 0) {
                fmt = String.format(fmt, args);
            }

            System.out.print(fmt);
        }
        catch (Exception e) {
            System.out.println("Error: " + fmt);
            // e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Environment#sysout(java.lang.Throwable)
     */
    @Override
    public void sysout(Throwable throwable) {
        throwable.printStackTrace(System.out);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Environment#getPrompt()
     */
    @Override
    public String getPrompt() {
        return prompt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Environment#setPrompt(java.lang.String)
     */
    @Override
    public void setPrompt(String prompt) {

        if (prompt == null || prompt.isEmpty()) {
            prompt = DEFAULT_PROMPT;
        }

        this.prompt = prompt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Environment#readPrompt(java.lang.String)
     */
    @Override
    public String[] readPrompt(String prompt) {

        // long index = count.getAndIncrement();

        String line = console.readLine(prompt);
        line = line.replaceAll("[\\s\\ ]+", " ");

        if (line.isEmpty()) {
            return new String[0];
        }

        return line.split(" ");
    }

    /*
     * 
     */
    String[] readPrompt() {
        return readPrompt(prompt);
    }

}
