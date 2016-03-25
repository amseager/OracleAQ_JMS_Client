package com.company;

import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class OJMSClient {
    private static final Logger log = LoggerFactory.getLogger(OJMSClient.class);

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

    public static void createTable(AQjmsSession session, String userName, String tableName) {
        try {
            AQQueueTableProperty queueTableProperty = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            session.createQueueTable(userName, tableName, queueTableProperty);
            log.info("Queue Table \"" + tableName + "\" has been created");
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
            log.info("Queue \"" + queue.getQueueName() +"\" has been created and started");
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
            log.info("Sent message = " + tMsg.getText());
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
            log.debug("ONE MESSAGE RECEIVED " + msg.getText());
            consumer.close();
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void dropQueueTable(AQjmsSession session, String userName, String tableName) {
        try {
            AQQueueTable queueTable = session.getQueueTable (userName, tableName);
            queueTable.drop(true);
            log.info("Table \"" + userName + "." + tableName + "\" has been dropped");
        } catch (JMSException | AQException e) {
            e.printStackTrace();
        }
    }
}
