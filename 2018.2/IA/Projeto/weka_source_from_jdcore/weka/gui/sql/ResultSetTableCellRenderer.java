package weka.gui.sql;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;



































public class ResultSetTableCellRenderer
  extends DefaultTableCellRenderer
{
  private static final long serialVersionUID = -8106963669703497351L;
  private Color missingColor;
  private Color missingColorSelected;
  
  public ResultSetTableCellRenderer()
  {
    this(new Color(223, 223, 223), new Color(192, 192, 192));
  }
  







  public ResultSetTableCellRenderer(Color missingColor, Color missingColorSelected)
  {
    this.missingColor = missingColor;
    this.missingColorSelected = missingColorSelected;
  }
  










  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    

    if ((table.getModel() instanceof ResultSetTableModel)) {
      ResultSetTableModel model = (ResultSetTableModel)table.getModel();
      
      if (row >= 0) {
        if (model.isNullAt(row, column)) {
          Messages.getInstance();setToolTipText(Messages.getString("ResultSetTableCellRenderer_GetTableCellRendererComponent_SetToolTipText_Text"));
          if (isSelected) {
            result.setBackground(missingColorSelected);
          } else {
            result.setBackground(missingColor);
          }
        } else {
          setToolTipText(null);
          if (isSelected) {
            result.setBackground(table.getSelectionBackground());
          } else {
            result.setBackground(Color.WHITE);
          }
        }
        
        if (model.isNumericAt(column)) {
          setHorizontalAlignment(4);
        } else {
          setHorizontalAlignment(2);
        }
      }
      else {
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setHorizontalAlignment(0);
        if (table.getColumnModel().getSelectionModel().isSelectedIndex(column)) {
          result.setBackground(UIManager.getColor("TableHeader.background").darker());
        } else {
          result.setBackground(UIManager.getColor("TableHeader.background"));
        }
      }
    }
    return result;
  }
}
