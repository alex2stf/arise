package com.arise.weland.ui;


import com.arise.core.models.Handler;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.StringUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;

public class KPanel extends JPanel {



    private final KProxy kProxy;

    public Properties getProps() {
        return props;
    }

    private Properties props;

    public KPanel(Properties props) {
        kProxy = new KProxy(props);
        this.props = props;
        kProxy.scanConsumer();
        initComponents();
        addEventsAndModel();
    }

    private void addEventsAndModel() {
        treePane.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                System.out.println(e);
                TreeSelectionModel selectionModel = treePane.getSelectionModel();
                TreePath[] paths = e.getPaths();
                for (TreePath path : paths) {
                    Object node = path.getLastPathComponent();
                    int cnt = 0;
                    boolean isRoot = false;
                    Object usrObj = node;
                    String parentTxt = "";
                    if (node instanceof DefaultMutableTreeNode){
                        DefaultMutableTreeNode dfn = (DefaultMutableTreeNode) node;
                        cnt = dfn.getChildCount();
                        isRoot = dfn.isRoot();
                        usrObj = dfn.getUserObject();
                        if (dfn.getParent() != null && dfn.getParent() instanceof DefaultMutableTreeNode){
                            parentTxt = ((DefaultMutableTreeNode)dfn.getParent()).getUserObject() + "";
                        }
                        System.out.println(dfn);
                    }


                    if (selectionModel.isPathSelected(path)) {
                        if ("root".equals(usrObj) && isRoot){
                            System.out.println("e root");
                            infoTxtArea.setText(kProxy.getConnectionDetails());
                        } else if ("topics".equals(usrObj) && cnt > 0){
                            System.out.println("e root topics");
                        } else {

                            if ("topics".equals(parentTxt)){
                                setSelectedTopic(usrObj);
                            }

                            if ("brokers".equals(parentTxt)){
                                setSelectedBroker(usrObj + "");
                            }


                        }
                    }
                }
            }
        });

        treePane.setModel(kProxy.getTreeModel());


        btnCreateTopic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topicName = txtTopicName.getText();
                kProxy.createTopic(topicName, StringUtil.loadProps(txtTopic.getText()), new Handler<String>() {
                    @Override
                    public void handle(String s) {
                        infoTxtArea.setText(s);
                    }
                });
                AppCache.putString("kvr.lastTopic", topicName);
                kProxy.scanConsumer();
                treePane.setModel(kProxy.getTreeModel());
            }
        });
    }




    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        leftPanel = new JPanel();
        treeScrollPane = new JScrollPane();
        treePane = new JTree();
        watchPanel = new JPanel();
        startConsumeBtn = new JButton();
        tScrPane = new JScrollPane();
        tTextArea = new JTextArea();
        lblStat = new JLabel();
        rightPanel = new JPanel();
        scrIntoTextArea = new JScrollPane();
        infoTxtArea = new JTextArea();
        panCnt = new JPanel();
        scrlTxt = new JScrollPane();
        txtTopic = new JTextArea();
        txtTopicName = new JTextField();
        btnCreateTopic = new JButton();

        setLayout(new java.awt.GridLayout(1, 0));

        leftPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 20));

        treeScrollPane.setViewportView(treePane);

        leftPanel.add(treeScrollPane);

        watchPanel.setBackground(new java.awt.Color(255, 204, 153));
        watchPanel.setLayout(new java.awt.GridBagLayout());

        startConsumeBtn.setText("Start Watch");
        startConsumeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startConsumeBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.1;
        watchPanel.add(startConsumeBtn, gridBagConstraints);

        tScrPane.setBackground(new java.awt.Color(255, 255, 204));

        tTextArea.setColumns(20);
        tTextArea.setRows(5);
        tTextArea.setText("");
        tScrPane.setViewportView(tTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        watchPanel.add(tScrPane, gridBagConstraints);

        lblStat.setBackground(new java.awt.Color(102, 102, 255));
        lblStat.setText("------");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        watchPanel.add(lblStat, gridBagConstraints);

        leftPanel.add(watchPanel);

        add(leftPanel);

        rightPanel.setLayout(new java.awt.GridLayout(2, 0, 4, 4));

        infoTxtArea.setColumns(20);
        infoTxtArea.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        infoTxtArea.setRows(5);
        infoTxtArea.setText("zona de preview mesaj");
        scrIntoTextArea.setViewportView(infoTxtArea);

        rightPanel.add(scrIntoTextArea);

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 4, 0};
        jPanel1Layout.rowHeights = new int[] {0, 1, 0};
        jPanel1Layout.columnWeights = new double[] {1.0};
        jPanel1Layout.rowWeights = new double[] {1.0, 3.0, 1.0};
        panCnt.setLayout(jPanel1Layout);

        txtTopic.setColumns(20);
        txtTopic.setRows(5);
        txtTopic.setText("num.partitions=1\nreplication.factor=1");
        scrlTxt.setViewportView(txtTopic);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 357;
        gridBagConstraints.ipady = 69;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        panCnt.add(scrlTxt, gridBagConstraints);

        txtTopicName.setText(AppCache.getString("kvr.lastTopic", "my-topic"));
        txtTopicName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                txtTopicNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 462;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        panCnt.add(txtTopicName, gridBagConstraints);

        btnCreateTopic.setText("Create Topic");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        panCnt.add(btnCreateTopic, gridBagConstraints);

        rightPanel.add(panCnt);

        add(rightPanel);
    }// </editor-fold>


    private void setSelectedBroker(String brokerName) {
        infoTxtArea.setText(kProxy.getBrokerDetails(brokerName));
    }

    private void setSelectedTopic(Object node) {
        lblStat.setText(node + "");
        infoTxtArea.setText(kProxy.getTopicDetails(node + ""));
    }

    private void startConsumeBtnActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:

        String topic = lblStat.getText().trim();

        if(kProxy.isReading()){
            kProxy.stopRead();
            startConsumeBtn.setText("Start consume");
        }
        else {
            kProxy.readTopic(topic, new Handler<Map<String, Object>>() {
                @Override
                public void handle(Map<String, Object> x) {
                    tTextArea.append(StringUtil.join(x) + "\n\n");
                }
            });
            startConsumeBtn.setText("Stop consume");
        }


    }





    // Variables declaration - do not modify                     
    private JTextArea infoTxtArea;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JScrollPane scrIntoTextArea;
    private JButton startConsumeBtn;
    private JScrollPane tScrPane;
    private JTextArea tTextArea;
    private JTree treePane;
    private JScrollPane treeScrollPane;
    private JPanel watchPanel;

    private JButton btnCreateTopic;
    private JLabel lblStat;
    private JPanel panCnt;
    private JScrollPane scrlTxt;
    private JTextArea txtTopic;
    private JTextField txtTopicName;
    // End of variables declaration
}

