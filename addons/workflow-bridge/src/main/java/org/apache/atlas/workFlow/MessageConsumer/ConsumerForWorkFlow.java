package org.apache.atlas.workFlow.MessageConsumer;

import org.apache.atlas.workFlow.handler.WorkflowJobHandler;
import org.apache.atlas.workFlow.model.RegisteredModel;
import org.apache.oozie.AppType;
import org.apache.oozie.client.event.Event;
import org.apache.oozie.client.event.jms.JMSHeaderConstants;
import org.apache.oozie.client.event.jms.JMSMessagingUtils;
import org.apache.oozie.client.event.message.CoordinatorActionMessage;
import org.apache.oozie.client.event.message.SLAMessage;
import org.apache.oozie.client.event.message.WorkflowJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/25 9:57
 */
public class ConsumerForWorkFlow extends CommonConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerForWorkFlow.class);

    boolean registeredSign = false;

    private ExecutorService executor;

    public ConsumerForWorkFlow(ExecutorService executor, ConnectionFactory connectionFactory, String topicName){
        super(connectionFactory, topicName);
        this.executor = executor;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message.getStringProperty(JMSHeaderConstants.MESSAGE_TYPE).equals(Event.MessageType.SLA.name())) {
                SLAMessage slaMessage = JMSMessagingUtils.getEventMessage(message);
                System.out.println("收到消息sla:" + slaMessage.toString());
            } else if (message.getStringProperty(JMSHeaderConstants.APP_TYPE).equals(AppType.WORKFLOW_JOB.name())) {
                WorkflowJobMessage wfJobMessage = JMSMessagingUtils.getEventMessage(message);
                Runnable thread = new WorkflowJobHandler().process(wfJobMessage);
                if (thread != null) {
                    registeredModel();
                    executor.submit(thread);
                }
                //System.out.println("收到消息wf:" + wfJobMessage.toString());
            } else if (message.getStringProperty(JMSHeaderConstants.APP_TYPE).equals(AppType.COORDINATOR_JOB.name())) {
                CoordinatorActionMessage wfJobMessage = JMSMessagingUtils.getEventMessage(message);
                System.out.println("收到消息c:" + wfJobMessage.toString());
            } else {
                System.out.println("收到消息other:" + message.toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registeredModel(){
        if(registeredSign){
            return;
        }
        synchronized(RegisteredModel.class){
            if(registeredSign){
                return;
            }
            RegisteredModel model = new RegisteredModel();
            try {
                registeredSign = model.registerCommonModel();
                registeredSign = model.registerWorkflowDataModel() && registeredSign;
            } catch (Exception e) {
                LOG.error("regist error",e);
            }
        }

    }
}
