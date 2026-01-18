/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.arbolesbplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Athenea
 */
public class Visualizacion {
   public static void visualizar(Nodo raiz) {
        JFrame frame = new JFrame("Visualización Árbol B (Corregido)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 700); // Ventana un poco más ancha

        PanelArbol panel = new PanelArbol(raiz);
        // Añadimos un JScrollPane por si el árbol crece mucho horizontalmente
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    static class PanelArbol extends JPanel {
        private Nodo raiz;
        private final int ANCHO_CELDA = 30; // Ancho de cada "cuadrito" de clave
        private final int ALTO_NODO = 30;
        private final int ESPACIO_VERTICAL = 80;

        public PanelArbol(Nodo raiz) {
            this.raiz = raiz;
            this.setBackground(Color.WHITE);
            // Definimos un tamaño preferido grande para activar el scroll si es necesario
            this.setPreferredSize(new Dimension(2000, 1000));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (raiz != null) {
                // Usamos Graphics2D para líneas más suaves (Antialiasing)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Empezamos en el centro del ancho del panel (o del visor)
                dibujarNodo(g2, raiz, getWidth() / 2, 50, getWidth() / 5);
            }
        }

        private void dibujarNodo(Graphics2D g, Nodo nodo, int x, int y, int rangoHorizontal) {
            int anchoTotalNodo = nodo.d * ANCHO_CELDA;
            int inicioX = x - (anchoTotalNodo / 2);

            // 1. Dibujar el Nodo (Rectángulos y Claves)
            for (int i = 0; i < nodo.d; i++) {
                int celdaX = inicioX + (i * ANCHO_CELDA);
                
                // Rectángulo de la clave
                g.setColor(new Color(240, 240, 240));
                g.fillRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);
                g.setColor(Color.BLACK);
                g.drawRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);

                // Texto de la clave
                String texto = String.valueOf(nodo.claves.get(i));
                FontMetrics fm = g.getFontMetrics();
                int textoX = celdaX + (ANCHO_CELDA - fm.stringWidth(texto)) / 2;
                int textoY = y + (ALTO_NODO - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(texto, textoX, textoY);
            }

            // 2. Dibujar Hijos recursivamente
            if (!nodo.esHoja) {
                // Calculamos cuántos hijos hay para distribuir el espacio
                int numHijos = nodo.d + 1;
                
                for (int i = 0; i < numHijos; i++) {
                    if (nodo.hijos.get(i) != null) {
                        // Lógica de separación: calculamos hijoX basándonos en el rango horizontal disponible
                        int hijoX = x - rangoHorizontal + (i * 2 * rangoHorizontal / (numHijos - 1));
                        int hijoY = y + ESPACIO_VERTICAL;

                        // Línea conectora
                        g.setColor(Color.GRAY);
                        // La línea sale de la base del nodo hacia el hijo
                        g.drawLine(x, y + ALTO_NODO, hijoX, hijoY);

                        // Llamada recursiva reduciendo el rango para el siguiente nivel
                        // Usamos un factor de 0.6 para que no se agote tan rápido el espacio
                        dibujarNodo(g, nodo.hijos.get(i), hijoX, hijoY, (int)(rangoHorizontal * 0.55));
                    }
                }
            }
        }
    }
}
