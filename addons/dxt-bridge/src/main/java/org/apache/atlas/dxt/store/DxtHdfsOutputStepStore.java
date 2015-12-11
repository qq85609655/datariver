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

import org.apache.atlas.dxt.util.DxtConstant;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;

/**
 * HdfsOutput解析json，创建referenceable，用于保存元数据。
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtHdfsOutputStepStore extends DxtHdfsInputStepStore {
    @Override
    protected String getStepType() {
        return "HDFS_OUTPUT";
    }

    @Override
    public String getStepTraitName() {
        return DxtConstant.HDFSOUTPUT_TRAIT;
    }

    @Override
    protected void setMappingFields() throws Exception {
        mappingFields = new ArrayList<>();
        JSONArray fieldObjArray = stepObj.getJSONArray("srcColumns");

        for (int i = 0; i < fieldObjArray.length(); i++) {
            JSONObject fieldObj = fieldObjArray.getJSONObject(i);
            mappingFields.add(fieldObj.getString("name"));
        }
    }
}
