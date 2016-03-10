package com.company.Forms;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

public class SysPasswordForm extends JDialog {
    private JPanel contentPane;
    private JButton btnOK;
    private JButton btnCancel;
    private JPasswordField txtSysPassword;
    private JLabel lblSysPassword;
    private JLabel lblIncorrectPassword;

    private String host;
    private String sid;
    private String port;
    private String driver;

    public SysPasswordForm(String host, String sid, String port, String driver) {
        this.host = host;
        this.sid = sid;
        this.port = port;
        this.driver = driver;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnOK);
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setTitle("Connect as SYS");

        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private Connection connectAsSys(String password) throws SQLException {
        Locale.setDefault(Locale.ENGLISH);
//        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String url = "jdbc:oracle:" + this.driver + ":@" + this.host + ":" + this.port + ":" + this.sid;
        Properties props = new Properties();
        props.put("user", "sys");
        props.put("password", password);
        props.put("internal_logon", "sysdba");
        return DriverManager.getConnection(url, props);
    }

    private void onOK() {
        try {
            Connection sysConnection = connectAsSys(String.valueOf(txtSysPassword.getPassword()));
            System.out.println("Connected as SYS");
            dispose();
            NewUserForm newUserForm = new NewUserForm(sysConnection);
            newUserForm.pack();
            newUserForm.setVisible(true);
        } catch (SQLException e) {
            lblIncorrectPassword.setText("Incorrect password");
        }
    }

    private void onCancel() {
        dispose();
    }
}
