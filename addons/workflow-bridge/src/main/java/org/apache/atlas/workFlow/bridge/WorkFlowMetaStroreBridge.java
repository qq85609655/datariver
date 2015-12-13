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

package org.apache.atlas.workFlow.bridge;

import com.google.common.collect.ImmutableList;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.CommonMetaStoreBridge;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.apache.atlas.workFlow.handler.WorkflowJobHandler;
import org.apache.atlas.workFlow.model.WorkFlowDataModelGenerator;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkFlowMetaStroreBridge {

    private static final Logger LOG = LoggerFactory.getLogger(WorkFlowMetaStroreBridge.class);

    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";

    private final AtlasClient atlasClient;

    public WorkFlowMetaStroreBridge() throws Exception {
        atlasClient = new AtlasClient(DEFAULT_DGI_URL);
    }

    public AtlasClient getAtlasClient() {
        return atlasClient;
    }

    public synchronized void registerWorkflowDataModel() throws Exception {
        WorkFlowDataModelGenerator dataModelGenerator = new WorkFlowDataModelGenerator();
        AtlasClient dgiClient = AtlasConnectionFactory.getAtlasClient();

        LOG.info(">>>>>>Beginning register workflow metamodel.");
        try {
            dgiClient.getType(WorkFlowDataTypes.WORKFLOW_ACTION.getValue());
            LOG.info("Workflow data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering workflow data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());

            //trait
            HierarchicalTypeDefinition<TraitType> workflowTraitDef =
                TypesUtil.createTraitTypeDef(WorkflowJobHandler.WORKFLOW_TRAIT, null);
            TypesDef typesDef = TypesUtil.getTypesDef(ImmutableList.<EnumTypeDefinition>of(),
                ImmutableList.<StructTypeDefinition>of(),
                ImmutableList.of(workflowTraitDef),
                ImmutableList.<HierarchicalTypeDefinition<ClassType>>of());
            String typesAsJSON = TypesSerialization.toJson(typesDef);
            dgiClient.createType(typesAsJSON);
        }
        LOG.info("<<<<<<Finished register workflow metamodel.");
    }

    public static void main(String[] argv) throws Exception {
        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();

        WorkFlowMetaStroreBridge workFlowMetaStroreBridge = new WorkFlowMetaStroreBridge();
        workFlowMetaStroreBridge.registerWorkflowDataModel();
        //WorkflowHook.getInstance();
    }
}
