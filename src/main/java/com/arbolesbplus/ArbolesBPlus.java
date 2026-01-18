/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.arbolesbplus;

import javax.swing.JOptionPane;

/**
 *
 * @author Athenea
 */
public class ArbolesBPlus {

    public static void main(String[] args) {
        String ord = JOptionPane.showInputDialog("Digite el orden de su Arbol");
                int d = Integer.parseInt(ord);
                int me;
                OperacionesArbolBPlus arbolIns = new OperacionesArbolBPlus();
        do {            
             String m = JOptionPane.showInputDialog(null, "1.- Insercion \n"
                + "2.- Busqueda \n"
                + "3.- Eliminacion \n"
                + "4.- Visualizacion \n"
                + "5.- Visualizacion por niveles \n"
                + "6.- Salir", "MENU", 1);
                me = Integer.parseInt(m);
             
       
            if(me==1){
                String valorStr = JOptionPane.showInputDialog("Ingrese la clave numérica:");
                        if (valorStr != null) {
                            int clave = Integer.parseInt(valorStr);
                            arbolIns.insertar(clave);
                            System.out.println("Clave " + clave + " insertada correctamente.");
                        }
                        //break;
            }else if(me==2){
                
                break;
            }else if(me==3){
            
            }else if(me==4){  
               Visualizacion.visualizar(arbolIns.raiz);
                 
            }else if(me==5){
                 arbolIns.imprimirPorNiveles();

        }
        } while (me != 6);
       
        }
}
