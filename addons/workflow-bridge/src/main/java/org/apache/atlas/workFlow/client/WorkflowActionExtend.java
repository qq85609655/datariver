package org.apache.atlas.workFlow.client;

import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.rest.JsonTags;
import org.apache.oozie.client.rest.JsonUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public class WorkflowActionExtend implements WorkflowAction {

    private String id;

    private String name = null;

    private String cred = null;

    private String type = null;

    private String conf;

    private WorkflowAction.Status status = null;

    private String stats;

    private int retries;

    private int userRetryCount;

    private int userRetryMax;

    private int userRetryInterval;

    private Date startTime;

    private Date endTime;

    private String transition = null;

    private String data;

    private String externalChildIDs;

    private String externalId = null;

    private String externalStatus = null;

    private String trackerUri = null;

    private String consoleUrl = null;

    private String errorCode = null;

    private String errorMessage = null;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCred() {
        return cred;
    }

    public void setCred(String cred) {
        this.cred = cred;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    @Override
    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    @Override
    public int getUserRetryCount() {
        return userRetryCount;
    }

    public void setUserRetryCount(int userRetryCount) {
        this.userRetryCount = userRetryCount;
    }

    @Override
    public int getUserRetryMax() {
        return userRetryMax;
    }

    public void setUserRetryMax(int userRetryMax) {
        this.userRetryMax = userRetryMax;
    }

    @Override
    public int getUserRetryInterval() {
        return userRetryInterval;
    }

    public void setUserRetryInterval(int userRetryInterval) {
        this.userRetryInterval = userRetryInterval;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    @Override
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getExternalChildIDs() {
        return externalChildIDs;
    }

    public void setExternalChildIDs(String externalChildIDs) {
        this.externalChildIDs = externalChildIDs;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(String externalStatus) {
        this.externalStatus = externalStatus;
    }

    @Override
    public String getTrackerUri() {
        return trackerUri;
    }

    public void setTrackerUri(String trackerUri) {
        this.trackerUri = trackerUri;
    }

    @Override
    public String getConsoleUrl() {
        return consoleUrl;
    }

    public void setConsoleUrl(String consoleUrl) {
        this.consoleUrl = consoleUrl;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static WorkflowActionExtend toWorkflowActionExtend(WorkflowAction action) {
        WorkflowActionExtend actionExtend = new WorkflowActionExtend();
        actionExtend.setId(action.getId());
        actionExtend.setName(action.getName());
        actionExtend.setCred(action.getCred());
        actionExtend.setType(action.getType());
        actionExtend.setConf(action.getConf());
        actionExtend.setStatus(action.getStatus());
        actionExtend.setRetries(action.getRetries());
        //actionExtend.setUserRetryCount(action.getUserRetryCount());
        //actionExtend.setUserRetryMax(action.getUserRetryMax());
        //actionExtend.setUserRetryInterval(action.getUserRetryInterval());
        actionExtend.setStartTime(action.getStartTime());
        actionExtend.setEndTime(action.getEndTime());
        actionExtend.setTransition(action.getTransition());
        actionExtend.setData(action.getData());
        actionExtend.setStats(action.getStats());
        actionExtend.setExternalChildIDs(action.getExternalChildIDs());
        actionExtend.setExternalId(action.getExternalId());
        actionExtend.setExternalStatus(action.getExternalStatus());
        actionExtend.setTrackerUri(action.getTrackerUri());
        actionExtend.setConsoleUrl(action.getConsoleUrl());
        actionExtend.setErrorCode(action.getErrorCode());
        actionExtend.setErrorMessage(action.getErrorMessage());
        return actionExtend;
    }
}
