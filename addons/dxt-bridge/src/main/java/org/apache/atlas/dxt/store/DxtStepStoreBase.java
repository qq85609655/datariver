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

import org.apache.atlas.common.model.LineageDataTypes;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.dxt.client.DxtClientUtil;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.typesystem.Referenceable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * step元数据保存的基类，其余各种step都需要继承该基类
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public abstract class DxtStepStoreBase implements IDxtTransStepStore {
    protected JSONObject transitionObj;
    protected JSONObject stepObj;
    protected String transId;
    protected String instanceId;
    protected String stepName;
    protected String dbId;
    protected String qualifiedDbName; //创建container后才初始化
    protected String tableName;
    protected String workspaceName;
    protected String[] stepSequence;

    protected Referenceable containerRef;
    protected Referenceable accInfoRef;
    protected Referenceable dataTableRef;
    protected List<Referenceable> dataFieldRefs;
    protected Map<String, Referenceable> fieldsMap;
    protected Referenceable stepRef;
    protected Referenceable lineageRef;
    protected List<String> mappingFields;

    protected abstract void createContainerRef() throws Exception;

    protected abstract Referenceable createAccInfoRef(JSONObject dbConnObj, JSONObject dbInfoObj) throws Exception;

    protected abstract void setDbId() throws Exception;

    protected abstract void setTableName() throws Exception;

    protected abstract void setMappingFields() throws Exception;

    protected abstract String getStepType();

    @Override
    public void initialize(JSONObject transitionObj, JSONObject stepObj, String workspaceName,
                           String instanceId) throws Exception {
        this.transitionObj = transitionObj;
        this.stepObj = stepObj;
        this.workspaceName = workspaceName;
        this.instanceId = instanceId;
        this.transId = transitionObj.getString("id");
        this.stepName = stepObj.getString("name");
        this.stepSequence = transitionObj.getString("workStream").split("->");
        setDbId();
        setTableName();
        setMappingFields();

        dataFieldRefs = new ArrayList<>();
        fieldsMap = new HashMap<>();

        createContainerRef();
        createDataTableRef();
    }

    public Referenceable getContainerRef() {
        return containerRef;
    }

    public List<String> getMappingFields() {
        return mappingFields;
    }

    public String getDbId() {
        return dbId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getQualifiedDbName() {
        return qualifiedDbName;
    }

    public void setQualifiedDbName(String qualifiedDbName) {
        this.qualifiedDbName = qualifiedDbName;
    }

    public Referenceable getStepRef() {
        return stepRef;
    }

    public Referenceable getLineageRef() {
        return lineageRef;
    }

    public Referenceable getDataTableRef() {
        return dataTableRef;
    }

    @Override
    public JSONArray getTableArray() throws Exception {
        JSONObject tablesObj = new JSONObject(new DxtClientUtil(this.workspaceName).getTables(getDbId()));
        return tablesObj.getJSONArray("tables");
    }

    @Override
    public JSONArray getFieldObjArray(String tableName) throws Exception {
        Map fieldsInfo = new DxtClientUtil(workspaceName).getColumns(dbId, tableName);
        JSONObject fieldsObj = new JSONObject(fieldsInfo);

        return fieldsObj.getJSONArray("columns");
    }

    public Referenceable getFieldItemRef(String fieldName) {
        Referenceable fieldItemRef = null;
        if (null != fieldsMap) {
            fieldItemRef = fieldsMap.get(fieldName);
        }

        return fieldItemRef;
    }

    protected IDxtTransStepStore getNextStepStore(Map<String, IDxtTransStepStore> stepStoreMap) throws Exception {
        for (int i = 0; i < stepSequence.length; i++) {
            if ((i != (stepSequence.length - 1)) && stepSequence[i].equals(stepName)) {
                return stepStoreMap.get(stepSequence[i + 1]);
            }
        }

        return null;
    }

    protected void createDataFieldRefs() throws Exception {
        JSONArray fieldObjs = getFieldObjArray(tableName);

        for (int i = 0; i < fieldObjs.length(); i++) {
            JSONObject fieldObj = fieldObjs.getJSONObject(i);
            String fieldName = fieldObj.getString("name").trim();

            String qualifiedName = DxtStoreUtil.formatQualifiedName(getQualifiedDbName(), tableName, fieldName);
            Referenceable fieldRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(),
                qualifiedName, DxtConstant.FIELD_TRAIT, getStepTraitName());
            fieldRef.set("name", fieldName);
            fieldRef.set("dataType", fieldObj.getString("type"));
            dataFieldRefs.add(fieldRef);

            fieldsMap.put(fieldName, fieldRef);
        }
    }

    /**
     * 构造DataTable reference，必须在createContainerRef之后调用.
     */
    protected void createDataTableRef() throws Exception {
        String qualifiedName = DxtStoreUtil.formatQualifiedName(qualifiedDbName, tableName);
        dataTableRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(),
            qualifiedName, DxtConstant.TABLE_TRAIT, getStepTraitName());
        dataTableRef.set("name", tableName);
        dataTableRef.set("database", containerRef);

        createDataFieldRefs();
        dataTableRef.set("fields", dataFieldRefs);
    }

    @Override
    public void createStepRef() throws Exception {
        String qualifiedName = DxtStoreUtil.formatQualifiedName(transId, instanceId, stepName);
        stepRef = DxtStoreUtil.createDxtRef(DxtDataTypes.TRANS_STEP.getValue(), qualifiedName, DxtConstant.STEP_TRAIT);

        stepRef.set("name", stepObj.getString("name"));
        stepRef.set("type", getStepType());
        stepRef.set("queryText", stepObj.toString());
        stepRef.set("db", containerRef);
    }

    protected List<Referenceable> getFieldMapsRef(Map<String, IDxtTransStepStore> stepStoreMap) throws Exception {
        List<Referenceable> fieldMapsRef = null;

        IDxtTransStepStore nextStepStore = getNextStepStore(stepStoreMap);
        if (null != nextStepStore) {
            fieldMapsRef = new ArrayList<>();
            for (int i = 0; i < mappingFields.size(); i++) {
                Referenceable dependencyRef = new Referenceable(LineageDataTypes.LINEAGE_DEPENDENCY.getValue());
                dependencyRef.set("dependencyType", "SIMPLE");
                dependencyRef.set("function", "equal");
                dependencyRef.set("description", "Step(" + stepName + "): directly into");

                Referenceable lineageFieldMapRef =
                    new Referenceable(LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue());
                lineageFieldMapRef.set("dependencyInfo", dependencyRef);

                String fieldName = mappingFields.get(i);
                lineageFieldMapRef.set("sourceFields", getSourceFieldRefs(fieldName));
                lineageFieldMapRef.set("targetField", getTargetFieldRef(nextStepStore, fieldName, i));

                fieldMapsRef.add(lineageFieldMapRef);
            }
        }

        return fieldMapsRef;
    }

    @Override
    public void createLineageRef(Map<String, IDxtTransStepStore> stepStoreMap) throws Exception {
        lineageRef = new Referenceable(LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue());

        lineageRef.set("name", "Step: " + stepName);
        lineageRef.set("fieldMaps", getFieldMapsRef(stepStoreMap));
        lineageRef.set("inputs", getInputsRef());
        lineageRef.set("outputs", getOutputsRef());
        lineageRef.set("inputDbs", getInputDbsRef());
        lineageRef.set("outputDbs", getOutputDbsRef());

        stepRef.set("lineage", lineageRef);
    }

    /**
     * sourceFied就是自己.
     *
     * @param fieldName 当前field名称
     * @return
     * @throws Exception
     */
    protected List<Referenceable> getSourceFieldRefs(String fieldName) throws Exception {
        List<Referenceable> sourceFieldRefs = new ArrayList<>();

        sourceFieldRefs.add(getFieldItemRef(fieldName));

        return sourceFieldRefs;
    }

    /**
     * 目前是顺序映射，nextStep的对应位置的field就是target.
     *
     * @param nextStepStore 下一个step的信息
     * @param fieldName     列名称
     * @param fieldPos      列的映射位置
     * @return Referenceable
     * @throws Exception
     */
    protected Referenceable getTargetFieldRef(IDxtTransStepStore nextStepStore, String fieldName,
                                              int fieldPos) throws Exception {
        String targetFieldName = nextStepStore.getMappingFields().get(fieldPos);
        Referenceable fieldItemRef = nextStepStore.getFieldItemRef(targetFieldName);
        if (null == fieldItemRef) {
            throw new Exception("Failed to find lineage_field_item for " + fieldName + ".");
        }

        return fieldItemRef;
    }

    protected List<Referenceable> getInputsRef() {
        List<Referenceable> inputsRef = null;

        //第一个step才有inputs
        if (stepName.equals(stepSequence[0])) {
            inputsRef = new ArrayList<>();
            inputsRef.add(dataTableRef);
        }

        return inputsRef;
    }

    protected List<Referenceable> getOutputsRef() {
        List<Referenceable> outputsRef = null;

        //目前最后一个step才有outputs
        if (stepName.equals(stepSequence[stepSequence.length - 1])) {
            outputsRef = new ArrayList<>();
            outputsRef.add(dataTableRef);
        }

        return outputsRef;
    }

    protected List<Referenceable> getInputDbsRef() {
        List<Referenceable> inputDbsRef = null;

        //第一个step才有inputDbs
        if (stepName.equals(stepSequence[0])) {
            inputDbsRef = new ArrayList<>();
            inputDbsRef.add(containerRef);
        }

        return inputDbsRef;
    }

    protected List<Referenceable> getOutputDbsRef() {
        List<Referenceable> outputDbsRef = null;

        //目前最后一个step才有outputDbs
        if (stepName.equals(stepSequence[stepSequence.length - 1])) {
            outputDbsRef = new ArrayList<>();
            outputDbsRef.add(containerRef);
        }

        return outputDbsRef;
    }
}
