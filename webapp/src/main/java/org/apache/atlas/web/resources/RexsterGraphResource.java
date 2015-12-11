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

package org.apache.atlas.web.resources;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.repository.graph.GraphProvider;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;

/**
 * <p>
 *     操作图数据库的接口。<br/>
 *     实现了Rexster接口中的大多数GET操作，但是没有使用索引。<br/>
 *     具体可以参考：https://github.com/tinkerpop/rexster/wiki/Basic-REST-API<br/>
 *     <br/>
 *     这里是Rexster的REST API的子集，目的是提供只读访问后端图数据库的方法。<br/>
 * </p>
 */
@Path("graph")
@Singleton
@JsonRootType
@JsonName("graph")
public class RexsterGraphResource {
    public static final String OUT_E = "outE";
    public static final String IN_E = "inE";
    public static final String BOTH_E = "bothE";
    public static final String OUT = "out";
    public static final String IN = "in";
    public static final String BOTH = "both";
    public static final String OUT_COUNT = "outCount";
    public static final String IN_COUNT = "inCount";
    public static final String BOTH_COUNT = "bothCount";
    public static final String OUT_IDS = "outIds";
    public static final String IN_IDS = "inIds";
    public static final String BOTH_IDS = "bothIds";
    private static final Logger LOG = LoggerFactory.getLogger(RexsterGraphResource.class);

    private TitanGraph graph;

    @Inject
    public RexsterGraphResource(GraphProvider<TitanGraph> graphProvider) {
        this.graph = graphProvider.get();
    }

