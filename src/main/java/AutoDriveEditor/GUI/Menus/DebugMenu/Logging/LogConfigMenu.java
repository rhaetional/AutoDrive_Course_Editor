package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogConfigMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogXMLInfo;

    public LogConfigMenu() {
        makeCheckBoxMenuItem("menu_debug_log_config", "menu_debug_log_config_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogXMLInfo = menuItem.isSelected();
    }
}
