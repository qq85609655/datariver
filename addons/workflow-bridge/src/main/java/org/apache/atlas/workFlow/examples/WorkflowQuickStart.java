package org.apache.atlas.workFlow.examples;

import com.google.common.collect.ImmutableList;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.atlas.workFlow.bridge.WorkFlowMetaStroreBridge;
import org.apache.atlas.workFlow.client.WorkflowExtendsJob;
import org.apache.atlas.workFlow.handler.WorkflowJobHandler;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/5 11:21
 */
public class WorkflowQuickStart {

    private String jsonStr = null;

    private ObjectNode node = null;

    private WorkflowJobHandler.WorkNodeService workNodeService = null;

    private final AtlasClient dgiCLient;

    public WorkflowQuickStart(String baseUrl) {
        dgiCLient = new AtlasClient(baseUrl);
    }

    public AtlasClient getAtlasClient() {
        return dgiCLient;
    }

    public static String getServerUrl(String[] args) {
        String baseUrl = "http://localhost:21000";
        if (args != null && args.length > 0) {
            baseUrl = args[0];
        }

        return baseUrl;
    }

    public void createTypes() throws Exception {
        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();
        WorkFlowMetaStroreBridge workFlowMetaStroreBridge = new WorkFlowMetaStroreBridge();
        workFlowMetaStroreBridge.registerWorkflowDataModel();
    }

    public void createEntities() {
        //setupExample(System.getProperty("atlas.home")!=null?(System.getProperty("atlas.home") + "/workflowJob.json"):("D:\\GitWorkspace\\DTalent\\master\\DThink-DTalent\\metadata\\addons\\workflow-bridge\\src\\main\\resources"+ "/workflowJob.json"));
        setupExample(System.getProperty("atlas.home") + File.separator +"examples" + File.separator + "workflowJob.json");
        importWorkFlowJobODSP();
        importWorkflowJobDXT();
    }

