package com.company.Forms;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class NewUserForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtUserName;
    private JPasswordField txtPassword;
    private JLabel lblUserName;
    private JLabel lblPassword;

    private Connection sysConnection;

    public NewUserForm(Connection sysConnection) {
        this.sysConnection = sysConnection;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setTitle("Create new user");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
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

    private void createUser(Connection sysConnection, String userName, String password) {
        try {
            Statement statement = sysConnection.createStatement();
            statement.execute("Grant connect, resource TO " + userName + " IDENTIFIED BY " + password);
            statement.execute("Grant aq_user_role TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqadm TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aq TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqin TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqjms TO " + userName);
            System.out.println("User " + userName + " has been created");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onOK() {
        createUser(sysConnection, txtUserName.getText(), String.valueOf(txtPassword.getPassword()));
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
