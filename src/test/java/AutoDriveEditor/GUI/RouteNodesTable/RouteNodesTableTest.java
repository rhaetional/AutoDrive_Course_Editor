package AutoDriveEditor.GUI.RouteNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteNodesTableTest {
    private RouteNodesTableModel nodesTableModel;
    private RouteNodesTableView nodesTableView;
    private RouteNodesTable nodesTableController;
    private LinkedList<MapNode> testRoadMap;
    private final int testRoadMapSize = 13;

    @BeforeEach
    void setUp() {
        nodesTableModel = new RouteNodesTableModel();
        nodesTableView = new RouteNodesTableView(nodesTableController, nodesTableModel);
        nodesTableController = new RouteNodesTable(nodesTableModel,nodesTableView);

        testRoadMap = new LinkedList<>();
        for (int i = 0; i < testRoadMapSize; i++) {
            MapNode mapNode = new MapNode(i + 1, 47.39939 - i, 8.44171 + i, 391.2 - i % 2 * 10 * i + (1 - i % 2) * 10 * i, 0, false, false);
            if (i % 2 == 1)
                mapNode.createMapMarker("Test Marker " + i, "Test Group " + i, Arrays.asList(20+i, 30-i, 33+i));

            testRoadMap.add(mapNode);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void loadRoadMap() {
        nodesTableController.loadRoadMap(testRoadMap);
        assertEquals(testRoadMapSize, nodesTableModel.getRowCount());
    }

    @Test
    void unloadRoadMap() {
        nodesTableController.loadRoadMap(testRoadMap);
        assertEquals(testRoadMapSize, nodesTableModel.getRowCount());
        nodesTableController.unloadRoadMap();
        assertEquals(0, nodesTableModel.getRowCount());
    }

    @Disabled("Todo")
    @Test
    void refreshRoadMap() {
    }

    @Disabled("Todo")
    @Test
    void propertyChange() {
    }
}