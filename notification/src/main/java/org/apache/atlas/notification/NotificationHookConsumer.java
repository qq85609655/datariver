/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.service.Service;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Consumer of notifications from hooks e.g., hive hook etc
 */
@Singleton
public class NotificationHookConsumer implements Service {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationHookConsumer.class);

    public static final String CONSUMER_THREADS_PROPERTY = "atlas.notification.hook.numthreads";
    public static final String ATLAS_ENDPOINT_PROPERTY = "atlas.rest.address";
    public static final String TYPE_UPDATE = "updateEntity";

    @Inject
    private NotificationInterface notificationInterface;
    private ExecutorService executors;
    private AtlasClient atlasClient;

    @Override
    public void start() throws AtlasException {
        Configuration applicationProperties = ApplicationProperties.get();

        String atlasEndpoint = applicationProperties.getString(ATLAS_ENDPOINT_PROPERTY, "http://localhost:21000");
        atlasClient = new AtlasClient(atlasEndpoint);
        int numThreads = applicationProperties.getInt(CONSUMER_THREADS_PROPERTY, 1);
        List<NotificationConsumer<JSONArray>> consumers =
            notificationInterface.createConsumers(NotificationInterface.NotificationType.HOOK, numThreads);
        executors = Executors.newFixedThreadPool(consumers.size());

        for (final NotificationConsumer<JSONArray> consumer : consumers) {
            executors.submit(new HookConsumer(consumer));
        }
    }

    @Override
    public void stop() {
        //Allow for completion of outstanding work
        notificationInterface.close();
        try {
            if (executors != null && !executors.awaitTermination(30000, TimeUnit.MILLISECONDS)) {
                LOG.error("!!!Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            LOG.error("!!!Failure in shutting down consumers");
        }
    }

    class HookConsumer implements Runnable {
        private final NotificationConsumer<JSONArray> consumer;

        public HookConsumer(NotificationConsumer<JSONArray> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while (consumer.hasNext()) {
                JSONArray entityJson = consumer.next();
                LOG.info("Processing message {}", entityJson);
                try {
                    if (isUpdate(entityJson)) {
                        processUpdate(entityJson);
                    } else {
                        JSONArray guids = atlasClient.createEntity(entityJson);
                        LOG.info("Create entities with guid {}", guids);
                    }
                } catch (Exception e) {
                    //todo handle failures
                    LOG.warn("Error handling message {}", entityJson, e);
                }
            }
        }

        private void processUpdate(JSONArray entityJson) throws JSONException, AtlasServiceException, AtlasException {
            JSONObject msg = entityJson.getJSONObject(0);
            String type = msg.getString("type");
            if (StringUtils.equals(type, TYPE_UPDATE)) {
                JSONObject data = msg.getJSONObject("data");
                String value = data.getString("value");
                if (value.startsWith(AtlasClient.QUALIFIED_NAME)) {
                    String valueJson = value.substring(AtlasClient.QUALIFIED_NAME.length());
                    JSONObject valueObject = new JSONObject(valueJson);
                    Referenceable createdEntity = atlasClient.getEntity(valueObject.getString("typeName"),
                        AtlasClient.QUALIFIED_NAME, valueObject.getString(AtlasClient.QUALIFIED_NAME));
                    value = createdEntity.getId().id;
                }
                atlasClient.updateEntityAttribute(data.getString("guid"), data.getString("property"), value);
                LOG.info("Update entities with result");
            } else {
                throw new AtlasException("UnSupported Type");
            }
        }

        private boolean isUpdate(JSONArray entityJson) throws JSONException {
            if (entityJson.length() != 1) {
                return false;
            }

            Object jsonObject = entityJson.get(0);
            return (jsonObject instanceof JSONObject)
                && StringUtils.equals(((JSONObject) jsonObject).getString("type"), "updateEntity");

        }
    }
}
