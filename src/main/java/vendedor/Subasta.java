
package vendedor;

import jade.core.AID;
import java.util.ArrayList;
import java.util.HashMap;
import tools.Tools;

public class Subasta {
    
    private final Integer id;
    private String libro;
    private double precio;
    private double precioAnterior;
    private double incremento;
    private HashMap<String,AID> participantes;
    private AID ganador;
    private Boolean finalizada;
    
    public Subasta(String libro, Double precio, Double incremento){
        this.libro = libro;
        this.precio = precio;
        this.incremento = incremento;
        this.participantes= new HashMap<>();
        this.ganador = null;
        this.id = Tools.generarIdSubasta();
        this.finalizada = false;
    }
    
    public Subasta(String libro, Double precio, Double incremento,HashMap<String,AID> participantes){
        this.libro = libro;
        this.precio = precio;
        this.incremento = incremento;
        this.participantes= participantes;
        this.ganador = null;
        this.id = Tools.generarIdSubasta();
        this.finalizada = false;
    }
    

    public String getLibro() {
        return libro;
    }

    public Boolean getFinalizada() {
        return finalizada;
    }

    public void setFinalizada(Boolean iniciada) {
        this.finalizada = iniciada;
    }

    public void setLibro(String libro) {
        this.libro = libro;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Double getIncremento() {
        return incremento;
    }

    public void setIncremento(Double incremento) {
        this.incremento = incremento;
    }
    
    public void anhadirParticante(AID participante){
        this.participantes.put(participante.getName(),participante);
    }
    
    public void setParticipantes(HashMap<String,AID> participantes){
        this.participantes = participantes;
    }
    
    public HashMap<String,AID> getParticipantes(){
        return this.participantes;
    }
    
    public void setGanador(AID ganador){
        this.ganador = ganador;
    }
    
    public AID getGanador(){
        return this.ganador;
    }
    
    public Boolean hayGanador(){
        return this.ganador == null;
    }
    
    public void limpiarGanador(){
        this.ganador = null;
    }
    
    public void setPrecioAnterior(Double precioAnterior){
        this.precioAnterior = precioAnterior;
    }
    
    public Double getPrecioAnterior(){
        return this.precioAnterior;
    }

    public Integer getId() {
        return id;
    }
    
    
    
    
}
