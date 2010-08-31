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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import sun.management.ConnectorAddressLink;

/**
 * @author pidster
 * 
 */
@Options({
        @Option(name = "pid", single = 'i', setter = true, description = "The PID to attach to"),
        @Option(name = "jmx", single = 'u', setter = true, description = "The JMX URL to connect to"),
        @Option(name = "username", single = 'u', setter = true, description = "The JMX URL to connect to"),
        @Option(name = "password", single = 'u', setter = true, description = "The JMX URL to connect to")
})
public abstract class AbstractJMXCommand extends AbstractCommand {

    protected static final String DEFAULT_JMX_PORT = "1099";

    protected static final String DEFAULT_JMX_HOST = "127.0.0.1";

    protected static final String DEFAULT_JMX_URI = "/jmxrmi";

    private MBeanServerConnection connection;

    /**
     * 
     */
    protected AbstractJMXCommand() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.AbstractCommand#configure()
     */
    @Override
    public void configure() {

        try {
            String serviceURL = serviceURL();
            Map<String, Object> properties = connectorProperties();

            JMXServiceURL jmxURL = new JMXServiceURL(serviceURL);
            JMXConnector connector = JMXConnectorFactory.newJMXConnector(
                    jmxURL, properties);

            connector.connect();

            this.connection = connector.getMBeanServerConnection();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * @return connection
     */
    protected MBeanServerConnection getConnection() {
        return this.connection;
    }

    /**
     * @return
     */
    private Map<String, Object> connectorProperties() {
        Map<String, Object> environment = new HashMap<String, Object>();

        if (getConfig().isOptionSet("username")) {

            String password;
            if (getConfig().isOptionSet("password")) {
                password = getConfig().getOptionValue("password");
            }
            else {
                password = getConfig().getEnvironment().readPrompt(
                        "JMX Password: ")[0];
            }

            String[] pair = new String[] {
                    getConfig().getOptionValue("username"), password
            };
            environment.put(JMXConnector.CREDENTIALS, pair);
        }
        return environment;
    }

    /**
     * @return
     * @throws IOException
     */
    private String serviceURL() throws IOException {
        String serviceURL;

        if (getConfig().isOptionSet("url")) {
            serviceURL = getConfig().getOptionValue("url");
        }

        else if (getConfig().isOptionSet("pid")) {
            int pid = Integer.parseInt(getConfig().getOptionValue("pid"));
            serviceURL = ConnectorAddressLink.importFrom(pid);
        }

        else {
            String port = DEFAULT_JMX_PORT;
            if (getConfig().isOptionSet("port"))
                getConfig().getOptionValue("port");

            String host = DEFAULT_JMX_HOST;
            if (getConfig().isOptionSet("host"))
                host = getConfig().getOptionValue("host");

            String path = DEFAULT_JMX_URI;

            StringBuilder s = new StringBuilder();
            s.append("service:jmx:rmi:///jndi/rmi://");
            s.append(host);
            s.append(":");
            s.append(port);
            s.append(path);

            serviceURL = s.toString();
        }
        return serviceURL;
    }
}
