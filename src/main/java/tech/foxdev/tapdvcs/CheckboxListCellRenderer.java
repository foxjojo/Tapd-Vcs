package tech.foxdev.tapdvcs;

import javax.swing.*;
import java.awt.*;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

    private TapdBugData data;

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        data = (TapdBugData) value;
        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setSelected(isSelected);
        setEnabled(list.isEnabled());

        setText(value == null ? "" : data.DisplayName);

        return this;
    }

}
