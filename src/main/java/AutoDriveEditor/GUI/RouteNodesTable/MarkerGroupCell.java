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
public class MarkerGroupCell implements Comparable<MarkerGroupCell> {
    private String name;

    public MarkerGroupCell(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(MarkerGroupCell otherGroup) {
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
    private List<MarkerGroupCell> listMarkerGroupCell;
    public MarkerGroupCellRenderer(List<MarkerGroupCell> listMarkerGroupCell) {
        this.listMarkerGroupCell = listMarkerGroupCell;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof MarkerGroupCell) {
            MarkerGroupCell markerGroupCell = (MarkerGroupCell) value;
            updateMarkerGroupList(markerGroupCell);
            setText(markerGroupCell.getName());
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
    private void updateMarkerGroupList (MarkerGroupCell markerGroupCell){

        Optional<MarkerGroupCell> result = listMarkerGroupCell.stream().parallel().filter(group -> group.getName().equals(markerGroupCell.getName())).findFirst();

        if (result.isEmpty()) {
            listMarkerGroupCell.add(markerGroupCell);
            Collections.sort(listMarkerGroupCell);
        }
    }
}


/**
 * A custom editor for cells in the MarkerGroup column.
 */
class MarkerGroupCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private MarkerGroupCell markerGroupCell;
    private List<MarkerGroupCell> listMarkerGroupCell;

    public MarkerGroupCellEditor(List<MarkerGroupCell> listMarkerGroupCell) {
        this.listMarkerGroupCell = listMarkerGroupCell;
    }

    @Override
    public Object getCellEditorValue() {
        return this.markerGroupCell;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (value instanceof MarkerGroupCell) {
            this.markerGroupCell = (MarkerGroupCell) value;
        }

        JComboBox<MarkerGroupCell> comboMarkerGroup = new JComboBox<MarkerGroupCell>();

        for (MarkerGroupCell aMarkerGroupCell : listMarkerGroupCell) {
            comboMarkerGroup.addItem(aMarkerGroupCell);
        }

        comboMarkerGroup.setSelectedItem(markerGroupCell);
        comboMarkerGroup.addActionListener(this);

        if (isSelected) {
            comboMarkerGroup.setBackground(table.getSelectionBackground());
        } else {
            comboMarkerGroup.setBackground(table.getSelectionForeground());
        }

        return comboMarkerGroup;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JComboBox) {
            JComboBox<MarkerGroupCell> comboMarkerGroup = (JComboBox<MarkerGroupCell>) event.getSource();
            this.markerGroupCell = (MarkerGroupCell) comboMarkerGroup.getSelectedItem();
        }
    }

}