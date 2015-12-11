package entities;

import DataModel.TenantDataModelType;
import org.apache.atlas.typesystem.Referenceable;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 11:03
 */
public class DataRiverEngine {
    private String name;
    private String description;
    private String proxyAccount;
    private String odpsProjectCluster;
    private String odpsProjectName;
    private Referenceable odpsProject;

    public  Referenceable createEngine(){
        odpsProjectCluster = odpsProjectCluster == null ? "default" : odpsProjectCluster;
        String odpsProQualifiedName = "odps."+odpsProjectCluster+"."+odpsProjectName;
        Referenceable existOdpsProject = DataRiverGetEntity.getEntity("OdpsProject",TenantUtil.QUALIFIED_NAME, odpsProQualifiedName);
        if (existOdpsProject == null){
            existOdpsProject = createOdpsProjectDictionary(odpsProQualifiedName, odpsProjectName);
        }
        String type = TenantDataModelType.DR_ENGINE.getName();
        Referenceable engine = DataRiverCreateEntity.createNewEntity(type, TenantUtil.formateTraitName(type));
        engine.set("name",name);
        engine.set("description", description);
        engine.set("dataContainer", existOdpsProject);
        return  null;
    }

    public Referenceable createOdpsProjectDictionary(String odpsProjectCluster, String odpsProjectName){
        //create odps project dictionary

        return null;
    }
}
