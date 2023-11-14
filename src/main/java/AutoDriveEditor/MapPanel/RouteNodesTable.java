package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import java.util.stream.Collectors;

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
    private final JTable table;
    private final ColumnWidthManager columnWidthManager;
    private final FilterButtonPanel filterButtonPanel;
    // Debug privately for now
    private final boolean bDebugRouteNodesTable = false;

    public RouteNodesTable() {

        this.setLayout(new BorderLayout());
        tableModel = new RouteNodesTableModel();
        table = new JTable(tableModel);
        columnWidthManager = new ColumnWidthManager();

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane, BorderLayout.CENTER);

        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new UpdateButtonListener());
        this.add(buttonUpdate, BorderLayout.SOUTH);

        filterButtonPanel = new FilterButtonPanel();
        this.add(filterButtonPanel, BorderLayout.NORTH);
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
            columnWidthManager.packColumns();
        }
    }

    public void unloadRoadMap() {
        // clear active sorter and filter
        if (table.getRowSorter() != null) {
            filterButtonPanel.setSelectedFilter(FilterButtonPanel.FILTER_CLEAR);
        }

        tableModel.removeAllNodes();
        columnWidthManager.packColumns();
        filterButtonPanel.setSelectedFilter(FilterButtonPanel.FILTER_ALL);
    }

    public void refreshRoadMap() {
        tableModel.updateAllNodes();
        columnWidthManager.packColumns();
    }

    /**
     * This method gets called when a bound property is changed.
     * TODO: Delegate logic to utility functions like loadRoadMap() or unloadRoadMap()
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
                columnWidthManager.packColumns();
                break;
            case "networkNodesList.add":
                tableModel.addNode((MapNode) evt.getNewValue());
                columnWidthManager.updateMaxColumnWidth(tableModel.getRowCount() - 1);
                break;
            case "networkNodesList.removeAll":
                for (MapNode node : safeCastToLinkedListMapNode(evt.getOldValue())) {
                    tableModel.removeNode(node);
                }
                columnWidthManager.packColumns();
                break;
            case "networkNodesList.addAll":
                for (MapNode node : safeCastToLinkedListMapNode(evt.getNewValue())) {
                    tableModel.addNode(node);
                }
                columnWidthManager.packColumns();
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
                refreshRoadMap();
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

    private void setTableFilter() {
        switch (filterButtonPanel.activeFilter) {
            case FilterButtonPanel.FILTER_CLEAR:
                table.setRowSorter(null);
                break;
            case FilterButtonPanel.FILTER_ALL:
                table.setAutoCreateRowSorter(true);
                break;
            case FilterButtonPanel.FILTER_MARKERS:
                table.setRowSorter(getFilterColumnNotEmpty(4));
                break;
            case FilterButtonPanel.FILTER_PARKING:
                table.setRowSorter(getFilterColumnNotEmpty(6));
                break;
            default:
                break;
        }
    }

    private TableRowSorter<RouteNodesTableModel> getFilterColumnNotEmpty(int columnId) {
        TableRowSorter<RouteNodesTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(new RowFilter<RouteNodesTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends RouteNodesTableModel, ? extends Integer> entry) {
                boolean included = true;
                Object cellValue = entry.getModel().getValueAt(entry.getIdentifier(), columnId);
                if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                    included = false;
                }
                return included;
            }
        });
        return sorter;
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

        // TODO: Why doesn't fireTableDataChanged() work here?
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
                    LinkedList<Integer> list = safeCastToLinkedListInteger(node);
                    if (list != null) {
                        return list.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                    } else {
                        return null;
                    }
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
     * Maintains Column widths based on content.
     * Needs to be re-initialised, if columns are dynamically added to the model
     */
    private class ColumnWidthManager {
        Integer[] columnWidths;
        int padding = 5;

        public ColumnWidthManager() {
            columnWidths = new Integer[tableModel.getColumnCount()];
            packColumns();
        }

        public void packColumns() {
            int tableWidth = 0;

            for (int column = 0; column < columnWidths.length; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int preferredWidth = 15; // Set a minimum width

                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component comp = table.prepareRenderer(renderer, row, column);
                    preferredWidth = Math.max(comp.getPreferredSize().width + padding, preferredWidth);
                }

                // Min Width = header width
                preferredWidth = Math.max(preferredWidth, getColumnHeaderWidth(tableColumn));

                // Max Width
                if (preferredWidth > 300)
                    preferredWidth = 300;

                columnWidths[column] = preferredWidth;
                table.getColumnModel().getColumn(column).setPreferredWidth(preferredWidth);

                tableWidth += preferredWidth;
                // LOG.info("ColumnWidthManager.packColumns(): Column id {}\tWIDTH: calculated {}\tmin {}\tpref {}\tmax {}\tactual {}", tableColumn.getIdentifier(), preferredWidth, tableColumn.getMinWidth(),tableColumn.getPreferredWidth(),tableColumn.getMaxWidth(),tableColumn.getWidth());

            }
            LOG.info("ColumnWidthManager.packColumns(): Table WIDTH: calculated {}\tmin {}\tpref {}\tmax {}\tactual {}", tableWidth, table.getMinimumSize().width, table.getPreferredSize().width, table.getMaximumSize().width, table.getSize().width);
        }

        public void updateMaxColumnWidth(int row) {

            for (int column = 0; column < columnWidths.length; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int newColumnWidth = 15; // Set a minimum width

                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                newColumnWidth = Math.max(comp.getPreferredSize().width + padding, newColumnWidth);

                // Min Width = header width
                newColumnWidth = Math.max(newColumnWidth, getColumnHeaderWidth(tableColumn));

                // Max Width
                if (newColumnWidth > 300)
                    newColumnWidth = 300;

                if (newColumnWidth > columnWidths[column]) {
                    columnWidths[column] = newColumnWidth;
                    table.getColumnModel().getColumn(column).setPreferredWidth(newColumnWidth);
                }
            }
        }

        private int getColumnHeaderWidth(TableColumn col) {
            TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();

            Component comp = renderer.getTableCellRendererComponent(table,
                    col.getHeaderValue(), false, false, 0, 0);
            return comp.getPreferredSize().width;
        }
    }

    /**
     * Class to encapsulate the FilterPanel and its "Filter States"
     */
    private class FilterButtonPanel extends JPanel {
        // Filter states (enum not allowed for inner class at language level 11)
        public static final int FILTER_CLEAR = -1;
        public static final int FILTER_ALL = 0;
        public static final int FILTER_MARKERS = 1;
        public static final int FILTER_PARKING = 2;

        protected int activeFilter = FILTER_CLEAR;
        private ButtonGroup filterButtonGroup;


        public FilterButtonPanel() {
            filterButtonGroup = new ButtonGroup();

            FilterButton showAll = new FilterButton("Show All", FILTER_ALL);
            FilterButton showMarkers = new FilterButton("Show Markers", FILTER_MARKERS);
            FilterButton showParking = new FilterButton("Show Parking", FILTER_PARKING);

            // Add radio buttons to the button group
            filterButtonGroup.add(showAll);
            filterButtonGroup.add(showMarkers);
            filterButtonGroup.add(showParking);

            // Add radio buttons to the panel
            this.add(showAll);
            this.add(showMarkers);
            this.add(showParking);

            // Add action listeners to the radio buttons
            showAll.addActionListener(new FilterButtonListener());
            showMarkers.addActionListener(new FilterButtonListener());
            showParking.addActionListener(new FilterButtonListener());

            showAll.setSelected(true);
        }

        /**
         * Set or update the current filter
         *
         * @param filterType    Identifies the filter to be set, enumerated in FILTER_*
         */
        public void setSelectedFilter(int filterType) {
            Enumeration<AbstractButton> buttons = filterButtonGroup.getElements();
            while (buttons.hasMoreElements()) {
                FilterButton button = (FilterButton) buttons.nextElement();
                // Note that FILTER_CLEAR will have no buttons selected
                button.setSelected(button.filterType == filterType);
            }

            activeFilter = filterType;
            setTableFilter();
        }

        /**
         * Filter Button adds filterType to link a button to its filter
         */
        private class FilterButton extends JRadioButton {
            protected final int filterType;
            public FilterButton(String text, int filterType) {
                super(text);
                this.filterType = filterType;
            }
            public void addActionListener (FilterButtonListener l) {
                l.setFilterType(filterType);
                super.addActionListener(l);
            }
        }

        /**
         * Filter button action listener
         */
        // Todo: Can't we do without storing the filtertype here?
        private class FilterButtonListener implements ActionListener {
            private int filterType = -1;
            public void setFilterType(int newFilterType) {
                filterType = newFilterType;
            }
            public int getFilterType() {
                return filterType;
            }

            public void actionPerformed(ActionEvent e) {
                activeFilter = this.filterType;
                setTableFilter();
            }

        }
    }

    /**
     * Action Listener
     */
    private class UpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refreshRoadMap();
        }
    }

}
