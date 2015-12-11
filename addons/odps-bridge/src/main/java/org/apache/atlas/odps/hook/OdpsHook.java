package org.apache.atlas.odps.hook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dtdream.dthink.dtalent.datastudio.activemq.MessageEnum;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.exception.BridgeException;
import org.apache.atlas.common.hook.BaseHook;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.common.util.LineageHandler;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.client.DDPClient;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.odps.parser.impl.PartitionParser;
import org.apache.atlas.odps.parser.impl.ResourceParser;
import org.apache.atlas.odps.parser.impl.TableParser;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.typesystem.persistence.Id;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.convert.Wrappers;

import java.util.*;

/**
 * odps入口钩子
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class OdpsHook extends BaseHook {
    public static final String DEFAULT_DGI_URL = "http://localhost:21000/";
    public static final String ORGANIZATIONS = AtlasClient.ORGANIZATIONS;
    public static final int SLEEP_FOR_NOTIFY_TIME = 15 * 1000;
    private static DDPClient ddpClient = DDPClient.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OdpsHook.class);
    private Set<Referenceable> parsedProject = new HashSet<>();

    /**
     * @param context
     */
    protected void doRun(HookContext context) {
        Map<String, String> params = context.getParams();
        String type = params.get(CommonInfo.SOURCE_TYPE);
        if (type.equalsIgnoreCase(MessageEnum.SystemName.WORKFLOW.name())){
            processWorkflowMessage(params, context.getLineageHandler());
        } else if (type.equalsIgnoreCase(MessageEnum.SystemName.DATA_STUDIO.name())){
            processDataStudioMessage(params.get(CommonInfo.CONFIG), context.getLineageHandler());
        }
    }

    private void processDataStudioMessage(String dataJson, LineageHandler lineageHandler) {
        JSONObject jsonObject = JSON.parseObject(dataJson);
        String type = jsonObject.getString("operation");
        String workspaceName = jsonObject.getString("workspaceName");
        JSONObject data = jsonObject.getJSONObject("data");
        String projectName = jsonObject.getString("project");
        Referenceable projectRef = findExistProject(workspaceName, projectName);
        switch (type) {  // if project entity doesn't exist, create it first
            case "createWorkspace": {
                updateProjectDictionary(workspaceName, projectName);
                break;
            }
            case "deleteJar":
            case "createJar": { //ignore delete message
                if (projectRef != null){
                    resourceUpdateProc(projectRef, jsonObject, "JAR", type);
                } else {
                    updateProjectDictionary(workspaceName, projectName);
                }
                break;
            }
            case "createTable":
            case "dropTable": {
                String tableName = jsonObject.getJSONObject("data").getJSONObject("tableSchema").getString("tableName");
                if (projectRef != null){
                    tableUpdateProc(workspaceName, projectRef, tableName, type);
                } else {
                    updateProjectDictionary(workspaceName, projectName);
                }
            }
            case "runMR":
            case "runSQL":{
                updateInstanceProc(workspaceName, data.getString("instanceId"), null, lineageHandler);
                break;
            }
            case "createTablePartition":{
                if (projectRef != null){
                    updatePartitionProc(workspaceName, projectRef, jsonObject);
                } else {
                    updateProjectDictionary(workspaceName, projectName);
                }
                break;
            }
            default:
                break;
        }
    }

    private void updatePartitionProc(String workspaceName ,Referenceable projectRef, JSONObject jsonObject){
        String tableName = jsonObject.getJSONObject("data").getString("tableName");
        String tableQualifiedName = BaseJsonParser.formatQualifiedName((String) projectRef.get(BaseJsonParser.QUALIFIED_NAME), tableName);
        Referenceable table = null;
        try{
            table = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_TABLE.getValue(),
                BaseJsonParser.QUALIFIED_NAME, tableQualifiedName);
        } catch (AtlasServiceException e){
            LOG.error("Catch an atlas service exception", e);
        }
        if (table == null){
            tableUpdateProc(workspaceName, projectRef, tableName, "create");
        } else {
            Referenceable partition = new PartitionParser().createOnePartition(table, jsonObject);
            Id newPartitionId = notifyOneEntity(partition);
            JSONArray idArray = new JSONArray();
            Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) table.get("partitions");
            Iterator<Id> innerIt = innserSeq.iterator();
            while (innerIt.hasNext()) {
                idArray.put(InstanceSerialization.toJson(innerIt.next().getId(), true));
            }
            idArray.put(InstanceSerialization.toJson(newPartitionId.getId(), true));
            try {
                AtlasClientFactory.getAtlasClient().updateEntityAttribute(table.getId().id, "partitions", idArray.toString());
            } catch (AtlasServiceException e) {
                LOG.error("Catch an atlas service exception", e);
            }
        }
    }

    private Referenceable findExistProject(String workspaceName, String projectName) {
        //generate qualifiedName for project
        String dictionary = ddpClient.getDictionary(workspaceName, projectName, null);
        JSONObject projectJson = JSON.parseObject(dictionary).getJSONObject("packageMeta").getJSONObject("projects").
                                                                            getJSONObject(projectName);
        if (projectJson != null){
            try {
                String qualifiedName = BaseJsonParser.getProjectQualifiedName(projectJson);
                Referenceable project = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_PROJECT.getValue(),
                    BaseJsonParser.QUALIFIED_NAME, qualifiedName);
                return  project;
            } catch (AtlasServiceException e) {
                LOG.error("Catch an atlas service exception", e);
            }
        }
        return null;
    }

    private void resourceUpdateProc(Referenceable projectRef, JSONObject data, String resourceType, String oper) {
        Referenceable resource = new ResourceParser().createResource(projectRef, data, resourceType);
        if (oper.contains("create")) {
            Id resourceId = notifyOneEntity(resource);
            //update project
            JSONArray idArray = new JSONArray();
            Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) projectRef.get("resources");
            Iterator<Id> innerIt = innserSeq.iterator();
            while (innerIt.hasNext()) {
                idArray.put(InstanceSerialization.toJson(innerIt.next().getId(), true));
            }
            idArray.put(InstanceSerialization.toJson(resourceId.getId(), true));
            try {
                AtlasClientFactory.getAtlasClient().updateEntityAttribute(projectRef.getId().id, "resources", idArray.toString());
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    private void tableUpdateProc(String workspaceName, Referenceable projectRef, String tableName, String oper){
        List<Referenceable> entities = new TableParser().createTableEntity(ddpClient, workspaceName, projectRef, tableName);
        Referenceable table = entities.get(0);

        if (oper.contains("create")){
            notifyEntity(entities);
            //update table
            String tableQualifiedName = (String)table.get(BaseJsonParser.QUALIFIED_NAME);
            Id newTableId = BaseJsonParser.updatePartitionsOfTable(tableQualifiedName);
            //update project
            JSONArray idArray = new JSONArray();
            Wrappers.SeqWrapper<Id> innserSeq = (Wrappers.SeqWrapper<Id>) projectRef.get("tables");
            Iterator<Id> innerIt = innserSeq.iterator();
            while (innerIt.hasNext()) {
                idArray.put(InstanceSerialization.toJson(innerIt.next().getId(), true));
            }
            idArray.put(InstanceSerialization.toJson(newTableId.getId(), true));
            try {
                AtlasClientFactory.getAtlasClient().updateEntityAttribute(projectRef.getId().id, "tables", idArray.toString());
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProjectDictionary(String workspaceName, String projectName) {
        String dictionary = ddpClient.getDictionary(workspaceName, projectName, null);
        // parse data
        ParserContext pscontext = new ParserContext();
        List<Referenceable> entities = new ArrayList<>();
        try {
            parsePackage(dictionary, pscontext, entities);
            parseDictionary(workspaceName, dictionary, pscontext, entities);
            collectReferenceables(entities, pscontext);
            notifyEntity(entities);
            //更新project和table下挂的列表
            if (BaseJsonParser.getProjectRefe() != null){
                for (Referenceable project: BaseJsonParser.getProjectRefe().values()){
                    BaseJsonParser.updatePackagesOfProject(project);
                    BaseJsonParser.updateResourceOfProject(project);
                    BaseJsonParser.updateTablesOfProject(project);
                }
            }
            if (BaseJsonParser.getTableRefe() != null){
                for (Referenceable table: BaseJsonParser.getTableRefe().values()){
                    BaseJsonParser.updatePartitionsOfTable((String)table.get(BaseJsonParser.QUALIFIED_NAME));
                }
            }
        } catch (AtlasException e) {
            e.printStackTrace();
        }
    }

    private void processWorkflowMessage(Map<String, String> params, LineageHandler lineageHandler) {
        //1.获取请求参数
        String config = params.get("config");
        String instanceId = params.get("instanceId");
        String actionGuid = params.get("actionGuid");
        String wsName = params.get("workspaceName");
        updateInstanceProc(wsName, instanceId, actionGuid, lineageHandler);
    }

    public void updateInstanceProc(String wsName, String instanceId, String actionGuid, LineageHandler lineageHandler){
        try {
            String tableName = null;
            String intanceSummay = null;
            String dictionary = null;
            String projectName = null;
            //2.查询ddp获取具体信息
            if (instanceId != null) {
                intanceSummay = ddpClient.getInstanceSummary(wsName, instanceId);
            }
            if (intanceSummay != null) {
                // parse project name
                JSONObject instSummary = JSON.parseObject(intanceSummay);
                projectName = instSummary.getString("projectName");
            }

            if (wsName != null && tableName != null) {
                //yuanyongxian
                dictionary = ddpClient.getDictionary(wsName, projectName, tableName);
            } else if (wsName != null) {
                //yuanyongxian
                dictionary = ddpClient.getDictionary(wsName, projectName, null);
            }
            //3.解析数据
            ParserContext pscontext = new ParserContext();
            List<Referenceable> entities = new ArrayList<>();
            parsePackage(dictionary, pscontext, entities);
            parseInstance(intanceSummay, dictionary, pscontext, entities, wsName);
            parseDictionary(wsName, dictionary, pscontext, entities);
            collectReferenceables(entities, pscontext);
            //4.创建数据
            notifyEntity(entities);
            //更新project和table下挂的列表
            if (BaseJsonParser.getProjectRefe() != null) {
                for (Referenceable project : BaseJsonParser.getProjectRefe().values()) {
                    BaseJsonParser.updatePackagesOfProject(project);
                    BaseJsonParser.updateResourceOfProject(project);
                    BaseJsonParser.updateTablesOfProject(project);
                }
            }
            if (BaseJsonParser.getTableRefe() != null) {
                for (Referenceable table : BaseJsonParser.getTableRefe().values()) {
                    BaseJsonParser.updatePartitionsOfTable((String) table.get(BaseJsonParser.QUALIFIED_NAME));
                }
            }
            // update lineageHandler
            if (lineageHandler != null){
                lineageHandler.addToLineage(new ArrayList(pscontext.inputTables),
                                            new ArrayList(pscontext.outputTables),
                                            new ArrayList(pscontext.inputDbs),
                                            new ArrayList(pscontext.outputDbs));
            }

            //5.更新工作流属性
            if ((actionGuid != null) && !MockHolder.isMockNotify()){
                notifyEntityForUpdate(actionGuid, "etlInstance", pscontext.getValue(OdpsDataTypes.ODPS_INSTANCE));
            }

        } catch (Exception e) {
            LOG.error("创建ODPS实体异常", e);
        } finally {
            clearRefes();
        }
    }

    private void createEntitiesWithoutNofity(List<Referenceable> entities) throws AtlasServiceException {
        JSONArray entitiesArray = new JSONArray();
        for (Referenceable entity : entities) {
            if (entity == null) {
                continue;
            }
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
        }
        JSONArray guids = dgiCLient.createEntity(entitiesArray);
        String definition = "{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Struct\"," +
                "\"typeName\":\""+ORGANIZATIONS+"\",\"values\":{\"name\":\"会计处\"," +
                "\"phone\":\"15254584154\",\"address\":\"杭州\",\"description\":\"集团财务处理等\"}}";
        try {
            dgiCLient.addTraits(guids.getString(0),definition);
        } catch (Exception e) {
            LOG.error("创建definition异常", e);
            //ignore
        }
    }

    /**
     * @param dictionary
     * @param pscontext
     * @param entities
     * @throws AtlasException
     */
    private void parsePackage(String dictionary, ParserContext pscontext, List<Referenceable> entities) throws AtlasException {
        JSONObject dictionaryJsonObject = JSON.parseObject(dictionary);
        ParserFactory.getPackageParser().parse(pscontext, dictionaryJsonObject.getJSONObject("packageMeta"));
        collectProjects(entities);
    }

    private void collectProjects(List<Referenceable> entities) {
        Map<String, Referenceable> projectRefe = BaseJsonParser.getProjectRefe();
        if (projectRefe != null && !projectRefe.isEmpty()) {
            entities.addAll(projectRefe.values());
        }
    }

    protected void notifyEntity(List<Referenceable> entities) {
        try {
            List<Referenceable> newEntities = new ArrayList<>();
            JSONArray entitiesArray = new JSONArray();
            for (Referenceable entity : entities) {
                if (entity == null) {
                    continue;
                }
                if (entity.getId()._getId().startsWith("-") && (entity.get(BaseJsonParser.QUALIFIED_NAME) != null)){
                    newEntities.add(entity);
                } else {
                    continue;
                }
                String entityJson = InstanceSerialization.toJson(entity, true);
                entitiesArray.put(entityJson);
            }
            JSONArray guids = AtlasClientFactory.getAtlasClient().createEntity(entitiesArray);
            Map<String, Id> qualifiedNameToId = BaseJsonParser.getIdsMap();
            for(int i= 0; i< guids.length(); i++){
                Referenceable referenceable = newEntities.get(i);
                String guid = guids.getString(i);
                if(!qualifiedNameToId.containsKey(referenceable.get(BaseJsonParser.QUALIFIED_NAME))){
                    qualifiedNameToId.put((String)referenceable.get(BaseJsonParser.QUALIFIED_NAME),
                        new Id(guid, referenceable.getId().getVersion(), referenceable.getTypeName()));
                }
            }
        } catch (AtlasServiceException e) {
            LOG.error("Create entities error :",  e);
        } catch (JSONException e) {
            LOG.error("Create json error :", e);
        }
    }

    public Id notifyOneEntity(Referenceable entity){
        try {
            JSONArray entitiesArray = new JSONArray();
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
            JSONArray guids= AtlasClientFactory.getAtlasClient().createEntity(entitiesArray);
            return new Id(guids.getString(0), entity.getId().getVersion(), entity.getTypeName());
        } catch (AtlasServiceException e) {
            LOG.error("Create one entity error :", e);
        } catch (JSONException e) {
            LOG.error("Create json error :", e);
        }

        return  null;
    }

    /**
     * @param entities
     */
    private void collectReferenceables(List<Referenceable> entities, ParserContext pscontext) {
        collectProjects(entities);
        Map<String, Referenceable> tableRefe = BaseJsonParser.getTableRefe();
        if (tableRefe != null && !tableRefe.isEmpty()) {
            entities.addAll(tableRefe.values());
        }
        Map<String, Referenceable> partitionRefe = BaseJsonParser.getPartitionRefe();
        if (partitionRefe != null && !partitionRefe.isEmpty()) {
            entities.addAll(partitionRefe.values());
        }
        List<Referenceable> resources = (List<Referenceable>) pscontext.getValue(OdpsDataTypes.ODPS_RESOURCE);
        if (resources != null && !resources.isEmpty()) {
            entities.addAll(resources);
        }
        List<Referenceable> partitions = (List<Referenceable>) pscontext.getValue(OdpsDataTypes.ODPS_PARTITION);
        if (partitions != null && !partitions.isEmpty()) {
            entities.addAll(partitions);
        }
        List<Referenceable> packages = (List<Referenceable>) pscontext.getValue(OdpsDataTypes.ODPS_PACKAGE);
        if (packages != null && !packages.isEmpty()){
            entities.addAll(packages);
        }
    }

    /**
     * @param intanceSummay
     * @param pscontext
     * @param entities
     * @throws AtlasException
     */
    private void parseInstance(String intanceSummay, String dictionary, ParserContext pscontext, List<Referenceable> entities, String workspaceName) throws AtlasException {
        if (intanceSummay != null) {
            ParserFactory.getInstanceParser().parse(pscontext, intanceSummay, dictionary, workspaceName);
            Referenceable instance = (Referenceable) pscontext.getValue(OdpsDataTypes.ODPS_INSTANCE);
            collectProjects(entities);
            entities.add(instance);
        }
    }

    /**
     * @param wsName
     * @param dictionary
     * @param pscontext
     * @param entities
     * @throws AtlasException
     */
    private void parseDictionary(String wsName, String dictionary, ParserContext pscontext, List<Referenceable> entities) throws AtlasException {
        if (dictionary != null) {
            ParserFactory.getProjectParser().parse(pscontext, JSON.parseObject(dictionary).getJSONObject("projectMeta"));
            List<Referenceable> values = new ArrayList<>();
            values.addAll(BaseJsonParser.getProjectRefe().values());
            do {
                for (Referenceable projectRef : values) {
                    if (!parsedProject.contains(projectRef)) {
                        String projectDict = ddpClient.getProjectRelatedDictionary(wsName, String.valueOf(projectRef.get("name")));
                        ParserFactory.getResourceParser().parse(pscontext, projectDict, projectRef);
                        JSONObject dictMeta = JSON.parseObject(projectDict);
                        ParserFactory.getTablesParser().parse(pscontext, dictMeta.getJSONArray("tablesMeta"), projectRef, dictMeta.getJSONObject("projectMeta"));
                        parsedProject.add(projectRef);
                    }
                }
                values = new ArrayList<>();
                values.addAll(BaseJsonParser.getProjectRefe().values());
            } while (values.size() > parsedProject.size());
        } else {
            throw new BridgeException("查询字典信息失败," + wsName);
        }
    }

    /**
     * 清空集合信息，避免内存泄漏.
     */
    private void clearRefes() {
        Map<String, Referenceable> partitionRefe = BaseJsonParser.getPartitionRefe();
        if (partitionRefe != null) {
            partitionRefe.clear();
        }
        Map<String, Referenceable> projectRefe = BaseJsonParser.getProjectRefe();
        if (projectRefe != null) {
            projectRefe.clear();
        }
        Map<String, Referenceable> tableRefe = BaseJsonParser.getTableRefe();
        if (tableRefe != null) {
            tableRefe.clear();
        }
    }

    public static void main(String[] args) throws Exception {
        OdpsHook hook = new OdpsHook();
        HookContext context = new HookContext();
        context.putParam("workspaceName", "ddd");
        context.putParam("instanceId", "ddd");
        context.putParam(CommonInfo.SOURCE_TYPE, "WORKFLOW");
        hook.startMock();
        hook.run(context);
//        hook.testJersyClient();
    }

    public void testJersyClient() {
        String dictionary = ddpClient.getInstanceSummary("", "");
        System.out.println(dictionary);
    }

    public void startMock() {
        MockHolder.startMock();
    }

    public static class MockHolder {
        private static boolean mockDdp = false;
        private static boolean mockNotify = false;
        private static boolean quickStart = false;

        public static void startMock() {
            mockDdp = true;
            mockNotify = true;
        }

        public static void quickStart() {
            mockDdp = true;
            quickStart = true;
            mockNotify = true;
        }

        public static boolean isMockDdp() {
            return mockDdp;
        }

        public static boolean isQuickStart() {
            return quickStart;
        }

        public static boolean isMockNotify() {
            return mockNotify;
        }

        public static void setMockNotify(boolean mockNotify) {
            MockHolder.mockNotify = mockNotify;
        }
    }

}
