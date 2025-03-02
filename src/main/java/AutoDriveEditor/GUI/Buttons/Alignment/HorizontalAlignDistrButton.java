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

public class HorizontalAlignDistrButton extends AlignBaseButton {

    // debug flag (local, hard-coded for now)
    private boolean DEBUG = false;

    public HorizontalAlignDistrButton(JPanel panel) {
        button = makeImageToggleButton("buttons/horizontaldistralign", "buttons/horizontaldistralign_selected", null, "align_horizontal_distribute_tooltip", "align_horizontal_distribute_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() {
        return "HorizontalAlignButton";
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
        return getLocaleString("align_horizontal_distribute_tooltip");
    }

    @Override
    protected void adjustNodesTo(MapNode toNode) {

        if (!multiSelectList.isEmpty()) {
            LOG.info("Horizontally Aligning'n'Distributing {} nodes at world Z coordinate {}", multiSelectList.size(), toNode.z);
            CoordinateChanger coordChange = new CoordinateChanger();

            // print original coord for debugging
            if (DEBUG)
                for (MapNode node : multiSelectList)
                    LOG.info("pre HAD x: {} y: {} z: {}", node.x, node.y, node.z);


            // Sort List by X coordinate
            multiSelectList.sort(Comparator.comparingDouble(value -> value.x));

            // calculate distance
            double distance = multiSelectList.getLast().x - multiSelectList.getFirst().x;
            int gapCount = multiSelectList.size() - 1;
            // calculate element step to three decimals
            double stepSize = distance / gapCount;
            double currentX = multiSelectList.getFirst().x;

            if (DEBUG)
                LOG.info("HAD distance: {} gaps : {} step-size: {} startingX: {}", distance, gapCount, stepSize, currentX);

            // apply by setting z = z(min)+element_step
            for (MapNode node : multiSelectList) {
                currentX = roundUpDoubleToDecimalPlaces(currentX, 3);
                coordChange.addCoordinateChange(node, currentX, node.y, toNode.z);

                node.z = toNode.z;
                node.x = currentX;
                currentX = currentX + stepSize;

                if (DEBUG) LOG.info("post HAD x: {} y: {} z: {}", node.x, node.y, node.z);
            }
            //update table view
            getMapPanel().getRoadMap().refreshTableNodeList(multiSelectList);

            changeManager.addChangeable(coordChange);
        }
    }
}
