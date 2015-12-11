package org.apache.atlas.dxt.hook;

import com.dtdream.dthink.dtalent.activemq.MessageEnum;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.common.util.CommonInfo;
import org.apache.atlas.common.util.LineageHandler;
import org.apache.atlas.dxt.client.DxtClientUtil;
import org.apache.atlas.dxt.model.DxtDataModelGenerator;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.store.DxtDictionaryStore;
import org.apache.atlas.dxt.store.DxtStepStoreFactory;
import org.apache.atlas.dxt.store.DxtTransInstanceStore;
import org.apache.atlas.dxt.store.IDxtTransStepStore;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/3 9:43
 */
public class DxtAction implements CommonInfo {
    private static final Logger LOG = LoggerFactory.getLogger(DxtAction.class);
    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";

    private final AtlasClient atlasClient;

    public DxtAction() throws Exception {
        atlasClient = new AtlasClient(DEFAULT_DGI_URL);
    }

    public AtlasClient getAtlasClient() {
        return atlasClient;
    }

    /**
     * 导入DXT相关的元模型.
     *
     * @throws Exception 异常
     */
    public synchronized void registerDxtDataModel() throws Exception {
        DxtDataModelGenerator dataModelGenerator = new DxtDataModelGenerator();
        AtlasClient dgiClient = getAtlasClient();

        try {
            dgiClient.getType(DxtDataTypes.GENERAL_ACCINFO.getValue());
            System.out.println("Dxt data model is already registered!");
        } catch (AtlasServiceException ase) {
            //Expected in case types do not exist
            System.out.println("Registering Dxt data model");
            dgiClient.createType(dataModelGenerator.getModelAsJson());
        }
    }