    private static void validateInputs(String errorMsg, String... inputs) {
        for (String input : inputs) {
            if (StringUtils.isEmpty(input)) {
                throw new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type("text/plain").build());
            }
        }
    }

    protected Graph getGraph() {
        return graph;
    }

    protected Set<String> getVertexIndexedKeys() {
        return graph.getIndexedKeys(Vertex.class);
    }

    protected Set<String> getEdgeIndexedKeys() {
        return graph.getIndexedKeys(Edge.class);
    }

    /**
     * <p>
     *     通过id获取一个顶点。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/vertices/{id}<br/>
     * </p>
     */
    @GET
    @Path("/vertices/{id}")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getVertex(@PathParam("id") final String vertexId) {
        LOG.info("Get vertex for vertexId= {}", vertexId);
        validateInputs("Invalid argument: vertex id passed is null or empty.", vertexId);
        try {
            Vertex vertex = findVertex(vertexId);

            JSONObject response = new JSONObject();
            response.put(AtlasClient.RESULTS,
                    GraphSONUtility.jsonFromElement(vertex, getVertexIndexedKeys(), GraphSONMode.NORMAL));
            return Response.ok(response).build();
        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private Vertex findVertex(String vertexId) {
        Vertex vertex = getGraph().getVertex(vertexId);
        if (vertex == null) {
            String message = "Vertex with [" + vertexId + "] cannot be found.";
            LOG.info(message);
            throw new WebApplicationException(Servlets.getErrorResponse(message, Response.Status.NOT_FOUND));
        }

        return vertex;
    }

    /**
     * <p>
     *     通过id获取顶点的属性。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/vertices/properties/{id}<br/>
     *     @param vertexId 定点的id值
     *     @param relationships 关系信息
     * </p>
     */
    @GET
    @Path("/vertices/properties/{id}")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getVertexProperties(@PathParam("id") final String vertexId,
            @DefaultValue("false") @QueryParam("relationships") final String relationships) {
        LOG.info("Get vertex for vertexId= {}", vertexId);
        validateInputs("Invalid argument: vertex id passed is null or empty.", vertexId);
        try {
            Vertex vertex = findVertex(vertexId);

            Map<String, String> vertexProperties = getVertexProperties(vertex);

            JSONObject response = new JSONObject();
            response.put(AtlasClient.RESULTS, new JSONObject(vertexProperties));
            response.put(AtlasClient.COUNT, vertexProperties.size());
            return Response.ok(response).build();
        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private Map<String, String> getVertexProperties(Vertex vertex) {
        Map<String, String> vertexProperties = new HashMap<>();
        for (String key : vertex.getPropertyKeys()) {
            vertexProperties.put(key, vertex.<String>getProperty(key));
        }

        // todo: get the properties from relationships

        return vertexProperties;
    }

    /**
     * <p>
     *     根据输入的属性值获取顶点列表。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/vertices?key=<key>&value=<value><br/>
     *     @param key 属性名称
     *     @param value 属性值
     *     @return JSON格式的数据，包含符合条件的顶点列表。
     * </p>
     */
    @GET
    @Path("/vertices")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getVertices(@QueryParam("key") final String key, @QueryParam("value") final String value) {
        LOG.info("Get vertices for property key= {}, value= {}", key, value);
        validateInputs("Invalid argument: key or value passed is null or empty.", key, value);
        try {
            JSONObject response = buildJSONResponse(getGraph().getVertices(key, value));
            return Response.ok(response).build();

        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p>
     *     获取顶点在某个方向上的边。<br/>
     *     方向可以是：{(?!outE)(?!bothE)(?!inE)(?!out)(?!both)(?!in)(?!query).+}<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/vertices/{id}/{direction}<br/>
     *     @param vertexId 顶点的id
     *     @param direction 方向信息
     *     @return JSON格式的数据，包含符合条件的边。
     * </p>
     */
    @GET
    @Path("vertices/{id}/{direction}")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getVertexEdges(@PathParam("id") String vertexId, @PathParam("direction") String direction) {
        LOG.info("Get vertex edges for vertexId= {}, direction= {}", vertexId, direction);
        // Validate vertex id. Direction is validated in VertexQueryArguments.
        validateInputs("Invalid argument: vertex id or direction passed is null or empty.", vertexId, direction);
        try {
            Vertex vertex = findVertex(vertexId);

            return getVertexEdges(vertex, direction);

        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private Response getVertexEdges(Vertex vertex, String direction) throws JSONException {
        // break out the segment into the return and the direction
        VertexQueryArguments queryArguments = new VertexQueryArguments(direction);
        // if this is a query and the _return is "count" then we don't bother to send back the
        // result array
        boolean countOnly = queryArguments.isCountOnly();
        // what kind of data the calling client wants back (vertices, edges, count, vertex
        // identifiers)
        ReturnType returnType = queryArguments.getReturnType();
        // the query direction (both, out, in)
        Direction queryDirection = queryArguments.getQueryDirection();

        VertexQuery query = vertex.query().direction(queryDirection);

        JSONArray elementArray = new JSONArray();
        long counter = 0;
        if (returnType == ReturnType.VERTICES || returnType == ReturnType.VERTEX_IDS) {
            Iterable<Vertex> vertexQueryResults = query.vertices();
            for (Vertex v : vertexQueryResults) {
                if (returnType.equals(ReturnType.VERTICES)) {
                    elementArray.put(GraphSONUtility.jsonFromElement(v, getVertexIndexedKeys(), GraphSONMode.NORMAL));
                } else {
                    elementArray.put(v.getId());
                }
                counter++;
            }
        } else if (returnType == ReturnType.EDGES) {
            Iterable<Edge> edgeQueryResults = query.edges();
            for (Edge e : edgeQueryResults) {
                elementArray.put(GraphSONUtility.jsonFromElement(e, getEdgeIndexedKeys(), GraphSONMode.NORMAL));
                counter++;
            }
        } else if (returnType == ReturnType.COUNT) {
            counter = query.count();
        }

        JSONObject response = new JSONObject();
        if (!countOnly) {
            response.put(AtlasClient.RESULTS, elementArray);
        }
        response.put(AtlasClient.COUNT, counter);
        return Response.ok(response).build();
    }

    /**
     * Get a single edge with a unique id.
     *
     * GET http://host/metadata/lineage/edges/id
     * graph.getEdge(id);
     */
    /**
     * <p>
     *     获取一条边。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/edges/{id}<br/>
     *     @param edgeId 边的id
     *     @return JSON格式的数据，包含符合条件的边。
     * </p>
     */
    @GET
    @Path("/edges/{id}")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getEdge(@PathParam("id") final String edgeId) {
        LOG.info("Get vertex for edgeId= {}", edgeId);
        validateInputs("Invalid argument: edge id passed is null or empty.", edgeId);
        try {
            Edge edge = getGraph().getEdge(edgeId);
            if (edge == null) {
                String message = "Edge with [" + edgeId + "] cannot be found.";
                LOG.info(message);
                throw new WebApplicationException(
                        Response.status(Response.Status.NOT_FOUND).entity(Servlets.escapeJsonString(message)).build());
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.RESULTS,
                    GraphSONUtility.jsonFromElement(edge, getEdgeIndexedKeys(), GraphSONMode.NORMAL));
            return Response.ok(response).build();
        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private <T extends Element> JSONObject buildJSONResponse(Iterable<T> elements) throws JSONException {
        JSONArray vertexArray = new JSONArray();
        long counter = 0;
        for (Element element : elements) {
            counter++;
            vertexArray.put(GraphSONUtility.jsonFromElement(element, getVertexIndexedKeys(), GraphSONMode.NORMAL));
        }

        JSONObject response = new JSONObject();
        response.put(AtlasClient.RESULTS, vertexArray);
        response.put(AtlasClient.COUNT, counter);

        return response;
    }

    private enum ReturnType {VERTICES, EDGES, COUNT, VERTEX_IDS}

    /**
     * Helper class for query arguments.
     */
    public static final class VertexQueryArguments {

        private final Direction queryDirection;
        private final ReturnType returnType;
        private final boolean countOnly;

        public VertexQueryArguments(String directionSegment) {
            if (OUT_E.equals(directionSegment)) {
                returnType = ReturnType.EDGES;
                queryDirection = Direction.OUT;
                countOnly = false;
            } else if (IN_E.equals(directionSegment)) {
                returnType = ReturnType.EDGES;
                queryDirection = Direction.IN;
                countOnly = false;
            } else if (BOTH_E.equals(directionSegment)) {
                returnType = ReturnType.EDGES;
                queryDirection = Direction.BOTH;
                countOnly = false;
            } else if (OUT.equals(directionSegment)) {
                returnType = ReturnType.VERTICES;
                queryDirection = Direction.OUT;
                countOnly = false;
            } else if (IN.equals(directionSegment)) {
                returnType = ReturnType.VERTICES;
                queryDirection = Direction.IN;
                countOnly = false;
            } else if (BOTH.equals(directionSegment)) {
                returnType = ReturnType.VERTICES;
                queryDirection = Direction.BOTH;
                countOnly = false;
            } else if (BOTH_COUNT.equals(directionSegment)) {
                returnType = ReturnType.COUNT;
                queryDirection = Direction.BOTH;
                countOnly = true;
            } else if (IN_COUNT.equals(directionSegment)) {
                returnType = ReturnType.COUNT;
                queryDirection = Direction.IN;
                countOnly = true;
            } else if (OUT_COUNT.equals(directionSegment)) {
                returnType = ReturnType.COUNT;
                queryDirection = Direction.OUT;
                countOnly = true;
            } else if (BOTH_IDS.equals(directionSegment)) {
                returnType = ReturnType.VERTEX_IDS;
                queryDirection = Direction.BOTH;
                countOnly = false;
            } else if (IN_IDS.equals(directionSegment)) {
                returnType = ReturnType.VERTEX_IDS;
                queryDirection = Direction.IN;
                countOnly = false;
            } else if (OUT_IDS.equals(directionSegment)) {
                returnType = ReturnType.VERTEX_IDS;
                queryDirection = Direction.OUT;
                countOnly = false;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(Servlets.escapeJsonString(directionSegment + " segment was invalid.")).build());
            }
        }

        public Direction getQueryDirection() {
            return queryDirection;
        }

        public ReturnType getReturnType() {
            return returnType;
        }

        public boolean isCountOnly() {
            return countOnly;
        }
    }

    /**
     * <p>
     *     获取所有的顶点。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/vertices-all<br/>
     *     @return JSON格式的数据，包含所有的顶点信息。
     *
     * </p>
     */
    @GET
    @Path("/vertices-all")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getVerticesAll() {
        LOG.info("Get all vertices.");
        try {
            JSONObject response = buildJSONResponse(getGraph().getVertices());
            return Response.ok(response).build();

        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * <p>
     *     获取所有的边。<br/>
     *     <br/>
     *     http://localhost:21000/api/atlas/graph/edges-all<br/>
     *     @return JSON格式的数据，包含所有边的信息。
     *
     * </p>
     */
    @GET
    @Path("/edges-all")
    @Produces({Servlets.JSON_MEDIA_TYPE})
    @org.codehaus.enunciate.jaxrs.TypeHint(RexsterGraphResource.class)
    @org.codehaus.enunciate.json.JsonProperty
    public Response getEdgesAll() {
        LOG.info("Get all edges.");
        try {
            JSONObject response = buildJSONResponse(getGraph().getEdges());
            return Response.ok(response).build();

        } catch (JSONException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }
}
