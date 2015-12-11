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

package org.apache.atlas.discovery;

import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.utils.ParamChecker;
import org.apache.atlas.discovery.graph.DefaultGraphPersistenceStrategy;
import org.apache.atlas.discovery.graph.GraphBackedDiscoveryService;
import org.apache.atlas.query.DtLineageQuery;
import org.apache.atlas.query.DtWhereUsedQuery;
import org.apache.atlas.query.GremlinQueryResult;
import org.apache.atlas.typesystem.exception.EntityNotFoundException;
import org.apache.atlas.repository.MetadataRepository;
import org.apache.atlas.repository.graph.GraphProvider;
import org.apache.atlas.typesystem.persistence.ReferenceableInstance;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Some;
import scala.collection.immutable.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * DT implementation of Lineage service interface.
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/7 11:04
 */
@Singleton
public class DtLineageService implements IDtLineageService {
    private static final String[] lineageProcessNames = new String[LINEAGE_MAX];

    private static final Logger LOG = LoggerFactory.getLogger(DtLineageService.class);
    private static final Configuration propertiesConf;

    private static final Option<List<String>> SELECT_ATTRIBUTES =
        Some.<List<String>>apply(List.<String>fromArray(new String[]{"qualifiedName"}));
    private static final String DATATABLE_TYPE_NAME;
    private static final String DATACONTAINER_TYPE_NAME;
    private static final String LINEAGE_TASK_PROCESS_TYPE_NAME;
    private static final String LINEAGE_STEP_PROCESS_TYPE_NAME;
    private static final String LINEAGE_WORKFLOW_PROCESS_TYPE_NAME;
    private static final String PROCESS_INPUT_ATTRIBUTE_NAME;
    private static final String PROCESS_OUTPUT_ATTRIBUTE_NAME;
    private static final String PROCESS_INPUTDB_ATTRIBUTE_NAME;
    private static final String PROCESS_OUTPUTDB_ATTRIBUTE_NAME;
    private static final String DATATABLE_EXISTS_QUERY;
    private static final String DATACONTAINER_EXISTS_QUERY;
    public static final String DXT_TABLE_SCHEMA_QUERY_PREFIX = "atlas.lineage.table.schema.query.";

    private static final String STEP_TYPE_NAME;
    private static final String ACTION_TYPE_NAME;
    private static final String TASK_TYPE_NAME;
    private static final String ABSTRACT_PROCESS_TYPE_NAME;
    private static final String SEQUENCE_TYPE_NAME;
    private static final String SEQUENCE_PRECEDING_ATTRIBUTE_NAME;
    private static final String SEQUENCE_SUCCEEDING_ATTRIBUTE_NAME;
    private static final String STEP_EXISTS_QUERY;

    private static final String DATAFIELD_TYPE_NAME;
    private static final String LINEAGE_TASK_MAP_TYPE_NAME;
    private static final String LINEAGE_STEP_MAP_TYPE_NAME;
    private static final String MAP_SOURCE_ATTRIBUTE_NAME;
    private static final String MAP_TARGET_ATTRIBUTE_NAME;
    private static final String DATAFIELD_EXISTS_QUERY;
    private static final String ACTION_EXISTS_QUERY;
    private static final String TASK_EXISTS_QUERY;

    static {
        try {
            propertiesConf = ApplicationProperties.get();
            DATATABLE_TYPE_NAME = propertiesConf.getString("atlas.lineage.table.type.name", "DataTable");
            DATACONTAINER_TYPE_NAME = propertiesConf.getString("atlas.lineage.db.type.name", "DataContainer");
            LINEAGE_TASK_PROCESS_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.task.process.type.name", "LineageTaskProcessInfo");
            LINEAGE_STEP_PROCESS_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.step.process.type.name", "LineageStepProcessInfo");
            LINEAGE_WORKFLOW_PROCESS_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.workflow.process.type.name", "LineageWorkflowProcessInfo");
            PROCESS_INPUT_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.process.inputs.name", "inputs");
            PROCESS_OUTPUT_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.process.outputs.name", "outputs");
            PROCESS_INPUTDB_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.process.inputdbs.name", "inputDbs");
            PROCESS_OUTPUTDB_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.process.outputdbs.name", "outputDbs");

            DATATABLE_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.table.exists.query",
                "from " + DATATABLE_TYPE_NAME + " where qualifiedName=\"%s\"");
            DATACONTAINER_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.container.exists.query",
                "from " + DATACONTAINER_TYPE_NAME + " where qualifiedName=\"%s\"");

