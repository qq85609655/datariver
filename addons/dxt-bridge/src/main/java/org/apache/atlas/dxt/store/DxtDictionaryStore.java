/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.dxt.store;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.typesystem.persistence.Id;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DXT涉及到的数据库的数据字典的元数据保存
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 14:25
 */
public class DxtDictionaryStore {
    private Map<String, IDxtTransStepStore> stepStoreMap;
    private Map<String, String> containerTablesMap;
    private final AtlasClient atlasClient;
    private static final Logger LOG = LoggerFactory.getLogger(DxtDictionaryStore.class);

    public DxtDictionaryStore(Map<String, IDxtTransStepStore> stepStoreMap, AtlasClient atlasClient) {
        this.stepStoreMap = stepStoreMap;
        this.containerTablesMap = new HashMap<>();
        this.atlasClient = atlasClient;
    }

    /**
     * 填充每个step中的container的tables信息.
     */
    public void refreshContainerTables() throws Exception {
        for (Map.Entry<String, IDxtTransStepStore> entry : stepStoreMap.entrySet()) {
            //HDFS无法遍历所有table，需要跳过
            if (entry instanceof DxtHdfsInputStepStore) {
                continue;
            }

            IDxtTransStepStore stepStore = entry.getValue();
            String tableIds = getTableRefs(stepStore);
            containerTablesMap.put(stepStore.getDbId(), tableIds);

            updateContainerTables(stepStore, tableIds);
        }
    }

    private void updateContainerTables(IDxtTransStepStore stepStore, String tableIds) throws Exception {
        String qualifiedName = (String) stepStore.getContainerRef().get("qualifiedName");
        Referenceable containerRef = atlasClient.getEntity("DataContainer", "qualifiedName", qualifiedName);

        if (null != containerRef) {
            atlasClient.updateEntityAttribute(containerRef.getId()._getId(), "tables", tableIds);
        } else {
            LOG.info("Failed to find container by qualifiedName: " + qualifiedName);
        }
    }

    private String getTableRefs(IDxtTransStepStore stepStore) throws Exception {
        String tableIds = containerTablesMap.get(stepStore.getDbId());
        //已经获取过数据字典信息的连接可以直接用已经创建过的tables信息
        if (null != tableIds) {
            return tableIds;
        }

        List<Referenceable> entities = new ArrayList<>();
        entities.add(stepStore.getContainerRef());

        JSONArray tableArray = stepStore.getTableArray();
        for (int i = 0; i < tableArray.length(); i++) {
            String tableName = tableArray.getString(i);

            //step中指定的table已经创建了元数据，无需再次创建
            Referenceable ref;
            if (!tableName.equals(stepStore.getTableName())) {
                ref = createDataTableRef(stepStore, tableArray.getString(i));
            } else {
                ref = stepStore.getDataTableRef();
            }

            entities.add(ref);
        }

        JSONArray guids = createEntities(entities);
        JSONArray idArray = new JSONArray();
        for (int i = 1; i < guids.length(); i++) {
            Id tableId = new Id(guids.getString(i), 0, "DataTable");
            idArray.put(InstanceSerialization.toJson(tableId, true));
        }

        return idArray.toString();
    }

    private Referenceable createDataTableRef(IDxtTransStepStore stepStore, String tableName) throws Exception {
        String qualifiedName = DxtStoreUtil.formatQualifiedName(stepStore.getQualifiedDbName(), tableName);
        Referenceable dataTableRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(),
            qualifiedName, DxtConstant.TABLE_TRAIT, stepStore.getStepTraitName());

        dataTableRef.set("name", tableName);
        dataTableRef.set("database", stepStore.getContainerRef());

        List<Referenceable> dataFieldRefs = getDataFieldRefs(stepStore, tableName);
        dataTableRef.set("fields", dataFieldRefs);

        return dataTableRef;
    }

    private List<Referenceable> getDataFieldRefs(IDxtTransStepStore stepStore, String tableName) throws Exception {
        JSONArray fieldObjs = stepStore.getFieldObjArray(tableName);

        List<Referenceable> dataFieldRefs = new ArrayList<>();
        for (int i = 0; i < fieldObjs.length(); i++) {
            JSONObject fieldObj = fieldObjs.getJSONObject(i);
            String fieldName = fieldObj.getString("name").trim();

            String qualifiedName = DxtStoreUtil.formatQualifiedName(stepStore.getQualifiedDbName(), tableName,
                fieldName);
            Referenceable fieldRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(),
                qualifiedName, DxtConstant.FIELD_TRAIT, stepStore.getStepTraitName());
            fieldRef.set("name", fieldName);
            fieldRef.set("dataType", fieldObj.getString("type"));
            dataFieldRefs.add(fieldRef);
        }

        return dataFieldRefs;
    }

    private JSONArray createEntities(List<Referenceable> entities) throws Exception {
        JSONArray entitiesArray = new JSONArray();
        for (Referenceable entity : entities) {
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
        }

        return atlasClient.createEntity(entitiesArray);
    }
}
