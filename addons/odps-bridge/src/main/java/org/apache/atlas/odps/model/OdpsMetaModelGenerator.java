package org.apache.atlas.odps.model;

import com.google.common.collect.ImmutableList;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.common.model.CoreDataTypes;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.common.model.TransformDataTypes;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * odps元模型生成器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class OdpsMetaModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(OdpsMetaModelGenerator.class);

    private static final DataTypes.MapType STRING_MAP_TYPE =
        new DataTypes.MapType(DataTypes.STRING_TYPE, DataTypes.STRING_TYPE);
    public static final String ODPS_TRAIT = "ODPS_";

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;
    private final Map<String, HierarchicalTypeDefinition<TraitType>> traitTypeDefinitions;

    public OdpsMetaModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
        traitTypeDefinitions = new HashMap<>();
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
                new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(), Multiplicity.REQUIRED, false,
                        null),
                new AttributeDefinition("address", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                        null),
                new AttributeDefinition("email", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                        null),
                new AttributeDefinition("phone", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                        null),
                new AttributeDefinition("master", DataTypes.STRING_TYPE.getName(),
                        Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("code", DataTypes.STRING_TYPE.getName(),
                        Multiplicity.OPTIONAL, false, null),
//                new AttributeDefinition("parent", AtlasClient.ORGANIZATIONS,
//                        Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                        Multiplicity.OPTIONAL, false, null)};
        HierarchicalTypeDefinition<TraitType> organizations = TypesUtil.createTraitTypeDef("Organizations", null, attributeDefinitions);
        traitTypeDefinitions.put("Organizations",organizations);
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the Odps Data Model....");
        //emnms
        createObjectTypeEnum();
        createResourceTypeEnum();
        createObjectPrivilegeEnum();
        createPackageResourceTypeEnum();
        //classes
        createProjectClass();
        createColumnClass();
        createPartitionClass();
        createTableClass();
        createResourceClass();
        createTaskClass();
        createInstanceClass();
        createAccessInfoClass();
        createPackageClass();
        createPackageResourceItemClass();
    }

    private void createPackageClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("ownerProject", OdpsDataTypes.ODPS_PROJECT.getValue(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("installTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("assignedProjects", DataTypes.arrayTypeName(OdpsDataTypes.ODPS_PROJECT.getValue()),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("resources",
                DataTypes.arrayTypeName(OdpsDataTypes.ODPS_PACKAGE_RESOURCE_ITEM.getValue()),
                Multiplicity.OPTIONAL, true, null),
        };
        String name = OdpsDataTypes.ODPS_PACKAGE.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createPackageResourceItemClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("type", OdpsDataTypes.ODPS_PACKAGE_RESOURCE_TYPE.getValue(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("privileges",
                DataTypes.arrayTypeName(OdpsDataTypes.ODPS_OBJECT_PRIVILEGE.getValue()),
                Multiplicity.REQUIRED, true, null),
            new AttributeDefinition("resource", CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue(),
                Multiplicity.OPTIONAL, false, null),
        };

        String name = OdpsDataTypes.ODPS_PACKAGE_RESOURCE_ITEM.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createAccessInfoClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("tunnelURL", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("endpointURL", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
        };

        String name = OdpsDataTypes.ODPS_ACCINFO.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createInstanceClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("owner", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("startTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("endTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("project", OdpsDataTypes.ODPS_PROJECT.getValue(), Multiplicity.OPTIONAL, false,
                null),};

        String name = OdpsDataTypes.ODPS_INSTANCE.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createTaskClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("queryPlan", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false, null)};

        String name = OdpsDataTypes.ODPS_TASK.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createResourceClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(), Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("project", OdpsDataTypes.ODPS_PROJECT.getValue(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("owner", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("lastModifiedTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("resourceType", OdpsDataTypes.ODPS_RESOURCE_TYPE.getValue(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null)};

        String name = OdpsDataTypes.ODPS_RESOURCE.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createTableClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("owner", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("lifeCycle", DataTypes.INT_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("origination", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("tableType", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("storageVolume", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("incementVolume", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("netIncrementVolume", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("lastModifiedTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null)
        };

        String name = OdpsDataTypes.ODPS_TABLE.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createPartitionClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("lastModifiedTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("numStorage", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null)};

        String name = OdpsDataTypes.ODPS_PARTITION.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(RelationalDataTypes.DATA_ROWSET_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createColumnClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("isPartitionKey", DataTypes.BOOLEAN_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("label", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null)};

        String name = OdpsDataTypes.ODPS_COLUMN.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createProjectClass() {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("owner", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("lastModifiedTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("projectGroupname", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("clusterQuota", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("clusterName", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("resources",
                DataTypes.arrayTypeName(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("packages",
                DataTypes.arrayTypeName(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null)
        };

        String name = OdpsDataTypes.ODPS_PROJECT.getValue();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(ODPS_TRAIT + name, null));
        LOG.debug("Created definition for " + name);
    }

    private void createResourceTypeEnum() {
        EnumValue[] values = {new EnumValue("FILE", 1), new EnumValue("JAR", 2),
            new EnumValue("PY", 3), new EnumValue("ARCHIVE", 4),
            new EnumValue("TABLE", 5), new EnumValue("VOLUMEFILE", 6),
            new EnumValue("UNKNOWN", 7)};
        String name = OdpsDataTypes.ODPS_RESOURCE_TYPE.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    private void createObjectTypeEnum() {
        EnumValue[] values = {new EnumValue("GLOBAL", 1), new EnumValue("PROJECT", 2), new EnumValue("TABLE", 3),
            new EnumValue("PARTITION", 4), new EnumValue("COLUMN", 5)};
        String name = OdpsDataTypes.ODPS_OBJECT_TYPE.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    private void createObjectPrivilegeEnum() {
        EnumValue[] values = {new EnumValue("READ", 1), new EnumValue("WRITE", 2), new EnumValue("LIST", 3),
            new EnumValue("CREATETABLE", 4), new EnumValue("CREATEFUNCTION", 5), new EnumValue("CREATERESOURCE", 6),
            new EnumValue("CREATEJOB", 7), new EnumValue("DESCRIBE", 8), new EnumValue("SELECT", 9),
            new EnumValue("ALTER", 10), new EnumValue("UPDATE", 11), new EnumValue("DROP", 12),
            new EnumValue("DELETE", 13), new EnumValue("EXECUTE", 14), new EnumValue("ALL", 15)};
        String name = OdpsDataTypes.ODPS_OBJECT_PRIVILEGE.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    private void createPackageResourceTypeEnum() {
        EnumValue[] values = {new EnumValue("FUNC", 1), new EnumValue("TABLE", 2), new EnumValue("RESOURCE", 3),
            new EnumValue("INSTANCE", 4)};
        String name = OdpsDataTypes.ODPS_PACKAGE_RESOURCE_TYPE.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    public TypesDef getTypesDef() {
        return TypesUtil.getTypesDef(getEnumTypeDefinitions(), getStructTypeDefinitions(), getTraitTypeDefinitions(),
            getClassTypeDefinitions());
    }

    public String getDataModelAsJSON() {
        return TypesSerialization.toJson(getTypesDef());
    }

    public ImmutableList<EnumTypeDefinition> getEnumTypeDefinitions() {
        return ImmutableList.copyOf(enumTypeDefinitionMap.values());
    }

    public ImmutableList<StructTypeDefinition> getStructTypeDefinitions() {
        return ImmutableList.copyOf(structTypeDefinitionMap.values());
    }

    public ImmutableList<HierarchicalTypeDefinition<ClassType>> getClassTypeDefinitions() {
        return ImmutableList.copyOf(classTypeDefinitions.values());
    }

    public ImmutableList<HierarchicalTypeDefinition<TraitType>> getTraitTypeDefinitions() {
        return ImmutableList.copyOf(traitTypeDefinitions.values());
    }

    public Map<String, EnumTypeDefinition> getEnumTypeDefinitionMap() {
        return enumTypeDefinitionMap;
    }

    public static void main(String[] args) throws AtlasException {
        OdpsMetaModelGenerator dataModel = new OdpsMetaModelGenerator();
        dataModel.createDataModel();
        System.out.println(dataModel.getDataModelAsJSON());
    }
}
