
package comprador.gui;


import comprador.Compra;
import comprador.Puja;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;


public final class ModeloTablaCompras extends AbstractTableModel {

    private ArrayList<Compra> compras;

    // ------------------------------------------------------------------------
    // ----------------------------- Constructor ------------------------------
    public ModeloTablaCompras() {
        this.compras = new ArrayList<>();
    }

    // ------------------------------------------------------------------------
    // -------------------------- Overrides Getters ---------------------------
    @Override
    public final int getRowCount() {
        return this.compras.size();
    }

    @Override
    public final int getColumnCount() {
        return 2;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object resultado=null;

        switch (columnIndex){
            case 0: 
                resultado = this.compras.get(rowIndex).getLibro();
                break;
            case 1:
                resultado = this.compras.get(rowIndex).getPrecio();
                break;
        }
        return resultado;
    }

    @Override
    public final String getColumnName(int col) {
        String nombre = null;

        switch (col){
            case 0: nombre = "Libro"; break;
            case 1: nombre = "Precio"; break;
        }
        return nombre;
    }

    @Override
    public final Class getColumnClass(int col) {
        Class clase = null;

        switch (col){
            case 0: clase = java.lang.String.class; break;
            case 1: clase = java.lang.Double.class; break;
        }
        return clase;
    }

    // ------------------------------------------------------------------------
    // ------------------------------ Funciones -------------------------------
    public final void setFilas(ArrayList<Compra> compras) {
        if (compras != null ) {
            this.compras = compras;
            fireTableDataChanged();
        }
    }

    public final void actualizarTabla() {
        fireTableDataChanged();
    }

    public final void nuevaCompra(Compra compra) {
        this.compras.add(compra);
        fireTableRowsInserted(this.compras.size() - 1, this.compras.size() - 1);
    }
 
}