package weka.gui.treevisualizer;

import java.util.Vector;




















































public class PlaceNode2
  implements NodePlace
{
  private double m_yRatio;
  private Group[] m_groups;
  private Level[] m_levels;
  private int m_groupNum;
  private int m_levelNum;
  
  public PlaceNode2() {}
  
  public void place(Node r)
  {
    m_groupNum = Node.getGCount(r, 0);
    
    m_groups = new Group[m_groupNum];
    
    for (int noa = 0; noa < m_groupNum; noa++) {
      m_groups[noa] = new Group(null);
      m_groups[noa].m_gap = 3.0D;
      m_groups[noa].m_start = -1;
    }
    
    groupBuild(r);
    m_levelNum = Node.getHeight(r, 0);
    m_yRatio = (1.0D / (m_levelNum + 1));
    
    m_levels = new Level[m_levelNum];
    
    for (int noa = 0; noa < m_levelNum; noa++) {
      m_levels[noa] = new Level(null);
    }
    r.setTop(m_yRatio);
    yPlacer();
    r.setCenter(0.0D);
    xPlacer(0);
    







    untangle2();
    
    scaleByMax();
  }
  























































































  private void xPlacer(int start)
  {
    if (m_groupNum > 0) {
      m_groups[0].m_p.setCenter(0.0D);
      for (int noa = start; noa < m_groupNum; noa++) {
        int alter = 0;
        double c = m_groups[noa].m_gap;
        Node r = m_groups[noa].m_p;
        Edge e; for (int nob = 0; (e = r.getChild(nob)) != null; nob++) {
          if (e.getTarget().getParent(0) == e) {
            e.getTarget().setCenter(nob * c);
          }
          else {
            alter++;
          }
        }
        m_groups[noa].m_size = ((nob - 1 - alter) * c);
        xShift(noa);
      }
    }
  }
  




  private void xShift(int n)
  {
    Node r = m_groups[n].m_p;
    double h = m_groups[n].m_size / 2.0D;
    double c = m_groups[n].m_p.getCenter();
    double m = c - h;
    m_groups[n].m_left = m;
    m_groups[n].m_right = (c + h);
    Edge e;
    for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      if (e.getTarget().getParent(0) == e) {
        e.getTarget().adjustCenter(m);
      }
    }
  }
  





  private void scaleByMax()
  {
    double l_x = 5000.0D;double h_x = -5000.0D;
    for (int noa = 0; noa < m_groupNum; noa++) {
      if (l_x > m_groups[noa].m_left) {
        l_x = m_groups[noa].m_left;
      }
      
      if (h_x < m_groups[noa].m_right) {
        h_x = m_groups[noa].m_right;
      }
    }
    


    double m_scale = h_x - l_x + 1.0D;
    if (m_groupNum > 0) {
      Node r = m_groups[0].m_p;
      r.setCenter((r.getCenter() - l_x) / m_scale);
      
      for (int noa = 0; noa < m_groupNum; noa++) {
        r = m_groups[noa].m_p;
        Edge e; for (int nob = 0; (e = r.getChild(nob)) != null; nob++) {
          Node s = e.getTarget();
          if (s.getParent(0) == e) {
            s.setCenter((s.getCenter() - l_x) / m_scale);
          }
        }
      }
    }
  }
  










  private void scaleByInd()
  {
    Node r = m_groups[0].m_p;
    r.setCenter(0.5D);
    
    for (int noa = 0; noa < m_levelNum; noa++) {
      double l_x = m_groups[m_levels[noa].m_start].m_left;
      double h_x = m_groups[m_levels[noa].m_end].m_right;
      double m_scale = h_x - l_x + 1.0D;
      for (int nob = m_levels[noa].m_start; nob <= m_levels[noa].m_end; nob++) {
        r = m_groups[nob].m_p;
        Edge e; for (int noc = 0; (e = r.getChild(noc)) != null; noc++) {
          Node s = e.getTarget();
          if (s.getParent(0) == e) {
            s.setCenter((s.getCenter() - l_x) / m_scale);
          }
        }
      }
    }
  }
  





  private void untangle2()
  {
    Node nf = null;Node ns = null;
    int l = 0;int times = 0;
    int tf = 0;int ts = 0;
    Ease a; while ((a = overlap(l)) != null) {
      times++;
      
      int f = m_place;
      int s = m_place + 1;
      while (f != s) {
        m_lev -= 1;
        tf = f;
        ts = s;
        f = m_groups[f].m_pg;
        s = m_groups[s].m_pg;
      }
      l = m_lev;
      int pf = 0;
      int ps = 0;
      Node r = m_groups[f].m_p;
      Node mark = m_groups[tf].m_p;
      nf = null;
      ns = null;
      for (int noa = 0; nf != mark; noa++) {
        pf++;
        nf = r.getChild(noa).getTarget();
      }
      mark = m_groups[ts].m_p;
      for (int noa = pf; ns != mark; noa++) {
        ps++;
        ns = r.getChild(noa).getTarget();
      }
      



      Vector o_pos = new Vector(20, 10);
      Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        if (e.getTarget().getParent(0) == e) {
          Double tem = new Double(e.getTarget().getCenter());
          o_pos.addElement(tem);
        }
      }
      
      pf--;
      double inc = m_amount / ps;
      for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        ns = e.getTarget();
        if (ns.getParent(0) == e) {
          if (noa > pf + ps) {
            ns.adjustCenter(m_amount);
          }
          else if (noa > pf) {
            ns.adjustCenter(inc * (noa - pf));
          }
        }
      }
      
      nf = r.getChild(0).getTarget();
      inc = ns.getCenter() - nf.getCenter();
      m_groups[f].m_size = inc;
      m_groups[f].m_left = (r.getCenter() - inc / 2.0D);
      m_groups[f].m_right = (m_groups[f].m_left + inc);
      inc = m_groups[f].m_left - nf.getCenter();
      

      int g_num = 0;
      for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
        ns = e.getTarget();
        if (ns.getParent(0) == e) {
          ns.adjustCenter(inc);
          double shift = ns.getCenter() - ((Double)o_pos.elementAt(noa)).doubleValue();
          
          if (ns.getChild(0) != null) {
            moveSubtree(m_groups[f].m_start + g_num, shift);
            g_num++;
          }
        }
      }
    }
  }
  











  private void moveSubtree(int n, double o)
  {
    Node r = m_groups[n].m_p;
    Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      if (e.getTarget().getParent(0) == e) {
        e.getTarget().adjustCenter(o);
      }
    }
    m_groups[n].m_left += o;
    m_groups[n].m_right += o;
    if (m_groups[n].m_start != -1) {
      for (int noa = m_groups[n].m_start; noa <= m_groups[n].m_end; noa++) {
        moveSubtree(noa, o);
      }
    }
  }
  






  private void untangle()
  {
    Node nf = null;Node ns = null;
    int l = 0;int times = 0;
    int tf = 0;int ts = 0;
    Ease a; while ((a = overlap(l)) != null) {
      times++;
      
      int f = m_place;
      int s = m_place + 1;
      while (f != s) {
        m_lev -= 1;
        tf = f;
        ts = s;
        f = m_groups[f].m_pg;
        s = m_groups[s].m_pg;
      }
      l = m_lev;
      int pf = 0;
      int ps = 0;
      Node r = m_groups[f].m_p;
      Node mark = m_groups[tf].m_p;
      nf = null;
      ns = null;
      for (int noa = 0; nf != mark; noa++) {
        pf++;
        nf = r.getChild(noa).getTarget();
      }
      mark = m_groups[ts].m_p;
      for (int noa = pf; ns != mark; noa++) {
        ps++;
        ns = r.getChild(noa).getTarget();
      }
      m_groups[f].m_gap = Math.ceil(m_amount / ps + m_groups[f].m_gap);
      

      xPlacer(f);
    }
  }
  






  private Ease overlap(int l)
  {
    Ease a = new Ease(null);
    for (int noa = l; noa < m_levelNum; noa++) {
      for (int nob = m_levels[noa].m_start; nob < m_levels[noa].m_end; nob++) {
        m_amount = (m_groups[nob].m_right - m_groups[(nob + 1)].m_left + 2.0D);
        

        if (m_amount >= 0.0D) {
          m_amount += 1.0D;
          m_lev = noa;
          m_place = nob;
          return a;
        }
      }
    }
    return null;
  }
  





















  private void yPlacer()
  {
    double changer = m_yRatio;
    int lev_place = 0;
    if (m_groupNum > 0) {
      m_groups[0].m_p.setTop(m_yRatio);
      m_levels[0].m_start = 0;
      
      for (int noa = 0; noa < m_groupNum; noa++) {
        if (m_groups[noa].m_p.getTop() != changer) {
          m_levels[lev_place].m_end = (noa - 1);
          lev_place++;
          m_levels[lev_place].m_start = noa;
          changer = m_groups[noa].m_p.getTop();
        }
        nodeY(m_groups[noa].m_p);
      }
      m_levels[lev_place].m_end = (m_groupNum - 1);
    }
  }
  





  private void nodeY(Node r)
  {
    double h = r.getTop() + m_yRatio;
    Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      if (e.getTarget().getParent(0) == e) {
        e.getTarget().setTop(h);
        if (e.getTarget().getVisible()) {}
      }
    }
  }
  







  private void groupBuild(Node r)
  {
    if (m_groupNum > 0) {
      m_groupNum = 0;
      m_groups[0].m_p = r;
      m_groupNum += 1;
      

      for (int noa = 0; noa < m_groupNum; noa++) {
        groupFind(m_groups[noa].m_p, noa);
      }
    }
  }
  





  private void groupFind(Node r, int pg)
  {
    boolean first = true;
    Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
      if ((e.getTarget().getParent(0) == e) && 
        (e.getTarget().getChild(0) != null) && (e.getTarget().getCVisible())) {
        if (first) {
          m_groups[pg].m_start = m_groupNum;
          first = false;
        }
        m_groups[pg].m_end = m_groupNum;
        m_groups[m_groupNum].m_p = e.getTarget();
        m_groups[m_groupNum].m_pg = pg;
        m_groups[m_groupNum].m_id = m_groupNum;
        
        m_groupNum += 1;
      }
    }
  }
  
  private class Ease
  {
    public int m_place;
    public double m_amount;
    public int m_lev;
    
    private Ease() {}
  }
  
  private class Group
  {
    public Node m_p;
    public int m_pg;
    public double m_gap;
    public double m_left;
    public double m_right;
    public double m_size;
    public int m_start;
    public int m_end;
    public int m_id;
    
    private Group() {}
  }
  
  private class Level
  {
    public int m_start;
    public int m_end;
    public int m_left;
    public int m_right;
    
    private Level() {}
  }
}
