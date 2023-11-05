package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Vector;

public class RouteNodesJTable {
    // frame
    JFrame f;
    // Table
    JTable j;

    // Constructor
    RouteNodesJTable(RoadMap roadMap) {
        final RouteNodesTableModel model = new RouteNodesTableModel();
        // Frame initialization
        f = new JFrame();

        // Frame Title
        f.setTitle("Network Nodes List");

        // Initializing the JTable
        j = new JTable(model);
        j.setBounds(30, 40, 200, 300);
        j.setAutoCreateRowSorter(true);

        // Data to be displayed in the JTable
        for (MapNode node : RoadMap.networkNodesList) {
            model.addNode(node);
        }

        // adding it to JScrollPane
        JScrollPane sp = new JScrollPane(j);
        f.add(sp);
        // Frame Size
        f.setSize(1024, 512);
        // Frame Visible = true
        f.setVisible(true);
    }

    // Unsere Implementation des TableModels
    class RouteNodesTableModel implements TableModel {
        private Vector<MapNode> nodes = new Vector<>();
        private Vector<TableModelListener> listeners = new Vector<>();

        public void addNode(MapNode node) {
            // Das wird der Index des Vehikels werden
            int index = nodes.size();
            nodes.add(node);

            // Jetzt werden alle Listeners benachrichtigt

            // Zuerst ein Event, "neue Row an der Stelle index" herstellen
            TableModelEvent e = new TableModelEvent(this, index, index,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            // Nun das Event verschicken
            for (int i = 0, n = listeners.size(); i < n; i++) {
                ((TableModelListener) listeners.get(i)).tableChanged(e);
            }
        }

        // Die Anzahl Columns
        public int getColumnCount() {
            return 7;
        }

        // Die Anzahl Vehikel
        public int getRowCount() {
            return nodes.size();
        }

        // Die Titel der einzelnen Columns
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Node ID";
                case 1:
                    return "X";
                case 2:
                    return "Y";
                case 3:
                    return "Z";
                case 4:
                    return "Marker Name";
                case 5:
                    return "Marker Group";
                case 6:
                    return "Parking Destination";
                default:
                    return null;
            }
        }

        // Der Wert der Zelle (rowIndex, columnIndex)
        public Object getValueAt(int rowIndex, int columnIndex) {
            MapNode node = (MapNode) nodes.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return node.id;
                case 1:
                    return node.x;
                case 2:
                    return node.y;
                case 3:
                    return node.z;
                case 4:
                    return node.hasMapMarker() ? node.getMarkerName() : "";
                case 5:
                    return node.hasMapMarker() ? node.getMarkerGroup() : "";
                case 6:
                    return node.isParkDestination() ? node.getParkedVehiclesList().toString() : "";
                default:
                    return null;
            }
        }

        // Eine Angabe, welchen Typ von Objekten in den Columns angezeigt werden soll
        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Integer.class;
                case 1:
                    return Double.class;
                case 2:
                    return Double.class;
                case 3:
                    return Double.class;
                case 4:
                    return String.class;
                case 5:
                    return String.class;
                case 6:
                    return String.class;
                default:
                    return null;
            }
        }

        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }


        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return false;
            else
                return true;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            MapNode node = nodes.get(rowIndex);
            switch (columnIndex) {
                case 4:
                    node.setMarkerName((String) aValue);
                    break;
                case 5:
                    node.setMarkerGroup((String) aValue);
                    break;
            }
        }
    }

}