    public void search() {
        List<String> list = new ArrayList<String>();
        list.add(WorkFlowDataTypes.WORKFLOW_ACTION.getValue());
        list.add(WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue());
        list.add(WorkFlowDataTypes.WORKFLOW_JOB.getValue());
        list.add(WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue());

        for (String dslQuery : list) {
            try {
                JSONArray results = dgiCLient.search(dslQuery);
                if (results != null) {
                    System.out.println("query [" + dslQuery + "] returned [" + results.length() + "] rows");
                } else {
                    System.out.println("query [" + dslQuery + "] failed.");
                }
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public String setupExample(String jsonName) {
        jsonStr = FileUtil.loadResourceFile(jsonName);
        return jsonStr;
    }

    public String setupIT(String jsonName) {
        jsonStr = FileUtil.loadResourceFile(this, jsonName);
        return jsonStr;
    }

    private void initODPS() {
        workNodeService = new WorkflowJobHandler.WorkNodeService() {
            @Override
            public JsonNode getWorkNode(String worknodeId) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                ObjectNode confNode = JsonNodeFactory.instance.objectNode();
                confNode.put("mrType", "mo");
                confNode.put("resources", "aopalliance-1.0.jar");
                confNode.put("mapperClass", "xxxx");
                node.put("actionConf", confNode);
                node.put("actionId", "28422d88-9b42-4060-85a4-0025763fede2-action-MR");
                node.put("version", "201510200001");
                node.put("workspaceName", "dtdream_hanwen");
                node.put("actionOwner", "aaa");
                //node.put("updateUserId",null);
                //node.put("deleteUserId",null);
                node.put("actionName", "SQL调度" + worknodeId);
                //node.put("actionDesc",null);
                node.put("actionType", "ODPSMR");
                node.put("createTime", "2015-10-20 20:25:51");
                //node.put("lastUpdateTime",null);
                //node.put("deleteTime",null);
                node.put("inRecycleBin", false);
                //node.put("confId",null);
                node.put("usedByWorkflow", "8273de4c-3d41-4cf4-b543-bce-oozie-root-W");
                //node.put("lockUser",null);
                JsonNode retData = node;
                return retData;
            }
        };
    }

    private void initDXT() {
        workNodeService = new WorkflowJobHandler.WorkNodeService() {
            @Override
            public JsonNode getWorkNode(String worknodeId) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();


                ObjectNode transition = JsonNodeFactory.instance.objectNode();
                transition.put("id", "");
                transition.put("name", "tran1");
                ArrayNode inArr = JsonNodeFactory.instance.arrayNode();
                ObjectNode tableInputsCont = JsonNodeFactory.instance.objectNode();
                tableInputsCont.put("name", "step1");
                tableInputsCont.put("dbID", "13548e24-5739-42b7-a227-e84d6ca1d96a");
                tableInputsCont.put("tableName", "for_test");
                tableInputsCont.put("column", "num, city, name, birthDate");
                tableInputsCont.put("splitColumn", "ID");
                tableInputsCont.put("splitRule", "mod");
                inArr.add(tableInputsCont);
                ObjectNode stepCont = JsonNodeFactory.instance.objectNode();
                stepCont.put("demoInputs", inArr);
                ObjectNode tableOutputsCont = JsonNodeFactory.instance.objectNode();
                tableOutputsCont.put("name", "step2");
                tableOutputsCont.put("dbID", "13548e24-5739-42b7-a227-e84d6ca1d96b");
                tableOutputsCont.put("tableName", "for_test2");
                tableOutputsCont.put("destColumn", "num, city, name, birthDate");
                tableOutputsCont.put("flushNumber", "1000");
                tableOutputsCont.put("insertMode", 1);
                ArrayNode outArr = JsonNodeFactory.instance.arrayNode();
                outArr.add(tableOutputsCont);
                stepCont.put("demoOutputs", outArr);
                transition.put("steps", stepCont);
                transition.put("workStream", "step1->step2");

                ObjectNode confNode = JsonNodeFactory.instance.objectNode();
                confNode.put("transition", transition);
                node.put("actionConf", confNode);
                node.put("actionId", "28422d88-9b42-4060-85a4-0025763fede2-action-DXT");
                node.put("version", "201510200001");
                node.put("workspaceName", "dtdream_hanwen");
                node.put("actionOwner", "aaa");
                //node.put("updateUserId",null);
                //node.put("deleteUserId",null);
                node.put("actionName", "DXT转换" + worknodeId);
                //node.put("actionDesc",null);
                node.put("actionType", "DATABRIDGE");
                node.put("createTime", "2015-10-20 20:25:51");
                //node.put("lastUpdateTime",null);
                //node.put("deleteTime",null);
                node.put("inRecycleBin", false);
                node.put("confId","e68192e8-39c3-4f65-b0da-15c833259326");
                node.put("usedByWorkflow", "8273de4c-3d41-4cf4-b543-bce-oozie-root-W");
                //node.put("lockUser",null);
                JsonNode retData = node;
                return retData;
            }
        };
    }

    public void importWorkFlowJobODSP() {
        WorkflowJobHandler.WorkFlowJob job = new WorkflowJobHandler().new WorkFlowJob();
        initODPS();
        job.setWorkNodeService(workNodeService);
        WorkflowExtendsJob exJob = job.toWorkflowExtendsJob(jsonStr);
        job.process(exJob);
    }

    public void importWorkflowJobDXT() {
        WorkflowJobHandler.WorkFlowJob job = new WorkflowJobHandler().new WorkFlowJob();
        initDXT();
        job.setWorkNodeService(workNodeService);
        jsonStr = jsonStr.replaceAll("774c6a47-9980-4520-b1d7-9dc-oozie-root-W","674c6a47-9980-4520-b1d7-9dc-oozie-root-W");//构造DXT与ODPS不同的ID
        WorkflowExtendsJob exJob = job.toWorkflowExtendsJob(jsonStr);
        job.process(exJob);
    }

    public WorkflowJobHandler.WorkNodeService getWorkNodeService() {
        return workNodeService;
    }


    public static void main(String[] args) throws Exception {
        String baseUrl = getServerUrl(args);
        WorkflowQuickStart workflowQuickStart = new WorkflowQuickStart(baseUrl);

        // Shows how to create types in Atlas for your meta model
        workflowQuickStart.createTypes();

        // Shows how to create entities (instances) for the added types in Atlas
        workflowQuickStart.createEntities();

        // Shows some search queries using DSL based on types
        workflowQuickStart.search();
    }
}
