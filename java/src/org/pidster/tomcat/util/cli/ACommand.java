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
import java.util.SortedMap;

/**
 * @author pidster
 * 
 */
public abstract class ACommand implements Command {

    private Environment environment;
    private SortedMap<String, String> options;
    private List<String> arguments;

    /**
     * 
     */
    public ACommand() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.Command#init(org.pidster.tomcat.util.cli.
     * Environment)
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Command#cleanup()
     */
    @Override
    public void cleanup() {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Command#execute(java.util.SortedMap,
     * java.util.List)
     */
    @Override
    public void execute(SortedMap<String, String> options,
            List<String> arguments) {

        this.options = options;
        this.arguments = arguments;

        this.execute();
    }

    /**
     * @return environment
     */
    protected final Environment getEnvironment() {
        return environment;
    }

    /**
     * @return the options
     */
    protected final SortedMap<String, String> getOptions() {
        return options;
    }

    /**
     * @return the arguments
     */
    protected final List<String> getArguments() {
        return arguments;
    }

    /**
     * Execute command
     */
    protected abstract void execute();

}
