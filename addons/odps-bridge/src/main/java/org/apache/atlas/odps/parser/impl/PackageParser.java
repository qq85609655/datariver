package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.odps.client.AtlasClientFactory;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.JsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.odps.parser.ParserFactory;
import org.apache.atlas.typesystem.Referenceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * package相关信息解析
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class PackageParser extends BaseJsonParser {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final Logger logger = LoggerFactory.getLogger(PackageParser.class);

    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject packageMeta = (JSONObject) data[0];
        JSONObject tablesJson = packageMeta.getJSONObject("tables");
        Map<String, JSONObject> tableNameMetaMap = new HashMap<>(tablesJson.size());
        for (Map.Entry<String, Object> tableEntry : tablesJson.entrySet()) {
            tableNameMetaMap.put(tableEntry.getKey(), ((JSONObject) tableEntry.getValue()).getJSONObject("tableMeta"));
        }
        JSONObject projectsJson = packageMeta.getJSONObject("projects");
        Map<String, JSONObject> projectsNameMetaMap = new HashMap<>(projectsJson.size());
        for (Map.Entry<String, Object> proEntry : projectsJson.entrySet()) {
            projectsNameMetaMap.put(proEntry.getKey(), (JSONObject) proEntry.getValue());
        }
        List<Referenceable> packages = new ArrayList<>();
        parsePackages(context, packageMeta, packageMeta.getJSONArray("CreatedPackages"),
            packages, tableNameMetaMap, projectsNameMetaMap);
        parsePackages(context, packageMeta, packageMeta.getJSONArray("InstalledPackages"),
            packages, tableNameMetaMap, projectsNameMetaMap);
        context.put(type(), packages);
    }

    /**
     * 解析package数组.
     *
     * @param context
     * @param packageMeta
     * @param createdPackages
     * @param packages
     * @throws AtlasException
     */
    private void parsePackages(ParserContext context, JSONObject packageMeta, JSONArray createdPackages,
                               List<Referenceable> packages, Map<String, JSONObject> tableNameMetaMap,
                               Map<String, JSONObject> projectNameMetaMap) throws AtlasException {
        if (createdPackages == null) {
            return;
        }
        for (int i = 0; i < createdPackages.size(); i++) {
            JSONObject apackageJson = createdPackages.getJSONObject(i);
            String packageName = apackageJson.getString("PackageName");
            String sourceProject = apackageJson.getString("SourceProject");
            JSONObject sourceProjectMeta = projectNameMetaMap.get(sourceProject);
            Referenceable project = BaseJsonParser.parseProject(context,
                getProjectQualifiedName(sourceProjectMeta), sourceProjectMeta);
            String packageQuarlifiedName =
                formatQualifiedName(String.valueOf(project.get(QUALIFIED_NAME)), packageName);
            Referenceable existPackage = null;
            try {
                existPackage = AtlasClientFactory.getAtlasClient().getEntity(OdpsDataTypes.ODPS_PACKAGE.getValue(),
                    BaseJsonParser.QUALIFIED_NAME, packageQuarlifiedName);
            } catch (Exception e) {
                logger.error("Catch an atlasServerException", e);
            }
            if (existPackage != null){
                continue;
            }
            Referenceable apackage = newInstance(OdpsDataTypes.ODPS_PACKAGE);
            apackage.set("name", packageName);
            apackage.set("ownerProject", project);
            apackage.set("metaSource", "ODPS");

            apackage.set(QUALIFIED_NAME, packageQuarlifiedName);
            parseTime(apackageJson, apackage);
            BaseJsonParser.updateEntityMap(ENTITY_MAP_TYPE.PROJECT_PACKAGES, (String)project.get(BaseJsonParser.QUALIFIED_NAME),
                (String)apackage.get(BaseJsonParser.QUALIFIED_NAME));
            parseAssignedProject(context, apackageJson, apackage, projectNameMetaMap);
            List<Referenceable> packageItems =
                buildPackageItems(context, apackageJson, sourceProjectMeta, tableNameMetaMap, packageQuarlifiedName);
            if (!packageItems.isEmpty()) {
                apackage.set("resources", packageItems);
            }
            packages.add(apackage);
        }
    }

    /**
     * @param context
     * @param apackageJson
     * @param sourceProjectMeta
     * @return
     * @throws AtlasException
     */
    private List<Referenceable> buildPackageItems(ParserContext context, JSONObject apackageJson,
                                                  JSONObject sourceProjectMeta,
                                                  Map<String, JSONObject> tableNameMetaMap,
                                                  String packageQuarlifiedName) throws AtlasException {
        List<Referenceable> packageItems = new ArrayList<>();
        JSONArray resourceList = apackageJson.getJSONArray("ResourceList");
        if (resourceList != null) {
            for (JSONObject resource : resourceList.toArray(new JSONObject[0])) {
                Referenceable packgeItem = newInstance(OdpsDataTypes.ODPS_PACKAGE_RESOURCE_ITEM);
                packgeItem.set("type", resource.getString("type").toUpperCase());
                setStringWithSameKey(packgeItem, resource, "name");
                packgeItem.set(QUALIFIED_NAME, formatQualifiedName(packageQuarlifiedName, resource.getString("name")));
                packgeItem.set("privileges",
                    JSONArray.parse(resource.getJSONArray("privileges").toJSONString().toUpperCase()));
                String meta = resource.getString("meta");
                JSONObject tableMeta = tableNameMetaMap.get(meta);
                JsonParser tableParser = ParserFactory.getSimpleTableParser();
                tableParser.parse(context, tableMeta, sourceProjectMeta);
                packgeItem.set("resource", context.getValue(OdpsDataTypes.ODPS_TABLE));
                packgeItem.set("metaSource", "ODPS");
                packageItems.add(packgeItem);
            }
        }
        return packageItems;
    }

    /**
     * @param context
     * @param apackageJson
     * @param apackage
     * @throws AtlasException
     */
    private void parseAssignedProject(ParserContext context, JSONObject apackageJson, Referenceable apackage,
                                      Map<String, JSONObject> projectNameMetaMap) throws AtlasException {
        JSONArray allowedProjectList = apackageJson.getJSONArray("AllowedProjectList");
        if (allowedProjectList != null) {
            List<Referenceable> projects = new ArrayList<>();
            for (String projectName : allowedProjectList.toArray(new String[0])) {
                JSONObject projectMeta = projectNameMetaMap.get(projectName);
                Referenceable oneProject = BaseJsonParser.parseProject(context,
                    getProjectQualifiedName(projectMeta), projectMeta);
                BaseJsonParser.updateEntityMap(ENTITY_MAP_TYPE.PROJECT_PACKAGES, (String) oneProject.get(BaseJsonParser.QUALIFIED_NAME),
                    (String) apackage.get(BaseJsonParser.QUALIFIED_NAME));
                projects.add(oneProject);
            }
            apackage.set("assignedProject", projects);
        }
    }

    /**
     * @param apackageJson
     * @param apackage
     */
    private void parseTime(JSONObject apackageJson, Referenceable apackage) {
        try {
            apackage.set("createTime", dateFormat.parse(apackageJson.getString("CreateTime")).getTime());
        } catch (Exception e) {
            logger.error("package创建时间解析异常", e);
        }
        try {
            String installTime = apackageJson.getString("InstallTime");
            if (installTime != null) {
                apackage.set("installTime", dateFormat.parse(installTime).getTime());
            }
        } catch (Exception e) {
            logger.error("package安装时间解析异常", e);
        }
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_PACKAGE;
    }
}
