package com.company.Forms;

import com.company.Utils;

import javax.swing.*;
import java.awt.event.*;

public class SysPasswordForm extends JDialog {
    private JPanel contentPane;
    private JButton btnOK;
    private JButton btnCancel;
    private JPasswordField txtSysPassword;
    private JLabel lblSysPassword;

    public SysPasswordForm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnOK);
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);

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

    private void onOK() {
// add your code here
        Utils.sysConnection = Utils.connectAsSys(String.valueOf(txtSysPassword.getPassword()));
        dispose();
        NewUserForm newUserForm = new NewUserForm();
        newUserForm.pack();
        newUserForm.setVisible(true);
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SysPasswordForm dialog = new SysPasswordForm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
