package org.apache.atlas.workFlow.handler;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.persistence.Id;
import org.apache.atlas.typesystem.types.EnumValue;
import org.apache.atlas.workFlow.client.WorkflowActionExtend;
import org.apache.atlas.workFlow.client.WorkflowExtendsJob;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.apache.atlas.workFlow.examples.WorkflowQuickStart;
import org.apache.atlas.workFlow.model.WorkFlowActionType;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;
import org.apache.commons.configuration.Configuration;
import org.apache.oozie.client.WorkflowAction;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.collection.convert.Wrappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.atlas.workFlow.model.WorkFlowActionType.END;
import static org.apache.atlas.workFlow.model.WorkFlowActionType.START;
import static org.apache.atlas.workFlow.model.WorkFlowActionType.exchangeToEnum;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkflowJobHandlerIT {

    public static final String ATLAS_ENDPOINT = "atlas.rest.address";

    WorkflowQuickStart workflowQuickStart = null;

    @BeforeClass
    public void setUp() throws Exception {
        Configuration atlasConf = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
        String[] args = new String[1];
        args[0] = atlasConf.getString(ATLAS_ENDPOINT, "http://localhost:21000");
        String baseUrl = WorkflowQuickStart.getServerUrl(args);
        workflowQuickStart = new WorkflowQuickStart(baseUrl);

        // Shows how to create types in Atlas for your meta model
        try {
            workflowQuickStart.createTypes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWorkFlowJobODSP() throws JSONException {
        // Shows how to create entities (instances) for the added types in Atlas
        String jsonStr = workflowQuickStart.setupIT("workflowJob_ODPS.json");
        workflowQuickStart.importWorkFlowJobODSP();
        AtlasClient client = workflowQuickStart.getAtlasClient();
        commonTest(client, jsonStr);
    }

    public void commonTest(final AtlasClient client, final String jsonStr) throws JSONException {
        WorkflowJobHandler jobHandler = new WorkflowJobHandler();
        WorkflowJobHandler.WorkFlowJob job = jobHandler.new WorkFlowJob();
        WorkflowExtendsJob json = job.toWorkflowExtendsJob(jsonStr);
        List<String> jobGuid = new ArrayList<String>();
        List<Referenceable> actionsRefList = new ArrayList();
        try {
            org.codehaus.jettison.json.JSONArray jobJsonArray = client.rawSearch(WorkFlowDataTypes.WORKFLOW_JOB.getValue(), "workflowId", json.getId());
            Assert.assertEquals(jobJsonArray.length() > 0, true);
            for (int index = 0; index < jobJsonArray.length(); index++) {
                org.codehaus.jettison.json.JSONObject jsonObject = jobJsonArray.getJSONObject(index);
                if (jsonObject != null && jsonObject.getString("config") != null && jsonObject.getString("config").trim().equals(json.getConf().trim())) {
                    String guid = jsonObject.getJSONObject("$id$").getString("id");//get guid
                    jobGuid.add(guid);
                    Referenceable referenceable = AtlasConnectionFactory.getAtlasClient().getEntity(guid);
                    Wrappers.SeqWrapper<Referenceable> actionsSeq = (Wrappers.SeqWrapper<Referenceable>) referenceable.get("actions");
                    Iterator<Referenceable> actinIt = actionsSeq.iterator();
                    while (actinIt.hasNext()) {
                        actionsRefList.add(actinIt.next());
                    }

                    Assert.assertEquals(referenceable.get("name").toString(), json.getAppName());
                    Assert.assertEquals(referenceable.get("workflowId").toString(), json.getId());
                    Assert.assertEquals(referenceable.get("config").toString(), json.getConf());
                    Assert.assertEquals(referenceable.get("createTime").toString(), String.valueOf(json.getCreatedTime().getTime()));
                    Assert.assertEquals(referenceable.get("startTime").toString(), String.valueOf(json.getStartTime().getTime()));
                    Assert.assertEquals(referenceable.get("endTime").toString(), String.valueOf(json.getEndTime().getTime()));
                    Assert.assertEquals(referenceable.get("user").toString(), json.getUser());
                    Assert.assertEquals(referenceable.get("parentId").toString(), json.getParentId());
                    //Assert.assertEquals(referenceable.get("actionsDAG").toString(), json.getParentId());

                    //workflow job template
                    Referenceable jobTemplateRef = jobHandler.getReferenceableFromTemplateId(json.getConf(), WorkFlowDataTypes.WORKFLOW_TEMPLATE);
                    Assert.assertEquals(jobTemplateRef != null, true);
                    Assert.assertEquals(jobTemplateRef.get("name").toString(), json.getAppName());
                    Assert.assertEquals(jobTemplateRef.get("templateId").toString(), Md5Handler.generateIdentity(json.getConf().trim()));
                    Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) jobTemplateRef.get("workflowJobs");
                    boolean includingSign = false;
                    Iterator<Id> innserSeqIt = innserSeq.iterator();
                    while (innserSeqIt.hasNext()) {
                        includingSign = (innserSeqIt.next()._getId().equals(guid) || includingSign);
                    }
                    Assert.assertEquals(includingSign, true);
                    Assert.assertEquals(jobTemplateRef.get("config").toString(), json.getConf());
                    Assert.assertEquals(jobTemplateRef.get("workflowName").toString(), json.getAppName());
                }
            }
        } catch (AtlasServiceException e) {
            e.printStackTrace();
        }
        List<WorkflowAction> actions = json.getActions();
        int subtract = 0;
        for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
            WorkflowAction subAction = actions.get(actionIndex);
            WorkflowActionExtend action = WorkflowActionExtend.toWorkflowActionExtend(subAction);
            WorkFlowActionType actionType = exchangeToEnum(action.getType());
            if (!(START.equals(actionType) || END.equals(actionType))) {

                Referenceable tableRef = actionsRefList.get(actionIndex - subtract);
                JsonNode workNode = workflowQuickStart.getWorkNodeService().getWorkNode(job.getWorknodeId(action.getConf(), json.getId(), action.getId()));
                action.setName(workNode.get("actionName").getTextValue());
                action.setType(workNode.get("actionType").getTextValue());
                action.setId(workNode.get("actionId").getTextValue());
                if (workNode.get("actionConf") != null && (workNode.get("actionConf").getClass().equals(ObjectNode.class))) {
                    action.setConf(workNode.get("actionConf").toString());
                } else {
                    action.setConf(workNode.get("actionConf").getTextValue());
                }

                Assert.assertEquals(tableRef.get("name"), action.getName());
                //Assert.assertEquals(tableRef.get("description"),null);
                Assert.assertEquals(tableRef.get("qualifiedName"), "workflow.default." + json.getWorkspaceName() + "." + json.getId() + "." + action.getName());
                Assert.assertEquals(((EnumValue) tableRef.get("metaSource")).value, "WORKFLOW");
                Assert.assertEquals(((EnumValue) tableRef.get("type")).value, exchangeToEnum(action.getType()).name());
                Assert.assertEquals(tableRef.get("actionId"), action.getId());
                Assert.assertEquals(tableRef.get("instanceId"), action.getExternalId());
                //Assert.assertEquals(tableRef.get("etlInstance"),"");//yuanyongxian
                Assert.assertEquals(tableRef.get("workflowId"), json.getId());
                Assert.assertEquals(tableRef.get("config"), action.getConf());
                Assert.assertEquals(tableRef.get("startTime").toString(), String.valueOf(action.getStartTime().getTime()));
                Assert.assertEquals(tableRef.get("endTime").toString(), String.valueOf(action.getEndTime().getTime()));
                Assert.assertEquals(((EnumValue) tableRef.get("status")).value, action.getStatus().name());

                //workflow action template
                Referenceable actionTemplateRef = jobHandler.getReferenceableFromTemplateId(action.getConf(), WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE);
                Assert.assertEquals(actionTemplateRef != null, true);
                String md5Conf = Md5Handler.generateIdentity(action.getConf().trim());
                Assert.assertEquals(actionTemplateRef.get("templateId").toString(), md5Conf);
                Assert.assertEquals(actionTemplateRef.get("name").toString(), action.getName());

                Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) actionTemplateRef.get("actions");
                boolean includingSign = false;
                Iterator<Id> innserSeqIt = innserSeq.iterator();
                while (innserSeqIt.hasNext()) {
                    includingSign = (innserSeqIt.next()._getId().equals(tableRef.getId()) || includingSign);
                }

                Assert.assertEquals(actionTemplateRef.get("config").toString(), action.getConf().trim());
                Assert.assertEquals(actionTemplateRef.get("workflowActionName").toString(), action.getName());
            } else {
                subtract++;
            }
        }
    }

    @Test
    public void testWorkflowJobDXT() throws JSONException {
        // Shows how to create entities (instances) for the added types in Atlas

        String jsonStr = workflowQuickStart.setupIT("workflowJob_DXT.json");
        workflowQuickStart.importWorkflowJobDXT();
        AtlasClient client = workflowQuickStart.getAtlasClient();
        commonTest(client, jsonStr);
    }
}
