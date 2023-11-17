package AutoDriveEditor.MapPanel.routeNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RouteNodesTableModelTest {

    private RouteNodesTableModel nodesTableModel;
    private final int testId = 7680;
    private int testRowId;
    private final MapNode testNode = new MapNode(testId, 47.39939, 8.44171, 391.2, 0, false, false);

    @BeforeEach
    void setUp() {
        nodesTableModel = new RouteNodesTableModel();

        // NOTE: The app generally assumes that the position in the LinkedList is equal to the node's ID -1. The table should be agnostic to this, though.
        nodesTableModel.addNode(new MapNode(0, 0, 0, 0, 0, false, false));
        nodesTableModel.addNode(new MapNode(testId - 1, 0, 0, 0, 0, false, false));
        nodesTableModel.addNode(new MapNode(testId + 1, 0, 0, 0, 0, false, false));
        nodesTableModel.addNode(new MapNode(Integer.MAX_VALUE, 0, 0, 0, 0, false, false));

        // add testID out of sequence, with marker
        testNode.createMapMarker("Test Marker", "Test Group", Arrays.asList(20, 30, 33));
        nodesTableModel.addNode(testNode);
        testRowId = 4;
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addNode() {
        assertEquals(5, nodesTableModel.getRowCount());
    }

    @Test
    void removeNode() {
        nodesTableModel.removeNode(testNode);
        assertEquals(4, nodesTableModel.getRowCount());
    }

    @Test
    void getMapNodeById() {
        MapNode gotNode = nodesTableModel.getMapNodeById(testId);
        assertEquals(testId, gotNode.id);

        gotNode = nodesTableModel.getMapNodeById(-1);
        assertNull(gotNode);
    }

    @Disabled("Todo")
    @Test
    void updateNode() {

    }

    @Test
    void removeAllNodes() {
        nodesTableModel.removeAllNodes();
        assertEquals(0, nodesTableModel.getRowCount());
    }

    @Disabled("Todo")
    @Test
    void updateAllNodes() {
    }

    @Test
    void getValueAt() {
        assertEquals(testId, nodesTableModel.getValueAt(testRowId, 0));
        assertEquals(testNode.x, nodesTableModel.getValueAt(testRowId, 1));
        assertEquals(testNode.y, nodesTableModel.getValueAt(testRowId, 2));
        assertEquals(testNode.z, nodesTableModel.getValueAt(testRowId, 3));
        assertTrue(testNode.hasMapMarker());
        assertEquals(testNode.getMarkerName(), nodesTableModel.getValueAt(testRowId, 4));
        assertEquals(testNode.getMarkerGroup(), nodesTableModel.getValueAt(testRowId, 5));
        String parkedVehicles = testNode.getParkedVehiclesList().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        assertEquals(parkedVehicles, nodesTableModel.getValueAt(testRowId, 6));
    }

    @Disabled("Editing not implemented yet.")
    @Test
    void setValueAt() {
        MapNode newNode = new MapNode(999, 10, 29, 93, 0, false, false);
        newNode.createMapMarker("Different Marker", "Different Group", Arrays.asList(1, 2, 3));

        int col = 0;

        nodesTableModel.setValueAt(newNode.id, testRowId, col);
        assertEquals(newNode.id, nodesTableModel.getValueAt(testRowId, col));
        col++;

        nodesTableModel.setValueAt(newNode.x, testRowId, col);
        assertEquals(newNode.x, nodesTableModel.getValueAt(testRowId, col));
        col++;


        nodesTableModel.setValueAt(newNode.y, testRowId, col);
        assertEquals(newNode.y, nodesTableModel.getValueAt(testRowId, col));
        col++;


        nodesTableModel.setValueAt(newNode.z, testRowId, col);
        assertEquals(newNode.z, nodesTableModel.getValueAt(testRowId, col));
        col++;

        assertTrue(testNode.hasMapMarker());
        assertTrue(newNode.hasMapMarker());

        nodesTableModel.setValueAt(newNode.getMarkerName(), testRowId, col);
        assertEquals(testNode.getMarkerName(), nodesTableModel.getValueAt(testRowId, col));
        col++;

        nodesTableModel.setValueAt(newNode.getMarkerGroup(), testRowId, col);
        assertEquals(testNode.getMarkerGroup(), nodesTableModel.getValueAt(testRowId, col));

//       col++;
//       nodesTableModel.setValueAt(, testRowId, col);
//       assertEquals(, nodesTableModel.getValueAt(testRowId, col));
    }
}