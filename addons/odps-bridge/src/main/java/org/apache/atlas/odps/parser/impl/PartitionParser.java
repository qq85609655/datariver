package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;

import java.util.*;

/**
 * 分区解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class PartitionParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONArray partJsonArr = (JSONArray) data[0];
        Referenceable table = (Referenceable) data[1];
        List<Referenceable> partitions = createPartitions(partJsonArr, table);
        context.put(type(), addCurrentTypeListReferenceables(context, partitions));
    }

    public List<Referenceable> createPartitions(JSONArray partitionArray, Referenceable table){
        Iterator<Object> iterator = partitionArray.iterator();
        List<Referenceable> partitions = new ArrayList<>();
        String partitionQualifiedName = null;
        Referenceable currentEntity = null;
        while (iterator.hasNext()) {
            currentEntity = newInstance(type());
            JSONObject partJson = (JSONObject) iterator.next();
            partitionQualifiedName = formatQualifiedName(String.valueOf(table.get(QUALIFIED_NAME)),
                partJson.getString("name"));
            Referenceable existPartition = null;
            try {
                existPartition = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_PARTITION.getValue(),
                    BaseJsonParser.QUALIFIED_NAME, partitionQualifiedName);
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
            if (existPartition != null){
                continue;
            }
            if (!getPartitionRefe().containsKey(partitionQualifiedName)){
                setStringWithSameKey(currentEntity, partJson, "name");
                setLongWithSameKey(currentEntity, partJson, "createTime");
                setLongWithSameKey(currentEntity, partJson, "lastModifiedTime");
                setLongWithSameKey(currentEntity, partJson, "numRecord");
                currentEntity.set("table", table);
                currentEntity.set("partitionKeys", table.get("partitionKeys"));
                currentEntity.set(QUALIFIED_NAME, partitionQualifiedName);
                currentEntity.set("metaSource", "ODPS");
                BaseJsonParser.updateEntityMap(ENTITY_MAP_TYPE.TABLE_PARTITIONS, (String)table.get(QUALIFIED_NAME), partitionQualifiedName);
                partitions.add(currentEntity);
            }
        }
        return partitions;
    }

    public Referenceable createOnePartition(Referenceable table, JSONObject data){
        String partitionName = data.getJSONObject("data").getString("partitions");
        String partitionQualifiedName = formatQualifiedName(String.valueOf(table.get(QUALIFIED_NAME)), partitionName);
        Referenceable existPartition = null;
        try {
            existPartition = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_PARTITION.getValue(),
                BaseJsonParser.QUALIFIED_NAME, partitionQualifiedName);
        } catch (AtlasServiceException e) {
            e.printStackTrace();
        }
        if (existPartition != null){
            return  null;
        }
        Referenceable partition = newInstance(OdpsDataTypes.ODPS_PARTITION);
        partition.set("table", table);
        partition.set("partitionKeys", table.get("partitionKeys"));
        partition.set(QUALIFIED_NAME, partitionQualifiedName);
        partition.set("metaSource", "ODPS");
        partition.set("createTime", data.getLong("time"));
        partition.set("lastModifiedTime", data.getLong("time"));
        partition.set("name", partitionName);

        return partition;
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_PARTITION;
    }
}
