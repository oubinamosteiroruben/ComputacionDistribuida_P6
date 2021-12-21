
package comprador.gui;

import comprador.Puja;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;


public final class ModeloTablaPujasActivasComprador extends AbstractTableModel {

    private ArrayList<Puja> pujas;
    private JFrame mainFrame;

    // ------------------------------------------------------------------------
    // ----------------------------- Constructor ------------------------------
    public ModeloTablaPujasActivasComprador() {
        this.pujas = new ArrayList<>();
        this.mainFrame = null;
    }
    
    public ModeloTablaPujasActivasComprador(HashMap<String,Puja> pujas, JFrame mainFrame) {
        this.pujas = hashMapToArrayList(pujas);
        this.mainFrame = mainFrame;
    }
    
    public ModeloTablaPujasActivasComprador(JFrame mainFrame) {
        this.pujas = new ArrayList<>();
        this.mainFrame = mainFrame;
    }

    // ------------------------------------------------------------------------
    // -------------------------- Overrides Getters ---------------------------
    @Override
    public final int getRowCount() {
        return this.pujas.size();
    }

    @Override
    public final int getColumnCount() {
        return 3;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object resultado=null;

        switch (columnIndex){
            case 0: 
                resultado = this.pujas.get(rowIndex).getLibro();
                break;
            case 1:
                resultado = this.pujas.get(rowIndex).getPrecio();
                break;
            case 2:
                resultado = "";
                if(this.pujas.get(rowIndex).isGanador()){
                    resultado = "W";
                }
        }
        return resultado;
    }

    @Override
    public final String getColumnName(int col) {
        String nombre = null;

        switch (col){
            case 0: nombre = "Libro"; break;
            case 1: nombre = "Max"; break;
            case 2: nombre = ""; break;
        }
        return nombre;
    }

    @Override
    public final Class getColumnClass(int col) {
        Class clase = null;

        switch (col){
            case 0: clase = java.lang.String.class; break;
            case 1: clase = java.lang.Double.class; break;
            case 2: clase = java.lang.String.class; break;
        }
        return clase;
    }

    // ------------------------------------------------------------------------
    // ------------------------------ Funciones -------------------------------
    public final void setFilas(HashMap<String,Puja> pujas) {
        if (pujas != null ) {
            this.pujas = hashMapToArrayList(pujas);
            fireTableDataChanged();
        }
    }

    public final Puja obtenerPuja(int i) {
        return this.pujas.get(i);
    }

    public final void actualizarTabla() {
        fireTableDataChanged();
    }

    public final void nuevaPuja(Puja puja) {
        this.pujas.add(puja);
        fireTableRowsInserted(this.pujas.size() - 1, this.pujas.size() - 1);
    }
    
    public ArrayList<Puja> hashMapToArrayList(HashMap<String,Puja> map){
        ArrayList<Puja> pujas = new ArrayList<>();
        for(String p: map.keySet()){
            pujas.add(map.get(p));
        }
        return pujas;
    }
 
}