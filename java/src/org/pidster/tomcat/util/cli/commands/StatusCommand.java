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

import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "<options>", description = "Determine server status")
@Descriptor(name = "status")
@Options({
        @Option(name = "engine", single = 'e', setter = true, description = "Selects a specific Engine"),
        @Option(name = "host", single = 'h', setter = true, description = "Selects a specific Host"),
        @Option(name = "webapp", single = 'a', setter = true, description = "Selects a specific application context"),
        @Option(name = "threads", single = 't', description = "Show thread info"),
        @Option(name = "connectors", single = 'c', description = "Show connector info"),
        @Option(name = "webapps", single = 'w', description = "Show webapps info"),
        @Option(name = "stats", single = 's', description = "Show stats")
})
public class StatusCommand extends AbstractJMXCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.apache.tomcat.
     * util.cli .Environment)
     */
    @Override
    public void execute() {
        log("Execute.internal: %s\n", this.getClass().getName());
    }

}
