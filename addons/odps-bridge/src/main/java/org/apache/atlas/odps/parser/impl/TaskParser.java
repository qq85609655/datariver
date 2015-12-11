package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.common.exception.BridgeException;
import org.apache.atlas.common.model.LineageDataTypes;
import org.apache.atlas.common.model.TransformDataTypes;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 任务解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class TaskParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONArray tasksCmd = (JSONArray) data[1];
        String instanceId = (String) data[2];
        String workspaceName = data.length > 3 ? String.valueOf(data[3]) : null;
        //1.解析task 执行的SQL语句
        Map<String, String> nameCmdMap = getTaskIdCmdMap(tasksCmd);
        JSONArray tasksDetail = (JSONArray) data[0];
        List<Referenceable> tasks = new ArrayList<>();
        context.put(type(), tasks);
        for (int i = 0; i < tasksDetail.size(); i++) {
            JSONObject taskJson = tasksDetail.getJSONObject(i);
            if (taskJson.keySet().iterator().hasNext()) {
                //2.解析Task开始
                String taskId = taskJson.keySet().iterator().next();
                JSONObject mapReduceJson = taskJson.getJSONObject(taskId).getJSONObject("mapReduce");
                JSONObject jsonSummary = getJsonSummary(mapReduceJson);
                JSONObject jobtask = jsonSummary.getJSONArray("jobs").getJSONObject(0);
                Referenceable taskRef = newInstance(type());
                String taskQualifiedName = formatQualifiedName(instanceId, taskId);
                taskRef.set(QUALIFIED_NAME, taskQualifiedName);
                //类似于：dtdream_hanwen_20151023083523761go408qv5_LOT_0_0_0_job0
                String taskName = jobtask.getString("name");
                taskRef.set("name", taskName);
                //类似于：MRonSQL_144523723601604416757
                taskRef.set("id", taskId);
                //LOT即为MR任务
                taskRef.set("type", nameCmdMap.isEmpty() ? "LOT" : "SQL");
                taskRef.set("queryText", nameCmdMap.get(taskId));
                taskRef.set("status", ((Referenceable) context.getValue(OdpsDataTypes.ODPS_INSTANCE)).get("status"));
                taskRef.set("lineage", buildTaskLineageInfo(context, mapReduceJson, workspaceName, taskName));
                List<Referenceable> stepsReferenceables =
                        buildStepsReferenceables(context, jsonSummary, jobtask, taskQualifiedName, workspaceName);
                taskRef.set("steps", stepsReferenceables);
                List<Referenceable> stepsDAG = buildStepsDAGReferenceables(jobtask, stepsReferenceables);
                taskRef.set("stepsDAG", stepsDAG);
                tasks.add(taskRef);
            }
        }
    }

    /**
     * @param jobtask
     * @param stepsReferenceables
     * @return
     */
    private List<Referenceable> buildStepsDAGReferenceables(JSONObject jobtask, List<Referenceable> stepsReferenceables) {
        JSONObject tasks = jobtask.getJSONObject("tasks");
        Map<String, String> stepDagNames = new HashMap<>();
        for (Map.Entry<String, Object> entry : tasks.entrySet()) {
            String precedingStepName = entry.getKey();
            JSONObject aTaskJson = (JSONObject) entry.getValue();
            JSONObject output_record_counts = aTaskJson.getJSONObject("output_record_counts");
            Iterator<String> iterator = output_record_counts.keySet().iterator();
            if (iterator.hasNext()) {
                String succeedingStepName = iterator.next();
                stepDagNames.put(precedingStepName, succeedingStepName);
            }
        }
        Map<String, Referenceable> nameToStepMap = new HashedMap();
        for (Referenceable referenceable : stepsReferenceables) {
            nameToStepMap.put((String) referenceable.get("name"), referenceable);
        }
        List<Referenceable> stepsDAG = new ArrayList<>();
        for (String preceding : stepDagNames.keySet()) {
            String succceeding = stepDagNames.get(preceding);
            if (succceeding.startsWith(preceding)) {//输入，输出都是自己的忽略掉
                continue;
            }
            Referenceable dagRef = newInstance(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE);
            dagRef.set("preceding", getStepByName(preceding, nameToStepMap));
            dagRef.set("succeeding", getStepByName(succceeding, nameToStepMap));
            dagRef.set("kind", "next");
            stepsDAG.add(dagRef);
        }
        return stepsDAG;
    }

    private Referenceable getStepByName(String name, Map<String, Referenceable> map) {
        for (String key : map.keySet()) {
            if (name.startsWith(key)) {
                return map.get(key);
            }
        }
        throw new BridgeException("根据step名称查询step失败");
    }

    /**
     * 解析step名称和表名称，并存储在map中
     */
    class StepNameToTableName {
        public StepNameToTableName(JSONObject summaryJson) {
            JSONObject inputoutputJson = summaryJson.getJSONObject("input_output");
            for (Map.Entry<String, Object> input : inputoutputJson.getJSONObject("input").entrySet()) {
                inputStepNameAndTableMap.put(input.getKey(), String.valueOf(input.getValue()));
            }
            for (Map.Entry<String, Object> output : inputoutputJson.getJSONObject("output").entrySet()) {
                outputStepNameAndTableMap.put(output.getKey(), String.valueOf(output.getValue()));
            }
            //outputjson没有包含MR名称，需要额外处理
            Set<String> outputsName = new HashSet<>();
            JSONArray jobs = summaryJson.getJSONArray("jobs");
            for (int i = 0; i < jobs.size(); i++) {
                JSONObject job = jobs.getJSONObject(i);
                outputsName.addAll(job.getJSONObject("outputs").keySet());
            }
            //将没有MR名称的key替换为含有mr名称的
            Map<String, String> outputStepNameAndTable = new HashMap<>();
            for (String key : outputStepNameAndTableMap.keySet()) {
                for (String fullKey : outputsName) {
                    if (fullKey.contains(key)) {
                        outputStepNameAndTable.put(fullKey, outputStepNameAndTableMap.get(key));
                    }
                }
            }
            outputStepNameAndTableMap.clear();
            this.outputStepNameAndTableMap = outputStepNameAndTable;
        }

        private Map<String, String> inputStepNameAndTableMap = new HashMap<>();
        private Map<String, String> outputStepNameAndTableMap = new HashMap<>();

        public Map<String, String> getInputStepNameAndTableMap() {
            return inputStepNameAndTableMap;
        }

        public Map<String, String> getOutputStepNameAndTableMap() {
            return outputStepNameAndTableMap;
        }
    }

    /**
     * 解析step
     *
     * @param context
     * @param jobtask
     * @return
     * @throws AtlasException
     */
    private List<Referenceable> buildStepsReferenceables(ParserContext context, JSONObject jsonSummary,
                                                         JSONObject jobtask, String taskQualifiedName, String workspaceName) throws AtlasException {
        StepNameToTableName nameToTableNames = new StepNameToTableName(jsonSummary);
        Map<String, String> inputStepNameAndTableMap = nameToTableNames.getInputStepNameAndTableMap();
        Map<String, String> outputStepNameAndTableMap = nameToTableNames.getOutputStepNameAndTableMap();
        JSONObject tasks = jobtask.getJSONObject("tasks");
        List<Referenceable> steps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : tasks.entrySet()) {
            String stepName = entry.getKey();
            JSONObject aTaskJson = (JSONObject) entry.getValue();
            Referenceable stepRef = newInstance(TransformDataTypes.ETL_STEP_SUPER_TYPE);
            stepRef.set("name", stepName);
            stepRef.set(QUALIFIED_NAME, formatQualifiedName(taskQualifiedName, stepName));
            stepRef.set(META_SOURCE, META_SOURCE_VALUE);
            stepRef.set("description", buildStepDescription(aTaskJson));
            stepRef.set("type", isInput(inputStepNameAndTableMap, stepName) ? "ODPS_INPUT" : "ODPS_OUTPUT");
            Referenceable stepLineage = buildStepLineageInfo(context, inputStepNameAndTableMap, outputStepNameAndTableMap, stepName,workspaceName);
            stepRef.set("lineage", stepLineage);
            steps.add(stepRef);
        }
        return steps;
    }

    /**
     * 构造step血缘信息
     *
     * @param context
     * @param inputStepNameAndTableMap
     * @param outputStepNameAndTableMap
     * @param stepName
     * @return
     * @throws AtlasException
     */
    private Referenceable buildStepLineageInfo(ParserContext context, Map<String, String> inputStepNameAndTableMap, Map<String, String> outputStepNameAndTableMap, String stepName, String workspaceName) throws AtlasException {
        Referenceable stepLineage = newInstance(LineageDataTypes.LINEAGE_STEP_PROCESS_INFO);
        List<String> inputTableNames = findTablesOfStep(inputStepNameAndTableMap, stepName);
        List<String> outputTableNames = findTablesOfStep(outputStepNameAndTableMap, stepName);
        ParserFactory.getTableParser().parse(context, inputTableNames, workspaceName);
        Object inputTables = context.getValue(OdpsDataTypes.ODPS_TABLE);
        stepLineage.set("name", "Step: " + stepName);
        if (inputTables != null) {
            stepLineage.set("inputs", makeUniqueByName((List<Referenceable>) inputTables));
        }
        ParserFactory.getTableParser().parse(context, outputTableNames, workspaceName);
        Object outTables = context.getValue(OdpsDataTypes.ODPS_TABLE);
        if (outTables != null) {
            stepLineage.set("outputs", makeUniqueByName((List<Referenceable>) outTables));
        }
        return stepLineage;
    }

    /**
     * 构造Step描述信息
     *
     * @param aTaskJson
     * @return
     */
    private String buildStepDescription(JSONObject aTaskJson) {
        StringBuilder description = new StringBuilder();
        description.append("输入记录数:").append(aTaskJson.getJSONObject("input_record_counts"))
                .append("输出记录数：").append(aTaskJson.getJSONObject("output_record_counts"));
        return description.toString();
    }

    /**
     * 获取step的表名,用于构建表信息
     *
     * @param nameAndTableMap
     * @param stepName
     * @return
     */
    private List<String> findTablesOfStep(Map<String, String> nameAndTableMap, String stepName) {
        List<String> tableNames = new ArrayList<>();
        for (String key : nameAndTableMap.keySet()) {
            if (key.startsWith(stepName)) {
                tableNames.add(nameAndTableMap.get(key));
            }
        }
        return tableNames;
    }

    /**
     * 解析并获取task的ID和SQL语句
     *
     * @param tasksCmd
     * @return
     */
    private Map<String, String> getTaskIdCmdMap(JSONArray tasksCmd) {
        Map<String, String> nameCmdMap = new HashMap<>();
        for (int i = 0; i < tasksCmd.size(); i++) {
            JSONObject cmd = tasksCmd.getJSONObject(i);
            for (Map.Entry<String, Object> en : cmd.entrySet()) {
                String value = (String) en.getValue();
                if (StringUtils.isNotEmpty(value)) {
                    nameCmdMap.put(en.getKey(), value);
                }
            }
        }
        return nameCmdMap;
    }

    /**
     * 判断当前step是输入还是输出
     *
     * @param inputStepNameAndTableMap
     * @param stepName
     * @return
     */
    private boolean isInput(Map<String, String> inputStepNameAndTableMap, String stepName) {
        boolean isInput = false;
        for (String key : inputStepNameAndTableMap.keySet()) {
            if (key.startsWith(stepName)) {
                isInput = true;
                break;
            }
        }
        return isInput;
    }

    /**
     * 构建task血缘信息
     *
     * @param context
     * @param mapReduceJson
     * @return
     * @throws AtlasException
     */
    private Referenceable buildTaskLineageInfo(ParserContext context, JSONObject mapReduceJson,
                                               String workspaceName, String taskName) throws AtlasException {
        JSONObject summJson = getJsonSummary(mapReduceJson);
        JSONObject inputOutputJson = summJson.getJSONObject("input_output");
        JSONObject input = inputOutputJson.getJSONObject("input");
        JSONObject output = inputOutputJson.getJSONObject("output");
        List<String> inputTableNames = new ArrayList<>();
        List<String> outputTableNames = new ArrayList<>();
        for (Map.Entry entry : input.entrySet()) {
            inputTableNames.add((String) entry.getValue());
        }
        for (Map.Entry entry : output.entrySet()) {
            outputTableNames.add((String) entry.getValue());
        }
        ParserFactory.getTableParser().parse(context, inputTableNames, workspaceName, context.inputTables, context.inputDbs);
        List<Referenceable> inputs = makeUniqueByName((List<Referenceable>) context.getValue(OdpsDataTypes.ODPS_TABLE));

        ParserFactory.getTableParser().parse(context, outputTableNames, workspaceName, context.outputDbs, context.outputDbs);
        List<Referenceable> outputs = makeUniqueByName((List<Referenceable>) context.getValue(OdpsDataTypes.ODPS_TABLE));
        Referenceable lineageProcessInfo = newInstance(LineageDataTypes.LINEAGE_TASK_PROCESS_INFO);
        lineageProcessInfo.set("name", "Task: " + taskName);
        if (inputs != null) {
            lineageProcessInfo.set("inputs", inputs);
        }
        if (outputs != null) {
            lineageProcessInfo.set("outputs", outputs);
        }
        return lineageProcessInfo;
    }

    /**
     * 获取jsonSummary信息
     *
     * @param mapReduceJson
     * @return
     */
    private JSONObject getJsonSummary(JSONObject mapReduceJson) {
        String jsonSummary = mapReduceJson.getString("jsonSummary");
        return JSON.parseObject(jsonSummary.replaceAll("\\\"", "\""));
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_TASK;
    }
}
