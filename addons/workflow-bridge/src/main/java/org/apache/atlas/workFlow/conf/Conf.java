/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.workFlow.conf;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.commons.configuration.Configuration;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class Conf {

    private static Configuration atlasProperties;

    public static final String CONF_PREFIX = "atlas.hook.hive.";
    public static final String MIN_THREADS = CONF_PREFIX + "minThreads";
    public static final String MAX_THREADS = CONF_PREFIX + "maxThreads";
    public static final String KEEP_ALIVE_TIME = CONF_PREFIX + "keepAliveTime";
    public static final String CONF_SYNC = CONF_PREFIX + "synchronous";
    public static final String ATLAS_ENDPOINT = "atlas.rest.address";
    public static final String HOOK_NUM_RETRIES = CONF_PREFIX + "numRetries";
    public static final String OOZIE_CLIENT_URL = "atlas.oozie.url";
    public static final String OOZIE_CLIENT_USER = "atlas.oozie.user";
    public static final String DEFAULT_OOZIE_CLIENT_URL = "http://localhost:11000/oozie";
    public static final String DEFAULT_OOZIE_CLIENT_USER = "root";
    public static final String WORKFLOW_INFO_TO_DXT = "atlas.workflow.info.to.dxt";
    public static final String WORKFLOW_INFO_TO_ODPS = "atlas.workflow.info.to.odps";

    static {
        try {
            atlasProperties = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
        } catch (AtlasException e) {
            e.printStackTrace();
        }
    }

    public static Configuration getConf() {
        return atlasProperties;
    }
}
