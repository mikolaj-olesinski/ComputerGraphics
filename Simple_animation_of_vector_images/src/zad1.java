import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import javax.swing.*;

public class zad1 {
    public static void main(String[] args) {
        // Create the window of the clock
        ClockWindow wnd = new ClockWindow();

        // Closing window terminates the program
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the initial position of the window on the screen
        // and make the window visible
        wnd.setBounds(70, 70, 300, 900);
        wnd.setVisible(true);

        // Start the infinite loop of animation.
        // The program will run until the clock window is closed
        while (true) {
            try {
                // Wait a second before the clock is redisplayed
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Program interrupted");
            }
            // Redraw the clock according to current time
            wnd.repaint();
        }
    }
}

// ===============================================================
// ClockPane class implements the content pane of the window
// in which the clock is displayed
// ===============================================================

class ClockPane extends JPanel {
    // The length of the tick mark
    final int TICK_LEN = 10;

    // Coordinates of the clock dial center
    int center_x, center_y;

    // Radiuses of the inner and outer circle enclosing the dial
    int r_outer, r_inner;

    // The calendar object - will be used to acquire the current
    // hour, minute and second
    GregorianCalendar calendar;

    ClockPane() {
        super();
        setBackground(new Color(200, 200, 255));
        calendar = new GregorianCalendar();
    }

    // This method draws the single tick mark on the clock dial
    public void DrawTickMark(double angle, Graphics g) {
        int xw, yw, xz, yz;

        angle = Math.PI * angle / 180.0;

        // The tick is drawn as a line segment
        xw = (int) (center_x + r_inner * Math.sin(angle));
        yw = (int) (center_y - r_inner * Math.cos(angle));
        xz = (int) (center_x + r_outer * Math.sin(angle));
        yz = (int) (center_y - r_outer * Math.cos(angle));

        g.drawLine(xw, yw, xz, yz);
    }

    // The method draws the clock hand. The hand angle and length
    // are specified by the method arguments
    public void DrawHand(double angle, int length, Graphics g) {
        int xw, yw, xz, yz;

        // Convert the angle from degrees to radians
        angle = Math.PI * angle / 180.0;

        // Use this angle to find the hand outer end
        xw = (int) (center_x + length * Math.sin(angle));
        yw = (int) (center_y - length * Math.cos(angle));

        // Complement the angle and find the hand inner end
        angle += Math.PI;
        xz = (int) (center_x + TICK_LEN * Math.sin(angle));
        yz = (int) (center_y - TICK_LEN * Math.cos(angle));

        g.drawLine(xw, yw, xz, yz);
    }

    // This method draws the circular dial of the clock
    public void DrawDial(Graphics g) {
        g.drawOval(center_x - r_outer, center_y - r_outer, 2 * r_outer, 2 * r_outer);

        // Draw tick mark at location corresponding to
        // hours 1 .. 12
        for (int i = 0; i <= 11; i++)
            DrawTickMark(i * 30.0, g);
    }

    public void DrawPendulum(double angle, int length, Graphics g) {
        int xw, yw, xz, yz;

        //Convert the angle from degrees to radians
        angle = Math.PI * angle / 180.0;

       // Beginning of pendulum line
        xw = center_x;
        yw = center_y + r_outer;

        // Calculate pendulum radius based on length
        int pendulumRadius = (int)(0.03 * length);
        // Adjust length to account for pendulum radius
        length -= pendulumRadius;

        //Calculate end of pendulum line
        xz = (int) (xw + length * Math.sin(angle));
        yz = (int) (yw + length * Math.cos(angle));

        //Draw the pendulum line
        g.drawLine(xw, yw, xz, yz);

        //Draw the pendulum weight
        g.fillOval(xz - pendulumRadius, yz - pendulumRadius, 2 * pendulumRadius, 2 * pendulumRadius);
    }


    // The complete drawing procedure in implemented in
    // paintComponent method, so it will be refreshed
    // automatically when necessary and also the repaint
    // of the clock can be forced by calling repaint()
    // method of the window.
    public void paintComponent(Graphics g) {
        int minute, second, hour, milisecond;

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Get actual window size in order to compute
        // basic clock dimensions
        Dimension size = getSize();

        // Find out the radiuses of inner and outer dial rings
        r_outer = Math.min(size.width, size.height / 3) / 2;
        r_inner = r_outer - TICK_LEN;

        // Calculate the position of the dial center
        center_x = size.width / 2;
        center_y = r_outer;


        // Acquire the current time
        Date time = new Date();

        // Convert it to hours/minutes/seconds
        calendar.setTime(time);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);
        if (hour > 11)
            hour = hour - 12;
        second = calendar.get(Calendar.SECOND);
        milisecond = calendar.get(Calendar.MILLISECOND);

        // Draw the dial with tick marks
        DrawDial(g);

        // Set the color and the line style for hour hand
        g2d.setColor(new Color(255, 0, 0));
        g2d.setStroke(new BasicStroke(5));
        // Draw the hour hand
        DrawHand(360.0 * (hour * 60 + minute) / (60.0 * 12), (int) (0.75 * r_inner), g);

        // Set the color and the line style for the minute hand
        g2d.setColor(new Color(255, 0, 0));
        g2d.setStroke(new BasicStroke(3));
        DrawHand(360.0 * (minute * 60 + second) / (3600.0), (int) (0.97 * r_outer), g);

        // Finally draw the second hand
        g2d.setColor(new Color(0, 0, 0));
        g2d.setStroke(new BasicStroke(1));
        DrawHand(second * 6.0, (int) (0.97 * r_inner), g);


        //Initial pendulum parameters
        double amplitude = 60;
        double period = 10.0;
        double timeInSeconds = second + (milisecond / 1000.0);

        //Calculate pendulum angle
        double pendulumAngle = amplitude * Math.cos(2 * Math.PI * timeInSeconds / period);

        //Draw the pendulum
        DrawPendulum(pendulumAngle, r_outer * 4, g);
    }
}

// ==============================================================
// ClockWindow class implements the window containing the clock
// ==============================================================

class ClockWindow extends JFrame {
    public ClockWindow() {
        setContentPane(new ClockPane());
        setTitle("Clock");
    }
}
