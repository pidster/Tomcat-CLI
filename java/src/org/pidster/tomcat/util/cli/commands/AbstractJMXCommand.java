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

package org.pidster.tomcat.util.cli.commands;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.util.DateTime;
import org.pidster.tomcat.util.cli.util.IO;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import sun.management.ConnectorAddressLink;

/**
 * @author pidster
 * 
 */
@Options({
        @Option(name = "pid", single = 'i', setter = true, description = "The PID to attach to"),
        @Option(name = "jmx", single = 'u', setter = true, description = "The JMX URL to connect to"),
        @Option(name = "port", single = 'p', setter = true, description = "The JMX port to connect to"),
        @Option(name = "username", single = 'U', setter = true, description = "The JMX username to use"),
        @Option(name = "password", single = 'P', setter = true, description = "The JMX password credential to use"),
        @Option(name = "guess", single = 'g', setter = false, description = "Guess which process to attach to"),
        @Option(name = "inject", single = 'j', setter = true, description = "Inject the management agent into a running PID") })
public abstract class AbstractJMXCommand extends AbstractCommand {

    /**
     * @author pidster
     * 
     */
    private final class SimpleComparator implements Comparator<ObjectName> {
        @Override
        public int compare(ObjectName o1, ObjectName o2) {
            return o1.compareTo(o2);
        }
    }

    protected static final String CATALINA_BOOTSTRAP = "org.apache.catalina.startup.Bootstrap";

    protected static final String DEFAULT_JMX_PROTOCOL = "service:jmx:rmi:///jndi/rmi://";

    protected static final String DEFAULT_JMX_PORT = "1099";

    protected static final String DEFAULT_JMX_HOST = "127.0.0.1";

    protected static final String DEFAULT_JMX_URI = "/jmxrmi";

    protected static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    private volatile static JMXConnector connector = null;

