package org.apache.atlas.odps.model;

/**
 * odsp数据类型枚举
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public enum OdpsDataTypes {
    //enums
    ODPS_OBJECT_TYPE("OdpsObjectType"),
    ODPS_RESOURCE_TYPE("OdpsResourceType"),
    ODPS_PACKAGE_RESOURCE_TYPE("OdpsPackageResourceType"),

    //classes
    ODPS_ACCINFO("OdpsAccInfo"),
    ODPS_PROJECT("OdpsProject"),
    ODPS_COLUMN("OdpsColumn"),
    ODPS_PARTITION("OdpsPartition"),
    ODPS_TABLE("OdpsTable"),
    ODPS_RESOURCE("OdpsResource"),
    ODPS_PACKAGE_RESOURCE_ITEM("OdpsPackageResourceItem"),
    ODPS_PACKAGE("OdpsPackage"),
    ODPS_TASK("OdpsTask"),
    ODPS_INSTANCE("OdpsInstance");

    private final String value;

    OdpsDataTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
