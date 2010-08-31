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
import java.util.Map;

/**
 * @author pidster
 * 
 */
public class CommandConfig {

    private final Environment environment;
    private final List<String> arguments;
    private final Map<Option, String> options;

    /**
     * @param environment
     * @param arguments
     * @param options
     */
    public CommandConfig(Environment environment, List<String> arguments,
            Map<Option, String> options) {
        this.environment = environment;
        this.arguments = arguments;
        this.options = options;
    }

    /**
     * @return the environment
     */
    public final Environment getEnvironment() {
        return environment;
    }

    /**
     * @return the arguments
     */
    public final List<String> getArguments() {
        return arguments;
    }

    /**
     * @param name
     * @return
     */
    public final Option getOption(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return option;
            }
        }
        return null;
    }

    /**
     * @param name
     * @return outcome
     */
    public final boolean isOptionSet(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param name
     * @return outcome
     */
    public final String getOptionValue(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return option.value();
            }
        }
        return null;
    }

}
