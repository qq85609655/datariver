package org.apache.atlas.dxt.client;

import com.google.gson.internal.LinkedTreeMap;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtRespBean {
    private String code;
    private String message;
    private LinkedTreeMap data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LinkedTreeMap getData() {
        return data;
    }

    public void setData(LinkedTreeMap data) {
        this.data = data;
    }

    public String getInstanceId() {
        return String.valueOf(data.get("instanceid"));
    }

    public String getId() {
        return String.valueOf(data.get("id"));
    }

    public String getResult() {
        return String.valueOf(data.get("result"));
    }
}
