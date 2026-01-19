package com.arbolesbplus;

import javax.swing.JOptionPane;

public class ArbolesBPlus {
    
    public static void main(String[] args) {
        String ord = JOptionPane.showInputDialog("Digite el orden de su Arbol");
        if (ord == null) return;
        
        int d = Integer.parseInt(ord);
        OperacionesArbolBPlus arbolIns = new OperacionesArbolBPlus(d);
        int me;
        
        do {
            String m = JOptionPane.showInputDialog(null,
                    "1.- Insercion \n" +
                    "2.- Busqueda \n" +
                    "3.- Eliminacion \n" +
                    "4.- Visualizacion \n" +
                    "5.- Visualizacion por niveles \n" +
                    "6.- Salir", "MENU", 1);
            
            if (m == null) break;
            me = Integer.parseInt(m);
            
            switch (me) {
                case 1: // Inserción
                    String valorStr = JOptionPane.showInputDialog("Ingrese la clave numérica:");
                    if (valorStr != null) {
                        int clave = Integer.parseInt(valorStr);
                        arbolIns.insertar(clave);
                        JOptionPane.showMessageDialog(null, 
                                "Clave " + clave + " insertada correctamente.");
                    }
                    break;
                    
                case 2: // Búsqueda
                    valorStr = JOptionPane.showInputDialog("Ingrese la clave a buscar:");
                    if (valorStr != null) {
                        int clave = Integer.parseInt(valorStr);
                        arbolIns.buscarYMostrar(clave);
                    }
                    break;
                    
                case 3: // Eliminación
                    valorStr = JOptionPane.showInputDialog("Ingrese la clave a eliminar:");
                    if (valorStr != null) {
                        int clave = Integer.parseInt(valorStr);
                        arbolIns.eliminar(clave);
                        JOptionPane.showMessageDialog(null, 
                                "Clave " + clave + " eliminada correctamente.");
                    }
                    break;
                    
                case 4: // Visualización gráfica
                    Visualizacion.visualizar(arbolIns.raiz);
                    break;
                    
                case 5: // Visualización por niveles
                    arbolIns.imprimirPorNiveles();
                    break;
                    
                case 6: // Salir
                    JOptionPane.showMessageDialog(null, "Saliendo del programa...");
                    break;
                    
                default:
                    JOptionPane.showMessageDialog(null, "Opción inválida");
                    break;
            }
        } while (me != 6);
    }
}