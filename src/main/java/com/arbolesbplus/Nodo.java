/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.arbolesbplus;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Athenea
 */
public class Nodo {   
    int d;
    List<Integer> claves;  // Los valores almacenados.
    List<Nodo> hijos;      // Punteros a los descendientes (solo para nodos internos).
    Nodo padre;            // Referencia al nodo superior para poder "subir".
    Nodo siguiente;
    boolean esHoja;        // Booleano para saber si estamos en el nivel de datos.
    
    Nodo(boolean esHoja, int d) {
        this.esHoja = esHoja;
        this.claves = new ArrayList<>();
        this.hijos = new ArrayList<>();
        this.padre = null;
        this.d = d;
    }
      /**
     * Busca el índice de la primera clave mayor o igual a la clave dada.
     * @param clave Valor a buscar
     * @return Índice donde está o debería estar la clave
     */
    public int findKey(int clave) {
        int idx = 0;
        // Avanzar mientras haya claves y la actual sea menor
        while (idx < d && claves.get(idx) < clave) {
            idx++;
        }
        return idx;
    }
}
