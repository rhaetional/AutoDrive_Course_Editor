package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.MapPanel.Rotation;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.backBufferGraphics;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class FlipHorizontalButton extends AlignBaseButton {

    public FlipHorizontalButton(JPanel panel) {
        button = makeImageToggleButton("buttons/flip_horizontal","buttons/flip_horizontal_selected", null,"flip_horizontal_tooltip","flip_horizontal_alt", panel, false, false,  null, false, this);
    }

    @Override
    public String getButtonID() { return "FlipHorizontalButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("flip_horizontal_tooltip"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Horizontally Flipping {} nodes at world Z coordinate {}",multiSelectList.size(), toNode.z);
        changeManager.addChangeable( new AlignBaseButton.AlignmentChanger(multiSelectList, 0, 0, toNode.z));
        for (MapNode node : multiSelectList) {
            LOG.info("pre Flip Horizontal x: {} y: {} z: {}",node.x, node.y,node.z);
        }

        // Sort List by X coordinate
        multiSelectList.sort(Comparator.comparingDouble(value -> value.x));

        // calculate midpoint
        double midpoint = (multiSelectList.getLast().x + multiSelectList.getFirst().x)/2;

        LOG.info("Flip Horizontal midpoint: {} ", midpoint);

        // apply by setting z = z(min)+element_step
        for (MapNode node : multiSelectList) {
            double newX = midpoint + (midpoint - node.x);
            node.x = roundUpDoubleToDecimalPlaces(newX, 3);

            LOG.info("post Flip Horizontal x: {} y: {} z: {}",node.x, node.y,node.z);
        }

    }
}
