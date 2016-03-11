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

    public static QueueConnection getConnection(String hostname, String sid, int port, String driver, String userName, String password) {
        Locale.setDefault(Locale.ENGLISH);
        QueueConnection queueConnection = null;
        try {
            QueueConnectionFactory factory = AQjmsFactory.getQueueConnectionFactory(hostname, sid, port, driver);
            queueConnection = factory.createQueueConnection(userName, password);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return queueConnection;
    }

    public static AQjmsSession getSession(QueueConnection connection) {
        AQjmsSession session = null;
        try {
            session = (AQjmsSession) connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return session;
    }

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

//            String url = "jdbc:oracle:thin:@localhost:1521:xe";
//            Properties props = new Properties();
//            props.put("user", "sys");
//            props.put("password", "");
//            props.put("internal_logon", "sysdba");
//            QFac = AQjmsFactory.getQueueConnectionFactory(url, props);

            // create connection
            QCon = QFac.createQueueConnection(userName, password);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return QCon;
    }

    public static void createTable(AQjmsSession session, String userName, String tableName) {
        try {
            AQQueueTableProperty queueTableProperty = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            session.createQueueTable(userName, tableName, queueTableProperty);
            System.out.println("Queue Table \"" + tableName + "\" has been created");
            session.commit();
        } catch (AQException | JMSException e) {
            e.printStackTrace();
        }
    }

    public static void createQueue(AQjmsSession session, String userName, String tableName, String queueName) {
        try {
            AQQueueTable table = session.getQueueTable(userName, tableName);
            Queue queue = session.createQueue(table, queueName, new AQjmsDestinationProperty());
            ((AQjmsDestination) queue).start(session, true, true);
            System.out.println("Queue \"" + queue.getQueueName() +"\" has been created and started");
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(AQjmsSession session, String userName, String queueName, String message) {
        try {
            Queue queue = session.getQueue(userName, queueName);
            MessageProducer producer = session.createProducer(queue);
            TextMessage tMsg = session.createTextMessage(message);
            tMsg.setStringProperty("SOAPAction", "getQuote");
            producer.send(tMsg);
            System.out.println("Sent message = " + tMsg.getText());
            producer.close();
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static List<String> browseMessage(AQjmsSession session, String userName, String queueName) {
        List<String> list = new ArrayList<>();
        try {
            Queue queue = session.getQueue(userName, queueName);
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration enu = browser.getEnumeration();
            while (enu.hasMoreElements()) {
                TextMessage message = (TextMessage) enu.nextElement();
                list.add(message.getText());
            }
            browser.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void consumeMessage(AQjmsSession session, String userName, String queueName) {
        try {
            Queue queue = session.getQueue(userName, queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive();
            System.out.println("ONE MESSAGE RECEIVED " + msg.getText());
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

    public static void dropQueueTable(AQjmsSession session, String userName, String tableName) {
        try {
            AQQueueTable queueTable = session.getQueueTable (userName, tableName);
            queueTable.drop(true);
            System.out.println("Table \"" + userName + "." + tableName + "\" has been dropped successfully");
        } catch (JMSException | AQException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String userName = "jmsuser";
        String queueName = "sample_aq";
        String tableName = "sample_aqtbl";

        try {
            QueueConnection connection = getConnection();
            AQjmsSession session = (AQjmsSession) connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            connection.start();

//            dropQueueTable(session, userName, tableName);

//            AQQueueTable queueTable = createTable(session, userName, tableName);
//            Queue queue = createQueue(session, queueTable, queueName);

//            new AsyncConsumer().run(session, userName, queueName);
//            for (int i = 0; i < 100; i++) sendMessage(session, userName, queueName,"<user>text" + i + "</user>");

//            browseMessage(session, userName, queueName);
//            for (int i = 0; i < 110; i++) consumeMessage(session, userName, queueName);
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }



    }
}
