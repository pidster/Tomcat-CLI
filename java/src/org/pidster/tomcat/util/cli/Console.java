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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.pidster.tomcat.util.cli.util.IO;

/**
 * @author pidster
 * 
 */
public class Console {

    /**
     * @author pidster
     */
    private static final class JarFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            if (!name.endsWith(".jar"))
                return false;

            if (name.matches("catalina(\\-\\w+)?\\.jar"))
                return true;

            if (name.matches("tomcat\\-\\w+?\\.jar"))
                return true;

            if (name.matches("\\w+\\-api.jar"))
                return true;

            return false;
        }
    }

    /**
     * @param arguments
     */
    public static void main(String[] arguments) {
        try {
            if (arguments == null)
                arguments = new String[0];

            URLClassLoader loader = createClassLoader();

            ServiceLoader<ConsoleUI> consoles = ServiceLoader.load(ConsoleUI.class, loader);
            if (!consoles.iterator().hasNext()) {
                // FAIL
                throw new Exception("ConsoleUI.class Implementation not found");
            }

            ConsoleUI consoleUI = consoles.iterator().next();

            // load services
            ServiceLoader<Command> commands = ServiceLoader.load(Command.class, loader);
            consoleUI.register(commands);
            consoleUI.process(arguments);
        }
        catch (Exception e) {
            // If it fails here, a stacktrace is the best option, for now at
            // least
            e.printStackTrace();
        }
    }

    /**
     * Attempts to modify the classloader by auto-discovering and loading
     * tools.jar. If the app is inside a Tomcat environment and can find
     * catalina.home or a similar environment variable, then try to load select
     * jars from that environment too.
     * @return
     * 
     * @throws MalformedURLException
     */
    private static URLClassLoader createClassLoader() throws MalformedURLException {
        List<URL> jars = new ArrayList<URL>();

        // Automatically discover and load tools.jar
        String toolsPath = IO.path(System.getProperty("java.home"), "lib", "tools.jar");
        File tools = new File(toolsPath);
        if (tools.exists()) {
            jars.add(tools.toURI().toURL());
        }

        String catalinaHome = System.getenv("CATALINA_HOME");

        if (catalinaHome == null) {
            catalinaHome = System.getProperty("catalina.home");
        }

        if ((catalinaHome != null) && (!catalinaHome.isEmpty())) {
            String libDir = IO.path(catalinaHome, "lib");

            File libs = new File(libDir);
            File[] files = libs.listFiles(new JarFilenameFilter());

            for (File f : files) {
                jars.add(f.toURI().toURL());
            }
        }

        URL[] urls = new URL[jars.size()];
        urls = jars.toArray(urls);

        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();

        return new URLClassLoader(urls, cl);
    }
}
