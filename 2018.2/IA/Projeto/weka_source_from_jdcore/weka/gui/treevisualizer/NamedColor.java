package weka.gui.treevisualizer;

import java.awt.Color;






































public class NamedColor
{
  public String m_name;
  public Color m_col;
  
  public NamedColor(String n, int r, int g, int b)
  {
    m_name = n;
    m_col = new Color(r, g, b);
  }
}
