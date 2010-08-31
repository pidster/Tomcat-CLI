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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.OptionParser;
import org.pidster.tomcat.util.cli.util.StringManager;

/**
 * @author pidster
 * 
 */
public class OptionParserImpl implements OptionParser {

    private static final StringManager manager = StringManager
            .getManager(Constants.PACKAGE_NAME);

    private final Map<Command, List<Option>> commandOptions;

    /**
     * @param options
     */
    public OptionParserImpl(Map<Command, List<Option>> options) {
        this.commandOptions = options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.OptionParser#activeOptions(java.util.List,
     * org.pidster.tomcat.util.cli.Command)
     */
    @Override
    public Map<Option, String> activeOptions(List<String> options,
            Command command) {

        Map<Option, String> activeOptions = new HashMap<Option, String>();
        List<Option> viableOptions = commandOptions.get(command);

        for (Option option : viableOptions) {

            LOOP: for (String argument : options) {

                String ext = "--".concat(option.name());
                String sng = "-" + option.single();

                if (argument.startsWith(ext) || argument.startsWith(sng)) {

                    String value = option.value();

                    if (argument.indexOf(':') > -1) {
                        value = argument.substring(argument.indexOf(':') + 1);
                    }
                    else {
                        value = option.value();
                    }

                    if (option.setter() && value.isEmpty()) {
                        throw new IllegalArgumentException(manager.getString(
                                "tomcat.cli.requiredOptionNotSet",
                                option.name()));
                    }

                    activeOptions.put(option, value);

                    break LOOP;
                }
            }

            // check to see if this option is required
            if (option.required() && !activeOptions.containsKey(option)) {
                throw new IllegalArgumentException("Option "
                        + option.extended() + " is required");
            }
        }
        return activeOptions;
    }

}