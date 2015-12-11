package org.apache.atlas.odps.client;

import com.alibaba.fastjson.JSONObject;

/**
 * DDP返回结果
 */
public class DDPResult {
    private int retCode;
    private String retMessage;
    private JSONObject retData;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetMessage() {
        return retMessage;
    }

    public void setRetMessage(String retMessage) {
        this.retMessage = retMessage;
    }

    public JSONObject getRetData() {
        return retData;
    }

    public void setRetData(JSONObject retData) {
        this.retData = retData;
    }

    public boolean isSuccess() {
        return getRetCode() == 200;
    }
}
