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
import org.apache.atlas.common.model.TransformDataTypes;
import org.apache.atlas.dxt.client.DxtClientUtil;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.typesystem.Referenceable;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 创建TransInstance
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtTransInstanceStore {
    protected JSONObject transitionObj;
    protected String instanceId;
    protected Map<String, IDxtTransStepStore> stepStoreMap;
    protected String[] stepSequence;
    protected List<Referenceable> inputTables;
    protected List<Referenceable> outputTables;
    protected List<Referenceable> inputDbs;
    protected List<Referenceable> outputDbs;

    protected Referenceable transInstanceRef;
    private String workspaceName = null;
    protected static final Logger LOG = LoggerFactory.getLogger(DxtTransInstanceStore.class);

    public Referenceable getTransInstanceRef() {
        return transInstanceRef;
    }

    public DxtTransInstanceStore(JSONObject transitionObj, Map<String, IDxtTransStepStore> stepStoreMap,
                                 String instanceId, String workspaceName) {
        this.transitionObj = transitionObj;
        this.stepStoreMap = stepStoreMap;
        this.instanceId = instanceId;
        this.workspaceName = workspaceName;

        inputTables = new ArrayList<>();
        outputTables = new ArrayList<>();
        inputDbs = new ArrayList<>();
        outputDbs = new ArrayList<>();

        try {
            String workStream = transitionObj.getString("workStream");
            if ((null != workStream) && !"".equals(workStream)) {
                stepSequence = workStream.split("->");
            }
        } catch (Exception e) {
            LOG.error("Failed to parse workStream.");
        }
    }

    /**
     * 目前字段映射是按顺序映射.
     */
    protected Referenceable getLineageRef() throws Exception {
        if ((null == stepSequence) || (stepSequence.length < 2)) {
            //少于两个step，没有字段血缘关系
            return null;
        }

        Referenceable lineageRef = new Referenceable(LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue());
        IDxtTransStepStore firstStepStore = stepStoreMap.get(stepSequence[0]);
        IDxtTransStepStore lastStepStore = stepStoreMap.get(stepSequence[stepSequence.length - 1]);
        List<Referenceable> fieldMapsRef = new ArrayList<>();

        String transName = transitionObj.getString("name");
        for (int i = 0; i < firstStepStore.getMappingFields().size(); i++) {
            Referenceable dependencyRef = new Referenceable(LineageDataTypes.LINEAGE_DEPENDENCY.getValue());
            dependencyRef.set("dependencyType", "SIMPLE");
            dependencyRef.set("function", "equal");
            dependencyRef.set("description", "Trans(" + transName + "): eventually become");

            Referenceable lineageFieldMapRef = new Referenceable(LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue());
            lineageFieldMapRef.set("dependencyInfo", dependencyRef);

            List<Referenceable> sourceFieldRefs = new ArrayList<>();
            String fieldName = firstStepStore.getMappingFields().get(i);
            sourceFieldRefs.add(firstStepStore.getFieldItemRef(fieldName));
            lineageFieldMapRef.set("sourceFields", sourceFieldRefs);

            String lastStepFieldName = lastStepStore.getMappingFields().get(i);
            Referenceable targetFieldRef = lastStepStore.getFieldItemRef(lastStepFieldName);
            lineageFieldMapRef.set("targetField", targetFieldRef);

            fieldMapsRef.add(lineageFieldMapRef);
        }
        lineageRef.set("name", "Trans: " + transName);
        lineageRef.set("fieldMaps", fieldMapsRef);

        //table血缘关系
        inputTables.add(firstStepStore.getDataTableRef());
        lineageRef.set("inputs", inputTables);

        outputTables.add(lastStepStore.getDataTableRef());
        lineageRef.set("outputs", outputTables);

        //DB血缘关系
        inputDbs.add(firstStepStore.getContainerRef());
        lineageRef.set("inputDbs", inputDbs);

        outputDbs = getOutputDbsRef(inputDbs, lastStepStore.getContainerRef());
        lineageRef.set("outputDbs", outputDbs);

        return lineageRef;
    }

    /**
     * 获取outputDbs，如果已经在inputDbs里存在，则不能添加，避免成环。
     * 目前outputDbs中只有一个container
     *
     * @param inputDbsRef  inputDbs
     * @param containerRef containerRef to be added in outputDbs
     * @return List<Referenceable>
     */
    protected List<Referenceable> getOutputDbsRef(List<Referenceable> inputDbsRef, Referenceable containerRef) {
        List<Referenceable> outputDbsRef = new ArrayList<>();

        boolean existInInputDbs = false;
        for (Referenceable ref : inputDbsRef) {
            if (containerRef.get("qualifiedName").equals(ref.get("qualifiedName"))) {
                existInInputDbs = true;
            }
        }

        if (!existInInputDbs) {
            outputDbsRef.add(containerRef);
        }
        return outputDbsRef;
    }

    protected List<Referenceable> getStepsRef() {
        List<Referenceable> stepRefList = new ArrayList<>();
        for (Map.Entry<String, IDxtTransStepStore> storeEntry : stepStoreMap.entrySet()) {
            stepRefList.add(storeEntry.getValue().getStepRef());
        }

        return stepRefList;
    }

    protected List<Referenceable> getStepDAGRef() throws Exception {
        if ((null == stepSequence) || (stepSequence.length < 2)) {
            return null;
        }

        List<Referenceable> stepRefList = new ArrayList<>();

        for (int i = 0; i < stepSequence.length; i++) {
            if (i <= stepSequence.length - 2) {
                Referenceable ref = new Referenceable(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue());
                ref.set("preceding", stepStoreMap.get(stepSequence[i]).getStepRef());
                ref.set("succeeding", stepStoreMap.get(stepSequence[i + 1]).getStepRef());
                ref.set("kind", "next");

                stepRefList.add(ref);
            }
        }

        return stepRefList;
    }

    protected List<Referenceable> getTaskListRef() throws Exception {
        String transId = transitionObj.getString("id");

        String qualifiedName = DxtStoreUtil.formatQualifiedName(transId, instanceId);
        Referenceable taskRef = DxtStoreUtil.createDxtRef(TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue(),
            qualifiedName, DxtConstant.TASK_TRAIT);

        taskRef.set("name", transitionObj.getString("name"));
        taskRef.set("id", instanceId);
        taskRef.set("lineage", getLineageRef());
        taskRef.set("steps", getStepsRef());
        taskRef.set("stepsDAG", getStepDAGRef());
        taskRef.set("type", "SQL");
        //todo: 需要根据实际情况设置
        taskRef.set("status", "SUCCESS");

        List<Referenceable> taskListRef = new ArrayList<>();
        taskListRef.add(taskRef);

        return taskListRef;
    }

    protected JSONObject getHisLogObj(String transUid) throws Exception {
        Map historicalLogs = new DxtClientUtil(this.workspaceName).getTransHisLogs(transUid);
        JSONObject hisLogObj = new JSONObject(historicalLogs).getJSONObject("statistics");

        return hisLogObj;
    }

    public Referenceable createTransInstanceRef() throws Exception {
        String transId = transitionObj.getString("id");

        transInstanceRef = DxtStoreUtil.createDxtRef(DxtDataTypes.TRANS_INSTANCE.getValue(), transId,
            DxtConstant.TRANS_TRAIT);
        transInstanceRef.set("name", transitionObj.getString("name"));
        transInstanceRef.set("id", transId);
        transInstanceRef.set("tasks", getTaskListRef());
        transInstanceRef.set("status", "TERMINATED");

        JSONObject hisLogObj;
        try {
            hisLogObj = getHisLogObj(transId);
        } catch (Exception e) {
            LOG.info("No historical log exists for trans: " + transId + ".");
            return transInstanceRef;
        }
        Date lastExecuteDate = DxtStoreUtil.formatDateFromStr(hisLogObj.getString("latestTime"));
        transInstanceRef.set("lastExecuteTime", lastExecuteDate.getTime());
        transInstanceRef.set("runTimes", hisLogObj.getInt("totalCount"));
        transInstanceRef.set("successTimes", hisLogObj.getInt("success"));
        transInstanceRef.set("failedTimes", hisLogObj.getInt("failed"));

        return transInstanceRef;
    }

    public List<Referenceable> getInputTables() {
        return inputTables;
    }

    public List<Referenceable> getOutputTables() {
        return outputTables;
    }

    public List<Referenceable> getInputDbs() {
        return inputDbs;
    }

    public List<Referenceable> getOutputDbs() {
        return outputDbs;
    }
}
