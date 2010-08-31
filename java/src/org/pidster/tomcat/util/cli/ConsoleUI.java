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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.pidster.tomcat.util.cli.impl.TerminalImpl;

/**
 * @author pidster
 * 
 */
public class ConsoleUI {

    /**
     * @param arguments
     */
    public static void main(String[] arguments) {

        try {
            if (arguments == null)
                arguments = new String[0];

            modifyClassLoader();

            Terminal terminal = new TerminalImpl();

            // load services
            ServiceLoader<Command> loader = ServiceLoader.load(Command.class);

            terminal.register(loader);

            terminal.process(arguments);

        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws MalformedURLException
     */
    private static void modifyClassLoader() throws MalformedURLException {
        URL[] urls = new URL[0];

        String catalinaHome = System.getenv("CATALINA_HOME");

        if (catalinaHome == null) {
            catalinaHome = System.getProperty("catalina.home");
        }

        if ((catalinaHome != null) && (!catalinaHome.isEmpty())) {
            String libDir = catalinaHome + File.separator + "lib";

            File libs = new File(libDir);
            File[] files = libs.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".jar")) {
                        return true;
                    }
                    return false;
                }
            });

            List<URL> jars = new ArrayList<URL>();
            for (File f : files) {
                jars.add(f.toURI().toURL());
            }

            urls = new URL[jars.size()];
            urls = jars.toArray(urls);
        }

        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        TerminalLoader tl = new TerminalLoader(urls, cl);
        t.setContextClassLoader(tl);
    }

}
