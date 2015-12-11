package org.apache.atlas.workFlow.client;

import org.apache.oozie.client.WorkflowJob;

/**
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:59
 */
public interface WorkflowExtendsJob extends WorkflowJob {

    String getWorkspaceName();
}
