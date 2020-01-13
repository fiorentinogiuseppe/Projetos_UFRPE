package weka.estimators;

import java.io.PrintStream;
import java.util.Random;
import java.util.Vector;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.matrix.Matrix;






























public class NNConditionalEstimator
  implements ConditionalEstimator
{
  private Vector m_Values = new Vector();
  

  private Vector m_CondValues = new Vector();
  

  private Vector m_Weights = new Vector();
  

  private double m_SumOfWeights;
  

  private double m_CondMean;
  

  private double m_ValueMean;
  

  private Matrix m_Covariance;
  

  private boolean m_AllWeightsOne = true;
  

  private static double TWO_PI = 6.283185307179586D;
  





  public NNConditionalEstimator() {}
  




  private int findNearestPair(double key, double secondaryKey)
  {
    int low = 0;
    int high = m_CondValues.size();
    int middle = 0;
    while (low < high) {
      middle = (low + high) / 2;
      double current = ((Double)m_CondValues.elementAt(middle)).doubleValue();
      if (current == key) {
        double secondary = ((Double)m_Values.elementAt(middle)).doubleValue();
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
  

  private void calculateCovariance()
  {
    double sumValues = 0.0D;double sumConds = 0.0D;
    for (int i = 0; i < m_Values.size(); i++) {
      sumValues += ((Double)m_Values.elementAt(i)).doubleValue() * ((Double)m_Weights.elementAt(i)).doubleValue();
      
      sumConds += ((Double)m_CondValues.elementAt(i)).doubleValue() * ((Double)m_Weights.elementAt(i)).doubleValue();
    }
    
    m_ValueMean = (sumValues / m_SumOfWeights);
    m_CondMean = (sumConds / m_SumOfWeights);
    double c00 = 0.0D;double c01 = 0.0D;double c10 = 0.0D;double c11 = 0.0D;
    for (int i = 0; i < m_Values.size(); i++) {
      double x = ((Double)m_Values.elementAt(i)).doubleValue();
      double y = ((Double)m_CondValues.elementAt(i)).doubleValue();
      double weight = ((Double)m_Weights.elementAt(i)).doubleValue();
      c00 += (x - m_ValueMean) * (x - m_ValueMean) * weight;
      c01 += (x - m_ValueMean) * (y - m_CondMean) * weight;
      c11 += (y - m_CondMean) * (y - m_CondMean) * weight;
    }
    c00 /= (m_SumOfWeights - 1.0D);
    c01 /= (m_SumOfWeights - 1.0D);
    c10 = c01;
    c11 /= (m_SumOfWeights - 1.0D);
    m_Covariance = new Matrix(2, 2);
    m_Covariance.set(0, 0, c00);
    m_Covariance.set(0, 1, c01);
    m_Covariance.set(1, 0, c10);
    m_Covariance.set(1, 1, c11);
  }
  







  private double normalKernel(double x, double variance)
  {
    return Math.exp(-x * x / (2.0D * variance)) / Math.sqrt(variance * TWO_PI);
  }
  







  public void addValue(double data, double given, double weight)
  {
    int insertIndex = findNearestPair(given, data);
    if ((m_Values.size() <= insertIndex) || (((Double)m_CondValues.elementAt(insertIndex)).doubleValue() != given) || (((Double)m_Values.elementAt(insertIndex)).doubleValue() != data))
    {



      m_CondValues.insertElementAt(new Double(given), insertIndex);
      m_Values.insertElementAt(new Double(data), insertIndex);
      m_Weights.insertElementAt(new Double(weight), insertIndex);
      if (weight != 1.0D) {
        m_AllWeightsOne = false;
      }
    } else {
      double newWeight = ((Double)m_Weights.elementAt(insertIndex)).doubleValue();
      
      newWeight += weight;
      m_Weights.setElementAt(new Double(newWeight), insertIndex);
      m_AllWeightsOne = false;
    }
    m_SumOfWeights += weight;
    
    m_Covariance = null;
  }
  






  public Estimator getEstimator(double given)
  {
    if (m_Covariance == null) {
      calculateCovariance();
    }
    Estimator result = new MahalanobisEstimator(m_Covariance, given - m_CondMean, m_ValueMean);
    

    return result;
  }
  







  public double getProbability(double data, double given)
  {
    return getEstimator(given).getProbability(data);
  }
  

  public String toString()
  {
    if (m_Covariance == null) {
      calculateCovariance();
    }
    String result = "NN Conditional Estimator. " + m_CondValues.size() + " data points.  Mean = " + Utils.doubleToString(m_ValueMean, 4, 2) + "  Conditional mean = " + Utils.doubleToString(m_CondMean, 4, 2);
    


    result = result + "  Covariance Matrix: \n" + m_Covariance;
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
      NNConditionalEstimator newEst = new NNConditionalEstimator();
      

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
      } else
        cond = Math.abs(r.nextInt() % 100);
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
