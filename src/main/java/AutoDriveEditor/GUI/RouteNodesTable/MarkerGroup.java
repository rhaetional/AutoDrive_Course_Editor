package AutoDriveEditor.GUI.RouteNodesTable;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
    Class to hold the marker group name
 */
public class MarkerGroup implements Comparable<MarkerGroup> {
    private String name;

    public MarkerGroup(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(MarkerGroup otherGroup) {
        return this.name.compareTo(otherGroup.name);
    }
    public String toString() {
        return this.name;
    }

}

/*
    custom renderer for cells in the MarkerGroup column.
 */
class MarkerGroupCellRenderer extends DefaultTableCellRenderer {
    private List<MarkerGroup> listMarkerGroup;
    public MarkerGroupCellRenderer(List<MarkerGroup> listMarkerGroup) {
        this.listMarkerGroup = listMarkerGroup;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof MarkerGroup) {
            MarkerGroup markerGroup = (MarkerGroup) value;
            updateMarkerGroupList(markerGroup);
            setText(markerGroup.getName());
        }

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getSelectionForeground());
        }

        return this;
    }

    public Dimension getPreferredSize() {
        Dimension parent = super.getPreferredSize();
        // System.out.println("Parent Dimension: w=" + parent.width + ", h=" + parent.height);
        // add some more space
        parent.height += 2;
        parent.height += 10;

        return parent;
    }
    private void updateMarkerGroupList (MarkerGroup markerGroup){

        Optional<MarkerGroup> result = listMarkerGroup.stream().parallel().filter(group -> group.getName().equals(markerGroup.getName())).findFirst();

        if (result.isEmpty()) {
            listMarkerGroup.add(markerGroup);
            Collections.sort(listMarkerGroup);
        }
    }
}


/**
 * A custom editor for cells in the MarkerGroup column.
 */
class MarkerGroupCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private MarkerGroup markerGroup;
    private List<MarkerGroup> listMarkerGroup;

    public MarkerGroupCellEditor(List<MarkerGroup> listMarkerGroup) {
        this.listMarkerGroup = listMarkerGroup;
    }

    @Override
    public Object getCellEditorValue() {
        return this.markerGroup;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (value instanceof MarkerGroup) {
            this.markerGroup = (MarkerGroup) value;
        }

        JComboBox<MarkerGroup> comboMarkerGroup = new JComboBox<MarkerGroup>();

        for (MarkerGroup aMarkerGroup : listMarkerGroup) {
            comboMarkerGroup.addItem(aMarkerGroup);
        }

        comboMarkerGroup.setSelectedItem(markerGroup);
        comboMarkerGroup.addActionListener(this);

        if (isSelected) {
            comboMarkerGroup.setBackground(table.getSelectionBackground());
        } else {
            comboMarkerGroup.setBackground(table.getSelectionForeground());
        }

        return comboMarkerGroup;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JComboBox<MarkerGroup> comboMarkerGroup = (JComboBox<MarkerGroup>) event.getSource();
        this.markerGroup = (MarkerGroup) comboMarkerGroup.getSelectedItem();
    }

}