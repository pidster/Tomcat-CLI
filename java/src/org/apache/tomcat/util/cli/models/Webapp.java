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

package org.apache.tomcat.util.cli.models;

import java.util.Date;

/**
 * @author SWilliams
 * 
 */
public class Webapp {

    private String path;

    private String docBase;

    private String state;

    private long sessions;

    private long startTime;

    private long tldScanTime;

    private Date created;

    /**
     * 
     */
    public Webapp() {

    }

    /**
     * @return the path
     */
    public final String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public final void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the docBase
     */
    public final String getDocBase() {
        return docBase;
    }

    /**
     * @param docBase
     *            the docBase to set
     */
    public final void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    /**
     * @return the state
     */
    public final String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public final void setState(String state) {
        this.state = state;
    }

    /**
     * @return the sessions
     */
    public final long getSessions() {
        return sessions;
    }

    /**
     * @param sessions
     *            the sessions to set
     */
    public final void setSessions(long sessions) {
        this.sessions = sessions;
    }

    /**
     * @return the startTime
     */
    public final long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public final void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the tldScanTime
     */
    public final long getTldScanTime() {
        return tldScanTime;
    }

    /**
     * @param tldScanTime
     *            the tldScanTime to set
     */
    public final void setTldScanTime(long tldScanTime) {
        this.tldScanTime = tldScanTime;
    }

    /**
     * @return the created
     */
    public final Date getCreated() {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    public final void setCreated(Date created) {
        this.created = created;
    }

}
