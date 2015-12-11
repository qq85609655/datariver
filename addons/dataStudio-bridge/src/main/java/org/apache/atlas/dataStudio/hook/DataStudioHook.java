package org.apache.atlas.dataStudio.hook;

import com.dtdream.dthink.dtalent.activemq.ActiveMqReceiver;
import com.dtdream.dthink.dtalent.activemq.ActiveMqSender;
import com.dtdream.dthink.dtalent.activemq.JobMessage;
import com.dtdream.dthink.dtalent.activemq.MessageEnum;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.dataStudio.Conf.Conf;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 描述信息
 *
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/26 13:38
 */
public class DataStudioHook {

    public static final String DataStudio_INFO_TO_ODPS = "atlas.workflow.info.to.odps";

    private static final Logger LOG = LoggerFactory.getLogger(DataStudioHook.class);

    // wait time determines how long we wait before we exit the jvm on
    // shutdown. Pending requests after that will not be sent.
    public static final int WAIT_TIME = 3;
    private static ExecutorService executor;

    public static final int minThreadsDefault = 5;
    public static final int maxThreadsDefault = 20;
    public static final long keepAliveTimeDefault = 10;


    private static DataStudioHook dataStudioHook = null;

    private static Configuration atlasProperties;

    private CommonInfo commonInfo;

    static {
        try {
            atlasProperties = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
        } catch (AtlasException e) {
            e.printStackTrace();
        }

        try {
            // initialize the async facility to process hook calls. We don't
            // want to do this inline since it adds plenty of overhead for the query.
            int minThreads = atlasProperties.getInt(Conf.MIN_THREADS, minThreadsDefault);
            int maxThreads = atlasProperties.getInt(Conf.MAX_THREADS, maxThreadsDefault);
            long keepAliveTime = atlasProperties.getLong(Conf.KEEP_ALIVE_TIME, keepAliveTimeDefault);

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

    public static DataStudioHook getInstance() {
        if (dataStudioHook == null) {
            synchronized (DataStudioHook.class) {
                if (dataStudioHook == null) {
                    dataStudioHook = new DataStudioHook();
                }
            }
        }
        return dataStudioHook;
    }

    private DataStudioHook() {
        boolean sign = true;
        while (sign) {
            try {
                receive();
                sign = false;
            } catch (JMSException e) {
                LOG.info("initJMS ", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (AtlasException e) {
                e.printStackTrace();
            }
        }
    }

    public void receive() throws JMSException, AtlasException {
        Configuration conf = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
        String dataStudioToOdpsClass = conf.getString(DataStudio_INFO_TO_ODPS);
        commonInfo = getCommonInfoInstance(dataStudioToOdpsClass);
        MessageListener listener = new MessageListener() {

            @Override
            public void onMessage(Message message) {
                JobMessage message1 = JobMessage.toJobMessage(message);
                Map<String, String> paramMap = new HashMap();
                paramMap.put(CommonInfo.CONFIG, message1.getBodyMessage());
                paramMap.put(CommonInfo.SOURCE_TYPE, message1.getSystemName().name());
                commonInfo.sendActionConf(paramMap, null);
                //System.out.println(message.toString());
            }
        };
        Properties props = new Properties();
        props.put(ActiveMqSender.JSM_URL, atlasProperties.getString(Conf.DATA_STUDIO_ACTIVEMQ_URL, "tcp://localhost:61616"));
        new ActiveMqReceiver(props, MessageEnum.SystemName.DATA_STUDIO, null, listener);
    }

    private static CommonInfo getCommonInfoInstance(String className) {
        if (className != null) {
            try {
                CommonInfo commonInfo = (CommonInfo) Class.forName(className).newInstance();
                return commonInfo;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
