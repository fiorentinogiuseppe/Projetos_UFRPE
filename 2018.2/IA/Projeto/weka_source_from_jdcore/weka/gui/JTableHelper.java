package weka.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintStream;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;































public class JTableHelper
{
  private JTable jtable;
  
  public JTableHelper(JTable jtable)
  {
    this.jtable = jtable;
  }
  


  public JTable getJTable()
  {
    return jtable;
  }
  


  public int calcColumnWidth(int col)
  {
    return calcColumnWidth(getJTable(), col);
  }
  











  public static int calcColumnWidth(JTable table, int col)
  {
    int width = calcHeaderWidth(table, col);
    if (width == -1) {
      return width;
    }
    TableColumnModel columns = table.getColumnModel();
    TableModel data = table.getModel();
    int rowCount = data.getRowCount();
    TableColumn column = columns.getColumn(col);
    try {
      for (int row = rowCount - 1; row >= 0; row--) {
        Component c = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
        

        width = Math.max(width, getPreferredSizewidth + 10);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return width;
  }
  


  public int calcHeaderWidth(int col)
  {
    return calcHeaderWidth(getJTable(), col);
  }
  







  public static int calcHeaderWidth(JTable table, int col)
  {
    if (table == null) {
      return -1;
    }
    if ((col < 0) || (col > table.getColumnCount())) {
      Messages.getInstance();System.out.println(Messages.getString("JTableHelper_CalcHeaderWidth_Text") + col);
      return -1;
    }
    
    JTableHeader header = table.getTableHeader();
    TableCellRenderer defaultHeaderRenderer = null;
    if (header != null) defaultHeaderRenderer = header.getDefaultRenderer();
    TableColumnModel columns = table.getColumnModel();
    TableModel data = table.getModel();
    TableColumn column = columns.getColumn(col);
    int width = -1;
    TableCellRenderer h = column.getHeaderRenderer();
    if (h == null) h = defaultHeaderRenderer;
    if (h != null)
    {
      Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, col);
      


      width = getPreferredSizewidth + 5;
    }
    
    return width;
  }
  


  public void setOptimalColumnWidth(int col)
  {
    setOptimalColumnWidth(getJTable(), col);
  }
  







  public static void setOptimalColumnWidth(JTable jtable, int col)
  {
    if ((col >= 0) && (col < jtable.getColumnModel().getColumnCount())) {
      int width = calcColumnWidth(jtable, col);
      
      if (width >= 0) {
        JTableHeader header = jtable.getTableHeader();
        TableColumn column = jtable.getColumnModel().getColumn(col);
        column.setPreferredWidth(width);
        jtable.sizeColumnsToFit(-1);
        header.repaint();
      }
    }
  }
  


  public void setOptimalColumnWidth()
  {
    setOptimalColumnWidth(getJTable());
  }
  




  public static void setOptimalColumnWidth(JTable jtable)
  {
    for (int i = 0; i < jtable.getColumnModel().getColumnCount(); i++) {
      setOptimalColumnWidth(jtable, i);
    }
  }
  

  public void setOptimalHeaderWidth(int col)
  {
    setOptimalHeaderWidth(getJTable(), col);
  }
  







  public static void setOptimalHeaderWidth(JTable jtable, int col)
  {
    if ((col >= 0) && (col < jtable.getColumnModel().getColumnCount())) {
      int width = calcHeaderWidth(jtable, col);
      
      if (width >= 0) {
        JTableHeader header = jtable.getTableHeader();
        TableColumn column = jtable.getColumnModel().getColumn(col);
        column.setPreferredWidth(width);
        jtable.sizeColumnsToFit(-1);
        header.repaint();
      }
    }
  }
  


  public void setOptimalHeaderWidth()
  {
    setOptimalHeaderWidth(getJTable());
  }
  




  public static void setOptimalHeaderWidth(JTable jtable)
  {
    for (int i = 0; i < jtable.getColumnModel().getColumnCount(); i++) {
      setOptimalHeaderWidth(jtable, i);
    }
  }
  



  public void scrollToVisible(int row, int col)
  {
    scrollToVisible(getJTable(), row, col);
  }
  




  public static void scrollToVisible(JTable table, int row, int col)
  {
    if (!(table.getParent() instanceof JViewport)) {
      return;
    }
    JViewport viewport = (JViewport)table.getParent();
    


    Rectangle rect = table.getCellRect(row, col, true);
    

    Point pt = viewport.getViewPosition();
    



    rect.setLocation(x - x, y - y);
    

    viewport.scrollRectToVisible(rect);
  }
}
