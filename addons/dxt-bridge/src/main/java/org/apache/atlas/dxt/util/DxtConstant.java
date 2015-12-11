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

package org.apache.atlas.dxt.util;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/10 16:39
 */
public class DxtConstant {
    public static final String DXT_TRAIT = "DXT_";
    public static final String TABLE_TRAIT = DXT_TRAIT + "table";
    public static final String CONTAINER_TRAIT = DXT_TRAIT + "container";
    public static final String FIELD_TRAIT = DXT_TRAIT + "field";
    public static final String STEP_TRAIT = DXT_TRAIT + "step";
    public static final String TRANS_TRAIT = DXT_TRAIT + "trans";
    public static final String TASK_TRAIT = DXT_TRAIT + "task";
    public static final String ACCINFO_TRAIT = DXT_TRAIT + "accinfo";

    public static final String STEP_PREFIX = "STEP_";
    public static final String TABLEINPUT_TRAIT = STEP_PREFIX + "tableInput";
    public static final String TABLEOUTPUT_TRAIT = STEP_PREFIX + "tableOutput";
    public static final String ODPSINPUT_TRAIT = STEP_PREFIX + "odpsInput";
    public static final String ODPSOUTPUT_TRAIT = STEP_PREFIX + "odpsOutput";
    public static final String HDFSINPUT_TRAIT = STEP_PREFIX + "hdfsInput";
    public static final String HDFSOUTPUT_TRAIT = STEP_PREFIX + "hdfsOutput";
}
