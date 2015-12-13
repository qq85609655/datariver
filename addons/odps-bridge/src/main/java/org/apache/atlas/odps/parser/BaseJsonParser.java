package org.apache.atlas.odps.parser;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.odps.utils.StringUtils;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.exception.BridgeException;
import org.apache.atlas.model.LineageDataTypes;
import org.apache.atlas.model.TransformDataTypes;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.client.DDPClient;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.model.OdpsMetaModelGenerator;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.typesystem.persistence.Id;
import org.codehaus.jettison.json.JSONArray;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public abstract class BaseJsonParser implements JsonParser {

    public static final String QUALIFIED_NAME = "qualifiedName";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String NAME = "name";
    protected static final String META_SOURCE = "metaSource";
    protected static final String META_SOURCE_VALUE = "ODPS";
    public static final String ODPS_PREFIX = "odps.";
    public static enum ENTITY_MAP_TYPE{
        PROJECT_PACKAGES,
        PROJECT_RESOURCES,
        PROJECT_TABLES,
        TABLE_PARTITIONS;
    }

    private static DDPClient ddpClient = DDPClient.getInstance();
    /**
     * 项目名称=项目Referenceable
     */
    private static ThreadLocal<Map<String, Referenceable>> projectRefe = new ThreadLocal<>();
    /**
     * table名称=table Referenceable,用于partition构建
     */
    private static ThreadLocal<Map<String, Referenceable>> tableRefe = new ThreadLocal<>();

    /**
     * 分区name=分区Referenceable
     */
    private static ThreadLocal<Map<String, Referenceable>> partitionRefe = new ThreadLocal<>();

    private static final Set<OdpsDataTypes> dataElementTypes = new HashSet<>();

    // qualifiedName=ID
    private static  ThreadLocal<Map<String, Id>> qualifiedNameToId = new ThreadLocal<>();

    // project qualifiedname To Packages List
    private static  ThreadLocal<Map<String, ArrayList<String>>> projectToPackages = new ThreadLocal<>();

    // project qualifiedname To Resources List
    private static  ThreadLocal<Map<String, ArrayList<String>>> projectToResources = new ThreadLocal<>();

    // project qualifiedname To Tables List
    private static  ThreadLocal<Map<String, ArrayList<String>>> projectToTables = new ThreadLocal<>();

    // Table qualifiedname To partitions List
    private static  ThreadLocal<Map<String, ArrayList<String>>> tableToPartitions = new ThreadLocal<>();

    static {
        dataElementTypes.add(OdpsDataTypes.ODPS_COLUMN);
        dataElementTypes.add(OdpsDataTypes.ODPS_INSTANCE);
        dataElementTypes.add(OdpsDataTypes.ODPS_PACKAGE);
        dataElementTypes.add(OdpsDataTypes.ODPS_ACCINFO);
        dataElementTypes.add(OdpsDataTypes.ODPS_PROJECT);
        dataElementTypes.add(OdpsDataTypes.ODPS_TABLE);
        dataElementTypes.add(OdpsDataTypes.ODPS_TASK);
    }

    @Override
    public void parse(ParserContext context, Object... data) throws AtlasException {
        Referenceable referenceable = newInstance(type());
        context.put(type(), referenceable);
        doParse(context, referenceable, data);
        collectRefes(context.getValue(type()));
        setMetaSource(context.getValue(type()));
    }

    protected Referenceable newInstance(OdpsDataTypes type) {
        String value = type.getValue();
        return new Referenceable(value, OdpsMetaModelGenerator.ODPS_TRAIT + value);
    }
    protected Referenceable newInstance(LineageDataTypes type) {
        String value = type.getValue();
        return new Referenceable(value);
    }
    protected Referenceable newInstance(TransformDataTypes type) {
        String value = type.getValue();
        return new Referenceable(value);
    }

    /**
     * 收集所有Project,方便创建实体
     *
     * @param referenceable
     */
    private void collectRefes(Object referenceable) {
        if (type() == OdpsDataTypes.ODPS_PROJECT) {//收集所有Project,方便创建实体
            collect(referenceable, projectRefe);
        } else if (type() == OdpsDataTypes.ODPS_PARTITION) {
            collect(referenceable, partitionRefe);
        } else if (type() == OdpsDataTypes.ODPS_TABLE) {
            collect(referenceable, tableRefe);
        }
    }

    private void setMetaSource(Object refes) {
        if (dataElementTypes.contains(type()) && refes != null) {
            if (refes instanceof Referenceable) {
                Referenceable referenceable = (Referenceable) refes;
                referenceable.set(META_SOURCE, META_SOURCE_VALUE);
            } else if (refes instanceof List) {
                List<Referenceable> referenceable = (List<Referenceable>) refes;
                for (Referenceable re : referenceable) {
                    re.set(META_SOURCE, META_SOURCE_VALUE);
                }
            }
        }
    }

    /**
     * 格式化qualifiedName,添加前缀 "odps."
     *
     * @param params
     * @return
     */
    public static String formatQualifiedName(String... params) {
        String formatedName = doFormat(params);
        if (!formatedName.startsWith(ODPS_PREFIX)) {
            formatedName = ODPS_PREFIX + formatedName;
        }
        return formatedName;
    }

    /**
     * 项目的qualifiedName
     * @param projectJson
     * @return
     */
    public static String getProjectQualifiedName(JSONObject projectJson) {
        return formatQualifiedName(getClusterName(projectJson), projectJson.getString(NAME));
    }

    public static void updateEntityMap(ENTITY_MAP_TYPE type, String key, String value){
        Map<String, ArrayList<String>> map = null;
        if (type.equals(ENTITY_MAP_TYPE.PROJECT_PACKAGES)){
            map = projectToPackages.get();
            if (map == null){
                map = new ConcurrentHashMap<>();
                projectToPackages.set(map);
            }
        } else if (type.equals(ENTITY_MAP_TYPE.PROJECT_RESOURCES)){
            map = projectToResources.get();
            if (map == null){
                map = new ConcurrentHashMap<>();
                projectToResources.set(map);
            }
        } else if (type.equals(ENTITY_MAP_TYPE.PROJECT_TABLES)){
            map = projectToTables.get();
            if (map == null){
                map = new ConcurrentHashMap<>();
                projectToTables.set(map);
            }
        } else if (type.equals(ENTITY_MAP_TYPE.TABLE_PARTITIONS)){
            map = tableToPartitions.get();
            if (map == null){
                map = new ConcurrentHashMap<>();
                tableToPartitions.set(map);
            }
        }
        if (map != null){
            ArrayList<String> values = map.get(key);
            if (values == null){
                values = new ArrayList<>();
                values.add(value);
                map.put(key, values);
            } else {
                if (!values.contains(value)){
                    values.add(value);
                }
            }
        }
    }
    /**
     * 格式化qualifiedName
     *
     * @param params
     * @return
     */
    private static String doFormat(String[] params) {
        List<String> nonEmptyParams = new ArrayList<>(params.length);
        for (String p : params) {
            if (!StringUtils.isNullOrEmpty(p)) {
                nonEmptyParams.add(p);
            }
        }
        StringBuilder formatString = new StringBuilder();
        for (int i = 0; i < nonEmptyParams.size(); i++) {
            formatString.append("%s").append(".");
        }
        //消除最后多余的"."
        if (formatString.charAt(formatString.length() - 1) == '.') {
            formatString.deleteCharAt(formatString.length() - 1);
        }
        return String.format(formatString.toString(), nonEmptyParams.toArray(new String[0]));
    }

    private static void collect(Object referenceable, ThreadLocal<Map<String, Referenceable>> refeMap) {
        if (referenceable == null) {
            return;
        }
        Map<String, Referenceable> stringReferenceableMap = refeMap.get();
        if (stringReferenceableMap == null) {
            stringReferenceableMap = new ConcurrentHashMap<>();
            refeMap.set(stringReferenceableMap);
        }
        if (referenceable instanceof Referenceable) {
            Referenceable reference = (Referenceable) referenceable;
            String name = (String) reference.get(QUALIFIED_NAME);
            if (!stringReferenceableMap.containsKey(name)) {
                stringReferenceableMap.put(name, reference);
            }
        } else if (referenceable instanceof List) {
            List<Referenceable> refes = (List<Referenceable>) referenceable;
            for (Referenceable ref : refes) {
                String name = (String) ref.get(QUALIFIED_NAME);
                if (!stringReferenceableMap.containsKey(name)) {
                    stringReferenceableMap.put(name, ref);
                }
            }
        } else {
            throw new BridgeException("context中的值，目前仅支持Reference类型及其List");
        }
    }

    public static Map<String, Referenceable> getProjectRefe() {
        return projectRefe.get();
    }

    public static Map<String, Referenceable> getPartitionRefe() {
        Map<String, Referenceable> referenceableMap = partitionRefe.get();
        if (referenceableMap == null) {
            referenceableMap = new ConcurrentHashMap<String, Referenceable>();
            partitionRefe.set(referenceableMap);
        }
        return partitionRefe.get();
    }

    public static Map<String, Referenceable> getTableRefe() {
        Map<String, Referenceable> stringReferenceableMap = tableRefe.get();
        if (stringReferenceableMap == null) {
            stringReferenceableMap = new ConcurrentHashMap<>();
            tableRefe.set(stringReferenceableMap);
        }
        return stringReferenceableMap;
    }

    public static Referenceable parseProject(ParserContext context, String qualifiedProjectName, Object... data) throws AtlasException {
        Map<String, Referenceable> projectRefes = getProjectRefe();
        if (projectRefes == null) {
            projectRefes = new ConcurrentHashMap<>();
            projectRefe.set(projectRefes);
        }
        Referenceable project = projectRefes.get(qualifiedProjectName);
        if (project != null){
            return project;
        }
        // check if project exists
        try {
             project = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_PROJECT.getValue(),
                 BaseJsonParser.QUALIFIED_NAME, qualifiedProjectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (project != null){
            context.put(OdpsDataTypes.ODPS_PROJECT, project);
            collect(project, projectRefe);
        } else {
            if (!projectRefes.containsKey(qualifiedProjectName)) {
                ParserFactory.getProjectParser().parse(context, data);
            }
        }
        return projectRefes.get(qualifiedProjectName);
    }

    public static Map<String, Id> getIdsMap(){
        Map<String, Id> qualifiedNameToIdMap = qualifiedNameToId.get();
        if (qualifiedNameToIdMap == null){
            qualifiedNameToIdMap = new ConcurrentHashMap<>();
            qualifiedNameToId.set(qualifiedNameToIdMap);
        }
        return qualifiedNameToIdMap;
    }

    protected String loadDict(String workspaceName,String projectName, String tableName) {
        return ddpClient.getDictionary(workspaceName, projectName, tableName);
    }

    public abstract void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException;

    protected void setStringWithSameKey(Referenceable currentEntity, JSONObject json, String key) {
        currentEntity.set(key, json.getString(key));
    }

    protected void setLongWithSameKey(Referenceable currentEntity, JSONObject json, String key) {
        currentEntity.set(key, json.getLong(key));
    }

    protected void setBooleanWithSameKey(Referenceable currentEntity, JSONObject json, String key) {
        currentEntity.set(key, json.getBooleanValue(key));
    }

    public static void updateTablesOfProject(Referenceable project){
        if ((projectToTables.get() != null) && (qualifiedNameToId.get() != null)){
            ArrayList<String> tables = projectToTables.get().get(project.get(QUALIFIED_NAME));
            JSONArray idArray = new JSONArray();
            Id projectId = qualifiedNameToId.get().get(project.get(QUALIFIED_NAME));
            for(String tableName:tables){
                Id tableId = qualifiedNameToId.get().get(tableName);
                if (tableId != null){
                    idArray.put(InstanceSerialization.toJson(tableId, true));
                }
            }
            try {
                if (idArray.length() != 0){
                    AtlasClientFactory.getAtlasClient().updateEntityAttribute(projectId._getId(), "tables", idArray.toString());
                }
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateResourceOfProject(Referenceable project){
        if ((projectToResources.get() != null) && (qualifiedNameToId.get() != null)){
            ArrayList<String> resources = projectToResources.get().get(project.get(QUALIFIED_NAME));
            JSONArray idArray = new JSONArray();
            Id projectId = qualifiedNameToId.get().get(project.get(QUALIFIED_NAME));
            for(String resource:resources){
                Id id = qualifiedNameToId.get().get(resource);
                if (id != null){
                    idArray.put(InstanceSerialization.toJson(id, true));
                }
            }
            try {
                if (idArray.length() != 0){
                    AtlasClientFactory.getAtlasClient().updateEntityAttribute(projectId._getId(), "resources", idArray.toString());
                }
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updatePackagesOfProject(Referenceable project){
        if ((projectToPackages.get() != null) && (qualifiedNameToId.get() != null)){
            ArrayList<String> packageNames = projectToPackages.get().get(project.get(QUALIFIED_NAME));
            JSONArray idArray = new JSONArray();
            Id projectId = qualifiedNameToId.get().get(project.get(QUALIFIED_NAME));
            for(String packageName : packageNames){
                Id id = qualifiedNameToId.get().get(packageName);
                if (id != null){
                    idArray.put(InstanceSerialization.toJson(id, true));
                }
            }
            try {
                if (idArray.length() != 0){
                    AtlasClientFactory.getAtlasClient().updateEntityAttribute(projectId._getId(), "packages", idArray.toString());
                }
            } catch (AtlasServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public static Id updatePartitionsOfTable(String tableQualifiedName){
        Id tableId = null;
        if (qualifiedNameToId.get() != null){
            tableId = qualifiedNameToId.get().get(tableQualifiedName);
            if ((tableToPartitions.get() != null) && (tableId != null)){
                ArrayList<String> partitions = tableToPartitions.get().get(tableQualifiedName);
                JSONArray idArray = new JSONArray();

                for(String partition : partitions){
                    Id id = qualifiedNameToId.get().get(partition);
                    if (id != null){
                        idArray.put(InstanceSerialization.toJson(id, true));
                    }
                }
                try {
                    if (idArray.length() != 0){
                        AtlasClientFactory.getAtlasClient().updateEntityAttribute(tableId._getId(), "partitions", idArray.toString());
                    }
                } catch (AtlasServiceException e) {
                    e.printStackTrace();
                }
            }
        }

        return tableId;
    }

    /**
     * 根据reference的name去除重复
     *
     * @param inputs
     * @return
     */
    protected List<Referenceable> makeUniqueByName(List<Referenceable> inputs) {
        if (inputs == null) {
            return null;
        }
        Map<String, Referenceable> newInputs = new HashMap<>();
        for (Referenceable obj : inputs) {
            if (!newInputs.containsKey(obj.get("name"))) {
                newInputs.put((String) obj.get("name"), obj);
            }
        }
        return new ArrayList<>(newInputs.values());
    }

    /**
     * 添加引用到已有集合中，如果没有则创建
     * @param context
     * @param listNeedToBeAdded
     * @return
     */
    protected List<Referenceable> addCurrentTypeListReferenceables(ParserContext context, List<Referenceable> listNeedToBeAdded) {
        Object value = context.getValue(type());
        List<Referenceable> referenceables = null;
        if ((value != null) && (value instanceof List)) {
            referenceables = (List<Referenceable>) value;
        } else {
            referenceables = new ArrayList<>();
        }
        referenceables.addAll(listNeedToBeAdded);
        return referenceables;
    }

    private static String getClusterName(JSONObject projectJson) {
        String clusterName = projectJson.getString(CLUSTER_NAME);
        clusterName = clusterName == null ? "default" : clusterName;
        return clusterName;
    }
}
