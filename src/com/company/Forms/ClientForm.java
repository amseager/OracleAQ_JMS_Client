package com.company.Forms;

import com.company.AsyncConsumer;
import com.company.JsonSettings;
import com.company.OJMSClient;
import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.swing.*;
import java.awt.event.*;

public class ClientForm extends JPanel {
    private JButton btnSend;
    private JButton btnAsyncReceive;
    private JPanel mainPanel;
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
    private JTabbedPane tabbedPane1;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JPanel pnlConnection;
    private JButton btnBrowse;
    private JTextArea textArea1;
    private JButton btnCreateUser;

    private static QueueConnection connection;
    private static AQjmsSession session;
    private static boolean isConnected = false;

    private String jsonFilePath = "settings.json";

    public ClientForm() {
        loadPreviousOrDefaultSettings();

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    isConnected = true;
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Connected successfully");
                switchState(btnConnect, btnDisconnect, txtUser, txtPassword, txtHost, txtPort, txtSid, txtDriver);
                JsonSettings.saveSettings(getCurrentSettings(), jsonFilePath);
            }
        });
        btnDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    session.close();
                    connection.close();
                    isConnected = false;
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Disconnected");
                switchState(btnConnect, btnDisconnect, txtUser, txtPassword, txtHost, txtPort, txtSid, txtDriver);
            }
        });

        btnCreateUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SysPasswordForm uc = new SysPasswordForm(
                        txtHost.getText(),
                        txtSid.getText(),
                        txtPort.getText(),
                        txtDriver.getText());
                uc.pack();
                uc.setVisible(true);
            }
        });

        btnCreateTable.addActionListener(e -> OJMSClient.createTable(session, txtUser.getText(), txtTable.getText()));

        btnCreateQueue.addActionListener(e -> OJMSClient.createQueue(session, txtUser.getText(), txtTable.getText(), txtQueue.getText()));

        btnDrop.addActionListener(e -> OJMSClient.dropQueueTable(session, txtUser.getText(), txtTable.getText()));

        btnSend.addActionListener(e -> OJMSClient.sendMessage(session, txtUser.getText(), txtQueue.getText(),"<user>text</user>"));

        btnBrowse.addActionListener(e -> OJMSClient.browseMessage(session, txtUser.getText(), txtQueue.getText()));

        btnAsyncReceive.addActionListener(e -> new AsyncConsumer().run(session, txtUser.getText(), txtQueue.getText()));

        btnSyncReceive.addActionListener(e -> OJMSClient.consumeMessage(session, txtUser.getText(), txtQueue.getText()));

    }

    public static void switchState(JComponent... fields) {
        for (JComponent field: fields) {
            field.setEnabled(!field.isEnabled());
        }
    }

    private void loadPreviousOrDefaultSettings() {
        JsonSettings settings = JsonSettings.loadSettings(this.jsonFilePath);
        this.txtQueue.setText(settings.queueName);
        this.txtTable.setText(settings.queueTable);
        this.txtUser.setText(settings.userName);
        this.txtPassword.setText(settings.password);
        this.txtHost.setText(settings.host);
        this.txtPort.setText(settings.port);
        this.txtSid.setText(settings.sid);
        this.txtDriver.setText(settings.driver);
    }

    public void saveSettings() {
        JsonSettings.saveSettings(getCurrentSettings(), jsonFilePath);
    }

    private JsonSettings getCurrentSettings() {
        JsonSettings settings = new JsonSettings();
        settings.queueName = this.txtQueue.getText();
        settings.queueTable = this.txtTable.getText();
        settings.userName = this.txtUser.getText();
        settings.password = String.valueOf(this.txtPassword.getPassword());
        settings.host = this.txtHost.getText();
        settings.port = this.txtPort.getText();
        settings.sid = this.txtSid.getText();
        settings.driver = this.txtDriver.getText();
        return settings;
    }

    private static void openSaveSettingsForm(ClientForm clientForm, JFrame frame) {
        SaveSettingsForm saveSettingsForm = new SaveSettingsForm(clientForm, frame);
        saveSettingsForm.setLocationRelativeTo(null);
        saveSettingsForm.setAlwaysOnTop(true);
        saveSettingsForm.pack();
        saveSettingsForm.setVisible(true);
    }

    private static void createAndShowGUI() {
        ClientForm clientForm = new ClientForm();
        JFrame frame = new JFrame("Client");
        frame.setContentPane(clientForm.mainPanel);
        JRootPane rootPane = clientForm.mainPanel.getRootPane();

        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        rootPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSaveSettingsForm(clientForm, frame);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        rootPane.setDefaultButton(clientForm.btnConnect);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                openSaveSettingsForm(clientForm, frame);
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (isConnected) {
                        session.close();
                        connection.close();
                    }
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
