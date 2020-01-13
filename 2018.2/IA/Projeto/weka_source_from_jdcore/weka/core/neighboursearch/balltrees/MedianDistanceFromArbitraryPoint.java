package weka.core.neighboursearch.balltrees;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
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











































































public class MedianDistanceFromArbitraryPoint
  extends BallSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 5617378551363700558L;
  protected int m_RandSeed = 17;
  




  protected Random m_Rand;
  





  public MedianDistanceFromArbitraryPoint() {}
  




  public MedianDistanceFromArbitraryPoint(int[] instList, Instances insts, EuclideanDistance e)
  {
    super(instList, insts, e);
  }
  





  public String globalInfo()
  {
    return "Class that splits a BallNode of a ball tree using Uhlmann's described method.\n\nFor information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jeffrey K. Uhlmann");
    result.setValue(TechnicalInformation.Field.TITLE, "Satisfying general proximity/similarity queries with metric trees");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Information Processing Letters");
    result.setValue(TechnicalInformation.Field.MONTH, "November");
    result.setValue(TechnicalInformation.Field.YEAR, "1991");
    result.setValue(TechnicalInformation.Field.NUMBER, "4");
    result.setValue(TechnicalInformation.Field.VOLUME, "40");
    result.setValue(TechnicalInformation.Field.PAGES, "175-179");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MASTERSTHESIS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Ashraf Masood Kibriya");
    additional.setValue(TechnicalInformation.Field.TITLE, "Fast Algorithms for Nearest Neighbour Search");
    additional.setValue(TechnicalInformation.Field.YEAR, "2007");
    additional.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, School of Computing and Mathematical Sciences, University of Waikato");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe seed value for the random number generator.\n\t(default: 17)", "S", 1, "-S <num>"));
    



    return result.elements();
  }
  















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() > 0) {
      setRandomSeed(Integer.parseInt(tmpStr));
    } else {
      setRandomSeed(17);
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
    result.add("" + getRandomSeed());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  



  public void setRandomSeed(int seed)
  {
    m_RandSeed = seed;
  }
  




  public int getRandomSeed()
  {
    return m_RandSeed;
  }
  





  public String randomSeedTipText()
  {
    return "The seed value for the random number generator.";
  }
  







  public void splitNode(BallNode node, int numNodesCreated)
    throws Exception
  {
    correctlyInitialized();
    
    m_Rand = new Random(m_RandSeed);
    
    int ridx = m_Start + m_Rand.nextInt(m_NumInstances);
    Instance randomInst = (Instance)m_Instances.instance(m_Instlist[ridx]).copy();
    
    double[] distList = new double[m_NumInstances - 1];
    
    int i = m_Start; for (int j = 0; i < m_End; j++) {
      Instance temp = m_Instances.instance(m_Instlist[i]);
      distList[j] = m_DistanceFunction.distance(randomInst, temp, Double.POSITIVE_INFINITY);i++;
    }
    


    int medianIdx = select(distList, m_Instlist, 0, distList.length - 1, m_Start, (m_End - m_Start) / 2 + 1) + m_Start;
    

    Instance pivot;
    
    m_Left = new BallNode(m_Start, medianIdx, numNodesCreated + 1, pivot = BallNode.calcCentroidPivot(m_Start, medianIdx, m_Instlist, m_Instances), BallNode.calcRadius(m_Start, medianIdx, m_Instlist, m_Instances, pivot, m_DistanceFunction));
    







    m_Right = new BallNode(medianIdx + 1, m_End, numNodesCreated + 2, pivot = BallNode.calcCentroidPivot(medianIdx + 1, m_End, m_Instlist, m_Instances), BallNode.calcRadius(medianIdx + 1, m_End, m_Instlist, m_Instances, pivot, m_DistanceFunction));
  }
  

























  protected int partition(double[] array, int[] index, int l, int r, int indexStart)
  {
    double pivot = array[((l + r) / 2)];
    

    while (l < r) {
      while ((array[l] < pivot) && (l < r)) {
        l++;
      }
      while ((array[r] > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        int help = index[(indexStart + l)];
        index[(indexStart + l)] = index[(indexStart + r)];
        index[(indexStart + r)] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (array[r] > pivot)) {
      r--;
    }
    
    return r;
  }
  



















  protected int select(double[] array, int[] indices, int left, int right, int indexStart, int k)
  {
    if (left == right) {
      return left;
    }
    int middle = partition(array, indices, left, right, indexStart);
    if (middle - left + 1 >= k) {
      return select(array, indices, left, middle, indexStart, k);
    }
    return select(array, indices, middle + 1, right, indexStart, k - (middle - left + 1));
  }
  







  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
