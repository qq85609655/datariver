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

package org.apache.atlas.model;

/**
 * 关系型元模型定义
 * @author 向日葵 0395
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/9 11:50
 */

public enum RelationalDataTypes {
    // Enums
    DATA_CONTAINER_TYPE("DataContainerType"),
    DATA_CONTAINER_STATUS("DataContainerStatus"),
    DATA_OBJECT_TYPE("DataObjectTypes"),
    DATA_OBJECT_PRIVILEGES("DataObjectPrivileges"),
    // Structs

    // Classes
    DB_ACCESS_SUPER_TYPE("DBAccess"),
    DATA_FIELD_SUPER_TYPE("DataField"),
    DATA_TABLE_SUPER_TYPE("DataTable"),
    DATA_CONTAINER_SUPER_TYPE("DataContainer"),
    DATA_ROWSET_SUPER_TYPE("DataRowSet");
    private final String value;

    RelationalDataTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
