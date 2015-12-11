package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.odps.parser.impl.TableParser;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理tablesMeta
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-13 10:52
 */
public class TablesParser extends TableParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONArray tablesMeta = (JSONArray) data[0];
        Referenceable project = (Referenceable) data[1];
        JSONObject projectMeta = (JSONObject) data[2];
        List<Referenceable> tables = new ArrayList<>();
        for (JSONObject tableAndPartionMeta : tablesMeta.toArray(new JSONObject[0])) {
            JSONObject tableMeta = tableAndPartionMeta.getJSONObject("tableMeta");
            String qName = formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), tableMeta.getString(NAME));
            Referenceable table = null;
            if (!getTableRefe().containsKey(qName)) {
                table = getTableFromMeta(context, tableMeta, projectMeta);
                tables.add(table);
            } else {
                table = getTableRefe().get(qName);
            }
            JSONArray partitionMeta = tableAndPartionMeta.getJSONArray("partitionMeta");
            ParserFactory.getPartitionParser().parse(context, partitionMeta,table);
        }
        context.put(type(), tables);
    }
}
