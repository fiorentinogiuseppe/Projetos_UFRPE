package weka.gui.graphvisualizer;







public class GraphEdge
{
  protected int src;
  





  protected int dest;
  




  protected int type;
  




  protected String srcLbl;
  




  protected String destLbl;
  





  public GraphEdge(int s, int d, int t)
  {
    src = s;dest = d;type = t;
    srcLbl = null;destLbl = null;
  }
  
  public GraphEdge(int s, int d, int t, String sLbl, String dLbl) {
    src = s;dest = d;type = t;
    srcLbl = sLbl;destLbl = dLbl;
  }
  
  public String toString() {
    return "(" + src + "," + dest + "," + type + ")";
  }
  
  public boolean equals(Object e) {
    if (((e instanceof GraphEdge)) && (src == src) && (dest == dest) && (type == type))
    {


      return true;
    }
    return false;
  }
}
