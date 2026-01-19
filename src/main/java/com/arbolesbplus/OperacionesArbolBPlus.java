package com.arbolesbplus;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OperacionesArbolBPlus {
    
    public Nodo raiz;
    private final int orden; // ORDEN del árbol B+
    private final int maxClaves; // Máximo de claves por nodo
    private final int minClaves; // Mínimo de claves por nodo
    
    public OperacionesArbolBPlus(int ordenUsuario) {
        this.orden = ordenUsuario;
        
        // CONFIGURACIÓN: Máximo 4 claves, se divide a la 5ta
        this.maxClaves = 4;    // Máximo de claves por nodo
        this.minClaves = 2;    // Mínimo de claves por nodo (excepto raíz)
        
        raiz = new Nodo(true, this.orden);
    }
    
    // ======================= INSERCIÓN CORREGIDA ===========================
    
    public void insertar(int clave) {
        if (raiz == null) {
            raiz = new Nodo(true, orden);
        }
        
        Nodo hoja = buscarHoja(raiz, clave);
        insertarOrdenado(hoja.claves, clave);
        
        // Si excede el máximo de claves (4), dividir
        if (hoja.claves.size() > maxClaves) {
            dividirHoja(hoja);
        }
    }
    
    // Buscar página descendiente - CORREGIDO para B+
    private Nodo buscarHoja(Nodo actual, int clave) {
        if (actual.esHoja) return actual;
        
        int i = 0;
        while (i < actual.claves.size() && clave >= actual.claves.get(i)) {
            i++;
        }
        return buscarHoja(actual.hijos.get(i), clave);
    }
    
    // Inserción ordenada manual
    private void insertarOrdenado(List<Integer> lista, int clave) {
        int i = 0;
        while (i < lista.size() && clave > lista.get(i)) {
            i++;
        }
        lista.add(i, clave);
    }
    
    private void dividirHoja(Nodo hoja) {
        Nodo nuevaHoja = new Nodo(true, orden);
        
        // Para máximo 4 claves, dividir cuando tenga 5
        // En B+, la clave de división se DUPLICA en ambas hojas
        int puntoCorte = (maxClaves + 1) / 2; // Para max=4: puntoCorte=2
        
        // Mover claves a la nueva hoja (incluyendo la clave de división)
        for (int i = puntoCorte; i < hoja.claves.size(); i++) {
            nuevaHoja.claves.add(hoja.claves.get(i));
        }
        
        // Eliminar las claves movidas (mantener la clave de división en la hoja original)
        // En B+, la clave de división se queda en AMBAS hojas
        hoja.claves.subList(puntoCorte, hoja.claves.size()).clear();
        
        // Conectar las hojas en la lista enlazada
        nuevaHoja.siguiente = hoja.siguiente;
        hoja.siguiente = nuevaHoja;
        
        // En B+, la primera clave de la nueva hoja sube al padre
        int claveSubir = nuevaHoja.claves.get(0);
        manejarSubida(hoja, claveSubir, nuevaHoja);
    }
    
    private void manejarSubida(Nodo izquierdo, int clave, Nodo derecho) {
        if (izquierdo == raiz) {
            Nodo nuevaRaiz = new Nodo(false, orden);
            nuevaRaiz.claves.add(clave);
            nuevaRaiz.hijos.add(izquierdo);
            nuevaRaiz.hijos.add(derecho);
            raiz = nuevaRaiz;
            izquierdo.padre = raiz;
            derecho.padre = raiz;
            return;
        }
        
        Nodo padre = izquierdo.padre;
        if (padre == null) return;
        
        derecho.padre = padre;
        
        // Insertar clave e hijo en el padre manteniendo el orden
        int i = 0;
        while (i < padre.claves.size() && clave > padre.claves.get(i)) {
            i++;
        }
        padre.claves.add(i, clave);
        padre.hijos.add(i + 1, derecho);
        
        // Si el padre se llena (más de 4 claves), se divide también
        if (padre.claves.size() > maxClaves) {
            dividirNodoInterno(padre);
        }
    }
    
    private void dividirNodoInterno(Nodo nodo) {
        Nodo nuevoNodo = new Nodo(false, orden);
        
        // Para máximo 4 claves, dividir cuando tenga 5
        // En nodos internos, la mediana sube al padre y NO se queda en los hijos
        int m = nodo.claves.size() / 2;  // Para 5 claves: m=2 (la tercera clave sube)
        int claveSubir = nodo.claves.get(m);
        
        // Mover claves a la derecha de la mediana al nuevo nodo
        for (int i = m + 1; i < nodo.claves.size(); i++) {
            nuevoNodo.claves.add(nodo.claves.get(i));
        }
        
        // Mover hijos (los hijos del nuevo nodo comienzan en m+1)
        for (int i = m + 1; i < nodo.hijos.size(); i++) {
            Nodo hijo = nodo.hijos.get(i);
            nuevoNodo.hijos.add(hijo);
            hijo.padre = nuevoNodo;
        }
        
        // Limpiar el nodo original (eliminar desde m inclusive)
        nodo.claves.subList(m, nodo.claves.size()).clear();
        nodo.hijos.subList(m + 1, nodo.hijos.size()).clear();
        
        manejarSubida(nodo, claveSubir, nuevoNodo);
    }
    
    // ======================= ELIMINACIÓN (sin cambios) =========================
    
    public void eliminar(int clave) {
        if (raiz == null) return;
        
        Nodo hoja = buscarHoja(raiz, clave);
        int idx = hoja.claves.indexOf(clave);
        
        if (idx == -1) return;
        
        boolean esPrimeraClave = (idx == 0);
        int claveEliminada = hoja.claves.get(idx);
        
        hoja.claves.remove(idx);
        
        // Corrección de underflow en hojas
        if (hoja != raiz && hoja.claves.size() < minClaves) {
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
        while (!actual.esHoja) {
            boolean encontrado = false;
            for (int i = 0; i < actual.claves.size(); i++) {
                if (actual.claves.get(i) == viejaClave) {
                    actual.claves.set(i, nuevaClave);
                    encontrado = true;
                    break;
                }
            }
            
            if (encontrado) break;
            
            // Buscar hijo adecuado
            int i = 0;
            while (i < actual.claves.size() && viejaClave > actual.claves.get(i)) {
                i++;
            }
            actual = actual.hijos.get(i);
        }
    }
    
    private void corregirUnderflowHoja(Nodo hoja) {
        Nodo padre = hoja.padre;
        if (padre == null) return;
        
        int idx = padre.hijos.indexOf(hoja);
        
        // Intentar préstamo izquierdo
        if (idx > 0) {
            Nodo hermanoIzq = padre.hijos.get(idx - 1);
            if (hermanoIzq.claves.size() > minClaves) {
                int clavePrestada = hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1);
                hoja.claves.add(0, clavePrestada);
                padre.claves.set(idx - 1, hoja.claves.get(0));
                return;
            }
        }
        
        // Intentar préstamo derecho
        if (idx < padre.hijos.size() - 1) {
            Nodo hermanoDer = padre.hijos.get(idx + 1);
            if (hermanoDer.claves.size() > minClaves) {
                int clavePrestada = hermanoDer.claves.remove(0);
                hoja.claves.add(clavePrestada);
                padre.claves.set(idx, hermanoDer.claves.get(0));
                return;
            }
        }
        
        // Fusión
        if (idx > 0) {
            fusionarHojas(padre, idx - 1, idx);
        } else if (idx < padre.hijos.size() - 1) {
            fusionarHojas(padre, idx, idx + 1);
        }
    }
    
    private void fusionarHojas(Nodo padre, int idxIzq, int idxDer) {
        Nodo izquierdo = padre.hijos.get(idxIzq);
        Nodo derecho = padre.hijos.get(idxDer);
        
        // Mover todas las claves del derecho al izquierdo
        izquierdo.claves.addAll(derecho.claves);
        izquierdo.siguiente = derecho.siguiente;
        
        // Eliminar clave separadora del padre
        padre.claves.remove(idxIzq);
        padre.hijos.remove(idxDer);
        
        // Verificar underflow en padre
        if (padre == raiz && padre.claves.isEmpty()) {
            raiz = izquierdo;
            raiz.padre = null;
        } else if (padre != raiz && padre.claves.size() < minClaves) {
            corregirUnderflowInterno(padre);
        }
    }
    
    private void corregirUnderflowInterno(Nodo nodo) {
        if (nodo == raiz) {
            if (nodo.claves.isEmpty() && !nodo.hijos.isEmpty()) {
                raiz = nodo.hijos.get(0);
                raiz.padre = null;
            }
            return;
        }
        
        Nodo padre = nodo.padre;
        if (padre == null) return;
        
        int idx = padre.hijos.indexOf(nodo);
        
        // Préstamo izquierdo
        if (idx > 0) {
            Nodo hermanoIzq = padre.hijos.get(idx - 1);
            if (hermanoIzq.claves.size() > minClaves) {
                // Rotar clave del padre
                nodo.claves.add(0, padre.claves.get(idx - 1));
                padre.claves.set(idx - 1, hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1));
                
                // Rotar hijo si es necesario
                if (!hermanoIzq.esHoja && !hermanoIzq.hijos.isEmpty()) {
                    Nodo hijo = hermanoIzq.hijos.remove(hermanoIzq.hijos.size() - 1);
                    nodo.hijos.add(0, hijo);
                    hijo.padre = nodo;
                }
                return;
            }
        }
        
        // Préstamo derecho
        if (idx < padre.hijos.size() - 1) {
            Nodo hermanoDer = padre.hijos.get(idx + 1);
            if (hermanoDer.claves.size() > minClaves) {
                // Rotar clave del padre
                nodo.claves.add(padre.claves.get(idx));
                padre.claves.set(idx, hermanoDer.claves.remove(0));
                
                // Rotar hijo si es necesario
                if (!hermanoDer.esHoja && !hermanoDer.hijos.isEmpty()) {
                    Nodo hijo = hermanoDer.hijos.remove(0);
                    nodo.hijos.add(hijo);
                    hijo.padre = nodo;
                }
                return;
            }
        }
        
        // Fusión
        if (idx > 0) {
            fusionarInternos(padre, idx - 1, idx);
        } else if (idx < padre.hijos.size() - 1) {
            fusionarInternos(padre, idx, idx + 1);
        }
    }
    
    private void fusionarInternos(Nodo padre, int idxIzq, int idxDer) {
        Nodo izquierdo = padre.hijos.get(idxIzq);
        Nodo derecho = padre.hijos.get(idxDer);
        
        // Bajar clave separadora
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
        if (padre == raiz && padre.claves.isEmpty()) {
            raiz = izquierdo;
            raiz.padre = null;
        } else if (padre != raiz && padre.claves.size() < minClaves) {
            corregirUnderflowInterno(padre);
        }
    }
    
    // ================= BUSQUEDA =============================
    
    public void buscarYMostrar(int clave) {
        if (raiz == null) {
            JOptionPane.showMessageDialog(null, "El árbol está vacío.");
            return;
        }
        
        boolean encontrado = buscarRecursivo(raiz, clave);
        if (encontrado) {
            JOptionPane.showMessageDialog(null,
                    "La clave " + clave + " SI existe en el árbol B+");
        } else {
            JOptionPane.showMessageDialog(null,
                    "La clave " + clave + " NO existe en el árbol B+");
        }
    }
    
    private boolean buscarRecursivo(Nodo actual, int clave) {
        if (actual.esHoja) {
            return actual.claves.contains(clave);
        }
        
        int i = 0;
        while (i < actual.claves.size() && clave >= actual.claves.get(i)) {
            i++;
        }
        return buscarRecursivo(actual.hijos.get(i), clave);
    }
    
    // ================= VISUALIZACIÓN =============================
    
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
        while (hoja != null && !hoja.esHoja && !hoja.hijos.isEmpty()) {
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