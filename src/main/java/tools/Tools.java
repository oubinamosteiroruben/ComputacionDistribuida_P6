
package tools;

import comprador.Puja;
import java.util.ArrayList;
import java.util.HashMap;
import vendedor.Subasta;

public class Tools {
    
    private static int contIdVendedores = 0;
    private static int subastaID = 0;
    
    public static int generarIdVendedor(){
        contIdVendedores++;
        return contIdVendedores;
    }
    
    public static int generarIdSubasta(){
        subastaID++;
        return subastaID;
    }
    
    /*public static Puja messageToPuja(String msg){
        String[] msgArray = msg.split("@");
        String titulo = msgArray[0];
        Double precio = Double.parseDouble(msgArray[1]);
        Puja puja = new Puja(titulo,precio);
        return puja;
    }
    
    public static String pujaToMessage(Puja puja){
        String msg = puja.getLibro() + "@" + puja.getPrecio();
        return msg;
    }*/
    
    public static String subastaToMessage(Subasta subasta){
        String msg = subasta.getId() + "@" + subasta.getLibro()+"@"+subasta.getPrecio();
        return msg;
    }
    
    public static Integer getIdFromMessage(String msg){
        String [] m = msg.split("@");
        return Integer.parseInt(m[0]);
    }
    
    public static String getLibroFromMessage(String msg){
        String [] m = msg.split("@");
        return m[1];
    }
    
    public static Double getPrecioFromMessage(String msg){
        String [] m = msg.split("@");
        return Double.parseDouble(m[2]);
    }
}
