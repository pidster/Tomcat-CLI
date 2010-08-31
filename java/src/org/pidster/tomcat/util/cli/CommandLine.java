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

import java.util.List;

/**
 * @author pidster
 *
 */
public interface CommandLine {

    /**
     * @return the prompt
     */
    public abstract String getPrompt();

    /**
     * @param the
     *            prompt to set
     */
    public abstract void setPrompt(String prompt);

    /**
     * @param options
     * @return value
     */
    public abstract List<String> getOptions();

    /**
     * @return the commandName
     */
    public abstract String getCommandName();

    /**
     * @return has command
     */
    public abstract boolean hasCommand();

    /**
     * @return the arguments
     */
    public abstract List<String> getArguments();

}