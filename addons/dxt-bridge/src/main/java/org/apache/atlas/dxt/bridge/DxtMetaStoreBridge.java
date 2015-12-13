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

package org.apache.atlas.dxt.bridge;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.CommonMetaStoreBridge;
import org.apache.atlas.dxt.model.DxtDataModelGenerator;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Bridge Utility that imports metadata from the DXT Meta Store
 * and registers then in Atlas.
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtMetaStoreBridge {
    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";

    private static final Logger LOG = LoggerFactory.getLogger(DxtMetaStoreBridge.class);

    private final AtlasClient atlasClient;

    public DxtMetaStoreBridge() throws Exception {
        atlasClient = new AtlasClient(DEFAULT_DGI_URL);
    }

    public AtlasClient getAtlasClient() {
        return atlasClient;
    }

    public synchronized void registerDxtDataModel() throws Exception {
        DxtDataModelGenerator dataModelGenerator = new DxtDataModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        LOG.info(">>>>>>Beginning register Dxt metamodel.");
        try {
            dgiClient.getType(DxtDataTypes.GENERAL_ACCINFO.getValue());
            LOG.info("Dxt data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering Dxt data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
        LOG.info("<<<<<<Finished register Dxt metamodel.");
    }

    public static void main(String[] argv) throws Exception {
        DxtMetaStoreBridge dxtMetaStoreBridge = new DxtMetaStoreBridge();
        dxtMetaStoreBridge.registerDxtDataModel();
    }
}
