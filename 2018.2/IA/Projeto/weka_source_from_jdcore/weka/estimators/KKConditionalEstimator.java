package weka.estimators;

import java.io.PrintStream;
import java.util.Random;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;





















































public class KKConditionalEstimator
  implements ConditionalEstimator
{
  private double[] m_Values;
  private double[] m_CondValues;
  private double[] m_Weights;
  private int m_NumValues;
  private double m_SumOfWeights;
  private double m_StandardDev;
  private boolean m_AllWeightsOne;
  private double m_Precision;
  
  private int findNearestPair(double key, double secondaryKey)
  {
    int low = 0;
    int high = m_NumValues;
    int middle = 0;
    while (low < high) {
      middle = (low + high) / 2;
      double current = m_CondValues[middle];
      if (current == key) {
        double secondary = m_Values[middle];
        if (secondary == secondaryKey) {
          return middle;
        }
        if (secondary > secondaryKey) {
          high = middle;
        } else if (secondary < secondaryKey) {
          low = middle + 1;
        }
      }
      if (current > key) {
        high = middle;
      } else if (current < key) {
        low = middle + 1;
      }
    }
    return low;
  }
  






  private double round(double data)
  {
    return Math.rint(data / m_Precision) * m_Precision;
  }
  







  public KKConditionalEstimator(double precision)
  {
    m_CondValues = new double[50];
    m_Values = new double[50];
    m_Weights = new double[50];
    m_NumValues = 0;
    m_SumOfWeights = 0.0D;
    m_StandardDev = 0.0D;
    m_AllWeightsOne = true;
    m_Precision = precision;
  }
  







  public void addValue(double data, double given, double weight)
  {
    data = round(data);
    given = round(given);
    int insertIndex = findNearestPair(given, data);
    if ((m_NumValues <= insertIndex) || (m_CondValues[insertIndex] != given) || (m_Values[insertIndex] != data))
    {

      if (m_NumValues < m_Values.length) {
        int left = m_NumValues - insertIndex;
        System.arraycopy(m_Values, insertIndex, m_Values, insertIndex + 1, left);
        
        System.arraycopy(m_CondValues, insertIndex, m_CondValues, insertIndex + 1, left);
        
        System.arraycopy(m_Weights, insertIndex, m_Weights, insertIndex + 1, left);
        
        m_Values[insertIndex] = data;
        m_CondValues[insertIndex] = given;
        m_Weights[insertIndex] = weight;
        m_NumValues += 1;
      } else {
        double[] newValues = new double[m_Values.length * 2];
        double[] newCondValues = new double[m_Values.length * 2];
        double[] newWeights = new double[m_Values.length * 2];
        int left = m_NumValues - insertIndex;
        System.arraycopy(m_Values, 0, newValues, 0, insertIndex);
        System.arraycopy(m_CondValues, 0, newCondValues, 0, insertIndex);
        System.arraycopy(m_Weights, 0, newWeights, 0, insertIndex);
        newValues[insertIndex] = data;
        newCondValues[insertIndex] = given;
        newWeights[insertIndex] = weight;
        System.arraycopy(m_Values, insertIndex, newValues, insertIndex + 1, left);
        
        System.arraycopy(m_CondValues, insertIndex, newCondValues, insertIndex + 1, left);
        
        System.arraycopy(m_Weights, insertIndex, newWeights, insertIndex + 1, left);
        
        m_NumValues += 1;
        m_Values = newValues;
        m_CondValues = newCondValues;
        m_Weights = newWeights;
      }
      if (weight != 1.0D) {
        m_AllWeightsOne = false;
      }
    } else {
      m_Weights[insertIndex] += weight;
      m_AllWeightsOne = false;
    }
    m_SumOfWeights += weight;
    double range = m_CondValues[(m_NumValues - 1)] - m_CondValues[0];
    m_StandardDev = Math.max(range / Math.sqrt(m_SumOfWeights), m_Precision / 6.0D);
  }
  








  public Estimator getEstimator(double given)
  {
    Estimator result = new KernelEstimator(m_Precision);
    if (m_NumValues == 0) {
      return result;
    }
    
    double delta = 0.0D;double currentProb = 0.0D;
    
    for (int i = 0; i < m_NumValues; i++) {
      delta = m_CondValues[i] - given;
      double zLower = (delta - m_Precision / 2.0D) / m_StandardDev;
      double zUpper = (delta + m_Precision / 2.0D) / m_StandardDev;
      currentProb = Statistics.normalProbability(zUpper) - Statistics.normalProbability(zLower);
      
      result.addValue(m_Values[i], currentProb * m_Weights[i]);
    }
    return result;
  }
  







  public double getProbability(double data, double given)
  {
    return getEstimator(given).getProbability(data);
  }
  



  public String toString()
  {
    String result = "KK Conditional Estimator. " + m_NumValues + " Normal Kernels:\n" + "StandardDev = " + Utils.doubleToString(m_StandardDev, 4, 2) + "  \nMeans =";
    


    for (int i = 0; i < m_NumValues; i++) {
      result = result + " (" + m_Values[i] + ", " + m_CondValues[i] + ")";
      if (!m_AllWeightsOne) {
        result = result + "w=" + m_Weights[i];
      }
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  






  public static void main(String[] argv)
  {
    try
    {
      int seed = 42;
      if (argv.length > 0) {
        seed = Integer.parseInt(argv[0]);
      }
      KKConditionalEstimator newEst = new KKConditionalEstimator(0.1D);
      

      Random r = new Random(seed);
      
      int numPoints = 50;
      if (argv.length > 2) {
        numPoints = Integer.parseInt(argv[2]);
      }
      for (int i = 0; i < numPoints; i++) {
        int x = Math.abs(r.nextInt() % 100);
        int y = Math.abs(r.nextInt() % 100);
        System.out.println("# " + x + "  " + y);
        newEst.addValue(x, y, 1.0D);
      }
      int cond;
      int cond;
      if (argv.length > 1) {
        cond = Integer.parseInt(argv[1]);
      } else {
        cond = Math.abs(r.nextInt() % 100);
      }
      System.out.println("## Conditional = " + cond);
      Estimator result = newEst.getEstimator(cond);
      for (int i = 0; i <= 100; i += 5) {
        System.out.println(" " + i + "  " + result.getProbability(i));
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
