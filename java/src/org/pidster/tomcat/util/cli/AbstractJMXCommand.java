/*
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.pidster.tomcat.util.cli.util.DateTime;

import com.sun.tools.attach.VirtualMachine;

import sun.management.ConnectorAddressLink;

/**
 * @author pidster
 * 
 */
@Options({
        @Option(name = "pid", single = 'i', setter = true, description = "The PID to attach to"),
        @Option(name = "jmx", single = 'u', setter = true, description = "The JMX URL to connect to"),
        @Option(name = "port", single = 'p', setter = true, description = "The JMX port to connect to"),
        @Option(name = "username", single = 'u', setter = true, description = "The JMX username to use"),
        @Option(name = "password", single = 'c', setter = true, description = "The JMX password credential to use"),
        @Option(name = "inject", single = 'j', setter = true, description = "Inject the management agent into a running PID")
})
public abstract class AbstractJMXCommand extends AbstractCommand {

    protected static final String DEFAULT_JMX_PROTOCOL = "service:jmx:rmi:///jndi/rmi://";

    protected static final String DEFAULT_JMX_PORT = "1099";

    protected static final String DEFAULT_JMX_HOST = "127.0.0.1";

    protected static final String DEFAULT_JMX_URI = "/jmxrmi";

    protected static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    private volatile static MBeanServerConnection connection;

    private Map<String, Object> runtimeProps;

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
    protected void configure() throws CommandException {

        try {

            // Not exactly thread safe, but it'll do for now
            if (connection == null) {
                String serviceURL = serviceURL();

                Map<String, Object> properties = connectorProperties();

                JMXServiceURL jmxURL = new JMXServiceURL(serviceURL);
                JMXConnector connector = JMXConnectorFactory.newJMXConnector(
                        jmxURL, properties);

                connector.connect();
                connection = connector.getMBeanServerConnection();
            }

            runtimeProps = new HashMap<String, Object>();

            // ------------------------------------------------------------
            // acquire runtime attributes
            ObjectName query = ObjectName.getInstance("java.lang:type=Runtime");

            String[] arr = new String[] {
                    "Name", "Uptime", "StartTime", "VmName", "VmVendor",
                    "VmVersion"
            };

            AttributeList list = connection.getAttributes(query, arr);
            for (Attribute attribute : list.asList()) {
                runtimeProps.put(attribute.getName(), attribute.getValue());
            }

            // ------------------------------------------------------------
            // acquire server attributes
            query = ObjectName.getInstance("*:type=Server");
            arr = new String[] {
                "serverInfo"
            };

            TreeSet<ObjectName> servers = new TreeSet<ObjectName>(
                    connection.queryNames(query, null));

            list = connection.getAttributes(servers.first(), arr);
            for (Attribute attribute : list.asList()) {
                runtimeProps.put(attribute.getName(), attribute.getValue());
            }

            // ------------------------------------------------------------
            // display connection information
            StringBuilder s = new StringBuilder();

            s.append("Connected to ");
            s.append(runtimeProps.get("serverInfo"));
            s.append(" [");
            s.append(runtimeProps.get("Name"));

            Object uptime = runtimeProps.get("Uptime");
            if (uptime != null) {
                s.append(", uptime:");
                s.append(DateTime.formatUptime((Long) uptime));
            }

            s.append("]\n");

            if (isVerbose()) {
                s.append("- ");
                s.append(runtimeProps.get("VmName"));
                s.append(" (");
                s.append(runtimeProps.get("VmVendor"));
                s.append(" ");
                s.append(runtimeProps.get("VmVersion"));
                s.append(")");
                s.append("\n");
            }

            log(s.toString());
        }
        catch (IOException ioe) {
            throw new CommandException("Connection FAILED: " + ioe.getMessage());
        }
        catch (MalformedObjectNameException mone) {
            throw new CommandException("Connection FAILED: "
                    + mone.getMessage());
        }
        catch (NullPointerException npe) {
            throw new CommandException("Connection FAILED: " + npe.getMessage());
        }
        catch (InstanceNotFoundException infe) {
            throw new CommandException(infe.getMessage(), infe.getCause());
        }
        catch (ReflectionException re) {
            throw new CommandException(re.getMessage(), re.getCause());
        }
    }

    /**
     * @return connection
     */
    protected MBeanServerConnection getConnection() {
        return connection;
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
                        "Please enter the JMX password: ")[0];
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
            if (serviceURL == null)
                throw new RuntimeException(
                        "JMX local connector not found in PID: " + pid);
        }

        else if (getConfig().isOptionSet("inject")) {
            String pid = getConfig().getOptionValue("inject");

            try {
                VirtualMachine machine = VirtualMachine.attach(pid);
                String javaHome = machine.getSystemProperties().getProperty(
                        "java.home");

                if (machine.getAgentProperties().contains(
                        LOCAL_CONNECTOR_ADDRESS)) {
                    log("WARN: Local management agent already installed...");
                }

                String agent = javaHome + File.separator + "lib"
                        + File.separator + "management-agent.jar";

                machine.loadAgent(agent);
                serviceURL = machine.getAgentProperties().getProperty(
                        LOCAL_CONNECTOR_ADDRESS);
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }

        else {
            String port = DEFAULT_JMX_PORT;
            if (getConfig().isOptionSet("port"))
                port = getConfig().getOptionValue("port");

            String host = DEFAULT_JMX_HOST;
            if (getConfig().isOptionSet("host"))
                host = getConfig().getOptionValue("host");

            String path = DEFAULT_JMX_URI;

            StringBuilder s = new StringBuilder();
            s.append(DEFAULT_JMX_PROTOCOL);
            s.append(host);
            s.append(":");
            s.append(port);
            s.append(path);

            serviceURL = s.toString();
        }

        if (isVerbose())
            log("Connecting via URL: " + serviceURL);

        return serviceURL;
    }
}
