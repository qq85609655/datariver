package entities;

import org.apache.atlas.AtlasException;
import org.apache.atlas.services.MetadataService;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 14:57
 */
public class DataRiverGetEntity {

    @Inject
    private static MetadataService metadataService;

    private static final Logger LOG = LoggerFactory.getLogger(DataRiverGetEntity.class);
    public static Referenceable getEntity(String entityType, String attribute, String value){
        try {
            final String entityDefinition = metadataService.getEntityDefinition(entityType, attribute, value);
            return   InstanceSerialization.fromJsonReferenceable(entityDefinition, true);
        } catch (AtlasException e) {
            LOG.error("Get entity error : ", e);
        }
        return  null;
    }

}
