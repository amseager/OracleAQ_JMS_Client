package com.company;

import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jms.*;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class OJMSClient {

    public static QueueConnection getConnection() {
        Locale.setDefault(Locale.ENGLISH);
        String hostname = "localhost";
        String oracle_sid = "xe";
        int portno = 1521;
        String userName = "jmsuser";
        String password = "jmsuser";
        String driver = "thin";
        QueueConnectionFactory QFac = null;
        QueueConnection QCon = null;
        try {
            // get connection factory , not going through JNDI here
            QFac = AQjmsFactory.getQueueConnectionFactory(hostname, oracle_sid, portno, driver);
            // create connection
            QCon = QFac.createQueueConnection(userName, password);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return QCon;
    }

    public static void createQueue(AQjmsSession session, String user, String qTable, String queueName) {
        try {
            // Create Queue Tables
            System.out.println("Creating Queue Table...");
            AQQueueTableProperty qt_prop = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            AQQueueTable q_table = session.createQueueTable(user, qTable, qt_prop);
            System.out.println("Qtable created");

            // create a queue
            AQjmsDestinationProperty dest_prop = new AQjmsDestinationProperty();
            Queue queue = session.createQueue(q_table, queueName, dest_prop);
            System.out.println("Queue created");

            // start the queue
            ((AQjmsDestination) queue).start(session, true, true);
            System.out.println("Queue started");

        } catch (JMSException | AQException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(AQjmsSession session, String user, String queueName,String message) {

        try {
            Queue queue = session.getQueue(user, queueName);
            MessageProducer producer = session.createProducer(queue);
            TextMessage tMsg = session.createTextMessage(message);

            //set properties to msg since axis2 needs this parameters to find the operation
            tMsg.setStringProperty("SOAPAction", "getQuote");
            producer.send(tMsg);
            System.out.println("Sent message = " + tMsg.getText());

            producer.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void browseMessage(AQjmsSession session, String user, String queueName) {
        try {
            Queue queue = session.getQueue(user, queueName);
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration enu = browser.getEnumeration();
            List<String> list = new ArrayList<>();
            while (enu.hasMoreElements()) {
                TextMessage message = (TextMessage) enu.nextElement();
                list.add(message.getText());
            }
            for (int i = 0; i < list.size(); i++) {
                System.out.println("Browsed msg " + list.get(i));
            }
            browser.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void consumeMessage(AQjmsSession session, String user, String queueName) {
        try {
            Queue queue = session.getQueue(user, queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive();
            System.out.println("MESSAGE RECEIVED " + msg.getText());
            consumer.close();

//            ((AQjmsDestination)queue).stop(session, true, true, true);
//            ((AQjmsDestination)queue).delete();
//            ((AQjmsDestination) queue).drop(session);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void dropAQTable(AQjmsSession session, String user, String qTable) {
        try {
            AQQueueTable q_table = session.getQueueTable (user, qTable);
            q_table.drop(true);
            System.out.println("Table " + user + "." + qTable + "has been dropped successfully");
        } catch (JMSException | AQException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String userName = "jmsuser";
        String queue = "sample_aq";
        String qTable = "sample_aqtbl";

        try {
            QueueConnection connection = getConnection();
            AQjmsSession session = (AQjmsSession) connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            connection.start();

//            dropAQTable(session, userName, qTable);
//            createQueue(session, userName, qTable, queue);
//            for (int i = 0; i < 10; i++)
                sendMessage(session, userName, queue,"<user>text" + 123 + "</user>");
//            browseMessage(session, userName, queue);
//            consumeMessage(session, userName, queue);

            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }



    }
}
