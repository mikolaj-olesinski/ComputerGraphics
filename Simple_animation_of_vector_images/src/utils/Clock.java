package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.Scanner;

import java.awt.*;
import java.awt.geom.*;
//import java.awt.Color;
//import java.awt.BasicStroke;
//import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Polygon;

import java.lang.Thread;
import java.lang.InterruptedException;

import javax.swing.*;

public class Clock {

    public static void main(String[] args)
    {
        SmpWindow wnd = new SmpWindow();

        // Closing window terminates the program
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(70, 70, 300, 300);
        wnd.setVisible(true);

        while (true)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                System.out.println("Interrupted");
            }
            wnd.repaint();
        }
    }

}

class DrawWndPane extends JPanel
{

    final int GAUGE_LEN = 10;
    int  center_x, center_y;
    int  r_outer, r_inner;
    GregorianCalendar calendar;


    DrawWndPane()
    {
        super();
        setBackground( new Color( 200, 200, 255) );
        calendar = new GregorianCalendar();
    }

    public void DrawGauge( double angle, Graphics g )
    {
        int xw, yw, xz, yz;

        angle = 3.1415 * angle / 180.0;
        xw = (int)(center_x + r_inner * Math.sin( angle ));
        yw = (int)(center_y - r_inner * Math.cos( angle ));
        xz = (int)(center_x + r_outer * Math.sin( angle ));
        yz = (int)(center_y - r_outer * Math.cos( angle ));

        g.drawLine( xw, yw, xz, yz );
    }

    public void DrawHand( double angle, int length, Graphics g )
    {
        int xw, yw, xz, yz;

        angle = 3.1415 * angle / 180.0;
        xw = (int)(center_x + length * Math.sin( angle ));
        yw = (int)(center_y - length * Math.cos( angle ));

        angle += 3.1415;
        xz = (int)(center_x + GAUGE_LEN * Math.sin( angle ));
        yz = (int)(center_y - GAUGE_LEN * Math.cos( angle ));

        g.drawLine( xw, yw, xz, yz );
    }

    public void DrawDial( Graphics g )
    {
        g.drawOval(  center_x - r_outer,
                center_y - r_outer,
                2*r_outer, 2*r_outer );

        for ( int i = 0; i <= 11; i++ )
            DrawGauge( i * 30.0, g );
    }

    public void paint( Graphics g )
    {
        paintComponent( g );
    }

    public void paintComponent( Graphics g )
    {
        int  minute, second, hour;

        super.paintComponent(g);
        Graphics2D  g2d = (Graphics2D)g;

        Dimension size = getSize();

        center_x = size.width/2;
        center_y = size.height/2;
        r_outer = Math.min( size.width, size.height)/2;
        r_inner = r_outer - GAUGE_LEN;

        Date time = new Date();
        calendar.setTime( time );

        minute = calendar.get( Calendar.MINUTE );
        hour   = calendar.get( Calendar.HOUR );
        if ( hour > 11 )
            hour = hour - 12;
        second = calendar.get( Calendar.SECOND );

        DrawDial( g );

        g2d.setColor( new Color( 255, 0, 0 ) );
        g2d.setStroke( new BasicStroke( 5 ) );
        DrawHand( 360.0 * (hour * 60 + minute) / ( 60.0 * 12 ) , (int)(0.75 * r_inner), g);

        g2d.setColor( new Color( 255, 0, 0 ) );
        g2d.setStroke( new BasicStroke( 3 ) );
        DrawHand( 360.0 * (minute * 60 + second )/( 3600.0), (int)(0.97 * r_outer), g);

        g2d.setColor( new Color( 0, 0, 0 ) );
        g2d.setStroke( new BasicStroke( 1 ) );
        DrawHand( second * 6.0, (int)(0.97 * r_inner), g);
    }
}

class SmpWindow extends JFrame
{
    public SmpWindow()
    {
        Container  contents = getContentPane();
        contents.add( new DrawWndPane() );
        setTitle( "Clock");
    }
}