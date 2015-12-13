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

import com.google.inject.Inject;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.services.MetadataService;
import org.apache.atlas.typesystem.types.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用元模型注册
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class CommonMetaStoreBridge {
    private static final Logger LOG = LoggerFactory.getLogger(CommonMetaStoreBridge.class);
    private final TypeSystem typeSystem;
    private final MetadataService service;

    public CommonMetaStoreBridge(TypeSystem typeSystem, MetadataService service){
        this.typeSystem = typeSystem;
        this.service = service;
    }

    public synchronized void registerCommonDataModel() throws Exception {
        LOG.info(">>>>>>Beginning register common metamodel.");
        registerCoreDataModel();
        registerRelationalDataModel();
        registerLineageDataModel();
        registerTransformDataModel();
        LOG.info("<<<<<<Finished register common metamodel.");
    }

    private void registerCoreDataModel() throws Exception {
        CoreDataModelGenerator dataModelGenerator = new CoreDataModelGenerator();
        if (!typeSystem.isRegistered(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue())){
            service.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerLineageDataModel() throws Exception {
        LineageDataModelGenerator dataModelGenerator = new LineageDataModelGenerator();
        if(!typeSystem.isRegistered(LineageDataTypes.LINEAGE_FIELD_MAP.getValue())){
            service.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerRelationalDataModel() throws Exception {
        RelationalDataModelGenerator dataModelGenerator = new RelationalDataModelGenerator();
        if (!typeSystem.isRegistered(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue())){
            service.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerTransformDataModel() throws Exception {
        TransformDataModelGenerator dataModelGenerator = new TransformDataModelGenerator();
        if (!typeSystem.isRegistered(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue())){
            service.createType(dataModelGenerator.getModelAsJson());
        }
    }
}
