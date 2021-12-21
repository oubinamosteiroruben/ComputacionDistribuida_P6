
package vendedor;

public class MensajeVendedor {
    
    private Integer tipo;
    private String mensaje;
    
    public MensajeVendedor(Integer tipo, String mensaje){
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
}
