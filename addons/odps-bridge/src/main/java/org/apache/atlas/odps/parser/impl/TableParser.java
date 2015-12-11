package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.client.DDPClient;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.commons.collections.CollectionUtils;

import javax.sql.rowset.Predicate;
import java.util.*;

/**
 * 表解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class TableParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        List<String> tableNames = (List<String>) data[0];
        String workspaceName = data.length > 1 ? String.valueOf(data[1]) : null;
        Set<Referenceable> lineageTables = data.length > 2 ? (HashSet)data[2] : null;
        Set<Referenceable> lineageDbs = data.length > 3 ? (HashSet)data[3] : null;
        List<Referenceable> tables = new ArrayList<>();
        for (String ptableName : tableNames) {
            String[] ptname = ptableName.split("\\.");
            String projectName = ptname[0];
            String tableName = ptname[1].split("/")[0];
            JSONObject jsonObject = JSON.parseObject(loadDict(workspaceName, projectName, tableName));
            JSONObject tableMeta = jsonObject.getJSONObject("tableMeta");
            JSONObject projectMeta = jsonObject.getJSONObject("projectMeta");

            Referenceable project = BaseJsonParser.parseProject(context,
                    getProjectQualifiedName(projectMeta), projectMeta);
            //已经解析过，直接使用,yuanyongxian 这块代码估计有问题，贾涛自己验证
            //Referenceable parsedTable = BaseJsonParser.getTableRefe() == null ? null : BaseJsonParser.getTableRefe().get
                    //(formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), tableName));
            Referenceable parsedTable = BaseJsonParser.getTableRefe().get(formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), tableName));
            if (parsedTable != null) {
                tables.add(parsedTable);
            } else {
                parsedTable = getTableFromMeta(context, tableMeta, projectMeta);
                if (parsedTable != null){
                    tables.add(parsedTable);
                }
            }
            if (lineageDbs != null){
                lineageDbs.add(project);
            }
        }
        if (tables.isEmpty()) {
            context.put(type(), null);
        } else {
            context.put(type(), tables);
            if (lineageTables != null){
                lineageTables.addAll(tables);
            }
        }
    }

    protected Referenceable getTableFromMeta(ParserContext context, JSONObject tableMeta, JSONObject projectMeta) throws AtlasException {
        Referenceable project = BaseJsonParser.parseProject(context,
            getProjectQualifiedName(projectMeta), projectMeta);
        if ((tableMeta == null) || (projectMeta == null)){
            return  null;
        }
        try {
            Referenceable existTable = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_TABLE.getValue(),
                BaseJsonParser.QUALIFIED_NAME, formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), tableMeta.getString("name")));
            if (existTable != null){
                return existTable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Referenceable table = fillTableInfo(tableMeta, project);
        String qualifiedName = (String)table.get(QUALIFIED_NAME);
        ParserFactory.getColumnParser().parse(context, tableMeta.getJSONArray("columns"), false, qualifiedName);
        table.set("fields", context.getValue(OdpsDataTypes.ODPS_COLUMN));
        ParserFactory.getColumnParser().parse(context, tableMeta.getJSONArray("partitionColumns"), true, qualifiedName);
        table.set("partitionKeys", context.getValue(OdpsDataTypes.ODPS_COLUMN));
        return table;
    }

    private Referenceable fillTableInfo(JSONObject tableMeta, Referenceable project){
        Referenceable table = newInstance(OdpsDataTypes.ODPS_TABLE);
        String tableName = tableMeta.getString("name");
        String qualifiedName = formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), tableName);
        table.set("name", tableName);
        setStringWithSameKey(table, tableMeta, "description");
        setStringWithSameKey(table, tableMeta, "owner");
        setStringWithSameKey(table, tableMeta, "description");
        setStringWithSameKey(table, tableMeta, "description");
        setLongWithSameKey(table, tableMeta, "lifeCycle");
        setLongWithSameKey(table, tableMeta, "createTime");
        setLongWithSameKey(table, tableMeta, "lastModifiedTime");
        table.set("isPartitioned", !tableMeta.getJSONArray("partitionColumns").isEmpty());
        table.set("database", project);
        table.set("metaSource", "ODPS");
        table.set(QUALIFIED_NAME, qualifiedName);
        BaseJsonParser.updateEntityMap(ENTITY_MAP_TYPE.PROJECT_TABLES, (String)project.get(QUALIFIED_NAME), qualifiedName);

        return table;
    }

    public List<Referenceable> createTableEntity(DDPClient ddpClient, String workspaceName, Referenceable projectRef, String tableName){
        String projectName = (String)projectRef.get(NAME);
        String dictionary = ddpClient.getDictionary(workspaceName, projectName, tableName);
        JSONObject dictionaryJson = JSON.parseObject(dictionary);
        JSONObject tableMeta = dictionaryJson.getJSONObject("tableMeta");
        JSONArray partitionMeta = dictionaryJson.getJSONArray("partitionMeta");
        Referenceable table = fillTableInfo(tableMeta, projectRef);
        List<Referenceable> partitions = new PartitionParser().createPartitions(partitionMeta, table);
        table.set("partitions", partitions);
        List<Referenceable> columns = new ColumnParser().createColumnEntities(tableMeta.getJSONArray("columns"), false, (String) table.get(QUALIFIED_NAME));
        List<Referenceable> partitionKeys = new ColumnParser().createColumnEntities(tableMeta.getJSONArray("partitionColumns"), false, (String) table.get(QUALIFIED_NAME));
        table.set("fields", columns);
        table.set("partitionKeys", partitionKeys);
        //return all entities
        List<Referenceable> entities = new ArrayList<>();
        entities.add(table);
        entities.addAll(partitions);
        return entities;
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_TABLE;
    }
}
