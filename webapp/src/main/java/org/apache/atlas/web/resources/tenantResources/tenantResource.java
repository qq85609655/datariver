package org.apache.atlas.web.resources.tenantResources;

import entities.DataRiverTenant;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/11 15:00
 */

@Path("tenant")
public class tenantResource {

    @POST
    public String createTenant(String json){
        DataRiverTenant tenant = null;
    }
}
