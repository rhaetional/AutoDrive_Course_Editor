package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.util.Comparator;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public final class FlipVerticalButton extends AlignBaseButton {

    public FlipVerticalButton(JPanel panel) {
        button = makeImageToggleButton("buttons/flip_vertical","buttons/flip_vertical_selected", null,"flip_vertical_tooltip","flip_vertical_alt", panel, false, false,  null, false, this);
    }

    @Override
    public String getButtonID() { return "FlipVerticalButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("flip_vertical_tooltip"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Vertically Flipping {} nodes at world Z coordinate {}",multiSelectList.size(), toNode.z);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, 0, toNode.z));
        for (MapNode node : multiSelectList) {
            LOG.info("pre Flip Vertical x: {} y: {} z: {}",node.x, node.y,node.z);
        }

        // Sort List by Z coordinate
        multiSelectList.sort(Comparator.comparingDouble(value -> value.z));

        // calculate midpoint
        double midpoint = (multiSelectList.getLast().z + multiSelectList.getFirst().z)/2;

        LOG.info("Flip Vertical midpoint: {} ", midpoint);

        // apply by setting z = z(min)+element_step
        for (MapNode node : multiSelectList) {
            double newZ = midpoint + (midpoint - node.z);
            node.z = roundUpDoubleToDecimalPlaces(newZ, 3);

            LOG.info("post Flip Vertical x: {} y: {} z: {}",node.x, node.y,node.z);
        }

    }
}
