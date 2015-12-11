package org.apache.atlas.odps.client;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.commons.configuration.Configuration;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/30 19:32
 */
public class AtlasClientFactory {
    private static Configuration atlasProperties;
    private static  AtlasClient atlasClient;
    public static final String ATLAS_ENDPOINT = "atlas.rest.address";
    private static final String DEFAULT_DGI_URL = "http://localhost:21000/";
    static {
        try {
            atlasProperties = ApplicationProperties.get(ApplicationProperties.CLIENT_PROPERTIES);
        } catch (AtlasException e) {
            e.printStackTrace();
        }
    }

    public static AtlasClient getAtlasClient(){
        atlasClient = new AtlasClient(atlasProperties.getString(ATLAS_ENDPOINT, DEFAULT_DGI_URL));
        return atlasClient;
    }
}
