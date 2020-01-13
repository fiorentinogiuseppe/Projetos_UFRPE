package weka.gui.treevisualizer;






public class TreeDisplayEvent
{
  public static final int NO_COMMAND = 0;
  




  public static final int ADD_CHILDREN = 1;
  




  public static final int REMOVE_CHILDREN = 2;
  




  public static final int ACCEPT = 3;
  




  public static final int CLASSIFY_CHILD = 4;
  




  public static final int SEND_INSTANCES = 5;
  




  private int m_command;
  




  private String m_nodeId;
  




  public TreeDisplayEvent(int ar, String id)
  {
    m_command = 0;
    if ((ar == 1) || (ar == 2) || (ar == 3) || (ar == 4) || (ar == 5))
    {
      m_command = ar;
    }
    m_nodeId = id;
  }
  


  public int getCommand()
  {
    return m_command;
  }
  


  public String getID()
  {
    return m_nodeId;
  }
}
