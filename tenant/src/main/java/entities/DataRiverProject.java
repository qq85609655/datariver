package entities;

import DataModel.TenantDataModelType;
import org.apache.atlas.typesystem.Referenceable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 10:58
 */
public class DataRiverProject {
    private String name;
    private String description;
    private DataRiverEngine engine;
    private List<DataRiverProjectRole> roleList = new ArrayList<>();
    private boolean autoSchedule = false;
    private boolean codeEnabled = true;

    //project --> roles --> users --> project  project & role entity create first without users
    public static Referenceable createOrGetDrProject(String tenantName, DataRiverProject project){
        String qualifiedName = TenantUtil.formatQualifiedName(tenantName, project.getName());
        String type = TenantDataModelType.DR_PROJECT.getName();
        Referenceable projectRef = DataRiverGetEntity.getEntity(type, TenantUtil.QUALIFIED_NAME, qualifiedName);
        if (projectRef == null){
            projectRef = DataRiverCreateEntity.createNewEntity(type, TenantUtil.formateTraitName(type));
            projectRef.set("name", project.getName());
            projectRef.set("description", project.getDescription());
            projectRef.set("autoSchedule", project.isAutoSchedule());
            projectRef.set("codeEnabled", project.isCodeEnabled());
            //create roles once
            projectRef.set("roles", createRolesRefer(tenantName, project));
            //create engine; engine may not exist
            if (project.getEngine() != null){
                projectRef.set("engine", project.getEngine().createEngine());
            }
            TenantUtil.formateDataElementProperty(projectRef, qualifiedName);
        }

        return projectRef;
    }

    private static List<Referenceable> createRolesRefer(String tenantName, DataRiverProject project){
        List<Referenceable> rolesRef = new ArrayList<>();
        //don't add users to role in create phase
        for (DataRiverProjectRole role : project.getRoleList()){
            rolesRef.add(role.createProjectRole(tenantName, project.getName()));
        }

        return rolesRef;
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

    public boolean isAutoSchedule() {
        return autoSchedule;
    }

    public void setAutoSchedule(boolean autoSchedule) {
        this.autoSchedule = autoSchedule;
    }

    public boolean isCodeEnabled() {
        return codeEnabled;
    }

    public void setCodeEnabled(boolean codeEnabled) {
        this.codeEnabled = codeEnabled;
    }

    public List<DataRiverProjectRole> getRoleList() {
        return roleList;
    }

    public DataRiverEngine getEngine() {
        return engine;
    }

    public void setEngine(DataRiverEngine engine) {
        this.engine = engine;
    }
}
