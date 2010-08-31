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

import org.pidster.tomcat.util.cli.AbstractCommand;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(description = "Used for debugging. Tests JMX environment.")
@Descriptor(name = "testjmx")
public class TestJMXCommand extends AbstractCommand {

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

        s.append("- TEST STARTS --------------------------------------------------------------- \n");

        s.append("");

        s.append("- TEST ENDS ----------------------------------------------------------------- \n");

        log(s.toString());
    }

}
