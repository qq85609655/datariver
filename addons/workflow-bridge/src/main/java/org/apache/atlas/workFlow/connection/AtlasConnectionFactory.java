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

package org.apache.atlas.workFlow.connection;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.notification.NotificationInterface;
import org.apache.atlas.notification.NotificationModule;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.workFlow.conf.Conf;
import org.codehaus.jettison.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class AtlasConnectionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasConnectionFactory.class);

    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";

    @Inject
    private static NotificationInterface notifInterface;

    private static AtlasClient atlasClient = null;

    static {
        atlasClient = new AtlasClient(Conf.getConf().getString(Conf.ATLAS_ENDPOINT, DEFAULT_DGI_URL), null, null);
        Injector injector = Guice.createInjector(new NotificationModule());
        notifInterface = injector.getInstance(NotificationInterface.class);
    }

    public static AtlasClient getAtlasClient() {
        return atlasClient;
    }

    public static NotificationInterface getNotificationInterface() {
        return notifInterface;
    }
}
