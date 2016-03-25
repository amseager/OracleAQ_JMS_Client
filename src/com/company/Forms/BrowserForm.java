package com.company.Forms;

import com.company.OJMSClient;
import oracle.jms.AQjmsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class BrowserForm extends JDialog {
    private static final Logger log = LoggerFactory.getLogger(BrowserForm.class);

    private JPanel contentPane;
    private JTextArea txaBrowser;
    private JLabel lblTotalRows;
    private JScrollPane scrBrowser;

    BrowserForm(AQjmsSession session, String userName, String queueName) {
        setContentPane(contentPane);
        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Queue Browser");

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        txaBrowser.setEditable(false);
        txaBrowser.setRows(15);
        txaBrowser.setColumns(30);

        List<String> messages = OJMSClient.browseMessage(session, userName, queueName);
        lblTotalRows.setText("Total rows: " + messages.size());
        for (String message: messages) {
            txaBrowser.append(message + "\n");
        }

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
