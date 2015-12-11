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

package org.apache.atlas.web.resources;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.utils.ParamChecker;
import org.apache.atlas.discovery.DiscoveryException;
import org.apache.atlas.discovery.IDtLineageService;
import org.apache.atlas.typesystem.exception.EntityNotFoundException;
import org.apache.atlas.web.util.Servlets;
import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Jersey Resource for Lineage.
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/7 11:04
 */
@Path("lineage")
@Singleton
@JsonRootType
@JsonName("lineageDt")
public class DtLineageResource {
    private static final int INPUT = 1;
    private static final int OUTPUT = 2;
    private static final int PRECEDING = 1;
    private static final int SUCCEEDING = 2;
    private static final int SOURCE = 1;
    private static final int TARGET = 2;

    private static final Logger LOG = LoggerFactory.getLogger(DtLineageResource.class);

    private final IDtLineageService dtLineageService;

    /**
     * Created by the Guice ServletModule and injected with the
     * configured LineageService.
     *
     * @param dtLineageService lineage service handle
     */
    @Inject
    public DtLineageResource(IDtLineageService dtLineageService) {
        this.dtLineageService = dtLineageService;
    }

    private Response tableGraph(String tableName, int lineageType, int direction) {
        try {
            ParamChecker.notEmpty(tableName, "table name cannot be null");
            final String jsonResult;
            if (INPUT == direction) {
                jsonResult = dtLineageService.getInputsGraph(tableName, lineageType);
            } else {
                jsonResult = dtLineageService.getOutputsGraph(tableName, lineageType);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("dataTableName", tableName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("DataTable entity not found for {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage inputs graph for table {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage inputs graph for table {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取TASK级别表的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/table/{tableName}/inputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/table/{tableName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskInputsGraph(@Context HttpServletRequest request, @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage inputs graph for tableName={} in task lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.TASK_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取TASK级别表的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/table/{tableName}/outputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/table/{tableName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskOutputsGraph(@Context HttpServletRequest request, @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage outputs graph for tableName={} in task lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.TASK_LINEAGE, OUTPUT);
    }

    /**
     * <p/>
     * 获取STEP级别表的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/table/{tableName}/inputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/table/{tableName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepInputsGraph(@Context HttpServletRequest request, @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage inputs graph for tableName={} in step lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.STEP_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取STEP级别表的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/table/{tableName}/outputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/table/{tableName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepOutputsGraph(@Context HttpServletRequest request, @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage outputs graph for tableName={} in step lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.STEP_LINEAGE, OUTPUT);
    }

    /**
     * <p/>
     * 获取WORKFLOW级别表的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/workflow/table/{tableName}/inputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/workflow/table/{tableName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response workflowInputsGraph(@Context HttpServletRequest request,
                                        @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage inputs graph for tableName={} in workflow lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.WORKFLOW_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取WORKFLOW级别表的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/workflow/table/{tableName}/outputs/graph<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/workflow/table/{tableName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response workflowOutputsGraph(@Context HttpServletRequest request,
                                         @PathParam("tableName") String tableName) {
        LOG.info("Fetching lineage outputs graph for tableName={} in workflow lineage.", tableName);
        return tableGraph(tableName, IDtLineageService.WORKFLOW_LINEAGE, OUTPUT);
    }

    private Response databaseGraph(String dbName, int lineageType, int direction) {
        try {
            ParamChecker.notEmpty(dbName, "db name cannot be null");

            final String jsonResult;
            if (INPUT == direction) {
                jsonResult = dtLineageService.getInputDbsGraph(dbName, lineageType);
            } else {
                jsonResult = dtLineageService.getOutputDbsGraph(dbName, lineageType);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("dbName", dbName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("DataDataContainer entity not found for {}", dbName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage inputs graph for db {}", dbName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage inputs graph for db {}", dbName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取TASK级别数据库的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/db/{dbName}/inputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/db/{dbName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskInputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage inputs graph for dbName={} in task lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.TASK_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取TASK级别数据库的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/db/{dbName}/outputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/db/{dbName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskOutputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage outputs graph for dbName={} in task lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.TASK_LINEAGE, OUTPUT);
    }

    /**
     * <p/>
     * 获取STEP级别数据库的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/db/{dbName}/inputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/db/{dbName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepInputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage inputs graph for dbName={} in step lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.STEP_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取STEP级别数据库的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/db/{dbName}/outputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/db/{dbName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepOutputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage outputs graph for dbName={}in step lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.STEP_LINEAGE, OUTPUT);
    }

    /**
     * <p/>
     * 获取WORKFLOW级别数据库的输入血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/workflow/db/{dbName}/inputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/workflow/db/{dbName}/inputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response workflowInputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage inputs graph for dbName={} in workflow lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.WORKFLOW_LINEAGE, INPUT);
    }

    /**
     * <p/>
     * 获取WORKFLOW级别数据库的输出血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/workflow/db/{dbName}/outputs/graph<br/>
     *
     * @param dbName 数据库的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/workflow/db/{dbName}/outputs/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response workflowOutputDbsGraph(@Context HttpServletRequest request, @PathParam("dbName") String dbName) {
        LOG.info("Fetching lineage outputs graph for dbName={} in workflow lineage.", dbName);
        return databaseGraph(dbName, IDtLineageService.WORKFLOW_LINEAGE, OUTPUT);
    }

    /**
     * <p/>
     * 获取表的schema信息.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/datatable/{tableName}/schema<br/>
     *
     * @param tableName 表的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("datatable/{tableName}/schema")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response schema(@Context HttpServletRequest request, @PathParam("tableName") String tableName) {
        LOG.info("Fetching schema for tableName={}", tableName);

        try {
            ParamChecker.notEmpty(tableName, "table name cannot be null");
            final String jsonResult = dtLineageService.getSchema(tableName);

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("tableName", tableName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("table entity not found for {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get schema for table {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get schema for table {}", tableName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private Response stepGraph(String stepName, int direction) {
        LOG.info("Fetching lineage preceding graph for stepName={}", stepName);

        try {
            ParamChecker.notEmpty(stepName, "step name cannot be null");
            final String jsonResult;
            if (PRECEDING == direction) {
                jsonResult = dtLineageService.getPreceedingGraph(stepName, IDtLineageService.SEQUENCE_FOR_STEP);
            } else {
                jsonResult = dtLineageService.getSucceedingGraph(stepName, IDtLineageService.SEQUENCE_FOR_STEP);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("stepName", stepName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("step entity not found for {}", stepName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage preceding graph for step {}", stepName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage preceding graph for step {}", stepName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取步骤的前置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/{stepName}/preceding/graph<br/>
     *
     * @param stepName 步骤的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("step/{stepName}/preceding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepPrecedingGraph(@Context HttpServletRequest request, @PathParam("stepName") String stepName) {
        return stepGraph(stepName, PRECEDING);
    }

    /**
     * <p/>
     * 获取步骤的后置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/{stepName}/succeeding/graph<br/>
     *
     * @param stepName 步骤的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("step/{stepName}/succeeding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response stepSucceedingGraph(@Context HttpServletRequest request, @PathParam("stepName") String stepName) {
        return stepGraph(stepName, SUCCEEDING);
    }

    private Response actionGraph(String actionName, int direction) {
        LOG.info("Fetching lineage preceding graph for actionName={}", actionName);

        try {
            ParamChecker.notEmpty(actionName, "action name cannot be null");
            final String jsonResult;
            if (PRECEDING == direction) {
                jsonResult = dtLineageService.getPreceedingGraph(actionName, IDtLineageService.SEQUENCE_FOR_ACTION);
            } else {
                jsonResult = dtLineageService.getSucceedingGraph(actionName, IDtLineageService.SEQUENCE_FOR_ACTION);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("actionName", actionName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("action entity not found for {}", actionName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage preceding graph for action {}", actionName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage preceding graph for action {}", actionName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取action的前置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/action/{actionName}/preceding/graph<br/>
     *
     * @param actionName action的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("action/{actionName}/preceding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response actionPrecedingGraph(@Context HttpServletRequest request,
                                         @PathParam("actionName") String actionName) {
        return actionGraph(actionName, PRECEDING);
    }

    /**
     * <p/>
     * 获取action的后置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/action/{actionName}/succeeding/graph<br/>
     *
     * @param actionName action的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("action/{actionName}/succeeding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response actionSucceedingGraph(@Context HttpServletRequest request,
                                          @PathParam("actionName") String actionName) {
        return actionGraph(actionName, SUCCEEDING);
    }

    private Response taskGraph(String taskName, int direction) {
        LOG.info("Fetching lineage preceding graph for taskName={}", taskName);

        try {
            ParamChecker.notEmpty(taskName, "task name cannot be null");
            final String jsonResult;
            if (PRECEDING == direction) {
                jsonResult = dtLineageService.getPreceedingGraph(taskName, IDtLineageService.SEQUENCE_FOR_TASK);
            } else {
                jsonResult = dtLineageService.getSucceedingGraph(taskName, IDtLineageService.SEQUENCE_FOR_TASK);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("taskName", taskName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("task entity not found for {}", taskName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage preceding graph for task {}", taskName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage preceding graph for task {}", taskName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取task的前置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/{taskName}/preceding/graph<br/>
     *
     * @param taskName task的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("task/{taskName}/preceding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskPrecedingGraph(@Context HttpServletRequest request, @PathParam("taskName") String taskName) {
        return taskGraph(taskName, PRECEDING);
    }

    /**
     * <p/>
     * 获取task的后置血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/{taskName}/succeeding/graph<br/>
     *
     * @param taskName task的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("task/{taskName}/succeeding/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response taskSucceedingGraph(@Context HttpServletRequest request, @PathParam("taskName") String taskName) {
        return taskGraph(taskName, SUCCEEDING);
    }

    private Response fieldGraph(String fieldName, int lingageType, int direction) {
        try {
            ParamChecker.notEmpty(fieldName, "field name cannot be null");
            final String jsonResult;
            if (SOURCE == direction) {
                jsonResult = dtLineageService.getSourceGraph(fieldName, lingageType);
            } else {
                jsonResult = dtLineageService.getTargetGraph(fieldName, lingageType);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put("fieldName", fieldName);
            response.put(AtlasClient.RESULTS, new JSONObject(jsonResult));

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            LOG.error("field entity not found for {}", fieldName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (DiscoveryException | IllegalArgumentException e) {
            LOG.error("Unable to get lineage source graph for field {}", fieldName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get lineage source graph for field {}", fieldName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p/>
     * 获取task级别表字段的的source血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/field/{fieldName}/source/graph<br/>
     *
     * @param fieldName 字段的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/field/{fieldName}/source/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response fieldSourceGraphInTask(@Context HttpServletRequest request,
                                           @PathParam("fieldName") String fieldName) {
        return fieldGraph(fieldName, IDtLineageService.TASK_LINEAGE, SOURCE);
    }

    /**
     * <p/>
     * 获取task级别表字段的的target血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/task/field/{fieldName}/target/graph<br/>
     *
     * @param fieldName 字段的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/task/field/{fieldName}/target/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response fieldTargetGraphInTask(@Context HttpServletRequest request,
                                           @PathParam("fieldName") String fieldName) {
        return fieldGraph(fieldName, IDtLineageService.TASK_LINEAGE, TARGET);
    }

    /**
     * <p/>
     * 获取step级别表字段的的source血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/field/{fieldName}/source/graph<br/>
     *
     * @param fieldName 字段的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/field/{fieldName}/source/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response fieldSourceGraphInStep(@Context HttpServletRequest request,
                                           @PathParam("fieldName") String fieldName) {
        return fieldGraph(fieldName, IDtLineageService.STEP_LINEAGE, SOURCE);
    }

    /**
     * <p/>
     * 获取step级别表字段的的target血缘关系.<br/>
     * <br/>
     * http://localhost:21000/api/atlas/lineage/step/field/{fieldName}/target/graph<br/>
     *
     * @param fieldName 字段的名称
     * @return JSON。
     * </p>
     */
    @GET
    @Path("/step/field/{fieldName}/target/graph")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(DtLineageResource.class)
    public Response fieldTargetGraphInStep(@Context HttpServletRequest request,
                                           @PathParam("fieldName") String fieldName) {
        return fieldGraph(fieldName, IDtLineageService.STEP_LINEAGE, TARGET);
    }
}
