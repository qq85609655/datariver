package org.apache.atlas.workFlow.model;

import com.google.common.collect.ImmutableList;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.model.LineageDataTypes;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.apache.atlas.workFlow.connection.AtlasConnectionFactory;
import org.apache.atlas.workFlow.handler.WorkflowJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述信息
 *
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/28 17:38
 */
public class RegisteredModel {

    private static final Logger LOG = LoggerFactory.getLogger(RegisteredModel.class);

    private AtlasClient dgiClient = AtlasConnectionFactory.getAtlasClient();

    public void registerAllModel() throws Exception {
        registerCommonModel();
        registerWorkflowDataModel();
    }

    public boolean registerCommonModel() throws Exception {
        CommonMetaStoreBridge commonMetaStoreBridge = null;
        commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();
        if (dgiClient.getType(LineageDataTypes.LINEAGE_FIELD_MAP.getValue()) != null) {
            return true;
        }
        return false;
    }

    public boolean registerWorkflowDataModel() throws Exception {
        boolean sign = false;
        WorkFlowDataModelGenerator dataModelGenerator = new WorkFlowDataModelGenerator();

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
            LOG.info("typesAsJSON = " + typesAsJSON);
            dgiClient.createType(typesAsJSON);
        }
        LOG.info("<<<<<<Finished register workflow metamodel.");

        if ((dgiClient.getType(WorkFlowDataTypes.WORKFLOW_ACTION.getValue()) != null)
                && (dgiClient.getType(WorkflowJobHandler.WORKFLOW_TRAIT) != null)) {
            sign = true;
        }
        return sign;
    }
}
