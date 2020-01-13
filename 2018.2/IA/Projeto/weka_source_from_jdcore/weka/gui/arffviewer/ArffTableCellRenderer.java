package weka.gui.arffviewer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;








































public class ArffTableCellRenderer
  extends DefaultTableCellRenderer
{
  static final long serialVersionUID = 9195794493301191171L;
  private Color missingColor;
  private Color missingColorSelected;
  private Color highlightColor;
  private Color highlightColorSelected;
  
  public ArffTableCellRenderer()
  {
    this(new Color(223, 223, 223), new Color(192, 192, 192));
  }
  







  public ArffTableCellRenderer(Color missingColor, Color missingColorSelected)
  {
    this(missingColor, missingColorSelected, Color.RED, Color.RED.darker());
  }
  















  public ArffTableCellRenderer(Color missingColor, Color missingColorSelected, Color highlightColor, Color highlightColorSelected)
  {
    this.missingColor = missingColor;
    this.missingColorSelected = missingColorSelected;
    this.highlightColor = highlightColor;
    this.highlightColorSelected = highlightColorSelected;
  }
  


















  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    String searchString;
    String searchString;
    if ((table instanceof ArffTable)) {
      searchString = ((ArffTable)table).getSearchString();
    } else
      searchString = null;
    boolean found; boolean found; if ((searchString != null) && (!searchString.equals(""))) {
      found = searchString.equals(value.toString());
    } else {
      found = false;
    }
    if ((table.getModel() instanceof ArffSortedTableModel)) {
      ArffSortedTableModel model = (ArffSortedTableModel)table.getModel();
      
      if (row >= 0) {
        if (model.isMissingAt(row, column)) {
          Messages.getInstance();setToolTipText(Messages.getString("ArffTableCellRenderer_GetTableCellRendererComponent_SetToolTipText_Text"));
          if (found) {
            if (isSelected) {
              result.setBackground(highlightColorSelected);
            } else {
              result.setBackground(highlightColor);
            }
          }
          else if (isSelected) {
            result.setBackground(missingColorSelected);
          } else {
            result.setBackground(missingColor);
          }
        }
        else {
          setToolTipText(null);
          if (found) {
            if (isSelected) {
              result.setBackground(highlightColorSelected);
            } else {
              result.setBackground(highlightColor);
            }
          }
          else if (isSelected) {
            result.setBackground(table.getSelectionBackground());
          } else {
            result.setBackground(Color.WHITE);
          }
        }
        

        if (model.getType(row, column) == 0) {
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
        } else
          result.setBackground(UIManager.getColor("TableHeader.background"));
      }
    }
    return result;
  }
}
