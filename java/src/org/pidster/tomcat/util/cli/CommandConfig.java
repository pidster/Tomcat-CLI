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

/**
 * @author pidster
 *
 */
public interface CommandConfig {

    /**
     * @return the environment
     */
    public abstract Environment getEnvironment();

    /**
     * @return the arguments
     */
    public abstract List<String> getArguments();

    /**
     * @param name
     * @return
     */
    public abstract Option getOption(String name);

    /**
     * @param name
     * @return outcome
     */
    public abstract boolean isOptionSet(String name);

    /**
     * @param name
     * @return outcome
     */
    public abstract String getOptionValue(String name);

}