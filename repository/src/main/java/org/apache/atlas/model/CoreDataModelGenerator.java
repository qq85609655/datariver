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
import org.apache.atlas.typesystem.types.StructTypeDefinition;
import org.apache.atlas.typesystem.types.utils.TypesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * 核心元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class CoreDataModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CoreDataModelGenerator.class);

    private final Map<String, HierarchicalTypeDefinition<ClassType>> classTypeDefinitions;
    private final Map<String, EnumTypeDefinition> enumTypeDefinitionMap;
    private final Map<String, StructTypeDefinition> structTypeDefinitionMap;


    public CoreDataModelGenerator() {
        classTypeDefinitions = new HashMap<>();
        enumTypeDefinitionMap = new HashMap<>();
        structTypeDefinitionMap = new HashMap<>();
    }

    public void createDataModel() throws AtlasException {
        LOG.info("Generating the core Data Model....");

        // core Types
        defineMetaSourceTypeEnum();

        defineDataElementSuperType();
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

    private static final AttributeDefinition UNIQUE_REQUIRED_NAME_ATTRIBUTE =
        TypesUtil.createUniqueRequiredAttrDef("qualifiedName", DataTypes.STRING_TYPE);
    private static final AttributeDefinition REQUIRED_SOURCE_ATTRIBUTE =
        TypesUtil.createRequiredAttrDef("metaSource", CoreDataTypes.ATLAS_META_SOURCE_TYPE.getValue());


    /* ------------------------Core meta model definition----------------------*/
    private void defineMetaSourceTypeEnum() throws AtlasException {
        EnumValue[] values = {
            new EnumValue("DXT", 1),
            new EnumValue("WORKFLOW", 2),
            new EnumValue("ODPS", 3),
        };

        EnumTypeDefinition definition =
            new EnumTypeDefinition(CoreDataTypes.ATLAS_META_SOURCE_TYPE.getValue(), values);

        LOG.debug("Created definition for " + CoreDataTypes.ATLAS_META_SOURCE_TYPE.getValue());
        enumTypeDefinitionMap.put(CoreDataTypes.ATLAS_META_SOURCE_TYPE.getValue(), definition);
    }

    private void defineDataElementSuperType() throws AtlasException {
        HierarchicalTypeDefinition<ClassType> definition = TypesUtil
            .createClassTypeDef(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue(),
                ImmutableList.<String>of(),
                UNIQUE_REQUIRED_NAME_ATTRIBUTE,
                REQUIRED_SOURCE_ATTRIBUTE
            );
        LOG.debug("Created definition for " + CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue());
        classTypeDefinitions.put(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue(), definition);
    }
}
