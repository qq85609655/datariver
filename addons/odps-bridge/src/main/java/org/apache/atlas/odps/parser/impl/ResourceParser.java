package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class ResourceParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject all = JSON.parseObject((String) data[0]);
        Referenceable projectRefe = (Referenceable) data[1];
        JSONArray resourceMeta = all.getJSONArray("resourceMeta");
        List<Referenceable> resources = new ArrayList<>();
        String resourceQualifiedName = null;
        for (int i = 0; i < resourceMeta.size(); i++) {
            JSONObject resourceJson = resourceMeta.getJSONObject(i);
            resourceQualifiedName = formatQualifiedName(String.valueOf(projectRefe.get(QUALIFIED_NAME)),
                resourceJson.getString("name"));
            Referenceable existResource = null;
            try {
                existResource = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_RESOURCE.getValue(),
                    BaseJsonParser.QUALIFIED_NAME, resourceQualifiedName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (existResource != null){
                continue;
            }
            Referenceable resource = newInstance(OdpsDataTypes.ODPS_RESOURCE);
            setStringWithSameKey(resource, resourceJson, "name");
            setStringWithSameKey(resource, resourceJson, "owner");
            setLongWithSameKey(resource, resourceJson, "createTime");
            setLongWithSameKey(resource, resourceJson, "lastModifiedTime");
            resource.set("description", resourceJson.getString("comment"));
            resource.set("project", projectRefe);
            resource.set("resourceType", StringUtils.upperCase(resourceJson.getString("type")));
            resource.set("metaSource", "ODPS");
            resource.set(QUALIFIED_NAME, resourceQualifiedName);
            BaseJsonParser.updateEntityMap(ENTITY_MAP_TYPE.PROJECT_RESOURCES, (String)projectRefe.get(QUALIFIED_NAME), resourceQualifiedName);
            resources.add(resource);
        }
        context.put(OdpsDataTypes.ODPS_RESOURCE, addCurrentTypeListReferenceables(context, resources));
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_RESOURCE;
    }

    public  Referenceable createResource(Referenceable projectRef, JSONObject jsonObject, String type){
        String resourceQualifiedName = null;
        Referenceable resource = newInstance(OdpsDataTypes.ODPS_RESOURCE);
        JSONObject dataJson = jsonObject.getJSONObject("data");

        resource.set("project", projectRef);
        resource.set("name", dataJson.getString("resourceName"));
        resource.set("owner", jsonObject.getString("user"));
        resource.set("createTime", jsonObject.getLong("time"));
        resource.set("lastModifiedTime", jsonObject.getLong("time"));
        resource.set("description", dataJson.getString("description"));
        resource.set("resourceType", type);
        resource.set("metaSource", "ODPS");
        resourceQualifiedName = formatQualifiedName(String.valueOf(projectRef.get(QUALIFIED_NAME)),
            dataJson.getString("resourceName"));
        resource.set(QUALIFIED_NAME, resourceQualifiedName);

        return  resource;
    }
}
