/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.arbolesbplus;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Athenea
 */
public class OperacionesArbolBPlus {
 public Nodo raiz;
    public static final int d = 2; // Grado mínimo

    public OperacionesArbolBPlus() {
        raiz = new Nodo(true, d);
    }

    public void insertar(int clave) {
        Nodo hoja = buscarHoja(raiz, clave);
        insertarOrdenado(hoja.claves, clave);

        // Si excede el máximo 2d-1 (es decir, llega a 2d)
        if (hoja.claves.size() == 2 * d) {
            dividirHoja(hoja);
        }
    }

    // Paso 2.2: Buscar página descendiente
    private Nodo buscarHoja(Nodo actual, int clave) {
        if (actual.esHoja) return actual;
        
        int i = 0;
        while (i < actual.claves.size() && clave >= actual.claves.get(i)) {
            i++;
        }
        return buscarHoja(actual.hijos.get(i), clave);
    }

    // Inserción manual para evitar Collections.sort
    private void insertarOrdenado(List<Integer> lista, int clave) {
        int i = 0;
        while (i < lista.size() && clave > lista.get(i)) {
            i++;
        }
        lista.add(i, clave);
    }

    private void dividirHoja(Nodo hoja) {
        Nodo nuevaHoja = new Nodo(true, d);
        int puntoCorte = d; 

        // Mover la mitad derecha a la nueva hoja
        nuevaHoja.claves.addAll(new ArrayList<>(hoja.claves.subList(puntoCorte, hoja.claves.size())));
        hoja.claves.subList(puntoCorte, hoja.claves.size()).clear();

        // En B+, la primera clave de la nueva hoja sube al padre como guía
        int claveSubir = nuevaHoja.claves.get(0);
        manejarSubida(hoja, claveSubir, nuevaHoja);
    }

    private void manejarSubida(Nodo izquierdo, int clave, Nodo derecho) {
        if (izquierdo == raiz) {
            Nodo nuevaRaiz = new Nodo(false, d);
            nuevaRaiz.claves.add(clave);
            nuevaRaiz.hijos.add(izquierdo);
            nuevaRaiz.hijos.add(derecho);
            raiz = nuevaRaiz;
            izquierdo.padre = derecho.padre = raiz;
            return;
        }

        Nodo padre = izquierdo.padre;
        derecho.padre = padre;

        // Insertar clave e hijo en el padre manteniendo el orden
        int i = 0;
        while (i < padre.claves.size() && clave > padre.claves.get(i)) {
            i++;
        }
        padre.claves.add(i, clave);
        padre.hijos.add(i + 1, derecho);

        // Si el padre se llena (2d), se divide también
        if (padre.claves.size() == 2 * d) {
            dividirNodoInterno(padre);
        }
    }

    private void dividirNodoInterno(Nodo nodo) {
        Nodo nuevoNodo = new Nodo(false, d);
        int m = d; 
        int claveSubir = nodo.claves.get(m);

        // Mover elementos a la derecha de la mediana al nuevo nodo
        nuevoNodo.claves.addAll(new ArrayList<>(nodo.claves.subList(m + 1, nodo.claves.size())));
        nuevoNodo.hijos.addAll(new ArrayList<>(nodo.hijos.subList(m + 1, nodo.hijos.size())));

        for (Nodo hijo : nuevoNodo.hijos) hijo.padre = nuevoNodo;

        // Limpiar el nodo original (la mediana sube y se quita de aquí)
        nodo.claves.subList(m, nodo.claves.size()).clear();
        nodo.hijos.subList(m + 1, nodo.hijos.size()).clear();

        manejarSubida(nodo, claveSubir, nuevoNodo);
    }
     // ==================== OPERACIÓN: RECORRER POR NIVELES ====================
    
    /*
Algoritmo ImprimirPorNiveles
Si (raiz == NULL) Entonces
    Retornar
Fin Si
Crear(sb)
Crear(cola)
Encolar(cola, raiz)
nivel <- 0
Mientras (cola NO esté vacía) Repetir
    nodosNivel <- Tamaño(cola)
    Escribir("Nivel ", nivel, ": ")
    i <- 0
    Mientras (i < nodosNivel) Repetir
        nodo <- Desencolar(cola)
        Escribir("[ ")
        j <- 0
        Mientras (j < nodo.n) Repetir
            Escribir(nodo.claves[j])
            Si (j < nodo.n - 1) Entonces
                Escribir(" | ")
            Fin Si
            j <- j + 1
        Fin Mientras
        Escribir(" ]  ")
        Si (nodo.esHoja == Falso) Entonces
            j <- 0
            Mientras (j <= nodo.n) Repetir
                Si (nodo.hijos[j] != NULL) Entonces
                    Encolar(cola, nodo.hijos[j])
                Fin Si
                j <- j + 1
            Fin Mientras
        Fin Si
        i <- i + 1
    Fin Mientras
    EscribirSaltoLinea
    nivel <- nivel + 1
Fin Mientras
MostrarVentana(sb)
Fin Algoritmo ImprimirPorNiveles
*/

    /**
     * Muestra el árbol por niveles en una ventana gráfica.
     * Usa recorrido BFS (por anchura).
     */
    public void imprimirPorNiveles() {
        if (raiz == null) return;
        
        StringBuilder sb = new StringBuilder();
        java.util.Queue<Nodo> cola = new java.util.LinkedList<>();
        cola.add(raiz);
        int nivel = 0;
        
        while (!cola.isEmpty()) {
            int nodosNivel = cola.size();
            sb.append("Nivel ").append(nivel).append(": ");
            
            for (int i = 0; i < nodosNivel; i++) {
                Nodo nodo = cola.poll();
                
                // Formatear nodo
                sb.append("[ ");
                for (int j = 0; j < nodo.d; j++) {
                    sb.append(nodo.claves.get(j));
                    if (j < nodo.d - 1) sb.append(" | ");
                }
                sb.append(" ]  ");
                
                // Agregar hijos a la cola
                if (!nodo.esHoja) {
                    for (int j = 0; j <= nodo.d; j++) {
                        if (nodo.hijos.get(j) != null) {
                            cola.add(nodo.hijos.get(j));
                        }
                    }
                }
            }
            sb.append("\n");
            nivel++;
        }
        
        // Mostrar en ventana

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, 
            new JScrollPane(textArea), 
            "Estructura por Niveles", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
