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

package org.apache.atlas.dxt.examples;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.dxt.model.DxtDataModelGenerator;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.store.DxtDictionaryStore;
import org.apache.atlas.dxt.store.DxtStepStoreFactory;
import org.apache.atlas.dxt.store.IDxtTransStepStore;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DXT元模型和元数据的demo，由import-demo.sh调用
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/5 9:35
 */
public class DxtQuickStart {
    private static final Logger LOG = LoggerFactory.getLogger(DxtQuickStart.class);

    private boolean isFormIT;
    private AtlasClient dgiClient;
    private static final String[] TYPES = {"DataContainer", "TransInstance", "LineageDependency", "ETLTask",
        "ETLTaskType", "ETLInstanceStatus", "DataTable", "LineageFieldMap",
        "TransStep", "LineageFieldItem", "DataContainerStatus", "ETLStep",
        "LineageDependType", "DataElement", "GeneralAccInfo", "ETLStepType",
        "ETLInstance", "DBAccess", "AtlasMetaSourceType", "ETLStepSequence",
        "AbstractProcess", "ETLTaskStatus", "LineageProcessInfo", "DataField",
        "DataContainerType"};

    public DxtQuickStart(boolean isFormIT) {
        this.isFormIT = isFormIT;
        this.dgiClient = new AtlasClient("http://localhost:21000/");
    }

    public DxtQuickStart() {
        this(false);
    }

    public static void main(String[] argv) throws Exception {
        DxtQuickStart dxtQuickStart = new DxtQuickStart();

        dxtQuickStart.createTypes();

        dxtQuickStart.createEntities();

        dxtQuickStart.search();
    }

    private void createTypes() throws Exception {
        //注册super class的元模型
        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();

        //注册DXT的元模型
        DxtDataModelGenerator dataModelGenerator = new DxtDataModelGenerator();

        try {
            dgiClient.getType(DxtDataTypes.GENERAL_ACCINFO.getValue());
            System.out.println("Dxt data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            System.out.println("Registering Dxt data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }

        verifyTypesCreated();
    }

    private void verifyTypesCreated() throws Exception {
        List<String> types = dgiClient.listTypes();
        for (String type : TYPES) {
            assert types.contains(type);
        }
    }

    public void createEntities() throws Exception {
        String transJson = loadResource("dxt_trans.json");
        createEntities("1234", "DXT-1", "e68192e8-39c3-4f65-b0da-15c833259326", transJson, "hangzhou");
    }

    public void createEntities(String actionGuid, String instanceId, String configId, String dxtJson,
                               String workspaceName) throws Exception {
        JSONObject jsonObject = new JSONObject(dxtJson);
        JSONObject transitionObj = jsonObject.getJSONObject("transition");
        transitionObj.put("id", configId);
        JSONObject stepObjs = transitionObj.getJSONObject("steps");

        List<Referenceable> entities = new ArrayList<>(); //需要发送给atlas保存的元数据信息
        Map<String, IDxtTransStepStore> stepStoreMap = new HashMap<>();

        Iterator keys = stepObjs.keys();

        System.out.println("Importing Dxt demo meta data.");

        //创建TransStep
        while (keys.hasNext()) {
            String stepType = (String) keys.next();
            JSONArray stepArray = stepObjs.getJSONArray(stepType);

            for (int i = 0; i < stepArray.length(); i++) {
                JSONObject oneStepObj = stepArray.getJSONObject(i);
                String stepName = oneStepObj.getString("name");

                //每种step类型的构造方式不同，需要由各自的实现类来完成构造动作。
                IDxtTransStepStore stepInfoStore = DxtStepStoreFactory.getStepStore(stepType);
                if (stepInfoStore instanceof DxtDemoInputStepStore) {
                    ((DxtDemoInputStepStore) stepInfoStore).setFormIT(this.isFormIT);
                }
                //必须先初始化
                stepInfoStore.initialize(transitionObj, oneStepObj, workspaceName, instanceId);

                stepStoreMap.put(stepName, stepInfoStore);
                stepInfoStore.createStepRef();
            }
        }

        //增加TransStep的血缘关系，只有在workStream中出现的step才有血缘关系
        String workStream = transitionObj.getString("workStream");
        if ((null == workStream) || "".equals(workStream)) {
            throw new Exception("Failed to parse workStream.");
        }

        String[] stepSequence = workStream.split("->");
        for (String stepName : stepSequence) {
            IDxtTransStepStore stepStore = stepStoreMap.get(stepName);
            if (null == stepStore) {
                throw new Exception("Step(" + stepName + ") in workstream does not exist.");
            }

            stepStore.createLineageRef(stepStoreMap);
        }

        for (Map.Entry<String, IDxtTransStepStore> storeEntry : stepStoreMap.entrySet()) {
            IDxtTransStepStore stepStore = storeEntry.getValue();

            entities.add(stepStore.getContainerRef());
            if (null != stepStore.getLineageRef()) {
                entities.add(stepStore.getLineageRef());
            }
            entities.add(stepStore.getStepRef());
        }

        //创建TransInstance
        DxtDemoInstanceStore instanceStore = new DxtDemoInstanceStore(transitionObj, stepStoreMap, instanceId,
            workspaceName);
        instanceStore.setFormIT(this.isFormIT);
        instanceStore.createTransInstanceRef();

        entities.add(instanceStore.getTransInstanceRef());

        JSONArray guids = createEntities(entities);

        if (!isFormIT) {
            //刷新每个step的container的tables信息
            new DxtDictionaryStore(stepStoreMap, dgiClient).refreshContainerTables();
        }
    }

    private JSONArray createEntities(List<Referenceable> entities) throws Exception {
        JSONArray entitiesArray = new JSONArray();
        for (Referenceable entity : entities) {
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
        }

        return dgiClient.createEntity(entitiesArray);
    }

    private String[] getDSLQueries() {
        return new String[]{"TransStep", "TransInstance", "GeneralAccInfo", "DataContainer", "DataTable",
            "LineageProcessInfo",};
    }

    private void search() throws Exception {
        for (String dslQuery : getDSLQueries()) {
            JSONArray results = dgiClient.search(dslQuery);
            if (results != null) {
                System.out.println("query [" + dslQuery + "] returned [" + results.length() + "] rows");
            } else {
                System.out.println("query [" + dslQuery + "] failed.");
            }
        }
    }

    private String loadResource(String fileName) throws Exception {
        String resourceName;
        if (isFormIT) {
            resourceName = this.getClass().getResource("/").getPath() + "/" + fileName;
        } else {
            resourceName = System.getProperty("atlas.home") + "/examples/" + fileName;
        }

        return FileUtil.loadResourceFile(resourceName);
    }
}
