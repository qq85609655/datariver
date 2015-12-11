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

package org.apache.atlas.workFlow.model;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public enum WorkFlowActionType {
    ODPSMR,
    ODPSSQL,
    DATABRIDGE,
    SUBFLOW,
    CTL_START,
    CTL_FORK,
    CTL_JOIN,
    CTL_KILL,
    CTL_END,
    SWITCH,
    SSH,
    EMAIL,
    MR,
    SQOOP,
    DISTCP,
    FS,
    HIVE,
    PIG,
    SHELL,
    START,
    END;

    public static WorkFlowActionType exchangeToEnum(String actionType) {
        if (actionType == null || "".equals(actionType.trim())) {
            return null;
        }
        for (WorkFlowActionType type : WorkFlowActionType.values()) {
            if (type.name().equals(actionType.toUpperCase())
                || (actionType.startsWith(":") && actionType.endsWith(":") && (actionType.length() > 2)
                && type.name().equals(actionType.substring(1, actionType.length() - 1)))) {
                return type;
            }
        }
        return null;
    }

}
