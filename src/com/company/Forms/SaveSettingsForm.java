package com.company.Forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;

public class SaveSettingsForm extends JDialog {
    private static final Logger log = LoggerFactory.getLogger(SaveSettingsForm.class);

    private JPanel contentPane;
    private JButton btnYes;
    private JButton btnCancel;
    private JButton btnNo;
    private JLabel lblSaveSettings;

    SaveSettingsForm(ClientForm clientForm, JFrame frame) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnYes);
        setAlwaysOnTop(true);
        btnYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clientForm.saveSettings();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                dispose();
                frame.dispose();
            }
        });

        btnNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                dispose();
                frame.dispose();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