    private volatile static boolean persistentConnection = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() throws CommandException {

        try {

            if (getConfig().isOptionSet("interactive")) {
                persistentConnection = true;
            }

            // Not exactly thread safe, but it'll do for now
            if (connector == null) {
                String serviceURL = serviceURL();

                Map<String, Object> properties = connectorProperties();

                JMXServiceURL jmxURL = new JMXServiceURL(serviceURL);
                connector = JMXConnectorFactory.newJMXConnector(jmxURL, properties);

                connector.connect();
            }

            // ------------------------------------------------------------
            // There should only ever be one Server, acquire server attributes
            ObjectName query = ObjectName.getInstance("*:type=Server");

            TreeSet<ObjectName> servers = new TreeSet<ObjectName>(getConnection().queryNames(query, null));

            String serverInfo = (String) attribute(servers.first(), "serverInfo");

            // ------------------------------------------------------------
            // Use the MXBean, it's nicer
            RuntimeMXBean runtime = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);

            log(String.format("Connected: %s [uptime:%s]\n", serverInfo, DateTime.formatUptime(runtime.getUptime())));

            // ------------------------------------------------------------
            // ----
            OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(getConnection(),
                    ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            if (isVerbose())
                log("   System: %s load: %s [%s %s (%s) x%s]", runtime.getName(), os.getSystemLoadAverage(),
                        os.getName(), os.getVersion(), os.getArch(), os.getAvailableProcessors());

            if (isDebug())
                log(String.format(" JVM info: %s %s %s", runtime.getVmName(), runtime.getVmVendor(),
                        runtime.getVmVersion()));
        }
        catch (IOException ioe) {
            throwException(ioe);
        }
        catch (MalformedObjectNameException mone) {
            throwException(mone);
        }
        catch (NullPointerException npe) {
            throwException(npe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        if (!persistentConnection) {
            if (isDebug()) {
                try {
                    log("Closing connection " + connector.getConnectionId());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            IO.close(connector);
            // connector = null;
        }

        super.cleanup();
    }

    /**
     * @return connection
     * @throws IOException
     */
    protected MBeanServerConnection getConnection() throws IOException {
        return connector.getMBeanServerConnection();
    }

    /**
     * @param on
     * @param qe
     * @return
     * @throws IOException
     */
    protected List<ObjectName> query(String name) throws IOException {
        return query(name, null);
    }

    /**
     * @param on
     * @param qe
     * @return
     * @throws IOException
     */
    protected List<ObjectName> query(String name, QueryExp qe) throws IOException {

        try {
            return query(ObjectName.getInstance(name), qe, null);
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param on
     * @param qe
     * @return
     * @throws IOException
     */
    protected List<ObjectName> query(String name, QueryExp qe, Comparator<ObjectName> comparator) throws IOException {

        try {
            return query(ObjectName.getInstance(name), qe, comparator);
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param on
     * @param qe
     * @return
     * @throws IOException
     */
    protected List<ObjectName> query(ObjectName on, QueryExp qe, Comparator<ObjectName> comparator) throws IOException {

        if (comparator == null) {
            comparator = new SimpleComparator();
        }

        // TODO consider using an inline, blocking Future in order to specify a
        // timeout
        List<ObjectName> names = new ArrayList<ObjectName>(getConnection().queryNames(on, qe));

        Collections.sort(names, comparator);

        return names;
    }

    /**
     * @param obj
     * @param attribute
     * @return obj
     */
    @SuppressWarnings("unchecked")
    protected <T> T attribute(ObjectName name, String attribute) throws RuntimeException {

        try {
            // TODO consider using an inline, blocking Future in order to
            // specify a timeout

            // Ooh a bit of cheeky generic casting!
            return (T) getConnection().getAttribute(name, attribute);
        }
        catch (Exception e) {
            quietException(e);
        }
        return null;
    }

    /**
     * @param name
     * @param operationName
     * @param params
     * @param signature
     * @return
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName,
     * java.lang.String, java.lang.Object[], java.lang.String[])
     */
    protected Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        // TODO consider using an inline, blocking Future in order to
        // specify a timeout

        return getConnection().invoke(name, operationName, params, signature);
    }

    /**
     * 
     * @param message
     * @param exception
     * @throws CommandException
     */
    private void throwException(Exception exception) throws CommandException {
        if (isDebug())
            exception.printStackTrace();

        throw new CommandException(exception);
    }

    /**
     * 
     * @param message
     * @param exception
     * @throws CommandException
     */
    private void quietException(Exception exception) {
        if (isDebug())
            exception.printStackTrace();

        // TODO remove this?
        log("ERROR: " + exception.getMessage());
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
                password = Arrays.toString(getConfig().getEnvironment().readPrompt("Please enter the JMX password: "));
            }

            String[] pair = new String[] { getConfig().getOptionValue("username"), password };
            environment.put(JMXConnector.CREDENTIALS, pair);
        }

        if (getConfig().isOptionSet("secure")) {
            // TODO enable SSL connections here?
        }

        return environment;
    }

    /**
     * @return
     * @throws IOException
     */
    private String serviceURL() throws IOException {
        String serviceURL;

        // use the manually provided service url
        if (getConfig().isOptionSet("url")) {
            serviceURL = getConfig().getOptionValue("url");
        }

        // use the local connector in the given process
        else if (getConfig().isOptionSet("pid")) {
            int pid = Integer.parseInt(getConfig().getOptionValue("pid"));
            serviceURL = ConnectorAddressLink.importFrom(pid);
            if (serviceURL == null)
                throw new RuntimeException("JMX local connector not found in PID: " + pid);
        }

        // inject a process with the management agent
        else if (getConfig().isOptionSet("inject")) {
            String pid = getConfig().getOptionValue("inject");

            try {
                VirtualMachine machine = VirtualMachine.attach(pid);
                String javaHome = machine.getSystemProperties().getProperty("java.home");

                if (machine.getAgentProperties().contains(LOCAL_CONNECTOR_ADDRESS)) {
                    log("WARN: Local management agent already installed...");
                }

                String agent = javaHome + File.separator + "lib" + File.separator + "management-agent.jar";

                machine.loadAgent(agent);
                serviceURL = machine.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS);
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }

        // guess which process to use
        else if (getConfig().isOptionSet("guess")) {
            List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
            List<String> tomcats = new ArrayList<String>();
            for (VirtualMachineDescriptor vmd : descriptors) {
                if (vmd.displayName().startsWith(CATALINA_BOOTSTRAP)) {
                    tomcats.add(vmd.id());
                }
            }

            if (tomcats.size() != 1) {
                throw new RuntimeException("Expected 1 virtual machine, found " + tomcats.size() + " PIDs: " + tomcats);
            }

            try {
                VirtualMachine machine = VirtualMachine.attach(tomcats.get(0));
                serviceURL = machine.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS);
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

        if (isDebug())
            log("Connecting via URL: " + serviceURL);

        return serviceURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {

        IO.close(connector);
        connector = null;

        super.finalize();
    }

}
