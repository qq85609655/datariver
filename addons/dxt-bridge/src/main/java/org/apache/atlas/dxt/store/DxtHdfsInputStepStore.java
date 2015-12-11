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

package org.apache.atlas.dxt.store;

import org.apache.atlas.common.model.RelationalDataTypes;
import org.apache.atlas.dxt.client.DxtClientUtil;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtDbType;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.typesystem.Referenceable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * HdfsInput解析json，创建referenceable，用于保存元数据。
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtHdfsInputStepStore extends DxtTableInputStepStore {
    @Override
    protected String getStepType() {
        return "HDFS_INPUT";
    }

    @Override
    public String getStepTraitName() {
        return DxtConstant.HDFSINPUT_TRAIT;
    }

    @Override
    protected void createContainerRef() throws Exception {
        JSONObject dbConnObj = getDbConnObj(dbId);
        String connName = dbConnObj.getString("name");

        setQualifiedDbName(DxtStoreUtil.formatQualifiedName("hdfs", connName, "default"));
        containerRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
            qualifiedDbName, DxtConstant.CONTAINER_TRAIT, getStepTraitName());

        //HDFS的dbConn信息中dbName为空，需要用连接名称来作为container的name
        containerRef.set("name", connName);
        containerRef.set("id", dbConnObj.getString("id"));
        //todo: need to be modified
        containerRef.set("tag", "Just for hangzhou");
        containerRef.set("status", "AVAILABLE");
        containerRef.set("dbType", DxtDbType.HDFS.getValue());

        JSONObject dbInfoObj = dbConnObj.getJSONObject("dbInfo");
        accInfoRef = createAccInfoRef(dbConnObj, dbInfoObj);
        containerRef.set("accessInfo", accInfoRef);
    }

    private String getHdfsQueryBody() throws Exception {
        JSONObject obj = new JSONObject();

        obj.put("name", stepName);
        obj.put("fileName", stepObj.getString("fileName"));
        obj.put("splitFlag", stepObj.getString("splitFlag"));
        obj.put("enterFlag", stepObj.getString("enterFlag"));
        obj.put("headerNumber", stepObj.getString("headerNumber"));
        obj.put("tailerNumber", stepObj.getString("tailerNumber"));
        obj.put("hasEmptyRows", stepObj.getString("hasEmptyRows"));

        return obj.toString();
    }

    @Override
    public JSONArray getFieldObjArray(String tableName) throws Exception {
        String hdfsId = stepObj.getString("hdfsID");
        Map fieldsInfo = new DxtClientUtil(workspaceName).getHdfsFields(hdfsId, getHdfsQueryBody());
        JSONObject fieldsObj = new JSONObject(fieldsInfo);

        return fieldsObj.getJSONArray("fields");
    }

    @Override
    protected void setMappingFields() throws Exception {
        mappingFields = new ArrayList<>();
        JSONArray fieldObjArray = stepObj.getJSONArray("columns");

        for (int i = 0; i < fieldObjArray.length(); i++) {
            JSONObject fieldObj = fieldObjArray.getJSONObject(i);
            mappingFields.add(fieldObj.getString("name"));
        }
    }

    @Override
    protected void setDbId() throws Exception {
        dbId = stepObj.getString("hdfsID");
    }

    //hdfs中的file相当于普通数据库的table
    @Override
    protected void setTableName() throws Exception {
        dbId = stepObj.getString("fileName");
    }

    @Override
    protected Referenceable createAccInfoRef(JSONObject dbConnObj, JSONObject dbInfoObj) throws Exception {
        Referenceable generalAccInfoRef = DxtStoreUtil.createDxtRef(DxtDataTypes.GENERAL_ACCINFO.getValue(),
            qualifiedDbName, DxtConstant.ACCINFO_TRAIT);
        generalAccInfoRef.set("name", dbConnObj.getString("name"));
        generalAccInfoRef.set("host", dbInfoObj.getString("host"));
        generalAccInfoRef.set("port", dbInfoObj.getInt("port"));

        return generalAccInfoRef;
    }
}