            STEP_TYPE_NAME = propertiesConf.getString("atlas.lineage.step.type.name", "ETLStep");
            ACTION_TYPE_NAME = propertiesConf.getString("atlas.lineage.action.type.name", "WorkflowAction");
            TASK_TYPE_NAME = propertiesConf.getString("atlas.lineage.task.type.name", "ETLTask");
            ABSTRACT_PROCESS_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.abstractProcess.type.name", "AbstractProcess");
            SEQUENCE_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.sequence.type.name", "ETLStepSequence");
            SEQUENCE_PRECEDING_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.sequence.preceding.name", "preceding");
            SEQUENCE_SUCCEEDING_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.sequence.succeeding.name", "succeeding");

            STEP_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.step.exists.query",
                "from " + STEP_TYPE_NAME + " where qualifiedName=\"%s\"");
            ACTION_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.step.exists.query",
                "from " + ACTION_TYPE_NAME + " where qualifiedName=\"%s\"");
            TASK_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.step.exists.query",
                "from " + TASK_TYPE_NAME + " where qualifiedName=\"%s\"");

            DATAFIELD_TYPE_NAME = propertiesConf.getString("atlas.lineage.field.type.name", "DataField");
            LINEAGE_TASK_MAP_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.task.map.type.name", "LineageTaskFieldMap");
            LINEAGE_STEP_MAP_TYPE_NAME = propertiesConf
                .getString("atlas.lineage.step.map.type.name", "LineageStepFieldMap");
            MAP_SOURCE_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.map.source.name", "sourceFields");
            MAP_TARGET_ATTRIBUTE_NAME = propertiesConf
                .getString("atlas.lineage.map.target.name", "targetField");

            DATAFIELD_EXISTS_QUERY = propertiesConf.getString("atlas.lineage.field.exists.query",
                "from " + DATAFIELD_TYPE_NAME + " where qualifiedName=\"%s\"");

            lineageProcessNames[TASK_LINEAGE] = LINEAGE_TASK_PROCESS_TYPE_NAME;
            lineageProcessNames[STEP_LINEAGE] = LINEAGE_STEP_PROCESS_TYPE_NAME;
            lineageProcessNames[WORKFLOW_LINEAGE] = LINEAGE_WORKFLOW_PROCESS_TYPE_NAME;
        } catch (AtlasException e) {
            throw new RuntimeException(e);
        }
    }

    private final TitanGraph titanGraph;
    private final DefaultGraphPersistenceStrategy graphPersistenceStrategy;
    private final GraphBackedDiscoveryService discoveryService;

    @Inject
    DtLineageService(GraphProvider<TitanGraph> graphProvider, MetadataRepository metadataRepository,
                     GraphBackedDiscoveryService discoveryService) throws DiscoveryException {
        this.titanGraph = graphProvider.get();
        this.graphPersistenceStrategy = new DefaultGraphPersistenceStrategy(metadataRepository);
        this.discoveryService = discoveryService;
    }

    /**
     * Return the lineage outputs graph for the given tableName.
     *
     * @param tableName   qualified table name
     * @param lineageType for "task" or "step"
     * @return Outputs Graph as JSON
     */
    @Override
    public String getOutputsGraph(String tableName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage outputs graph for tableName={}", tableName);
        ParamChecker.notEmpty(tableName, "table name cannot be null");
        validateTableExists(tableName);

        String ctasTypeName = lineageProcessNames[lineageType];
        DtWhereUsedQuery outputsQuery =
                new DtWhereUsedQuery(DATATABLE_TYPE_NAME, tableName, ctasTypeName,
                                     PROCESS_INPUT_ATTRIBUTE_NAME, PROCESS_OUTPUT_ATTRIBUTE_NAME,
                                     Option.empty(),
                                     SELECT_ATTRIBUTES, true, graphPersistenceStrategy, titanGraph);
        return outputsQuery.graph().toInstanceJson();
    }

    /**
     * Return the lineage inputs graph for the given tableName.
     *
     * @param tableName   qualified table name
     * @param lineageType for "task" or "step"
     * @return inputs Graph as JSON
     */
    @Override
    public String getInputsGraph(String tableName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage inputs graph for tableName={}", tableName);
        ParamChecker.notEmpty(tableName, "table name cannot be null");
        validateTableExists(tableName);

        String ctasTypeName = lineageProcessNames[lineageType];
        DtLineageQuery inputsQuery = new DtLineageQuery(DATATABLE_TYPE_NAME, tableName, ctasTypeName,
                                                        PROCESS_INPUT_ATTRIBUTE_NAME,
                                                        PROCESS_OUTPUT_ATTRIBUTE_NAME, Option.empty(),
                                                        SELECT_ATTRIBUTES, true, graphPersistenceStrategy,
                                                        titanGraph);
        return inputsQuery.graph().toInstanceJson();
    }

    /**
     * Return the lineage outputs graph for the given dbName.
     *
     * @param dbName      qualified database name
     * @param lineageType for "task" or "step"
     * @return Outputs Graph as JSON
     */
    @Override
    public String getOutputDbsGraph(String dbName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage outputs graph for dbName={}", dbName);
        ParamChecker.notEmpty(dbName, "table name cannot be null");
        validateContainerExists(dbName);

        String ctasTypeName = lineageProcessNames[lineageType];
        DtWhereUsedQuery outputsQuery =
                new DtWhereUsedQuery(DATACONTAINER_TYPE_NAME, dbName, ctasTypeName,
                                     PROCESS_INPUTDB_ATTRIBUTE_NAME, PROCESS_OUTPUTDB_ATTRIBUTE_NAME,
                                     Option.empty(),
                                     SELECT_ATTRIBUTES, true, graphPersistenceStrategy, titanGraph);
        return outputsQuery.graph().toInstanceJson();
    }

    /**
     * Return the lineage inputs graph for the given dbName.
     *
     * @param dbName      qualified database name
     * @param lineageType for "task" or "step"
     * @return inputs Graph as JSON
     */
    @Override
    public String getInputDbsGraph(String dbName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage inputs graph for dbName={}", dbName);
        ParamChecker.notEmpty(dbName, "table name cannot be null");
        validateContainerExists(dbName);

        String ctasTypeName = lineageProcessNames[lineageType];
        DtLineageQuery inputsQuery = new DtLineageQuery(DATACONTAINER_TYPE_NAME, dbName, ctasTypeName,
                                                        PROCESS_INPUTDB_ATTRIBUTE_NAME,
                                                        PROCESS_OUTPUTDB_ATTRIBUTE_NAME, Option.empty(),
                                                        SELECT_ATTRIBUTES, true, graphPersistenceStrategy,
                                                        titanGraph);
        return inputsQuery.graph().toInstanceJson();
    }

    /**
     * Return the schema for the given tableName.
     *
     * @param tableName qualified table name
     * @return Schema as JSON
     */
    @Override
    public String getSchema(String tableName) throws AtlasException {
        LOG.info("Fetching schema for tableName={}", tableName);
        ParamChecker.notEmpty(tableName, "table name cannot be null");
        String typeName = validateTableExists(tableName);

        final String schemaQuery =
                String.format(propertiesConf.getString(DXT_TABLE_SCHEMA_QUERY_PREFIX + typeName), tableName);
        return discoveryService.searchByDSL(schemaQuery);
    }

    /**
     * Validate if indeed this is a table type and exists.
     *
     * @param tableName qualified table name
     * @return Type name
     */
    private String validateTableExists(String tableName) throws AtlasException {
        return validateExists(tableName, String.format(DATATABLE_EXISTS_QUERY, tableName));
    }

    /**
     * Validate if indeed this is a db type and exists.
     *
     * @param dbName qualified db name
     * @return Type name
     */
    private String validateContainerExists(String dbName) throws AtlasException {
        return validateExists(dbName, String.format(DATACONTAINER_EXISTS_QUERY, dbName));
    }

    private void validateEntityExist(int sequenceType, String entityName) throws AtlasException {
        if (SEQUENCE_FOR_STEP == sequenceType) {
            validateStepExists(entityName);
        } else if (SEQUENCE_FOR_ACTION == sequenceType) {
            validateActionExists(entityName);
        } else {
            validateTaskExists(entityName);
        }
    }

    /**
     * Return the lineage preceding graph for the given name.
     *
     * @param entityName   qualified entity name
     * @param sequenceType search for step, action or task
     * @return Preceding Graph as JSON
     */
    @Override
    public String getPreceedingGraph(String entityName, int sequenceType) throws AtlasException {
        LOG.info("Fetching lineage preceeding graph for {}", entityName);
        ParamChecker.notEmpty(entityName, "name cannot be null");

        validateEntityExist(sequenceType, entityName);
        DtLineageQuery precedingQuery = new DtLineageQuery(ABSTRACT_PROCESS_TYPE_NAME, entityName,
                                                           SEQUENCE_TYPE_NAME,
                                                           SEQUENCE_PRECEDING_ATTRIBUTE_NAME,
                                                           SEQUENCE_SUCCEEDING_ATTRIBUTE_NAME, Option.empty(),
                                                           SELECT_ATTRIBUTES, true, graphPersistenceStrategy,
                                                           titanGraph);
        return precedingQuery.graph().toInstanceJson();
    }

    /**
     * Return the lineage succeeding graph for the given name.
     *
     * @param entityName   qualified step name
     * @param sequenceType search for step, action or task
     * @return Preceding Graph as JSON
     */
    @Override
    public String getSucceedingGraph(String entityName, int sequenceType) throws AtlasException {
        LOG.info("Fetching lineage preceeding graph for {}", entityName);
        ParamChecker.notEmpty(entityName, "name cannot be null");

        validateEntityExist(sequenceType, entityName);
        DtWhereUsedQuery succeedingQuery =
                new DtWhereUsedQuery(ABSTRACT_PROCESS_TYPE_NAME, entityName, SEQUENCE_TYPE_NAME,
                                     SEQUENCE_PRECEDING_ATTRIBUTE_NAME, SEQUENCE_SUCCEEDING_ATTRIBUTE_NAME,
                                     Option.empty(),
                                     SELECT_ATTRIBUTES, true, graphPersistenceStrategy, titanGraph);
        return succeedingQuery.graph().toInstanceJson();
    }

    /**
     * Validate if indeed this is a action type and exists.
     *
     * @param actionName qualified step name
     * @return Type name
     */
    private String validateActionExists(String actionName) throws AtlasException {
        return validateExists(actionName, String.format(ACTION_EXISTS_QUERY, actionName));
    }

    /**
     * Validate if indeed this is a task type and exists.
     *
     * @param taskName qualified step name
     * @return Type name
     */
    private String validateTaskExists(String taskName) throws AtlasException {
        return validateExists(taskName, String.format(TASK_EXISTS_QUERY, taskName));
    }

    /**
     * Validate if indeed this is a transStep type and exists.
     *
     * @param stepName qualified step name
     * @return Type name
     */
    private String validateStepExists(String stepName) throws AtlasException {
        return validateExists(stepName, String.format(STEP_EXISTS_QUERY, stepName));
    }

    /**
     * Return the lineage source graph for the given fieldName.
     *
     * @param fieldName   qualified field name
     * @param lineageType for "task" or "step"
     * @return Source Graph as JSON
     */
    @Override
    public String getSourceGraph(String fieldName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage source graph for fieldName={}", fieldName);
        ParamChecker.notEmpty(fieldName, "field name cannot be null");
        validateFieldExists(fieldName);

        String ctasTypeName = LINEAGE_TASK_MAP_TYPE_NAME;
        if (STEP_LINEAGE == lineageType) {
            ctasTypeName = LINEAGE_STEP_MAP_TYPE_NAME;
        }

        DtLineageQuery sourceQuery = new DtLineageQuery(DATAFIELD_TYPE_NAME, fieldName, ctasTypeName,
                                                        MAP_SOURCE_ATTRIBUTE_NAME,
                                                        MAP_TARGET_ATTRIBUTE_NAME, Option.empty(),
                                                        SELECT_ATTRIBUTES, true, graphPersistenceStrategy,
                                                        titanGraph);
        return sourceQuery.graph().toInstanceJson();
    }

    /**
     * Return the lineage target graph for the given fieldName.
     *
     * @param fieldName   qualified field name
     * @param lineageType for "task" or "step"
     * @return Target Graph as JSON
     */
    @Override
    public String getTargetGraph(String fieldName, int lineageType) throws AtlasException {
        LOG.info("Fetching lineage preceeding graph for fieldName={}", fieldName);
        ParamChecker.notEmpty(fieldName, "field name cannot be null");
        validateFieldExists(fieldName);

        String ctasTypeName = LINEAGE_TASK_MAP_TYPE_NAME;
        if (STEP_LINEAGE == lineageType) {
            ctasTypeName = LINEAGE_STEP_MAP_TYPE_NAME;
        }

        DtWhereUsedQuery targetQuery =
                new DtWhereUsedQuery(DATAFIELD_TYPE_NAME, fieldName, ctasTypeName,
                                     MAP_SOURCE_ATTRIBUTE_NAME, MAP_TARGET_ATTRIBUTE_NAME,
                                     Option.empty(),
                                     SELECT_ATTRIBUTES, true, graphPersistenceStrategy, titanGraph);
        return targetQuery.graph().toInstanceJson();
    }

    /**
     * Validate if indeed this is a field type and exists.
     *
     * @param fieldName qualified field name
     * @return Type name
     */
    private String validateFieldExists(String fieldName) throws AtlasException {
        return validateExists(fieldName, String.format(DATAFIELD_EXISTS_QUERY, fieldName));
    }

    /**
     * Validate if indeed this is an entity type and exists.
     *
     * @param entityName qualifiedName of an entity
     * @param existQuery query string
     * @return Type name
     */
    private String validateExists(String entityName, String existQuery) throws AtlasException {
        GremlinQueryResult queryResult = discoveryService.evaluate(existQuery);
        if (!(queryResult.rows().length() > 0)) {
            throw new EntityNotFoundException(entityName + " does not exist");
        }

        ReferenceableInstance referenceable = (ReferenceableInstance) queryResult.rows().apply(0);
        return referenceable.getTypeName();
    }
}
