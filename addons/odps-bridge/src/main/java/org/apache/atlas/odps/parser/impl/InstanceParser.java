package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * instance解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class InstanceParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject instSummary = JSON.parseObject((String) data[0]);
        JSONObject instanceMeta = instSummary.getJSONObject("instanceMeta");
        String instanceId = instanceMeta.getString("id");
        currentEntity.set(QUALIFIED_NAME, formatQualifiedName(instanceId));
        setStringWithSameKey(currentEntity, instanceMeta, "name");
        setStringWithSameKey(currentEntity, instanceMeta, "description");
        setStringWithSameKey(currentEntity, instanceMeta, "status");
        setStringWithSameKey(currentEntity, instanceMeta, "id");
        currentEntity.set("source", "ODPS");
        setStringWithSameKey(currentEntity, instanceMeta, "owner");
        setLongWithSameKey(currentEntity, instanceMeta, "createTime");
        setLongWithSameKey(currentEntity, instanceMeta, "startTime");
        setLongWithSameKey(currentEntity, instanceMeta, "endTime");
        JSONArray tasksDetail = instanceMeta.getJSONArray("tasksDetail");
        JSONArray tasksCmd = instanceMeta.getJSONArray("tasksCmd");
        List<Referenceable> tasksList = new ArrayList<>();
        if (tasksDetail != null && !tasksDetail.isEmpty()) {
            ParserFactory.getTaskParser().parse(context, tasksDetail, tasksCmd, instanceId,
                    data.length > 2 ? String.valueOf(data[2]) : null);
            tasksList.addAll((Collection<? extends Referenceable>) context.getValue(OdpsDataTypes.ODPS_TASK));
        }
        currentEntity.set("tasks", tasksList);
        JSONObject projectMeta = JSON.parseObject((String) data[1]).getJSONObject("projectMeta");
        String projectQualifiedName = getProjectQualifiedName(projectMeta);
        currentEntity.set("project", BaseJsonParser.getProjectRefe().get(projectQualifiedName));
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_INSTANCE;
    }
}
