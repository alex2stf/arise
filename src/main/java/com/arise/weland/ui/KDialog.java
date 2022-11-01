package com.arise.weland.ui;


import com.arise.core.models.Handler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

public class KDialog extends JDialog {

    private String title;


    /**
     * Creates new form ConnectDialog
     */
    public KDialog(java.awt.Frame parent, boolean modal, String title) {
        super(parent, modal);
        this.title = title;
        initComponents();
    }

    private Handler<ActionEvent> hclose;
    private Handler<ActionEvent> hopen;
    private Handler<ActionEvent> himport;


    public KDialog onImport(Handler<ActionEvent> himport){
        this.himport = himport;
        return this;
    }

    public KDialog onOpen(Handler<ActionEvent> hopen){
        this.hopen = hopen;
        return this;
    }

    public KDialog onClose(Handler<ActionEvent> hclose){
        this.hclose = hclose;
        return this;
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblStat = new javax.swing.JLabel();
        btnContainer = new javax.swing.JPanel();
        btnImport = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnTest = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        scrlPane = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Some title");
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0};
        layout.rowHeights = new int[] {1, 0, 0};
        layout.columnWeights = new double[] {1.0};
        layout.rowWeights = new double[] {1.0, 0.0, 0.0};
        getContentPane().setLayout(layout);

        lblStat.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblStat.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        getContentPane().add(lblStat, gridBagConstraints);

        btnContainer.setBackground(new java.awt.Color(255, 255, 204));
        btnContainer.setLayout(new java.awt.GridLayout(1, 2, 4, 2));

        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trigger(evt, himport);
            }
        });
        btnContainer.add(btnImport);

        btnOpen.setText("Open");
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trigger(evt, hopen);
            }
        });
        btnContainer.add(btnOpen);

        btnTest.setText("Test connection");
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });
        btnContainer.add(btnTest);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trigger(evt, hclose);
            }
        });
        btnContainer.add(btnClose);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        getContentPane().add(btnContainer, gridBagConstraints);

        txtArea.setColumns(20);
        txtArea.setRows(5);
        scrlPane.setViewportView(txtArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(scrlPane, gridBagConstraints);

        setTitle(title);
        pack();
    }// </editor-fold>

    private void trigger(ActionEvent e, Handler<ActionEvent> h) {
        h.handle(e);
    }






    private void btnTestActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(KDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(KDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(KDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(KDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                KDialog dialog = new KDialog(new JFrame(), true, "xxx");
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private JButton btnClose;
    private JPanel btnContainer;
    private JButton btnImport;
    private JButton btnOpen;
    private JButton btnTest;
    private JScrollPane scrlPane;
    private JTextArea txtArea;
    private JLabel lblStat;

    public String getTextAreaContent() {
        if (txtArea != null){
            return txtArea.getText();
        }
        return "";
    }

    public void setTextAreaContent(String content) {
        if (txtArea != null){
            txtArea.setText(content);
        }
    }


    public KDialog setLabelText(String txt){
        lblStat.setText(txt);
        return this;
    }
    // End of variables declaration             
}
