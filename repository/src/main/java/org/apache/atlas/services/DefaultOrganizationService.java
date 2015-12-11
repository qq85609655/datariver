package org.apache.atlas.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.discovery.DiscoveryException;
import org.apache.atlas.discovery.DiscoveryService;
import org.apache.atlas.typesystem.types.DataTypes;
import org.apache.atlas.typesystem.types.TypeSystem;
import org.apache.commons.collections.map.HashedMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 描述
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-18 11:46
 */
@Singleton
public class DefaultOrganizationService implements OrganizationService {
    private Logger LOG = LoggerFactory.getLogger(DefaultOrganizationService.class);
    private final MetadataService metadataService;
    private final DiscoveryService discoveryService;
    private final TypeSystem typeSystem;
    private static final Map<String, Set<String>> typeWithSuperTypes = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> typeWithChildTypes = new ConcurrentHashMap<>();
    private static final Set<String> tableChildTypes = new ConcurrentSkipListSet<>();
    private static final Set<String> containerChildTypes = new ConcurrentSkipListSet<>();
    @Inject
    public DefaultOrganizationService(MetadataService metadataService, DiscoveryService discoveryService) {
        this.metadataService = metadataService;
        this.discoveryService = discoveryService;
        typeSystem = TypeSystem.getInstance();
    }

    private synchronized void initSuperAndChildTypes() throws AtlasException, JSONException {
        if (!typeWithSuperTypes.isEmpty()) {
            return;
        }
        List<String> classTypeNames = typeSystem.getTypeNamesByCategory(DataTypes.TypeCategory.CLASS);
        for (String typeName : classTypeNames) {
            String typeDefinition = metadataService.getTypeDefinition(typeName);
            JSONArray superTypes = new JSONObject(typeDefinition).getJSONArray("classTypes").getJSONObject(0).getJSONArray("superTypes");
            Set<String> superTypesSet = new ConcurrentSkipListSet<>();
            for (int i = 0; i < superTypes.length(); i++) {
                superTypesSet.add(superTypes.getString(i));
            }
            typeWithSuperTypes.put(typeName, superTypesSet);
        }
        Set<String> typesSet = new HashSet<>(typeWithSuperTypes.keySet());
        for (String type : typesSet) {
            Set<String> childTypes = new ConcurrentSkipListSet<>();
            for (String typeName : typeWithSuperTypes.keySet()) {
                Set<String> strings = typeWithSuperTypes.get(typeName);
                if (strings.contains(type)) {//父类有当前类
                    childTypes.add(typeName);
                }
            }
            typeWithChildTypes.put(type, childTypes);
        }
    }

    private Set<String> getSuperTypesOfType(String typeName) throws AtlasException{
        return typeWithSuperTypes.get(typeName);
    }
    private boolean isSupperType(String currentType, String supperType) {
        try {
            Set<String> superTypesOfType = getSuperTypesOfType(currentType);
            return superTypesOfType == null ? false : superTypesOfType.contains(supperType);
        } catch (AtlasException e) {
            LOG.error("isSupperType异常", e);
        }
        return false;
    }
    private synchronized void initTableAndContainnerChildTypes() {
        if (!tableChildTypes.isEmpty()) {
            return;
        }
        init();
        List<String> classTypeNames = typeSystem.getTypeNamesByCategory(DataTypes.TypeCategory.CLASS);
        for (String type : classTypeNames) {
            if (isSupperType(type, "DataTable")) {
                tableChildTypes.add(type);
            } else if (isSupperType(type, "DataContainer")) {
                containerChildTypes.add(type);
            }
        }
    }

    private void init() {
        if (typeWithSuperTypes.isEmpty()) {
            try {
                initSuperAndChildTypes();
            } catch (AtlasException |JSONException e) {
                LOG.error("初始化superTypes异常", e);
            }
        }
    }

