package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;

/**
 * 项目解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class ProjectParser extends BaseJsonParser {

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_PROJECT;
    }

    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject projectJson = (JSONObject) data[0];
        String name = projectJson.getString(NAME);
        currentEntity.set(QUALIFIED_NAME, getProjectQualifiedName(projectJson));
        currentEntity.set(NAME, name);
        setStringWithSameKey(currentEntity, projectJson, "projectGroupname");
        setStringWithSameKey(currentEntity, projectJson, "description");
        setStringWithSameKey(currentEntity, projectJson, "owner");
        setLongWithSameKey(currentEntity, projectJson, "createTime");
        setLongWithSameKey(currentEntity, projectJson, "lastModifiedTime");
        setStringWithSameKey(currentEntity, projectJson, CLUSTER_NAME);
        setStringWithSameKey(currentEntity, projectJson, "clusterQuota");
        currentEntity.set("dbType", "ODPS");
        currentEntity.set("status", projectJson.getString("status"));
        ParserFactory.getAccInfoParser().parse(context, projectJson.getJSONObject("accessInfo"));
        Object value = context.getValue(OdpsDataTypes.ODPS_ACCINFO);
        currentEntity.set("accessInfo", value);
    }

}
