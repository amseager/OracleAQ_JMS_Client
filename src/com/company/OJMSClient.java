package com.company;

import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jms.*;

import javax.jms.*;
import java.util.Enumeration;
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

    public static AQQueueTable createQueueTable(AQjmsSession session, String userName, String queueTableName) {
        AQQueueTable queueTable = null;
        try {
            AQQueueTableProperty queueTableProperty = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            queueTable = session.createQueueTable(userName, queueTableName, queueTableProperty);
            System.out.println("Queue Table \"" + queueTableName + "\" has been created");
            session.commit();
        } catch (AQException | JMSException e) {
            e.printStackTrace();
        }
        return queueTable;
    }

    public static Queue createQueue(AQjmsSession session, AQQueueTable queueTable, String queueName) {
        Queue queue = null;
        try {
            AQjmsDestinationProperty dest_prop = new AQjmsDestinationProperty();
            queue = session.createQueue(queueTable, queueName, dest_prop);
            ((AQjmsDestination) queue).start(session, true, true);
            System.out.println("Queue \"" + queue.getQueueName() +"\" has been created and started");
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return queue;
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
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void browseMessage(AQjmsSession session, String user, String queueName) {
        try {
            Queue queue = session.getQueue(user, queueName);
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration enu = browser.getEnumeration();
            while (enu.hasMoreElements()) {
                TextMessage message = (TextMessage) enu.nextElement();
                System.out.println("Browsed msg " + message.getText());
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
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static void dropAQTable(AQjmsSession session, String user, String qTable) {
        try {
            AQQueueTable q_table = session.getQueueTable (user, qTable);
            q_table.drop(true);
            System.out.println("Table \"" + user + "." + qTable + "\" has been dropped successfully");
        } catch (JMSException | AQException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String userName = "jmsuser";
        String queueName = "sample_aq";
        String queueTableName = "sample_aqtbl";

        try {
            QueueConnection connection = getConnection();
            AQjmsSession session = (AQjmsSession) connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            connection.start();

//            dropAQTable(session, userName, queueTableName);

//            AQQueueTable queueTable = createQueueTable(session, userName, queueTableName);
//            Queue queue = createQueue(session, queueTable, queueName);

//            new Consumer().run(session, userName, queueName);
//            for (int i = 0; i < 100; i++) sendMessage(session, userName, queueName,"<user>text" + i + "</user>");

//            browseMessage(session, userName, queueName);
//            for (int i = 0; i < 110; i++) consumeMessage(session, userName, queueName);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }



    }
}
