package entities;

import org.apache.atlas.ObjectPrivileges;
import org.apache.atlas.ObjectTypes;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/13 9:43
 */
public class DataRiverObjectPrivileges {
    private ObjectTypes objectType;
    private List<ObjectPrivileges> privilege = new ArrayList<>();

    private Referenceable createPrivilege(){
        return  null;
    }
}
