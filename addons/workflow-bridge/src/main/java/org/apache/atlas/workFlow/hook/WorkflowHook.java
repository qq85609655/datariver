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

package org.apache.atlas.workFlow.hook;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.atlas.workFlow.MessageConsumer.ConsumerForWorkFlow;
import org.apache.atlas.workFlow.conf.Conf;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.apache.atlas.workFlow.handler.WorkflowJobHandler;
import org.apache.atlas.workFlow.model.WorkFlowDataModelGenerator;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;
import org.apache.commons.configuration.Configuration;
import org.apache.oozie.AppType;
import org.apache.oozie.client.JMSConnectionInfo;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.event.Event;
import org.apache.oozie.client.event.jms.JMSHeaderConstants;
import org.apache.oozie.client.event.jms.JMSMessagingUtils;
import org.apache.oozie.client.event.message.CoordinatorActionMessage;
import org.apache.oozie.client.event.message.SLAMessage;
import org.apache.oozie.client.event.message.WorkflowJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.*;
/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
@Singleton
public class WorkflowHook {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowHook.class);

    // wait time determines how long we wait before we exit the jvm on
    // shutdown. Pending requests after that will not be sent.
    public static final int WAIT_TIME = 3;
    private static ExecutorService executor;

    public static final int minThreadsDefault = 5;
    public static final int maxThreadsDefault = 5;
    public static final long keepAliveTimeDefault = 10;


    private static WorkflowHook workflowHook = null;

    static {
        try {
            // initialize the async facility to process hook calls. We don't
            // want to do this inline since it adds plenty of overhead for the query.
            int minThreads = Conf.getConf().getInt(Conf.MIN_THREADS, minThreadsDefault);
            int maxThreads = Conf.getConf().getInt(Conf.MAX_THREADS, maxThreadsDefault);
            long keepAliveTime = Conf.getConf().getLong(Conf.KEEP_ALIVE_TIME, keepAliveTimeDefault);

            executor = new ThreadPoolExecutor(minThreads, maxThreads, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryBuilder().setNameFormat("Atlas Logger %d").build());

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        executor.shutdown();
                        executor.awaitTermination(WAIT_TIME, TimeUnit.SECONDS);
                        executor = null;
                    } catch (InterruptedException ie) {
                        LOG.info("Interrupt received in shutdown.");
                    }
                    // shutdown client
                }
            });
        } catch (Exception e) {
            LOG.info("Attempting to send msg while shutdown in progress.", e);
        }
    }

    public static WorkflowHook getInstance() {
        if (workflowHook == null) {
            synchronized (WorkflowHook.class) {
                if (workflowHook == null) {
                    workflowHook = new WorkflowHook();
                }
            }
        }
        return workflowHook;
    }

    private WorkflowHook() {
        boolean sign = true;
        while (sign) {
            try {
                initJMS();
                sign = false;
            } catch (OozieClientException e) {
                LOG.info("initJMS ", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (NamingException e) {
                LOG.info("initJMS ", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (JMSException e) {
                LOG.info("initJMS ", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void initJMS() throws OozieClientException, NamingException, JMSException {
        OozieClient oc = new OozieClient(Conf.getConf().getString(Conf.OOZIE_CLIENT_URL, Conf.DEFAULT_OOZIE_CLIENT_URL));
        JMSConnectionInfo jmsInfo = oc.getJMSConnectionInfo();
        Properties jndiProperties = jmsInfo.getJNDIProperties();
        Context jndiContext = new InitialContext(jndiProperties);
        String connectionFactoryName = (String) jndiContext.getEnvironment().get("connectionFactoryNames");
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(connectionFactoryName);
        String topicPrefix = jmsInfo.getTopicPrefix();
        ConsumerForWorkFlow wfConsumer = new ConsumerForWorkFlow(executor, connectionFactory, topicPrefix + "workflowTopic");
        wfConsumer.start();
    }
}
