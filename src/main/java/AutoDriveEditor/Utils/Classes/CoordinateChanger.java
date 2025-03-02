package AutoDriveEditor.Utils.Classes;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.isStale;
import static AutoDriveEditor.GUI.MapPanel.setStale;

/**
 * Coordinate changer is a generic class to incrementally store arbitrary changes to the coordinates of
 * multiple nodes.
 */
public class CoordinateChanger  implements ChangeManager.Changeable {
    private final Boolean isStale;
    private final LinkedList<Coordinates> nodeList;

    public CoordinateChanger(){
        super();
        this.isStale = isStale();
        this.nodeList = new LinkedList<>();
    }

    public CoordinateChanger(MapNode node, double newX, double newY, double newZ) {
        this();
        addCoordinateChange(node, newX, newY, newZ);
    }

    public void addCoordinateChange(MapNode node, double newX, double newY, double newZ) {
        this.nodeList.add(new Coordinates(node, newX, newY, newZ));
    }
    public void undo() {
        for (Coordinates storedNode : nodeList) {
            storedNode.mapNode.x -= storedNode.diffX;
            storedNode.mapNode.y -= storedNode.diffY;
            storedNode.mapNode.z -= storedNode.diffZ;
            //update table view
            getMapPanel().getRoadMap().refreshTableNode(storedNode.mapNode);
        }
        getMapPanel().repaint();
        setStale(this.isStale);
    }

    public void redo() {
        for (Coordinates storedNode : nodeList) {
            storedNode.mapNode.x += storedNode.diffX;
            storedNode.mapNode.y += storedNode.diffY;
            storedNode.mapNode.z += storedNode.diffZ;
            //update table view
            getMapPanel().getRoadMap().refreshTableNode(storedNode.mapNode);
        }
        getMapPanel().repaint();
        setStale(true);
    }

    private static class Coordinates {
        private final MapNode mapNode;
        private final double diffX;
        private final double diffY;
        private final double diffZ;

        public Coordinates(MapNode node, double newX, double newY, double newZ) {
            // Reference to node
            this.mapNode = node;
            // store difference in coordinates
            this.diffX = newX - node.x;
            this.diffY = newY - node.y;
            this.diffZ = newZ - node.z;
        }
    }
}