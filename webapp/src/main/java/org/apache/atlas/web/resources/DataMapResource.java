package org.apache.atlas.web.resources;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.ErrorEnum;
import org.apache.atlas.utils.ParamChecker;
import org.apache.atlas.discovery.DiscoveryException;
import org.apache.atlas.discovery.DiscoveryService;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.repository.MetadataRepository;
import org.apache.atlas.services.DataMapService;
import org.apache.atlas.services.MetadataService;
import org.apache.atlas.services.OrganizationService;
import org.apache.atlas.typesystem.IStruct;
import org.apache.atlas.typesystem.ITypedReferenceableInstance;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.codehaus.enunciate.jaxrs.SampleRequest;
import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * 数据地图资源
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-17 16:58
 */
@Path("datamap")
@Singleton
@JsonRootType
@JsonName("datamap")
public class DataMapResource {
    private static final Logger LOG = LoggerFactory.getLogger(DataMapResource.class);
    public static final String TRAITS = "traits";
    public static final String PATTERN = "yyyy-MM-dd";
    private final MetadataService metadataService;
    private final MetadataRepository repository;
    private final DiscoveryService discoveryService;
    private final OrganizationService organizationService;
    private final DataMapService dataMapService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat();

    @Inject
    public DataMapResource(MetadataService metadataService, DiscoveryService discoveryService,
                           OrganizationService organizationService, MetadataRepository repository,
                           DataMapService dataMapService) {
        this.metadataService = metadataService;
        this.discoveryService = discoveryService;
        this.organizationService = organizationService;
        this.repository = repository;
        this.dataMapService = dataMapService;
    }

