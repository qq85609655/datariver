package entities;

import DataModel.TenantDataModelType;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 10:53
 */
public class DataRiverTenant {
    private String name;
    private String description;
    private List<DataRiverUser> admins = new ArrayList<>();
    private List<DataRiverUser> users = new ArrayList<>();
    private List<DataRiverProject> projects = new ArrayList<>();

    public Referenceable createEntity(){
        String type = TenantDataModelType.DR_TENANT.getName();
        String qualifiedName = TenantUtil.formatQualifiedName(name);
        Referenceable existTenant = DataRiverGetEntity.getEntity(type, TenantUtil.QUALIFIED_NAME, qualifiedName);
        if (existTenant != null){
            return  existTenant;
        }
        Referenceable tenant = DataRiverCreateEntity.createNewEntity(type, TenantUtil.formateTraitName(type));
        tenant.set("name", name);
        tenant.set("description", description);
        TenantUtil.formateDataElementProperty(tenant, qualifiedName);
        List<Referenceable> adminsRef = new ArrayList<>();
        for (DataRiverUser admin : admins){
            adminsRef.add(DataRiverUser.createUserEntity(admin));
        }
        if (adminsRef.size() > 0){
            tenant.set("admins", adminsRef);
        }
        List<Referenceable> usersRef = new ArrayList<>();
        for (DataRiverUser user:users){
            usersRef.add(DataRiverUser.createUserEntity(user));
        }
        if (usersRef.size() > 0){
            tenant.set("users", usersRef);
        }
        List<Referenceable> projectsRef = new ArrayList<>();
        for (DataRiverProject project : projects){
            projectsRef.add(DataRiverProject.createOrGetDrProject(name, project));
        }
        if (projectsRef.size() > 0){
            tenant.set("projects", projectsRef);
        }
        return tenant;
    }

}
