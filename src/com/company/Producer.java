package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;

public class Producer {
    private MessageProducer producer;

    public Producer(AQjmsSession session, String user, String queueName) {
        try {
            Queue queue = session.getQueue(user, queueName);
            producer = session.createProducer(queue);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void run(AQjmsSession session, String message) {
        try {
            TextMessage tMsg = session.createTextMessage(message);
            //set properties to msg since axis2 needs this parameters to find the operation
            tMsg.setStringProperty("SOAPAction", "getQuote");
            for (int i = 0; i < 100; i++) {
                producer.send(tMsg);
                System.out.println("Sent message = " + tMsg.getText());
            }
            //producer.close();
            //session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
