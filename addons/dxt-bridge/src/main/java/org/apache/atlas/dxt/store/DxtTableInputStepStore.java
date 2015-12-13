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

import org.apache.atlas.model.RelationalDataTypes;
import org.apache.atlas.dxt.client.DxtClientUtil;
import org.apache.atlas.dxt.model.DxtDataTypes;
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtDbType;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.typesystem.Referenceable;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;


/**
 * TableInput解析json，创建referenceable，用于保存元数据。
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtTableInputStepStore extends DxtStepStoreBase {
    @Override
    protected String getStepType() {
        return "TABLE_INPUT";
    }

    @Override
    public String getStepTraitName() {
        return DxtConstant.TABLEINPUT_TRAIT;
    }

    protected JSONObject getDbConnObj(String dbConnId) throws Exception {
        return new JSONObject(new DxtClientUtil(workspaceName).getDbConnInfo(dbConnId));
    }

    @Override
    protected void createContainerRef() throws Exception {
        JSONObject dbConnObj = getDbConnObj(dbId);
        JSONObject dbInfoObj = dbConnObj.getJSONObject("dbInfo");
        String dbType = dbConnObj.getString("dbType").toUpperCase();
        String connName = dbConnObj.getString("name");
        String dbName = dbInfoObj.getString("dbName");

        setQualifiedDbName(DxtStoreUtil.formatQualifiedName(dbType.toLowerCase(), connName, "default", dbName));
        containerRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
            qualifiedDbName, DxtConstant.CONTAINER_TRAIT, getStepTraitName());

        containerRef.set("name", dbName);
        containerRef.set("id", dbConnObj.getString("id"));
        //todo: need to be modified
        containerRef.set("tag", "Just for hangzhou");
        containerRef.set("status", "AVAILABLE");
        containerRef.set("dbType", DxtDbType.findByName(dbType).getValue());

        accInfoRef = createAccInfoRef(dbConnObj, dbInfoObj);
        containerRef.set("accessInfo", accInfoRef);
    }

    @Override
    protected void setMappingFields() throws Exception {
        mappingFields = new ArrayList<>();
        String[] tmpFields = stepObj.getString("column").split(",");
        for (String field : tmpFields) {
            mappingFields.add(field.trim());
        }
    }

    @Override
    protected void setDbId() throws Exception {
        dbId = stepObj.getString("dbID");
    }

    @Override
    protected void setTableName() throws Exception {
        tableName = stepObj.getString("tableName");
    }

    @Override
    protected Referenceable createAccInfoRef(JSONObject dbConnObj, JSONObject dbInfoObj) throws Exception {
        String typeName = dbConnObj.getString("dbType").toLowerCase();
        String connName = dbConnObj.getString("name");
        String qualifiedName = DxtStoreUtil.formatQualifiedName(typeName, connName);

        Referenceable generalAccInfoRef = DxtStoreUtil.createDxtRef(DxtDataTypes.GENERAL_ACCINFO.getValue(),
            qualifiedName, DxtConstant.ACCINFO_TRAIT);
        generalAccInfoRef.set("name", connName);
        generalAccInfoRef.set("host", dbInfoObj.getString("host"));
        generalAccInfoRef.set("port", dbInfoObj.getInt("port"));

        return generalAccInfoRef;
    }
}
