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

/**
 * @author pidster
 * 
 */
public class DateTime {

    private static final int UNIT_SECOND = 1000;

    private static final int UNIT_MINUTE = 60000;

    private static final int UNIT_HOUR = 60 * UNIT_MINUTE;

    private static final int UNIT_DAY = 24 * UNIT_HOUR;

    /**
     * @param uptime
     * @return
     */
    public static String formatUptime(Long uptime) {

        Long days = (uptime) / UNIT_DAY;
        Long hours = (uptime - (days * UNIT_DAY)) / UNIT_HOUR;
        Long minutes = (uptime - ((days * UNIT_DAY) + (hours * UNIT_HOUR)))
                / UNIT_MINUTE;
        Long seconds = (uptime - ((days * UNIT_DAY) + (hours * UNIT_HOUR) + (minutes * UNIT_MINUTE)))
                / UNIT_SECOND;
        Long millis = (uptime - ((days * UNIT_DAY) + (hours * UNIT_HOUR)
                + (minutes * UNIT_MINUTE) + (seconds * UNIT_SECOND))) / (1);

        StringBuilder s = new StringBuilder();

        if (days > 0) {
            s.append(days);
            s.append("d ");
        }
        if (hours > 0) {
            s.append(hours);
            s.append("h ");
        }
        if (minutes > 0) {
            s.append(minutes);
            s.append("m ");
        }
        if (seconds > 0) {
            s.append(seconds);
            s.append("s ");
        }
        if (millis > 0) {
            s.append(millis);
            s.append("ms");
        }

        return s.toString();
    }

}
