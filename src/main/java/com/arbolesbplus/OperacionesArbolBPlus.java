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
    
    // ======================= ELIMINACIÓN =========================
    
    public void eliminar(int clave) {
        if (raiz == null) {
            JOptionPane.showMessageDialog(null, "El árbol está vacío.");
            return;
        }
        
        Nodo hoja = buscarHoja(raiz, clave);
        int idx = hoja.claves.indexOf(clave);
        
        if (idx != -1) {
            // Guardar si es la primera clave
            boolean esPrimeraClave = (idx == 0);
            int claveEliminada = hoja.claves.get(idx);
            
            // Eliminar de la hoja
            hoja.claves.remove(idx);
            
            // IMPORTANTE: Solo corregir underflow si está POR DEBAJO del mínimo
            if (hoja != raiz && hoja.claves.size() < MIN_CLAVES) {
                corregirUnderflowHoja(hoja);
            }
            
            // SIEMPRE actualizar índices si la clave eliminada era la primera
            // Incluso si no hubo underflow
            if (esPrimeraClave && !hoja.claves.isEmpty()) {
                actualizarIndices(claveEliminada, hoja.claves.get(0));
            }
            
            // Caso especial: raíz hoja vacía
            if (hoja == raiz && hoja.claves.isEmpty()) {
                raiz = null;
            }
            
            JOptionPane.showMessageDialog(null, "Clave " + clave + " eliminada correctamente.");
            
        } else {
            JOptionPane.showMessageDialog(null, "La clave " + clave + " no existe.");
        }
    }
    
    private void actualizarIndices(int viejaClave, int nuevaClave) {
        if (raiz == null || raiz.esHoja) return;
        
        Nodo actual = raiz;
        boolean encontrado = false;
        
        // Buscar recursivamente la clave vieja en los índices
        while (!actual.esHoja && !encontrado) {
            // Buscar en las claves del nodo actual
            for (int i = 0; i < actual.claves.size(); i++) {
                if (actual.claves.get(i) == viejaClave) {
                    actual.claves.set(i, nuevaClave);
                    encontrado = true;
                    break;
                }
            }
            
            // Si no se encontró en este nodo, buscar en el hijo adecuado
            if (!encontrado) {
                int i = 0;
                while (i < actual.claves.size() && viejaClave >= actual.claves.get(i)) {
                    i++;
                }
                if (i < actual.hijos.size()) {
                    actual = actual.hijos.get(i);
                } else {
                    break; // Evitar IndexOutOfBounds
                }
            }
        }
    }
    
    private void corregirUnderflowHoja(Nodo hoja) {
        Nodo padre = hoja.padre;
        if (padre == null) return;
        
        int idx = padre.hijos.indexOf(hoja);
        
        // Solo hacer préstamo/fusión si está POR DEBAJO del mínimo
        if (hoja.claves.size() < MIN_CLAVES) {
            // PRÉSTAMO IZQUIERDO
            if (idx > 0) {
                Nodo hermanoIzq = padre.hijos.get(idx - 1);
                // El hermano puede prestar si tiene MÁS del mínimo
                if (hermanoIzq.claves.size() > MIN_CLAVES) {
                    // Tomar la última clave del hermano izquierdo
                    int clavePrestada = hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1);
                    hoja.claves.add(0, clavePrestada);
                    
                    // Actualizar clave en padre
                    padre.claves.set(idx - 1, hoja.claves.get(0));
                    return;
                }
            }
            
            // PRÉSTAMO DERECHO
            if (idx < padre.hijos.size() - 1) {
                Nodo hermanoDer = padre.hijos.get(idx + 1);
                // El hermano puede prestar si tiene MÁS del mínimo
                if (hermanoDer.claves.size() > MIN_CLAVES) {
                    // Tomar la primera clave del hermano derecho
                    int clavePrestada = hermanoDer.claves.remove(0);
                    hoja.claves.add(clavePrestada);
                    
                    // Actualizar clave en padre
                    padre.claves.set(idx, hermanoDer.claves.get(0));
                    return;
                }
            }
            
            // FUSIÓN (solo si ambos hermanos están en el mínimo)
            if (idx > 0) {
                fusionarConHermanoIzquierdo(padre, idx);
            } else {
                fusionarConHermanoDerecho(padre, idx);
            }
        }
    }
    
    private void fusionarConHermanoIzquierdo(Nodo padre, int idx) {
        Nodo hermanoIzq = padre.hijos.get(idx - 1);
        Nodo hoja = padre.hijos.get(idx);
        
        // Mover todas las claves de la hoja al hermano izquierdo
        hermanoIzq.claves.addAll(hoja.claves);
        
        // Actualizar enlace de hojas
        hermanoIzq.siguiente = hoja.siguiente;
        
        // Eliminar clave separadora en padre y la hoja
        padre.claves.remove(idx - 1);
        padre.hijos.remove(idx);
        
        // Si el padre queda con underflow
        if (padre != raiz && padre.claves.size() < MIN_CLAVES) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = hermanoIzq;
            raiz.padre = null;
        }
    }
    
    private void fusionarConHermanoDerecho(Nodo padre, int idx) {
        Nodo hoja = padre.hijos.get(idx);
        Nodo hermanoDer = padre.hijos.get(idx + 1);
        
        // Mover todas las claves del hermano derecho a la hoja
        hoja.claves.addAll(hermanoDer.claves);
        
        // Actualizar enlace de hojas
        hoja.siguiente = hermanoDer.siguiente;
        
        // Eliminar clave separadora en padre y el hermano derecho
        padre.claves.remove(idx);
        padre.hijos.remove(idx + 1);
        
        // Si el padre queda con underflow
        if (padre != raiz && padre.claves.size() < MIN_CLAVES) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = hoja;
            raiz.padre = null;
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
        int idx = padre.hijos.indexOf(nodo);
        
        // PRÉSTAMO IZQUIERDO
        if (idx > 0) {
            Nodo hermanoIzq = padre.hijos.get(idx - 1);
            if (hermanoIzq.claves.size() > MIN_CLAVES) {
                // Rotar derecha
                int clavePadre = padre.claves.get(idx - 1);
                int claveHermano = hermanoIzq.claves.remove(hermanoIzq.claves.size() - 1);
                Nodo hijoHermano = hermanoIzq.hijos.remove(hermanoIzq.hijos.size() - 1);
                
                nodo.claves.add(0, clavePadre);
                padre.claves.set(idx - 1, claveHermano);
                nodo.hijos.add(0, hijoHermano);
                hijoHermano.padre = nodo;
                return;
            }
        }
        
        // PRÉSTAMO DERECHO
        if (idx < padre.hijos.size() - 1) {
            Nodo hermanoDer = padre.hijos.get(idx + 1);
            if (hermanoDer.claves.size() > MIN_CLAVES) {
                // Rotar izquierda
                int clavePadre = padre.claves.get(idx);
                int claveHermano = hermanoDer.claves.remove(0);
                Nodo hijoHermano = hermanoDer.hijos.remove(0);
                
                nodo.claves.add(clavePadre);
                padre.claves.set(idx, claveHermano);
                nodo.hijos.add(hijoHermano);
                hijoHermano.padre = nodo;
                return;
            }
        }
        
        // FUSIÓN
        if (idx > 0) {
            fusionarInternoConHermanoIzquierdo(padre, idx);
        } else {
            fusionarInternoConHermanoDerecho(padre, idx);
        }
    }
    
    private void fusionarInternoConHermanoIzquierdo(Nodo padre, int idx) {
        Nodo hermanoIzq = padre.hijos.get(idx - 1);
        Nodo nodo = padre.hijos.get(idx);
        
        // Bajar clave separadora del padre
        int clavePadre = padre.claves.get(idx - 1);
        hermanoIzq.claves.add(clavePadre);
        hermanoIzq.claves.addAll(nodo.claves);
        
        // Mover hijos
        for (Nodo hijo : nodo.hijos) {
            hijo.padre = hermanoIzq;
            hermanoIzq.hijos.add(hijo);
        }
        
        // Eliminar del padre
        padre.claves.remove(idx - 1);
        padre.hijos.remove(idx);
        
        // Verificar underflow en padre
        if (padre != raiz && padre.claves.size() < MIN_CLAVES) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = hermanoIzq;
            raiz.padre = null;
        }
    }
    
    private void fusionarInternoConHermanoDerecho(Nodo padre, int idx) {
        Nodo nodo = padre.hijos.get(idx);
        Nodo hermanoDer = padre.hijos.get(idx + 1);
        
        // Bajar clave separadora del padre
        int clavePadre = padre.claves.get(idx);
        nodo.claves.add(clavePadre);
        nodo.claves.addAll(hermanoDer.claves);
        
        // Mover hijos
        for (Nodo hijo : hermanoDer.hijos) {
            hijo.padre = nodo;
            nodo.hijos.add(hijo);
        }
        
        // Eliminar del padre
        padre.claves.remove(idx);
        padre.hijos.remove(idx + 1);
        
        // Verificar underflow en padre
        if (padre != raiz && padre.claves.size() < MIN_CLAVES) {
            corregirUnderflowInterno(padre);
        } else if (padre == raiz && padre.claves.isEmpty()) {
            raiz = nodo;
            raiz.padre = null;
        }
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