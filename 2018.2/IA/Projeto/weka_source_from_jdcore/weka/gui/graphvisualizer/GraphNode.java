package weka.gui.graphvisualizer;



































public class GraphNode
  implements GraphConstants
{
  public int x = 0; public int y = 0;
  





  public int nodeType = 3;
  String ID;
  String lbl;
  String[] outcomes;
  
  public GraphNode(String id, String label)
  {
    ID = id;lbl = label;nodeType = 3;
  }
  



  public GraphNode(String id, String label, int type)
  {
    ID = id;lbl = label;nodeType = type;
  }
  

  double[][] probs;
  
  int[] prnts;
  int[][] edges;
  public boolean equals(Object n)
  {
    if (((n instanceof GraphNode)) && (ID.equalsIgnoreCase(ID)))
    {

      return true;
    }
    
    return false;
  }
}
