package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.typesystem.Referenceable;

/**
 * 简单单个表信息解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 15:27
 */
public class SimpleTableParser extends TableParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject tableMeta = (JSONObject) data[0];
        JSONObject projectMeta = (JSONObject) data[1];
        String tableName = tableMeta.getString("name");

        Referenceable tableFromMeta = getTableFromMeta(context, tableMeta, projectMeta);
        context.put(type(), tableFromMeta);
    }
}
