package com.company;

import com.company.Forms.ClientForm;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public class AsyncConsumer extends Thread implements MessageListener {
    private AQjmsSession session;
    private List<String> messageList;

    public AsyncConsumer(AQjmsSession session, String userName, String queueName) {
        this.session = session;
        messageList = new ArrayList<>();
        try {
            Queue queue = this.session.getQueue(userName, queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String txtMessage = ((TextMessage)message).getText();
                System.out.println(this.getName() + " Message received: " + txtMessage);
                messageList.add(txtMessage);
                if (ClientForm.getClientForm().isRowSelected(this.getName())) {
                    ClientForm.getClientForm().appendConsumerOutput(txtMessage);
                }
                this.session.commit();
            } else {
                System.out.println("Invalid message received.");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000); //temporary
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMessageList() {
        return messageList;
    }

    @Override
    public void run() {
    }

    public void shutdown() {
        try {
            this.session.close();
            this.interrupt();
//            this.consumer.setMessageListener(null);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


}
