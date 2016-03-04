package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client extends JPanel{
    private JButton sendButton;
    private JButton asyncReceiveButton;
    private JPanel mainPanel;
    private JScrollPane output;
    private JButton syncReceiveButton;

    private AQjmsSession session;
    private String userName = "jmsuser";
    private String queueName = "sample_aq";
    private String queueTableName = "sample_aqtbl";

    public Client() {
        try {
            QueueConnection connection = OJMSClient.getConnection();
            this.session = (AQjmsSession) connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OJMSClient.sendMessage(session, userName, queueName,"<user>text</user>");
            }
        });

        asyncReceiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AsyncConsumer().run(session, userName, queueName);
            }
        });

        syncReceiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OJMSClient.consumeMessage(session, userName, queueName);
            }
        });
    }


    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Client");
        frame.setContentPane(new Client().mainPanel);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
