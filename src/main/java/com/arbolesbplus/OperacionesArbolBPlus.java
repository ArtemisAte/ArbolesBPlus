package com.arbolesbplus;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OperacionesArbolBPlus {

    public Nodo raiz;
    public static final int d = 2; // ORDEN 2: máximo 3 claves por nodo
    
    // CONSTANTES CLARAS
    private static final int MAX_CLAVES = 3;    // 2*d - 1 = 3
    private static final int MIN_CLAVES = 1;    // d - 1 = 1

    public OperacionesArbolBPlus() {
        raiz = new Nodo(true, d);
    }

    // ======================= INSERCIÓN ===========================
    
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
        while (i < actual.claves.size() && clave > actual.claves.get(i)) {
            i++;
        }
        // En B+, si clave es igual, va al hijo derecho (i+1)
        if (i < actual.claves.size() && clave == actual.claves.get(i)) {
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

        // CORRECCIÓN: Conectar las hojas en la lista enlazada
        nuevaHoja.siguiente = hoja.siguiente;
        hoja.siguiente = nuevaHoja;

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
    
    // ======================= ELIMINACIÓN COMPACTA =========================

    public void eliminar(int clave) {
        if (raiz == null) return;

        Nodo hoja = buscarHoja(raiz, clave);
        int idx = hoja.claves.indexOf(clave);

        if (idx == -1) return;

        boolean esPrimeraClave = (idx == 0);
        int claveEliminada = hoja.claves.get(idx);

        hoja.claves.remove(idx);

        // Corrección de underflow en hojas
        if (hoja != raiz && hoja.claves.size() < MIN_CLAVES) {
            corregirUnderflowHoja(hoja);
        }

        // Actualizar índice si se eliminó la primera clave
        if (esPrimeraClave && !hoja.claves.isEmpty()) {
            actualizarIndices(claveEliminada, hoja.claves.get(0));
        }

        // Caso especial: raíz hoja vacía
        if (hoja == raiz && hoja.claves.isEmpty()) {
            raiz = null;
        }
    }
    private void actualizarIndices(int viejaClave, int nuevaClave) {
    if (raiz == null || raiz.esHoja) return;
    
    Nodo actual = raiz;
    boolean encontrado = false;
    
    // Buscar la clave vieja en los índices
    while (!actual.esHoja && !encontrado) {
        for (int i = 0; i < actual.claves.size(); i++) {
            if (actual.claves.get(i) == viejaClave) {
                actual.claves.set(i, nuevaClave);
                encontrado = true;
                break;
            }
        }
        
        // Si no se encontró, ir al hijo adecuado
        if (!encontrado) {
            int i = 0;
            while (i < actual.claves.size() && viejaClave >= actual.claves.get(i)) {
                i++;
            }
            if (i < actual.hijos.size()) {
                actual = actual.hijos.get(i);
            } else {
                break;
            }
        }
    }
    }

    private void corregirUnderflowHoja(Nodo hoja) {
        Nodo padre = hoja.padre;
        if (padre == null) return;

        int idx = padre.hijos.indexOf(hoja);

        // Intentar préstamo izquierdo
        if (idx > 0 && padre.hijos.get(idx - 1).claves.size() > MIN_CLAVES) {
            Nodo hermanoIzq = padre.hijos.get(idx - 1);
            int clavePrestada = hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1);
            hoja.claves.add(0, clavePrestada);
            padre.claves.set(idx - 1, hoja.claves.get(0));
            return;
        }

        // Intentar préstamo derecho
        if (idx < padre.hijos.size() - 1 && padre.hijos.get(idx + 1).claves.size() > MIN_CLAVES) {
            Nodo hermanoDer = padre.hijos.get(idx + 1);
            int clavePrestada = hermanoDer.claves.remove(0);
            hoja.claves.add(clavePrestada);
            padre.claves.set(idx, hermanoDer.claves.get(0));
            return;
        }

        // Fusión (con izquierdo si existe, sino con derecho)
        if (idx > 0) {
            fusionarHojas(padre, idx - 1, idx);
        } else {
            fusionarHojas(padre, idx, idx + 1);
        }
    }

    private void fusionarHojas(Nodo padre, int idxIzq, int idxDer) {
        Nodo izquierdo = padre.hijos.get(idxIzq);
        Nodo derecho = padre.hijos.get(idxDer);

        // Mover claves del derecho al izquierdo
        izquierdo.claves.addAll(derecho.claves);
        izquierdo.siguiente = derecho.siguiente;

        // Eliminar clave separadora y nodo derecho
        padre.claves.remove(idxIzq);
        padre.hijos.remove(idxDer);

        // Verificar underflow en padre
        if (padre.claves.size() < MIN_CLAVES && padre != raiz) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = izquierdo;
            raiz.padre = null;
        }
    }

    private void corregirUnderflowInterno(Nodo nodo) {
        if (nodo == raiz && nodo.claves.isEmpty()) {
            if (!nodo.hijos.isEmpty()) {
                raiz = nodo.hijos.get(0);
                raiz.padre = null;
            }
            return;
        }

        Nodo padre = nodo.padre;
        if (padre == null) return;

        int idx = padre.hijos.indexOf(nodo);

        // Préstamo izquierdo
        if (idx > 0 && padre.hijos.get(idx - 1).claves.size() > MIN_CLAVES) {
            Nodo hermanoIzq = padre.hijos.get(idx - 1);
            nodo.claves.add(0, padre.claves.get(idx - 1));
            padre.claves.set(idx - 1, hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1));
            if (!hermanoIzq.esHoja) {
                Nodo hijo = hermanoIzq.hijos.remove(hermanoIzq.hijos.size() - 1);
                nodo.hijos.add(0, hijo);
                hijo.padre = nodo;
            }
            return;
        }

        // Préstamo derecho
        if (idx < padre.hijos.size() - 1 && padre.hijos.get(idx + 1).claves.size() > MIN_CLAVES) {
            Nodo hermanoDer = padre.hijos.get(idx + 1);
            nodo.claves.add(padre.claves.get(idx));
            padre.claves.set(idx, hermanoDer.claves.remove(0));
            if (!hermanoDer.esHoja) {
                Nodo hijo = hermanoDer.hijos.remove(0);
                nodo.hijos.add(hijo);
                hijo.padre = nodo;
            }
            return;
        }

        // Fusión
        if (idx > 0) {
            fusionarInternos(padre, idx - 1, idx);
        } else {
            fusionarInternos(padre, idx, idx + 1);
        }
    }

    private void fusionarInternos(Nodo padre, int idxIzq, int idxDer) {
        Nodo izquierdo = padre.hijos.get(idxIzq);
        Nodo derecho = padre.hijos.get(idxDer);

        // Bajar clave separadora y fusionar
        izquierdo.claves.add(padre.claves.get(idxIzq));
        izquierdo.claves.addAll(derecho.claves);

        // Mover hijos
        for (Nodo hijo : derecho.hijos) {
            hijo.padre = izquierdo;
            izquierdo.hijos.add(hijo);
        }

        // Eliminar del padre
        padre.claves.remove(idxIzq);
        padre.hijos.remove(idxDer);

        // Verificar underflow recursivo
        if (padre.claves.size() < MIN_CLAVES && padre != raiz) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = izquierdo;
            raiz.padre = null;
        }
    }

     // ================= BUSQUEDA =============================
