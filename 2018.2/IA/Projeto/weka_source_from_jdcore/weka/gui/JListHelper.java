package weka.gui;

import java.io.PrintStream;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;













































public class JListHelper
{
  public static final int MOVE_UP = 0;
  public static final int MOVE_DOWN = 1;
  
  public JListHelper() {}
  
  protected static void moveItems(JList list, int moveby, int direction)
  {
    DefaultListModel model = (DefaultListModel)list.getModel();
    int[] indices;
    int i; switch (direction) {
    case 0: 
      indices = list.getSelectedIndices();
      for (i = 0; i < indices.length; i++)
        if (indices[i] != 0)
        {
          Object o = model.remove(indices[i]);
          indices[i] -= moveby;
          model.insertElementAt(o, indices[i]);
        }
      list.setSelectedIndices(indices);
      break;
    
    case 1: 
      indices = list.getSelectedIndices();
      for (i = indices.length - 1; i >= 0; i--)
        if (indices[i] != model.getSize() - 1)
        {
          Object o = model.remove(indices[i]);
          indices[i] += moveby;
          model.insertElementAt(o, indices[i]);
        }
      list.setSelectedIndices(indices);
      break;
    
    default: 
      Messages.getInstance();Messages.getInstance();System.err.println(JListHelper.class.getName() + Messages.getString("JListHelper_MoveItems_Error_Text_First") + direction + Messages.getString("JListHelper_MoveItems_Error_Text_Second"));
    }
    
  }
  





  public static void moveUp(JList list)
  {
    if (canMoveUp(list)) {
      moveItems(list, 1, 0);
    }
  }
  



  public static void moveDown(JList list)
  {
    if (canMoveDown(list)) {
      moveItems(list, 1, 1);
    }
  }
  






  public static void moveTop(JList list)
  {
    if (canMoveUp(list)) {
      int[] indices = list.getSelectedIndices();
      int diff = indices[0];
      moveItems(list, diff, 0);
    }
  }
  







  public static void moveBottom(JList list)
  {
    if (canMoveDown(list)) {
      int[] indices = list.getSelectedIndices();
      int diff = list.getModel().getSize() - 1 - indices[(indices.length - 1)];
      moveItems(list, diff, 1);
    }
  }
  







  public static boolean canMoveUp(JList list)
  {
    boolean result = false;
    
    int[] indices = list.getSelectedIndices();
    if ((indices.length > 0) && 
      (indices[0] > 0)) {
      result = true;
    }
    
    return result;
  }
  







  public static boolean canMoveDown(JList list)
  {
    boolean result = false;
    
    int[] indices = list.getSelectedIndices();
    if ((indices.length > 0) && 
      (indices[(indices.length - 1)] < list.getModel().getSize() - 1)) {
      result = true;
    }
    
    return result;
  }
}
