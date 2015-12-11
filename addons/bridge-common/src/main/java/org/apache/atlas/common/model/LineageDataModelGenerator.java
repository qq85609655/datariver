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

package org.apache.atlas.common.model;

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
 * 血缘元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class LineageDataModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(LineageDataModelGenerator.class);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;


    public LineageDataModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the lineage Data Model....");

        // lineage type
        defineLineageDependTypeEnum();

        defineLineageDependencyStruct();

        defineLineageFieldMapClass();
        defineLineageTaskFieldMapClass();
        defineLineageStepFieldMapClass();

        defineLineageProcessClass();
        defineLineageTaskProcessClass();
        defineLineageStepProcessClass();
        defineLineageWorkflowProcessClass();
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
        TypesUtil.createUniqueRequiredAttrDef("name", DataTypes.STRING_TYPE);
    private static final AttributeDefinition DESCRIPTION_ATTRIBUTE =
        TypesUtil.createOptionalAttrDef("description", DataTypes.STRING_TYPE);

    /* ------------------------Lineage meta model definition----------------------*/
    private void defineLineageDependTypeEnum() throws AtlasException {
        EnumValue[] values = {new EnumValue("SIMPLE", 1), new EnumValue("EXPRESSION", 2),
            new EnumValue("SCRIPT", 3),};

        EnumTypeDefinition definition =
            new EnumTypeDefinition(LineageDataTypes.LINEAGE_DEPEND_TYPE.getValue(), values);

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_DEPEND_TYPE.getValue());
        enumTypeDefinitionMap.put(LineageDataTypes.LINEAGE_DEPEND_TYPE.getValue(), definition);
    }

    private void defineLineageDependencyStruct() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("function", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            DESCRIPTION_ATTRIBUTE,
            new AttributeDefinition("dependencyType", LineageDataTypes.LINEAGE_DEPEND_TYPE.getValue(),
                Multiplicity.REQUIRED, false, null),};
        StructTypeDefinition definition =
            new StructTypeDefinition(
                LineageDataTypes.LINEAGE_DEPENDENCY.getValue(),
                attributeDefinitions);

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_DEPENDENCY.getValue());
        structTypeDefinitionMap.put(LineageDataTypes.LINEAGE_DEPENDENCY.getValue(), definition);
    }

    private void defineLineageFieldMapClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("dependencyInfo",
                LineageDataTypes.LINEAGE_DEPENDENCY.getValue(),
                Multiplicity.REQUIRED, true, null)
        };

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                LineageDataTypes.LINEAGE_FIELD_MAP.getValue(),
                null,
                attributeDefinitions
            );

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_FIELD_MAP.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_FIELD_MAP.getValue(), definition);
    }

    private void defineLineageTaskFieldMapClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("sourceFields",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("targetField",
                RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(),
                Multiplicity.REQUIRED, true, null)
        };

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue(),
                ImmutableList.of(LineageDataTypes.LINEAGE_FIELD_MAP.getValue()),
                attributeDefinitions
            );

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue(), definition);
    }

    private void defineLineageStepFieldMapClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("sourceFields",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("targetField",
                RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(),
                Multiplicity.REQUIRED, true, null)
        };

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue(),
                ImmutableList.of(LineageDataTypes.LINEAGE_FIELD_MAP.getValue()),
                attributeDefinitions
            );

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue(), definition);
    }

    private void defineLineageProcessClass() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                LineageDataTypes.LINEAGE_PROCESS_INFO.getValue(),
                null,
                new AttributeDefinition("name", DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_PROCESS_INFO.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_PROCESS_INFO.getValue(), definition);
    }

    private void defineLineageTaskProcessClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("inputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("inputDbs",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputDbs",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("fieldMaps",
                DataTypes.arrayTypeName(LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue()),
                Multiplicity.OPTIONAL, true, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue(),
                ImmutableList.of(LineageDataTypes.LINEAGE_PROCESS_INFO.getValue()),
                attributeDefinitions);

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue(), definition);
    }

    private void defineLineageWorkflowProcessClass() throws AtlasException {
        String typeName = LineageDataTypes.LINEAGE_WORKFLOW_PROCESS_INFO.getValue();
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("inputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("inputDbs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputDbs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                typeName,
                ImmutableList.of(LineageDataTypes.LINEAGE_PROCESS_INFO.getValue()),
                attributeDefinitions);

        LOG.debug("Created definition for " + typeName);
        classTypeDefinitions.put(typeName, definition);
    }

    private void defineLineageStepProcessClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("inputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputs",
                DataTypes.arrayTypeName(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("inputDbs",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("outputDbs",
                    DataTypes.arrayTypeName(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("fieldMaps",
                DataTypes.arrayTypeName(LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue()),
                Multiplicity.OPTIONAL, true, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(
                ClassType.class,
                LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue(),
                ImmutableList.of(LineageDataTypes.LINEAGE_PROCESS_INFO.getValue()),
                attributeDefinitions);

        LOG.debug("Created definition for " + LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue());
        classTypeDefinitions.put(LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue(), definition);
    }
}
