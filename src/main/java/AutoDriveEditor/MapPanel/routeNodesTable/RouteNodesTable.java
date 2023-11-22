package AutoDriveEditor.MapPanel.routeNodesTable;
// Packages to import

import AutoDriveEditor.MapPanel.MapPanel;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import static AutoDriveEditor.GUI.GUIBuilder.routeNodesTable;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

/**
 * Controller for JPanel (View) displaying a JTable of the MapNodes currently stored in MapPanel.RoadMap.networkNodesList.
 * Data is managed via RouteNodesTableModel.
 * It implements PropertyChangeListener for RoadMap as subject to be informed when MapNodes are added or removed.
 */
public class RouteNodesTable implements PropertyChangeListener {
    private final RouteNodesTableModel nodesTableModel;
    private final RouteNodesTableView nodesTableView;
    // Debug privately for now
    private final boolean bDebugRouteNodesTable = false;

    public RouteNodesTable() {
        this.nodesTableModel = new RouteNodesTableModel();
        this.nodesTableView = new RouteNodesTableView(this, nodesTableModel);
    }
    public RouteNodesTable(RouteNodesTableModel nodesTableModel, RouteNodesTableView nodesTableView) {
        this.nodesTableModel = nodesTableModel;
        this.nodesTableView = nodesTableView;
    }

    public static RouteNodesTable getRouteNodesTable() {
        return routeNodesTable;
    }

    /**
     * Loads LinkedList of MapNodes to the model
     * @param roadList MapNodes to be added to model
     */
    public void loadRoadMap(LinkedList<MapNode> roadList) {
        if (roadList != null) {
            // Data to be displayed in the JTable
            for (MapNode node : roadList) {
                nodesTableModel.addNode(node);
            }
            nodesTableView.resizeTableColumns();
        }
    }

    /**
     * Removes all MapNodes from model / view
     */
    public void unloadRoadMap() {
        RouteNodesTableView.NodeFilterType selectedFilter;
        selectedFilter = nodesTableView.clearTableFilter();

        nodesTableModel.removeAllNodes();

        nodesTableView.setTableFilter(selectedFilter);
        nodesTableView.resizeTableColumns();
    }

    /**
     * Refreshed data in model and, thus, view
     */
    public void refreshRoadMap() {
        nodesTableModel.updateAllNodes();
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
                nodesTableModel.removeNode((MapNode) evt.getOldValue());
                break;
            case "networkNodesList.add":
                nodesTableModel.addNode((MapNode) evt.getNewValue());
                break;
            case "networkNodesList.refresh":
                nodesTableModel.updateNode((MapNode) evt.getNewValue());
                break;
            case "networkNodesList.removeAll":
                for (MapNode node : safeCastToLinkedListMapNode(evt.getOldValue())) {
                    nodesTableModel.removeNode(node);
                }
                break;
            case "networkNodesList.addAll":
                if (evt.getNewValue() != null) {
                    loadRoadMap(safeCastToLinkedListMapNode(evt.getNewValue()));
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


    public void centreNodeInMapPanel(MapNode mapNode) {
        SwingUtilities.invokeLater(() -> MapPanel.centreNodeInMapPanel(mapNode));
    }

    public JPanel getView() {
        return nodesTableView;
    }
}
