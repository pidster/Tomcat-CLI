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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.Descriptor;
import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;
import org.pidster.tomcat.util.cli.Usage;

/**
 * @author pidster
 * 
 */
@Usage(syntax = "<options>", description = "Determine server status")
@Descriptor(name = "status")
@Options({
        @Option(name = "engine", single = 'e', setter = true, value = "*", description = "Selects a specific Engine"),
        @Option(name = "hostname", single = 'n', setter = true, value = "*", description = "Selects a specific Host"),
        @Option(name = "webapp", single = 'a', setter = true, description = "Selects a specific application context"),
        @Option(name = "webapps", single = 'w', description = "Show webapps info"),
        @Option(name = "connectors", single = 'c', description = "Show connector info"),
        @Option(name = "threads", single = 't', description = "Show thread info"),
        @Option(name = "stats", single = 's', description = "Show stats")
})
public class StatusCommand extends AbstractJMXCommand {

    private static final String[] WEBAPP_STATES = new String[] {
            "stopped", "started"
    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pidster.tomcat.util.cli.AbstractCommand#execute(org.apache.tomcat.
     * util.cli .Environment)
     */
    @Override
    public void execute() throws CommandException {

        try {
            ObjectName server = queryNames("*:type=Server").first();

            StringBuilder s = new StringBuilder();

            s.append("Server: ");
            s.append(server.getDomain());

            if (isVerbose()) {
                s.append("[");
                s.append(getAttribute(server, "shutdown"));
                s.append(">");
                s.append(getAttribute(server, "port"));
                s.append("]");
            }

            s.append("");
            s.append(services());

            log(s.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param name
     * @param attribute
     * @return
     * @throws RuntimeException
     */
    private Object getAttribute(ObjectName name, String attribute)
            throws RuntimeException {

        try {
            return getConnection().getAttribute(name, attribute);
        }
        catch (AttributeNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (InstanceNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (MBeanException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (ReflectionException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    /**
     * @param query
     * @return
     */
    private SortedSet<ObjectName> queryNames(String query) {

        try {
            ObjectName name = ObjectName.getInstance(query);
            return new TreeSet<ObjectName>(getConnection().queryNames(name,
                    null));
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (NullPointerException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

    }

    /**
     * @return services
     * @throws JMXException
     */
    private String services() throws RuntimeException {

        StringBuilder s = new StringBuilder();
        SortedSet<ObjectName> names = queryNames("*:type=Service,*");

        if (names.size() == 0) {
            s.append("\n");
            s.append(names.size());
            s.append(" services found.");
        }

        for (ObjectName service : names) {
            s.append(String.format("\nService:%1s",
                    getAttribute(service, "name")));

            s.append(engines(service));
        }

        return s.toString();
    }

    /**
     * @param service
     * @return str
     * @throws JMXException
     */
    private String engines(ObjectName service) throws RuntimeException {

        StringBuilder s = new StringBuilder();

        SortedSet<ObjectName> engines = queryNames(service.getDomain()
                + ":type=Engine");

        for (ObjectName engine : engines) {

            String engineName = (String) getAttribute(engine, "name");

            s.append(String.format("\n Engine:%1s", engineName));

            s.append(" [");
            s.append("defaultHost=");
            s.append(getAttribute(engine, "defaultHost"));

            Object jvmRoute = getAttribute(engine, "jvmRoute");
            if (jvmRoute != null) {
                s.append(", jvmRoute=");
                s.append(jvmRoute);
            }
            if (super.getConfig().isOptionSet("verbose")) {
                s.append(", baseDir=");
                s.append(getAttribute(engine, "baseDir"));

            }
            s.append("]");
            s.append("");

            if (super.getConfig().isOptionSet("connectors")) {
                s.append(connectors(engineName, engine));
            }

            s.append(hosts(engineName, engine));

        }

        return s.toString();
    }

    /**
     * @param engineName
     * @param engine
     * @return engine
     * @throws JMXException
     */
    private String connectors(String engineName, ObjectName engine)
            throws RuntimeException {
        StringBuilder s = new StringBuilder();

        SortedSet<ObjectName> executors = queryNames(engineName
                + ":type=Executor,*");

        if (executors.size() > 0) {
            s.append(executors(executors));
        }

        SortedSet<ObjectName> connectors = queryNames(engineName
                + ":type=Connector,*");

        for (ObjectName connector : connectors) {
            String address = (String) getAttribute(connector, "address");
            Integer port = (Integer) getAttribute(connector, "port");

            SortedSet<ObjectName> protocolHandlers = queryNames(engineName
                    + ":type=ProtocolHandler,port=" + port);
            ObjectName protocolHandler = protocolHandlers.first();
            String name = (String) getAttribute(protocolHandler, "name");

            s.append(String.format("\n  Connector:%-8s [",
                    getAttribute(connector, "protocol")));

            if (address == null) {
                address = "0.0.0.0";
            }

            s.append(String.format("%s::%s:%s, secure=%s, redirect=%s]",
                    getAttribute(connector, "scheme"), address, port,
                    getAttribute(connector, "secure"),
                    getAttribute(connector, "redirectPort")));

            if (super.getConfig().isOptionSet("threads")) {
                SortedSet<ObjectName> threadPools = queryNames(engineName
                        + ":type=ThreadPool,name=" + name);
                s.append(threads(threadPools));
            }
        }

        return s.toString();
    }

    /**
     * @param executors
     * @return content
     */
    private String executors(SortedSet<ObjectName> executors) {
        StringBuilder s = new StringBuilder();

        if (executors.size() > 0) {

            for (ObjectName executor : executors) {

                String name = (String) getAttribute(executor, "name");
                int activeCount = (Integer) getAttribute(executor,
                        "activeCount");
                int maxThreads = (Integer) getAttribute(executor, "maxThreads");
                int minSpareThreads = (Integer) getAttribute(executor,
                        "minSpareThreads");

                int queueSize = (Integer) getAttribute(executor, "queueSize");
                int poolSize = (Integer) getAttribute(executor, "poolSize");
                int corePoolSize = (Integer) getAttribute(executor,
                        "corePoolSize");
                int largestPoolSize = (Integer) getAttribute(executor,
                        "largestPoolSize");

                s.append("\n  Executor: ");
                s.append(name);
                s.append(String
                        .format(" [active:%d, max:%d, spare:%d; queue:%d, pool:%d, core:%d, largest:%d]",
                                activeCount, maxThreads, minSpareThreads,
                                queueSize, poolSize, corePoolSize,
                                largestPoolSize));
            }
        }

        return s.toString();
    }

    /**
     * @param threadPools
     * @return thread info
     */
    private String threads(SortedSet<ObjectName> threadPools) {

        StringBuilder s = new StringBuilder();

        if (threadPools.size() >= 1) {
            ObjectName threadPool = threadPools.first();

            s.append(String.format("\n   - threads=%s/%s, busy=%s, backlog=%s",
                    getAttribute(threadPool, "currentThreadCount"),
                    getAttribute(threadPool, "maxThreads"),
                    getAttribute(threadPool, "currentThreadsBusy"),
                    // getAttribute(threadPool, "minSpareThreads"),
                    // getAttribute(threadPool, "maxKeepAliveRequests"),
                    getAttribute(threadPool, "backlog")));

        }

        /*
         * String query = engine + ":type=RequestProcessor,worker=" + connector
         * + ",name=HttpRequest1";
         * 
         * requestProcessingTime: 242 bytesSent: 194516 protocol: HTTP/1.1
         * processingTime: 453 currentQueryString: qry=*:type=*,* errorCount: 0
         * maxTime: 214 requestBytesReceived: 0 stage: 3
         * lastRequestProcessingTime: 2 virtualHost: localhost serverPort: 8080
         * bytesReceived: 0 currentUri: /manager/jmxproxy workerThreadName:
         * diagnostic-exec-1 method: GET requestCount: 12 requestBytesSent:
         * 40960 contentLength: -1 remoteAddr: 127.0.0.1
         */

        return s.toString();

    }

    /**
     * @param engineName
     * @param engine
     * @return hosts
     * @throws JMXException
     */
    private String hosts(String engineName, ObjectName engine)
            throws RuntimeException {
        StringBuilder s = new StringBuilder();

        SortedSet<ObjectName> hosts = queryNames(engineName
                + ":type=Host,host=*");

        for (ObjectName host : hosts) {

            String hostname = (String) getAttribute(host, "name");

            if (getConfig().isOptionSet("hostname")) {
                if (!getConfig().getOptionValue("hostname").equals(hostname))
                    continue;
            }

            s.append("\n  Host:");
            s.append(hostname);

            String[] aliases = (String[]) getAttribute(host, "aliases");
            ObjectName[] webapps = (ObjectName[]) getAttribute(host, "children");

            if (super.getConfig().isOptionSet("verbose")) {
                s.append("\n   Aliases[");
                boolean first = true;
                for (String alias : aliases) {
                    if (!first)
                        s.append(",");
                    s.append(alias);
                    first = false;
                }
                s.append("]");
            }

            if ((getConfig().isOptionSet("webapps") || getConfig().isOptionSet(
                    "webapp"))
                    && (webapps.length > 0)) {
                s.append(webapps(engineName, hostname, webapps));
                if (hosts.size() > 1) {
                    s.append("\n");
                }
            }
            else {
                if (getConfig().isOptionSet("verbose")) {
                    s.append("\n   ");
                }
                else {
                    s.append(" - ");
                }
                s.append(webapps.length);
                s.append(" applications");
            }

        }

        return s.toString();

    }

    /**
     * @param webapps
     * @return
     */
    private String webapps(String engineName, String hostname,
            ObjectName[] webapps) {

        StringBuilder s = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        s.append("\n   Application      path              state    sessions startup tldscan             started");
        s.append("\n   ----------------------------------------------------------------------------------------");

        for (ObjectName webapp : webapps) {

            // Hacks to get around v6.0 to v7.0 transition
            Object stateObj = getAttribute(webapp, "state");

            String appState = "unknown";
            if (stateObj instanceof Integer) {
                appState = WEBAPP_STATES[(Integer) stateObj];
            }
            else if (stateObj.getClass().isEnum()) {
                appState = ((Enum<?>) stateObj).name().toLowerCase();
            }

            if ("stopped".equalsIgnoreCase(appState)) {
                appState = appState + "!";
            }

            String path = "";
            String started = "";
            String docBase = (String) getAttribute(webapp, "docBase");

            if (getConfig().isOptionSet("webapp")) {
                if (!getConfig().getOptionValue("webapp").equals(docBase))
                    continue;
            }

            s.append("\n   ");

            // ------------------------------------------------------

            String activeSessions = "";
            String startupTime = "";
            String tldScanTime = "";

            if ("STARTED".equalsIgnoreCase(appState)) {
                appState = "ok";
                path = (String) getAttribute(webapp, "path");
                Long startTime = (Long) getAttribute(webapp, "startTime");
                started = sdf.format(new Date(startTime));

                if (path.isEmpty())
                    path = "/";

                String query = engineName + ":type=Manager,path=" + path
                        + ",host=" + hostname;

                SortedSet<ObjectName> managers = queryNames(query);
                ObjectName manager = managers.first();

                activeSessions = String.valueOf(getAttribute(manager,
                        "activeSessions"));
                startupTime = String
                        .valueOf(getAttribute(webapp, "startupTime")) + "ms";
                tldScanTime = String
                        .valueOf(getAttribute(webapp, "tldScanTime")) + "ms";
            }

            s.append(String.format("%-16s %-17s %-8s %-8s %-7s %-6s %20s",
                    docBase, path, appState, activeSessions, startupTime,
                    tldScanTime, started));

        }

        return s.toString();
    }

}
