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

package org.apache.tomcat.util.cli.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.AttributeValueExp;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.StringValueExp;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import sun.management.ConnectorAddressLink;

/**
 * @author SWilliams
 * 
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            int pid = 41749;
            String url = ConnectorAddressLink.importFrom(pid);

            System.out.println("url: " + url);

            JMXServiceURL serviceURL = new JMXServiceURL(url);

            Map<String, Object> environment = new HashMap<String, Object>();

            // String username = "";
            // String password = "";
            // String[] pair = new String[] { username, password };
            // environment.put(JMXConnector.CREDENTIALS, pair);

            JMXConnector connector = JMXConnectorFactory.connect(serviceURL,
                    environment);

            // NotificationListener listener = null;
            // NotificationFilter filter = null;
            // Object handback = null;
            //
            // connector.addConnectionNotificationListener(listener, filter,
            // handback);

            String id = connector.getConnectionId();

            MBeanServerConnection connection = connector
                    .getMBeanServerConnection();

            String objectName = "java.lang:type=MemoryPool,*";
            String attribute = "Name";
            String value = "CMS Old Gen";

            ObjectName name = new ObjectName(objectName);
            AttributeValueExp attrValueExp = Query.attr(attribute);
            StringValueExp stringValueExp = Query.value(value);

            Query q = new Query();

            QueryExp query = Query.match(attrValueExp, stringValueExp);

            SortedSet<ObjectName> names = new TreeSet<ObjectName>(
                    connection.queryNames(name, query));

            for (ObjectName on : names) {
                System.out.println(on);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // try {
        // String protocol = "service:jmx:rmi:///jndi/rmi://";
        // String host = "localhost";
        // int port = 1099;
        // String path = "/jmxrmi";
        //
        // JMXServiceURL url = new JMXServiceURL(protocol, host, port, path);
        //
        // } catch (MalformedURLException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }
}
