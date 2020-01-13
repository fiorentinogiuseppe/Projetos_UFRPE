package weka.gui.arffviewer;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Undoable;
import weka.gui.SortedTableModel;




































public class ArffSortedTableModel
  extends SortedTableModel
  implements Undoable
{
  static final long serialVersionUID = -5733148376354254030L;
  
  public ArffSortedTableModel(String filename)
  {
    this(new ArffTableModel(filename));
  }
  





  public ArffSortedTableModel(Instances data)
  {
    this(new ArffTableModel(data));
  }
  




  public ArffSortedTableModel(TableModel model)
  {
    super(model);
  }
  




  public boolean isNotificationEnabled()
  {
    return ((ArffTableModel)getModel()).isNotificationEnabled();
  }
  




  public void setNotificationEnabled(boolean enabled)
  {
    ((ArffTableModel)getModel()).setNotificationEnabled(enabled);
  }
  




  public boolean isUndoEnabled()
  {
    return ((ArffTableModel)getModel()).isUndoEnabled();
  }
  




  public void setUndoEnabled(boolean enabled)
  {
    ((ArffTableModel)getModel()).setUndoEnabled(enabled);
  }
  




  public boolean isReadOnly()
  {
    return ((ArffTableModel)getModel()).isReadOnly();
  }
  




  public void setReadOnly(boolean value)
  {
    ((ArffTableModel)getModel()).setReadOnly(value);
  }
  







  public double getInstancesValueAt(int rowIndex, int columnIndex)
  {
    return ((ArffTableModel)getModel()).getInstancesValueAt(mIndices[rowIndex], columnIndex);
  }
  








  public Object getModelValueAt(int rowIndex, int columnIndex)
  {
    Object result = super.getModel().getValueAt(rowIndex, columnIndex);
    

    if (((ArffTableModel)getModel()).isMissingAt(rowIndex, columnIndex)) {
      result = null;
    }
    return result;
  }
  





  public int getType(int columnIndex)
  {
    return ((ArffTableModel)getModel()).getType(mIndices[0], columnIndex);
  }
  






  public int getType(int rowIndex, int columnIndex)
  {
    return ((ArffTableModel)getModel()).getType(mIndices[rowIndex], columnIndex);
  }
  




  public void deleteAttributeAt(int columnIndex)
  {
    ((ArffTableModel)getModel()).deleteAttributeAt(columnIndex);
  }
  




  public void deleteAttributes(int[] columnIndices)
  {
    ((ArffTableModel)getModel()).deleteAttributes(columnIndices);
  }
  





  public void renameAttributeAt(int columnIndex, String newName)
  {
    ((ArffTableModel)getModel()).renameAttributeAt(columnIndex, newName);
  }
  




  public void attributeAsClassAt(int columnIndex)
  {
    ((ArffTableModel)getModel()).attributeAsClassAt(columnIndex);
  }
  




  public void deleteInstanceAt(int rowIndex)
  {
    ((ArffTableModel)getModel()).deleteInstanceAt(mIndices[rowIndex]);
  }
  







  public void deleteInstances(int[] rowIndices)
  {
    int[] realIndices = new int[rowIndices.length];
    for (int i = 0; i < rowIndices.length; i++) {
      realIndices[i] = mIndices[rowIndices[i]];
    }
    ((ArffTableModel)getModel()).deleteInstances(realIndices);
  }
  




  public void sortInstances(int columnIndex)
  {
    ((ArffTableModel)getModel()).sortInstances(columnIndex);
  }
  





  public int getAttributeColumn(String name)
  {
    return ((ArffTableModel)getModel()).getAttributeColumn(name);
  }
  






  public boolean isMissingAt(int rowIndex, int columnIndex)
  {
    return ((ArffTableModel)getModel()).isMissingAt(mIndices[rowIndex], columnIndex);
  }
  




  public void setInstances(Instances data)
  {
    ((ArffTableModel)getModel()).setInstances(data);
  }
  




  public Instances getInstances()
  {
    return ((ArffTableModel)getModel()).getInstances();
  }
  






  public Attribute getAttributeAt(int columnIndex)
  {
    return ((ArffTableModel)getModel()).getAttributeAt(columnIndex);
  }
  





  public void addTableModelListener(TableModelListener l)
  {
    if (getModel() != null) {
      ((ArffTableModel)getModel()).addTableModelListener(l);
    }
  }
  




  public void removeTableModelListener(TableModelListener l)
  {
    if (getModel() != null) {
      ((ArffTableModel)getModel()).removeTableModelListener(l);
    }
  }
  



  public void notifyListener(TableModelEvent e)
  {
    ((ArffTableModel)getModel()).notifyListener(e);
  }
  


  public void clearUndo()
  {
    ((ArffTableModel)getModel()).clearUndo();
  }
  





  public boolean canUndo()
  {
    return ((ArffTableModel)getModel()).canUndo();
  }
  


  public void undo()
  {
    ((ArffTableModel)getModel()).undo();
  }
  


  public void addUndoPoint()
  {
    ((ArffTableModel)getModel()).addUndoPoint();
  }
}
