package AutoDriveEditor.MapPanel.routeNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

public class RouteNodesTableView extends JPanel implements TableModelListener {
    public enum NodeFilterType {
        FILTER_CLEAR(-1, "NULL"),
        FILTER_ALL(0,"Show All"),
        FILTER_MARKERS(1,"Show Markers"),
        FILTER_PARKING(2,"Show Parking");
        private String filterName;
        private int filterId;
        private NodeFilterType(int filterId, String filterName) {
            this.filterId = filterId;
            this.filterName = filterName;
        }
        public String getFilterName() {
            return filterName;
        }
        public int getFilterId() {
            return filterId;
        }
    }
    private final RouteNodesTableModel nodesTableModel;
    private final RouteNodesTable nodesTableController;
    private final JTable nodesTable;
    private final ColumnWidthManager columnWidthManager;
    private final FilterButtonPanel filterButtonPanel;

    public RouteNodesTableView(RouteNodesTable routeNodesTable, RouteNodesTableModel nodesTableModel) {
        this.nodesTableController = routeNodesTable;
        this.nodesTableModel = nodesTableModel;
        this.nodesTableModel.addTableModelListener(this);

        nodesTable = new JTable(nodesTableModel);
        columnWidthManager = new ColumnWidthManager();
        filterButtonPanel = new FilterButtonPanel();
        initializeUI();
    }


    /**
     * clearTableFilter() and setTableFilter() are used to get around issues caused by the filters
     * when all data is removed from the model.
     * TODO: There has to be a better way to avoid the above issue.
     *
     * @return NodeFilterType that was active before clearing
     */
    public NodeFilterType clearTableFilter() {
        NodeFilterType filterType = null;

        // clear active sorter and filter
        if (nodesTable.getRowSorter() != null) {
            filterType = filterButtonPanel.activeFilter;
            filterButtonPanel.setSelectedFilter(NodeFilterType.FILTER_CLEAR);
        }

        return filterType;
    }
    public void setTableFilter(NodeFilterType selectedFilter) {
        filterButtonPanel.setSelectedFilter(selectedFilter);
    }

    private void initializeUI() {

        this.setLayout(new BorderLayout());

        // configure table
        nodesTable.setIntercellSpacing(new Dimension(5, 0));
        nodesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        nodesTable.setAutoCreateRowSorter(true);
        nodesTable.setShowGrid(true);
        nodesTable.setGridColor(Color.gray);

        JScrollPane scrollPane = new JScrollPane(nodesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new UpdateButtonListener());
        this.add(buttonUpdate, BorderLayout.SOUTH);

        this.add(filterButtonPanel, BorderLayout.NORTH);

        nodesTable.addMouseListener(new TableMouseAdapter());
    }

    private void setTableFilter() {
        switch (filterButtonPanel.activeFilter) {
            case FILTER_CLEAR:
                nodesTable.setRowSorter(null);
                break;
            case FILTER_ALL:
                nodesTable.setAutoCreateRowSorter(true);
                break;
            case FILTER_MARKERS:
                nodesTable.setRowSorter(getFilterColumnNotEmpty(4));
                break;
            case FILTER_PARKING:
                nodesTable.setRowSorter(getFilterColumnNotEmpty(6));
                break;
            default:
                break;
        }
        //
        setColumnLayout();
    }

    private void setColumnLayout() {
        TableColumnModel columnModel = nodesTable.getColumnModel();
        int columnCount = columnModel.getColumnCount();

        // create column layout
        String[] columnOrder = new String[columnCount];
        if ((filterButtonPanel.activeFilter == NodeFilterType.FILTER_MARKERS) ||
                (filterButtonPanel.activeFilter == NodeFilterType.FILTER_PARKING)) {
            columnOrder[0] = nodesTableModel.getColumnName(0);   // Node ID
            columnOrder[1] = nodesTableModel.getColumnName(4);   // Marker Name
            columnOrder[2] = nodesTableModel.getColumnName(5);   // Marker Group
            columnOrder[3] = nodesTableModel.getColumnName(6);   // Parking Destination
            columnOrder[4] = nodesTableModel.getColumnName(1);   // X
            columnOrder[5] = nodesTableModel.getColumnName(2);   // Y
            columnOrder[6] = nodesTableModel.getColumnName(3);   // Z

        } else {
            // order as defined in table model
            for (int i = 0; i < columnCount; i++) {
                columnOrder[i] = nodesTableModel.getColumnName(i);
            }
        }

        for (int i = 0; i < columnCount; i++) {
            int currIndex = columnModel.getColumnIndex(columnOrder[i]);
            if (i != currIndex)
                columnModel.moveColumn(currIndex, i);
        }

    }

