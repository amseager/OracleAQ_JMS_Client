package com.company;

import com.company.Forms.ClientForm;
import oracle.jms.AQjmsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public class AsyncConsumer extends Thread implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(AsyncConsumer.class);

    private AQjmsSession session;
    private String userName;
    private String queueName;
    private List<String> messageList;
    private MessageConsumer messageConsumer;

    public AsyncConsumer(AQjmsSession session, String userName, String queueName) {
        this.session = session;
        this.userName = userName;
        this.queueName = queueName;
        this.messageList = new ArrayList<>();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String txtMessage = ((TextMessage)message).getText();
                messageList.add(txtMessage);
                ClientForm.getForm().appendConsumerOutputIfRowIsSelected(this.getName(), txtMessage);
//                ClientForm.getForm().refreshBrowser(txtMessage);
                session.commit();
                log.debug(this.getName() + " Message received: " + txtMessage);
            } else {
                log.error("Invalid message received.");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep((int) ClientForm.getForm().getSpnThreadLatency().getValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMessageList() {
        return messageList;
    }

    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    @Override
    public void run() {
        try {
            Queue queue = session.getQueue(userName, queueName);
            messageConsumer = session.createConsumer(queue);
            messageConsumer.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    public void pause() {
        try {
            if (messageConsumer.getMessageListener() == null) {
                ClientForm.getForm().getBtnPauseAsyncThread().setText("Pause AR thread");
                messageConsumer.setMessageListener(this);
            } else {
                ClientForm.getForm().getBtnPauseAsyncThread().setText("Resume AR thread");
                messageConsumer.setMessageListener(null);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            session.close();
            this.interrupt();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
