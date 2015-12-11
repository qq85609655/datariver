package org.apache.atlas.common.hook;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.common.exception.BridgeException;
import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.common.util.LineageHandler;
import org.apache.atlas.notification.NotificationHookConsumer;
import org.apache.atlas.notification.NotificationInterface;
import org.apache.atlas.notification.NotificationModule;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.commons.configuration.Configuration;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 其他插件的钩子需要集成的基本类
 * <li>管理线程池及其配置</li>
 * <li>管理AtlasClient</li>
 * <li>管理NotificationInterface</li>
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */

public abstract class BaseHook {
    private static final Logger LOG = LoggerFactory.getLogger(BaseHook.class);

    public static final String COMMON_CONF_PREFIX = "atlas.hook.common.";
    private static final String MIN_THREADS = COMMON_CONF_PREFIX + "minThreads";
    private static final String MAX_THREADS = COMMON_CONF_PREFIX + "maxThreads";
    private static final String KEEP_ALIVE_TIME = COMMON_CONF_PREFIX + "keepAliveTime";
    public static final String QUEUE_SIZE = COMMON_CONF_PREFIX + "queueSize";
    public static final String HOOK_NUM_RETRIES = COMMON_CONF_PREFIX + "numRetries";

    // wait time determines how long we wait before we exit the jvm on
    // shutdown. Pending requests after that will not be sent.
    private static final int WAIT_TIME = 3;
    private static ExecutorService executor;

    private static final int minThreadsDefault = 5;
    private static final int maxThreadsDefault = 5;
    private static final long keepAliveTimeDefault = 10;
    private static final int queueSizeDefault = 10000;

    private static Configuration atlasProperties;
    @Inject
    private static NotificationInterface notifInterface;

    protected AtlasClient dgiCLient;
    private static String dgiUrl = "http://localhost:21000/";

    public BaseHook() {
        dgiCLient = new AtlasClient(dgiUrl);
    }

    static {
        try {
            atlasProperties = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);

            // initialize the async facility to process hook calls. We don't
            // want to do this inline since it adds plenty of overhead for the query.
            int minThreads = atlasProperties.getInt(MIN_THREADS, minThreadsDefault);
            int maxThreads = atlasProperties.getInt(MAX_THREADS, maxThreadsDefault);
            long keepAliveTime = atlasProperties.getLong(KEEP_ALIVE_TIME, keepAliveTimeDefault);
            int queueSize = atlasProperties.getInt(QUEUE_SIZE, queueSizeDefault);

            executor = new ThreadPoolExecutor(minThreads, maxThreads, keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueSize),
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
            //Find Kafka Server host,use the first one
            String serverInfo = ApplicationProperties.get(ApplicationProperties.APPLICATION_PROPERTIES)
                .getString("atlas.kafka.bootstrap.servers");
            String kafkaServerHost = serverInfo.split(";")[0].split(":")[0];
            dgiUrl = "http://" + kafkaServerHost + ":21000/";
        } catch (Exception e) {
            LOG.info("Attempting to send msg while shutdown in progress.", e);
        }

        Injector injector = Guice.createInjector(new NotificationModule());
        notifInterface = injector.getInstance(NotificationInterface.class);
        LOG.info("Created Atlas Base Hook");
    }

    /**
     * 加载文件.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    protected String loadFile(String filename) throws IOException {
        return FileUtil.loadResourceFile(this, filename);
    }

    /**
     * 发送消息，目前，消费端仅做创建操作,更新使用{@link #notifyEntityForUpdate}.
     *
     * @param entities
     */
    protected void notifyEntity(Collection<Referenceable> entities) {
        JSONArray entitiesArray = new JSONArray();
        for (Referenceable entity : entities) {
            if (entity == null) {
                continue;
            }
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
        }
        notifyEntity(entitiesArray.toString());
    }

    protected void notifyEntity(Referenceable entity){
        JSONArray entitiesArray = new JSONArray();
        String entityJson = InstanceSerialization.toJson(entity, true);
        entitiesArray.put(entityJson);
        notifyEntity(entitiesArray.toString());
    }

    /**
     * Notify atlas of the entity through message. The entity can be a complex entity with reference to other entities.
     * De-duping of entities is done on server side depending on the unique attribute on the
     *
     * @param entities
     */
    protected void notifyEntity(String entities) {
        int maxRetries = atlasProperties.getInt(HOOK_NUM_RETRIES, 3);
        String message = entities;

        int numRetries = 0;
        while (true) {
            try {
                notifInterface.send(NotificationInterface.NotificationType.HOOK, message);
                return;
            } catch (Exception e) {
                numRetries++;
                if (numRetries < maxRetries) {
                    LOG.debug("Failed to notify atlas for entity {}. Retrying", message, e);
                } else {
                    LOG.error("Failed to notify atlas for entity {} after {} retries. Quitting", message,
                        maxRetries, e);
                    return;
                }
            }
        }
    }

    /**
     * 发送更新消息.
     *
     * @param guid
     * @param property
     * @param value
     */
    protected void notifyEntityForUpdate(String guid, String property, Object value) {
        try {
            if (value instanceof Referenceable) {
                JSONArray entities = dgiCLient.createEntity(InstanceSerialization.toJson((Referenceable) value, true));
                value = entities.get(0);
            }
            JSONObject updateObject = buildUpdateEntity(guid, property, value);

            //dgiCLient.updateEntity(guid, property, updateObject.getJSONObject("data").getString("value"));
            notifyEntity(updateObject.toString());
        } catch (Exception e) {
            LOG.error("更新失败", e);
        }
    }

    /**
     * 构造更新实体.
     *
     * @param guid
     * @param property
     * @param value
     * @return
     */
    protected JSONObject buildUpdateEntity(String guid, String property, Object value) {
        if (guid == null || property == null || value == null) {
            throw new BridgeException("更新缺少参数");
        }
        JSONObject updateObject = new JSONObject();
        try {
            updateObject.put("type", NotificationHookConsumer.TYPE_UPDATE);
            JSONObject data = new JSONObject();
            data.put("guid", guid);
            data.put("property", property);
            data.put("value", value);
            updateObject.put("data", data);
        } catch (JSONException e) {
            LOG.error("构建更新参数异常", e);
            throw new BridgeException("构建更新参数异常");
        }
        return updateObject;
    }

    /**
     * 启动方法.
     *
     * @param context
     */
    public void run(final HookContext context) {
        if (isSynchronized()) {
            doRun(context);
        } else {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        doRun(context);
                    } catch (Throwable e) {
                        LOG.error("执行Hook失败", e);
                    }
                }
            });
        }
    }

    /**
     * 执行子类具体业务.
     *
     * @param context
     */
    protected abstract void doRun(HookContext context);

    /**
     * 是否同步，子类可覆盖.
     *
     * @return
     */
    protected boolean isSynchronized() {
        return false;
    }

    /**
     * 上下文信息.
     */
    public static class HookContext {
        private Map<String, String> params = new HashMap<>();

        private LineageHandler lineageHandler;

        public LineageHandler getLineageHandler() {
            return lineageHandler;
        }

        public void setLineageHandler(LineageHandler lineageHandler) {
            this.lineageHandler = lineageHandler;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public void putParam(String key, String value) {
            this.params.put(key, value);
        }
    }
}