    @Override
    public JSONArray statistics() throws AtlasException{
        JSONArray results = null;
        try {
            if (tableChildTypes.isEmpty()) {
                initTableAndContainnerChildTypes();
            }
            //1.获取所有含有organizations标签的table和container
            String organizations = AtlasClient.ORGANIZATIONS;
            String organizationsElements = discoveryService.searchByDSL(organizations);
            JSONArray rows = new JSONObject(organizationsElements).getJSONArray(AtlasClient.ROWS);
            Set<String> allTypeNames = new HashSet<>();
            for (int i = 0; i < rows.length(); i++) {
                JSONObject instance = rows.getJSONObject(i);
                JSONObject instanceInfo = instance.getJSONObject("instanceInfo");
                String typeName = instanceInfo.getString("typeName");
                allTypeNames.add(typeName);
            }
            //key:organizations trait的name属性；value: 含有这个trait的table
            Map<String, List<JSONObject>> traitTables = new HashedMap();
            Map<String, List<JSONObject>> traitContainers = new HashedMap();
            for (String type : allTypeNames) {
                if (tableChildTypes.contains(type) || "DataTable".equalsIgnoreCase(type)) {
                    String dslQuery = type + " is " + organizations;
                    fillMap(traitTables, organizations, dslQuery);
                } else if (containerChildTypes.contains(type) || "DataContainer".equalsIgnoreCase(type)) {
                    String dslQuery = type + " is " + organizations;
                    fillMap(traitContainers, organizations, dslQuery);
                }
            }
            //2.解析数据，组装返回值
            results = buildResults(traitTables, traitContainers);
        } catch (Exception e) {
            LOG.error("获取组织含有的表/数据库统计信息出错", e);
            throw new AtlasException(e);
        }
        return results;
    }
    private JSONArray buildResults(Map<String, List<JSONObject>> traitTables, Map<String, List<JSONObject>> traitContainers) throws JSONException {
        JSONArray results = new JSONArray();
        //用于traitContainers区分是否已经存在
        Set<String> traitNames = new HashSet<>();
        for (String traitName : traitTables.keySet()) {
            JSONObject result = new JSONObject();
            result.put("name", traitName);
            traitNames.add(traitName);
            List<JSONObject> tables = traitTables.get(traitName);
            result.put("tableCount", tables == null ? 0 : tables.size());
            List<JSONObject> containers = traitContainers.get(traitName);
            if (containers == null || containers.isEmpty()) {
                result.put("databases", new JSONArray());
            } else {
                result.put("databases", convertContainers(containers));
            }
            results.put(result);
        }
        for (String traitName : traitContainers.keySet()) {
            if (traitNames.contains(traitName)) {
                //已经存在的traitName，说明在上面的循环里面已经处理过了
                continue;
            }
            JSONObject result = new JSONObject();
            result.put("name", traitName);
            result.put("tableCount", 0);
            result.put("databases", convertContainers(traitContainers.get(traitName)));
            results.put(result);
        }
        return results;
    }

    private JSONArray convertContainers(List<JSONObject> containers) throws JSONException {
        JSONArray dbs = new JSONArray();
        for (JSONObject db : containers) {
            JSONObject aDbResult = new JSONObject();
            aDbResult.put("name", db.getString("name"));
            aDbResult.put("GUID", db.getJSONObject("$id$").getString("id"));
            dbs.put(aDbResult);
        }
        return dbs;
    }

    private void fillMap(Map<String, List<JSONObject>> traitTables, String organizations, String dslQuery) throws DiscoveryException, JSONException {
        String searchResult = discoveryService.searchByDSL(dslQuery);
        JSONArray rows = new JSONObject(searchResult).getJSONArray(AtlasClient.ROWS);
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            JSONObject orgTrait = row.getJSONObject("$traits$").getJSONObject(organizations);
            String name = orgTrait.getString("name");
            List<JSONObject> values = putRowIntoValues(traitTables, row, name);
            traitTables.put(name, values);
        }
    }

    private List<JSONObject> putRowIntoValues(Map<String, List<JSONObject>> traitTables, JSONObject row, String name) {
        List<JSONObject> values = traitTables.get(name);
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(row);
        return values;
    }

    public Map<String, Set<String>> getTypeWithSuperTypes() {
        init();
        return typeWithSuperTypes;
    }

    public Map<String, Set<String>> getTypeWithChildTypes() {
        init();
        return typeWithChildTypes;
    }
}
