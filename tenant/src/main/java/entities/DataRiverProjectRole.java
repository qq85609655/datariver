package entities;

import DataModel.TenantDataModelType;
import org.apache.atlas.ObjectPrivileges;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 11:03
 */
public class DataRiverProjectRole {
    private String name;
    private List<DataRiverUser> users = new ArrayList<>();
    private String policy;
    private String proxyAccount;
    private List<DataRiverObjectPrivileges> acls = new ArrayList<>();

    public DataRiverProjectRole(String name){
        this.name = name;
    }

    //create only once
    public Referenceable createProjectRole(String tenantName, String projectName){
        String type = TenantDataModelType.DR_PROJECT_ROLE.getName();
        String qualifiedName = TenantUtil.formatQualifiedName(tenantName, projectName, name);
        Referenceable role = DataRiverCreateEntity.createNewEntity(type, TenantUtil.formateTraitName(type));
        role.set("name", name);
        role.set("policy", policy);
        role.set("proxyAccount", proxyAccount);
        List<Referenceable> usersRef = new ArrayList<>();
        for (DataRiverUser user : users){
            usersRef.add(DataRiverUser.createUserEntity(user));
        }
        if (usersRef.size() > 0){
            role.set("users", usersRef);
        }

        TenantUtil.formateDataElementProperty(role, qualifiedName);
        return role;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getProxyAccount() {
        return proxyAccount;
    }

    public void setProxyAccount(String proxyAccount) {
        this.proxyAccount = proxyAccount;
    }

    public List<DataRiverUser> getUsers() {
        return users;
    }

    public void setUsers(List<DataRiverUser> users) {
        this.users = users;
    }
}
