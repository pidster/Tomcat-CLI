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
@Options({
        @Option(name = "verbose", single = 'v', description = "Enable verbose output"),
        @Option(name = "interactive", single = 'r', description = "Enable interactive prompt")
})
public abstract class AbstractCommand implements Command {

    private CommandConfig config;

    /**
     * 
     */
    protected AbstractCommand() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.Command#init(org.pidster.tomcat.util.cli.
     * EnvironmentImpl)
     */
    @Override
    public void configure(CommandConfig config) throws CommandException {
        this.config = config;
        configure();
    }

    /**
     * 
     */
    protected void configure() throws CommandException {

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
     * @see org.pidster.tomcat.util.cli.Command#getConfig()
     */
    @Override
    public final CommandConfig getConfig() {
        return config;
    }

    /*
     * 
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Command#log(java.lang.String)
     */
    @Override
    public final void log(String message) {
        getConfig().getEnvironment().sysout(message, new Object[] {});
    }

    /**
     * 
     */
    protected final void log(String message, Object... args) {
        getConfig().getEnvironment().sysout(message, args);
    }

    /**
     * @return outcome
     */
    protected final boolean isVerbose() {
        return getConfig().isOptionSet("verbose");
    }

}
