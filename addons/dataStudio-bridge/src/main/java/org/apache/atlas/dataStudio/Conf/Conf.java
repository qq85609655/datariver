package org.apache.atlas.dataStudio.Conf;

/**
 * 描述信息
 *
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/26 14:15
 */
public class Conf {

    public static final String CONF_PREFIX = "atlas.hook.hive.";
    public static final String MIN_THREADS = CONF_PREFIX + "minThreads";
    public static final String MAX_THREADS = CONF_PREFIX + "maxThreads";
    public static final String KEEP_ALIVE_TIME = CONF_PREFIX + "keepAliveTime";
    public static final String CONF_SYNC = CONF_PREFIX + "synchronous";
    public static final String ATLAS_ENDPOINT = "atlas.rest.address";
    public static final String HOOK_NUM_RETRIES = CONF_PREFIX + "numRetries";
    public static final String DATA_STUDIO_ACTIVEMQ_URL = "atlas.dataStudio.activemq.url";
}
