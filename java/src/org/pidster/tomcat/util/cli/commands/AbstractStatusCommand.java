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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.management.ObjectName;

import org.pidster.tomcat.util.cli.CommandException;
import org.pidster.tomcat.util.cli.AbstractJMXCommand;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.Options;

/**
 * @author pidster
 * 
 */
@Options({
        @Option(name = "engine", single = 'E', setter = true, value = "*", description = "Selects a specific Engine"),
        @Option(name = "hostname", single = 'H', setter = true, value = "*", description = "Selects a specific Host")
})
public abstract class AbstractStatusCommand extends AbstractJMXCommand {

    private static final String[] WEBAPP_STATES = new String[] {
            "stopped", "started"
    };

    /**
     * ${@inheritDoc}
     */
    @Override
    public void execute() throws CommandException {

        try {
            ObjectName server = query("*:type=Server").get(0);

            StringBuilder s = new StringBuilder();

            s.append("Server: ");
            s.append(server.getDomain());

            if (isVerbose()) {
                s.append("[");
                s.append(attribute(server, "shutdown"));
                s.append(">");
                s.append(attribute(server, "port"));
                s.append("]");
            }

            s.append(datasources(server));

            s.append(services());

            log(s.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param server
     * @param s
     * @throws IOException
     * @throws RuntimeException
     */
    private String datasources(ObjectName server) throws IOException,
            RuntimeException {

        StringBuilder s = new StringBuilder();

        List<ObjectName> globalResources = query(server.getDomain()
                + ":type=Resource,resourcetype=Global,*");

        if (getConfig().isOptionSet("datasources")
                && globalResources.size() > 0) {
            s.append("\nGlobal Resources:");
            for (ObjectName global : globalResources) {

                String name = attribute(global, "name");
                String type = attribute(global, "type");

                String dsq = server.getDomain() + ":type=DataSource,name=\""
                        + name + "\",class=" + type;

                List<ObjectName> dataSources = query(dsq, null);
                for (ObjectName ds : dataSources) {
                    s.append(String
                            .format("\n %-25s[init:%s, now:%s, mxA:%s, idl:%s, mnI:%s, mxI:%s]",
                                    name, attribute(ds, "initialSize"), 0,
                                    attribute(ds, "numActive"),
                                    attribute(ds, "maxActive"),
                                    attribute(ds, "numIdle"),
                                    attribute(ds, "minIdle"),
                                    attribute(ds, "maxIdle")));
                    if (isVerbose()) {
                        s.append("\n - ");
                        s.append(attribute(ds, "url"));
                    }
                }
            }
        }

        return s.toString();
    }

    /**
     * @return services
     * @throws IOException
     * @throws JMXException
     */
    private String services() throws RuntimeException, IOException {

        StringBuilder s = new StringBuilder();
        List<ObjectName> names = query("*:type=Service,*", null);

        if (names.size() == 0) {
            s.append("\n");
            s.append(names.size());
            s.append(" services found.");
        }

        for (ObjectName service : names) {
            s.append(String.format("\nService:%1s", attribute(service, "name")));

            s.append(engines(service));
        }

        return s.toString();
    }

    /**
     * @param service
     * @return str
     * @throws IOException
     * @throws JMXException
     */
    private String engines(ObjectName service) throws RuntimeException,
            IOException {

        StringBuilder s = new StringBuilder();

        List<ObjectName> engines = query(service.getDomain() + ":type=Engine");

        for (ObjectName engine : engines) {

            String engineName = (String) attribute(engine, "name");

            s.append(String.format("\n Engine:%1s", engineName));

            s.append(" [");
            s.append("defaultHost=");
            s.append(attribute(engine, "defaultHost"));

            Object jvmRoute = attribute(engine, "jvmRoute");
            if (jvmRoute != null) {
                s.append(", jvmRoute=");
                s.append(jvmRoute);
            }
            if (super.getConfig().isOptionSet("verbose")) {
                s.append(", baseDir=");
                s.append(attribute(engine, "baseDir"));

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
     * @throws IOException
     * @throws JMXException
     */
    private String connectors(String engineName, ObjectName engine)
            throws RuntimeException, IOException {
        StringBuilder s = new StringBuilder();

        List<ObjectName> executors = query(engineName + ":type=Executor,*");

        if (executors.size() > 0) {
            s.append(executors(executors));
        }

        List<ObjectName> connectors = query(engineName + ":type=Connector,*");

        for (ObjectName connector : connectors) {
            String address = (String) attribute(connector, "address");
            Integer port = (Integer) attribute(connector, "port");

            List<ObjectName> protocolHandlers = query(engineName
                    + ":type=ProtocolHandler,port=" + port);
            ObjectName protocolHandler = protocolHandlers.get(0);
            String name = (String) attribute(protocolHandler, "name");

            s.append(String.format("\n  Connector:%-8s [",
                    attribute(connector, "protocol")));

            if (address == null) {
                address = "0.0.0.0";
            }

            s.append(String.format("%s::%s:%s", attribute(connector, "scheme"),
                    address, port));

            if (isVerbose())
                s.append(String.format(", secure=%s, redirect=%s",
                        attribute(connector, "secure"),
                        attribute(connector, "redirectPort")));

            s.append("]");

            if (super.getConfig().isOptionSet("threads")) {
                List<ObjectName> threadPools = query(engineName
                        + ":type=ThreadPool,name=" + name);
                s.append(threads(engineName, threadPools));
            }
        }

        return s.toString();
    }

    /**
     * @param executors
     * @return content
     */
    private String executors(List<ObjectName> executors) {
        StringBuilder s = new StringBuilder();

        if (executors.size() > 0) {

            for (ObjectName executor : executors) {

                String name = (String) attribute(executor, "name");
                int activeCount = (Integer) attribute(executor, "activeCount");
                int maxThreads = (Integer) attribute(executor, "maxThreads");
                int minSpareThreads = (Integer) attribute(executor,
                        "minSpareThreads");

                int queueSize = (Integer) attribute(executor, "queueSize");
                int poolSize = (Integer) attribute(executor, "poolSize");
                int corePoolSize = (Integer) attribute(executor, "corePoolSize");
                int largestPoolSize = (Integer) attribute(executor,
                        "largestPoolSize");

                s.append("\n  Executor: ");
                s.append(name);
                s.append(String
                        .format(" [active:%d, pool:%d, init:%d, largest:%d, max:%d, spare:%d; queue:%d]",
                                activeCount, poolSize, corePoolSize,
                                largestPoolSize, maxThreads, minSpareThreads,
                                queueSize));
            }
        }

        return s.toString();
    }

    /**
     * @param threadPools
     * @return thread info
     * @throws IOException
     */
    private String threads(String engine, List<ObjectName> threadPools)
            throws IOException {

        StringBuilder s = new StringBuilder();

        if (threadPools.size() >= 1) {

            for (ObjectName pool : threadPools) {
                String name = attribute(pool, "name");
                s.append("\n  Pool[");
                s.append(name);
                s.append("]");

                String processorQuery = engine
                        + ":type=RequestProcessor,worker=" + name + ",name=*";

                Comparator<ObjectName> sorter = threadSorter();

                List<ObjectName> processors = query(processorQuery, null,
                        sorter);

                // TOOD make maxtime/proctime only under verbose

                if (isVerbose()) {
                    s.append("\n   name ----------- sent ------- recd ----- reqs ---- errors -- maxtime - proctime - maxURI -----------");
                    for (ObjectName rp : processors) {
                        String rpName = rp.toString();
                        rpName = rpName.replaceAll(engine
                                + ":type=RequestProcessor,worker=" + name
                                + ",name=", "");

                        s.append(String
                                .format("\n   %-16s %-12s %-10s %-9s %-9s %-9s %-9s  %s",
                                        rpName, attribute(rp, "bytesSent"),
                                        attribute(rp, "bytesReceived"),
                                        attribute(rp, "requestCount"),
                                        attribute(rp, "errorCount"),
                                        attribute(rp, "maxTime"),
                                        attribute(rp, "processingTime"),
                                        attribute(rp, "maxRequestUri")));
                    }
                }
                else {
                    s.append("\n   name ----------- sent ------- recd ----- reqs ---- errors --- maxURI -----------");
                    for (ObjectName rp : processors) {
                        String rpName = rp.toString();
                        rpName = rpName.replaceAll(engine
                                + ":type=RequestProcessor,worker=" + name
                                + ",name=", "");

                        s.append(String.format(
                                "\n   %-16s %-12s %-10s %-9s %-9s  %s", rpName,
                                attribute(rp, "bytesSent"),
                                attribute(rp, "bytesReceived"),
                                attribute(rp, "requestCount"),
                                attribute(rp, "errorCount"),
                                attribute(rp, "maxRequestUri")));
                    }
                }
                s.append("\n");
            }
        }

        return s.toString();
    }

    /**
     * @return
     */
    private Comparator<ObjectName> threadSorter() {

        final String sort = getConfig().getOptionValue("threads");

        Comparator<ObjectName> sorter = new Comparator<ObjectName>() {
            @Override
            public int compare(ObjectName o1, ObjectName o2) {
                if ("name".equalsIgnoreCase(sort)) {
                    String a1 = attribute(o1, "rpName").toString();
                    String a2 = attribute(o2, "rpName").toString();
                    return a1.compareTo(a2);
                }
                else if ("sent".equalsIgnoreCase(sort)) {
                    Long a1 = attribute(o1, "bytesSent");
                    Long a2 = attribute(o2, "bytesSent");
                    return a1.compareTo(a2);
                }
                else if ("recd".equalsIgnoreCase(sort)) {
                    Long a1 = attribute(o1, "bytesReceived");
                    Long a2 = attribute(o2, "bytesReceived");
                    return a1.compareTo(a2);
                }
                else if ("reqs".equalsIgnoreCase(sort)) {
                    Integer a1 = attribute(o1, "requestCount");
                    Integer a2 = attribute(o2, "requestCount");
                    return a1.compareTo(a2);
                }
                else if ("errors".equalsIgnoreCase(sort)) {
                    Integer a1 = attribute(o1, "errorCount");
                    Integer a2 = attribute(o2, "errorCount");
                    return a1.compareTo(a2);
                }
                else if ("maxtime".equalsIgnoreCase(sort)) {
                    Long a1 = attribute(o1, "maxTime");
                    Long a2 = attribute(o2, "maxTime");
                    return a1.compareTo(a2);
                }
                else if ("proctime".equalsIgnoreCase(sort)) {
                    Long a1 = attribute(o1, "processingTime");
                    Long a2 = attribute(o2, "processingTime");
                    return a1.compareTo(a2);
                }
                else if ("maxURI".equalsIgnoreCase(sort)) {
                    String a1 = attribute(o1, "maxRequestUri");
                    String a2 = attribute(o2, "maxRequestUri");
                    return a1.compareTo(a2);
                }
                else {
                    return o1.compareTo(o2);
                }
            }
        };
        return sorter;
    }

    /**
     * @param engineName
     * @param engine
     * @return hosts
     * @throws IOException
     * @throws JMXException
     */
    private String hosts(String engineName, ObjectName engine)
            throws RuntimeException, IOException {
        StringBuilder s = new StringBuilder();

        List<ObjectName> hosts = query(engineName + ":type=Host,host="
                + getConfig().getOptionValue("hostname"));

        for (ObjectName host : hosts) {

            String hostname = (String) attribute(host, "name");

            s.append("\n  Host:");
            s.append(hostname);

            String[] aliases = (String[]) attribute(host, "aliases");
            ObjectName[] webapps = (ObjectName[]) attribute(host, "children");

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
     * @throws IOException
     */
    private String webapps(String engineName, String hostname,
            ObjectName[] webapps) throws IOException {

        StringBuilder s = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (isVerbose()) {
            s.append("\n   application ---- path ------------ state sessions - total ---- proctime init -- tldscan ----------- started");
        }
        else {
            s.append("\n   application ---- path ------------ state sessions - startup ------------ started");
        }

        for (ObjectName webapp : webapps) {

            // Hacks to get around v6.0 to v7.0 transition
            Object stateObj = attribute(webapp, "state");

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
            String docBase = (String) attribute(webapp, "docBase");

            if (getConfig().isOptionSet("webapp")) {
                if (!getConfig().getOptionValue("webapp").equals(docBase))
                    continue;
            }

            s.append("\n   ");

            // ------------------------------------------------------

            String activeSessions = "";
            String totalSessions = "";
            String processingTime = "";
            String initTime = "";
            String startupTime = "";
            String tldScanTime = "";

            if ("STARTED".equalsIgnoreCase(appState)) {
                appState = "ok";
                path = (String) attribute(webapp, "path");
                Long startTime = (Long) attribute(webapp, "startTime");
                started = sdf.format(new Date(startTime));

                if (path.isEmpty())
                    path = "/";

                String query = engineName + ":type=Manager,path=" + path
                        + ",host=" + hostname;

                List<ObjectName> managers = query(query);
                ObjectName manager = managers.get(0);

                activeSessions = String.valueOf(attribute(manager,
                        "activeSessions"));
                totalSessions = String.valueOf(attribute(manager,
                        "sessionCounter"));

                processingTime = String.valueOf(attribute(manager,
                        "processingTime"));

                startupTime = attribute(webapp, "startupTime") + "ms";
                tldScanTime = attribute(webapp, "tldScanTime") + "ms";
                initTime = ((Long) attribute(webapp, "startupTime") + (Long) attribute(
                        webapp, "tldScanTime")) + "ms";

            }

            if (isVerbose()) {
                s.append(String.format(
                        "%-16s %-17s %-5s %-10s %-10s %-8s %-7s %-6s %20s",
                        docBase, path, appState, activeSessions, totalSessions,
                        processingTime, startupTime, tldScanTime, started));
            }
            else {
                s.append(String.format("%-16s %-17s %-5s %-10s %-7s %20s",
                        docBase, path, appState, activeSessions, initTime,
                        started));
            }

        }

        return s.toString();
    }

}
