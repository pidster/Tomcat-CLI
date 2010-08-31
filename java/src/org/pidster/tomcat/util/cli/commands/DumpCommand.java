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

import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pidster.tomcat.util.cli.AbstractCommand;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Used for debugging. Dumps environment and CLI state information.")
@Descriptor(name = "dump")
public class DumpCommand extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.pidster.tomcat.util.cli
     * .Environment)
     */
    @Override
    public void execute() {

        StringBuilder s = new StringBuilder();

        s.append("---------------------------------------------------------------- \n");

        Properties properties = System.getProperties();
        s.append("\nDumping System environment variables...\n");

        SortedSet<String> names = new TreeSet<String>(
                properties.stringPropertyNames());

        for (String name : names) {
            s.append("\t");
            s.append(name);
            s.append("=");
            s.append(properties.getProperty(name));
            s.append("\n");
        }

        s.append("---------------------------------------------------------------- \n");

        Map<String, String> environment = System.getenv();

        SortedSet<String> envNames = new TreeSet<String>(environment.keySet());

        for (String name : envNames) {
            s.append("\t");
            s.append(name);
            s.append("=");
            s.append(environment.get(name));
            s.append("\n");
        }

        System.out.println(s.toString());
    }

}
