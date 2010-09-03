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

import java.util.Comparator;
import java.util.TreeSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Used for debugging. Query JMX environment and CLI state information.")
@Descriptor(name = "query")
@Options({
        @Option(name = "domain", single = 'd', description = "Limit domain"),
        @Option(name = "type", single = 't', description = "Limit type"),
        @Option(name = "query", single = 'q', description = "Raw query ")
})
public class QueryJMXCommand extends AbstractJMXCommand {

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

        try {
            MBeanServerConnection connection = super.getConnection();

            QueryExp query = null;

            ObjectName name = ObjectName.WILDCARD;
            if (getConfig().isOptionSet("query")) {
                name = ObjectName.getInstance(getConfig().getOptionValue(
                        "query"));
            }
            else {

                StringBuilder q = new StringBuilder();

                if (getConfig().isOptionSet("domain")) {
                    q.append(getConfig().getOptionValue("domain"));
                }
                else {
                    q.append("*");
                }
                q.append(":");
                if (getConfig().isOptionSet("type")) {
                    q.append("type=");
                    q.append(getConfig().getOptionValue("type"));
                }
                else {
                    q.append("*");
                }

                name = ObjectName.getInstance(q.toString());
            }

            TreeSet<ObjectName> names = new TreeSet<ObjectName>(
                    new Comparator<ObjectName>() {
                        @Override
                        public int compare(ObjectName one, ObjectName two) {
                            return one.getCanonicalName().compareTo(
                                    two.getCanonicalName());
                        }
                    });

            names.addAll(connection.queryNames(name, query));

            for (ObjectName obj : names) {
                s.append(" ");
                s.append(obj.getCanonicalName());
                s.append("]\n");

                if (isVerbose()) {
                    try {
                        MBeanInfo info = connection.getMBeanInfo(obj);
                        for (MBeanAttributeInfo mbai : info.getAttributes()) {
                            s.append("   - ");
                            s.append(mbai.getName());
                            s.append("=");
                            try {
                                s.append(connection.getAttribute(obj,
                                        mbai.getName()));
                            }
                            catch (Exception e) {
                                s.append("ERROR: " + e.getMessage());
                            }
                            s.append("\n");
                        }
                        s.append("\n");
                    }
                    catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            log("ERROR" + e.getMessage());
        }

        log(s.toString());
    }
}
