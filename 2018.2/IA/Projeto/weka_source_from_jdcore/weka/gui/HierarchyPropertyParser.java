package weka.gui;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;














































public class HierarchyPropertyParser
  implements Serializable
{
  private static final long serialVersionUID = -4151103338506077544L;
  private TreeNode m_Root;
  private TreeNode m_Current;
  private String m_Seperator = ".";
  

  private int m_Depth = 0;
  





  private class TreeNode
  {
    public TreeNode parent = null;
    

    public String value = null;
    

    public Vector children = null;
    

    public int level = 0;
    

    public String context = null;
    
    private TreeNode() {}
  }
  
  public HierarchyPropertyParser() { m_Root = new TreeNode(null);
    m_Root.parent = null;
    m_Root.children = new Vector();
    goToRoot();
  }
  





  public HierarchyPropertyParser(String p, String delim)
    throws Exception
  {
    this();
    build(p, delim);
  }
  




  public void setSeperator(String s)
  {
    m_Seperator = s;
  }
  



  public String getSeperator()
  {
    return m_Seperator;
  }
  



  public void build(String p, String delim)
    throws Exception
  {
    StringTokenizer st = new StringTokenizer(p, delim);
    
    while (st.hasMoreTokens()) {
      String property = st.nextToken().trim();
      if (!isHierachic(property)) {
        Messages.getInstance();throw new Exception(Messages.getString("HierarchyPropertyParser_Build_Exception_Text")); }
      add(property);
    }
    goToRoot();
  }
  




  public synchronized void add(String property)
  {
    String[] values = tokenize(property);
    if (m_Root.value == null) {
      m_Root.value = values[0];
    }
    buildBranch(m_Root, values, 1);
  }
  









  private void buildBranch(TreeNode parent, String[] values, int lvl)
  {
    if (lvl == values.length) {
      children = null;
      return;
    }
    
    if (lvl > m_Depth - 1) {
      m_Depth = (lvl + 1);
    }
    Vector kids = children;
    int index = search(kids, values[lvl]);
    if (index != -1) {
      TreeNode newParent = (TreeNode)kids.elementAt(index);
      if (children == null)
        children = new Vector();
      buildBranch(newParent, values, lvl + 1);
    }
    else {
      TreeNode added = new TreeNode(null);
      parent = parent;
      value = values[lvl];
      children = new Vector();
      level = lvl;
      if (parent != m_Root) {
        context = (context + m_Seperator + value);
      } else {
        context = value;
      }
      kids.addElement(added);
      buildBranch(added, values, lvl + 1);
    }
  }
  






  public String[] tokenize(String rawString)
  {
    Vector result = new Vector();
    StringTokenizer tk = new StringTokenizer(rawString, m_Seperator);
    while (tk.hasMoreTokens()) {
      result.addElement(tk.nextToken());
    }
    String[] newStrings = new String[result.size()];
    for (int i = 0; i < result.size(); i++) {
      newStrings[i] = ((String)result.elementAt(i));
    }
    return newStrings;
  }
  






  public boolean contains(String string)
  {
    String[] item = tokenize(string);
    if (!item[0].equals(m_Root.value)) {
      return false;
    }
    return isContained(m_Root, item, 1);
  }
  








  private boolean isContained(TreeNode parent, String[] values, int lvl)
  {
    if (lvl == values.length)
      return true;
    if (lvl > values.length) {
      return false;
    }
    Vector kids = children;
    int index = search(kids, values[lvl]);
    if (index != -1) {
      TreeNode newParent = (TreeNode)kids.elementAt(index);
      return isContained(newParent, values, lvl + 1);
    }
    
    return false;
  }
  






  public boolean isHierachic(String string)
  {
    int index = string.indexOf(m_Seperator);
    
    if ((index == string.length() - 1) || (index == -1)) {
      return false;
    }
    return true;
  }
  









  public int search(Vector vct, String target)
  {
    if (vct == null) {
      return -1;
    }
    for (int i = 0; i < vct.size(); i++) {
      if (target.equals(elementAtvalue))
        return i;
    }
    return -1;
  }
  








  public synchronized boolean goTo(String path)
  {
    if (!isHierachic(path)) {
      if (m_Root.value.equals(path)) {
        goToRoot();
        return true;
      }
      
      return false;
    }
    
    TreeNode old = m_Current;
    m_Current = new TreeNode(null);
    goToRoot();
    String[] nodes = tokenize(path);
    if (!m_Current.value.equals(nodes[0])) {
      return false;
    }
    for (int i = 1; i < nodes.length; i++) {
      int pos = search(m_Current.children, nodes[i]);
      if (pos == -1) {
        m_Current = old;
        return false;
      }
      m_Current = ((TreeNode)m_Current.children.elementAt(pos));
    }
    
    return true;
  }
  








  public synchronized boolean goDown(String path)
  {
    if (!isHierachic(path)) {
      return goToChild(path);
    }
    TreeNode old = m_Current;
    m_Current = new TreeNode(null);
    String[] nodes = tokenize(path);
    int pos = search(children, nodes[0]);
    if (pos == -1) {
      m_Current = old;
      return false;
    }
    
    m_Current = ((TreeNode)children.elementAt(pos));
    for (int i = 1; i < nodes.length; i++) {
      pos = search(m_Current.children, nodes[i]);
      if (pos == -1) {
        m_Current = old;
        return false;
      }
      
      m_Current = ((TreeNode)m_Current.children.elementAt(pos));
    }
    
    return true;
  }
  


  public synchronized void goToRoot()
  {
    m_Current = m_Root;
  }
  




  public synchronized void goToParent()
  {
    if (m_Current.parent != null) {
      m_Current = m_Current.parent;
    }
  }
  








  public synchronized boolean goToChild(String value)
  {
    if (m_Current.children == null) {
      return false;
    }
    int pos = search(m_Current.children, value);
    if (pos == -1) {
      return false;
    }
    m_Current = ((TreeNode)m_Current.children.elementAt(pos));
    return true;
  }
  





  public synchronized void goToChild(int pos)
    throws Exception
  {
    if ((m_Current.children == null) || (pos < 0) || (pos >= m_Current.children.size()))
    {
      Messages.getInstance();throw new Exception(Messages.getString("HierarchyPropertyParser_GoToChild_Exception_Text"));
    }
    m_Current = ((TreeNode)m_Current.children.elementAt(pos));
  }
  





  public synchronized int numChildren()
  {
    if (m_Current.children == null) {
      return 0;
    }
    return m_Current.children.size();
  }
  





  public synchronized String[] childrenValues()
  {
    if (m_Current.children == null) {
      return null;
    }
    Vector kids = m_Current.children;
    String[] values = new String[kids.size()];
    for (int i = 0; i < kids.size(); i++)
      values[i] = elementAtvalue;
    return values;
  }
  






  public synchronized String parentValue()
  {
    if (m_Current.parent != null)
      return m_Current.parent.value;
    return null;
  }
  




  public synchronized boolean isLeafReached()
  {
    return m_Current.children == null;
  }
  




  public synchronized boolean isRootReached()
  {
    return m_Current.parent == null;
  }
  



  public synchronized String getValue()
  {
    return m_Current.value;
  }
  


  public synchronized int getLevel()
  {
    return m_Current.level;
  }
  


  public int depth()
  {
    return m_Depth;
  }
  





  public synchronized String context()
  {
    return m_Current.context;
  }
  





  public synchronized String fullValue()
  {
    if (m_Current == m_Root) {
      return m_Root.value;
    }
    return m_Current.context + m_Seperator + m_Current.value;
  }
  





  public String showTree()
  {
    return showNode(m_Root, null);
  }
  





  private String showNode(TreeNode node, boolean[] hasBar)
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < level - 1; i++) {
      if (hasBar[i] != 0) {
        text.append("  |       ");
      } else
        text.append("          ");
    }
    if (level != 0)
      text.append("  |------ ");
    text.append(value + "(" + level + ")" + "[" + context + "]\n");
    
    if (children != null) {
      for (int i = 0; i < children.size(); i++) {
        boolean[] newBar = new boolean[level + 1];
        int lvl = level;
        
        if (hasBar != null) {
          for (int j = 0; j < lvl; j++)
            newBar[j] = hasBar[j];
        }
        if (i == children.size() - 1) {
          newBar[lvl] = false;
        } else {
          newBar[lvl] = true;
        }
        text.append(showNode((TreeNode)children.elementAt(i), newBar));
      }
    }
    return text.toString();
  }
  




  public static void main(String[] args)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("node1.node1_1.node1_1_1.node1_1_1_1, ");
    sb.append("node1.node1_1.node1_1_1.node1_1_1_2, ");
    sb.append("node1.node1_1.node1_1_1.node1_1_1_3, ");
    sb.append("node1.node1_1.node1_1_2.node1_1_2_1, ");
    sb.append("node1.node1_1.node1_1_3.node1_1_3_1, ");
    sb.append("node1.node1_2.node1_2_1.node1_2_1_1, ");
    sb.append("node1.node1_2.node1_2_3.node1_2_3_1, ");
    sb.append("node1.node1_3.node1_3_3.node1_3_3_1, ");
    sb.append("node1.node1_3.node1_3_3.node1_3_3_2, ");
    
    String p = sb.toString();
    try {
      HierarchyPropertyParser hpp = new HierarchyPropertyParser(p, ", ");
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_First") + hpp.getSeperator());
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Second") + hpp.depth());
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Third") + hpp.showTree());
      hpp.goToRoot();
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Fourth") + hpp.goTo("node1.node1_2.node1_2_1") + ": " + hpp.getValue() + " | " + hpp.fullValue() + Messages.getString("HierarchyPropertyParser_Main_Text_Fifth") + hpp.isLeafReached());
      

      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Sixth") + hpp.goDown("node1"));
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Seventh") + hpp.getValue());
      Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Eighth") + hpp.goToChild("node1_2_1_1") + ": " + hpp.getValue() + " | " + hpp.fullValue() + Messages.getString("HierarchyPropertyParser_Main_Text_Nineth") + hpp.isLeafReached() + Messages.getString("HierarchyPropertyParser_Main_Text_Tenth") + hpp.isRootReached());
      


      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Eleventh") + hpp.parentValue());
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Twelveth") + hpp.getLevel());
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Thirteenth") + hpp.context());
      hpp.goToRoot();
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Fourteenth") + hpp.isLeafReached() + Messages.getString("HierarchyPropertyParser_Main_Text_Fifteenth") + hpp.isRootReached());
      
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_Sixteenth") + hpp.goDown("node1_1.node1_1_1") + Messages.getString("HierarchyPropertyParser_Main_Text_Seventeenth") + hpp.getValue() + " | " + hpp.fullValue() + Messages.getString("HierarchyPropertyParser_Main_Text_Eighteenth") + hpp.getLevel() + Messages.getString("HierarchyPropertyParser_Main_Text_Nineteenth") + hpp.isLeafReached() + Messages.getString("HierarchyPropertyParser_Main_Text_Twenty") + hpp.isRootReached());
      




      hpp.goToParent();
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_TwentyFirst") + hpp.getValue() + " | " + hpp.fullValue());
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_TwentySecond") + hpp.getLevel());
      
      String[] chd = hpp.childrenValues();
      for (int i = 0; i < chd.length; i++) {
        Messages.getInstance();System.out.print(Messages.getString("HierarchyPropertyParser_Main_Text_TwentyThird") + i + ": " + chd[i]);
        hpp.goDown(chd[i]);
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_TwentyFourth") + hpp.getValue() + " | " + hpp.fullValue() + Messages.getString("HierarchyPropertyParser_Main_Text_TwentyFifth") + hpp.getLevel() + Messages.getString("HierarchyPropertyParser_Main_Text_TwentySixth"));
        

        hpp.goToParent();
      }
      
      Messages.getInstance();System.out.println(Messages.getString("HierarchyPropertyParser_Main_Text_TwentySeventh") + hpp.goTo("node1") + ": " + hpp.getValue() + " | " + hpp.fullValue());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
