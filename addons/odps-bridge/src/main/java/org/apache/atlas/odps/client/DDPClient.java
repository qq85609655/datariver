package org.apache.atlas.odps.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.odps.hook.OdpsHook;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 * DDP客户端，用于根据API获取信息
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class DDPClient {
    public static final String DDP_CLIENT_URL = "http://localhost:8080/api/v1/dtalent/ddp";
    public static final String QUICK_START_FOLDER_PREFIX = "/examples/";
    public static final String ODPS_DICTIONARY_JSON = "odps-dictionary.json";
    public static final String ODPS_INSTANCE_SUMMARY_JSON = "odps-instance-summary.json";
    private org.slf4j.Logger logger = LoggerFactory.getLogger(DDPClient.class);

    private WebResource service;

    private static DDPClient instance = null;

    private DDPClient() {
        try {
            int readTimeout = 60000;
            int connectTimeout = 60000;
            Configuration clientConfig = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
            readTimeout = clientConfig.getInt("atlas.client.readTimeoutMSecs", readTimeout);
            connectTimeout = clientConfig.getInt("atlas.client.connectTimeoutMSecs", connectTimeout);
            String baseUrl = clientConfig.getString("atlas.ddp.url", DDP_CLIENT_URL);
            DefaultClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            client.setConnectTimeout(connectTimeout);
            client.setReadTimeout(readTimeout);
            service = client.resource(UriBuilder.fromUri(baseUrl).build());
        } catch (Exception e) {
            logger.error("Create DDPClient Failed", e);
        }
    }

    public static DDPClient getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (DDPClient.class) {
            if (instance != null) {
                return instance;
            }
            instance = new DDPClient();
            return instance;
        }
    }

    /**
     * 获取数据字典信息
     *
     * @param wsName
     * @param table
     * @return
     */
    public String getDictionary(String wsName, String projectName, String table) {
        Map<String, String> params = new HashedMap();
        params.put("workspaceName", wsName);
        if (projectName != null){
            params.put("projectName",projectName);
        }
        if (table != null){
            params.put("tableName", table);
        }

        if (OdpsHook.MockHolder.isMockDdp()) {
            return loadFromFile(table);
        }
        String retStr = requestGet(DDPAPI.TABLE, params);
        return retStr;
    }

    /**
     * 从文件中加载dict信息
     *
     * @param table
     * @return
     */
    private String loadFromFile(String table) {
        String dict;
        if (OdpsHook.MockHolder.isQuickStart()) {
            dict = FileUtil.loadResourceFile(System.getProperty("atlas.home") +
                    QUICK_START_FOLDER_PREFIX + ODPS_DICTIONARY_JSON);
        } else {
            dict = FileUtil.loadResourceFile(this, ODPS_DICTIONARY_JSON);
        }
        JSONObject jsonObject = JSON.parseObject(dict);
        if (table != null) {
            //返回正确的tableMeta
            Map<String, JSONObject> tname2metaMap = new HashMap<>();
            for (Map.Entry<String, Object> tableJson :
                    jsonObject.getJSONObject("packageMeta").getJSONObject("tables").entrySet()) {
                String ptname = tableJson.getKey();
                JSONObject tableMeta = (JSONObject) tableJson.getValue();
                String key[] = ptname.split("\\.");
                tname2metaMap.put(key[1], tableMeta);
            }
            JSONObject meta = tname2metaMap.get(table);
            if (meta != null) {
                jsonObject.put("tableMeta", meta.getJSONObject("tableMeta"));
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 获取项目的所有tables,resources,package信息
     *
     * @param wsName
     * @return
     */
    public String getProjectRelatedDictionary(String wsName,String projectName) {
        Map<String, String> params = new HashedMap();
        params.put("workspaceName", wsName);
        params.put("projectName", projectName);
        params.put("types", DictType.ALL_TABLES + "," + DictType.RESOURCE + "," + DictType.PACKAGE + "," + DictType.PROJECT);
        if (OdpsHook.MockHolder.isMockDdp()) {
            return loadFromFile(null);
        }
        return requestGet(DDPAPI.DICTOINARY, params);
    }

    /**
     * 获取实例相关信息
     *
     * @param wsName
     * @param instanceId
     * @return
     */
    public String getInstanceSummary(String wsName, String instanceId) {
        Map<String, String> params = new HashedMap();
        params.put("workspaceName", wsName);
        if(instanceId.startsWith("MR-")){
            params.put("instanceId", instanceId.substring(3));
        } else if (instanceId.startsWith("SQL-")){
            params.put("instanceId", instanceId.substring(4));
        } else {
            params.put("instanceId", instanceId);
        }
        if (OdpsHook.MockHolder.isMockDdp()) {
            if (OdpsHook.MockHolder.isQuickStart()) {
                return FileUtil.loadResourceFile(System.getProperty("atlas.home") +
                        QUICK_START_FOLDER_PREFIX + ODPS_INSTANCE_SUMMARY_JSON);
            } else {
                return FileUtil.loadResourceFile(this, ODPS_INSTANCE_SUMMARY_JSON);
            }
        }
        return requestGet(DDPAPI.INSTANCE_SUMMAY, params);
    }

    public String getDXTAccount(String workspaceName) {
        Map<String, String> params = new HashedMap();
        params.put("workspaceName", workspaceName);
        return requestGet(DDPAPI.DXT_ACCOUNT, params);
    }

    /**
     * 发送请求
     *
     * @param api
     * @param params
     * @return
     */
    private String requestGet(DDPAPI api, Map<String, String> params) {
        MultivaluedMap map = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            map.add(entry.getKey(), entry.getValue());
        }
        String result = service.path(api.getPath()).queryParams(map).get(String.class);
        return getResultData(result);
    }

    private String getResultData(String result) {
        DDPResult ddpResult = JSON.parseObject(result, DDPResult.class);
        String returnResult = "{}";
        if (ddpResult.isSuccess()) {
            returnResult = ddpResult.getRetData().toJSONString();
        }
        return returnResult;
    }

    enum DDPAPI {
        INSTANCE_SUMMAY("/metadata/proxy/odps/instance", HttpMethod.GET),
        DICTOINARY("/metadata/proxy/odps/dictionary", HttpMethod.GET),
        TABLE("/metadata/proxy/odps/table", HttpMethod.GET),
        DXT_ACCOUNT("/workspace/account", HttpMethod.GET);
        private String path;
        private String method;

        DDPAPI(String path, String method) {
            this.path = path;
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }
    }

    public enum DictType {
        ALL_TABLES,
        TABLE_AND_PARTITION,
        RESOURCE,
        PACKAGE,
        PROJECT
    }
}
