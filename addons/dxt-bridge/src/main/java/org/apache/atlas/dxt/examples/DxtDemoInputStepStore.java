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

package org.apache.atlas.dxt.examples;

import org.apache.atlas.common.util.FileUtil;
import org.apache.atlas.dxt.store.DxtTableInputStepStore;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * DemoInput解析json，创建referenceable，用于导入demo元数据。
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date
 */
public class DxtDemoInputStepStore extends DxtTableInputStepStore {
    private boolean isFormIT = false;

    @Override
    protected JSONObject getDbConnObj(String dbConnId) throws Exception {
        String dbInfo;
        if (dbConnId.equals("13548e24-5739-42b7-a227-e84d6ca1d96a")) {
            dbInfo = loadResource("dxt_conn1.json");
        } else {
            dbInfo = loadResource("dxt_conn2.json");
        }

        return new JSONObject(dbInfo);
    }

    @Override
    public JSONArray getFieldObjArray(String tableName) throws Exception {
        String fieldsInfo = loadResource("dxt_field.json");
        JSONObject fieldsObj = new JSONObject(fieldsInfo);

        return fieldsObj.getJSONArray("columns");
    }

    @Override
    public JSONArray getTableArray() throws Exception {
        String tablesInfo = loadResource("dxt_tables.json");
        JSONObject tablesObj = new JSONObject(tablesInfo);
        JSONArray tableArray = tablesObj.getJSONArray("tables");

        return tableArray;
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
