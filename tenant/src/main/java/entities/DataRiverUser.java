package entities;

import DataModel.TenantDataModelType;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 10:55
 */
public class DataRiverUser {
    private String name;
    private String description;
    private String accId;
    private String accKey;
    private List<DataRiverProject> projects = new ArrayList<>();

    public static Referenceable createUserEntity(DataRiverUser user){
        String type = TenantDataModelType.DR_USER.getName();
        Referenceable existUser = DataRiverGetEntity.getEntity(type, "name", user.getName());
        if (existUser != null){
            return existUser;
        }
        Referenceable userEntity = DataRiverCreateEntity.createNewEntity(type, TenantUtil.formateTraitName(type));
        userEntity.set("name", user.getName());
        userEntity.set("description", user.getDescription());
        userEntity.set("accId", user.getAccId());
        userEntity.set("accKey", user.getAccKey());
        //add projects

        return  userEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccId() {
        return accId;
    }

    public void setAccId(String accId) {
        this.accId = accId;
    }

    public String getAccKey() {
        return accKey;
    }

    public void setAccKey(String accKey) {
        this.accKey = accKey;
    }
}
