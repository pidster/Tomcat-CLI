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

import java.util.TreeSet;

import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Used for debugging. Dumps CLI state information.")
@Descriptor(name = "dumpstate")
public class DumpStateCommand extends AbstractJMXCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.pidster.tomcat
     * .util.cli .Environment)
     */
    @Override
    public void execute() {

        StringBuilder s = new StringBuilder();

        s.append("- DUMP STARTS --------------------------------------------------------------- \n");
        s.append("Dumping options...\n");

        TreeSet<Option> options = new TreeSet<Option>(getConfig().getOptions());

        for (Option option : options) {
            s.append("\t");
            s.append(option.name());
            s.append("=");
            s.append(getConfig().getOptionValue(option.name()));
            s.append("\n");
        }

        s.append("- DUMP ENDS ----------------------------------------------------------------- \n");

        System.out.println(s.toString());
    }

}
