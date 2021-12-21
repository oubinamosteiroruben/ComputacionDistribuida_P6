
package vendedor.gui;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;
import vendedor.Subasta;



public final class ModeloTablaSubastasActivasVendedor extends AbstractTableModel {

    private ArrayList<Subasta> subastas;
    private JFrame mainFrame;

    // ------------------------------------------------------------------------
    // ----------------------------- Constructor ------------------------------
    public ModeloTablaSubastasActivasVendedor() {
        this.subastas = new ArrayList<>();
        this.mainFrame = null;
    }
    
    /*public ModeloTablaSubastasActivasVendedor(HashMap<String,Subasta> subastas, JFrame mainFrame) {
        this.subastas = subastas;
        this.mainFrame = mainFrame;
    }
    
    public ModeloTablaSubastasActivasVendedor(JFrame mainFrame) {
        this.subastas = new HashMap<>();
        this.mainFrame = mainFrame;
    }*/

    // ------------------------------------------------------------------------
    // -------------------------- Overrides Getters ---------------------------
    @Override
    public final int getRowCount() {
        return this.subastas.size();
    }

    @Override
    public final int getColumnCount() {
        return 5;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object resultado=null;

        switch (columnIndex){
            case 0:
                resultado = this.subastas.get(rowIndex).getId();
                break;
            case 1: 
                resultado = this.subastas.get(rowIndex).getLibro();
                break;
            case 2:
                resultado = this.subastas.get(rowIndex).getPrecio();
                break;
            case 3:
                resultado = this.subastas.get(rowIndex).getIncremento();
                break;
            case 4:
                resultado = this.subastas.get(rowIndex).getParticipantes().size();
                break;
        }
        return resultado;
    }

    @Override
    public final String getColumnName(int col) {
        String nombre = null;

        switch (col){
            case 0: nombre = "ID"; break;
            case 1: nombre = "Libro"; break;
            case 2: nombre = "P"; break;
            case 3: nombre = "I"; break;
            case 4: nombre = "N"; break;
        }
        return nombre;
    }

    @Override
    public final Class getColumnClass(int col) {
        Class clase = null;

        switch (col){
            case 0: clase = java.lang.Integer.class; break;
            case 1: clase = java.lang.String.class; break;
            case 2: clase = java.lang.Double.class; break;
            case 3: clase = java.lang.Double.class; break;
            case 4: clase = java.lang.Integer.class; break;
        }
        return clase;
    }

    // ------------------------------------------------------------------------
    // ------------------------------ Funciones -------------------------------
    public final void setFilas(HashMap<String,HashMap<Integer,Subasta>> subastas) {
        if (subastas != null ) {
            this.subastas = new ArrayList<>();
            for(HashMap<Integer,Subasta> HS: subastas.values()){
                for(Subasta s: HS.values()){
                    if(s.getFinalizada()==false){
                        this.subastas.add(s);
                    }
                }
            }
            fireTableDataChanged();
        }
    }

    public final Subasta obtenerSubasta(int i) {
        return this.subastas.get(i);
    }

    public final void actualizarTabla() {
        fireTableDataChanged();
    }

    public final void nuevaSubasta(Subasta subasta) {
        this.subastas.add(subasta);
        fireTableRowsInserted(this.subastas.size() - 1, this.subastas.size() - 1);
    }
    
    public final void eliminarSubasta(Subasta subasta){
        for(Subasta s: this.subastas){
            if(s.getId().equals(subasta.getId())){
                this.subastas.remove(s);
                break;
            }
        }
        fireTableRowsInserted(this.subastas.size() - 1, this.subastas.size() - 1);
    }
 
}