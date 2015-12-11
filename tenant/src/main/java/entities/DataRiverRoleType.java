package entities;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 11:16
 */
public enum DataRiverRoleType {
    PROJECT_ADMIN("dataRiver-administrator", 999),
    PROJECT_ROLE0("column-role0", 0),
    PROJECT_ROLE1("column-role1", 1),
    PROJECT_ROLE2("column-role2", 2),
    PROJECT_ROLE3("column-role3", 3),
    PROJECT_ROLE4("column-role4", 4),
    PROJECT_ROLE5("column-role5", 5),
    PROJECT_ROLE6("column-role6", 6),
    PROJECT_ROLE7("column-role7", 7),
    PROJECT_ROLE8("column-role8", 8);

    private String roleName;
    private int level;

    DataRiverRoleType(String roleName, int level){
        this.roleName = roleName;
        this.level = level;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
