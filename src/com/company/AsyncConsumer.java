package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.*;

public class AsyncConsumer implements MessageListener {
    private AQjmsSession session;
    private MessageConsumer consumer;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage txtMessage = (TextMessage)message;
                System.out.println("Message received: " + txtMessage.getText());
                this.session.commit();
            } else {
                System.out.println("Invalid message received.");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void run(AQjmsSession session, String user, String queueName) {
        try {
            this.session = session;
            Queue queue = this.session.getQueue(user, queueName);
            this.consumer = session.createConsumer(queue);
            this.consumer.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.consumer.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
