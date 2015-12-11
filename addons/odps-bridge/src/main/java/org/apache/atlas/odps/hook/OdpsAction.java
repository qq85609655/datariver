package org.apache.atlas.odps.hook;

import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.common.hook.BaseHook;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.common.util.LineageHandler;
import org.apache.atlas.odps.bridge.OdpsMetaStoreBridge;
import org.elasticsearch.common.xcontent.ToXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * workflow分流后调用odps，生成元数据的入口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class OdpsAction implements CommonInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdpsAction.class);
    /*
    *  paramMap.put("instanceId",String.valueOf(actionRef.get("instanceId")));
            paramMap.put("config",String.valueOf(actionRef.get("config")));
            paramMap.put("actionGuid",actionGuid);
            paramMap.put("workspaceName",workspaceName);
            paramMap.put("configId",configId);
    * */

    public synchronized  void registerDataModel(){
        try{
            CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
            commonMetaStoreBridge.registerCommonDataModel();
            OdpsMetaStoreBridge bridge = new OdpsMetaStoreBridge();
            bridge.registerOdpsDataModel();
        } catch (Exception e){
            LOGGER.error("Register data model error", e);
        }
    }
     @Override
    public void sendActionConf(Map<String,String> paramMap, LineageHandler lineageHandler) {
         registerDataModel();
         BaseHook.HookContext context = new BaseHook.HookContext();
         context.setParams(paramMap);
         context.setLineageHandler(lineageHandler);
         new OdpsHook().run(context);
    }
    public static void main(String[] args) {
        OdpsAction odpsAction = new OdpsAction();
        Map<String, String> params = new HashMap<>();
//        params.put(CommonInfo.ACTION_GUID, "d4a0bf13-f6d9-4b93-9d6f-7a5a3b583aab");
        params.put(CommonInfo.INSTANCE_ID, "MR-20151118060322573gjp0iqbb1");
        params.put(CommonInfo.CONFIG, "");
        params.put(CommonInfo.WORKSPACE_NAME, "dtdream_metadata");
        OdpsHook.MockHolder.setMockNotify(true);
        odpsAction.sendActionConf(params, null);
    }
}
