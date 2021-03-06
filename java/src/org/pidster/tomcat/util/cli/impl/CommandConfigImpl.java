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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pidster.tomcat.util.cli.CommandConfig;
import org.pidster.tomcat.util.cli.Environment;
import org.pidster.tomcat.util.cli.Option;

/**
 * @author pidster
 * 
 */
public class CommandConfigImpl implements CommandConfig {

    private final Environment environment;
    private final String name;
    private final List<String> arguments;
    private final Map<Option, String> options;

    /**
     * @param environment
     * @param environment
     * @param arguments
     * @param options
     */
    public CommandConfigImpl(Environment environment, String name,
            List<String> arguments, Map<Option, String> options) {
        this.environment = environment;
        this.name = name;
        this.arguments = arguments;
        this.options = options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandConfig#getEnvironment()
     */
    @Override
    public final Environment getEnvironment() {
        return environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandConfig#getName()
     */
    @Override
    public final String getCommandName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandConfig#getArguments()
     */
    @Override
    public final List<String> getArguments() {
        return arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.CommandConfig#getOption(java.lang.String)
     */
    @Override
    public final Option getOption(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return option;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.CommandConfig#isOptionSet(java.lang.String)
     */
    @Override
    public final boolean isOptionSet(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.CommandConfig#getOptionValue(java.lang.String
     * )
     */
    @Override
    public final String getOptionValue(String name) {
        for (Option option : options.keySet()) {
            if (option.name().equals(name)) {
                return options.get(option);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.CommandConfig#getOptions()
     */
    @Override
    public Set<Option> getOptions() {
        return options.keySet();
    }

}
