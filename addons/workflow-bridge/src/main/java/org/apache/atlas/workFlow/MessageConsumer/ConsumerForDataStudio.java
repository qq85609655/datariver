package org.apache.atlas.workFlow.MessageConsumer;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.ExecutorService;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/25 10:03
 */
public class ConsumerForDataStudio extends CommonConsumer {

    private ExecutorService executor;
    public ConsumerForDataStudio(ExecutorService executor, ConnectionFactory connectionFactory, String topicName) {
        super(connectionFactory, topicName);
        this.executor = executor;
    }
}
