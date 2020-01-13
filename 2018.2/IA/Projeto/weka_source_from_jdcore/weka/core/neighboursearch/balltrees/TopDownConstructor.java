package weka.core.neighboursearch.balltrees;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
































































public class TopDownConstructor
  extends BallTreeConstructor
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -5150140645091889979L;
  protected BallSplitter m_Splitter = new PointsClosestToFurthestChildren();
  





  public TopDownConstructor() {}
  




  public String globalInfo()
  {
    return "The class implementing the TopDown construction method of ball trees. It further uses one of a number of different splitting methods to split a ball while constructing the tree top down.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Stephen M. Omohundro");
    result.setValue(TechnicalInformation.Field.YEAR, "1989");
    result.setValue(TechnicalInformation.Field.TITLE, "Five Balltree Construction Algorithms");
    result.setValue(TechnicalInformation.Field.MONTH, "December");
    result.setValue(TechnicalInformation.Field.NUMBER, "TR-89-063");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "International Computer Science Institute");
    
    return result;
  }
  






  public BallNode buildTree()
    throws Exception
  {
    m_NumNodes = (this.m_MaxDepth = 0);
    m_NumLeaves = 1;
    
    m_Splitter.setInstances(m_Instances);
    m_Splitter.setInstanceList(m_InstList);
    m_Splitter.setEuclideanDistanceFunction((EuclideanDistance)m_DistanceFunction);
    

    BallNode root = new BallNode(0, m_InstList.length - 1, 0);
    root.setPivot(BallNode.calcCentroidPivot(m_InstList, m_Instances));
    root.setRadius(BallNode.calcRadius(m_InstList, m_Instances, root.getPivot(), m_DistanceFunction));
    
    splitNodes(root, m_MaxDepth + 1, m_Radius);
    
    return root;
  }
  










  protected void splitNodes(BallNode node, int depth, double rootRadius)
    throws Exception
  {
    if ((m_NumInstances <= m_MaxInstancesInLeaf) || (rootRadius == 0.0D) || (m_Radius / rootRadius < m_MaxRelLeafRadius))
    {
      return;
    }
    m_NumLeaves -= 1;
    m_Splitter.splitNode(node, m_NumNodes);
    m_NumNodes += 2;
    m_NumLeaves += 2;
    
    if (m_MaxDepth < depth) {
      m_MaxDepth = depth;
    }
    splitNodes(m_Left, depth + 1, rootRadius);
    splitNodes(m_Right, depth + 1, rootRadius);
    
    if (m_FullyContainChildBalls) {
      double radius = BallNode.calcRadius(m_Left, m_Right, node.getPivot(), m_DistanceFunction);
      
      Instance pivot = BallNode.calcPivot(m_Left, m_Right, m_Instances);
      







      node.setRadius(radius);
    }
  }
  










  public int[] addInstance(BallNode node, Instance inst)
    throws Exception
  {
    if ((m_Left != null) && (m_Right != null))
    {

      double leftDist = m_DistanceFunction.distance(inst, m_Left.getPivot(), Double.POSITIVE_INFINITY);
      
      double rightDist = m_DistanceFunction.distance(inst, m_Right.getPivot(), Double.POSITIVE_INFINITY);
      
      if (leftDist < rightDist) {
        addInstance(m_Left, inst);
        
        processNodesAfterAddInstance(m_Right);
      }
      else {
        addInstance(m_Right, inst);
      }
      
      m_End += 1;
    } else {
      if ((m_Left != null) || (m_Right != null)) {
        throw new Exception("Error: Only one leaf of the built ball tree is assigned. Please check code.");
      }
      


      int index = m_Instances.numInstances() - 1;
      
      int[] instList = new int[m_Instances.numInstances()];
      System.arraycopy(m_InstList, 0, instList, 0, m_End + 1);
      if (m_End < m_InstList.length - 1)
        System.arraycopy(m_InstList, m_End + 2, instList, m_End + 2, m_InstList.length - m_End - 1);
      instList[(m_End + 1)] = index;
      m_End += 1;
      m_NumInstances += 1;
      m_InstList = instList;
      
      m_Splitter.setInstanceList(m_InstList);
      
      if (m_NumInstances > m_MaxInstancesInLeaf) {
        m_Splitter.splitNode(node, m_NumNodes);
        m_NumNodes += 2;
      }
    }
    return m_InstList;
  }
  







  protected void processNodesAfterAddInstance(BallNode node)
  {
    m_Start += 1;
    m_End += 1;
    
    if ((m_Left != null) && (m_Right != null)) {
      processNodesAfterAddInstance(m_Left);
      processNodesAfterAddInstance(m_Right);
    }
  }
  





  public String ballSplitterTipText()
  {
    return "The BallSplitter algorithm set that would be used by the TopDown BallTree constructor.";
  }
  






  public BallSplitter getBallSplitter()
  {
    return m_Splitter;
  }
  




  public void setBallSplitter(BallSplitter splitter)
  {
    m_Splitter = splitter;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tBall splitting algorithm to use.", "S", 1, "-S <classname and options>"));
    


    return newVector.elements();
  }
  














  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      String[] nnSearchClassSpec = Utils.splitOptions(optionString);
      if (nnSearchClassSpec.length == 0) {
        throw new Exception("Invalid BallSplitter specification string.");
      }
      String className = nnSearchClassSpec[0];
      nnSearchClassSpec[0] = "";
      
      setBallSplitter((BallSplitter)Utils.forName(BallSplitter.class, className, nnSearchClassSpec));

    }
    else
    {
      setBallSplitter(new PointsClosestToFurthestChildren());
    }
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-S");
    result.add(m_Splitter.getClass().getName());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
