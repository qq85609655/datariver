package org.apache.atlas.odps.bridge;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.bridge.CommonMetaStoreBridge;
import org.apache.atlas.odps.hook.OdpsHook;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.model.OdpsMetaModelGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * odps元模型生成器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class OdpsMetaStoreBridge {

    private static final Logger LOG = LoggerFactory.getLogger(OdpsMetaStoreBridge.class);

    private final AtlasClient atlasClient;

    public OdpsMetaStoreBridge() throws Exception {
        atlasClient = new AtlasClient(OdpsHook.DEFAULT_DGI_URL);
    }

    public AtlasClient getAtlasClient() {
        return atlasClient;
    }

    public synchronized void registerOdpsDataModel() throws Exception {
        OdpsMetaModelGenerator dataModelGenerator = new OdpsMetaModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(OdpsDataTypes.ODPS_ACCINFO.getValue());
            System.out.println("Odps data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            System.out.println("Registering odps data model");
            dataModelGenerator.createDataModel();
            dgiClient.createType(dataModelGenerator.getDataModelAsJSON());
        }
    }

    public static void main(String[] argv) throws Exception {
        CommonMetaStoreBridge commonMetaStoreBridge = new CommonMetaStoreBridge();
        commonMetaStoreBridge.registerCommonDataModel();

        OdpsMetaStoreBridge odpsMetaStoreBridge = new OdpsMetaStoreBridge();
        odpsMetaStoreBridge.registerOdpsDataModel();
    }
}
