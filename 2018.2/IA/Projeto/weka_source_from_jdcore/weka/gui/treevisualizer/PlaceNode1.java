package weka.gui.treevisualizer;











public class PlaceNode1
  implements NodePlace
{
  private double[] m_levels;
  








  private int m_noLevels;
  








  private int[] m_levelNode;
  








  private double m_yRatio;
  









  public PlaceNode1() {}
  









  public void place(Node r)
  {
    m_noLevels = (Node.getHeight(r, 0) + 1);
    
    m_yRatio = (1.0D / m_noLevels);
    
    m_levels = new double[m_noLevels];
    m_levelNode = new int[m_noLevels];
    for (int noa = 0; noa < m_noLevels; noa++) {
      m_levels[noa] = 1.0D;
      m_levelNode[noa] = 0;
    }
    
    setNumOfNodes(r, 0);
    
    for (int noa = 0; noa < m_noLevels; noa++) {
      m_levels[noa] = (1.0D / m_levels[noa]);
    }
    
    placer(r, 0);
  }
  






  private void setNumOfNodes(Node r, int l)
  {
    l++;
    
    m_levels[l] += 1.0D;
    Edge e; for (int noa = 0; ((e = r.getChild(noa)) != null) && (r.getCVisible()); noa++) {
      setNumOfNodes(e.getTarget(), l);
    }
  }
  






  private void placer(Node r, int l)
  {
    l++;
    m_levelNode[l] += 1;
    r.setCenter(m_levelNode[l] * m_levels[l]);
    r.setTop(l * m_yRatio);
    Edge e; for (int noa = 0; ((e = r.getChild(noa)) != null) && (r.getCVisible()); noa++) {
      placer(e.getTarget(), l);
    }
  }
}
