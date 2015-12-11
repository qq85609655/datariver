/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.dxt.util;

/**
 * DXT支持的数据库类型
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public enum DxtDbType {
    /* MySQL,Oracle,MSSQL,ODPS,ADS,TeraData,IBM DB2,Sybase,PostgreSQL,HIVE,HIVE2,HDFS */
    MYSQL("MYSQL"),
    ORACLE("ORACLE"),
    MSSQL("SQLSERVER"),
    ODPS("ODPS"),
    ADS("ADS"),
    TERADATA("TERADATA"),
    IBM_DB2("DB2"),
    SYBASE("SYBASE"),
    POSTGRESQL("POSTGRESQL"),
    HIVE("HIVE"),
    HDFS("HDFS");

    private final String value;

    DxtDbType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static DxtDbType findByName(String name) throws Exception {
        DxtDbType dbType = null;
        switch (name.toUpperCase()) {
            case "MYSQL":
                dbType = MYSQL;
                break;
            case "ORACLE":
                dbType = ORACLE;
                break;
            case "MSSQL":
                dbType = MSSQL;
                break;
            case "ODPS":
                dbType = ODPS;
                break;
            case "ADS":
                dbType = ADS;
                break;
            case "TERADATA":
                dbType = TERADATA;
                break;
            case "IBM DB2":
                dbType = IBM_DB2;
                break;
            case "SYBASE":
                dbType = SYBASE;
                break;
            case "POSTGRESQL":
                dbType = POSTGRESQL;
                break;
            case "HIVE":
            case "HIVE2":
                dbType = HIVE;
                break;
            case "HDFS":
                dbType = HDFS;
                break;
            default:
                throw new Exception("Unknown database type: " + dbType + ".");
        }

        return dbType;
    }
}
