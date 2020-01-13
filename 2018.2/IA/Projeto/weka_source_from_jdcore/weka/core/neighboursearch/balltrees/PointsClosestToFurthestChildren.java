package weka.core.neighboursearch.balltrees;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;







































































public class PointsClosestToFurthestChildren
  extends BallSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -2947177543565818260L;
  
  public String globalInfo()
  {
    return "Implements the Moore's method to split a node of a ball tree.\n\nFor more information please see section 2 of the 1st and 3.2.3 of the 2nd:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew W. Moore");
    result.setValue(TechnicalInformation.Field.TITLE, "The Anchors Hierarchy: Using the Triangle Inequality to Survive High Dimensional Data");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "UAI '00: Proceedings of the 16th Conference on Uncertainty in Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.PAGES, "397-405");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann Publishers Inc.");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco, CA, USA");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MASTERSTHESIS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Ashraf Masood Kibriya");
    additional.setValue(TechnicalInformation.Field.TITLE, "Fast Algorithms for Nearest Neighbour Search");
    additional.setValue(TechnicalInformation.Field.YEAR, "2007");
    additional.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, School of Computing and Mathematical Sciences, University of Waikato");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    return result;
  }
  





  public PointsClosestToFurthestChildren() {}
  





  public PointsClosestToFurthestChildren(int[] instList, Instances insts, EuclideanDistance e)
  {
    super(instList, insts, e);
  }
  







  public void splitNode(BallNode node, int numNodesCreated)
    throws Exception
  {
    correctlyInitialized();
    
    double maxDist = Double.NEGATIVE_INFINITY;double dist = 0.0D;
    Instance furthest1 = null;Instance furthest2 = null;Instance pivot = node.getPivot();
    double[] distList = new double[m_NumInstances];
    for (int i = m_Start; i <= m_End; i++) {
      Instance temp = m_Instances.instance(m_Instlist[i]);
      dist = m_DistanceFunction.distance(pivot, temp, Double.POSITIVE_INFINITY);
      if (dist > maxDist) {
        maxDist = dist;furthest1 = temp;
      }
    }
    maxDist = Double.NEGATIVE_INFINITY;
    furthest1 = (Instance)furthest1.copy();
    for (int i = 0; i < m_NumInstances; i++) {
      Instance temp = m_Instances.instance(m_Instlist[(i + m_Start)]);
      distList[i] = m_DistanceFunction.distance(furthest1, temp, Double.POSITIVE_INFINITY);
      
      if (distList[i] > maxDist) {
        maxDist = distList[i];furthest2 = temp;
      }
    }
    furthest2 = (Instance)furthest2.copy();
    dist = 0.0D;int numRight = 0;
    
    int i = 0; for (int j = 0; i < m_NumInstances - numRight; j++) {
      Instance temp = m_Instances.instance(m_Instlist[(i + m_Start)]);
      dist = m_DistanceFunction.distance(furthest2, temp, Double.POSITIVE_INFINITY);
      if (dist < distList[i]) {
        int t = m_Instlist[(m_End - numRight)];
        m_Instlist[(m_End - numRight)] = m_Instlist[(i + m_Start)];
        m_Instlist[(i + m_Start)] = t;
        double d = distList[(distList.length - 1 - numRight)];
        distList[(distList.length - 1 - numRight)] = distList[i];
        distList[i] = d;
        numRight++;
        i--;
      }
      i++;
    }
    












    if ((numRight <= 0) || (numRight >= m_NumInstances)) {
      throw new Exception("Illegal value for numRight: " + numRight);
    }
    m_Left = new BallNode(m_Start, m_End - numRight, numNodesCreated + 1, pivot = BallNode.calcCentroidPivot(m_Start, m_End - numRight, m_Instlist, m_Instances), BallNode.calcRadius(m_Start, m_End - numRight, m_Instlist, m_Instances, pivot, m_DistanceFunction));
    








    m_Right = new BallNode(m_End - numRight + 1, m_End, numNodesCreated + 2, pivot = BallNode.calcCentroidPivot(m_End - numRight + 1, m_End, m_Instlist, m_Instances), BallNode.calcRadius(m_End - numRight + 1, m_End, m_Instlist, m_Instances, pivot, m_DistanceFunction));
  }
  











  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
