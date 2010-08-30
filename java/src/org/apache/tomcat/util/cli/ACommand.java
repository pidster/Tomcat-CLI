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

package org.apache.tomcat.util.cli;

/**
 * @author pidster
 * 
 */
public abstract class ACommand implements Command {

    private Environment environment;

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
     * org.apache.tomcat.util.cli.Command#init(org.apache.tomcat.util.cli.CommandLine)
     */
    @Override
    public void init(CommandLine commandLine) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tomcat.util.cli.Command#getEnvironment()
     */
    @Override
    public Environment getEnvironment() {
        return environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tomcat.util.cli.Command#init(org.apache.tomcat.util.cli.
     * Environment)
     */
    @Override
    public void init(Environment environment) {
        this.environment = environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tomcat.util.cli.Command#execute()
     */
    @Override
    public abstract void execute();

}
