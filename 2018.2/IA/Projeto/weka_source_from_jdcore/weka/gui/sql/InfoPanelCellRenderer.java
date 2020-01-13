package weka.gui.sql;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;



































public class InfoPanelCellRenderer
  extends JLabel
  implements ListCellRenderer
{
  private static final long serialVersionUID = -533380118807178531L;
  
  public InfoPanelCellRenderer()
  {
    setOpaque(true);
  }
  










  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
  {
    if ((value instanceof JLabel)) {
      setIcon(((JLabel)value).getIcon());
      setText(((JLabel)value).getText());
    }
    else {
      setIcon(null);
      setText(value.toString());
    }
    
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    }
    else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    setEnabled(list.isEnabled());
    setFont(list.getFont());
    
    return this;
  }
}