    /**
     * 获取管理的数据库数目和相关信息.
     *
     * @return 按库类型统计数据库个数，并列出所有的数据库，数据库信息包括名称和GUID.
     */
    @GET
    @Path("warehouse")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getWareHouseStatistics() {
        try {
            JSONObject response = new JSONObject();
            String dataContainers = discoveryService.searchByDSL("DataContainer");
            JSONArray rows = new JSONObject(dataContainers).getJSONArray("rows");
            Map<String, List<JSONObject>> dbsGroupedByDbType = new HashMap<>();
            for (int i = 0; i < rows.length(); i++) {
                JSONObject dbRow = rows.getJSONObject(i);
                String name = dbRow.getString("name");
                String guid = dbRow.getJSONObject("$id$").getString("id");
                String dbType = dbRow.getString("dbType");
                List<JSONObject> dbs = dbsGroupedByDbType.get(dbType);
                if (dbs == null) {
                    dbs = new ArrayList<>();
                    dbsGroupedByDbType.put(dbType, dbs);
                }
                JSONObject resultDbRow = new JSONObject();
                //组装需要的返回值
                resultDbRow.put("name", name);
                resultDbRow.put("GUID", guid);
                dbs.add(resultDbRow);
            }
            JSONArray results = new JSONArray();
            for (String dbTypekey : dbsGroupedByDbType.keySet()) {
                //本期只使用ODPS数据库
                if (!StringUtils.equalsIgnoreCase(dbTypekey, "ODPS")) {
                    continue;
                }
                List<JSONObject> values = dbsGroupedByDbType.get(dbTypekey);
                JSONObject dbtypeResultObj = new JSONObject();
                dbtypeResultObj.put("name", dbTypekey);
                dbtypeResultObj.put("dbCount", values.size());
                dbtypeResultObj.put("databases", values);
                results.put(dbtypeResultObj);
            }
            response.put(AtlasClient.RESULTS, results);
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("warehouse获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("warehouse获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取管理的库/表/字段/作业/资源/任务/组织等相关统计信息.
     *
     * @return 数据库个数、表个数、字段个数、作业个数、资源个数、组织个数。
     */
    @GET
    @Path("statistics")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getStatistics(@QueryParam("startTime") String startTime,
                                  @QueryParam("endTime") String endTime,
                                  @QueryParam("time") String time) {
        try {
            TimeScope timeScope = new TimeScope(startTime, endTime, time).parseTime();
            startTime = timeScope.getStartTime();
            endTime = timeScope.getEndTime();
            JSONObject response = new JSONObject();
            Set<String> traitNames = new HashSet<>();
            JSONObject dataContainer = getInnerOuterCount("DataContainer", startTime, endTime);
            collectTraits(traitNames, dataContainer);
            response.put("database", dataContainer);
            JSONObject dataTable = getInnerOuterCount("DataTable", startTime, endTime);
            collectTraits(traitNames, dataTable);
            response.put("table", dataTable);
            JSONObject dataField = getInnerOuterCount("DataField", startTime, endTime);
            collectTraits(traitNames, dataField);
            response.put("field", dataField);
            response.put("job", getJobCount(startTime, endTime));
            response.put("resource", getResouceCount(startTime, endTime));
            response.put("task", getTaskCount(startTime, endTime));
            response.put("organization", new JSONObject().put("count", traitNames.size()));
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private void collectTraits(Set<String> traitNames, JSONObject dataContainer) throws JSONException {
        JSONArray traits = (JSONArray) dataContainer.get(TRAITS);
        for (int i = 0; i < traits.length(); i++) {
            traitNames.add(traits.getString(i));
        }
        dataContainer.remove(TRAITS);
    }

    /**
     * 获取管理的库相关统计信息.
     *
     * @return
     */
    @GET
    @Path("/database/{guid}/statistics")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getDatabaseStatistics(@PathParam("guid") String guid) {
        try {
            ParamChecker.notEmpty(guid, "GUID");
            List<ITypedReferenceableInstance> tables = dataMapService.getEntitesWithSamePropertyGuid("DataTable",guid);
            Set<String> tablesOfCurrentDb = new HashSet<>();
            //大小暂取所有表大小之和
            long size = 0;
            if (tables != null) {
                for (ITypedReferenceableInstance instance : tables) {
                    tablesOfCurrentDb.add(instance.getId().id);
                    long tableSize = instance.getLong("size");
                    size += tableSize;
                }
            }
            Set<String> taskids = dataMapService.getTaskGuidsByTableOrContainerGuid("container", guid);
            Set<String> tableIds = producedTables(tablesOfCurrentDb, taskids);
            JSONObject response = new JSONObject();
            response.put("usedTasksCount", taskids.size());
            response.put("tablesCount", tablesOfCurrentDb.size());
            response.put("producedTablesCount", tableIds.size());
            response.put("size", size);
            response.put("usedTasks", taskids);
            response.put("producedTables", tableIds);
            response.put("tables", tablesOfCurrentDb);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("getDatabaseStatistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("getDatabaseStatistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取产出表
     *
     * @param tablesOfCurrentDb
     * @param taskids
     * @return
     * @throws AtlasException
     */
    private Set<String> producedTables(Set<String> tablesOfCurrentDb, Set<String> taskids) throws AtlasException {
        Set<String> tableIds = new HashSet<>();
        Map<Set<String>, Set<String>> inputOutputTables = getTableIdsFromTasks(taskids);
        for (Set<String> inputs : inputOutputTables.keySet()) {
            if (!CollectionUtils.union(tablesOfCurrentDb, inputs).isEmpty()) {
                //inputs中包含当前库中的某个表，认为：当前task的输出表均为当前库的产出
                tableIds.addAll(inputOutputTables.get(inputs));
            }
        }
        return tableIds;
    }

    /**
     * 获取task的input/output的表
     *
     * @param taskids
     * @return
     * @throws AtlasException
     */
    private Map<Set<String>, Set<String>> getTableIdsFromTasks(Set<String> taskids) throws AtlasException {
        Map<Set<String>, Set<String>> inputOutputTables = new HashMap<>();
        for (String taskId : taskids) {
            ITypedReferenceableInstance task = repository.getEntityDefinition(taskId);
            Object lineage = task.get("lineage");
            if (lineage != null && lineage instanceof ITypedReferenceableInstance) {
                ITypedReferenceableInstance lineageRef = (ITypedReferenceableInstance) lineage;
                Object outputs = lineageRef.get("outputs");
                Object inputs = lineageRef.get("inputs");
                Set<String> outputTableIds = new HashSet<>();
                Set<String> inputTableIds = new HashSet<>();
                if (outputs != null && outputs instanceof List) {
                    List<ITypedReferenceableInstance> outTables = (List<ITypedReferenceableInstance>) outputs;
                    for (ITypedReferenceableInstance table : outTables) {
                        outputTableIds.add(table.getId().id);
                    }
                }
                if (inputs != null && inputs instanceof List) {
                    List<ITypedReferenceableInstance> inTables = (List<ITypedReferenceableInstance>) inputs;
                    for (ITypedReferenceableInstance table : inTables) {
                        inputTableIds.add(table.getId().id);
                    }
                }
                inputOutputTables.put(ImmutableSet.copyOf(inputTableIds), outputTableIds);
            }
        }
        return inputOutputTables;
    }

    /**
     * 获取管理的表相关统计信息.
     *
     * @return
     */
    @GET
    @Path("/table/{guid}/statistics")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getTableStatistics(@PathParam("guid") String guid) {
        try {
            ParamChecker.notEmpty(guid, "GUID");
            ITypedReferenceableInstance tableRef = repository.getEntityDefinition(guid);
            List<String> fieldIds = getListPropertyValueIds(tableRef, "fields");
            List<String> partitionKeys = getListPropertyValueIds(tableRef, "partitionKeys");
            List<String> partitions = getListPropertyValueIds(tableRef, "partitions");
            Set<String> taskIds = dataMapService.getTaskGuidsByTableOrContainerGuid("table", guid);
            Set<String> currentTableId = new HashSet<>();
            currentTableId.add(guid);
            Set<String> producedTables = producedTables(currentTableId, taskIds);
            JSONObject response = new JSONObject();
            response.put("usedTasksCount", taskIds.size());
            response.put("producedTablesCount", producedTables.size());
            response.put("usedTasks", taskIds);
            response.put("producedTables", producedTables);
            response.put("fieldsCount", fieldIds.size());
            response.put("fields", fieldIds);
            response.put("partitionKeysCount", partitionKeys.size());
            response.put("partitionKeys", partitionKeys);
            response.put("partitionsCount", partitions.size());
            response.put("partitions", partitions);
            response.put("size",tableRef.getLong("size"));
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("getTableStatistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("getTableStatistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private List<String> getListPropertyValueIds(ITypedReferenceableInstance tableRef, String fields1) throws AtlasException {
        List<ITypedReferenceableInstance> fields = (List<ITypedReferenceableInstance>) tableRef.get(fields1);
        List<String> fieldIds = new ArrayList<>();
        if (fields != null) {
            for (ITypedReferenceableInstance instance : fields) {
                fieldIds.add(instance.getId().id);
            }
        }
        return fieldIds;
    }

    /**
     * 查询具有相同的property的实体.
     *
     * @return 具有相同属性的实体列表。
     */
    @GET
    @Path("samePropertyEntities")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getEntitesBySamePropertyGuid(@QueryParam("typeName") String typeName,
                                                 @QueryParam("guid") String guid) {
        try {
            ParamChecker.notEmpty(typeName, "typeName");
            ParamChecker.notEmpty(guid, "GUID");
            JSONObject response = new JSONObject();
            List<ITypedReferenceableInstance> entities =
                    dataMapService.getEntitesWithSamePropertyGuid(typeName, guid);
            JSONArray results = new JSONArray();
            for (ITypedReferenceableInstance instance : entities) {
                results.put(new JSONObject(metadataService.getEntityDefinition(instance.getId().id)));
            }
            response.put(AtlasClient.RESULTS, results);
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 全文查询实体信息
     *
     * @return
     */
    @GET
    @Path("fullTextSearch")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getEntitesByFullTextSearch(@QueryParam("type") String type,
                                               @QueryParam("query") String query,
                                               @QueryParam("startTime") String startTime,
                                               @QueryParam("endTime") String endTime) {
        try {
            ParamChecker.notEmpty(type, "type");
            ParamChecker.notEmpty(query, "query");
            JSONObject response = new JSONObject();
            List<String> entitiyGuids = new ArrayList<>();
            JSONArray searchResult = new JSONArray(discoveryService.searchByFullText(query));
            Set<String> typeNames = getTypes(type);
            for (int i = 0; i < searchResult.length(); i++) {
                JSONObject entityObj = searchResult.getJSONObject(i);
                String typeName = entityObj.getString("typeName");
                if (typeNames.contains(typeName.toUpperCase())) {
                    entitiyGuids.add(entityObj.getString("guid"));
                }
            }
            //todo 根据时间过滤下
            JSONArray results = new JSONArray();
            for (String guid : entitiyGuids) {
                results.put(new JSONObject(metadataService.getEntityDefinition(guid)));
            }
            response.put(AtlasClient.RESULTS, results);
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取所有类型，包括子类型
     *
     * @param type
     * @return
     */
    private Set<String> getTypes(String type) {
        String[] types = type.split(",");
        Set<String> typeNames = new HashSet<>();
        for (String typea : types) {
            if (StringUtils.isNotEmpty(typea)) {
                typeNames.add(typea.toUpperCase());
                //查找子类
                Set<String> childrenTypes = organizationService.getTypeWithChildTypes().get(typea);
                if (CollectionUtils.isNotEmpty(childrenTypes)) {
                    for (String t : childrenTypes) {
                        typeNames.add(t.toUpperCase());
                    }
                }
            }
        }
        return typeNames;
    }

    /**
     * 查询最近某段时间创建的实体信息
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @GET
    @Path("recently")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getRecentlyEntites(@QueryParam("properties") String property,
                                       @QueryParam("type") String type,
                                       @QueryParam("startTime") String startTime,
                                       @QueryParam("endTime") String endTime,
                                       @QueryParam("time") String time) {
        try {
            TimeScope timeScope = new TimeScope(startTime, endTime, time).parseTime();
            startTime = timeScope.getStartTime();
            endTime = timeScope.getEndTime();
            JSONObject response = new JSONObject();
            Map<String, Object> properties = new HashedMap();
            parseProperties(property, properties);
            if (StringUtils.isNotEmpty(type)) {
                properties.put(Constants.ENTITY_TYPE_PROPERTY_KEY, type);
            }
            JSONArray results = new JSONArray(dataMapService.selectEntityDefinitions(properties, startTime, endTime));
            findEntitiesOfChildTypes(type, startTime, endTime, properties, results);
            response.put(AtlasClient.RESULTS, results);
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private void findEntitiesOfChildTypes(String type, String startTime,String endTime, Map<String, Object> properties, JSONArray results) throws JSONException, AtlasException {
        if(StringUtils.isNotEmpty(type)) {
            Set<String> typeNames = new HashSet<>();
            Set<String> childrenTypes = organizationService.getTypeWithChildTypes().get(type);
            if (CollectionUtils.isNotEmpty(childrenTypes)) {
                for (String t : childrenTypes) {
                    typeNames.add(t);
                }
            }
            for(String typea:typeNames){
                properties.put(Constants.ENTITY_TYPE_PROPERTY_KEY, typea);
                JSONArray result = new JSONArray(dataMapService.selectEntityDefinitions(properties, startTime, endTime));
                for (int i = 0; i < result.length(); i++) {
                    results.put(result.get(i));
                }
            }
        }
    }

    private static void parseProperties(@QueryParam("properties") String property, Map<String, Object> properties) {
        if (StringUtils.isNotEmpty(property)) {
            String[] ps = property.split(";");
            for (String prop : ps) {
                String[] pair = prop.split(",");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                        properties.put(key, value);
                    }
                }
            }
        }
    }

    /**
     * 根据表/容器（库）的guid查询使用该表/容器的所有ETLTask
     *
     * @param type
     * @param guid
     * @return
     */
    @GET
    @Path("tasks")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(DataMapResource.class)
    public Response getTaskEntitesByTableOrContainerGuid(@QueryParam("type") String type, @QueryParam("guid") String guid) {
        try {
            JSONObject response = new JSONObject();
            Set<String> hitTaskIds = dataMapService.getTaskGuidsByTableOrContainerGuid(type, guid);
            JSONArray results = new JSONArray();
            for (String id : hitTaskIds) {
                results.put(new JSONObject(metadataService.getEntityDefinition(id)));
            }
            response.put(AtlasClient.RESULTS, results);
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("statistics获取异常", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取资源数
     *
     * @return
     * @throws DiscoveryException
     * @throws JSONException
     */
    private JSONObject getResouceCount(String startTime, String endTime) throws AtlasException, JSONException {
        JSONObject resouce = new JSONObject();
        resouce.put("count", dataMapService.getTypedEntities("OdpsResource", startTime, endTime).size());
        return resouce;
    }
    /**
     * 获取资源数
     *
     * @return
     * @throws DiscoveryException
     * @throws JSONException
     */
    private JSONObject getTaskCount(String startTime, String endTime) throws AtlasException, JSONException {
        JSONObject resouce = new JSONObject();
        resouce.put("count", dataMapService.getTypedEntities("ETLTask", startTime, endTime).size());
        return resouce;
    }

    /**
     * 获取作业和作业模版数
     *
     * @return
     * @throws DiscoveryException
     * @throws JSONException
     */
    private JSONObject getJobCount(String startTime, String endTime) throws AtlasException, JSONException {
        JSONObject jobResult = new JSONObject();
        jobResult.put("jobCount", dataMapService.getTypedEntities("WorkflowJob", startTime, endTime).size());
        jobResult.put("templateCount", dataMapService.getTypedEntities("WorkflowTemplate", startTime, endTime).size());
        return jobResult;
    }

    /**
     * 获取库/表/字段的内部和外部数目。（内部目前指来自ODPS数据）
     *
     * @param typeName
     * @return
     * @throws DiscoveryException
     * @throws JSONException
     */
    private JSONObject getInnerOuterCount(String typeName, String startTime, String endTime) throws AtlasException, JSONException {
        List<ITypedReferenceableInstance> typedEntities = dataMapService.getTypedEntities(typeName, startTime, endTime);
        int odpsCount = 0;
        int allCount = typedEntities.size();
        Set<String> namePropertyOfTrait = new HashSet<>();
        for (ITypedReferenceableInstance instance : typedEntities) {
            String metaSource = String.valueOf(instance.get("metaSource"));
            if ("ODPS".equalsIgnoreCase(metaSource)) {
                odpsCount++;
            }
            IStruct trait = instance.getTrait(AtlasClient.ORGANIZATIONS);
            if (trait != null) {
                namePropertyOfTrait.add(String.valueOf(trait.get("name")));
            }
        }
        JSONObject db = new JSONObject();
        db.put("innerCount", odpsCount);
        db.put("outerCount", allCount - odpsCount);
        db.put(TRAITS, namePropertyOfTrait);
        return db;
    }
    public static class TimeScope {
        private String startTime;
        private String endTime;
        private String time;

        public TimeScope(String startTime, String endTime, String time) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.time = time;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public TimeScope parseTime() throws AtlasException {
            if (StringUtils.isEmpty(startTime)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN);
                ParamChecker.notEmpty(time, "时间参数");
                Calendar instance = Calendar.getInstance();
                instance.add(Calendar.DAY_OF_YEAR, 1);
                endTime = dateFormat.format(instance.getTime());
                if (time.equalsIgnoreCase("week")) {
                    instance.add(Calendar.DAY_OF_YEAR, -7);
                } else if (time.equalsIgnoreCase("month")) {
                    instance.add(Calendar.MONTH,-1);
                } else {
                    throw new AtlasException(ErrorEnum.ILLEGAL_ARGUMENT, time);
                }
                startTime = dateFormat.format(instance.getTime());
            } else {
                ParamChecker.notEmpty(startTime, "开始时间");
                ParamChecker.notEmpty(endTime, "结束时间");
            }
            LOG.debug("开始时间:"+startTime+";结束时间:"+endTime);
            return this;
        }

        @Override
        public String toString() {
            return "TimeScope{" +
                    "startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }
    }
}
