package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * 列解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class ColumnParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONArray columnsJson = (JSONArray) data[0];
        boolean isPartitionColumn = (boolean) data[1];
        String quarlifiedName = (String) data[2];
        List<Referenceable> columns = createColumnEntities(columnsJson, isPartitionColumn, quarlifiedName);
        context.put(type(), columns);
    }

    public  List<Referenceable> createColumnEntities(JSONArray columnsJson, Boolean isPartitionColumn, String quarlifiedName){
        List<Referenceable> columns = new ArrayList<>();
        for (int i = 0; i < columnsJson.size(); i++) {
            JSONObject columnJson = columnsJson.getJSONObject(i);
            Referenceable column = newInstance(OdpsDataTypes.ODPS_COLUMN);
            setStringWithSameKey(column, columnJson, "name");
            column.set(QUALIFIED_NAME, formatQualifiedName(quarlifiedName, columnJson.getString("name")));
            column.set("dataType", columnJson.getString("type"));
            column.set("description",columnJson.getString("comment"));
            column.set("label",columnJson.getLongValue("label"));
            column.set("isPartitionKey", isPartitionColumn);
            column.set("metaSource", "ODPS");
            columns.add(column);
        }
        return  columns;
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_COLUMN;
    }
}
