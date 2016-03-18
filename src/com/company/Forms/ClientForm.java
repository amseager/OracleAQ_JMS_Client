package com.company.Forms;

import com.company.*;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private JPanel pnlBrowser;
    private JLabel lblTotalRowsTitle;
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
    private JLabel lblReceivedMessageTitle;
    private JList<String> lstBrowser;
    private JScrollPane scrBrowser;
    private JLabel lblTotalRowsValue;
    private JLabel lblReceivedMessageValue;

    private static ClientForm form;

    private static QueueConnection connection;
    private static AQjmsSession mainSession;
    private static boolean isConnected = false;

    private String jsonFilePath = "settings.json";

    private AtomicInteger msgNumber = new AtomicInteger();
    private DefaultListModel<String> listModelConsumer = new DefaultListModel<>();

    public DefaultListModel<String> getListModelBrowser() {
        return listModelBrowser;
    }

    public JLabel getLblTotalRowsValue() {
        return lblTotalRowsValue;
    }

    private volatile DefaultListModel<String> listModelBrowser = new DefaultListModel<>();

    private List<AsyncConsumer> threads = new ArrayList<>();

    public JSpinner getSpnThreadLatency() {
        return spnThreadLatency;
    }

    public static ClientForm getForm() {
        return form;
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
//        if (listModelBrowser.getElementAt(0).equals(message)) {
//            listModelBrowser.removeElementAt(0);
//            listModelBrowser.removeElement(message);
//        }
    }

    private static void commitSpinners(JSpinner... spinners) {
        for (JSpinner spinner: spinners) {
            try {
                spinner.commitEdit();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private ClientForm() {
        spnSend.setModel(new SpinnerNumberModel(1, 1, null, 1));
        spnThreads.setModel(new SpinnerNumberModel(1, 1, null, 1));
        spnThreadLatency.setModel(new SpinnerNumberModel(1, 0, null, 100));

        loadPreviousOrDefaultSettings();

        ((DefaultCaret) txtConsumerOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        tbdPane.setSelectedIndex(1);
        lstBrowser.setModel(listModelBrowser);
        lstConsumer.setModel(listModelConsumer);
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
                lblTotalRowsValue.setText(String.valueOf(messageList.size()));
                for (String message: messageList) {
                    listModelBrowser.addElement(message);
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

                lblTotalRowsValue.setText("0");
                listModelBrowser.clear();
            }
        });

        btnCreateUser.addActionListener(e -> new SysPasswordForm(txtHost.getText(), txtSid.getText(), txtPort.getText(), txtDriver.getText()));

        btnCreateTable.addActionListener(e -> OJMSClient.createTable(mainSession, txtUser.getText(), txtTable.getText()));

        btnCreateQueue.addActionListener(e -> OJMSClient.createQueue(mainSession, txtUser.getText(), txtTable.getText(), txtQueue.getText()));

        btnDrop.addActionListener(e -> OJMSClient.dropQueueTable(mainSession, txtUser.getText(), txtTable.getText()));

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commitSpinners(spnSend);
                int msgCount = (int) spnSend.getValue();
                AQjmsSession sendSession = OJMSClient.getSession(connection);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                MessageProducer producer = null;
                try {
                    Queue queue = sendSession.getQueue(txtUser.getText(), txtQueue.getText());
                    producer = sendSession.createProducer(queue);
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
                Sender sender = new Sender(sendSession, producer, msgNumber, msgCount);
                executor.execute(sender);
            }
        });

        btnBrowse.addActionListener(e -> new BrowserForm(mainSession, txtUser.getText(), txtQueue.getText()));

        btnAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commitSpinners(spnThreads, spnThreadLatency);
                int numberOfThreads = (int) spnThreads.getValue();

                for (int i = 0; i < numberOfThreads; i++) {
                    AQjmsSession threadSession = OJMSClient.getSession(connection);
                    AsyncConsumer asyncConsumer = new AsyncConsumer(threadSession, txtUser.getText(), txtQueue.getText());
                    listModelConsumer.addElement(asyncConsumer.getName());
                    threads.add(asyncConsumer);
                    asyncConsumer.start();
                }
            }
        });

        btnStopAsyncReceive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listModelConsumer.getSize() > 0) {
                    int index = lstConsumer.getSelectedIndex();
                    System.out.println("shutdown " + threads.get(index).getName());
                    threads.get(index).shutdown();
                    threads.remove(index);
                    listModelConsumer.remove(index);
                    int size = listModelConsumer.getSize();
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
                    lblTotalRowsValue.setText(String.valueOf(size - 1));
                    lblReceivedMessageValue.setText(msgList.get(0));

                    listModelBrowser.remove(0);
                    lstBrowser.setSelectedIndex(0);
                    lstBrowser.ensureIndexIsVisible(0);
                }
            }
        });

    }

    private static void switchState(JComponent... fields) {
        for (JComponent field: fields) {
            field.setEnabled(!field.isEnabled());
        }
    }

    private void loadPreviousOrDefaultSettings() {
        JsonSettings settings = JsonSettings.loadSettings(this.jsonFilePath);
        txtQueue.setText(settings.queueName);
        txtTable.setText(settings.queueTable);
        txtUser.setText(settings.userName);
        txtPassword.setText(settings.password);
        txtHost.setText(settings.host);
        txtPort.setText(settings.port);
        txtSid.setText(settings.sid);
        txtDriver.setText(settings.driver);
        spnSend.setValue(settings.sendCount);
        spnThreads.setValue(settings.threadsCount);
        spnThreadLatency.setValue(settings.threadLatency);
    }

    void saveSettings() {
        JsonSettings.saveSettings(getCurrentSettings(), jsonFilePath);
    }

    private JsonSettings getCurrentSettings() {
        JsonSettings settings   = new JsonSettings();
        settings.queueName      = txtQueue.getText();
        settings.queueTable     = txtTable.getText();
        settings.userName       = txtUser.getText();
        settings.password       = String.valueOf(txtPassword.getPassword());
        settings.host           = txtHost.getText();
        settings.port           = txtPort.getText();
        settings.sid            = txtSid.getText();
        settings.driver         = txtDriver.getText();
        settings.sendCount      = (Integer) spnSend.getValue();
        settings.threadsCount   = (Integer) spnThreads.getValue();
        settings.threadLatency  = (Integer) spnThreadLatency.getValue();
        return settings;
    }

    private static void openSaveSettingsForm(ClientForm clientForm, JFrame frame) {
        new SaveSettingsForm(clientForm, frame);
    }

    private static void createAndShowGUI() {
        form = new ClientForm();
        JFrame frame = new JFrame("Client");
        frame.setContentPane(form.mainPanel);
        JRootPane rootPane = form.mainPanel.getRootPane();

        frame.setAlwaysOnTop(true);
//        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        rootPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commitSpinners(form.spnSend, form.spnThreads, form.spnThreadLatency);
                form.saveSettings();  //temporary
                frame.dispose();        //temporary
//                openSaveSettingsForm(form, frame);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        rootPane.setDefaultButton(form.btnConnect);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                commitSpinners(form.spnSend, form.spnThreads, form.spnThreadLatency);
                form.saveSettings();  //temporary
                // openSaveSettingsForm(form, frame);
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
        form.btnConnect.doClick();    //temporary
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
