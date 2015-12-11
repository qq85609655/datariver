package org.apache.atlas.dxt.examples;

import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.dxt.store.DxtTransInstanceStore;
import org.apache.atlas.dxt.store.IDxtTransStepStore;
import org.codehaus.jettison.json.JSONObject;

import java.util.Map;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/5 14:09
 */
public class DxtDemoInstanceStore extends DxtTransInstanceStore {
    private boolean isFormIT = false;

    public DxtDemoInstanceStore(JSONObject transitionObj, Map<String, IDxtTransStepStore> stepStoreMap,
                                String instanceId, String workspaceName) {
        super(transitionObj, stepStoreMap, instanceId, workspaceName);
    }

    @Override
    protected JSONObject getHisLogObj(String transUid) throws Exception {
        String historicalLogs = loadResource("dxt_history.json");
        return new JSONObject(historicalLogs).getJSONObject("statistics");
    }

    private String loadResource(String fileName) throws Exception {
        String resourceName;
        if (isFormIT) {
            resourceName = this.getClass().getResource("/").getPath() + "/" + fileName;
        } else {
            resourceName = System.getProperty("atlas.home") + "/examples/" + fileName;
        }

        return FileUtil.loadResourceFile(resourceName);
    }

    public void setFormIT(boolean isFormIT) {
        this.isFormIT = isFormIT;
    }
}
