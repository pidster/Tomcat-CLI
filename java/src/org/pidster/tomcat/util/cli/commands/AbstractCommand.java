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

package org.pidster.tomcat.util.cli.commands;

import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.CommandConfig;
import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.util.StringManager;

/**
 * @author pidster
 * 
 */
@Options({ @Option(name = "verbose", single = 'V', description = "Enable verbose output"),
        @Option(name = "debug", single = 'D', description = "Enable debugging output"),
        @Option(name = "interactive", single = 'I', description = "Enable interactive prompt") })
public abstract class AbstractCommand implements Command {

    // use a local StringManager instance for all commands
    private final StringManager sm;

    private CommandConfig config;

    /**
     * 
     */
    protected AbstractCommand() {
        super();
        sm = StringManager.getManager(getClass());
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

        if (message.matches("[\\w]+(\\.[\\w]+)+"))
            message = sm.getString(message);

        getConfig().getEnvironment().sysout(message);
    }

    /**
     * 
     */
    protected final void log(String message, Object... args) {

        if (message.matches("[\\w]+(\\.[\\w]+)+"))
            message = sm.getString(message, args);

        getConfig().getEnvironment().sysout(message);
    }

    /**
     * @return outcome
     */
    protected final boolean isVerbose() {
        return getConfig().isOptionSet("verbose");
    }

    /**
     * @return outcome
     */
    protected final boolean isDebug() {
        return getConfig().isOptionSet("debug");
    }

}
