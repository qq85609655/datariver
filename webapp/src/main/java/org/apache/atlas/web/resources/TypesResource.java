/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.web.resources;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.services.MetadataService;
import org.apache.atlas.typesystem.exception.TypeExistsException;
import org.apache.atlas.typesystem.types.DataTypes;
import org.apache.atlas.web.util.Servlets;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;

/**
 * <p>类型系统对外提供的REST接口。<br/>
 * <br/>
 * 类型用于描述一种可以表达的项目，比如Hive的表，ODPS的Project，工作流等。<br/>
 * 可以使用这些类型表示任何领域的元模型。<br/>
 * </p>
 */
@Path("types")
@Singleton
@JsonRootType
@JsonName("types")
public class TypesResource {

    private static final Logger LOG = LoggerFactory.getLogger(TypesResource.class);

    private final MetadataService metadataService;

    static final String TYPE_ALL = "all";

    @Inject
    public TypesResource(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * 提交类型定义，创建元模型，输入数据为JSON格式的类型定义。可以表示如Hive数据库、Hive表、ODPS的Project等。
     * @return JSON格式数据，包括servlet的请求Id和类型名称。
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(TypesResource.class)
    public Response submit(@Context HttpServletRequest request) {
        try {
            final String typeDefinition = Servlets.getRequestPayload(request);
            LOG.debug("Creating type with definition {} ", typeDefinition);

            JSONObject typesJson = metadataService.createType(typeDefinition);
            final JSONArray typesJsonArray = typesJson.getJSONArray(AtlasClient.TYPES);

            JSONArray typesResponse = new JSONArray();
            for (int i = 0; i < typesJsonArray.length(); i++) {
                final String name = typesJsonArray.getString(i);
                typesResponse.put(new JSONObject() {{
                    put(AtlasClient.NAME, name);
                }});
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put(AtlasClient.TYPES, typesResponse);
            return Response.status(ClientResponse.Status.CREATED).entity(response).build();
        } catch (TypeExistsException e) {
            LOG.error("Type already exists", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.CONFLICT));
        } catch (AtlasException | IllegalArgumentException e) {
            LOG.error("Unable to persist types", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to persist types", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Update of existing types - if the given type doesn't exist, creates new type
     * Allowed updates are:
     * 1. Add optional attribute
     * 2. Change required to optional attribute
     * 3. Add super types - super types shouldn't contain any required attributes
     * @param request
     * @return
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response update(@Context HttpServletRequest request) {
        try {
            final String typeDefinition = Servlets.getRequestPayload(request);
            LOG.debug("Updating type with definition {} ", typeDefinition);

            JSONObject typesJson = metadataService.updateType(typeDefinition);
            final JSONArray typesJsonArray = typesJson.getJSONArray(AtlasClient.TYPES);

            JSONArray typesResponse = new JSONArray();
            for (int i = 0; i < typesJsonArray.length(); i++) {
                final String name = typesJsonArray.getString(i);
                typesResponse.put(new JSONObject() {{
                    put(AtlasClient.NAME, name);
                }});
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put(AtlasClient.TYPES, typesResponse);
            return Response.ok().entity(response).build();
        } catch (TypeExistsException e) {
            LOG.error("Type already exists", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.CONFLICT));
        } catch (AtlasException | IllegalArgumentException e) {
            LOG.error("Unable to persist types", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to persist types", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 通过类型名称获取类型的定义。
     *
     * @param typeName 类型名称，是唯一的
     * @return JSON格式数据，包括类型名称、类型定义和servlet的请求Id。
     */
    @GET
    @Path("{typeName}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(TypesResource.class)
    public Response getDefinition(@Context HttpServletRequest request, @PathParam("typeName") String typeName) {
        try {
            final String typeDefinition = metadataService.getTypeDefinition(typeName);

            JSONObject response = new JSONObject();
            response.put(AtlasClient.TYPENAME, typeName);
            response.put(AtlasClient.DEFINITION, new JSONObject(typeDefinition));
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());

            return Response.ok(response).build();
        } catch (AtlasException e) {
            //LOG.error("Unable to get type definition for type {}, Not Found!", typeName, e);
            LOG.error("Unable to get type definition for type {}, Not Found!", typeName);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.NOT_FOUND));
        } catch (JSONException | IllegalArgumentException e) {
            LOG.error("Unable to get type definition for type {}, Bad Request!", typeName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get type definition for type {}, Internal Server Error!", typeName, e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取类型系统中注册的类型列表。
     *
     * @param type 类型的分类，比如TRAIT，CLASS，ENUM，STRUCT，MAP，ARRAY或者PRIMITIVE，缺省值是all
     * @return JSON格式数据，包括类型列表、类型个数和servlet的请求Id。
     */
    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @org.codehaus.enunciate.jaxrs.TypeHint(TypesResource.class)
    public Response getTypesByFilter(@Context HttpServletRequest request,
            @DefaultValue(TYPE_ALL) @QueryParam("type") String type) {
        try {
            List<String> result;
            if (TYPE_ALL.equals(type)) {
                result = metadataService.getTypeNamesList();
            } else {
                DataTypes.TypeCategory typeCategory = DataTypes.TypeCategory.valueOf(type);
                result = metadataService.getTypeNamesByCategory(typeCategory);
            }

            JSONObject response = new JSONObject();
            response.put(AtlasClient.RESULTS, new JSONArray(result));
            response.put(AtlasClient.COUNT, result.size());
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());

            return Response.ok(response).build();
        } catch (IllegalArgumentException | AtlasException ie) {
            LOG.error("Unsupported typeName while retrieving type list {}", type);
            throw new WebApplicationException(
                    Servlets.getErrorResponse("Unsupported type " + type, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            LOG.error("Unable to get types list", e);
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }
}
