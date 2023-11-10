package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import static AutoDriveEditor.GUI.GUIBuilder.routeNodesTable;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

/**
 * JPanel to display a JTable of the MapNodes currently stored in MapPanel.RoadMap.networkNodesList.
 * It implements PropertyChangeListener for RoadMap as subject to be informed when MapNodes are added or removed.
 * Updates to existing nodes are "caught" by overloading RoadMap.refresh(), as RoadMap has no awareness about these
 * changes to the networkNodesList
 */
public class RouteNodesTable extends JPanel implements PropertyChangeListener {
    private final RouteNodesTableModel tableModel;
    // Debug privately for now
    private final boolean bDebugRouteNodesTable = true;

    public RouteNodesTable() {

        this.setLayout(new BorderLayout());
        tableModel = new RouteNodesTableModel();
        JTable nodesTable = new JTable(tableModel);
        nodesTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(nodesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new UpdateButtonListener());
        this.add(buttonUpdate, BorderLayout.SOUTH);
    }

    public static RouteNodesTable getRouteNodesTable() {
        return routeNodesTable;
    }

    public void loadRoadMap(LinkedList<MapNode> roadList) {
        if (roadList != null) {
            // Data to be displayed in the JTable
            for (MapNode node : roadList) {
                tableModel.addNode(node);
            }
        }
    }

    public void unloadRoadMap() {
        tableModel.removeAllNodes();
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (bDebugRouteNodesTable)
            LOG.info("## bDebugRouteNodesTable ## Variation of {}\t\t({}\t->\t{})\t\tProperty in object {}", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue(), evt.getSource());

        if (Objects.equals(evt.getPropertyName(), "networkNodesList.remove")) {
            tableModel.removeNode((MapNode) evt.getOldValue());
        }
        if (Objects.equals(evt.getPropertyName(), "networkNodesList.add")) {
            tableModel.addNode((MapNode) evt.getNewValue());
        }
        if (Objects.equals(evt.getPropertyName(), "networkNodesList.replaceList")) {
            // clear existing rows
            if (evt.getOldValue() != null) {
                unloadRoadMap();
            }
            if ((evt.getNewValue() != null) && (evt.getNewValue() instanceof LinkedList<?>)) {
                loadRoadMap((LinkedList<MapNode>) evt.getNewValue());
            }
        }
        if (Objects.equals(evt.getPropertyName(), "networkNodesList.refreshList")) {
                tableModel.fireTableDataChanged();
        }
    }


    /**
     * Table Model mapping MapNode to rows
     */
    static class RouteNodesTableModel extends AbstractTableModel {
        private final Vector<MapNode> nodeRows = new Vector<>();
        private final String[] columnNames = {
                "Node ID",
                "X",
                "Y",
                "Z",
                "Marker Name",
                "Marker Group",
                "Parking Destination"};
        private final Vector<TableModelListener> listeners = new Vector<>();

        /**
         * @param node
         */
        public void addNode(MapNode node) {

            // new row index
            int index = nodeRows.size();
            nodeRows.add(node);

            // Event to create row at index
            TableModelEvent e = new TableModelEvent(this, index, index,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            // Send the event
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        /**
         * @param mapNode
         */
        public void removeNode(MapNode mapNode) {
            int rowIndex = nodeRows.indexOf(mapNode);
            nodeRows.remove(rowIndex);

            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public void updateNode(MapNode mapNode) {
            int rowIndex = nodeRows.indexOf(mapNode);
            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public void removeAllNodes() {
            synchronized (nodeRows) {
                nodeRows.removeAllElements();
            }
        }

        public void updateAllNodes() {
            int maxIndex = nodeRows.size() - 1;
            TableModelEvent e = new TableModelEvent(this, 0, maxIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return nodeRows.size();
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MapNode node = nodeRows.get(rowIndex);

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

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            MapNode node = nodeRows.get(rowIndex);
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

        //TODO: This does not seem to work. Revert to TableModel?
        @Override
        public void fireTableDataChanged() {
            super.fireTableDataChanged();
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

    }

    /**
     * Action Listener
     */
    private class UpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tableModel.updateAllNodes();
        }
    }

}
