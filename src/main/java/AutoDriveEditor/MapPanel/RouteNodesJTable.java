package AutoDriveEditor.MapPanel;
// Packages to import

import AutoDriveEditor.RoadNetwork.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class RouteNodesJTable {
    // frame
    JFrame f;
    // Table
    JTable j;

    // Constructor
    RouteNodesJTable(RoadMap roadMap) {
        // Frame initialization
        f = new JFrame();

        // Frame Title
        f.setTitle("Network Nodes List");

        // Data to be displayed in the JTable
        int nodeCount = RoadMap.networkNodesList.size();
        int i = 0;
        String[][] data = new String[nodeCount][6];
        for (MapNode node : RoadMap.networkNodesList) {
            data[i][0] = Integer.toString(node.id);
            data[i][1] = Double.toString(node.x);
            data[i][2] = Double.toString(node.y);
            data[i][3] = Double.toString(node.z);
            if (node.hasMapMarker()) {
                data[i][4] = node.getMarkerName();
                data[i][5] = node.getMarkerGroup();
            }

            i++;
        }

        // Column Names
        String[] columnNames = {"Node ID", "X", "Y", "Z","Marker Name","Marker group"};


        // Initializing the JTable
        j = new JTable(data, columnNames);
        j.setBounds(30, 40, 200, 300);
        j.setAutoCreateRowSorter(true);
        // adding it to JScrollPane
        JScrollPane sp = new JScrollPane(j);
        f.add(sp);
        // Frame Size
        f.setSize(500, 200);
        // Frame Visible = true
        f.setVisible(true);
    }

}
