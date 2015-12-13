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

package org.apache.atlas.workFlow.handler;

import com.dtdream.dthink.dtalent.datastudio.activemq.MessageEnum;
import com.dtdream.dthink.dtalent.datastudio.utils.HttpUtils;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.TransformDataTypes;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.typesystem.IStruct;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.typesystem.persistence.Id;
import org.apache.atlas.typesystem.types.EnumValue;
import org.apache.atlas.workFlow.client.WorkflowActionExtend;
import org.apache.atlas.workFlow.client.WorkflowExtendsJob;
import org.apache.atlas.workFlow.client.WorkflowJsonToBean;
import org.apache.atlas.workFlow.conf.Conf;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.apache.atlas.workFlow.model.WorkFlowActionType;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;
import com.dtdream.dthink.dtalent.oozie.client.OozieClient;
import org.apache.oozie.AppType;
//import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.event.JobEvent;
import org.apache.oozie.client.event.message.WorkflowJobMessage;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.convert.Wrappers;

import java.util.*;

import static org.apache.atlas.workFlow.model.WorkFlowActionType.*;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkflowJobHandler implements MessageHandler<WorkflowJobMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowJobHandler.class);

    private OozieClient oc = null;

    private WorkflowJobMessage message = null;

    public static final String WORKFLOW_JOB_LOCK = new String("WORKFLOW_JOB_LOCK");

    public static final String WORKFLOW_ACTION_LOCK = new String("WORKFLOW_ACTION_LOCK");

    public static final String WORKFLOW_TRAIT = "Workflow";

    @Override
    public Runnable process(final WorkflowJobMessage message) {

        /*WorkflowJobMessage newMessage = new WorkflowJobMessage();
        newMessage.setStatus(WorkflowJob.Status.SUCCEEDED);
        newMessage.setId("0000015-151021071445617-oozie-ltf-W");
        newMessage.setStartTime(new Date());*/
        oc = new OozieClient(Conf.getConf().getString(Conf.OOZIE_CLIENT_URL));
        this.message = message;
        return switchHandle(message.getAppType(), message.getId());
    }

    public Runnable switchHandle(AppType appType, String id) {
        Runnable thread = null;
        switch (appType) {
            case WORKFLOW_JOB:
                LOG.info("WORKFLOW_JOB", appType.toString());
                //if(message.getEventStatus().equals(JobEvent.EventStatus.SUCCESS)||message.getEventStatus().equals(JobEvent.EventStatus.FAILURE)){
                if (message.getEventStatus().equals(JobEvent.EventStatus.SUCCESS)) {
                    thread = new WorkFlowJob();
                }
                break;
            case WORKFLOW_ACTION:
                LOG.info("WORKFLOW_ACTION", appType.toString());
                //thread = new WorkFlowAction();
                break;
        }
        return thread;
    }

    private Referenceable initWorkflowJob(final WorkflowExtendsJob job, final List<Referenceable> actionRefList) {
        List<WorkflowAction> actions = job.getActions();
        Referenceable tableRef = new Referenceable(WorkFlowDataTypes.WORKFLOW_JOB.getValue(),
            WorkflowJobHandler.WORKFLOW_TRAIT);
        tableRef.set("name", job.getAppName());
        //tableRef.set("description",null);
        tableRef.set("workflowId", job.getId());
        tableRef.set("actions", actionRefList);
        List<Referenceable> list = new ArrayList<>();
        for (int i = 0; i < actionRefList.size(); i++) {
            Referenceable dagRef = new Referenceable(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue());
            if (i + 1 < actionRefList.size()) {
                dagRef.set("preceding", actionRefList.get(i));
                dagRef.set("succeeding", actionRefList.get(i + 1));
            }
            if (dagRef.get("preceding") != null || dagRef.get("succeeding") != null) {
                list.add(dagRef);
            }
        }
        if (list.size() > 0) {
            tableRef.set("actionsDAG", list);
        }
        tableRef.set("config", job.getConf());
        String md5Conf = Md5Handler.generateIdentity(job.getConf());
        tableRef.set("qualifiedName", "workflow.default." + job.getWorkspaceName() + "." + md5Conf + "." + job.getId());
        tableRef.set("createTime", job.getCreatedTime().getTime());
        tableRef.set("startTime", job.getStartTime().getTime());
        tableRef.set("endTime", job.getEndTime().getTime());
        tableRef.set("user", job.getUser());
        tableRef.set("status", job.getStatus().name());
        if (job.getParentId() != null) {
            tableRef.set("parentId", job.getParentId());
        }
        return tableRef;
    }

    //yuanyongxian 验证send后是否自动确认新增或修改
    private DB_OPERATION initWorkflowJobTemplate(Referenceable[] tableRef,
                                                 final Referenceable jobRef,
                                                 final WorkflowExtendsJob job) {
        tableRef[0].set("name", jobRef.get("name"));
        //tableRef.set("description",null);
        String conf = jobRef.get("config") == null ? "" : String.valueOf(jobRef.get("config")).trim();
        //加密后的字符串
        Referenceable referenceable = getReferenceableFromTemplateId(conf, WorkFlowDataTypes.WORKFLOW_TEMPLATE);
        if (referenceable == null) {
            String md5Conf = Md5Handler.generateIdentity(conf);
            tableRef[0].set("qualifiedName", "workflow.default." + job.getWorkspaceName() + "." + md5Conf);
            tableRef[0].set("templateId", md5Conf);
            List<Referenceable> list = new ArrayList();
            list.add(jobRef);
            tableRef[0].set("workflowJobs", list);
            tableRef[0].set("config", jobRef.get("config").toString().trim());
            tableRef[0].set("workflowName", jobRef.get("name"));
            return DB_OPERATION.CREATE;
        } else {
            tableRef[0] = referenceable;
            return DB_OPERATION.UPDATE;
        }

    }

    private Referenceable initWorkflowAction(final WorkflowAction action, final WorkflowExtendsJob job, String configId) {
        Referenceable tableRef = new Referenceable(WorkFlowDataTypes.WORKFLOW_ACTION.getValue(), WorkflowJobHandler.WORKFLOW_TRAIT);
        tableRef.set("name", action.getName());
        //tableRef.set("description",null);
        tableRef.set("qualifiedName", "workflow.default." + job.getWorkspaceName() + "." + job.getId() + "." + action.getName());
        tableRef.set("metaSource", "WORKFLOW");
        tableRef.set("type", exchangeToEnum(action.getType()).name());
        tableRef.set("actionId", action.getId());
        tableRef.set("instanceId", action.getExternalId());
        //tableRef.set("etlInstance","");//yuanyongxian
        tableRef.set("workflowId", job.getId());
        tableRef.set("config", action.getConf());
        tableRef.set("startTime", action.getStartTime().getTime());
        tableRef.set("endTime", action.getEndTime().getTime());
        tableRef.set("status", action.getStatus().name());
        if (configId != null) {
            tableRef.set("configId", configId);
        }
        return tableRef;
    }

    //yuanyongxian 验证send后是否自动确认新增或修改
    private DB_OPERATION initWorkflowActionTemplate(Referenceable[] tableRef, final Referenceable actionRef, final WorkflowExtendsJob job) {
        String conf = actionRef.get("config") == null ? "" : String.valueOf(actionRef.get("config")).trim();
        ;
        //加密后的字符串
        Referenceable referenceable = getReferenceableFromTemplateId(conf, WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE);
        if (referenceable == null) {
            String md5Conf = Md5Handler.generateIdentity(conf);
            tableRef[0].set("templateId", md5Conf);
            tableRef[0].set("name", actionRef.get("name"));
            tableRef[0].set("qualifiedName", "workflow.default." + job.getWorkspaceName() + "." + Md5Handler.generateIdentity(job.getConf()) + "." + md5Conf);
            //tableRef.set("description",null);
            List<Referenceable> list = new ArrayList();
            list.add(actionRef);
            tableRef[0].set("actions", list);
            tableRef[0].set("config", conf);
            tableRef[0].set("workflowActionName", actionRef.get("name"));
            return DB_OPERATION.CREATE;
        } else {
            tableRef[0] = referenceable;
            return DB_OPERATION.UPDATE;
        }

    }

    private Id[] addArray(Id[] src, String addStr) {
        if (src == null) {
            //src = new String[]{addStr};
            return null;
        }
        for (Id subSrc : src) {
            if (addStr != null && addStr.equals(subSrc)) {//contain return src
                return src;
            }
        }
        Id[] dist = new Id[src.length + 1];
        System.arraycopy(src, 0, dist, 0, src.length);
        //dist[src.length] = addStr;
        return dist;
    }

    public static String notifyEntityStr(final Collection entities) {
        JSONArray entitiesArray = new JSONArray();
        for (Object entity : entities) {
            notifyEntityStr((IStruct) entity, entitiesArray);
        }
        return notifyEntityStr(entitiesArray);
    }

    public static void notifyEntityStr(final IStruct entity, JSONArray entitiesArray) {
        String entityJson = null;
        try {
            entityJson = InstanceSerialization.toJson(entity, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        entitiesArray.put(entityJson);
    }

    /**
     * Notify atlas of the entity through message. The entity can be a complex entity with reference to other entities.
     * De-duping of entities is done on server side depending on the unique attribute on the
     *
     * @param entities
     */
    public static String notifyEntityStr(final JSONArray entities) {
        String message = entities.toString();
        return message;
    }

    public Referenceable getReferenceableFromTemplateId(String conf, WorkFlowDataTypes workFlowDataTypes) {
        String md5Conf = Md5Handler.generateIdentity(conf);
        Referenceable retRef = null;
        if (md5Conf != null) {
            try {
                JSONArray jsonArray = AtlasConnectionFactory.getAtlasClient().rawSearch(workFlowDataTypes.getValue(), "templateId", md5Conf);
                for (int index = 0; index < jsonArray.length(); index++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    if (jsonObject != null && jsonObject.getString("config") != null && jsonObject.getString("config").trim().equals(conf.trim())) {
                        String guid = jsonObject.getJSONObject("$id$").getString("id");//get guid
                        Referenceable referenceable = AtlasConnectionFactory.getAtlasClient().getEntity(guid);
                        retRef = referenceable;
                        break;
                    }
                }
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return retRef;
    }

    public interface WorkNodeService {

        JsonNode getWorkNode(String worknodeId);
    }

    public class WorkFlowJob implements Runnable {

        private WorkNodeService workNodeService;

        public WorkFlowJob() {
            workNodeService = new WorkNodeService() {
                @Override
                public JsonNode getWorkNode(String worknodeId) {
                    HttpUtils.ResponseBean retBean = oc.getWorknode(worknodeId);
                    if (retBean.getRetCode() != 200) {//exception get again
                        retBean = oc.getWorknode(worknodeId);
                    }
                    JsonNode retData = retBean.getRetData();
                    return retData;
                }
            };
        }

        public void setWorkNodeService(WorkNodeService workNodeService) {
            this.workNodeService = workNodeService;
        }

        public WorkflowExtendsJob toWorkflowExtendsJob(String jsonStr) {
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) org.json.simple.JSONValue.parse(jsonStr);
            return WorkflowJsonToBean.createWorkflowJob(json);
        }

        public String getWorknodeId(String conf, String jobId, String actionId) {
            String startSub = "<action-id>";
            String endSub = "</action-id>";
            if (conf.indexOf(startSub) < 0 || conf.indexOf(endSub) < 0) {
                LOG.error("无法获取对应worknode id(action-id),因为" + conf + "不存在" + startSub + "或" + endSub + ",job id:" + jobId + ";actionId:" + actionId);
                return null;
            } else if (conf.indexOf(endSub) <= conf.indexOf(startSub)) {
                LOG.error("无法获取对应worknode id(action-id),因为" + conf + "结构异常,job id:" + jobId + ";actionId:" + actionId);
                return null;
            }
            String worknodeId = conf.substring(conf.indexOf(startSub) + startSub.length(), conf.indexOf(endSub));
            return worknodeId;
        }

        public void process(WorkflowExtendsJob job) {
            List<Referenceable> entities = new ArrayList<>();
            List<WorkflowAction> actions = job.getActions();
            DB_OPERATION dbOper = null;
            List<Referenceable> actionRefList = new ArrayList();
            //初始化工作流job参数
            for (WorkflowAction action : actions) {
                String configId = null;
                WorkFlowActionType actionType = exchangeToEnum(action.getType());
                if (actionType == null) {
                    if ("ddp".equals(action.getType())) {//ddp 需要再获取一遍，这个是
                        String conf = action.getConf();
                        String worknodeId = getWorknodeId(conf, job.getId(), action.getId());
                        if (worknodeId == null) {
                            return;
                        }
                        JsonNode workNode = workNodeService.getWorkNode(worknodeId);
                        WorkflowActionExtend actionExtend = WorkflowActionExtend.toWorkflowActionExtend(action);
                        actionExtend.setName(workNode.get("actionName").getTextValue());
                        actionExtend.setType(workNode.get("actionType").getTextValue());
                        if ("MR".equals(actionExtend.getType()) || "SQL".equals(actionExtend.getType())) {
                            actionExtend.setType("ODPS" + actionExtend.getType());
                        }
                        actionExtend.setId(workNode.get("actionId").getTextValue());
                        if (workNode.get("actionConf") != null && (workNode.get("actionConf").getClass().equals(ObjectNode.class))) {
                            actionExtend.setConf(workNode.get("actionConf").toString());
                        } else {
                            actionExtend.setConf(workNode.get("actionConf").getTextValue());
                        }
                        if (workNode.get("confId") != null) {
                            configId = workNode.get("confId").getTextValue();
                        }
                        action = actionExtend;
                    } else {
                        LOG.warn(action.getType() + " 不属于WorkFlowActionType类型,job id:" + job.getId() + ";actionId:" + action.getId());
                        continue;
                    }
                }
                //不添加start和end的action
                if (!(START.equals(actionType) || END.equals(actionType))) {
                    Referenceable actionRef = initWorkflowAction(action, job, configId);
                    actionRefList.add(actionRef);
                }
            }
            Referenceable jobRef = initWorkflowJob(job, actionRefList);
            entities.add(jobRef);
            int maxRetries = Conf.getConf().getInt(Conf.HOOK_NUM_RETRIES, 3);
            int numRetries = 0;

            try {
                JSONArray notifyArray = new JSONArray(notifyEntityStr(entities));
                JSONArray array = AtlasConnectionFactory.getAtlasClient().createEntity(notifyArray);
                for (int index = 0; index < array.length(); index++) {
                    String jsonGuid = array.getString(index);
                    //修改workflow_template时，kafka的异步消息队列有可能用不上，原因是修改数组需要知道其guid，异步修改创建后无法得知guid
                    Referenceable referenceable = AtlasConnectionFactory.getAtlasClient().getEntity(jsonGuid);

                    if (referenceable.typeName.equals(WorkFlowDataTypes.WORKFLOW_JOB.getValue())) {
                        Referenceable jobTempRef = new Referenceable(WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue(), WorkflowJobHandler.WORKFLOW_TRAIT);
                        Referenceable[] jobTempRefArr = new Referenceable[]{jobTempRef};
                        //创建或修改工作流job模板，由于多线程要考虑线程安全。
                        String worktemplateId = null;
                        synchronized (WorkflowJobHandler.WORKFLOW_JOB_LOCK) {
                            dbOper = initWorkflowJobTemplate(jobTempRefArr, referenceable, job);
                            switch (dbOper) {
                                case CREATE:
                                    JSONArray guids = createEntity(jobTempRefArr);
                                    worktemplateId = guids.getString(0);
                                    break;
                                case UPDATE:
                                    Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) jobTempRefArr[0].get("workflowJobs");
                                    List<Id> idList = new ArrayList();
                                    Iterator<Id> innerIt = innserSeq.iterator();
                                    while (innerIt.hasNext()) {
                                        idList.add(innerIt.next());
                                    }
                                    idList.add(referenceable.getId());
                                    String innerSeqMessage = notifyEntityStr(idList);
                                    worktemplateId = jobTempRefArr[0].getId().id;
                                    AtlasConnectionFactory.getAtlasClient().updateEntityAttribute(jobTempRefArr[0].getId().id, "workflowJobs", innerSeqMessage);
                                    break;
                            }
                        }

                        WorkflowLineageHandler lineageHandler = new WorkflowLineageHandler(worktemplateId,
                            (String) jobTempRefArr[0].get("qualifiedName"), actionRefList.size());

                        Wrappers.SeqWrapper<Referenceable> actionsSeq = (Wrappers.SeqWrapper<Referenceable>) referenceable.get("actions");
                        Iterator<Referenceable> actinIt = actionsSeq.iterator();
                        //创建或修改工作流action模板，存在线程不安全的问题，后续需要加线程锁。
                        while (actinIt.hasNext()) {
                            Referenceable actionRef = actinIt.next();

                            //添加ODPS,DXT接口实现
                            WorkFlowActionType actionTypeEnum = ((EnumValue) actionRef.get("type")).toEnum(WorkFlowActionType.class);
                            CommonInfo commonInfo = null;
                            String configId = null;
                            switch (actionTypeEnum) {
                                case ODPSMR:
                                case ODPSSQL:
                                    commonInfo = WorkflowJobHandler.getWorkflowToODPS();
                                    break;
                                case DATABRIDGE:
                                    commonInfo = WorkflowJobHandler.getWorkflowToDXT();
                                    configId = actionRef.get("configId").toString();
                                    break;
                            }
                            if (null != commonInfo) {
                                Runnable sendThread = new WorkflowSendThread(commonInfo, lineageHandler, actionRef,
                                    actionRef.getId().id, job.getWorkspaceName(), configId);
                                new Thread(sendThread).start();
                            }

                            Referenceable actionTemplate = new Referenceable(WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue(), WorkflowJobHandler.WORKFLOW_TRAIT);
                            Referenceable[] actionTempRefArr = new Referenceable[]{actionTemplate};
                            synchronized (WorkflowJobHandler.WORKFLOW_ACTION_LOCK) {
                                dbOper = initWorkflowActionTemplate(actionTempRefArr, actionRef, job);
                                switch (dbOper) {
                                    case CREATE:
                                        createEntity(actionTempRefArr);
                                        break;
                                    case UPDATE:
                                        Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) actionTempRefArr[0].get("actions");
                                        List<Id> idList = new ArrayList();
                                        Iterator<Id> innerIt = innserSeq.iterator();
                                        while (innerIt.hasNext()) {
                                            idList.add(innerIt.next());
                                        }
                                        idList.add(actionRef.getId());
                                        AtlasConnectionFactory.getAtlasClient().updateEntityAttribute(actionTempRefArr[0].getId().id, "actions", notifyEntityStr(idList));
                                        break;
                                }
                            }
                        }
                    }
                }
                System.out.println(array);
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

                /*while(true){
                    try {
                        AtlasConnectionFactory.getNotificationInterface().send(NotificationInterface.NotificationType.HOOK, notifyMessage);
                    } catch (NotificationException e) {
                        numRetries++;
                        if(numRetries < maxRetries) {
                            LOG.debug("Failed to notify atlas for entity {}. Retrying", message, e);
                        } else {
                            LOG.error("Failed to notify atlas for entity {} after {} retries. Quitting", message,
                                    maxRetries, e);
                            break;
                        }
                    }
                }
                if(!(numRetries < maxRetries)){//update entity
                    for(Referenceable entityRef:updateEntity){
                        //synchrnoize
                        //AtlasConnectionFactory.getAtlasClient()
                        if(WorkFlowDataTypes.WORKFLOW_TEMPLATE.getName().equals(entityRef.typeName)){

                        }else if(WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getName().equals(entityRef.typeName)){

                        }
                    }
                }*/
            /*} catch (OozieClientException e) {
                LOG.info("get oozie job info error from message id:" + message.getId(), e);
            }*/
        }

        @Override
        public void run() {
            //try {
            //HttpUtils.ResponseBean job = oc.getWorkflowInfo(message.getId(),null);
            HttpUtils.ResponseBean workflowBean = oc.getWorkflowDetailInfo(message.getId());
            WorkflowExtendsJob job = null;
            if (workflowBean.getRetCode() == 200) {
                JsonNode retData = workflowBean.getRetData();
                job = toWorkflowExtendsJob(retData.toString());
            }
            if (job == null) {
                LOG.error("WorkFlowJob id is null,id:" + message.getId());
                return;
            }
            process(job);
        }
    }

    private class WorkflowSendThread implements Runnable {
        private CommonInfo commonInfo = null;
        private WorkflowLineageHandler lineageHandler;
        private Map<String, String> paramMap;

        public WorkflowSendThread(CommonInfo commonInfo, WorkflowLineageHandler lineageHandler, Referenceable actionRef,
                                  String actionGuid, String workspaceName, String configId) {
            this.commonInfo = commonInfo;
            this.lineageHandler = lineageHandler;

            paramMap = new HashMap<>();
            paramMap.put(CommonInfo.INSTANCE_ID, String.valueOf(actionRef.get("instanceId")));
            paramMap.put(CommonInfo.CONFIG, String.valueOf(actionRef.get("config")));
            paramMap.put(CommonInfo.ACTION_GUID, actionGuid);
            paramMap.put(CommonInfo.WORKSPACE_NAME, workspaceName);
            paramMap.put(CommonInfo.CONFIG_ID, configId);
            paramMap.put(CommonInfo.SOURCE_TYPE, MessageEnum.SystemName.WORKFLOW.name());
        }

        @Override
        public void run() {
            commonInfo.sendActionConf(paramMap, lineageHandler);
        }
    }

    private static CommonInfo getWorkflowCommonInfoInstance(String className) {
        if (className != null) {
            try {
                CommonInfo commonInfo = (CommonInfo) Class.forName(className).newInstance();
                return commonInfo;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static CommonInfo getWorkflowToODPS() {
        String workflowInfoClass = Conf.getConf().getString(Conf.WORKFLOW_INFO_TO_ODPS);
        return getWorkflowCommonInfoInstance(workflowInfoClass);
    }

    private static CommonInfo getWorkflowToDXT() {
        String workflowInfoClass = Conf.getConf().getString(Conf.WORKFLOW_INFO_TO_DXT);
        return getWorkflowCommonInfoInstance(workflowInfoClass);
    }

    public JSONArray createEntity(Referenceable[] refArray) throws JSONException, AtlasServiceException {
        List<Referenceable> subEntities = new ArrayList<>();
        for (Referenceable ref : refArray) {
            subEntities.add(ref);
        }
        return createEntity(subEntities);
    }

    public JSONArray createEntity(List<Referenceable> refList) throws JSONException, AtlasServiceException {
        String innerNotifyMessage = notifyEntityStr(refList);
        JSONArray innerNotifyArray = new JSONArray(innerNotifyMessage);
        return AtlasConnectionFactory.getAtlasClient().createEntity(innerNotifyArray);
    }

    class WorkFlowAction implements Runnable {
        @Override
        public void run() {
        }
    }

}
