
package comprador;

public class Puja {
    
    private String libro;
    private Double precio;
    private Boolean ganador = false;
    
    public Puja(String libro, Double precioMax){
        this.libro = libro;
        this.precio = precioMax;
    }

    public String getLibro() {
        return libro;
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
    
    public void setGanador(Boolean ganador){
        this.ganador = ganador;
    }
    
    public Boolean isGanador(){
        return this.ganador;
    }
    
}
