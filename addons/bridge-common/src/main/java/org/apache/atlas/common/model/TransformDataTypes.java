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

package org.apache.atlas.common.model;

/**
 * ETL元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public enum TransformDataTypes {
    // Enums
    ETL_TASK_TYPE("ETLTaskType"),
    ETL_TASK_STATUS("ETLTaskStatus"),
    ETL_STEP_TYPE("ETLStepType"),
    ETL_INSTANCE_STATUS("ETLInstanceStatus"),

    // Structs

    // Classes
    ABSTRACT_PROCESS_SUPER_TYPE("AbstractProcess"),
    ETL_INSTANCE_SUPER_TYPE("ETLInstance"),
    ETL_TASK_SUPER_TYPE("ETLTask"),
    ETL_STEP_SEQUENCE_SUPER_TYPE("ETLStepSequence"),
    ETL_STEP_SUPER_TYPE("ETLStep"),;

    private final String value;

    TransformDataTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
