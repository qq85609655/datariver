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

package org.apache.atlas.common.bridge;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.model.*;
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
    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";

    private static final Logger LOG = LoggerFactory.getLogger(CommonMetaStoreBridge.class);

    private final AtlasClient atlasClient;

    public CommonMetaStoreBridge() throws Exception {
        atlasClient = new AtlasClient(DEFAULT_DGI_URL);
    }

    public AtlasClient getAtlasClient() {
        return atlasClient;
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
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue());
            LOG.info("Core data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering Core data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerLineageDataModel() throws Exception {
        LineageDataModelGenerator dataModelGenerator = new LineageDataModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(LineageDataTypes.LINEAGE_FIELD_MAP.getValue());
            LOG.info("Lineage data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering Lineage data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerRelationalDataModel() throws Exception {
        RelationalDataModelGenerator dataModelGenerator = new RelationalDataModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue());
            LOG.info("Relational data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering Relational data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
    }

    private void registerTransformDataModel() throws Exception {
        TransformDataModelGenerator dataModelGenerator = new TransformDataModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue());
            LOG.info("Transform data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering Transform data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
    }

    public static void main(String[] argv) throws Exception {
        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();
    }
}
