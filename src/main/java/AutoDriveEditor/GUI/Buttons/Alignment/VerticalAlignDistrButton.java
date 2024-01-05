package AutoDriveEditor.GUI.Buttons.Alignment;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.CoordinateChanger;

import javax.swing.*;
import java.util.Comparator;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public class VerticalAlignDistrButton extends AlignBaseButton {

    // debug flag (local, hard-coded for now)
    private boolean DEBUG = false;

    public VerticalAlignDistrButton(JPanel panel) {
        button = makeImageToggleButton("buttons/verticalaligndistr", "buttons/verticalaligndistr_selected", null, "align_vertical_distribute_tooltip", "align_vertical_distribute_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() {
        return "VerticalAlignDistributeButton";
    }

    @Override
    public String getButtonAction() {
        return "ActionButton";
    }

    @Override
    public String getButtonPanel() {
        return "Alignment";
    }

    @Override
    public String getInfoText() {
        return getLocaleString("align_vertical_distribution_tooltip");
    }

    @Override
    protected void adjustNodesTo(MapNode toNode) {

        if (!multiSelectList.isEmpty()) {
            LOG.info("Vertically Aligning'n'Distributing {} nodes at world X coordinate {}", multiSelectList.size(), toNode.x);
            CoordinateChanger coordChange = new CoordinateChanger();

            // print original coord for debugging
            if (DEBUG)
                for (MapNode node : multiSelectList)
                    LOG.info("pre VAD x: {} y: {} z: {}", node.x, node.y, node.z);

            // Sort List by Z coordinate
            multiSelectList.sort(Comparator.comparingDouble(value -> value.z));

            // calculate distance
            double distance = multiSelectList.getLast().z - multiSelectList.getFirst().z;
            int gapCount = multiSelectList.size() - 1;
            // calculate element step to three decimals
            double stepSize = distance / gapCount;
            double currentZ = multiSelectList.getFirst().z;

            if (DEBUG)
                LOG.info("VAD distance: {} gaps : {} step-size: {} startingZ: {}", distance, gapCount, stepSize, currentZ);

            // apply by setting z = z(min)+element_step
            for (MapNode node : multiSelectList) {
                currentZ = roundUpDoubleToDecimalPlaces(currentZ, 3);
                coordChange.addCoordinateChange(node, toNode.x, node.y, currentZ);

                node.x = toNode.x;
                node.z = currentZ;
                currentZ = currentZ + stepSize;

                if (DEBUG) LOG.info("post VAD x: {} y: {} z: {}", node.x, node.y, node.z);
            }
            //update table view
            getMapPanel().getRoadMap().refreshTableNodeList(multiSelectList);

            changeManager.addChangeable(coordChange);
        }
    }
}
