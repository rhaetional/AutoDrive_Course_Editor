package AutoDriveEditor.GUI.RouteNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.CoordinateChanger;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.Markers.EditMarkerButton.editMarker;
import static AutoDriveEditor.GUI.MapPanel.setStale;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

/**
 * Table Model mapping MapNode to rows
 */
public class RouteNodesTableModel extends AbstractTableModel {
    private final Vector<MapNode> data = new Vector<>();
    private final String[] columns = {
            "Node ID",
            "X",
            "Y",
            "Z",
            "Marker Name",
            "Marker Group",
            "Parking Destination"};
//    private final Vector<TableModelListener> listeners = new Vector<>();

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

    /**
     * Add MapNode to model
     *
     * @param mapNode The MapNode to add
     */
    public void addNode(MapNode mapNode) {

        // new row index
        int rowIndex = data.size();
        data.add(mapNode);

        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Remove MapNode to model
     *
     * @param mapNode The MapNode to remove
     */
    public void removeNode(MapNode mapNode) {
        int rowIndex = data.indexOf(mapNode);
        data.remove(mapNode);

        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    /**
     * Get MapNode by its MapNode.id
     *
     * @param mapNodeID MapNode.id to find
     * @return MapNode with mapNodeID or null
     */
    public MapNode getMapNodeById(int mapNodeID) {
        Optional<MapNode> result = data.stream().parallel().filter(node -> node.id == mapNodeID).findFirst();
        return result.orElse(null);
    }

    /**
     * Update MapNode in model
     * NOTE: The nodes in the model (data) refer to the same objects as the roadMap. Thus, any changes
     * are immediately available. This function only informs any listeners (eg. view) of this change.
     *
     * @param mapNode The MapNode to update
     */
    public void updateNode(MapNode mapNode) {
        int rowIndex = data.indexOf(mapNode);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Removes all nodes from data model
     */
    public void removeAllNodes() {
        data.clear();
        fireTableDataChanged();
    }

    /**
     * Signals that data's changed to all table event listeners
     */
    public void updateAllNodes() {
        fireTableDataChanged();
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
                return node.hasMapMarker() ? new MarkerGroupCell(node.getMarkerGroup()) : new MarkerGroupCell("");
            case 6:
                if (node.isParkDestination()) {
                    List<Integer> list = node.getParkedVehiclesList();
                    if (list != null) {
                        return list.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                    }
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MapNode node = data.get(rowIndex);
        //MapNodeChanger mapNodeChange = new MapNodeChanger();

        suspendAutoSaving();

        switch (columnIndex) {
            case 0:
                // currently not editable
                node.id = (int) aValue;
                break;
            case 1:
                double newX = (double) aValue;
                changeManager.addChangeable(new CoordinateChanger(node, newX, node.y, node.z));
                node.x = newX;
                break;
            case 2:
                double newY = (double) aValue;
                changeManager.addChangeable(new CoordinateChanger(node, node.x, newY, node.z));
                node.y = newY;
                break;
            case 3:
                double newZ = (double) aValue;
                changeManager.addChangeable(new CoordinateChanger(node, node.x, node.y, newZ));
                node.z = newZ;
                break;
            case 4:
                String newMarkerName = (String) aValue;
                editMarker(node, newMarkerName, node.getMarkerGroup()); // reuse logic from EditMarkerButton
                node.setMarkerName(newMarkerName);
                break;
            case 5:
                String newMarkerGroup = ((MarkerGroupCell) aValue).getName();
                editMarker(node, node.getMarkerName(), newMarkerGroup); // reuse logic from EditMarkerButton
                node.setMarkerGroup(newMarkerGroup);
                break;
            case 6:
                // currently not editable
                node.setParkedVehiclesList(safeCastToLinkedListInteger(aValue));
                break;
        }

        // update table view
        fireTableRowsUpdated(rowIndex, rowIndex);
        // update MapPanel model
        setStale(true);
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public Class<?> getColumnClass(int columnIndex) {
        // return getValueAt(0, columnIndex).getClass();
        // Keeping static, to support empty table
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
            case 2:
            case 3:
                return Double.class;
            case 4:
            case 6:
                return String.class;
            case 5:
                return MarkerGroupCell.class;
            default:
                return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // columns 0 and 6 are read-only
        return (columnIndex != 0) && (columnIndex != 6);
    }
}
