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

package org.pidster.tomcat.util.cli.util;

import java.io.Closeable;
import java.io.File;

/**
 * @author pidster
 * 
 * @version $Date$
 * 
 */
public class IO {

    /**
     * Private constructor, just so we're clear it's not usable
     */
    private IO() {
        //
    }

    /**
     * @param parts
     * @return path
     */
    public static final String path(String... parts) {
        return path(false, parts);
    }

    /**
     * @param parts
     * @return path
     */
    public static final String path(boolean absolute, String... parts) {

        StringBuilder s = new StringBuilder();

        boolean first = true;
        for (String part : parts) {
            if (!first || absolute)
                s.append(File.separator);
            s.append(part);
            first = false;
        }

        return s.toString();
    }

    /**
     * @param closable
     */
    public static final void close(Closeable closable) {

        if (closable == null)
            return;

        try {
            closable.close();
        }
        catch (Exception e) {
            // If this happens, we might as well know about it
            e.printStackTrace();
        }
    }

}
