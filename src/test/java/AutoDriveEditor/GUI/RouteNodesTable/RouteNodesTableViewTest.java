package AutoDriveEditor.GUI.RouteNodesTable;

import AutoDriveEditor.RoadNetwork.MapNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RouteNodesTableViewTest {
    private RouteNodesTableModel nodesTableModel;
    private RouteNodesTableView nodesTableView;
    private RouteNodesTable nodesTableController;

    @BeforeEach
    void setUp() {
        nodesTableModel = new RouteNodesTableModel();
        nodesTableController = new RouteNodesTable();
        nodesTableView = new RouteNodesTableView(nodesTableController, nodesTableModel);

        for (int i = 0; i < 13; i++) {
            MapNode mapNode = new MapNode(i + 1, 47.39939 - i, 8.44171 + i, 391.2 - i % 2 * 10 * i + (1 - i % 2) * 10 * i, 0, false, false);
            if (i % 2 == 1)
                mapNode.createMapMarker("Test Marker " + i, "Test Group " + i, Arrays.asList(20+i, 30-i, 33+i));

            nodesTableModel.addNode(mapNode);
        }
    }

    @Test
    void clearTableFilter() {
        nodesTableView.clearTableFilter();
        assertEquals(null, nodesTableView.getTable().getRowSorter());
    }

    @Disabled("Can't see the change from here. Leave for now, as minor function")
    @Test
    void restoreTableFilter() {
        RouteNodesTableView.NodeFilterType selectedFilter;
        selectedFilter = nodesTableView.clearTableFilter();
        nodesTableView.setTableFilter(selectedFilter);
    }

    @Disabled("Todo")
    @Test
    void tableChanged() {
    }

    @Disabled("Todo")
    @Test
    public void testFilterButton() {
//        nodesTableView.
    }

    @Disabled("Todo")
    @Test
    public void testDoubleClickTableMouseListener() {
        nodesTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nodesTableView.getTable().getSelectedRow();
                    if (row >= 0) {
                        fail("Unexpected double-click event");
                    }
                }
            }
        });
//        MouseEvent(Object source, int id, long when, int x, int y, int clickCount, int modifiers)
//        MouseEvent newMouseEvent = new MouseEvent((Component) nodesTableView.getTable(), MouseEvent.MOUSE_CLICKED, 0, 0, 0, 2, 1);
//        nodesTableView.getTable().dispatchEvent(new MouseEvent(nodesTableView.getTable(), MouseEvent.MOUSE_CLICKED, 0, 0, 0, 1, 1));
//        nodesTableView.dispatchEvent(new MouseEvent(nodesTableView, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 1, 1));
    }
}
