package org.apache.atlas.workFlow.handler;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.model.LineageDataTypes;
import org.apache.atlas.common.util.LineageHandler;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.codehaus.jettison.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/3 11:28
 */
public class WorkflowLineageHandler implements LineageHandler {
    private String workTemplateId;
    private String workTemplateName;
    private int actionCount;
    private int processedActionNum;
    private AtlasClient atlasClient;

    private Map<String, Referenceable> inputTablesMap;
    private Map<String, Referenceable> outputTablesMap;
    private Map<String, Referenceable> inputDbsMap;
    private Map<String, Referenceable> outputDbsMap;

    /**
     * 构造workflowTemplate的血缘处理对象.
     *
     * @param workTemplateId   workflowTemplate的元数据id
     * @param workTemplateName workflowTemplate的qualifiedName
     * @param actionCount      workflow中包含的action的数量
     */
    public WorkflowLineageHandler(String workTemplateId, String workTemplateName, int actionCount) {
        this.workTemplateId = workTemplateId;
        this.workTemplateName = workTemplateName;
        this.actionCount = actionCount;
        this.atlasClient = AtlasConnectionFactory.getAtlasClient();

        inputTablesMap = new HashMap<>();
        outputTablesMap = new HashMap<>();
        inputDbsMap = new HashMap<>();
        outputDbsMap = new HashMap<>();

        processedActionNum = 0;
    }

    /**
     * 将源表加入血缘关系的inputs中，如果在outputs中存在该表，
     * 则workflow中这个表是中间过程的表，无需加入，且需要从outputs中将该表删除.
     *
     * @param inputTables 源表
     */
    private void addToInputTables(List<Referenceable> inputTables) {
        addToMap(inputTables, inputTablesMap, outputTablesMap);
    }

    /**
     * 将源表加入血缘关系的outputs中，如果在inputs中存在该表，
     * 则workflow中这个表是中间过程的表，无需加入，且需要从inputs中将该表删除.
     *
     * @param outputTables 源表
     */
    private void addToOutputTables(List<Referenceable> outputTables) {
        addToMap(outputTables, outputTablesMap, inputTablesMap);
    }

    /**
     * 将源数据库加入血缘关系的inputDbs中，如果在outputDbs中存在该数据库，
     * 则workflow中这个表是中间过程的数据库，无需加入，且需要从outputDbs中将该库删除.
     *
     * @param inputDbs 源表
     */
    private void addToInputDbs(List<Referenceable> inputDbs) {
        addToMap(inputDbs, inputDbsMap, outputDbsMap);
    }

    /**
     * 将源数据库加入血缘关系的outputDbs中，如果在inputDbs中存在该库，
     * 则workflow中这个库是中间过程的库，无需加入，且需要从inputs中将该库删除.
     *
     * @param outputDbs 源表
     */
    private void addToOutputDbs(List<Referenceable> outputDbs) {
        addToMap(outputDbs, outputDbsMap, inputDbsMap);
    }

    private void addToMap(List<Referenceable> refsToAdd, Map<String, Referenceable> targetMap,
                          Map<String, Referenceable> relatedMap) {
        if (null != refsToAdd) {
            for (Referenceable ref : refsToAdd) {
                String qualifiedName = (String) ref.get("qualifiedName");
                if (null != relatedMap.get(qualifiedName)) {
                    relatedMap.remove(qualifiedName);
                } else {
                    targetMap.put(qualifiedName, ref);
                }
            }
        }
    }

    /**
     * 由各个action调用，将本action的源表和目的表填入，
     * 最后一个action填入后，需要更新workflowTemplate的血缘关系.
     *
     * @param inputTables  源表
     * @param outputTables 目的表
     * @param inputDbs     源数据库
     * @param outputDbs    目的数据库
     */
    public synchronized void addToLineage(List<Referenceable> inputTables,
                                          List<Referenceable> outputTables,
                                          List<Referenceable> inputDbs,
                                          List<Referenceable> outputDbs) throws Exception {
        addToInputTables(inputTables);
        addToOutputTables(outputTables);

        addToInputDbs(inputDbs);
        addToOutputDbs(outputDbs);

        processedActionNum++;
        if (processedActionNum == actionCount) {
            Referenceable lineageRef = new Referenceable(LineageDataTypes.LINEAGE_WORKFLOW_PROCESS_INFO.getValue());
            lineageRef.set("inputs", transToList(inputTablesMap));
            lineageRef.set("outputs", transToList(outputTablesMap));
            lineageRef.set("inputDbs", transToList(inputDbsMap));
            lineageRef.set("outputDbs", transToList(outputDbsMap));
            lineageRef.set("name", this.workTemplateName);

            JSONArray entitiesArray = new JSONArray();
            String entityJson = InstanceSerialization.toJson(lineageRef, true);
            entitiesArray.put(entityJson);

            JSONArray guids = atlasClient.createEntity(entitiesArray);

            atlasClient.updateEntityAttribute(workTemplateId, "lineage", guids.getString(0));
        }
    }

    private List<Referenceable> transToList(Map<String, Referenceable> refMap) {
        List<Referenceable> refList = new ArrayList<>();
        for (Referenceable ref : refMap.values()) {
            refList.add(ref);
        }

        return refList;
    }
}