    @Override
    public void sendActionConf(Map<String, String> paramMap, LineageHandler lineageHandler) {
        try {
            registerDxtDataModel();

            String sourceType = paramMap.get(CommonInfo.SOURCE_TYPE);
            if (MessageEnum.SystemName.WORKFLOW.name().equalsIgnoreCase(sourceType)) {
                createEntities(paramMap.get(CommonInfo.ACTION_GUID),
                    paramMap.get(CommonInfo.INSTANCE_ID),
                    paramMap.get(CommonInfo.CONFIG_ID),
                    paramMap.get(CommonInfo.CONFIG),
                    paramMap.get(CommonInfo.WORKSPACE_NAME),
                    lineageHandler);
            } else if (MessageEnum.SystemName.DATA_STUDIO.name().equalsIgnoreCase(sourceType)) {
                createEntities(paramMap.get(CommonInfo.CONFIG), lineageHandler);
            } else {
                LOG.error("Unknown source type: " + sourceType);
            }
        } catch (Exception e) {
            LOG.error("Failed to sendActionConf: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建trans相关的元数据.
     *
     * @param actionGuid     action的元数据的id
     * @param instanceId     trans每次执行的id
     * @param configId       trans的配置id
     * @param dxtConfig      dxt配置信息，JSONObject格式
     * @param workspaceName  名字空间
     * @param lineageHandler dxt action上一级的血缘处理类
     * @throws Exception 异常
     */
    private void createEntities(String actionGuid, String instanceId, String configId, JSONObject dxtConfig,
                                String workspaceName, LineageHandler lineageHandler) throws Exception {
        JSONObject transitionObj = dxtConfig.getJSONObject("transition");
        transitionObj.put("id", configId);
        JSONObject stepObjs = transitionObj.getJSONObject("steps");

        List<Referenceable> entities = new ArrayList<>(); //需要发送给atlas保存的元数据信息
        Map<String, IDxtTransStepStore> stepStoreMap = new HashMap<>();

        Iterator keys = stepObjs.keys();

        LOG.info("Importing Dxt meta data.");

        //创建TransStep
        while (keys.hasNext()) {
            String stepType = (String) keys.next();
            JSONArray stepArray = stepObjs.getJSONArray(stepType);

            for (int i = 0; i < stepArray.length(); i++) {
                JSONObject oneStepObj = stepArray.getJSONObject(i);
                String stepName = oneStepObj.getString("name");

                //每种step类型的构造方式不同，需要由各自的实现类来完成构造动作。
                IDxtTransStepStore stepInfoStore = DxtStepStoreFactory.getStepStore(stepType);
                //必须先初始化
                stepInfoStore.initialize(transitionObj, oneStepObj, workspaceName, instanceId);

                stepStoreMap.put(stepName, stepInfoStore);
                stepInfoStore.createStepRef();
            }
        }

        //增加TransStep的血缘关系，只有在workStream中出现的step才有血缘关系
        String workStream = transitionObj.getString("workStream");
        if ((null == workStream) || "".equals(workStream)) {
            throw new Exception("Failed to parse workStream.");
        }

        String[] stepSequence = workStream.split("->");
        for (String stepName : stepSequence) {
            IDxtTransStepStore stepStore = stepStoreMap.get(stepName);
            if (null == stepStore) {
                throw new Exception("Step(" + stepName + ") in workstream does not exist.");
            }

            stepStore.createLineageRef(stepStoreMap);
        }

        for (Map.Entry<String, IDxtTransStepStore> storeEntry : stepStoreMap.entrySet()) {
            IDxtTransStepStore stepStore = storeEntry.getValue();

            entities.add(stepStore.getContainerRef());
            if (null != stepStore.getLineageRef()) {
                entities.add(stepStore.getLineageRef());
            }
            entities.add(stepStore.getStepRef());
        }

        //创建TransInstance
        DxtTransInstanceStore instanceStore = new DxtTransInstanceStore(transitionObj, stepStoreMap, instanceId,
            workspaceName);
        instanceStore.createTransInstanceRef();

        entities.add(instanceStore.getTransInstanceRef());

        JSONArray guids = createEntities(entities);

        //更新WORKFLOW_ACTION中的instance信息
        if (null != actionGuid) {
            String transInstanceId = guids.getString(guids.length() - 1);
            getAtlasClient().updateEntityAttribute(actionGuid, "etlInstance", transInstanceId);
        }

        //刷新每个step的container的tables信息
        new DxtDictionaryStore(stepStoreMap, getAtlasClient()).refreshContainerTables();

        //更新action上一级的血缘关系
        if (null != lineageHandler) {
            lineageHandler.addToLineage(instanceStore.getInputTables(), instanceStore.getOutputTables(),
                instanceStore.getInputDbs(), instanceStore.getOutputDbs());
        }
    }

    /**
     * 创建DXT trans相关的元数据，来源是DataStudio.
     *
     * @param config DataStudio传过来的配置
     * @param lineageHandler dxt action上一级的血缘处理类
     * @throws Exception 异常
     */
    public void createEntities(String config, LineageHandler lineageHandler) throws Exception {
        JSONObject jsonObject = new JSONObject(config);
        String workspaceName = jsonObject.getString("workspaceName");

        JSONObject dataObject = jsonObject.getJSONObject("data");
        String instanceId = dataObject.getString("instanceId");
        String configId = dataObject.getString("configId");

        Map transConfig = new DxtClientUtil(workspaceName).getTrans(configId);

        createEntities(null, instanceId, configId, new JSONObject(transConfig), workspaceName, lineageHandler);
    }

    /**
     * 创建trans相关的元数据，来源是workflow.
     *
     * @param actionGuid    action的元数据的id
     * @param instanceId    trans每次执行的id
     * @param configId      trans的配置id
     * @param dxtJson       dxt配置信息，json格式
     * @param workspaceName 名字空间
     * @param lineageHandler dxt action上一级的血缘处理类
     * @throws Exception 异常
     */
    public void createEntities(String actionGuid, String instanceId, String configId, String dxtJson,
                               String workspaceName, LineageHandler lineageHandler) throws Exception {
        createEntities(actionGuid, instanceId, configId, new JSONObject(dxtJson), workspaceName, lineageHandler);
    }

    private JSONArray createEntities(List<Referenceable> entities) throws Exception {
        JSONArray entitiesArray = new JSONArray();
        for (Referenceable entity : entities) {
            String entityJson = InstanceSerialization.toJson(entity, true);
            entitiesArray.put(entityJson);
        }

        return getAtlasClient().createEntity(entitiesArray);
    }
}
