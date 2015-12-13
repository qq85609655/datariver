package org.apache.atlas.odps.examples;

import com.dtdream.dthink.dtalent.datastudio.activemq.MessageEnum;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.model.CommonMetaStoreBridge;
import org.apache.atlas.common.hook.BaseHook;
import org.apache.atlas.model.RelationalDataTypes;
import org.apache.atlas.model.TransformDataTypes;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.odps.bridge.OdpsMetaStoreBridge;
import org.apache.atlas.odps.hook.OdpsHook;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.codehaus.jettison.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-07 9:33
 */
public class OdpsQuickStart {
    private static final Logger LOG = LoggerFactory.getLogger(OdpsQuickStart.class);

    private AtlasClient dgiClient;
    private static final List<String> TYPES = new ArrayList<>();

    static {
        //ODPS 的type
        for (OdpsDataTypes type : OdpsDataTypes.values()) {
            TYPES.add(type.getValue());
        }
    }

    public OdpsQuickStart() {
        dgiClient = new AtlasClient(OdpsHook.DEFAULT_DGI_URL);
    }

    public static void main(String[] args) throws Exception {
        OdpsQuickStart odpsQuickStart = new OdpsQuickStart();
        odpsQuickStart.createTypes();
        odpsQuickStart.createEntities();
        Thread.sleep(OdpsHook.SLEEP_FOR_NOTIFY_TIME);
        odpsQuickStart.search();
        System.exit(0);
    }

    private void createTypes() throws Exception {
        OdpsMetaStoreBridge bridge = new OdpsMetaStoreBridge();
        bridge.registerOdpsDataModel();

        //验证
        verifyTypesCreated();
    }

    private void verifyTypesCreated() throws Exception {
        List<String> types = dgiClient.listTypes();
        for (String type : TYPES) {
            assert types.contains(type);
        }
    }

    private void createEntities() {
        OdpsHook hook = new OdpsHook();
        BaseHook.HookContext context = new BaseHook.HookContext();
        context.putParam("workspaceName", "ddd");
        context.putParam("instanceId", "ddd");
        context.putParam(CommonInfo.SOURCE_TYPE, MessageEnum.SystemName.WORKFLOW.name());
        OdpsHook.MockHolder.quickStart();
        hook.run(context);
    }

    private String[] getDSLQueries() {
        String[] commonTypes = new String[]{TransformDataTypes.ETL_INSTANCE_SUPER_TYPE.getValue(),
                TransformDataTypes.ETL_STEP_SUPER_TYPE.getValue(), TransformDataTypes.ETL_TASK_SUPER_TYPE.getValue(),
                RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(), RelationalDataTypes.DATA_TABLE_SUPER_TYPE.getValue(),
                RelationalDataTypes.DATA_FIELD_SUPER_TYPE.getValue(), RelationalDataTypes.DB_ACCESS_SUPER_TYPE.getValue()};
        String[] odpsTyps = new String[]{OdpsDataTypes.ODPS_ACCINFO.getValue(),
                OdpsDataTypes.ODPS_COLUMN.getValue(), OdpsDataTypes.ODPS_INSTANCE.getValue(),
                OdpsDataTypes.ODPS_PACKAGE.getValue(), OdpsDataTypes.ODPS_PACKAGE_RESOURCE_ITEM.getValue(),
                OdpsDataTypes.ODPS_PARTITION.getValue(), OdpsDataTypes.ODPS_PROJECT.getValue(),
                OdpsDataTypes.ODPS_RESOURCE.getValue(), OdpsDataTypes.ODPS_TABLE.getValue(), OdpsDataTypes.ODPS_TASK.getValue()};
        int commonLength = commonTypes.length;
        int odpsLength = odpsTyps.length;
        String[] all = new String[commonLength + odpsLength];
        System.arraycopy(commonTypes, 0, all, 0, commonLength);
        System.arraycopy(odpsTyps, 0, all, commonLength, odpsLength);
        return all;
    }

    private void search() throws Exception {
        for (String dslQuery : getDSLQueries()) {
            JSONArray results = dgiClient.search(dslQuery);
            if (results != null) {
                System.out.println("query [" + dslQuery + "] returned [" + results.length() + "] rows");
            } else {
                System.out.println("query [" + dslQuery + "] failed.");
            }
        }
    }
}
