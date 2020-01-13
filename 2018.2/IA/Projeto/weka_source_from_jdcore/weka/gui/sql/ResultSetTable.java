package weka.gui.sql;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import weka.gui.JTableHelper;














































public class ResultSetTable
  extends JTable
{
  private static final long serialVersionUID = -3391076671854464137L;
  protected String m_Query;
  protected String m_URL;
  protected String m_User;
  protected String m_Password;
  
  public ResultSetTable(String url, String user, String pw, String query, ResultSetTableModel model)
  {
    super(model);
    
    m_URL = url;
    m_User = user;
    m_Password = pw;
    m_Query = query;
    
    setAutoResizeMode(0);
    

    for (int i = 0; i < getColumnCount(); i++) {
      JTableHelper.setOptimalHeaderWidth(this, i);
      getColumnModel().getColumn(i).setCellRenderer(new ResultSetTableCellRenderer());
    }
    


    final JTable table = this;
    getTableHeader().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = convertColumnIndexToModel(viewColumn);
        if ((e.getButton() == 1) && (e.getClickCount() == 2) && (column != -1))
        {

          JTableHelper.setOptimalColumnWidth(table, column); }
      }
    });
    Messages.getInstance();getTableHeader().setToolTipText(Messages.getString("ResultSetTable_GetTableHeader_SetToolTipText_Text"));
  }
  


  public String getURL()
  {
    return m_URL;
  }
  


  public String getUser()
  {
    return m_User;
  }
  


  public String getPassword()
  {
    return m_Password;
  }
  


  public String getQuery()
  {
    return m_Query;
  }
  

  public void finalize()
    throws Throwable
  {
    if (getModel() != null) {
      ((ResultSetTableModel)getModel()).finalize();
    }
    super.finalize();
    
    System.gc();
  }
}
