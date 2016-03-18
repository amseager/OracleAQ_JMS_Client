package com.company;

import com.company.Forms.ClientForm;
import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender implements Runnable {
    private AQjmsSession session;
    private MessageProducer producer;
    private AtomicInteger msgNumber;
    private int msgCount;
    private ArrayList<String> msgList;

    public Sender(AQjmsSession session, MessageProducer producer, AtomicInteger msgNumber, int msgCount) {
        this.session = session;
        this.producer = producer;
        this.msgNumber = msgNumber;
        this.msgCount = msgCount;
        msgList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < msgCount; i++) {
                String txtMessage = "<user>" + (msgNumber.incrementAndGet()) + "</user>";
                TextMessage message = session.createTextMessage(txtMessage);
                message.setStringProperty("SOAPAction", "getQuote");
                producer.send(message);
                System.out.println("Sent message = " + message.getText());
                msgList.add(txtMessage);
            }
            session.commit();
            for (String message : msgList) {
                ClientForm.getForm().getListModelBrowser().addElement(message);
            }
            ClientForm.getForm().getLblTotalRowsValue()
                    .setText(String.valueOf(ClientForm.getForm().getListModelBrowser().getSize()));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
