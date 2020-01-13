package weka.estimators;

import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;












































public class KernelEstimator
  extends Estimator
  implements IncrementalEstimator
{
  private static final long serialVersionUID = 3646923563367683925L;
  private double[] m_Values;
  private double[] m_Weights;
  private int m_NumValues;
  private double m_SumOfWeights;
  private double m_StandardDev;
  private double m_Precision;
  private boolean m_AllWeightsOne;
  private static double MAX_ERROR = 0.01D;
  







  private int findNearestValue(double key)
  {
    int low = 0;
    int high = m_NumValues;
    int middle = 0;
    while (low < high) {
      middle = (low + high) / 2;
      double current = m_Values[middle];
      if (current == key) {
        return middle;
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
  











  public KernelEstimator(double precision)
  {
    m_Values = new double[50];
    m_Weights = new double[50];
    m_NumValues = 0;
    m_SumOfWeights = 0.0D;
    m_AllWeightsOne = true;
    m_Precision = precision;
    
    if (m_Precision < Utils.SMALL) { m_Precision = Utils.SMALL;
    }
    m_StandardDev = (m_Precision / 6.0D);
  }
  






  public void addValue(double data, double weight)
  {
    if (weight == 0.0D) {
      return;
    }
    data = round(data);
    int insertIndex = findNearestValue(data);
    if ((m_NumValues <= insertIndex) || (m_Values[insertIndex] != data)) {
      if (m_NumValues < m_Values.length) {
        int left = m_NumValues - insertIndex;
        System.arraycopy(m_Values, insertIndex, m_Values, insertIndex + 1, left);
        
        System.arraycopy(m_Weights, insertIndex, m_Weights, insertIndex + 1, left);
        

        m_Values[insertIndex] = data;
        m_Weights[insertIndex] = weight;
        m_NumValues += 1;
      } else {
        double[] newValues = new double[m_Values.length * 2];
        double[] newWeights = new double[m_Values.length * 2];
        int left = m_NumValues - insertIndex;
        System.arraycopy(m_Values, 0, newValues, 0, insertIndex);
        System.arraycopy(m_Weights, 0, newWeights, 0, insertIndex);
        newValues[insertIndex] = data;
        newWeights[insertIndex] = weight;
        System.arraycopy(m_Values, insertIndex, newValues, insertIndex + 1, left);
        
        System.arraycopy(m_Weights, insertIndex, newWeights, insertIndex + 1, left);
        
        m_NumValues += 1;
        m_Values = newValues;
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
    double range = m_Values[(m_NumValues - 1)] - m_Values[0];
    if (range > 0.0D) {
      m_StandardDev = Math.max(range / Math.sqrt(m_SumOfWeights), m_Precision / 6.0D);
    }
  }
  








  public double getProbability(double data)
  {
    double delta = 0.0D;double sum = 0.0D;double currentProb = 0.0D;
    double zLower = 0.0D;double zUpper = 0.0D;
    if (m_NumValues == 0) {
      zLower = (data - m_Precision / 2.0D) / m_StandardDev;
      zUpper = (data + m_Precision / 2.0D) / m_StandardDev;
      return Statistics.normalProbability(zUpper) - Statistics.normalProbability(zLower);
    }
    
    double weightSum = 0.0D;
    int start = findNearestValue(data);
    for (int i = start; i < m_NumValues; i++) {
      delta = m_Values[i] - data;
      zLower = (delta - m_Precision / 2.0D) / m_StandardDev;
      zUpper = (delta + m_Precision / 2.0D) / m_StandardDev;
      currentProb = Statistics.normalProbability(zUpper) - Statistics.normalProbability(zLower);
      
      sum += currentProb * m_Weights[i];
      





      weightSum += m_Weights[i];
      if (currentProb * (m_SumOfWeights - weightSum) < sum * MAX_ERROR) {
        break;
      }
    }
    for (int i = start - 1; i >= 0; i--) {
      delta = m_Values[i] - data;
      zLower = (delta - m_Precision / 2.0D) / m_StandardDev;
      zUpper = (delta + m_Precision / 2.0D) / m_StandardDev;
      currentProb = Statistics.normalProbability(zUpper) - Statistics.normalProbability(zLower);
      
      sum += currentProb * m_Weights[i];
      weightSum += m_Weights[i];
      if (currentProb * (m_SumOfWeights - weightSum) < sum * MAX_ERROR) {
        break;
      }
    }
    return sum / m_SumOfWeights;
  }
  

  public String toString()
  {
    String result = m_NumValues + " Normal Kernels. \nStandardDev = " + Utils.doubleToString(m_StandardDev, 6, 4) + " Precision = " + m_Precision;
    

    if (m_NumValues == 0) {
      result = result + "  \nMean = 0";
    } else {
      result = result + "  \nMeans =";
      for (int i = 0; i < m_NumValues; i++) {
        result = result + " " + m_Values[i];
      }
      if (!m_AllWeightsOne) {
        result = result + "\nWeights = ";
        for (int i = 0; i < m_NumValues; i++) {
          result = result + " " + m_Weights[i];
        }
      }
    }
    return result + "\n";
  }
  




  public int getNumKernels()
  {
    return m_NumValues;
  }
  




  public double[] getMeans()
  {
    return m_Values;
  }
  




  public double[] getWeights()
  {
    return m_Weights;
  }
  




  public double getPrecision()
  {
    return m_Precision;
  }
  




  public double getStdDev()
  {
    return m_StandardDev;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    if (!m_noClass) {
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    } else {
      result.enable(Capabilities.Capability.NO_CLASS);
    }
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5540 $");
  }
  




  public static void main(String[] argv)
  {
    try
    {
      if (argv.length < 2) {
        System.out.println("Please specify a set of instances.");
        return;
      }
      KernelEstimator newEst = new KernelEstimator(0.01D);
      for (int i = 0; i < argv.length - 3; i += 2) {
        newEst.addValue(Double.valueOf(argv[i]).doubleValue(), Double.valueOf(argv[(i + 1)]).doubleValue());
      }
      
      System.out.println(newEst);
      
      double start = Double.valueOf(argv[(argv.length - 2)]).doubleValue();
      double finish = Double.valueOf(argv[(argv.length - 1)]).doubleValue();
      for (double current = start; current < finish; 
          current += (finish - start) / 50.0D) {
        System.out.println("Data: " + current + " " + newEst.getProbability(current));
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
