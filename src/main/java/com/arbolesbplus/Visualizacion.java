package com.arbolesbplus;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

public class Visualizacion {

    public static void visualizar(Nodo raiz) {
        if (raiz == null) {
            JOptionPane.showMessageDialog(null, "El árbol está vacío.");
            return;
        }
        
        JFrame frame = new JFrame("Visualizador B+ Pro (Zoom & Pan)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 800);

        PanelArbolInteractivo panel = new PanelArbolInteractivo(raiz);
        frame.add(panel);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class PanelArbolInteractivo extends JPanel {
        private Nodo raiz;
        private final int CELDA_ANCHO = 45;
        private final int CELDA_ALTO = 35;
        private final int ESP_VERTICAL = 120;
        
        // Variables para Transformación (Zoom y Pan)
        private double escala = 1.0;
        private double translateX = 0;
        private double translateY = 0;
        private Point puntoPresionado;

        private Map<Nodo, Point> posiciones = new HashMap<>();

        public PanelArbolInteractivo(Nodo raiz) {
            this.raiz = raiz;
            this.setBackground(new Color(25, 25, 30));

            // --- EVENTOS DE MOUSE PARA ZOOM Y ARRASTRE ---
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    puntoPresionado = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (puntoPresionado != null) {
                        translateX += e.getX() - puntoPresionado.x;
                        translateY += e.getY() - puntoPresionado.y;
                        puntoPresionado = e.getPoint();
                        repaint();
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double factorZoom = (e.getWheelRotation() < 0) ? 1.1 : 0.9;
                    escala *= factorZoom;
                    // Limitar zoom para no perderse
                    escala = Math.max(0.1, Math.min(escala, 3.0));
                    repaint();
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
            addMouseWheelListener(mouseHandler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            // Aplicar suavizado
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // --- APLICAR TRANSFORMACIÓN DE ZOOM Y PAN ---
            AffineTransform at = new AffineTransform();
            at.translate(translateX, translateY);
            at.scale(escala, escala);
            g2.transform(at);

            // Calcular posiciones (puedes ajustar el ancho inicial según el tamaño del árbol)
            posiciones.clear();
            calcularPosiciones(raiz, 0, 100, 2000); 

            dibujarConexiones(g2, raiz);
            dibujarEnlacesHojas(g2);
            
            for (Map.Entry<Nodo, Point> entry : posiciones.entrySet()) {
                dibujarNodo(g2, entry.getKey(), entry.getValue().x, entry.getValue().y);
            }
            
            // Dibujar leyenda fija (opcional)
            dibujarInterfazUI(g);
        }

        private void calcularPosiciones(Nodo nodo, int xInicio, int y, int ancho) {
            if (nodo == null) return;
            int xCentro = xInicio + ancho / 2;
            posiciones.put(nodo, new Point(xCentro, y));

            if (!nodo.esHoja) {
                int numHijos = nodo.hijos.size();
                for (int i = 0; i < numHijos; i++) {
                    int nuevoAncho = ancho / numHijos;
                    calcularPosiciones(nodo.hijos.get(i), xInicio + i * nuevoAncho, y + ESP_VERTICAL, nuevoAncho);
                }
            }
        }

        private void dibujarNodo(Graphics2D g2, Nodo nodo, int xCentro, int y) {
            int n = nodo.claves.size();
            int totalAncho = n * CELDA_ANCHO;
            int xInicio = xCentro - totalAncho / 2;

            // Fondo del nodo
            g2.setColor(nodo.esHoja ? new Color(46, 204, 113) : new Color(52, 152, 219));
            g2.fillRoundRect(xInicio, y, totalAncho, CELDA_ALTO, 10, 10);
            
            // Celdas
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.2f));
            for (int i = 0; i < n; i++) {
                int cx = xInicio + i * CELDA_ANCHO;
                g2.drawRect(cx, y, CELDA_ANCHO, CELDA_ALTO);
                
                String clave = String.valueOf(nodo.claves.get(i));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(clave, cx + (CELDA_ANCHO - fm.stringWidth(clave))/2, y + 23);
            }
        }

        private void dibujarConexiones(Graphics2D g2, Nodo nodo) {
            if (nodo == null || nodo.esHoja) return;
            Point pP = posiciones.get(nodo);
            for (Nodo hijo : nodo.hijos) {
                Point pH = posiciones.get(hijo);
                g2.setColor(new Color(150, 150, 150, 100));
                g2.draw(new Line2D.Double(pP.x, pP.y + CELDA_ALTO, pH.x, pH.y));
                dibujarConexiones(g2, hijo);
            }
        }

        private void dibujarEnlacesHojas(Graphics2D g2) {
            // Lógica similar a la anterior para conectar hojas...
            g2.setColor(new Color(46, 204, 113, 80));
            // ... (código de líneas entre hojas)
        }

        private void dibujarInterfazUI(Graphics g) {
            // Dibujar controles de ayuda en una esquina ignorando la transformación
            Graphics2D gUI = (Graphics2D) g.create();
            gUI.setColor(Color.WHITE);
            gUI.setFont(new Font("Arial", Font.PLAIN, 12));
            gUI.drawString("Vizualizador Grafica", 35, getHeight() - 20);
            gUI.dispose();
        }
    }
}