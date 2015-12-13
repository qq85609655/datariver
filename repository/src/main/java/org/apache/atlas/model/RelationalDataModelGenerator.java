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

package org.apache.atlas.model;

import com.google.common.collect.ImmutableList;

import org.apache.atlas.AtlasException;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.AttributeDefinition;
import org.apache.atlas.typesystem.types.ClassType;
import org.apache.atlas.typesystem.types.DataTypes;
import org.apache.atlas.typesystem.types.EnumTypeDefinition;
import org.apache.atlas.typesystem.types.EnumValue;
import org.apache.atlas.typesystem.types.HierarchicalTypeDefinition;
import org.apache.atlas.typesystem.types.Multiplicity;
import org.apache.atlas.typesystem.types.StructTypeDefinition;
import org.apache.atlas.typesystem.types.utils.TypesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 关系型元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class RelationalDataModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(RelationalDataModelGenerator.class);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;


    public RelationalDataModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the relational Data Model....");

        // Relational Types
        defineDataContainerTypeEnum();
        defineDataContainerStatusEnum();
        defineObjectTypeEnum();
        defineObjectPrivilegeEnum();

        defineDBAccessInfoSuperType();
        defineDataFieldSuperType();
        defineDataContainerSuperType();
        defineDataTableSuperType();
        defineDataRowSetSuperType();
    }

    public TypesDef getTypesDef() {
        return TypesUtil.getTypesDef(getEnumTypeDefinitions(), getStructTypeDefinitions(), getTraitTypeDefinitions(),
            getClassTypeDefinitions());
    }

    public String getModelAsJson() throws AtlasException {
        createDataModel();
        return TypesSerialization.toJson(getTypesDef());
    }

    public ImmutableList<EnumTypeDefinition> getEnumTypeDefinitions() {
        return ImmutableList.copyOf(enumTypeDefinitionMap.values());
    }

    public ImmutableList<StructTypeDefinition> getStructTypeDefinitions() {
        return ImmutableList.copyOf(structTypeDefinitionMap.values());
    }

    public ImmutableList<HierarchicalTypeDefinition<TraitType>> getTraitTypeDefinitions() {
        return ImmutableList.of();
    }

    public ImmutableList<HierarchicalTypeDefinition<ClassType>> getClassTypeDefinitions() {
        return ImmutableList.copyOf(classTypeDefinitions.values());
    }

    private static final AttributeDefinition NAME_ATTRIBUTE =
        TypesUtil.createRequiredAttrDef("name", DataTypes.STRING_TYPE);
    private static final AttributeDefinition DESCRIPTION_ATTRIBUTE =
        TypesUtil.createOptionalAttrDef("description", DataTypes.STRING_TYPE);

    /* ------------------------Relational meta model definition----------------------*/
    private void defineDataContainerTypeEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("ODPS", 1),
            new EnumValue("RDS", 2),
            new EnumValue("HDFS", 3),
            new EnumValue("HIVE", 4),
            new EnumValue("ORACLE", 5),
            new EnumValue("SQLSERVER", 6),
            new EnumValue("MYSQL", 7),
            new EnumValue("DB2", 8),
            new EnumValue("ADS", 9),
            new EnumValue("SYBASE", 10),
            new EnumValue("TERADATA", 11),
            new EnumValue("POSTGRESQL", 12),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(RelationalDataTypes.DATA_CONTAINER_TYPE.getValue(), values);

        LOG.debug("Created definition for " + RelationalDataTypes.DATA_CONTAINER_TYPE.getValue());
        enumTypeDefinitionMap.put(RelationalDataTypes.DATA_CONTAINER_TYPE.getValue(), definition);
    }

    private void defineObjectTypeEnum() {
        EnumValue[] values = {
                new EnumValue("PROJECT", 1),
                new EnumValue("TABLE", 2),
                new EnumValue("FUNCTION", 3),
                new EnumValue("COLUMN", 4),
                new EnumValue("RESOURCE", 5),
                new EnumValue("INSTANCE", 6),
                new EnumValue("JOB", 7),
                new EnumValue("Volume", 8)
        };
        String name = RelationalDataTypes.DATA_OBJECT_TYPE.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    private void  defineObjectPrivilegeEnum() {
        EnumValue[] values = {
                new EnumValue("READ", 1),
                new EnumValue("WRITE", 2),
                new EnumValue("LIST", 3),
                new EnumValue("CREATETABLE", 4),
                new EnumValue("CREATEFUNCTION", 5),
                new EnumValue("CREATERESOURCE", 6),
                new EnumValue("CREATEJOB", 7),
                new EnumValue("DESCRIBE", 8),
                new EnumValue("SELECT", 9),
                new EnumValue("ALTER", 10),
                new EnumValue("UPDATE", 11),
                new EnumValue("DROP", 12),
                new EnumValue("DELETE", 13),
                new EnumValue("EXECUTE", 14),
                new EnumValue("ALL", 15),
                new EnumValue("CREATEINSTANCE", 16)};
        String name = RelationalDataTypes.DATA_OBJECT_PRIVILEGES.getValue();
        EnumTypeDefinition definition = new EnumTypeDefinition(name, values);
        enumTypeDefinitionMap.put(name, definition);
        LOG.debug("Created definition for " + name);
    }

    private void defineDataContainerStatusEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("AVAILABLE", 1),
            new EnumValue("READONLY", 2),
            new EnumValue("DELETING", 3),
            new EnumValue("FROZEN", 4),
            new EnumValue("UNKNOWN", 5),};

        EnumTypeDefinition definition =
            new EnumTypeDefinition(RelationalDataTypes.DATA_CONTAINER_STATUS.getValue(), values);

        LOG.debug("Created definition for " + RelationalDataTypes.DATA_CONTAINER_STATUS.getValue());
        enumTypeDefinitionMap.put(RelationalDataTypes.DATA_CONTAINER_STATUS.getValue(), definition);
    }

    private void defineDBAccessInfoSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE);
        LOG.debug("Created definition for " + RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue());
        classTypeDefinitions.put(RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue(), definition);
    }

    private void defineDataFieldSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE,
                new AttributeDefinition("dataType", DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );
        LOG.debug("Created definition for " + RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue());
        classTypeDefinitions.put(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(), definition);
    }

    private void defineDataContainerSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE,
                new AttributeDefinition("dbType",
                    RelationalDataTypes.DATA_CONTAINER_TYPE.getValue(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("id",
                    DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("tag",
                    DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("status",
                    RelationalDataTypes.DATA_CONTAINER_STATUS.getValue(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("accessInfo",
                    RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue(),
                    Multiplicity.REQUIRED, true, null),
                new AttributeDefinition("tables",
                    DataTypes.arrayTypeName(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                            Multiplicity.OPTIONAL, false, null)
            );
        LOG.debug("Created definition for " + RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE);
        classTypeDefinitions.put(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(), definition);
    }

    private void defineDataTableSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE,
                new AttributeDefinition("database",
                    RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("fields",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()),
                    Multiplicity.REQUIRED, true, null),
                new AttributeDefinition("isPartitioned",
                    DataTypes.BOOLEAN_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("partitionKeys",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("partitions",
                    DataTypes.arrayTypeName(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("size",
                    DataTypes.LONG_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );
        LOG.debug("Created definition for " + RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue());
        classTypeDefinitions.put(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(), definition);
    }

    private void defineDataRowSetSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
                .createClassTypeDef(
                        RelationalDataTypes.DATA_ROWSET_SUPER_TYPE.getValue(),
                        ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                        NAME_ATTRIBUTE,
                        DESCRIPTION_ATTRIBUTE,
                        new AttributeDefinition("table",
                                RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(),
                                Multiplicity.REQUIRED, false, null),
                        new AttributeDefinition("partitionKeys",
                                DataTypes.arrayTypeName(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()),
                                Multiplicity.REQUIRED, true, null),
                        new AttributeDefinition("numRecord",
                                DataTypes.LONG_TYPE.getName(),
                                Multiplicity.OPTIONAL, false, null)
                );
        LOG.debug("Created definition for " + RelationalDataTypes.DATA_ROWSET_SUPER_TYPE.getValue());
        classTypeDefinitions.put(RelationalDataTypes.DATA_ROWSET_SUPER_TYPE.getValue(), definition);
    }
}
