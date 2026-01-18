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
 * Ajustada para reflejar cambios dinámicos tras la eliminación y evitar errores de visualización.
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
            // Tamaño amplio para evitar que el árbol se corte por los bordes
            this.setPreferredSize(new Dimension(2500, 1000));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (raiz != null) {
                Graphics2D g2 = (Graphics2D) g;
                // Suavizado de bordes para una mejor apariencia visual (Antialiasing)
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Inicia la recursión desde el centro superior de la pantalla
                dibujarNodo(g2, raiz, getWidth() / 2, 50, getWidth() / 5);
            }
        }

        private void dibujarNodo(Graphics2D g, Nodo nodo, int x, int y, int rangoHorizontal) {
            // CORRECCIÓN CLAVE: Usar el tamaño real de la lista de claves (.size())
            // Esto evita errores cuando un nodo queda con menos de 'd' claves tras eliminar
            int numClaves = nodo.claves.size();
            int anchoTotalNodo = numClaves * ANCHO_CELDA;
            int inicioX = x - (anchoTotalNodo / 2);

            // 1. DIBUJO DE LAS CELDAS Y LAS CLAVES (VALORES)
            for (int i = 0; i < numClaves; i++) {
                int celdaX = inicioX + (i * ANCHO_CELDA);
                
                // Dibujar el fondo del cuadro (gris claro)
                g.setColor(new Color(245, 245, 245));
                g.fillRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);
                
                // Dibujar el borde negro
                g.setColor(Color.BLACK);
                g.drawRect(celdaX, y, ANCHO_CELDA, ALTO_NODO);

                // Dibujar el texto centrado dentro de cada celda
                String texto = String.valueOf(nodo.claves.get(i));
                FontMetrics fm = g.getFontMetrics();
                int textoX = celdaX + (ANCHO_CELDA - fm.stringWidth(texto)) / 2;
                int textoY = y + (ALTO_NODO - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(texto, textoX, textoY);
            }

            // 2. DIBUJO DE LOS HIJOS Y SUS LÍNEAS CONECTORAS
            if (!nodo.esHoja) {
                // Usamos el tamaño real de la lista de hijos para mayor robustez
                int numHijos = nodo.hijos.size();
                
                for (int i = 0; i < numHijos; i++) {
                    Nodo hijoActual = nodo.hijos.get(i);
                    
                    // Solo dibujamos si el hijo existe (evita errores si una referencia se perdió)
                    if (hijoActual != null) {
                        // Cálculo matemático para distribuir los hijos proporcionalmente en el espacio
                        int hijoX = x - rangoHorizontal + (i * 2 * rangoHorizontal / Math.max(1, numHijos - 1));
                        int hijoY = y + ESPACIO_VERTICAL;

                        // Dibujar línea gris desde la base del padre al hijo
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawLine(x, y + ALTO_NODO, hijoX, hijoY);

                        // Llamada recursiva: reducimos el rango horizontal para que los nietos no se encimen
                        dibujarNodo(g, hijoActual, hijoX, hijoY, (int)(rangoHorizontal * 0.55));
                    }
                }
            }
        }
    }
}