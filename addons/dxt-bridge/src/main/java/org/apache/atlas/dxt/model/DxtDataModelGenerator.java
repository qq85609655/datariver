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

package org.apache.atlas.dxt.model;

import com.google.common.collect.ImmutableList;

import org.apache.atlas.AtlasException;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.common.model.TransformDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.AttributeDefinition;
import org.apache.atlas.typesystem.types.ClassType;
import org.apache.atlas.typesystem.types.DataTypes;
import org.apache.atlas.typesystem.types.EnumType;
import org.apache.atlas.typesystem.types.EnumTypeDefinition;
import org.apache.atlas.typesystem.types.HierarchicalTypeDefinition;
import org.apache.atlas.typesystem.types.Multiplicity;
import org.apache.atlas.typesystem.types.StructType;
import org.apache.atlas.typesystem.types.StructTypeDefinition;
import org.apache.atlas.typesystem.types.TraitType;
import org.apache.atlas.typesystem.types.TypeUtils;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility that generates dxt data model for both meta store entities and DDL/DML queries.
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtDataModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DxtDataModelGenerator.class);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;


    public DxtDataModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the DXT Data Model....");

        // enums

        // structs

        // classes
        createGeneralAccinfoClass();
        createTransInstanceClass();
        createTransStepClass();
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
        HierarchicalTypeDefinition<TraitType> accTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.ACCINFO_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> transTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.TRANS_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> stepTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.STEP_TRAIT, null);

        HierarchicalTypeDefinition<TraitType> tableTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.TABLE_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> containerTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.CONTAINER_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> fieldTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.FIELD_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> taskTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.TASK_TRAIT, null);

        HierarchicalTypeDefinition<TraitType> tableInputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.TABLEINPUT_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> tableOutputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.TABLEOUTPUT_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> hdfsInputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.HDFSINPUT_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> hdfsOutputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.HDFSOUTPUT_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> odpsInputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.ODPSINPUT_TRAIT, null);
        HierarchicalTypeDefinition<TraitType> odpsOutputTraitDef =
            TypesUtil.createTraitTypeDef(DxtConstant.ODPSOUTPUT_TRAIT, null);

        return ImmutableList.of(accTraitDef, transTraitDef, stepTraitDef, tableTraitDef,
            containerTraitDef, fieldTraitDef, taskTraitDef, tableInputTraitDef, tableOutputTraitDef,
            hdfsInputTraitDef, hdfsOutputTraitDef, odpsInputTraitDef, odpsOutputTraitDef);
    }

    private void createGeneralAccinfoClass() throws AtlasException {
        String typeName = DxtDataTypes.GENERAL_ACCINFO.getValue();
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("host", DataTypes.STRING_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),
            new AttributeDefinition("port", DataTypes.INT_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),
            new AttributeDefinition("version", DataTypes.FLOAT_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, typeName,
                ImmutableList.of(RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue()),
                attributeDefinitions);
        classTypeDefinitions.put(typeName, definition);
        LOG.debug("Created definition for " + typeName);
    }

    private void createTransInstanceClass() throws AtlasException {
        String typeName = DxtDataTypes.TRANS_INSTANCE.getValue();
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("lastExecuteTime", DataTypes.LONG_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),
            new AttributeDefinition("runTimes", DataTypes.INT_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),
            new AttributeDefinition("successTimes", DataTypes.INT_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),
            new AttributeDefinition("failTimes", DataTypes.INT_TYPE.getName(), Multiplicity.OPTIONAL,
                false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, typeName,
                ImmutableList.of(TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue()),
                attributeDefinitions);
        classTypeDefinitions.put(typeName, definition);
        LOG.debug("Created definition for " + typeName);
    }

    private void createTransStepClass() throws AtlasException {
        String typeName = DxtDataTypes.TRANS_STEP.getValue();
        AttributeDefinition[] attributeDefinitions = new AttributeDefinition[]{
            new AttributeDefinition("db", RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
                Multiplicity.OPTIONAL, false, null),};

        HierarchicalTypeDefinition<ClassType> definition =
            new HierarchicalTypeDefinition<>(ClassType.class, typeName,
                ImmutableList.of(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue()),
                attributeDefinitions);
        classTypeDefinitions.put(typeName, definition);
        LOG.debug("Created definition for " + typeName);
    }

    public String getModelAsJson() throws AtlasException {
        createDataModel();
        return getDataModelAsJSON();
    }

    public static void main(String[] args) throws Exception {
        DxtDataModelGenerator dxtDataModelGenerator = new DxtDataModelGenerator();
        System.out.println("dxtDataModelAsJSON = " + dxtDataModelGenerator.getModelAsJson());

        TypesDef typesDef = dxtDataModelGenerator.getTypesDef();
        for (EnumTypeDefinition enumType : typesDef.enumTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - values %s", enumType.name, EnumType.class.getSimpleName(),
                Arrays.toString(enumType.enumValues)));
        }
        for (StructTypeDefinition structType : typesDef.structTypesAsJavaList()) {
            System.out.println(
                String.format("%s(%s) - attributes %s", structType.typeName, StructType.class.getSimpleName(),
                    Arrays.toString(structType.attributeDefinitions)));
        }
        for (HierarchicalTypeDefinition<ClassType> classType : typesDef.classTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - super types [%s] - attributes %s", classType.typeName,
                ClassType.class.getSimpleName(),
                StringUtils.join(classType.superTypes, ","),
                Arrays.toString(classType.attributeDefinitions)));
        }
        for (HierarchicalTypeDefinition<TraitType> traitType : typesDef.traitTypesAsJavaList()) {
            System.out.println(String.format("%s(%s) - %s", traitType.typeName, TraitType.class.getSimpleName(),
                Arrays.toString(traitType.attributeDefinitions)));
        }
    }
}
