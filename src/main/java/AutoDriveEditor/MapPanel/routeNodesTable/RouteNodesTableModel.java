package AutoDriveEditor.MapPanel.routeNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

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

    /**
     * @param node
     */
    public void addNode(MapNode node) {

        // new row index
        int rowIndex = data.size();
        data.add(node);

        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * @param mapNode
     */
    public void removeNode(MapNode mapNode) {
        int rowIndex = data.indexOf(mapNode);
        data.remove(mapNode);

        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public MapNode getMapNodeById(int mapNodeID) {
        Optional<MapNode> result = data.stream().parallel().filter(node -> node.id == mapNodeID).findFirst();
        return result.orElse(null);
    }

    public void updateNode(MapNode mapNode) {
        int rowIndex = data.indexOf(mapNode);
        fireTableRowsUpdated(rowIndex,rowIndex);
    }

    public void removeAllNodes() {
        data.clear();
        fireTableDataChanged();
    }

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
                return node.hasMapMarker() ? node.getMarkerGroup() : "";
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

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // columns 0 and 6 are read-only
        // return (columnIndex != 0) && (columnIndex != 6);

        // disable editing until changes are updated to the model.
        return false;
    }
}
