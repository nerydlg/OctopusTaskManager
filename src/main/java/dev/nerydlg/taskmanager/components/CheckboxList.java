package dev.nerydlg.taskmanager.components;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.event.MouseAdapter;

public class CheckboxList extends JList {
  protected static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

  public CheckboxList() {
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        int index = locationToIndex(evt.getPoint());
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          checkbox.setSelected(!checkbox.isSelected());
        }
      }
    });
  }

  public void addCheckbox(JCheckBox checkBox) {
    ListModel currentList = this.getModel();
    JCheckBox[] newList = new JCheckBox[currentList.getSize() + 1];
    for (int i = 0; i < currentList.getSize(); i++) {
      newList[i] = (JCheckBox) currentList.getElementAt(i);
    }
    newList[newList.length - 1] = checkBox;
    setListData(newList);
  }

  protected class CellRenderer implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JCheckBox checkbox = (JCheckBox) value;
      checkbox.setBackground(isSelected ?
          getSelectionBackground() : getBackground());
      checkbox.setForeground(isSelected ?
          getSelectionForeground() : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ?
          UIManager.getBorder(
              "List.focusCellHighlightBorder") : NO_FOCUS_BORDER);
      return checkbox;
    }


  }
}
