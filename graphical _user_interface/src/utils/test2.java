package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class test2 {
    public static void main(String[] args) {
        SmpWindow wnd = new SmpWindow();
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setVisible(true);
        wnd.setBounds(70, 70, 450, 300);
        wnd.setTitle("Obsługa przycisków w Swing");
    }
}

// Panel z przyciskami i obsługa kliknięć
class ButtonPane extends JPanel implements ActionListener {
    private JButton button1, button2, button3;
    private String message;

    // Konstruktor - inicjalizacja przycisków i ustawienia panelu
    ButtonPane() {
        setLayout(null); // Wyłączamy automatyczne rozmieszczanie elementów

        // Tworzenie przycisków
        button1 = new JButton("1");
        button2 = new JButton("2");
        button3 = new JButton("3");

        // Ustawienie pozycji i rozmiaru przycisków
        button1.setBounds(100, 100, 70, 30);
        button2.setBounds(190, 100, 70, 30);
        button3.setBounds(280, 100, 70, 30);

        // Dodanie przycisków do panelu
        add(button1);
        add(button2);
        add(button3);

        // Rejestracja wspólnego nasłuchiwacza zdarzeń
        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);

        // Domyślny komunikat
        message = "Żaden przycisk nie został jeszcze naciśnięty";
    }

    // Rysowanie komunikatu na panelu
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString(message, 130, 150);
    }

    // Obsługa kliknięcia w przycisk
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource(); // Sprawdzenie, który przycisk został naciśnięty

        if (source == button1)
            message = "Kliknięto przycisk 1";
        else if (source == button2)
            message = "Kliknięto przycisk 2";
        else
            message = "Kliknięto przycisk 3";

        repaint(); // Odświeżenie panelu, aby wyświetlić nowy komunikat
    }
}

// Klasa głównego okna JFrame
class SmpWindow extends JFrame {
    public SmpWindow() {
        // Dodanie panelu z przyciskami do okna
        getContentPane().add(new ButtonPane());
    }
}
