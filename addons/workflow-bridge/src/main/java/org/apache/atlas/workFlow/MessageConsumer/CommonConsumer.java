package org.apache.atlas.workFlow.MessageConsumer;

import org.apache.oozie.AppType;

import javax.jms.*;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/25 10:06
 */
public class CommonConsumer implements MessageListener {

    protected ConnectionFactory connectionFactory;
    protected String topicName;
    public CommonConsumer(ConnectionFactory connectionFactory, String topicName){
        this.connectionFactory  = connectionFactory;
        this.topicName = topicName;
    }

    @Override
    public void onMessage(Message message) {

    }

    public void start(){
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(this);
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
