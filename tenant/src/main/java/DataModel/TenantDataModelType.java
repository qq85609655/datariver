package DataModel;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 9:17
 */
public enum TenantDataModelType {
    //enum type
    DR_PROJECT_TYPE("DrProjectType"),
    DR_PROJECT_STATUS("DrProjectStatus"),
    DR_ENGINE_TYPE("DrEngineType"),
    //class type
    DR_USER("DrUser"),
    DR_PROJECT_ROLE("DrProjectRole"),
    DR_TENANT("DrTenant"),
    DR_PROJECT("DrProject"),
    DR_ENGINE("DrEngine");

    private String name;
    public String getName() {
        return this.name;
    }

    TenantDataModelType(String name){
        this.name = name;
    }

}
