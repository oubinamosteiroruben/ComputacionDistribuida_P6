

package vendedor.gui;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;
import vendedor.Subasta;



public final class ModeloTablaNotificacionesVendedor extends AbstractTableModel {

    private ArrayList<String> notificaciones;

    // ------------------------------------------------------------------------
    // ----------------------------- Constructor ------------------------------
    public ModeloTablaNotificacionesVendedor() {
        this.notificaciones = new ArrayList<>();
    }
    
    public ModeloTablaNotificacionesVendedor(ArrayList<String> notificaciones) {
        this.notificaciones = notificaciones;
    }
    // ------------------------------------------------------------------------
    // -------------------------- Overrides Getters ---------------------------
    @Override
    public final int getRowCount() {
        return this.notificaciones.size();
    }

    @Override
    public final int getColumnCount() {
        return 1;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object resultado=null;

        switch (columnIndex){
            case 0: 
                resultado = this.notificaciones.get(rowIndex);
                break;
        }
        return resultado;
    }

    @Override
    public final String getColumnName(int col) {
        String nombre = null;

        switch (col){
            case 0: nombre = ""; break;
        }
        return nombre;
    }

    @Override
    public final Class getColumnClass(int col) {
        Class clase = null;

        switch (col){
            case 0: clase = java.lang.String.class; break;
        }
        return clase;
    }

    // ------------------------------------------------------------------------
    // ------------------------------ Funciones -------------------------------
    public final void setFilas(ArrayList<String> notificaciones) {
        if (notificaciones != null ) {
            this.notificaciones = notificaciones;
            fireTableDataChanged();
        }
    }

    public final String obtenerNotificaiones(int i) {
        return this.notificaciones.get(i);
    }

    public final void actualizarTabla() {
        fireTableDataChanged();
    }

    public final void nuevaNotificaion(String notificacion) {
        ArrayList<String> notificacionAux = new ArrayList<>();
        notificacionAux.add(notificacion);
        for(String noti: this.notificaciones){
            notificacionAux.add(noti);
        }
        this.notificaciones = notificacionAux;
        fireTableRowsInserted(this.notificaciones.size() - 1, this.notificaciones.size() - 1);
    }
 
}