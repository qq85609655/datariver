package org.apache.atlas.web.resources;

import com.google.inject.Inject;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.discovery.DiscoveryException;
import org.apache.atlas.discovery.DiscoveryService;
import org.apache.atlas.typesystem.exception.EntityNotFoundException;
import org.apache.atlas.services.MetadataService;
import org.apache.atlas.services.OrganizationService;
import org.apache.atlas.typesystem.Struct;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.enunciate.jaxrs.SampleRequest;
import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 组织相关资源
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-17 14:04
 */
@Path("organizations")
@Singleton
@JsonRootType
@JsonName("organizations")
public class OrganizationResource {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationResource.class);
    private final MetadataService metadataService;
    private final DiscoveryService discoveryService;
    private final OrganizationService organizationService;

    @Inject
    public OrganizationResource(MetadataService metadataService, DiscoveryService discoveryService,OrganizationService organizationService) {
        this.metadataService = metadataService;
        this.discoveryService = discoveryService;
        this.organizationService = organizationService;
    }
    @GET
    @Path("statistics")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @SampleRequest
    @org.codehaus.enunciate.jaxrs.TypeHint(EntityResource.class)
    public Response getStatistics() {
        try {
            JSONArray results = organizationService.statistics();
            JSONObject response = new JSONObject();
            response.put(AtlasClient.REQUEST_ID, Servlets.getRequestId());
            response.put(AtlasClient.RESULTS, results);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.BAD_REQUEST));
        } catch (Throwable e) {
            throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
        }
    }
}
