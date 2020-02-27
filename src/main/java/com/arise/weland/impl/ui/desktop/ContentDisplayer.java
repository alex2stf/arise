package com.arise.weland.impl.ui.desktop;

import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.WelandClient;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;
import com.arise.weland.impl.ContentInfoProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.arise.weland.impl.ui.desktop.WelandFrame.DARK_BLUE;
import static com.arise.weland.impl.ui.desktop.WelandFrame.LIGHT_BLUE;
import static com.arise.weland.impl.ui.desktop.WelandFrame.SMALL_FONT;

/**
 * @author alexandru2.stefan
 */
public class ContentDisplayer extends javax.swing.JPanel {

    final Object worker;
    private final Playlist playlist;
    int SIZE = 200;
    int MAX_COLUMNS = 3;
    Dimension DIMENSION = new Dimension(SIZE, SIZE);
    Integer nextIndex = 0;
    volatile boolean isFetching = false;
    List<ContentIcon> icons = new ArrayList<>();
    ContentInfoProvider contentInfoProvider;
    private GridLayout gridLayout = new java.awt.GridLayout();

    // Variables declaration - do not modify
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField searchTextArea;
    private javax.swing.JToolBar toolBar;

    public ContentDisplayer(URI worker, Playlist playlist, ContentInfoProvider contentInfoProvider) {
        this.worker = worker;
        this.playlist = playlist;
        this.contentInfoProvider = contentInfoProvider;
        initComponents();
    }

    private void initComponents() {
        gridLayout.setColumns(MAX_COLUMNS);
        gridLayout.setRows(MAX_COLUMNS);
        gridLayout.setHgap(20);
        gridLayout.setVgap(20);
        java.awt.GridBagConstraints gridBagConstraints;

        toolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        searchTextArea = new javax.swing.JTextField();
        scrollPane = new javax.swing.JScrollPane();
        container = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        jLabel1.setText("Search:");
        jLabel1.setFont(SMALL_FONT);
        jLabel1.setForeground(Color.WHITE);
        toolBar.add(jLabel1);

        searchTextArea.setText("text to search");
        searchTextArea.setBackground(LIGHT_BLUE);
        searchTextArea.setFont(SMALL_FONT);
        toolBar.add(searchTextArea);
        toolBar.setBackground(DARK_BLUE);
        toolBar.setBorderPainted(false);
        toolBar.setForeground(Color.WHITE);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        add(toolBar, gridBagConstraints);

        container.setLayout(gridLayout);


        scrollPane.setViewportView(container);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        add(scrollPane, gridBagConstraints);



        setBackground(DARK_BLUE);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                fetchData();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                fetchData();
            }


        });

        searchTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println(e.getKeyChar());
                super.keyReleased(e);
            }
        });
    }

    private void fetchData() {

        if (isFetching) {
            return;
        }

        isFetching = true;
        if (nextIndex == null) {
            return;
        }

        WelandClient.mediaList(worker, playlist.name(), nextIndex, new CompleteHandler<ContentPage>() {
            @Override
            public void onComplete(ContentPage data) {
                nextIndex = data.getIndex();
                addMediaIcons(data.getData());
                isFetching = false;
            }
        }, new CompleteHandler() {
            @Override
            public void onComplete(Object data) {
                System.out.println(data);
                isFetching = false;
            }
        });
    }

    void callFromEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private void addMediaIcons(final List<ContentInfo> data) {
        callFromEdt(new Runnable() {
            @Override
            public void run() {
                for (ContentInfo info : data) {
                    addMediaIcon(info);
                }
            }
        });
    }

    int numRows = MAX_COLUMNS;
    private void addMediaIcon(ContentInfo info) {
        ContentIcon button = new ContentIcon(info, this);
        button.setPreferredSize(DIMENSION);
        button.setSize(DIMENSION);
        icons.add(button);
        container.add(button);
        if (icons.size() % MAX_COLUMNS == 0){
            numRows++;
            gridLayout.setRows(numRows);
        }
//        gridLayout.setColumns(MAX_COLUMNS);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                fetchData();
            }
        });
        container.revalidate();
        container.repaint();
    }
}
