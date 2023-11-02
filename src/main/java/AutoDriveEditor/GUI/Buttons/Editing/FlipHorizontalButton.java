package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.CoordinateChanger;

import javax.swing.*;
import java.util.Comparator;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public final class FlipHorizontalButton extends AlignBaseButton {

    //local debug flag
    private boolean DEBUG = false;

    public FlipHorizontalButton(JPanel panel) {
        button = makeImageToggleButton("buttons/flip_horizontal", "buttons/flip_horizontal_selected", null, "copypaste_flip_horizontal_tooltip", "copypaste_flip_horizontal_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() {
        return "FlipHorizontalButton";
    }

    @Override
    public String getButtonAction() {
        return "ActionButton";
    }

    @Override
    public String getButtonPanel() {
        return "Edit";
    }

    @Override
    public String getInfoText() {
        return getLocaleString("copypaste_flip_horizontal_tooltip");
    }

    @Override
    protected void adjustNodesTo(MapNode toNode) {

        if (!multiSelectList.isEmpty()) {
            LOG.info("Horizontally Flipping {} nodes at world Z coordinate {}", multiSelectList.size(), toNode.z);
            CoordinateChanger coordChanger = new CoordinateChanger();

            // Debug: Output nodes before change
            if (DEBUG)
                for (MapNode node : multiSelectList)
                    LOG.info("pre Flip Horizontal x: {} y: {} z: {}", node.x, node.y, node.z);

            // Sort List by X coordinate
            multiSelectList.sort(Comparator.comparingDouble(value -> value.x));

            // calculate midpoint
            double midpoint = (multiSelectList.getLast().x + multiSelectList.getFirst().x) / 2;

            if (DEBUG) LOG.info("Flip Horizontal midpoint: {} ", midpoint);


            // apply by setting z = z(min)+element_step
            for (MapNode node : multiSelectList) {
                double newX = midpoint + (midpoint - node.x);
                newX = roundUpDoubleToDecimalPlaces(newX, 3);

                coordChanger.addCoordinateChange(node, newX, node.y, node.z);
                node.x = newX;

                // Debug: Output nodes after change
                if (DEBUG) LOG.info("post Flip Horizontal x: {} y: {} z: {}", node.x, node.y, node.z);
            }

            changeManager.addChangeable(coordChanger);
        }
    }
}
