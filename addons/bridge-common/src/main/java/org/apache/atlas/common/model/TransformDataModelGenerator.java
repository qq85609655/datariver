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
 * ETL元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class TransformDataModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(TransformDataModelGenerator.class);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;


    public TransformDataModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the transform Data Model....");

        // transform Types
        defineTaskTypeEnum();
        defineTaskStatusEnum();
        defineStepTypeEnum();
        defineInstanceStatusEnum();

        defineAbstractProcessSuperType();
        defineStepSuperType();
        defineStepSequenceSuperType();
        defineTaskSuperType();
        defineInstanceSuperType();
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


    /* ------------------------Transform meta model definition----------------------*/
    private void defineTaskTypeEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("GRAPH", 1),
            new EnumValue("LOT", 2),
            new EnumValue("SQLPLAN", 3),
            new EnumValue("SQL", 4),
            new EnumValue("XLIB", 5),
            new EnumValue("SQLCOST", 6),
            new EnumValue("STREAM", 7),
            new EnumValue("MOYE", 8),
            new EnumValue("GALAXY", 9),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(TransformDataTypes.ETL_TASK_TYPE.getValue(), values);

        LOG.debug("Created definition for " + TransformDataTypes.ETL_TASK_TYPE.getValue());
        enumTypeDefinitionMap.put(TransformDataTypes.ETL_TASK_TYPE.getValue(), definition);
    }

    private void defineTaskStatusEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("WAITING", 1),
            new EnumValue("RUNNING", 2),
            new EnumValue("SUCCESS", 3),
            new EnumValue("FAILED", 4),
            new EnumValue("SUSPENDED", 5),
            new EnumValue("CANCELLED", 6),
            new EnumValue("SCHEDULE", 7),
            new EnumValue("TERMINATED", 8),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(TransformDataTypes.ETL_TASK_STATUS.getValue(), values);

        LOG.debug("Created definition for " + TransformDataTypes.ETL_TASK_STATUS.getValue());
        enumTypeDefinitionMap.put(TransformDataTypes.ETL_TASK_STATUS.getValue(), definition);
    }

    private void defineStepTypeEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("TABLE_INPUT", 1),
            new EnumValue("TABLE_OUTPUT", 2),
            new EnumValue("ODPS_INPUT", 3),
            new EnumValue("ODPS_OUTPUT", 4),
            new EnumValue("HDFS_INPUT", 5),
            new EnumValue("HDFS_OUTPUT", 6),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(TransformDataTypes.ETL_STEP_TYPE.getValue(), values);

        LOG.debug("Created definition for " + TransformDataTypes.ETL_STEP_TYPE.getValue());
        enumTypeDefinitionMap.put(TransformDataTypes.ETL_STEP_TYPE.getValue(), definition);
    }

    private void defineInstanceStatusEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("RUNNING", 1),
            new EnumValue("SUSPENDED", 2),
            new EnumValue("TERMINATED", 3),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(TransformDataTypes.ETL_INSTANCE_STATUS.getValue(), values);

        LOG.debug("Created definition for " + TransformDataTypes.ETL_INSTANCE_STATUS.getValue());
        enumTypeDefinitionMap.put(TransformDataTypes.ETL_INSTANCE_STATUS.getValue(), definition);
    }

    private void defineAbstractProcessSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE
            );
        LOG.debug("Created definition for " + TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue());
        classTypeDefinitions.put(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue(), definition);
    }

    private void defineStepSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue(),
                ImmutableList.of(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue()),
                new AttributeDefinition("lineage", LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue(),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("type", TransformDataTypes.ETL_STEP_TYPE.getValue(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("queryText", DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );

        LOG.debug("Created definition for " + TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue());
        classTypeDefinitions.put(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue(), definition);
    }

    private void defineStepSequenceSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue(),
                ImmutableList.<String>of(),
                new AttributeDefinition("preceding",
                    TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("succeeding",
                    TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("kind",
                    DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );

        LOG.debug("Created definition for " + TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue());
        classTypeDefinitions.put(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue(), definition);
    }

    private void defineTaskSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue(),
                ImmutableList.of(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue()),
                new AttributeDefinition("status",
                    TransformDataTypes.ETL_TASK_STATUS.getValue(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("lineage",
                    LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue(),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("steps",
                    DataTypes.arrayTypeName(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("stepsDAG",
                    DataTypes.arrayTypeName(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("id",
                    DataTypes.STRING_TYPE.getName(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("type",
                    TransformDataTypes.ETL_TASK_TYPE.getValue(),
                    Multiplicity.OPTIONAL, false, null),
                new AttributeDefinition("queryText",
                    DataTypes.STRING_TYPE.getName(),
                    Multiplicity.OPTIONAL, false, null)
            );

        LOG.debug("Created definition for " + TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue());
        classTypeDefinitions.put(TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue(), definition);
    }

    private void defineInstanceSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(
                TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue(),
                ImmutableList.of(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue()),
                NAME_ATTRIBUTE,
                DESCRIPTION_ATTRIBUTE,
                new AttributeDefinition("id", DataTypes.STRING_TYPE.getName(),
                    Multiplicity.REQUIRED, false, null),
                new AttributeDefinition("tasks",
                    DataTypes.arrayTypeName(TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("tasksDAG",
                    DataTypes.arrayTypeName(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue()),
                    Multiplicity.OPTIONAL, true, null),
                new AttributeDefinition("status",
                    TransformDataTypes.ETL_INSTANCE_STATUS.getValue(),
                    Multiplicity.OPTIONAL, false, null)
            );

        LOG.debug("Created definition for " + TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue());
        classTypeDefinitions.put(TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue(), definition);
    }
}
