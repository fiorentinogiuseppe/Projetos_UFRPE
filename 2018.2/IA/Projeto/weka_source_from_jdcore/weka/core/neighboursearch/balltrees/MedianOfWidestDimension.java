package weka.core.neighboursearch.balltrees;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;





























































public class MedianOfWidestDimension
  extends BallSplitter
  implements OptionHandler, TechnicalInformationHandler
{
  private static final long serialVersionUID = 3054842574468790421L;
  protected boolean m_NormalizeDimWidths = true;
  






  public MedianOfWidestDimension() {}
  






  public MedianOfWidestDimension(int[] instList, Instances insts, EuclideanDistance e)
  {
    super(instList, insts, e);
  }
  




  public String globalInfo()
  {
    return "Class that splits a BallNode of a ball tree based on the median value of the widest dimension of the points in the ball. It essentially implements Omohundro's  KD construction algorithm.";
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
  







  public void splitNode(BallNode node, int numNodesCreated)
    throws Exception
  {
    correctlyInitialized();
    
    double[][] ranges = m_DistanceFunction.initializeRanges(m_Instlist, m_Start, m_End);
    


    int splitAttrib = widestDim(ranges, m_DistanceFunction.getRanges());
    



    int medianIdxIdx = m_Start + (m_End - m_Start) / 2;
    

    int medianIdx = select(splitAttrib, m_Instlist, m_Start, m_End, (m_End - m_Start) / 2 + 1);
    


    m_SplitAttrib = splitAttrib;
    m_SplitVal = m_Instances.instance(m_Instlist[medianIdx]).value(splitAttrib);
    
    Instance pivot;
    m_Left = new BallNode(m_Start, medianIdxIdx, numNodesCreated + 1, pivot = BallNode.calcCentroidPivot(m_Start, medianIdxIdx, m_Instlist, m_Instances), BallNode.calcRadius(m_Start, medianIdxIdx, m_Instlist, m_Instances, pivot, m_DistanceFunction));
    






    m_Right = new BallNode(medianIdxIdx + 1, m_End, numNodesCreated + 2, pivot = BallNode.calcCentroidPivot(medianIdxIdx + 1, m_End, m_Instlist, m_Instances), BallNode.calcRadius(medianIdxIdx + 1, m_End, m_Instlist, m_Instances, pivot, m_DistanceFunction));
  }
  






















  protected int partition(int attIdx, int[] index, int l, int r)
  {
    double pivot = m_Instances.instance(index[((l + r) / 2)]).value(attIdx);
    

    while (l < r) {
      while ((m_Instances.instance(index[l]).value(attIdx) < pivot) && (l < r)) {
        l++;
      }
      while ((m_Instances.instance(index[r]).value(attIdx) > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        int help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (m_Instances.instance(index[r]).value(attIdx) > pivot)) {
      r--;
    }
    
    return r;
  }
  
















  public int select(int attIdx, int[] indices, int left, int right, int k)
  {
    if (left == right) {
      return left;
    }
    int middle = partition(attIdx, indices, left, right);
    if (middle - left + 1 >= k) {
      return select(attIdx, indices, left, middle, k);
    }
    return select(attIdx, indices, middle + 1, right, k - (middle - left + 1));
  }
  














  protected int widestDim(double[][] nodeRanges, double[][] universe)
  {
    int classIdx = m_Instances.classIndex();
    double widest = 0.0D;
    int w = -1;
    if (m_NormalizeDimWidths) {
      for (int i = 0; i < nodeRanges.length; i++) {
        double newWidest = nodeRanges[i][2] / universe[i][2];
        
        if ((newWidest > widest) && 
          (i != classIdx)) {
          widest = newWidest;
          w = i;
        }
        
      }
    } else {
      for (int i = 0; i < nodeRanges.length; i++) {
        if ((nodeRanges[i][2] > widest) && 
          (i != classIdx)) {
          widest = nodeRanges[i][2];
          w = i;
        }
      }
    }
    return w;
  }
  





  public String normalizeDimWidthsTipText()
  {
    return "Whether to normalize the widths(ranges) of the dimensions (attributes) before selecting the widest one.";
  }
  







  public void setNormalizeDimWidths(boolean normalize)
  {
    m_NormalizeDimWidths = normalize;
  }
  




  public boolean getNormalizeDimWidths()
  {
    return m_NormalizeDimWidths;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tNormalize dimensions' widths.", "N", 0, "-N"));
    


    return newVector.elements();
  }
  














  public void setOptions(String[] options)
    throws Exception
  {
    setNormalizeDimWidths(Utils.getFlag('N', options));
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getNormalizeDimWidths()) {
      result.add("-N");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
