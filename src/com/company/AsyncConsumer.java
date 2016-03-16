package com.company;

import com.company.Forms.ClientForm;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public class AsyncConsumer extends Thread implements MessageListener {
    private AQjmsSession session;
    private String userName;
    private String queueName;
    private List<String> messageList;

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
                ClientForm.getClientForm().appendConsumerOutputIfRowIsSelected(this.getName(), txtMessage);
                session.commit();
                System.out.println(this.getName() + " Message received: " + txtMessage);
            } else {
                System.out.println("Invalid message received.");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMessageList() {
        return messageList;
    }

    @Override
    public void run() {
        try {
            Queue queue = session.getQueue(userName, queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            session.close();
            this.interrupt();
//            this.consumer.setMessageListener(null);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
