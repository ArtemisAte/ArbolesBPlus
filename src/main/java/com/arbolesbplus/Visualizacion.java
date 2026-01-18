package com.arbolesbplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Visualizador gráfico con claves separadas en recuadros individuales
 */
public class Visualizacion {

    public static void visualizar(Nodo raiz) {
        if (raiz == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "El árbol está vacío.");
            return;
        }
        
        JFrame frame = new JFrame("🌳 Árbol B+ - Visualización");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 600);

        PanelArbolConRecuadros panel = new PanelArbolConRecuadros(raiz);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(new Color(245, 248, 250));
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    /**
     * Panel que dibuja cada clave en su propio recuadro
     */
    static class PanelArbolConRecuadros extends JPanel {
        private Nodo raiz;
        private final int ANCHO_CELDA = 35;    // Ancho de cada celda individual
        private final int ALTO_CELDA = 30;     // Alto de cada celda individual
        private final int ESPACIO_CELDAS = 2;  // Espacio entre celdas
        private final int PADDING_NODO = 8;    // Espacio interno del nodo
        private final int ESPACIO_VERTICAL = 90; // Distancia vertical entre niveles
        
        // Colores organizados por nivel
        private final Color[] COLORES_FONDO_NIVEL = {
            new Color(173, 216, 230), // Nivel 0 - LightBlue
            new Color(144, 238, 144), // Nivel 1 - LightGreen
            new Color(255, 218, 185), // Nivel 2 - Peach
            new Color(221, 160, 221), // Nivel 3 - Plum
            new Color(240, 230, 140), // Nivel 4 - Khaki
            new Color(175, 238, 238), // Nivel 5 - PaleTurquoise
        };
        
        private final Color COLOR_HOJA = new Color(230, 255, 230); // Verde claro para hojas
        private final Color COLOR_BORDE = new Color(70, 70, 70);
        private final Color COLOR_TEXTO = Color.BLACK;
        private final Color COLOR_LINEA = new Color(120, 120, 120);

        public PanelArbolConRecuadros(Nodo raiz) {
            this.raiz = raiz;
            this.setBackground(new Color(250, 252, 255));
            
            // Calcular dimensiones necesarias
            int[] dimensiones = calcularDimensiones(raiz);
            this.setPreferredSize(new Dimension(
                Math.max(900, dimensiones[0]), 
                Math.max(500, dimensiones[1])
            ));
        }
        
        private int[] calcularDimensiones(Nodo nodo) {
            if (nodo == null) return new int[]{0, 0};
            
            java.util.Queue<Object[]> cola = new java.util.LinkedList<>();
            cola.add(new Object[]{nodo, 0}); // nodo y su posición x estimada
            
            int maxDerecha = 0;
            int maxIzquierda = 0;
            int maxNivel = 0;
            
            while (!cola.isEmpty()) {
                int nodosNivel = cola.size();
                maxNivel++;
                
                for (int i = 0; i < nodosNivel; i++) {
                    Object[] obj = cola.poll();
                    Nodo actual = (Nodo) obj[0];
                    int posX = (int) obj[1];
                    
                    // Calcular ancho del nodo actual
                    int anchoNodo = actual.claves.size() * (ANCHO_CELDA + ESPACIO_CELDAS) + PADDING_NODO * 2;
                    
                    // Actualizar límites
                    maxIzquierda = Math.min(maxIzquierda, posX - anchoNodo/2);
                    maxDerecha = Math.max(maxDerecha, posX + anchoNodo/2);
                    
                    // Encolar hijos
                    if (!actual.esHoja) {
                        int numHijos = actual.hijos.size();
                        int espacioHijos = anchoNodo * 2;
                        
                        for (int j = 0; j < numHijos; j++) {
                            int hijoPosX;
                            if (numHijos == 1) {
                                hijoPosX = posX;
                            } else {
                                hijoPosX = posX - espacioHijos/2 + (j * espacioHijos/(numHijos-1));
                            }
                            cola.add(new Object[]{actual.hijos.get(j), hijoPosX});
                        }
                    }
                }
            }
            
            int anchoTotal = maxDerecha - maxIzquierda + 200; // Margen extra
            int alturaTotal = maxNivel * ESPACIO_VERTICAL + 150;
            
            return new int[]{anchoTotal, alturaTotal};
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            // Configuración de calidad
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Dibujar título
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("Árbol B+ - Claves Separadas en Recuadros", 20, 25);
            
            // Dibujar árbol centrado
            if (raiz != null) {
                dibujarNodoConRecuadros(g2, raiz, getWidth() / 2, 60, getWidth() / 3, 0);
            }
            
        }

        private int dibujarNodoConRecuadros(Graphics2D g, Nodo nodo, int x, int y, int rangoHorizontal, int nivel) {
            if (nodo == null) return 0;
            
            int numClaves = nodo.claves.size();
            
            // Seleccionar color según nivel y tipo de nodo
            Color colorFondo;
            if (nodo.esHoja) {
                colorFondo = COLOR_HOJA;
            } else {
                colorFondo = nivel < COLORES_FONDO_NIVEL.length ? 
                           COLORES_FONDO_NIVEL[nivel] : 
                           COLORES_FONDO_NIVEL[COLORES_FONDO_NIVEL.length - 1];
            }
            
            // Calcular dimensiones del contenedor del nodo
            int anchoTotalCeldas = numClaves * (ANCHO_CELDA + ESPACIO_CELDAS) - ESPACIO_CELDAS;
            int anchoContenedor = anchoTotalCeldas + PADDING_NODO * 2;
            int inicioX = x - (anchoContenedor / 2);
            
            // Dibujar fondo del contenedor del nodo (solo para visualización)
            if (nivel == 0) { // Solo para la raíz dibujamos un fondo
                g.setColor(new Color(240, 240, 240, 100));
                g.fillRoundRect(inicioX - 5, y - 5, anchoContenedor + 10, ALTO_CELDA + 10, 10, 10);
            }
            
            // Dibujar cada clave en su propio recuadro
            for (int i = 0; i < numClaves; i++) {
                int celdaX = inicioX + PADDING_NODO + i * (ANCHO_CELDA + ESPACIO_CELDAS);
                
                // Dibujar fondo del recuadro
                g.setColor(colorFondo);
                g.fillRect(celdaX, y, ANCHO_CELDA, ALTO_CELDA);
                
                // Dibujar borde del recuadro
                g.setColor(COLOR_BORDE);
                g.setStroke(new java.awt.BasicStroke(1.2f));
                g.drawRect(celdaX, y, ANCHO_CELDA, ALTO_CELDA);
                
                // Dibujar el número de la clave (centrado)
                g.setColor(COLOR_TEXTO);
                g.setFont(new Font("Arial", Font.BOLD, 11));
                String texto = String.valueOf(nodo.claves.get(i));
                
                FontMetrics fm = g.getFontMetrics();
                int textoX = celdaX + (ANCHO_CELDA - fm.stringWidth(texto)) / 2;
                int textoY = y + (ALTO_CELDA - fm.getHeight()) / 2 + fm.getAscent();
                
                g.drawString(texto, textoX, textoY);
            }
            
            // Etiqueta del tipo de nodo
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 9));
            String etiqueta = nodo.esHoja ? "Hoja" : ("Nivel " + nivel);
            int etiquetaX = inicioX + anchoContenedor + 5;
            g.drawString(etiqueta, etiquetaX, y + ALTO_CELDA/2 + 3);
            
            // Dibujar hijos si no es hoja
            if (!nodo.esHoja) {
                int numHijos = nodo.hijos.size();
                int nuevaY = y + ESPACIO_VERTICAL;
                
                // Calcular posición de los hijos
                int espacioTotalHijos = Math.min(rangoHorizontal * 2, getWidth() - 100);
                int inicioHijosX = x - (espacioTotalHijos / 2);
                
                for (int i = 0; i < numHijos; i++) {
                    // Distribuir hijos uniformemente
                    int hijoX;
                    if (numHijos == 1) {
                        hijoX = x;
                    } else {
                        hijoX = inicioHijosX + (i * espacioTotalHijos / (numHijos - 1));
                    }
                    
                    // Dibujar línea conectora desde el centro del nodo padre
                    int puntoOrigenX = inicioX + PADDING_NODO + (numClaves * (ANCHO_CELDA + ESPACIO_CELDAS)) / 2;
                    g.setColor(COLOR_LINEA);
                    g.setStroke(new java.awt.BasicStroke(1.0f));
                    g.drawLine(puntoOrigenX, y + ALTO_CELDA, hijoX, nuevaY);
                    
                    // Dibujar hijo recursivamente
                    dibujarNodoConRecuadros(g, nodo.hijos.get(i), hijoX, nuevaY, 
                                          (int)(espacioTotalHijos * 0.4 / numHijos), nivel + 1);
                }
            }
            
            return y + ALTO_CELDA;
        }
    }
}