package AutoDriveEditor.GUI.RouteNodesTable;

// Packages to import

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.routeNodesTable;
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
     * This method gets called when a bound property is changed. It is used to notify / update the table model when the
     * road map is modified in the MapPanel or a new file was loaded, for example.
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
    private static LinkedList<MapNode> safeCastToLinkedListMapNode(Object unknownObject) throws ClassCastException {
        if (unknownObject == null) {
            return null;
        }

        boolean invalidClass = true;
        if (unknownObject instanceof LinkedList) {
            invalidClass = false;
            LinkedList<?> unknownList = (LinkedList<?>) unknownObject;
            if (!unknownList.isEmpty() && unknownList.get(0) instanceof MapNode) {
                return (LinkedList<MapNode>) unknownList;
            }
        } else if (unknownObject instanceof ArrayList) {
            invalidClass = false;
            // with 1.0.7, ArrayLists are used in some contexts
            LinkedList<?> unknownList = new LinkedList<>((ArrayList<?>) unknownObject);
            if (!unknownList.isEmpty() && unknownList.get(0) instanceof MapNode) {
                return (LinkedList<MapNode>) unknownList;
            }
        }
        if (invalidClass) {
            throw new ClassCastException("safeCastToLinkedListMapNode cannot cast " + unknownObject.getClass());
        }
        // return empty list
        return new LinkedList<>();
    }


    public void centreNodeInMapPanel(MapNode mapNode) {
        SwingUtilities.invokeLater(() -> getMapPanel().centreNodeInMapPanel(mapNode));
    }

    public JPanel getView() {
        return nodesTableView;
    }

    public RouteNodesTableModel getModel() {
        return nodesTableModel;
    }
}
