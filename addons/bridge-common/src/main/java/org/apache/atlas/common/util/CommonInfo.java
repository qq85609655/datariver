package org.apache.atlas.common.util;

import java.util.Map;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */

public interface CommonInfo {

    public static final String INSTANCE_ID = "instanceId";

    public static final String CONFIG = "config";

    public static final String ACTION_GUID = "actionGuid";

    public static final String WORKSPACE_NAME = "workspaceName";

    public static final String CONFIG_ID = "configId";

    public static final String TABLE_NAME = "tableName";

    public static final String SOURCE_TYPE = "sourceType";

    void sendActionConf(Map<String, String> paramMap, LineageHandler lineageHandler);
}
