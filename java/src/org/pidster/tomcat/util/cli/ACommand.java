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

/**
 * @author pidster
 * 
 */
public abstract class ACommand implements Command {

    private CommandConfig config;

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
    public void configure(CommandConfig config) {
        this.config = config;
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

    /**
     * @return the config
     */
    protected final CommandConfig getConfig() {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Command#log(java.lang.String)
     */
    @Override
    public void log(String message) {
        getConfig().getEnvironment().sysout(message, new Object[] {});
    }

    /**
     * 
     */
    public void log(String message, Object... args) {
        getConfig().getEnvironment().sysout(message, args);
    }

}