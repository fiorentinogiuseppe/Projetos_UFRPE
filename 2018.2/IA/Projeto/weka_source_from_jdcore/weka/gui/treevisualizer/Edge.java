package weka.gui.treevisualizer;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.Vector;


















































public class Edge
{
  private String m_label;
  private String m_rsource;
  private String m_rtarget;
  private Node m_source;
  private Node m_target;
  private Vector m_lines;
  
  public Edge(String label, String source, String target)
  {
    m_label = label;
    m_rsource = source;
    m_rtarget = target;
    m_lines = new Vector(3, 2);
    breakupLabel();
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
  







  public String getRsource()
  {
    return m_rsource;
  }
  





  public void setRsource(String v)
  {
    m_rsource = v;
  }
  







  public String getRtarget()
  {
    return m_rtarget;
  }
  





  public void setRtarget(String v)
  {
    m_rtarget = v;
  }
  





  public Node getSource()
  {
    return m_source;
  }
  






  public void setSource(Node v)
  {
    m_source = v;
    v.addChild(this);
  }
  





  public Node getTarget()
  {
    return m_target;
  }
  






  public void setTarget(Node v)
  {
    m_target = v;
    v.setParent(this);
  }
}
