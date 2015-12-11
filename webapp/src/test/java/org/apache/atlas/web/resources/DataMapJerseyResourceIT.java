package org.apache.atlas.web.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.atlas.web.util.Servlets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

/**
 * 描述
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-18 16:08
 */
public class DataMapJerseyResourceIT extends BaseResourceIT {
    //@BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        Class<?> aClass = Class.forName("org.apache.atlas.odps.examples.OdpsQuickStart");
        Object quickStart = aClass.newInstance();
        aClass.getDeclaredMethod("main", String[].class).invoke(quickStart);
    }

    //@Test
    public void testgetWareHouseStatistics() throws Exception{
        WebResource resource = service.path("api/atlas/datamap/warehouse");

        ClientResponse clientResponse = resource.accept(Servlets.JSON_MEDIA_TYPE).type(Servlets.JSON_MEDIA_TYPE)
                .method(HttpMethod.GET, ClientResponse.class);
        Assert.assertEquals(clientResponse.getStatus(), Response.Status.OK.getStatusCode());

        String responseAsString = clientResponse.getEntity(String.class);
        Assert.assertNull(responseAsString);
    }
}