//Algoritmo BuscarEnArbolBPlus(raiz, claveBuscada)
//    Si (raiz == NULL) Entonces
//        Escribir("El árbol está vacío")
//        Retornar FALSO
//    Fin Si
//    nodoActual <- raiz
//    Mientras (nodoActual.esHoja == FALSO) Repetir
//        i <- 0
//        Mientras (i < Tamaño(nodoActual.claves) Y claveBuscada > nodoActual.claves[i]) Repetir
//            i <- i + 1
//        Fin Mientras
        // Regla del B+: si es igual, ir al hijo derecho
//        Si (i < Tamaño(nodoActual.claves) Y claveBuscada == nodoActual.claves[i]) Entonces
//            i <- i + 1
//        Fin Si
//        nodoActual <- nodoActual.hijos[i]
//    Fin Mientras
    // Ya estamos en una hoja
//    i <- 0
//    Mientras (i < Tamaño(nodoActual.claves)) Repetir
//        Si (nodoActual.claves[i] == claveBuscada) Entonces
//            Escribir("Clave encontrada")
//            Retornar VERDADERO
//        Fin Si
//        i <- i + 1
//    Fin Mientras
// Escribir("Clave NO encontrada")
//  Retornar FALSO
//Fin Algoritmo BuscarEnArbolBPlus

