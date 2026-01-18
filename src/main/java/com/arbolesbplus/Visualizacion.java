package com.arbolesbplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Clase encargada de la representación gráfica del Árbol B+.
 * Ajustada para reflejar cambios dinámicos tras la eliminación.
 * @author Athenea
 */
public class Visualizacion {

    public static void visualizar(Nodo raiz) {
        JFrame frame = new JFrame("Visualización Árbol B+ (Corregido)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 700); 

        PanelArbol panel = new PanelArbol(raiz);
        // El scroll permite navegar si el árbol crece mucho horizontalmente
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    /**
     * Subclase interna que gestiona el dibujo de los nodos y líneas.
     */
    static class PanelArbol extends JPanel {
        private Nodo raiz;
        private final int ANCHO_CELDA = 35; // Espacio para cada clave
        private final int ALTO_NODO = 30;   // Altura del rectángulo del nodo
        private final int ESPACIO_VERTICAL = 80; // Distancia entre niveles

        public PanelArbol(Nodo raiz) {
            this.raiz = raiz;
            this.setBackground(Color.WHITE);
            // Tamaño amplio para evitar que el árbol se corte
            this.setPreferredSize(new Dimension(2500, 1000));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (raiz != null) {
                Graphics2D g2 = (Graphics2D) g;
                // Suavizado de bordes para una mejor apariencia visual
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Inicia la recursión desde el centro superior
                dibujarNodo(g2, raiz, getWidth() / 2, 50, getWidth() / 5);
            }
        }

        private void dibujarNodo(Graphics2D g, Nodo nodo, int x, int y, int rangoHorizontal) {
            // Se usa .size() en lugar de .d para soportar nodos con pocas claves tras borrar
            int numClaves = nodo.claves.size();
            int anchoTotalNodo = numClaves * ANCHO_CELDA;
            int inicioX = x - (anchoTotalNodo / 2);

            // 1. DIBUJO DE LAS CELDAS Y CLAVES
            for (int i = 0; i < numClaves; i++) {
                int celdaX = inicioX + (i * ANCHO_CELDA);
                
                // Dibujar el fondo del cuadro de la clave
                g.setColor(new Color(245, 245, 245));
                g.fillRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);
                
                // Dibujar el borde
                g.setColor(Color.BLACK);
                g.drawRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);

                // Dibujar el texto centrado en la celda
                String texto = String.valueOf(nodo.claves.get(i));
                FontMetrics fm = g.getFontMetrics();
                int textoX = celdaX + (ANCHO_CELDA - fm.stringWidth(texto)) / 2;
                int textoY = y + (ALTO_NODO - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(texto, textoX, textoY);
            }

            // 2. DIBUJO DE HIJOS Y CONECTORES
            if (!nodo.esHoja) {
                int numHijos = nodo.hijos.size();
                
                for (int i = 0; i < numHijos; i++) {
                    if (nodo.hijos.get(i) != null) {
                        // Cálculo de la posición X del hijo basándose en el rango disponible
                        int hijoX = x - rangoHorizontal + (i * 2 * rangoHorizontal / Math.max(1, numHijos - 1));
                        int hijoY = y + ESPACIO_VERTICAL;

                        // Línea gris que une el padre con el hijo
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawLine(x, y + ALTO_NODO, hijoX, hijoY);

                        // Llamada recursiva para procesar el siguiente nivel
                        dibujarNodo(g, nodo.hijos.get(i), hijoX, hijoY, (int)(rangoHorizontal * 0.55));
                    }
                }
            }
        }
    }
}