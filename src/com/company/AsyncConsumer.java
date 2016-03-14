package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.*;

public class AsyncConsumer extends Thread implements MessageListener {
    private AQjmsSession session;

    public AsyncConsumer(AQjmsSession session, String userName, String queueName) {
        this.session = session;
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
                TextMessage txtMessage = (TextMessage)message;
                System.out.println(this.getName() + " Message received: " + txtMessage.getText());
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
