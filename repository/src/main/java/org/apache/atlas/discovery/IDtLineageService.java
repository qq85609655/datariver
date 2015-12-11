/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.discovery;

import org.apache.atlas.AtlasException;

/**
 * Lineage service interface.
 */
public interface IDtLineageService {
    int TASK_LINEAGE = 0;
    int STEP_LINEAGE = 1;
    int WORKFLOW_LINEAGE = 2;
    int LINEAGE_MAX = 3;

    int SEQUENCE_FOR_STEP = 0;
    int SEQUENCE_FOR_ACTION = 1;
    int SEQUENCE_FOR_TASK = 2;
    int SEQUENCE_MAX = 3;

    /**
     * Return the lineage outputs graph for the given tableName.
     *
     * @param tableName   qualified tableName
     * @param lineageType for "TASK" or "STEP"
     * @return Outputs Graph as JSON
     */
    String getOutputsGraph(String tableName, int lineageType) throws AtlasException;

    /**
     * Return the lineage inputs graph for the given tableName.
     *
     * @param tableName   qualified tableName
     * @param lineageType for "TASK" or "STEP"
     * @return Inputs Graph as JSON
     */
    String getInputsGraph(String tableName, int lineageType) throws AtlasException;

    /**
     * Return the lineage outputs graph for the given dbName.
     *
     * @param dbName      qualified database name
     * @param lineageType for "TASK" or "STEP"
     * @return Outputs Graph as JSON
     */
    String getOutputDbsGraph(String dbName, int lineageType) throws AtlasException;

    /**
     * Return the lineage inputs graph for the given dbName.
     *
     * @param dbName      qualified database name
     * @param lineageType for "TASK" or "STEP"
     * @return Inputs Graph as JSON
     */
    String getInputDbsGraph(String dbName, int lineageType) throws AtlasException;

    /**
     * Return the schema for the given tableName.
     *
     * @param tableName qualified tableName
     * @return Schema as JSON
     */
    String getSchema(String tableName) throws AtlasException;

    /**
     * Return the lineage preceeding graph for the given stepName.
     *
     * @param entityName entityName
     * @param sequenceType search for step, action or task
     * @return Preceeding Graph as JSON
     */
    String getPreceedingGraph(String entityName, int sequenceType) throws AtlasException;

    /**
     * Return the lineage succeeding graph for the given stepName.
     *
     * @param entityName entityName
     * @param sequenceType search for step, action or task
     * @return Preceeding Graph as JSON
     */
    String getSucceedingGraph(String entityName, int sequenceType) throws AtlasException;

    /**
     * Return the lineage source graph for the given fieldName.
     *
     * @param fieldName fieldName
     * @param lineageType for "TASK" or "STEP"
     * @return Outputs Graph as JSON
     */
    String getSourceGraph(String fieldName, int lineageType) throws AtlasException;

    /**
     * Return the lineage target graph for the given fieldName.
     *
     * @param fieldName fieldName
     * @param lineageType for "TASK" or "STEP"
     * @return Outputs Graph as JSON
     */
    String getTargetGraph(String fieldName, int lineageType) throws AtlasException;
}