// Método público que inicia la búsqueda y muestra el resultado en una ventana
public void buscarYMostrar(int clave) {
    // Verifica si el árbol está vacío
    if (raiz == null) {
        // Muestra mensaje si no hay nodos en el árbol
        JOptionPane.showMessageDialog(null, "El árbol está vacío.");
        return; // Sale del método
    }
    // Llama al método recursivo para buscar la clave desde la raíz
    boolean encontrado = buscarRecursivo(raiz, clave);
    // Si la clave fue encontrada
    if (encontrado) {
        // Muestra mensaje positivo
        JOptionPane.showMessageDialog(null,
                "La clave " + clave + " SI existe en el árbol B+");
    } else {
        // Si no fue encontrada, muestra mensaje negativo
        JOptionPane.showMessageDialog(null,
                "La clave " + clave + " NO existe en el árbol B+");
    }
}
// Método privado recursivo que recorre el árbol B+ para buscar la clave
private boolean buscarRecursivo(Nodo actual, int clave) {
    // Variable para recorrer las claves del nodo actual
    int i = 0;
    // Avanza mientras la clave buscada sea mayor que la clave actual del nodo
    while (i < actual.claves.size() && clave > actual.claves.get(i)) {
        i++; // Mueve el índice a la siguiente clave
    }
    // Si el nodo actual es una hoja del árbol B+
    if (actual.esHoja) {
        // Verifica si la clave existe exactamente en esta posición
        return (i < actual.claves.size() && actual.claves.get(i) == clave);
    }
    // Si no es hoja y la clave coincide con una clave separadora del nodo
    // En árboles B+, se baja al hijo derecho
    if (i < actual.claves.size() && clave == actual.claves.get(i)) {
        i++; // Se mueve al hijo derecho correspondiente
    }
    // Llamada recursiva para continuar la búsqueda en el hijo adecuado
    return buscarRecursivo(actual.hijos.get(i), clave);
}
    
    // ================= VISUALIZACIÓN =============================
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
        if (raiz == null) {
            JOptionPane.showMessageDialog(null, "El árbol está vacío.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        java.util.Queue<Nodo> cola = new java.util.LinkedList<>();
        cola.add(raiz);
        int nivel = 0;
        
        while (!cola.isEmpty()) {
            int nodosNivel = cola.size();
            sb.append("Nivel ").append(nivel).append(": ");
            for (int i = 0; i < nodosNivel; i++) {
                Nodo nodo = cola.poll();
                sb.append("[");
                for (int j = 0; j < nodo.claves.size(); j++) {
                    sb.append(nodo.claves.get(j));
                    if (j < nodo.claves.size() - 1) sb.append(",");
                }
                sb.append("] ");
                
                if (!nodo.esHoja) {
                    for (Nodo hijo : nodo.hijos) {
                        cola.add(hijo);
                    }
                }
            }
            sb.append("\n");
            nivel++;
        }
        
        // Mostrar hojas enlazadas
        sb.append("\nHojas enlazadas: ");
        Nodo hoja = raiz;
        while (!hoja.esHoja && !hoja.hijos.isEmpty()) {
            hoja = hoja.hijos.get(0);
        }
        while (hoja != null) {
            sb.append("[");
            for (int j = 0; j < hoja.claves.size(); j++) {
                sb.append(hoja.claves.get(j));
                if (j < hoja.claves.size() - 1) sb.append(",");
            }
            sb.append("] -> ");
            hoja = hoja.siguiente;
        }
        sb.append("null");
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea),
                "Estructura del B+ por Niveles", 1);
    }
}
