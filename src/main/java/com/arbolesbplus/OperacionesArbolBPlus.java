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
    
    // --- MÉTODOS DE ELIMINACIÓN ---

    /*
     * ALGORITMO Eliminar(nodo raíz, T clave)
     * INICIO
     * hoja <- BuscarHoja(raíz, clave)
     * SI clave EXISTE EN hoja ENTONCES
     * EliminarClave(hoja, clave)
     * SI (hoja != raíz) Y (TAM(hoja.claves) < d) ENTONCES
     * LLAMAR CorregirUnderflow(hoja)
     * FIN SI
     * SINO
     * ESCRIBIR "Clave no encontrada"
     * FIN SI
     * FIN
    */
    public void eliminar(int clave) {
        Nodo hoja = buscarHoja(raiz, clave);
        int idx = hoja.claves.indexOf(clave);

        if (idx != -1) {
            hoja.claves.remove(idx);
            if (hoja != raiz && hoja.claves.size() < d) {
                corregirUnderflow(hoja);
            }
        } else {
            JOptionPane.showMessageDialog(null, "La clave no existe en el árbol.");
        }
    }

    /*
     * ALGORITMO CorregirUnderflow(nodo n)
     * INICIO
     * p <- n.padre
     * idx <- POSICIÓN DE n EN p.hijos
     * SI (idx > 0) Y (TAM(p.hijos[idx-1].claves) > d) ENTONCES
     * LLAMAR PrestarDeIzquierda(n, p.hijos[idx-1], p, idx-1)
     * SINO SI (idx < TAM(p.hijos)-1) Y (TAM(p.hijos[idx+1].claves) > d) ENTONCES
     * LLAMAR PrestarDeDerecha(n, p.hijos[idx+1], p, idx)
     * SINO
     * SI (idx > 0) ENTONCES LLAMAR Fusionar(p.hijos[idx-1], n)
     * SINO ENTONCES LLAMAR Fusionar(n, p.hijos[idx+1])
     * FIN SI
     * FIN
     */
    private void corregirUnderflow(Nodo nodo) {
        if (nodo == raiz) return;

        Nodo padre = nodo.padre;
        int idxHijo = padre.hijos.indexOf(nodo);

        if (idxHijo > 0) {
            Nodo hermanoIzquierdo = padre.hijos.get(idxHijo - 1);
            if (hermanoIzquierdo.claves.size() > d) {
                prestarDeIzquierda(nodo, hermanoIzquierdo, padre, idxHijo - 1);
                return;
            }
        }

        if (idxHijo < padre.hijos.size() - 1) {
            Nodo hermanoDerecho = padre.hijos.get(idxHijo + 1);
            if (hermanoDerecho.claves.size() > d) {
                prestarDeDerecha(nodo, hermanoDerecho, padre, idxHijo);
                return;
            }
        }

        if (idxHijo > 0) {
            fusionar(padre.hijos.get(idxHijo - 1), nodo);
        } else {
            fusionar(nodo, padre.hijos.get(idxHijo + 1));
        }
    }

    /*
     * ALGORITMO PrestarDeIzquierda(nodo n, hermano h, padre p, entero i)
     * INICIO
     * c <- h.claves.ÚLTIMO()
     * ELIMINAR h.claves.ÚLTIMO()
     * INSERTAR c EN n.claves[0]
     * p.claves[i] <- n.claves[0]
     * FIN
     */
    private void prestarDeIzquierda(Nodo nodo, Nodo hermano, Nodo padre, int idxPadre) {
        int clavePrestada = hermano.claves.remove(hermano.claves.size() - 1);
        nodo.claves.add(0, clavePrestada);
        padre.claves.set(idxPadre, nodo.claves.get(0));
    }

    /*
     * ALGORITMO PrestarDeDerecha(nodo n, hermano h, padre p, entero i)
     * INICIO
     * c <- h.claves[0]
     * ELIMINAR h.claves[0]
     * INSERTAR c AL FINAL DE n.claves
     * p.claves[i] <- h.claves[0]
     * FIN
     */
    private void prestarDeDerecha(Nodo nodo, Nodo hermano, Nodo padre, int idxPadre) {
        int clavePrestada = hermano.claves.remove(0);
        nodo.claves.add(clavePrestada);
        padre.claves.set(idxPadre, hermano.claves.get(0));
    }

    /*
     * ALGORITMO Fusionar(izq, der)
     * INICIO
     * p <- izq.padre
     * izq.claves <- izq.claves + der.claves
     * SI (izq NO ES hoja) ENTONCES
     * izq.hijos <- izq.hijos + der.hijos
     * FIN SI
     * ELIMINAR p.claves[i] Y p.hijos[der]
     * SI (p = raíz) Y (TAM(p.claves) = 0) ENTONCES
     * raíz <- izq
     * SINO SI (p != raíz) Y (TAM(p.claves) < d) ENTONCES
     * LLAMAR CorregirUnderflow(p)
     * FIN SI
     * FIN
     */
    private void fusionar(Nodo izq, Nodo der) {
        Nodo padre = izq.padre;
        izq.claves.addAll(der.claves);

        if (!izq.esHoja) {
            izq.hijos.addAll(der.hijos);
            for (Nodo hijo : der.hijos) hijo.padre = izq;
        }

        int idxDer = padre.hijos.indexOf(der);
        padre.claves.remove(idxDer - 1);
        padre.hijos.remove(idxDer);

        if (padre == raiz && padre.claves.isEmpty()) {
            raiz = izq;
            raiz.padre = null;
        } else if (padre != raiz && padre.claves.size() < d) {
            corregirUnderflow(padre);
        }
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
