package com.company;

import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Client extends JPanel{
    private JButton btnSend;
    private JButton btnAsyncReceive;
    private JPanel mainPanel;
    private JScrollPane output;
    private JButton btnSyncReceive;
    private JButton btnDrop;
    private JTextField txtTable;
    private JTextField txtQueue;
    private JLabel lblTable;
    private JLabel lblQueue;
    private JButton btnCreateTable;
    private JButton btnCreateQueue;
    private JTextField txtUser;
    private JLabel lblHost;
    private JTextField txtHost;
    private JLabel lblPort;
    private JTextField txtPort;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblSid;
    private JTextField txtSid;
    private JTextField txtDriver;
    private JLabel lblDriver;
    private JLabel lblUser;

    private static QueueConnection connection;
    private static AQjmsSession session;

    public Client() {

        try {
            connection = OJMSClient.getConnection(
                    txtHost.getText(),
                    txtSid.getText(),
                    Integer.parseInt(txtPort.getText()),
                    txtDriver.getText(),
                    txtUser.getText(),
                    String.valueOf(txtPassword.getPassword())
            );
            connection.start();
            session = OJMSClient.getSession(connection);

        } catch (JMSException e) {
            e.printStackTrace();
        }

        btnCreateTable.addActionListener(e -> OJMSClient.createTable(session, txtUser.getText(), txtTable.getText()));

        btnCreateQueue.addActionListener(e -> OJMSClient.createQueue(session, txtUser.getText(), txtTable.getText(), txtQueue.getText()));

        btnDrop.addActionListener(e -> OJMSClient.dropQueueTable(session, txtUser.getText(), txtTable.getText()));

        btnSend.addActionListener(e -> OJMSClient.sendMessage(session, txtUser.getText(), txtQueue.getText(),"<user>text</user>"));

        btnAsyncReceive.addActionListener(e -> new AsyncConsumer().run(session, txtUser.getText(), txtQueue.getText()));

        btnSyncReceive.addActionListener(e -> OJMSClient.consumeMessage(session, txtUser.getText(), txtQueue.getText()));
    }


    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Client");
        frame.setContentPane(new Client().mainPanel);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    session.close();
                    connection.close();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }
        });
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
