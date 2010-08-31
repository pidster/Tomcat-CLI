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

package org.pidster.tomcat.util.cli.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pidster
 * 
 */
public class Server {

    private String name;

    private int port;

    private final List<Service> services;

    /**
     * 
     */
    public Server() {
        this.services = new ArrayList<Service>();
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @return the port
     */
    public final int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public final void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the services
     */
    public final List<Service> getServices() {
        return services;
    }

}
