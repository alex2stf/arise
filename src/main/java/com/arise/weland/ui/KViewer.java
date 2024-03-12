package com.arise.weland.ui;


import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;

import static com.arise.core.tools.AppCache.getString;
import static com.arise.core.tools.AppCache.putString;
import static com.arise.core.tools.FileUtil.fileToString;

public class KViewer extends JFrame {
    private JMenuItem miNew;
    private JTabbedPane conxTabPane;
    private JMenu mEdit;
    private JMenuItem miExit;
    private JMenu mFile;
    private JMenuItem mLook;
    private JMenuBar menuBar;
    private JMenuItem miOpen;
    private JMenuItem miSaveAs;
    private JMenuItem miSave;

    private KDialog kDialog = null;

//    private Properties diagProps;

    private static final Mole log = Mole.getInstance(KViewer.class);

    public KViewer() {
        initComponents();
        loadLastIfExists();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        conxTabPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        mFile = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miOpen = new javax.swing.JMenuItem();
        miSave = new javax.swing.JMenuItem();
        miSaveAs = new javax.swing.JMenuItem();
        miExit = new javax.swing.JMenuItem();
        mEdit = new javax.swing.JMenu();
        mLook = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(700, 500));
        getContentPane().setLayout(new java.awt.CardLayout());
        getContentPane().add(conxTabPane, "card2");

        mFile.setText("File");

        miNew.setText("New");
        miNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuAction(evt);
            }
        });
        mFile.add(miNew);

        miOpen.setText("Open");
        miOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuActionPerformed(evt);
            }
        });
        mFile.add(miOpen);

        miSave.setText("Save");
        miSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        mFile.add(miSave);

        miSaveAs.setText("Save As");
        miSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuActionPerformed(evt);
            }
        });
        mFile.add(miSaveAs);

        miExit.setText("Exit");
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuActionPerformed(evt);
            }
        });
        mFile.add(miExit);

        menuBar.add(mFile);

        mEdit.setText("Edit");

        mLook.setText("Look & feel");
        mEdit.add(mLook);

        menuBar.add(mEdit);

        setJMenuBar(menuBar);

        pack();

    }



    private void newMenuAction(ActionEvent evt) {
        if (null == kDialog){
            kDialog = new KDialog(this, true, "New connection");
            kDialog.onClose(new Handler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    kDialog.setVisible(false);
                }
            });

            kDialog.onImport(new Handler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    JFileChooser fCh = new JFileChooser();
                    fCh.setCurrentDirectory(new File(getString("kvr", "/")));


                    int result = fCh.showOpenDialog(kDialog);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File fil = fCh.getSelectedFile();
                        String cnt = fileToString(fil);
                        kDialog.setTextAreaContent(cnt);
                        putString("kvr", fil.getParentFile().getAbsolutePath());
                        kDialog.setLabelText( "- " + fil.getAbsolutePath() + " -");
                    }
                }
            });

            kDialog.onOpen(new Handler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {

                    String content = kDialog.getTextAreaContent();
                    kDialog.setVisible(false);
                    addNewConnectionTab(
                            StringUtil.loadProps(content)
                    );
                }
            });

        }
        kDialog.setVisible(true);
        kDialog.setLabelText("");
    }

    private void addNewConnectionTab(Properties p) {
        String title = p.getProperty("kviewer.tab.title", "Kafka connection");
        KPanel kPanel = new KPanel(p);
        kPanel.setComponentPopupMenu(new KPopup());
//        kPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                kPanel.getComponentPopupMenu().show(kPanel, e.getX(), e.getY());
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                kPanel.getComponentPopupMenu().hide();
//            }
//        });
        conxTabPane.add(title,  kPanel);
        storeLastOpened(p);
    }



    private void storeLastOpened(Properties p) {
        FileUtil.saveProps(p, new File(FileUtil.findAppDir(), "last.kvr"));
    }

    private void loadLastIfExists() {
        File l = new File(FileUtil.findAppDir(), "last.kvr");
        if (l.exists()){
            try {
                addNewConnectionTab(
                        FileUtil.loadProps(l)
                );
            } catch (Exception e){
                log.e("Exception in loading file " + l.getAbsolutePath(), e);
            }
        }
    }




    private void openMenuActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        JFileChooser fCh = new JFileChooser();
        fCh.setCurrentDirectory(new File(getString("kvropn", "/")));
        int res = fCh.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File fil = fCh.getSelectedFile();
            putString("kvropn", fil.getParentFile().getAbsolutePath());

            Properties props = FileUtil.loadProps(fil);
            addNewConnectionTab(props);
        }
    }

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void saveAsMenuActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fCh = new JFileChooser();
        fCh.setDialogTitle("Save as");
        fCh.setCurrentDirectory(new File(getString("kvrsvs", "/")));
        int res = fCh.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File fil = fCh.getSelectedFile();

            Component c = conxTabPane.getSelectedComponent();
            System.out.println(c);
            putString("kvrsvs", fil.getParentFile().getAbsolutePath());

            if(c instanceof KPanel){
                KPanel p = (KPanel) c;
                FileUtil.saveProps(p.getProps(), fil);
            }
        }
    }

    private void exitMenuActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }


    public static void main(String[] args) {
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(KViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(KViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(KViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(KViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                KViewer kViewer = new KViewer();
                kViewer.setVisible(true);
            }
        });
    }
}