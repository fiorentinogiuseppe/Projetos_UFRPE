package weka.gui.treevisualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Vector;
import weka.core.Instances;















































































public class Node
{
  private int m_backstyle;
  private int m_shape;
  private Color m_color;
  private String m_label;
  private Vector m_lines;
  private double m_center;
  private double m_top;
  private boolean m_cVisible;
  private boolean m_visible;
  private boolean m_root;
  private Vector m_parent;
  private Vector m_children;
  private String m_refer;
  private String m_data;
  private Instances m_theData;
  
  public Node(String label, String refer, int backstyle, int shape, Color color, String d)
  {
    m_label = label;
    m_backstyle = backstyle;
    m_shape = shape;
    m_color = color;
    m_refer = refer;
    
    m_center = 0.0D;
    m_top = 0.0D;
    
    m_cVisible = true;
    m_visible = true;
    m_root = false;
    m_parent = new Vector(1, 1);
    m_children = new Vector(20, 10);
    m_lines = new Vector(4, 2);
    breakupLabel();
    m_data = d;
    m_theData = null;
  }
  





  public Instances getInstances()
  {
    if ((m_theData == null) && (m_data != null)) {
      try {
        m_theData = new Instances(new StringReader(m_data));
      } catch (Exception e) {
        Messages.getInstance();System.out.println(Messages.getString("Node_GetInstances_Exception_Text") + e);
      }
      m_data = null;
    }
    return m_theData;
  }
  




  public boolean getCVisible()
  {
    return m_cVisible;
  }
  






  private void childVis(Node r)
  {
    r.setVisible(true);
    if (r.getCVisible()) { Edge e;
      for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        childVis(e.getTarget());
      }
    }
  }
  




  public void setCVisible(boolean v)
  {
    m_cVisible = v;
    if (v) {
      childVis(this);
    }
    else if (!v) {
      childInv(this);
    }
  }
  




  private void childInv(Node r)
  {
    Edge e;
    


    for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      Node s = e.getTarget();
      s.setVisible(false);
      childInv(s);
    }
  }
  





  public String getRefer()
  {
    return m_refer;
  }
  





  public void setRefer(String v)
  {
    m_refer = v;
  }
  







  public int getShape()
  {
    return m_shape;
  }
  





  public void setShape(int v)
  {
    m_shape = v;
  }
  






  public Color getColor()
  {
    return m_color;
  }
  





  public void setColor(Color v)
  {
    m_color = v;
  }
  






  public String getLabel()
  {
    return m_label;
  }
  



  private void breakupLabel()
  {
    int prev = 0;
    for (int noa = 0; noa < m_label.length(); noa++) {
      if (m_label.charAt(noa) == '\n') {
        m_lines.addElement(m_label.substring(prev, noa));
        prev = noa + 1;
      }
    }
    m_lines.addElement(m_label.substring(prev, noa));
  }
  







  public Dimension stringSize(FontMetrics f)
  {
    Dimension d = new Dimension();
    int old = 0;
    
    int noa = 0;
    String s; while ((s = getLine(noa)) != null) {
      noa++;
      old = f.stringWidth(s);
      
      if (old > width) {
        width = old;
      }
    }
    height = (noa * f.getHeight());
    return d;
  }
  






  public String getLine(int n)
  {
    if (n < m_lines.size()) {
      return (String)m_lines.elementAt(n);
    }
    
    return null;
  }
  















  public double getCenter()
  {
    return m_center;
  }
  





  public void setCenter(double v)
  {
    m_center = v;
  }
  




  public void adjustCenter(double v)
  {
    m_center += v;
  }
  






  public double getTop()
  {
    return m_top;
  }
  





  public void setTop(double v)
  {
    m_top = v;
  }
  









  public boolean getVisible()
  {
    return m_visible;
  }
  





  private void setVisible(boolean v)
  {
    m_visible = v;
  }
  








  public boolean getRoot()
  {
    return m_root;
  }
  





  public void setRoot(boolean v)
  {
    m_root = v;
  }
  








  public Edge getParent(int i)
  {
    if (i < m_parent.size()) {
      return (Edge)m_parent.elementAt(i);
    }
    
    return null;
  }
  







  public void setParent(Edge v)
  {
    m_parent.addElement(v);
  }
  








  public Edge getChild(int i)
  {
    if (i < m_children.size()) {
      return (Edge)m_children.elementAt(i);
    }
    
    return null;
  }
  





  public void addChild(Edge v)
  {
    m_children.addElement(v);
  }
  









  public static int getGCount(Node r, int n)
  {
    if ((r.getChild(0) != null) && (r.getCVisible())) {
      n++;
      Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        n = getGCount(e.getTarget(), n);
      }
    }
    return n;
  }
  








  public static int getTotalGCount(Node r, int n)
  {
    if (r.getChild(0) != null) {
      n++;
      Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        n = getTotalGCount(e.getTarget(), n);
      }
    }
    return n;
  }
  




  public static int getCount(Node r, int n)
  {
    
    



    Edge e;
    


    for (int noa = 0; ((e = r.getChild(noa)) != null) && (r.getCVisible()); noa++) {
      n = getCount(e.getTarget(), n);
    }
    return n;
  }
  



  public static int getTotalCount(Node r, int n)
  {
    
    

    Edge e;
    

    for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      n = getTotalCount(e.getTarget(), n);
    }
    return n;
  }
  







  public static int getHeight(Node r, int l)
  {
    l++;
    int lev = l;int temp = 0;
    
    Edge e;
    for (int noa = 0; ((e = r.getChild(noa)) != null) && (r.getCVisible()); noa++) {
      temp = getHeight(e.getTarget(), l);
      if (temp > lev) {
        lev = temp;
      }
    }
    

    return lev;
  }
  








  public static int getTotalHeight(Node r, int l)
  {
    l++;
    int lev = l;int temp = 0;
    
    Edge e;
    for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      temp = getTotalHeight(e.getTarget(), l);
      if (temp > lev) {
        lev = temp;
      }
    }
    return lev;
  }
}
