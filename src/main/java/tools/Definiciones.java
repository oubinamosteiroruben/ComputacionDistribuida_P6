
package tools;

public class Definiciones {
    
    public static Integer MSJ_ID_EXITO = 0;
    public static Integer MSJ_ID_FRACASO = 1;
    
    public static String MSJ_TXT_EXITO = "OK";
    public static String MSJ_TXT_SUBASTA_EXISTE = "Subasta existente!";
    public static String MSJ_TXT_ERROR = "ERROR";

    public static String MSJ_TXT_GANADOR = "Ganador de la subasta del libro ";
    
    public static String generarMensajeGanadorRonda(String titulo){
        return MSJ_TXT_GANADOR + titulo;
    }
    
    public static long TIMEOUT = 10000;
}
