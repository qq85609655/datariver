/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.workFlow.model;

import com.google.common.collect.ImmutableList;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.common.model.LineageDataTypes;
import org.apache.atlas.common.model.TransformDataTypes;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkFlowDataModelGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(WorkFlowDataModelGenerator.class);

    private static final DataTypes.MapType STRING_MAP_TYPE =
        new DataTypes.MapType(DataTypes.STRING_TYPE, DataTypes.STRING_TYPE);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;

    public static final String COMMENT = "comment";

    public static final String STORAGE_NUM_BUCKETS = "numBuckets";
    public static final String STORAGE_IS_STORED_AS_SUB_DIRS = "storedAsSubDirectories";

    public static final String NAME = "name";
    public static final String TABLE_NAME = "tableName";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String TABLE = "table";
    public static final String DB = "db";

    public WorkFlowDataModelGenerator() {
        classTypeDefinitions = new HashMap();
        enumTypeDefinitionMap = new HashMap();
        structTypeDefinitionMap = new HashMap();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the Hive Data Model....");

        // enums
        createWorkFlowActionTypeEnum();
        this.createWorkFlowActionStatusEnum();
        this.createWorkFlowJobStatusEnum();
        this.createWorkFlowJobNumTimeunitEnum();

        // structs


        // classes
        this.createWorkFlowActionClass();
        this.createWorkFlowActionTemplateClass();
        ;
        this.createWorkFlowJobClass();
        this.createWorkFlowTemplateClass();
        // DDL/DML Process
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
        return ImmutableList.of();
    }

    //WORKFLOW_ACTION_TYPE
    private void createWorkFlowActionTypeEnum() throws AtlasException {
        WorkFlowActionType[] actionTypes = WorkFlowActionType.values();
        EnumValue[] values = new EnumValue[actionTypes.length];
        for (int index = 0; index < actionTypes.length; index++) {
            values[index] = new EnumValue(actionTypes[index].name(), index + 1);
        }
        EnumTypeDefinition definition =
            new EnumTypeDefinition(WorkFlowDataTypes.WORKFLOW_ACTION_TYPE.getValue(), values);
        enumTypeDefinitionMap.put(WorkFlowDataTypes.WORKFLOW_ACTION_TYPE.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_ACTION_TYPE.getValue());
    }

    private void createWorkFlowActionStatusEnum() throws AtlasException {
        EnumValue[] values = {new EnumValue("PREP", 1), new EnumValue("RUNNING", 2), new EnumValue("OK", 3),
            new EnumValue("ERROR", 4), new EnumValue("USER_RETRY", 5), new EnumValue("START_RETRY", 6),
            new EnumValue("START_MANUAL", 7), new EnumValue("DONE", 8), new EnumValue("END_RETRY", 9),
            new EnumValue("END_MANUAL", 10), new EnumValue("KILLED", 11), new EnumValue("FAILED", 12)};

        EnumTypeDefinition definition =
            new EnumTypeDefinition(WorkFlowDataTypes.WORKFLOW_ACTION_STATUS.getValue(), values);
        enumTypeDefinitionMap.put(WorkFlowDataTypes.WORKFLOW_ACTION_STATUS.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_ACTION_STATUS.getValue());
    }

    private void createWorkFlowJobStatusEnum() throws AtlasException {
        //PREP, RUNNING, SUCCEEDED, KILLED, FAILED, SUSPENDED
        EnumValue[] values = {new EnumValue("PREP", 1), new EnumValue("RUNNING", 2), new EnumValue("SUCCEEDED", 3),
            new EnumValue("KILLED", 4), new EnumValue("FAILED", 5), new EnumValue("SUSPENDED", 6)};

        EnumTypeDefinition definition =
            new EnumTypeDefinition(WorkFlowDataTypes.WORKFLOW_JOB_STATUS.getValue(), values);
        enumTypeDefinitionMap.put(WorkFlowDataTypes.WORKFLOW_JOB_STATUS.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_JOB_STATUS.getValue());
    }

    private void createWorkFlowJobNumTimeunitEnum() throws AtlasException {
        //MINUTE, HOUR, DAY, WEEK, MONTH, END_OF_DAY, END_OF_MONTH, CRON, NONE
        EnumValue[] values = {new EnumValue("MINUTE", 1), new EnumValue("HOUR", 2), new EnumValue("DAY", 3),
            new EnumValue("WEEK", 4), new EnumValue("MONTH", 5), new EnumValue("END_OF_DAY", 6),
            new EnumValue("END_OF_MONTH", 7), new EnumValue("CRON", 8), new EnumValue("NONE", 9)};

        EnumTypeDefinition definition =
            new EnumTypeDefinition(WorkFlowDataTypes.WORKFLOW_JOB_RUN_TIMEUNIT.getValue(), values);
        enumTypeDefinitionMap.put(WorkFlowDataTypes.WORKFLOW_JOB_RUN_TIMEUNIT.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_JOB_RUN_TIMEUNIT.getValue());
    }

    private void createWorkFlowActionClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("type", WorkFlowDataTypes.WORKFLOW_ACTION_TYPE.getValue(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("actionId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("instanceId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("etlInstance", TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("workflowId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("config", DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("configId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("startTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("endTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("status", WorkFlowDataTypes.WORKFLOW_ACTION_STATUS.getValue(),
                Multiplicity.OPTIONAL, false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<ClassType>(ClassType.class, WorkFlowDataTypes.WORKFLOW_ACTION.getValue(),
                ImmutableList.of(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue()), attributeDefinitions);
        classTypeDefinitions.put(WorkFlowDataTypes.WORKFLOW_ACTION.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_ACTION.getValue());
    }


    private void createWorkFlowActionTemplateClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition(NAME, DataTypes.STRING_TYPE.getName(), Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("templateId", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("actions",
                DataTypes.arrayTypeName(WorkFlowDataTypes.WORKFLOW_ACTION.getValue()),
                Multiplicity.COLLECTION, false, null),
            new AttributeDefinition("config", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("workflowActionName", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<ClassType>(ClassType.class,
                WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue(),
                ImmutableList.of(AtlasClient.REFERENCEABLE_SUPER_TYPE), attributeDefinitions);
        classTypeDefinitions.put(WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue());
    }


    private void createWorkFlowJobClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition(NAME, DataTypes.STRING_TYPE.getName(), Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("workflowId", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL, false,
                null),
            new AttributeDefinition("actions",
                DataTypes.arrayTypeName(WorkFlowDataTypes.WORKFLOW_ACTION.getValue()),
                Multiplicity.COLLECTION, true, null),
            new AttributeDefinition("actionsDAG",
                DataTypes.arrayTypeName(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue()),
                Multiplicity.OPTIONAL, true, null),
            new AttributeDefinition("config", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("createTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("startTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("endTime", DataTypes.LONG_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("user", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("status", WorkFlowDataTypes.WORKFLOW_JOB_STATUS.getValue(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("parentId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<ClassType>(ClassType.class, WorkFlowDataTypes.WORKFLOW_JOB.getValue(),
                ImmutableList.of(AtlasClient.REFERENCEABLE_SUPER_TYPE), attributeDefinitions);
        classTypeDefinitions.put(WorkFlowDataTypes.WORKFLOW_JOB.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_JOB.getValue());
    }


    private void createWorkFlowTemplateClass() throws AtlasException {
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition(NAME, DataTypes.STRING_TYPE.getName(),
                Multiplicity.REQUIRED, false, null),
            new AttributeDefinition("description", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("templateId", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("workflowJobs",
                DataTypes.arrayTypeName(WorkFlowDataTypes.WORKFLOW_JOB.getValue()),
                Multiplicity.COLLECTION, false, null),
            new AttributeDefinition("config", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("workflowName", DataTypes.STRING_TYPE.getName(),
                Multiplicity.OPTIONAL, false, null),
            new AttributeDefinition("lineage", LineageDataTypes.LINEAGE_WORKFLOW_PROCESS_INFO.getValue(),
                Multiplicity.OPTIONAL, false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<ClassType>(ClassType.class,
                WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue(),
                ImmutableList.of(AtlasClient.REFERENCEABLE_SUPER_TYPE), attributeDefinitions);
        classTypeDefinitions.put(WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue(), definition);
        LOG.debug("Created definition for " + WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue());
    }

    public String getModelAsJson() throws AtlasException {
        createDataModel();
        return getDataModelAsJSON();
    }

    public static void main(String[] args) throws Exception {
        WorkFlowDataModelGenerator wfDataModelGenerator = new WorkFlowDataModelGenerator();
        System.out.println("hiveDataModelAsJSON = " + wfDataModelGenerator.getModelAsJson());

        TypesDef typesDef = wfDataModelGenerator.getTypesDef();
        for (EnumTypeDefinition enumType : typesDef.enumTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - %s", enumType.name, EnumType.class.getSimpleName(),
                Arrays.toString(enumType.enumValues)));
        }
        for (StructTypeDefinition structType : typesDef.structTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - %s", structType.typeName, StructType.class.getSimpleName(),
                Arrays.toString(structType.attributeDefinitions)));
        }
        for (HierarchicalTypeDefinition<ClassType> classType : typesDef.classTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - %s", classType.typeName, ClassType.class.getSimpleName(),
                Arrays.toString(classType.attributeDefinitions)));
        }
        for (HierarchicalTypeDefinition<TraitType> traitType : typesDef.traitTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - %s", traitType.typeName, TraitType.class.getSimpleName(),
                Arrays.toString(traitType.attributeDefinitions)));
        }
    }
}
