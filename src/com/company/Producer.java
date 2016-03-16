package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer extends Thread {
    private AQjmsSession session;
    private String userName;
    private String queueName;
    private AtomicInteger msgNumber;
    private int msgCount;

    public Producer(AQjmsSession session, String userName, String queueName, AtomicInteger msgNumber, int msgCount) {
        this.session = session;
        this.userName = userName;
        this.queueName = queueName;
        this.msgNumber = msgNumber;
        this.msgCount = msgCount;
    }

    @Override
    public void run() {
        try {
            Queue queue = session.getQueue(userName, queueName);
            MessageProducer producer = session.createProducer(queue);
            for (int i = 0; i < msgCount; i++) {
                String message = "<user>" + (msgNumber.incrementAndGet()) + "</user>";
                TextMessage txtMessage = session.createTextMessage(message);
//                txtMessage.setStringProperty("SOAPAction", "getQuote");
                producer.send(txtMessage);
                System.out.println("Sent message = " + txtMessage.getText());
            }
            session.commit();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }
}
