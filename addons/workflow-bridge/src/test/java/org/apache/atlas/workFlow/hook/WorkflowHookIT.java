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

package org.apache.atlas.workFlow.hook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.util.BaseAddonIT;
import org.apache.atlas.workFlow.model.WorkFlowDataModelGenerator;
import org.apache.atlas.workFlow.model.WorkFlowDataTypes;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkflowHookIT extends BaseAddonIT {

    private AtlasClient dgiClient;

    @BeforeClass
    public void setUp() throws Exception {
        BaseAddonIT("workflowModel.json");
        dgiClient = getAtlasClient();
    }

    @Test
    public void testMetaModel() throws Exception {
        WorkFlowDataModelGenerator dataModelGenerator = new WorkFlowDataModelGenerator();

        // 多次运行测试用例时，可能上次的数据还存在
        try {
            dgiClient.getType(WorkFlowDataTypes.WORKFLOW_ACTION.getValue());
            LOG.info("Workflow data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            LOG.info("Registering workflow data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }

        String[] name = {
            WorkFlowDataTypes.WORKFLOW_ACTION_TYPE.getValue(),
            WorkFlowDataTypes.WORKFLOW_ACTION_STATUS.getValue(),
            WorkFlowDataTypes.WORKFLOW_JOB_STATUS.getValue(),
            WorkFlowDataTypes.WORKFLOW_JOB_RUN_TIMEUNIT.getValue(),
            WorkFlowDataTypes.WORKFLOW_ACTION.getValue(),
            WorkFlowDataTypes.WORKFLOW_ACTION_TEMPLATE.getValue(),
            WorkFlowDataTypes.WORKFLOW_JOB.getValue(),
            WorkFlowDataTypes.WORKFLOW_TEMPLATE.getValue()
        };

        // Enums
        assertEnum(name[0]);
        assertEnum(name[1]);
        assertEnum(name[2]);
        assertEnum(name[3]);
        assertClass(name[4]);
        assertClass(name[5]);
        assertClass(name[6]);
        assertClass(name[7]);
    }

    private void assertEnum(String enumName) throws Exception {
        String type = dgiClient.getType(enumName);
        JSONObject enumDefinition = getEnumDefinitionByName(enumName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("enumTypes").get(0), enumDefinition);
    }

    private void assertStruct(String structName) throws Exception {
        String type = dgiClient.getType(structName);
        JSONObject structDefinition = getStructDefinitionByName(structName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("structTypes").get(0), structDefinition);
    }

    private void assertClass(String className) throws Exception {
        String type = dgiClient.getType(className);
        JSONObject classDefinition = getClassDefinitionByName(className);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("classTypes").get(0), classDefinition);
    }
}
