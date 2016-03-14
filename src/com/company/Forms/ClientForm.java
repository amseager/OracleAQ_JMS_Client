package com.company.Forms;

import com.company.AsyncConsumer;
import com.company.JsonSettings;
import com.company.OJMSClient;
import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.swing.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.List;

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
    private JTabbedPane tbdPane;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JPanel pnlConnection;
    private JButton btnBrowse;
    private JButton btnCreateUser;
    private JTextArea txaBrowser;
    private JScrollPane scrBrowser;
    private JPanel pnlBrowser;
    private JLabel lblTotalRows;
    private JSpinner spnSend;
    private JButton btnStopAsyncReceive;

    private static QueueConnection connection;
    private static AQjmsSession session;
    private static boolean isConnected = false;

    private String jsonFilePath = "settings.json";
    private String TOTAL_ROWS_STRING = "Total rows: ";

    int tempMessageNumber = 0;

    private AsyncConsumer asyncConsumer = new AsyncConsumer();

    public ClientForm() {
        loadPreviousOrDefaultSettings();

        spnSend.setModel(new SpinnerNumberModel(10, 1, 1000, 1));

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

                // Initialize browser
                List<String> messages = OJMSClient.browseMessage(session, txtUser.getText(), txtQueue.getText());
                lblTotalRows.setText(TOTAL_ROWS_STRING + messages.size());
                txaBrowser.setText("");
                for (String message: messages) {
                    txaBrowser.append(message + "\n");
                }
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

                lblTotalRows.setText(TOTAL_ROWS_STRING + "0");
                txaBrowser.setText("");
            }
        });

        btnCreateUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SysPasswordForm sysPasswordForm = new SysPasswordForm(
                        txtHost.getText(),
                        txtSid.getText(),
                        txtPort.getText(),
                        txtDriver.getText());
                sysPasswordForm.pack();
                sysPasswordForm.setVisible(true);
            }
        });

        btnCreateTable.addActionListener(e -> OJMSClient.createTable(session, txtUser.getText(), txtTable.getText()));

        btnCreateQueue.addActionListener(e -> OJMSClient.createQueue(session, txtUser.getText(), txtTable.getText(), txtQueue.getText()));

        btnDrop.addActionListener(e -> OJMSClient.dropQueueTable(session, txtUser.getText(), txtTable.getText()));

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spnSend.commitEdit();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                for (int i = 0; i < (int) spnSend.getValue(); i++) {
                    String newMessage = "<user>" + (++tempMessageNumber) + "</user>";
                    OJMSClient.sendMessage(session, txtUser.getText(), txtQueue.getText(), newMessage);
                    txaBrowser.append(newMessage + "\n");
                }
                int prevNumRows = Integer.parseInt(lblTotalRows.getText().substring(TOTAL_ROWS_STRING.length(), lblTotalRows.getText().length()));
                lblTotalRows.setText(TOTAL_ROWS_STRING + (prevNumRows + (int) spnSend.getValue()));
            }
        });

        btnBrowse.addActionListener(e -> new BrowserForm(session, txtUser.getText(), txtQueue.getText()).setVisible(true));

        btnAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asyncConsumer.run(session, txtUser.getText(), txtQueue.getText());
            }
        });

        btnStopAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asyncConsumer.close();
            }
        });

        btnSyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> msgList = OJMSClient.browseMessage(session, txtUser.getText(), txtQueue.getText());
                int size = msgList.size();
                if (size > 0) {
                    lblTotalRows.setText(TOTAL_ROWS_STRING + (size - 1));
                    txaBrowser.setText("");
                    for (int i = 1; i < size; i++) {
                        txaBrowser.append(msgList.get(i) + "\n");
                    }
                    OJMSClient.consumeMessage(session, txtUser.getText(), txtQueue.getText());
                }
            }
        });

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
//        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        rootPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();        //temporary
//                openSaveSettingsForm(clientForm, frame);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        rootPane.setDefaultButton(clientForm.btnConnect);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
//                openSaveSettingsForm(clientForm, frame);
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
        clientForm.btnConnect.doClick();    //temporary
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
