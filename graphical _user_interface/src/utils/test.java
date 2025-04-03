package utils;

import java.awt.event.*;
import javax.swing.JPanel;


import javax.swing.JFrame;

public class test {
    public static void main(String[] args) {
        // Tworzymy główne okno
        JFrame frame = new JFrame("Mouse & Keyboard Listener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        // Tworzymy nasz panel i dodajemy go do okna
        DrawWndPane panel = new DrawWndPane();
        frame.add(panel);

        // Umożliwiamy panelowi odbieranie zdarzeń klawiatury
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        // Pokazujemy okno
        frame.setVisible(true);
    }
}


// Klasa obsługująca zdarzenia myszy i klawiatury
class DrawWndPane extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    // Konstruktor: rejestracja słuchaczy zdarzeń
    DrawWndPane() {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    // Obsługa zdarzeń myszy
    public void mouseClicked(MouseEvent e) {
        System.out.println("mouseClicked at " + e.getX() + " " + e.getY());
    }

    public void mouseEntered(MouseEvent e) {
        System.out.println("mouseEntered at " + e.getX() + " " + e.getY());
    }

    public void mouseExited(MouseEvent e) {
        System.out.println("mouseExited at " + e.getX() + " " + e.getY());
    }

    public void mousePressed(MouseEvent e) {
        String which = (e.getButton() == MouseEvent.BUTTON1) ? "Button 1" :
                (e.getButton() == MouseEvent.BUTTON2) ? "Button 2" : "Button 3";
        System.out.println("mousePressed at " + e.getX() + " " + e.getY() + " " + which + " was pressed");
    }

    public void mouseReleased(MouseEvent e) {
        System.out.println("mouseReleased at " + e.getX() + " " + e.getY());
    }

    public void mouseDragged(MouseEvent e) {
        System.out.println("mouseDragged at " + e.getX() + " " + e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        System.out.println("mouseMoved to " + e.getX() + " " + e.getY());
    }

    // Obsługa zdarzeń klawiatury
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed Key code: " + e.getKeyCode() + " Char: " + e.getKeyChar());
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
    }

    public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped Char: " + e.getKeyChar());
    }
}



