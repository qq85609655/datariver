package org.apache.atlas.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.graphdb.vertices.CacheVertex;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.groovy.GremlinGroovyPipeline;
import org.apache.atlas.AtlasException;
import org.apache.atlas.ErrorEnum;
import org.apache.atlas.discovery.DiscoveryService;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.repository.MetadataRepository;
import org.apache.atlas.repository.RepositoryException;
import org.apache.atlas.repository.graph.GraphProvider;
import org.apache.atlas.typesystem.ITypedReferenceableInstance;
import org.apache.atlas.typesystem.exception.EntityNotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 描述
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-20 9:07
 */
@Singleton
public class DefaultDataMapService implements DataMapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataMapService.class);

    private final TitanGraph titanGraph;

    private final MetadataService metadataService;
    private final MetadataRepository repository;
    private final DiscoveryService discoveryService;
    private final OrganizationService organizationService;

    @Inject
    public DefaultDataMapService(GraphProvider<TitanGraph> graphProvider, MetadataService metadataService, DiscoveryService discoveryService,
                                 OrganizationService organizationService, MetadataRepository repository) {
        this.titanGraph = graphProvider.get();
        this.metadataService = metadataService;
        this.discoveryService = discoveryService;
        this.organizationService = organizationService;
        this.repository = repository;
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static SimpleDateFormat getFORMAT() {
        return FORMAT;
    }

    @Override
    public String selectEntityDefinitions(Map<String, Object> properties, String startTime, String endTime) throws AtlasException {
        try {
            Date start = FORMAT.parse(startTime);
            Date end = FORMAT.parse(endTime);
            List<String> guids = selectByPropertiesAndTime(properties, start.getTime(), end.getTime());
            JSONArray result = new JSONArray();
            for (String guid : guids) {
                result.put(new JSONObject(metadataService.getEntityDefinition(guid)));
            }
            return result.toString();
        } catch (ParseException e) {
            throw new AtlasException(ErrorEnum.TIME_FORMAT_ERROR, e);
        } catch (Exception e) {
            throw new AtlasException(ErrorEnum.SYS_ERR, e);
        }
    }

    @Override
    public List<ITypedReferenceableInstance> getTypedEntities(String typeName, String startTime, String endTime) throws AtlasException {
        try {
            Date start = FORMAT.parse(startTime);
            Date end = FORMAT.parse(endTime);
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.ENTITY_TYPE_PROPERTY_KEY, typeName);
            Set<ITypedReferenceableInstance> iTypedReferenceableInstances = new HashSet<>();
            iTypedReferenceableInstances.addAll(selectEntities(properties, start.getTime(), end.getTime()));
            for (String type : organizationService.getTypeWithChildTypes().get(typeName)) {
                properties.put(Constants.ENTITY_TYPE_PROPERTY_KEY, type);
                iTypedReferenceableInstances.addAll(selectEntities(properties, start.getTime(), end.getTime()));
            }
            return new ArrayList<>(iTypedReferenceableInstances);
        } catch (ParseException e) {
            throw new AtlasException(ErrorEnum.TIME_FORMAT_ERROR, e);
        } catch (Exception e) {
            throw new AtlasException(ErrorEnum.SYS_ERR, e);
        }
    }

    @Override
    public List<ITypedReferenceableInstance> getEntitesWithSamePropertyGuid(String typeName, String guid) throws AtlasException {
        List<ITypedReferenceableInstance> entities = new ArrayList<>();
        String gremlinQuery = "g.V(\"__guid\",\"" + guid + "\").in.has(\"__typeName\",\"" + typeName + "\")";
        Set<String> ids = getEntityGuidsByGremlinQuery(gremlinQuery);
        //查找当前type
        Set<String> childrenTypes = organizationService.getTypeWithChildTypes().get(typeName);
        //处理子类型
        if (CollectionUtils.isNotEmpty(childrenTypes)) {
            for (String type : childrenTypes) {
                gremlinQuery = "g.V(\"__guid\",\"" + guid + "\").in.has(\"__typeName\",\"" + type + "\")";
                ids.addAll(getEntityGuidsByGremlinQuery(gremlinQuery));
            }
        }
        for (String id : ids) {
            entities.add(repository.getEntityDefinition(id));
        }
        return entities;
    }

    @Override
    public Set<String> getTaskGuidsByTableOrContainerGuid(String tableOrContainer, String guid) throws AtlasException {
        List<ITypedReferenceableInstance> etlTask = getTaskEntities("ETLTask");
        Set<String> hitTaskIds = new HashSet<>();
        //查询表相关task
        if (StringUtils.equalsIgnoreCase(tableOrContainer, "table")) {
            findHitTasksByTableGuid(guid, etlTask, hitTaskIds);
        } else {
            //查询库相关task
            List<ITypedReferenceableInstance> entitesByPropertyGuid = getEntitesWithSamePropertyGuid("DataTable", guid);
            for (ITypedReferenceableInstance table : entitesByPropertyGuid) {
                findHitTasksByTableGuid(table.getId().id, etlTask, hitTaskIds);
            }
        }
        return hitTaskIds;
    }

    @Override
    public List<ITypedReferenceableInstance> selectEntities(Map<String, Object> properties, long startTime, long endTime) throws AtlasException {
        List<String> guids = selectByPropertiesAndTime(properties, startTime, endTime);
        List<ITypedReferenceableInstance> entities = new ArrayList<>();
        for (String guid : guids) {
            entities.add(repository.getEntityDefinition(guid));
        }
        return entities;
    }

    @Override
    public Set<String> getEntityGuidsByGremlinQuery(String gremlinQuery) throws AtlasException {
        LOGGER.info("Executing getEntityGuidsByGremlinQuery query={}", gremlinQuery);
        Set<String> ids = new HashSet<>();
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("gremlin-groovy");
            Bindings bindings = engine.createBindings();
            bindings.put("g", titanGraph);
            Object o = engine.eval(gremlinQuery, bindings);
            GremlinGroovyPipeline pipeline = (GremlinGroovyPipeline) o;
            while (pipeline.hasNext()) {
                CacheVertex next = (CacheVertex) pipeline.next();
                String id = next.getProperty(Constants.GUID_PROPERTY_KEY);
                if (id != null) {
                    ids.add(id);
                }
            }
        } catch (Exception se) {
            throw new AtlasException(ErrorEnum.GREMLIN_QUERY_ERROR, se);
        }
        return ids;
    }

    /**
     * 根据属性和时间查询
     *
     * @param properties
     * @param startTime
     * @param endTime
     * @return
     */
    private List<String> selectByPropertiesAndTime(Map<String, Object> properties, long startTime, long endTime) {
        List<String> guids = new ArrayList<>();
        TitanGraphQuery<? extends TitanGraphQuery> query = titanGraph.query();
        for (String pKey : properties.keySet()) {
            query.has(pKey, properties.get(pKey));
        }
        Iterator<Vertex> iterator = query.interval(Constants.TIMESTAMP_PROPERTY_KEY, startTime, endTime).vertices().iterator();
        while (iterator.hasNext()) {
            Vertex vertex = iterator.next();
            String guid = vertex.getProperty(Constants.GUID_PROPERTY_KEY);
            if (guid != null) {
                guids.add(guid);
            }
        }
        return guids;
    }

    private void findHitTasksByTableGuid(String guid, List<ITypedReferenceableInstance> etlTask, Set<String> hitTaskIds) throws AtlasException {
        for (ITypedReferenceableInstance task : etlTask) {
            Set<String> tableGuidsInTask = new HashSet<>();
            ITypedReferenceableInstance lineage = (ITypedReferenceableInstance) task.get("lineage");
            if (lineage == null) {
                return;
            }
            List<ITypedReferenceableInstance> inputTables = (List<ITypedReferenceableInstance>) lineage.get("inputs");
            if (inputTables != null) {
                for (ITypedReferenceableInstance table : inputTables) {
                    tableGuidsInTask.add(table.getId().id);
                }
            }
            List<ITypedReferenceableInstance> outTables = (List<ITypedReferenceableInstance>) lineage.get("outputs");
            if (outTables != null) {
                for (ITypedReferenceableInstance table : outTables) {
                    tableGuidsInTask.add(table.getId().id);
                }
            }
            if (tableGuidsInTask.contains(guid)) {
                hitTaskIds.add(task.getId().id);
            }
        }
    }

    private List<ITypedReferenceableInstance> getTaskEntities(String type) throws RepositoryException,EntityNotFoundException {
        List<ITypedReferenceableInstance> taskEntities = new ArrayList<>();
        List<String> entities = new ArrayList<>();
        entities.addAll(repository.getEntityList(type));
        for (String childType : organizationService.getTypeWithChildTypes().get(type)) {
            entities.addAll(repository.getEntityList(childType));
        }
        for (String guidResult : entities) {
            taskEntities.add(repository.getEntityDefinition(guidResult));
        }
        return taskEntities;
    }

    public TitanGraph getTitanGraph() {
        return titanGraph;
    }
}
