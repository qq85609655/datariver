package org.apache.atlas.common.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.util.BaseAddonIT;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 通用元模型测试
 *
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public class DataModelIT extends BaseAddonIT {
    private AtlasClient dgiCLient;

    @BeforeClass
    public void setUp() throws Exception {
        BaseAddonIT("commonModel.json");
        dgiCLient = getAtlasClient();

        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();
    }

    @Test
    public void testMetaModel() throws Exception {
        assertEnum(CoreDataTypes.ATLAS_META_SOURCE_TYPE.getValue());
        assertClass(CoreDataTypes.DATA_ELEMENT_SUPER_TYPE.getValue());

        assertEnum(RelationalDataTypes.DATA_CONTAINER_TYPE.getValue());
        assertEnum(RelationalDataTypes.DATA_CONTAINER_STATUS.getValue());
        assertClass(RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue());
        assertClass(RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue());
        assertClass(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue());
        assertClass(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue());

        assertEnum(LineageDataTypes.LINEAGE_DEPEND_TYPE.getValue());
        assertStruct(LineageDataTypes.LINEAGE_DEPENDENCY.getValue());
        assertClass(LineageDataTypes.LINEAGE_PROCESS_INFO.getValue());
        assertClass(LineageDataTypes.LINEAGE_TASK_PROCESS_INFO.getValue());
        assertClass(LineageDataTypes.LINEAGE_STEP_PROCESS_INFO.getValue());
        assertClass(LineageDataTypes.LINEAGE_FIELD_MAP.getValue());
        assertClass(LineageDataTypes.LINEAGE_TASK_FIELD_MAP.getValue());
        assertClass(LineageDataTypes.LINEAGE_STEP_FIELD_MAP.getValue());

        assertEnum(TransformDataTypes.ETL_TASK_TYPE.getValue());
        assertEnum(TransformDataTypes.ETL_TASK_STATUS.getValue());
        assertEnum(TransformDataTypes.ETL_STEP_TYPE.getValue());
        assertEnum(TransformDataTypes.ETL_INSTANCE_STATUS.getValue());
        assertClass(TransformDataTypes.ABSTRACT_PROCESS_SUPER_TYPE.getValue());
        assertClass(TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue());
        assertClass(TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue());
        assertClass(TransformDataTypes.ETL_STEP_SEQUENCE_SUPER_TYPE.getValue());
        assertClass(TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue());
    }

    private void assertEnum(String enumName) throws Exception {
        String type = dgiCLient.getType(enumName);
        JSONObject enumDefinition = getEnumDefinitionByName(enumName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("enumTypes").getJSONObject(0), enumDefinition);
    }

    private void assertStruct(String structName) throws Exception {
        String type = dgiCLient.getType(structName);
        JSONObject structDefinition = getStructDefinitionByName(structName);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("structTypes").getJSONObject(0), structDefinition);
    }

    private void assertClass(String className) throws Exception {
        String type = dgiCLient.getType(className);
        JSONObject classDefinition = getClassDefinitionByName(className);
        Assert.assertEquals(JSON.parseObject(type).getJSONArray("classTypes").getJSONObject(0), classDefinition);
    }
}
