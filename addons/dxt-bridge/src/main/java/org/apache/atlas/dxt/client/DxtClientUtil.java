package org.apache.atlas.dxt.client;

import java.util.Map;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/6 15:44
 */
public class DxtClientUtil {
    private DxtClient dxtClient = null;

    public DxtClientUtil(String workspaceName) {
        dxtClient = new DxtClient(workspaceName);
    }

    public Map getTables(String connId) throws Exception {
        DxtRespBean respBean = dxtClient.getDBTables(connId);
        String code = respBean.getCode();
        if (!code.equals("Success")) {
            throw new Exception("Failed to get tables of " + connId + ": " + code + ".");
        }

        return respBean.getData();
    }

    public Map getColumns(String connId, String tableName) throws Exception {
        DxtRespBean respBean = dxtClient.getDBTableColumns(connId, tableName);
        String code = respBean.getCode();
        if (!code.equals("Success")) {
            throw new Exception("Failed to get columns of " + tableName + ": " + code + ".");
        }

        return respBean.getData();
    }

    public Map getHdfsFields(String hdfsId, String body) throws Exception {
        DxtRespBean respBean = dxtClient.getHDFSFields(hdfsId, body);
        if (!respBean.getCode().equals("Success")) {
            throw new Exception("Failed to get HDFS fields for: " + hdfsId);
        }

        return respBean.getData();
    }

    public Map getDbConnInfo(String connId) throws Exception {
        DxtRespBean respBean = dxtClient.queryDBConnection(connId);
        if (!respBean.getCode().equals("Success")) {
            throw new Exception("Failed to get connection info for: " + connId);
        }

        return (Map) respBean.getData().get("connection");
    }

    public Map getTransHisLogs(String transId) throws Exception {
        DxtRespBean respBean = dxtClient.getTransHisLogs(transId);
        if (!respBean.getCode().equals("Success")) {
            throw new Exception("Failed to get trans historical info for: " + transId);
        }

        return respBean.getData();
    }

    public Map getTrans(String transId) throws Exception {
        DxtRespBean respBean = dxtClient.getTrans(transId);
        if (!respBean.getCode().equals("Success")) {
            throw new Exception("Failed to get trans for: " + transId);
        }

        return respBean.getData();
    }
}
