package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import static AutoDriveEditor.GUI.GUIBuilder.routeNodesTable;

public class RouteNodesTable extends JPanel {
    private final RoadMap roadMap;
    private final RouteNodesTableModel tableModel;

    // Constructor
    public RouteNodesTable(RoadMap roadMap) {
        this.roadMap = roadMap;

        this.setLayout(new BorderLayout());

        tableModel = new RouteNodesTableModel();

        // Initializing the JTable
        JTable nodesTable = new JTable(tableModel);
        nodesTable.setAutoCreateRowSorter(true);

        // adding table to JScrollPane
        JScrollPane sp = new JScrollPane(nodesTable);
        // add to panel
        this.add(sp, BorderLayout.CENTER);

        //
        // Test Button
        final JButton buttonUpdate = new JButton( "Update" );
        buttonUpdate.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                tableModel.updateAllNodes();
            }
        });
        this.add(buttonUpdate, BorderLayout.SOUTH);
        //
        //
    }

    public static RouteNodesTable getRouteNodesTable() {
        return routeNodesTable;
    }

    public void loadRoadMap(RoadMap roadMap) {
        tableModel.removeAllNodes();
        if (roadMap != null) {
            // Data to be displayed in the JTable
            for (MapNode node : RoadMap.networkNodesList) {
                tableModel.addNode(node);
            }
        }
    }

    /**
     * Table Model mapping MapNode to rows
     */
    static class RouteNodesTableModel implements TableModel {
        private final Vector<MapNode> nodes = new Vector<>();
        private final Vector<TableModelListener> listeners = new Vector<>();

        public void addNode(MapNode node) {

            // new row index
            int index = nodes.size();
            nodes.add(node);

            // Event to create row at index
            TableModelEvent e = new TableModelEvent(this, index, index,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            // Send the event
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public void removeNode(int rowIndex) {
            nodes.remove(rowIndex);

            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }
        public void updateNode(MapNode mapNode) {
            int rowIndex = nodes.indexOf(mapNode);
            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public void removeAllNodes() {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                this.removeNode(i);
            }
        }

        public void updateAllNodes() {
            int maxIndex = nodes.size() - 1;
            TableModelEvent e = new TableModelEvent(this, 0, maxIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public int getColumnCount() {
            return 7;
        }

        public int getRowCount() {
            return nodes.size();
        }

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

        public Object getValueAt(int rowIndex, int columnIndex) {
            MapNode node = nodes.get(rowIndex);

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
                case 6:
                    node.setParkedVehiclesList((List<Integer>) aValue);
                    break;
            }
        }
    }

}
