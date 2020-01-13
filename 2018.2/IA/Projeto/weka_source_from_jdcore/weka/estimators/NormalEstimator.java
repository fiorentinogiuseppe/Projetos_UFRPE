package weka.estimators;

import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;
















































public class NormalEstimator
  extends Estimator
  implements IncrementalEstimator
{
  private static final long serialVersionUID = 93584379632315841L;
  private double m_SumOfWeights;
  private double m_SumOfValues;
  private double m_SumOfValuesSq;
  private double m_Mean;
  private double m_StandardDev;
  private double m_Precision;
  
  private double round(double data)
  {
    return Math.rint(data / m_Precision) * m_Precision;
  }
  











  public NormalEstimator(double precision)
  {
    m_Precision = precision;
    

    m_StandardDev = (m_Precision / 6.0D);
  }
  






  public void addValue(double data, double weight)
  {
    if (weight == 0.0D) {
      return;
    }
    data = round(data);
    m_SumOfWeights += weight;
    m_SumOfValues += data * weight;
    m_SumOfValuesSq += data * data * weight;
    
    if (m_SumOfWeights > 0.0D) {
      m_Mean = (m_SumOfValues / m_SumOfWeights);
      double stdDev = Math.sqrt(Math.abs(m_SumOfValuesSq - m_Mean * m_SumOfValues) / m_SumOfWeights);
      



      if (stdDev > 1.0E-10D) {
        m_StandardDev = Math.max(m_Precision / 6.0D, stdDev);
      }
    }
  }
  








  public double getProbability(double data)
  {
    data = round(data);
    double zLower = (data - m_Mean - m_Precision / 2.0D) / m_StandardDev;
    double zUpper = (data - m_Mean + m_Precision / 2.0D) / m_StandardDev;
    
    double pLower = Statistics.normalProbability(zLower);
    double pUpper = Statistics.normalProbability(zUpper);
    return pUpper - pLower;
  }
  



  public String toString()
  {
    return "Normal Distribution. Mean = " + Utils.doubleToString(m_Mean, 4) + " StandardDev = " + Utils.doubleToString(m_StandardDev, 4) + " WeightSum = " + Utils.doubleToString(m_SumOfWeights, 4) + " Precision = " + m_Precision + "\n";
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
  




  public double getMean()
  {
    return m_Mean;
  }
  




  public double getStdDev()
  {
    return m_StandardDev;
  }
  




  public double getPrecision()
  {
    return m_Precision;
  }
  




  public double getSumOfWeights()
  {
    return m_SumOfWeights;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5540 $");
  }
  





  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        System.out.println("Please specify a set of instances.");
        return;
      }
      NormalEstimator newEst = new NormalEstimator(0.01D);
      for (int i = 0; i < argv.length; i++) {
        double current = Double.valueOf(argv[i]).doubleValue();
        System.out.println(newEst);
        System.out.println("Prediction for " + current + " = " + newEst.getProbability(current));
        
        newEst.addValue(current, 1.0D);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
