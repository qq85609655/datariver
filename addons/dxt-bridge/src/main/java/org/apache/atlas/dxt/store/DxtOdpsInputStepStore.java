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
import org.apache.atlas.dxt.util.DxtConstant;
import org.apache.atlas.dxt.util.DxtDbType;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.typesystem.Referenceable;
import org.codehaus.jettison.json.JSONObject;

/**
 * OdpsInput解析json，创建referenceable，用于保存元数据。
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtOdpsInputStepStore extends DxtTableInputStepStore {
    @Override
    protected String getStepType() {
        return "ODPS_INPUT";
    }

    @Override
    public String getStepTraitName() {
        return DxtConstant.ODPSINPUT_TRAIT;
    }

    @Override
    protected void createContainerRef() throws Exception {
        JSONObject dbConnObj = getDbConnObj(dbId);
        JSONObject dbInfoObj = dbConnObj.getJSONObject("dbInfo");
        String projectName = dbInfoObj.getString("projectName");
        String connName = dbConnObj.getString("name");

        setQualifiedDbName(DxtStoreUtil.formatQualifiedName("odps", connName, "default", projectName));
        containerRef = DxtStoreUtil.createDxtRef(RelationalDataTypes.DATA_CONTAINER_SUPER_TYPE.getValue(),
            qualifiedDbName, DxtConstant.CONTAINER_TRAIT, getStepTraitName());

        containerRef.set("name", projectName);
        containerRef.set("id", dbConnObj.getString("id"));
        //todo: need to be modified
        containerRef.set("tag", "Just for hangzhou");
        containerRef.set("status", "AVAILABLE");
        containerRef.set("dbType", DxtDbType.ODPS.getValue());

        accInfoRef = createAccInfoRef(dbConnObj, dbInfoObj);
        containerRef.set("accessInfo", accInfoRef);
    }

    @Override
    protected Referenceable createAccInfoRef(JSONObject dbConnObj, JSONObject dbInfoObj) throws Exception {
        String connName = dbConnObj.getString("name");
        String qualifiedName = DxtStoreUtil.formatQualifiedName("odps", connName);

        Referenceable odpsAccInfoRef = DxtStoreUtil.createDxtRef(OdpsDataTypes.ODPS_ACCINFO.getValue(),
            qualifiedName, DxtConstant.ACCINFO_TRAIT);
        odpsAccInfoRef.set("name", connName);
        odpsAccInfoRef.set("tunnelURL", dbInfoObj.getString("tunnelURL"));
        odpsAccInfoRef.set("endpointURL", dbInfoObj.getString("endpointURL"));

        return odpsAccInfoRef;
    }
}
