package org.apache.atlas.odps.hook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.dtdream.dthink.dtalent.datastudio.activemq.MessageEnum;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.hook.BaseHook;
import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.odps.bridge.OdpsMetaStoreBridge;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.common.util.BaseAddonIT;

/**
 * odps集成測試入口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class OdpsIT extends BaseAddonIT {
    private static final String  QUALIFIED_NAME = BaseJsonParser.QUALIFIED_NAME;
    private AtlasClient dgiCLient;

    @BeforeClass
    public void setUp() throws Exception {
        BaseAddonIT("odpsModel.json");
        dgiCLient = getAtlasClient();
    }
    @Test
    public void testMetaModel() throws Exception {
        try {
            CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
            commonMetaStoreBridge.registerCommonDataModel();
        } catch (Exception e) {
            //第二次创建则抛异常,后面的测试正常即可
        }
        try {
            OdpsMetaStoreBridge bridge = new OdpsMetaStoreBridge();
            bridge.registerOdpsDataModel();
        } catch (Exception e) {
            //第二;次创建则抛异常,后面的测试正常即可
        }
        //Assert.assertNotNull(dgiCLient.getType("DataElement"));
        assertEnum(OdpsDataTypes.ODPS_OBJECT_TYPE.getValue());
        assertEnum(OdpsDataTypes.ODPS_RESOURCE_TYPE.getValue());
        assertEnum(OdpsDataTypes.ODPS_OBJECT_PRIVILEGE.getValue());
        assertEnum(OdpsDataTypes.ODPS_PACKAGE_RESOURCE_TYPE.getValue());
        assertClass(OdpsDataTypes.ODPS_ACCINFO.getValue());
        assertClass(OdpsDataTypes.ODPS_PROJECT.getValue());
        assertClass(OdpsDataTypes.ODPS_COLUMN.getValue());
        assertClass(OdpsDataTypes.ODPS_PARTITION.getValue());
        assertClass(OdpsDataTypes.ODPS_TABLE.getValue());
        assertClass(OdpsDataTypes.ODPS_RESOURCE.getValue());
        assertClass(OdpsDataTypes.ODPS_TASK.getValue());
        assertClass(OdpsDataTypes.ODPS_INSTANCE.getValue());
        assertClass(OdpsDataTypes.ODPS_PACKAGE_RESOURCE_ITEM.getValue());
        assertClass(OdpsDataTypes.ODPS_PACKAGE.getValue());
    }

    @Test(dependsOnMethods = "testMetaModel")
    public void testMetaData() throws Exception {
        OdpsHook hook = new OdpsHook();
        BaseHook.HookContext context = new BaseHook.HookContext();
        context.putParam("workspaceName", "ddd");
        context.putParam("instanceId", "ddd");
        context.putParam(CommonInfo.SOURCE_TYPE, MessageEnum.SystemName.WORKFLOW.name());
        hook.startMock();
        hook.doRun(context);
//        Thread.sleep(OdpsHook.SLEEP_FOR_NOTIFY_TIME);
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_PROJECT.getValue(), QUALIFIED_NAME, "odps.default.metatest111"));
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_COLUMN.getValue(), QUALIFIED_NAME, "odps.default.dtdream_hanwen.testp123.c123"));
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_PACKAGE.getValue(), QUALIFIED_NAME, "odps.default.dtdream_hanwen.p123"));
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_TABLE.getValue(), QUALIFIED_NAME, "odps.default.dtdream_hanwen.j0051_table1"));
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_INSTANCE.getValue(), QUALIFIED_NAME, "odps.20151019064734467guuskdx5"));
        Assert.assertNotNull(dgiCLient.getEntity(OdpsDataTypes.ODPS_TASK.getValue(), QUALIFIED_NAME, "odps.20151019064734467guuskdx5.MRonSQL_144523723601604416757"));
        Assert.assertNotNull(dgiCLient.getEntity(RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(), QUALIFIED_NAME, "odps.default.xuxiang_test.wc_out"));
    }

    private void assertEnum(String enumName) throws Exception {
        String type = dgiCLient.getType(enumName);
        Assert.assertNotNull(type);
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
        classDefinition = JSON.parseObject(classDefinition.toJSONString(), Feature.OrderedField);
        JSONObject classTypes = JSON.parseObject(type).getJSONArray("classTypes").getJSONObject(0);
        Assert.assertEquals(JSON.parseObject(classTypes.toJSONString(), Feature.OrderedField), classDefinition);
    }
}
