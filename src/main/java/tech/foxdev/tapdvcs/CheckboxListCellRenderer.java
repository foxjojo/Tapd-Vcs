package tech.foxdev.tapdvcs;

import javax.swing.*;
import java.awt.*;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setSelected(isSelected);
        setEnabled(list.isEnabled());

        setText(value == null ? "" : ((TapdBugData)value).DisplayName );

        return this;
    }
}
