package org.apache.atlas.odps.bridge;

import org.apache.atlas.common.bridge.CommonMetaStoreBridge;

/**
 * odps测试入口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class testOdps {

    public static void main(String[] args) throws Exception {
        try {
            CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
            commonMetaStoreBridge.registerCommonDataModel();
        } catch (Exception e) {
            //ignore,maybe has been registered
        }
        OdpsMetaStoreBridge bridge = new OdpsMetaStoreBridge();
        bridge.registerOdpsDataModel();
    }
}
