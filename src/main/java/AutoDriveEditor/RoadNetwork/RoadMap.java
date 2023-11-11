package AutoDriveEditor.RoadNetwork;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.MapPanel.MapPanel.getYValueFromHeightMap;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.limitDoubleToDecimalPlaces;

public class RoadMap {

    public static String mapName;
    public static LinkedList<MapNode> networkNodesList;
    public static UUID uuid;

    private static PropertyChangeSupport pcs;

    public RoadMap() {
        networkNodesList = new LinkedList<>();
        mapName = null;
        pcs = new PropertyChangeSupport(this);

        // generate a unique random UUID, we can use this to compare and detect when
        // a different config has been loaded.

        // The UUID is used by the undo/redo system to detect if the current function
        // it is trying to complete an operation on a config that is no longer loaded
        // into the editor. A Dialog box will display explaining why the error occurred.

        uuid = UUID.randomUUID();
    }

    public static MapNode createMapNode(int id, double x, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(getYValueFromHeightMap(x, z),3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        return new MapNode(id, xPos, yPos, zPos, nodeType, isSelected, isControlNode);
    }

    public static MapNode createMapNode(int id, double x, double y, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(y,3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        return new MapNode(id, xPos, yPos, zPos, nodeType, isSelected, isControlNode);
    }

    public static MapNode createNewNetworkNode(double x, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        MapNode createdNode = createMapNode(RoadMap.networkNodesList.size() + 1, x, z, nodeType, isSelected, isControlNode);
        RoadMap.networkNodesList.add(createdNode);
        pcs.firePropertyChange("networkNodesList.add", null,createdNode);
        return createdNode;
    }

    public static MapNode createNewNetworkNode(double x, double y, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        MapNode createdNode = createMapNode(RoadMap.networkNodesList.size() + 1, x, y, z, nodeType, isSelected, isControlNode);
        RoadMap.networkNodesList.add(createdNode);
        pcs.firePropertyChange("networkNodesList.add", null,createdNode);
        return createdNode;
    }

    public static MapNode createControlNode(double x, double z) {
        return new MapNode(-99, x, 0, z, NODE_FLAG_STANDARD, false, true);
    }

    public static void addMapNode(MapNode newNode) {
        networkNodesList.add(newNode);
        pcs.firePropertyChange("networkNodesList.add", null,newNode);
    }

    public static void addAll(LinkedList<MapNode> nodes) {
        networkNodesList.addAll(nodes);
        pcs.firePropertyChange("networkNodesList.addAll", null, nodes);
    }

    public static void removeAll(LinkedList<MapNode> nodes) {
        networkNodesList.removeAll(nodes);
        pcs.firePropertyChange("networkNodesList.removeAll", nodes, null);
    }

    public void insertMapNode(MapNode toAdd, LinkedList<MapNode> otherNodesInList, LinkedList<MapNode> otherNodesOutList) {

        // starting at the index of where we need to insert the node
        // increment the ID's of all nodes to the right of the mapNodes by +1
        // so when we insert the node, all the id's match their index

        ListIterator<MapNode> roadMap = networkNodesList.listIterator(toAdd.id - 1);
        while (roadMap.hasNext()) {
            MapNode node = roadMap.next();
            node.id++;
        }

        // insert the MapNode into the list

        if (bDebugLogUndoRedo) LOG.info("## insertMapNode() ## inserting index {} ( ID {} ) into mapNodes", toAdd.id - 1, toAdd.id );
        networkNodesList.add(toAdd.id -1 , toAdd);
        pcs.firePropertyChange("networkNodesList.add", null,toAdd);

        //now we need to restore all the connections that went from/to it

        if (otherNodesInList != null) {
            for (MapNode otherInNode : otherNodesInList) {
                if (!otherInNode.incoming.contains(toAdd)) otherInNode.incoming.add(toAdd);
            }
        }

        if (otherNodesOutList != null) {
            for (MapNode otherOutNode : otherNodesOutList) {
                if (!otherOutNode.outgoing.contains(toAdd)) otherOutNode.outgoing.add(toAdd);
            }
        }
    }

    public static void removeMapNode(MapNode toDelete) {
        for (MapNode mapNode : networkNodesList) {
            mapNode.outgoing.remove(toDelete);
            mapNode.incoming.remove(toDelete);
            if (mapNode.id > toDelete.id) {
                mapNode.id--;
            }
        }

        networkNodesList.remove(toDelete);
        pcs.firePropertyChange("networkNodesList.remove", toDelete, null);
    }

    public static boolean isDual(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.incoming.contains(start) && target.outgoing.contains(start) && start.incoming.contains(target);
    }

    public static boolean isReverse(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && !target.incoming.contains(start);
    }

    // setters

    public static void setRoadMapNodes(RoadMap roadMap, LinkedList<MapNode> mapNodes) {
        pcs.firePropertyChange("networkNodesList.replaceList", networkNodesList, mapNodes);
        networkNodesList = mapNodes;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void refreshListeners() {
        pcs.firePropertyChange("networkNodesList.refreshList", null, networkNodesList);
    }
}
