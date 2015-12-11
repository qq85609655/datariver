package entities;

import org.apache.atlas.typesystem.Referenceable;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 13:57
 */
public class DataRiverCreateEntity {

    public static Referenceable createNewEntity(String type, String... traitNames){
        return new Referenceable(type, traitNames);
    }
}
