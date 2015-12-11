package org.apache.atlas.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * 集成测试的公共接口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */

public abstract class BaseAddonIT {
    public static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BaseAddonIT.class);
    public static final String ATLAS_ENDPOINT = "atlas.rest.address";
    private static final String DGI_URL = "http://localhost:21000/";
    private JSONObject bridgeMetaJson;

    public void BaseAddonIT(String filename) throws Exception {
        bridgeMetaJson = JSON.parseObject(loadFile(filename));
    }

    private String loadFile(String filename) throws IOException {
        return FileUtil.loadResourceFile(this, filename);
    }

    public JSONObject getStructDefinitionByName(String name) throws Exception {
        JSONArray jsonArray = bridgeMetaJson.getJSONArray("structTypes");
        for (JSONObject obj : jsonArray.toArray(new JSONObject[0])) {
            if (StringUtils.equalsIgnoreCase(obj.getString("typeName"), name)) {
                return obj;
            }
        }
        return null;
    }

    public JSONObject getEnumDefinitionByName(String name) {
        JSONArray jsonArray = bridgeMetaJson.getJSONArray("enumTypes");
        for (JSONObject obj : jsonArray.toArray(new JSONObject[0])) {
            if (StringUtils.equalsIgnoreCase(obj.getString("name"), name)) {
                return obj;
            }
        }
        return null;
    }

    public JSONObject getClassDefinitionByName(String name) {
        JSONArray jsonArray = bridgeMetaJson.getJSONArray("classTypes");
        for (JSONObject obj : jsonArray.toArray(new JSONObject[0])) {
            if (StringUtils.equalsIgnoreCase(obj.getString("typeName"), name)) {
                return obj;
            }
        }
        return null;
    }

    public AtlasClient getAtlasClient() throws Exception {
        Configuration configuration = ApplicationProperties.get();
        String baseUrl = configuration.getString(ATLAS_ENDPOINT, DGI_URL);
        LOG.info("创建AtlasClient, {}.", baseUrl);
        return new AtlasClient(baseUrl);
    }
}
