package DataModel;

import com.google.common.collect.ImmutableList;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.model.CoreDataTypes;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 9:43
 */
public class TenantDataModelGenerator {
    public static final String TENANT_TRAIT = "TENANT_";
    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, HierarchicalTypeDefinition<TraitType>> traitTypeDefinitions;
    private static final AttributeDefinition NAME_ATTRIBUTE =
        TypesUtil.createUniqueRequiredAttrDef("name", DataTypes.STRING_TYPE);
    public TenantDataModelGenerator(){
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        traitTypeDefinitions = new HashMap<>();
    }

    public  void createDataModel(AtlasClient client) throws AtlasServiceException {
        createDrProjectType();
        createDrProjectStatus();
        createDrUser();
        createDrProjectRole();
        createEngine();
        createDrProject();
        createDrTenant();
        client.createType(getTypesDef());
    }

    private void createDrEngineType(){

    }
    private void createDrProjectType(){
        EnumValue[] values = {new EnumValue("DR_PROJECT_TYPE_DEVELOP", 1), new EnumValue("DR_PROJECT_TYPE_TEST", 2), new EnumValue("DR_PROJECT_TYPE_PRODUCE", 3),
            new EnumValue("DR_PROJECT_TYPE_PREPARE", 4)};
        String name = TenantDataModelType.DR_PROJECT_TYPE.getName();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
    }

    private void createDrProjectStatus(){
        EnumValue[] values = {new EnumValue("DR_PROJECT_STATUS_NORMAL", 1), new EnumValue("DR_PROJECT_STATUS_DELETED", 2), new EnumValue("DR_PROJECT_STATUS_INACTIVE", 3),
            new EnumValue("DR_PROJECT_STATUS_BOOTING", 4)};
        String name = TenantDataModelType.DR_PROJECT_STATUS.getName();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
    }

    private void createDrUser(){
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            NAME_ATTRIBUTE,
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("accId",
                DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("accKey", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("projects", DataTypes.arrayTypeName(TenantDataModelType.DR_PROJECT.getName()),
                Multiplicity.OPTIONAL, false, null),
        };

        String name = TenantDataModelType.DR_USER.getName();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name, null, attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(TENANT_TRAIT + name, null));
    }

    private void createDrProjectRole(){
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("users", DataTypes.arrayTypeName(TenantDataModelType.DR_USER.getName()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("acls", DataTypes.arrayTypeName(OdpsDataTypes.ODPS_OBJECT_PRIVILEGE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("policy",
                DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("proxyAccount", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
        };

        String name = TenantDataModelType.DR_PROJECT_ROLE.getName();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(TENANT_TRAIT + name, null));
    }

    private void createDrTenant(){
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            NAME_ATTRIBUTE,
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("admins", DataTypes.arrayTypeName(TenantDataModelType.DR_USER.getName()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("users", DataTypes.arrayTypeName(TenantDataModelType.DR_USER.getName()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("projects", DataTypes.arrayTypeName(TenantDataModelType.DR_PROJECT.getName()),
                Multiplicity.OPTIONAL, false, null),
        };

        String name = TenantDataModelType.DR_TENANT.getName();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(TENANT_TRAIT + name, null));
    }

    private void createDrProject(){
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            NAME_ATTRIBUTE,
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("engine", TenantDataModelType.DR_ENGINE.getName(),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("roles", DataTypes.arrayTypeName(TenantDataModelType.DR_PROJECT_ROLE.getName()),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("autoSchedule", DataTypes.BOOLEAN_TYPE.getName(),Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("codeEnabled", DataTypes.BOOLEAN_TYPE.getName(),Multiplicity.OPTIONAL, false, null),
        };

        String name = TenantDataModelType.DR_PROJECT.getName();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(TENANT_TRAIT + name, null));
    }

    private void createEngine(){
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            NAME_ATTRIBUTE,
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("dataContainer", RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("proxyAccount", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
        };

        String name = TenantDataModelType.DR_ENGINE.getName();
        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, name,
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(name, definition);
        traitTypeDefinitions.put(name, TypesUtil.createTraitTypeDef(TENANT_TRAIT + name, null));
    }

    public TypesDef getTypesDef() {
        return TypesUtil.getTypesDef(getEnumTypeDefinitions(), ImmutableList.<StructTypeDefinition>of(), getTraitTypeDefinitions(),
            getClassTypeDefinitions());
    }

    public String getDataModelAsJSON() {
        return TypesSerialization.toJson(getTypesDef());
    }

    public ImmutableList<EnumTypeDefinition> getEnumTypeDefinitions() {
        return ImmutableList.copyOf(enumTypeDefinitionMap.values());
    }

    public ImmutableList<HierarchicalTypeDefinition<ClassType>> getClassTypeDefinitions() {
        return ImmutableList.copyOf(classTypeDefinitions.values());
    }

    public ImmutableList<HierarchicalTypeDefinition<TraitType>> getTraitTypeDefinitions() {
        return ImmutableList.copyOf(traitTypeDefinitions.values());
    }
}
