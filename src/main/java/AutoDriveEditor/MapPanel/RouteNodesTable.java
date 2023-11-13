package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
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
    private static boolean markerFilterActive = false;
    private final RouteNodesTableModel tableModel;
    private final JTable table;
    // Debug privately for now
    private final boolean bDebugRouteNodesTable = false;

    public RouteNodesTable() {

        this.setLayout(new BorderLayout());
        tableModel = new RouteNodesTableModel();
        table = new JTable(tableModel);


        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane, BorderLayout.CENTER);

        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new UpdateButtonListener());
        this.add(buttonUpdate, BorderLayout.SOUTH);

        JButton buttonFilter = new JButton("Toggle Filter on Marker");
        buttonFilter.addActionListener(new FilterButtonListener());
        this.add(buttonFilter, BorderLayout.NORTH);
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
        // clear active sorter and filter
        if (table.getRowSorter() != null) {
            toggleMarkerFilter();
        }

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
        if (bDebugRouteNodesTable) {
            LOG.info("## bDebugRouteNodesTable ## Variation of {}\t\t({}\t->\t{})\t\tProperty in object {}", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue(), evt.getSource());
        }

        switch (evt.getPropertyName()) {
            case "networkNodesList.remove":
                tableModel.removeNode((MapNode) evt.getOldValue());
                break;
            case "networkNodesList.add":
                tableModel.addNode((MapNode) evt.getNewValue());
                break;
            case "networkNodesList.removeAll":
                for (MapNode node : safeCastToLinkedListMapNode(evt.getOldValue())) {
                    tableModel.removeNode(node);
                }
                break;
            case "networkNodesList.addAll":
                for (MapNode node : safeCastToLinkedListMapNode(evt.getNewValue())) {
                    tableModel.addNode(node);
                }
                break;
            case "networkNodesList.replaceList":
                // clear existing rows
                if (evt.getOldValue() != null) {
                    unloadRoadMap();
                }
                if (evt.getNewValue() != null) {
                    loadRoadMap(safeCastToLinkedListMapNode(evt.getNewValue()));
                }
                break;
            case "networkNodesList.refreshList":
                tableModel.updateAllNodes();
                break;
            default:
                LOG.warn("## bDebugRouteNodesTable ## Unhandled Property change for variation of {}\t\t({}\t->\t{})\t\tProperty in object {}", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue(), evt.getSource());
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private static LinkedList<MapNode> safeCastToLinkedListMapNode(Object unknownObject) {
        if (unknownObject instanceof LinkedList) {
            LinkedList<?> unknownList = (LinkedList<?>) unknownObject;
            if (!unknownList.isEmpty() && unknownList.get(0) instanceof MapNode) {
                return (LinkedList<MapNode>) unknownList;
            }
        }
        // Object of different type or LL is empty
        return null;
    }

    private void toggleMarkerFilter() {
        if (markerFilterActive) {
            table.setRowSorter(null);
        } else {
            TableRowSorter<RouteNodesTableModel> sorter = new TableRowSorter<>(tableModel);
            sorter.setRowFilter(new RowFilter<RouteNodesTableModel, Integer>() {
                @Override
                public boolean include(RowFilter.Entry<? extends RouteNodesTableModel, ? extends Integer> entry) {
                    boolean included = true;
                    Object cellValue = entry.getModel().getValueAt(entry.getIdentifier(), 4);
                    if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                        included = false;
                    }
                    return included;
                }
            });
            table.setRowSorter(sorter);
        }

        // toggle flag
        markerFilterActive = !markerFilterActive;
    }

    /**
     * Table Model mapping MapNode to rows
     */
    static class RouteNodesTableModel extends AbstractTableModel {
        private final Vector<MapNode> data = new Vector<>();
        private final String[] columns = {
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
            int index = data.size();
            data.add(node);

            // Event to create row at index
            TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            // Send the event
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
        }

        /**
         * @param mapNode
         */
        public void removeNode(MapNode mapNode) {
            int rowIndex = data.indexOf(mapNode);
            data.remove(rowIndex);

            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }

            //fireTableRowsDeleted(rowIndex, rowIndex);
        }

        public void updateNode(MapNode mapNode) {
            int rowIndex = data.indexOf(mapNode);
            // Fire a table model event to notify listeners that the data has changed
            TableModelEvent e = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
            //fireTableRowsUpdated(rowIndex,rowIndex);
        }

        public void removeAllNodes() {
            data.clear();
            fireTableDataChanged();
        }

        // TODO: Can this function be replaced by fireTableDataChanged();?
        public void updateAllNodes() {
            int maxIndex = data.size() - 1;
            TableModelEvent e = new TableModelEvent(this, 0, maxIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(e);
            }
            //fireTableDataChanged();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MapNode node = data.get(rowIndex);

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
            MapNode node = data.get(rowIndex);
            switch (columnIndex) {
                case 4:
                    node.setMarkerName((String) aValue);
                    break;
                case 5:
                    node.setMarkerGroup((String) aValue);
                    break;
                case 6:
                    node.setParkedVehiclesList(safeCastToLinkedListInteger(aValue));
                    break;
            }
        }

        @SuppressWarnings("unchecked")
        private static LinkedList<Integer> safeCastToLinkedListInteger(Object unknownObject) {
            if (unknownObject instanceof LinkedList) {
                LinkedList<?> unknownList = (LinkedList<?>) unknownObject;
                if (!unknownList.isEmpty() && unknownList.get(0) instanceof Integer) {
                    return (LinkedList<Integer>) unknownList;
                }
            }
            // Object of different type or LL is empty
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
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
            // columns 0 and 6 are read-only
            return (columnIndex != 0) && (columnIndex != 6);
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

    private class FilterButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            toggleMarkerFilter();
        }
    }
}
