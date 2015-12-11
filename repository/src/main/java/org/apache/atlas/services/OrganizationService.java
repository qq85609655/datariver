package org.apache.atlas.services;

import com.google.inject.Singleton;
import org.apache.atlas.AtlasException;
import org.codehaus.jettison.json.JSONArray;

import java.util.Map;
import java.util.Set;

/**
 * 描述
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-18 11:44
 */

public interface OrganizationService {

    JSONArray statistics() throws AtlasException;
    public Map<String, Set<String>> getTypeWithSuperTypes();
    public Map<String, Set<String>> getTypeWithChildTypes() ;
}
