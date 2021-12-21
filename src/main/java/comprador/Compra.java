
package comprador;

public class Compra {
    
    private String libro;
    private Double precio;
    
    public Compra(String libro, Double precio){
        this.precio = precio;
        this.libro = libro;
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
    
}
