package com.company.Forms;

import com.company.AsyncConsumer;
import com.company.JsonSettings;
import com.company.OJMSClient;
import com.company.Producer;
import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private JPanel pnlThreads;
    private JSplitPane splThreads;
    private JScrollPane scrConsumer;
    private JScrollPane scrConsumerOutput;
    private JSpinner spnThreads;
    private JList<String> lstConsumer;
    private JTextArea txtConsumerOutput;
    private JSpinner spnThreadLatency;
    private JPanel pnlThreadsMenu;
    private JLabel lblReceivedMessage;
    private JList<String> lstBrowser;
    private JScrollPane scrBrowser2;

    private static ClientForm clientForm;

    private static QueueConnection connection;
    private static AQjmsSession mainSession;
    private static boolean isConnected = false;

    private String jsonFilePath = "settings.json";
    private final String TOTAL_ROWS_STRING = "Total rows: ";
    private final String RECEIVED_MESSAGE_STRING = "Last received message: ";

    private AtomicInteger msgNumber = new AtomicInteger();
    private DefaultListModel<String> listConsumerModel = new DefaultListModel<>();
    private DefaultListModel<String> listBrowserModel = new DefaultListModel<>();


    private List<AsyncConsumer> threads = new ArrayList<>();

    public JSpinner getSpnThreadLatency() {
        return spnThreadLatency;
    }

    public static ClientForm getClientForm() {
        return clientForm;
    }

    public synchronized void appendConsumerOutputIfRowIsSelected(String expectedThreadName, String message) {
        int index = lstConsumer.getSelectedIndex();
        if (index > -1) {
            String realThreadName = threads.get(index).getName();
            if (expectedThreadName.equals(realThreadName)) {
                txtConsumerOutput.append(message + "\n");
            }
        }
    }

    public synchronized void refreshBrowser(String message) {
        listBrowserModel.removeElement(message);
    }

    public ClientForm() {
        loadPreviousOrDefaultSettings();

        ((DefaultCaret) txtConsumerOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        spnSend.setModel(new SpinnerNumberModel(20, 1, null, 1));
        spnThreads.setModel(new SpinnerNumberModel(1, 1, null, 1));
        spnThreadLatency.setModel(new SpinnerNumberModel(1000, 0, null, 100));
        tbdPane.setSelectedIndex(1);
        lstBrowser.setModel(listBrowserModel);
        lstConsumer.setModel(listConsumerModel);
        lstConsumer.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = lstConsumer.getSelectedIndex();
                txtConsumerOutput.setText("");
                if (index > -1) {
                    List<String> messageList = threads.get(index).getMessageList();
                    for (String message : messageList) {
                        txtConsumerOutput.append(message + "\n");
                    }
                }
            }
        });


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
                    mainSession = OJMSClient.getSession(connection);
                    isConnected = true;
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Connected successfully");
                switchState(btnConnect, btnDisconnect, txtUser, txtPassword, txtHost, txtPort, txtSid, txtDriver);
                JsonSettings.saveSettings(getCurrentSettings(), jsonFilePath);

                // Initialize browser
                List<String> messageList = OJMSClient.browseMessage(mainSession, txtUser.getText(), txtQueue.getText());
                lblTotalRows.setText(TOTAL_ROWS_STRING + messageList.size());
                txaBrowser.setText("");
                for (String message: messageList) {
                    txaBrowser.append(message + "\n");
                    listBrowserModel.addElement(message);
                }
            }
        });

        btnDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mainSession.close();
                    connection.close();
                    isConnected = false;
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Disconnected");
                switchState(btnConnect, btnDisconnect, txtUser, txtPassword, txtHost, txtPort, txtSid, txtDriver);

                lblTotalRows.setText(TOTAL_ROWS_STRING + "0");
                txaBrowser.setText("");
                listBrowserModel.clear();
            }
        });

        btnCreateUser.addActionListener(e -> new SysPasswordForm(txtHost.getText(), txtSid.getText(), txtPort.getText(), txtDriver.getText()));

        btnCreateTable.addActionListener(e -> OJMSClient.createTable(mainSession, txtUser.getText(), txtTable.getText()));

        btnCreateQueue.addActionListener(e -> OJMSClient.createQueue(mainSession, txtUser.getText(), txtTable.getText(), txtQueue.getText()));

        btnDrop.addActionListener(e -> OJMSClient.dropQueueTable(mainSession, txtUser.getText(), txtTable.getText()));

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spnSend.commitEdit();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
//                String newMessage = "<user>" + (++msgNumber) + "</user>";
                AQjmsSession threadSession = OJMSClient.getSession(connection);
                Producer producer = new Producer(threadSession, txtUser.getText(), txtQueue.getText(), msgNumber, (int) spnSend.getValue());
                producer.start();
//                    txaBrowser.append(newMessage + "\n");
//                    listBrowserModel.addElement(newMessage);
//                    lstBrowser.setSelectedIndex(0);
              //  }
                lblTotalRows.setText(TOTAL_ROWS_STRING + listBrowserModel.getSize());
            }
        });

        btnBrowse.addActionListener(e -> new BrowserForm(mainSession, txtUser.getText(), txtQueue.getText()));

        btnAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spnThreads.commitEdit();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                int numberOfThreads = (int) spnThreads.getValue();

                for (int i = 0; i < numberOfThreads; i++) {
                    AQjmsSession threadSession = OJMSClient.getSession(connection);
                    AsyncConsumer asyncConsumer = new AsyncConsumer(threadSession, txtUser.getText(), txtQueue.getText());
                    listConsumerModel.addElement(asyncConsumer.getName());
                    threads.add(asyncConsumer);
                    asyncConsumer.start();
                }
            }
        });

        btnStopAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listConsumerModel.getSize() > 0) {
                    int index = lstConsumer.getSelectedIndex();
                    System.out.println("shutdown " + threads.get(index).getName());
                    threads.get(index).shutdown();
                    threads.remove(index);
                    listConsumerModel.remove(index);
                    int size = listConsumerModel.getSize();
                    if (size > 0) {
                        if (index == size) {
                            index--;
                        }
                        lstConsumer.setSelectedIndex(index);
                        lstConsumer.ensureIndexIsVisible(index);
                    }
                }
            }
        });

        btnSyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> msgList = OJMSClient.browseMessage(mainSession, txtUser.getText(), txtQueue.getText());
                int size = msgList.size();
                if (size > 0) {
                    OJMSClient.consumeMessage(mainSession, txtUser.getText(), txtQueue.getText());
                    lblTotalRows.setText(TOTAL_ROWS_STRING + (size - 1));
                    lblReceivedMessage.setText(RECEIVED_MESSAGE_STRING + msgList.get(0));
                    txaBrowser.setText("");
                    for (int i = 1; i < size; i++) {
                        txaBrowser.append(msgList.get(i) + "\n");
                    }
                    txaBrowser.setCaretPosition(0);

                    listBrowserModel.remove(0);
                    lstBrowser.setSelectedIndex(0);
                    lstBrowser.ensureIndexIsVisible(0);
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
        new SaveSettingsForm(clientForm, frame);
    }

    private static void createAndShowGUI() {
        clientForm = new ClientForm();
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
                        mainSession.close();
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
