package com.arise.weland.ui;

import com.arise.core.tools.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalogClock extends JFrame  implements MouseListener, MouseMotionListener {

    private static final int MAX_MINUTES = 60;
    private static final int MAX_HOURS = 12;
    private int screenX = 0;
    private int screenY = 0;
    private int compX = 0;
    private int compY = 0;


    public AnalogClock() {
        JPanel contentPane = new Circle();
        setLayout(new CardLayout());
        setContentPane(contentPane);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addMouseListener(this);
        addMouseMotionListener(this);

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }


    public static void main(String[] args) {
        new AnalogClock();
    }

    static BasicStroke BS0 = new BasicStroke(0);
    static BasicStroke BS1 = new BasicStroke(1);
    static BasicStroke BS2 = new BasicStroke(2);
    static BasicStroke BS3 = new BasicStroke(3);
    static BasicStroke BS4 = new BasicStroke(4);
    static BasicStroke BS5 = new BasicStroke(5);

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        screenX = e.getXOnScreen();
        screenY = e.getYOnScreen();
        compX = getX();
        compY = getY();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dX = e.getXOnScreen() - screenX;
        int dY = e.getYOnScreen() - screenY;
        setLocation(compX + dX, compY + dY);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private class Circle extends JPanel {
        int rad = 250;
        int pad = 10;
        int size;
        int center;
        int radius;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        Point[] puncteSecundar = new Point[MAX_MINUTES];
        Point[] puncteMinutar = new Point[MAX_MINUTES];
        Point[] minuteOvalPoints = new Point[MAX_MINUTES];
        Point[] puncteOrar = new Point[MAX_HOURS];

        Point[] oreLinieFrom = new Point[MAX_HOURS];
        Point[] oreLinieTo = new Point[MAX_HOURS];
        Point[] orePoz = new Point[MAX_HOURS];
        int secunda;
        int minutul;
        int ora = 0;
        int height;
        Date now = new Date();

        public Circle(){
            size = rad + pad * 2;
            height = size + (size / 6);
            setPreferredSize(new Dimension(size, height));
            center = (size / 2); //+ 20;// (oRad / 2);
            radius = rad / 2; // e ok

            for(int i = 0; i < MAX_MINUTES; i++) {
                int startPoint = i - (MAX_MINUTES / 4);
                double angle = startPoint * (2 * Math.PI / MAX_MINUTES);
                int x = (int) ((center) + (radius) * Math.cos(angle));
                int y = (int) ((center) + (radius) * Math.sin(angle));

                int ax = (int) ((center) + (radius / 1.3) * Math.cos(angle));
                int ay = (int) ((center) + (radius / 1.3) * Math.sin(angle));

                int mx = (int) ((center) + (radius / 2 ) * Math.cos(angle));
                int my = (int) ((center) + (radius / 2) * Math.sin(angle));
                puncteSecundar[i] = new Point(x, y);
                minuteOvalPoints[i] = new Point(ax, ay);
                puncteMinutar[i] = new Point(mx, my);
            }

            for(int i = 0; i < MAX_HOURS; i++) {
                int startPoint = i - 3;

                double angle = (startPoint * (2 * Math.PI / MAX_HOURS));
                int fx = (int) ((center) + (radius / 1.2) * Math.cos(angle));
                int fy = (int) ((center) + (radius / 1.2) * Math.sin(angle));

                int tx = (int) ((center) + (radius / 1.5) * Math.cos(angle));
                int ty = (int) ((center) + (radius / 1.5) * Math.sin(angle));

                int ox = (int) ((center) + (radius / 3) * Math.cos(angle));
                int oy = (int) ((center) + (radius / 3) * Math.sin(angle));


                int x = (int) (center + radius * Math.cos(angle));
                int y = (int) (center + radius * Math.sin(angle));

                oreLinieFrom[i] = new Point(fx, fy);
                oreLinieTo[i] = new Point(tx, ty);
                orePoz[i] = new Point(x, y);
                puncteOrar[i] = new Point(ox, oy);
            }


            refreshValues();
            ThreadUtil.repeatedTask(new Runnable() {
                @Override
                public void run() {
                    refreshValues();
                    repaint();
                }
            }, 1000);


        }

        void refreshValues(){
            now = new Date();
            secunda = now.getSeconds();
            minutul = now.getMinutes();
            ora = now.getHours() - 12;
        }




        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.clearRect(0, 0, size, height);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillOval(0, 0, size, size);
            // Set the color for the circle shape
            g2d.setColor(Color.BLUE);

            g2d.setStroke(BS3);

            int off = g2d.getFont().getSize() / 3;



            //deseneaza cadran cu orele
            for(int i = 0; i < MAX_HOURS; i++) {
                g2d.setColor(Color.RED);
                int val = i == 0 ? 12 : i;
                g2d.drawString(val + "", orePoz[i].x - off, orePoz[i].y + off);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(oreLinieFrom[i].x, oreLinieFrom[i].y, oreLinieTo[i].x, oreLinieTo[i].y);
            }

            g2d.setStroke(BS1);
            //deseneaza cadran cu minutele
            for(int i = 0; i < MAX_MINUTES; i++) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(minuteOvalPoints[i].x, minuteOvalPoints[i].y, 2, 2);
            }

            g2d.setStroke(BS1);


            Point secundaAt = puncteSecundar[secunda];
            g2d.drawLine(center, center, secundaAt.x, secundaAt.y);


            g2d.setStroke(BS2);
            Point minutulAt = puncteMinutar[minutul];

            g2d.setStroke(BS3);
            g2d.setColor(Color.CYAN);
            g2d.drawLine(center, center, minutulAt.x, minutulAt.y);

            Point oraAt = puncteOrar[ora];

            g2d.setColor(Color.GREEN);
            g2d.drawLine(center, center, oraAt.x, oraAt.y);





            g2d.drawString(sdf.format(now), pad, height - (height / 12));
        }
    }


}
