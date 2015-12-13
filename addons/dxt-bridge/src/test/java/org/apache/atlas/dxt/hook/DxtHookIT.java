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

package org.apache.atlas.dxt.hook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.model.CommonMetaStoreBridge;
import org.apache.atlas.common.util.BaseAddonIT;
import org.apache.atlas.dxt.bridge.DxtMetaStoreBridge;
import org.apache.atlas.dxt.examples.DxtQuickStart;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.codehaus.jettison.json.JSONArray;

import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * IT for DXT bridge.
 */
public class DxtHookIT extends BaseAddonIT {
    public static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DxtHookIT.class);
    private AtlasClient dgiCLient;

    @BeforeClass
    public void setUp() throws Exception {
        BaseAddonIT("dxtModel.json");
        dgiCLient = getAtlasClient();

        DxtMetaStoreBridge dxtMetaStoreBridge = new DxtMetaStoreBridge();
        dxtMetaStoreBridge.registerDxtDataModel();

        //导入元数据
        DxtQuickStart dxtQuickStart = new DxtQuickStart(true);
        dxtQuickStart.createEntities();
    }

    @Test
    public void testClasses() throws Exception {
        LOG.info("Begin to test class define.");
        assertClass(DxtDataTypes.GENERAL_ACCINFO.getValue());
        assertClass(DxtDataTypes.TRANS_INSTANCE.getValue());
        assertClass(DxtDataTypes.TRANS_STEP.getValue());
    }

    @Test
    public void testEntities() throws Exception {
        LOG.info("Begin to test entities.");
        JSONArray results;

        results = dgiCLient.search("TransStep");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);

        results = dgiCLient.search("TransInstance");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);

        results = dgiCLient.search("GeneralAccInfo");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);

        results = dgiCLient.search("DataContainer");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);

        results = dgiCLient.search("DataTable");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);

        results = dgiCLient.search("LineageProcessInfo");
        Assert.assertNotEquals(results, null);
        Assert.assertNotEquals(results.length(), 0);
    }

    private void assertEnum(String enumName) throws Exception {
        String type = dgiCLient.getType(enumName);
        JSONObject enumDefinition = getEnumDefinitionByName(enumName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("enumTypes").get(0), enumDefinition);
    }

    private void assertStruct(String structName) throws Exception {
        String type = dgiCLient.getType(structName);
        JSONObject structDefinition = getStructDefinitionByName(structName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("structTypes").get(0), structDefinition);
    }

    private void assertClass(String className) throws Exception {
        String type = dgiCLient.getType(className);
        JSONObject classDefinition = getClassDefinitionByName(className);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("classTypes").get(0), classDefinition);
    }
}