    private TableRowSorter<RouteNodesTableModel> getFilterColumnNotEmpty(int columnId) {
        TableRowSorter<RouteNodesTableModel> sorter = new TableRowSorter<>(nodesTableModel);
        sorter.setRowFilter(new RowFilter<>() {
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
     * This fine grain notification tells listeners the exact range
     * of cells, rows, or columns that changed.
     *
     * @param e a {@code TableModelEvent} to notify listener that a table model
     *          has changed
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        switch (e.getType()) {
            case TableModelEvent.INSERT:
                columnWidthManager.updateMaxColumnWidth(nodesTableModel.getRowCount() - 1);
                break;
            case TableModelEvent.DELETE:
                break;
            case TableModelEvent.UPDATE:
                if (e.getFirstRow() != e.getLastRow()) {
                    // refresh entire table. Crude, but sufficient for now.
                    columnWidthManager.packColumns();
                } else {
                    //update single row
                    columnWidthManager.updateMaxColumnWidth(e.getFirstRow());
                }
                break;
            default:
                break;
        }
        //LOG.info("tableChange fired. Type={}, Col={} FirstRow={} LastRow={}", e.getType(), e.getColumn(),e.getFirstRow(),e.getLastRow());

    }

    public JTable getTable() {
        return nodesTable;
    }

    public FilterButtonPanel getFilterButtonPanel() {
        return filterButtonPanel;
    }

    /**
     * Maintains Column widths based on content.
     * Needs to be re-initialised, if columns are dynamically added to the model
     */
    private class ColumnWidthManager {
        Integer[] columnWidths;
        int padding = 5;

        public ColumnWidthManager() {
            columnWidths = new Integer[nodesTableModel.getColumnCount()];
            packColumns();
        }

        public void packColumns() {
//            int tableWidth = 0;

            for (int column = 0; column < columnWidths.length; column++) {
                TableColumn tableColumn = nodesTable.getColumnModel().getColumn(column);
                int preferredWidth = 15; // Set a minimum width

                for (int row = 0; row < nodesTable.getRowCount(); row++) {
                    TableCellRenderer renderer = nodesTable.getCellRenderer(row, column);
                    Component comp = nodesTable.prepareRenderer(renderer, row, column);
                    preferredWidth = Math.max(comp.getPreferredSize().width + padding, preferredWidth);
                }

                // Min Width = header width
                preferredWidth = Math.max(preferredWidth, getColumnHeaderWidth(tableColumn));

                // Max Width
                if (preferredWidth > 300)
                    preferredWidth = 300;

                columnWidths[column] = preferredWidth;
                nodesTable.getColumnModel().getColumn(column).setPreferredWidth(preferredWidth);

//                tableWidth += preferredWidth;
//                 LOG.info("ColumnWidthManager.packColumns(): Column id {}\tWIDTH: calculated {}\tmin {}\tpref {}\tmax {}\tactual {}", tableColumn.getIdentifier(), preferredWidth, tableColumn.getMinWidth(),tableColumn.getPreferredWidth(),tableColumn.getMaxWidth(),tableColumn.getWidth());

            }
//             LOG.info("ColumnWidthManager.packColumns(): Table WIDTH: calculated {}\tmin {}\tpref {}\tmax {}\tactual {}", tableWidth, nodesTable.getMinimumSize().width, nodesTable.getPreferredSize().width, nodesTable.getMaximumSize().width, nodesTable.getSize().width);
        }

        public void updateMaxColumnWidth(int row) {

            for (int column = 0; column < columnWidths.length; column++) {
                TableColumn tableColumn = nodesTable.getColumnModel().getColumn(column);
                int newColumnWidth = 15; // Set a minimum width

                TableCellRenderer renderer = nodesTable.getCellRenderer(row, column);
                Component comp = nodesTable.prepareRenderer(renderer, row, column);
                newColumnWidth = Math.max(comp.getPreferredSize().width + padding, newColumnWidth);

                // Min Width = header width
                newColumnWidth = Math.max(newColumnWidth, getColumnHeaderWidth(tableColumn));

                // Max Width
                if (newColumnWidth > 300)
                    newColumnWidth = 300;

                if (newColumnWidth > columnWidths[column]) {
                    columnWidths[column] = newColumnWidth;
                    nodesTable.getColumnModel().getColumn(column).setPreferredWidth(newColumnWidth);
                }
            }
        }

        private int getColumnHeaderWidth(TableColumn col) {
            TableCellRenderer renderer = nodesTable.getTableHeader().getDefaultRenderer();

            Component comp = renderer.getTableCellRendererComponent(nodesTable,
                    col.getHeaderValue(), false, false, 0, 0);
            return comp.getPreferredSize().width;
        }
    }

    /**
     * Class to encapsulate the FilterPanel and its "Filter States"
     */
    private class FilterButtonPanel extends JPanel {

        protected NodeFilterType activeFilter = NodeFilterType.FILTER_CLEAR;
        private ButtonGroup filterButtonGroup;


        public FilterButtonPanel() {
            filterButtonGroup = new ButtonGroup();

            FilterButton showAll = new FilterButton(NodeFilterType.FILTER_ALL);
            FilterButton showMarkers = new FilterButton(NodeFilterType.FILTER_MARKERS);
            FilterButton showParking = new FilterButton(NodeFilterType.FILTER_PARKING);

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
         * @param filterType Identifies the filter to be set, enumerated in NodeFilterType
         */
        public void setSelectedFilter(NodeFilterType filterType) {
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
            protected final NodeFilterType filterType;

            public FilterButton(NodeFilterType filterType) {
                super(filterType.getFilterName());
                this.filterType = filterType;
            }

            public void addActionListener(FilterButtonListener l) {
                l.setFilterType(filterType);
                super.addActionListener(l);
            }
        }

        /**
         * Filter button action listener
         */
        // Todo: Can't we do without storing the filtertype here?
        private class FilterButtonListener implements ActionListener {
            private NodeFilterType filterType = NodeFilterType.FILTER_CLEAR;

            public void setFilterType(NodeFilterType newFilterType) {
                filterType = newFilterType;
            }

            public NodeFilterType getFilterType() {
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
            nodesTableController.refreshRoadMap();
        }
    }

    private class TableMouseAdapter implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            // If the double-clicked row contains a MapNode, get it
            if (e.getClickCount() == 2) {
                int row = nodesTable.getSelectedRow();
                if (row >= 0) {
                    int mapNodeId = (int) nodesTable.getValueAt(row, 0);
                    MapNode mapNode = nodesTableModel.getMapNodeById(mapNodeId);
                    nodesTableController.centreNodeInMapPanel(mapNode);
                }
            }
        }

        /**
         * Invoked when a mouse button has been pressed on a component.
         *
         * @param e the event to be processed
         */
        @Override
        public void mousePressed(MouseEvent e) {

        }

        /**
         * Invoked when a mouse button has been released on a component.
         *
         * @param e the event to be processed
         */
        @Override
        public void mouseReleased(MouseEvent e) {

        }

        /**
         * Invoked when the mouse enters a component.
         *
         * @param e the event to be processed
         */
        @Override
        public void mouseEntered(MouseEvent e) {

        }

        /**
         * Invoked when the mouse exits a component.
         *
         * @param e the event to be processed
         */
        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
