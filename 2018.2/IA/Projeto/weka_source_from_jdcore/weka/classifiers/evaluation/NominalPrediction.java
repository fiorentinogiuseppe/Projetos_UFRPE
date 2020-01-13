package weka.classifiers.evaluation;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;







































public class NominalPrediction
  implements Prediction, Serializable, RevisionHandler
{
  static final long serialVersionUID = -8871333992740492788L;
  private double[] m_Distribution;
  private double m_Actual = MISSING_VALUE;
  

  private double m_Predicted = MISSING_VALUE;
  

  private double m_Weight = 1.0D;
  







  public NominalPrediction(double actual, double[] distribution)
  {
    this(actual, distribution, 1.0D);
  }
  









  public NominalPrediction(double actual, double[] distribution, double weight)
  {
    if (distribution == null) {
      throw new NullPointerException("Null distribution in NominalPrediction.");
    }
    m_Actual = actual;
    m_Distribution = ((double[])distribution.clone());
    m_Weight = weight;
    updatePredicted();
  }
  





  public double[] distribution()
  {
    return m_Distribution;
  }
  






  public double actual()
  {
    return m_Actual;
  }
  






  public double predicted()
  {
    return m_Predicted;
  }
  






  public double weight()
  {
    return m_Weight;
  }
  









  public double margin()
  {
    if ((m_Actual == MISSING_VALUE) || (m_Predicted == MISSING_VALUE))
    {
      return MISSING_VALUE;
    }
    double probActual = m_Distribution[((int)m_Actual)];
    double probNext = 0.0D;
    for (int i = 0; i < m_Distribution.length; i++) {
      if ((i != m_Actual) && (m_Distribution[i] > probNext))
      {
        probNext = m_Distribution[i]; }
    }
    return probActual - probNext;
  }
  













  public static double[] makeDistribution(double predictedClass, int numClasses)
  {
    double[] dist = new double[numClasses];
    if (predictedClass == MISSING_VALUE) {
      return dist;
    }
    dist[((int)predictedClass)] = 1.0D;
    return dist;
  }
  








  public static double[] makeUniformDistribution(int numClasses)
  {
    double[] dist = new double[numClasses];
    for (int i = 0; i < numClasses; i++) {
      dist[i] = (1.0D / numClasses);
    }
    return dist;
  }
  






  private void updatePredicted()
  {
    int predictedClass = -1;
    double bestProb = 0.0D;
    for (int i = 0; i < m_Distribution.length; i++) {
      if (m_Distribution[i] > bestProb) {
        predictedClass = i;
        bestProb = m_Distribution[i];
      }
    }
    
    if (predictedClass != -1) {
      m_Predicted = predictedClass;
    } else {
      m_Predicted = MISSING_VALUE;
    }
  }
  





  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("NOM: ").append(actual()).append(" ").append(predicted());
    sb.append(' ').append(weight());
    double[] dist = distribution();
    for (int i = 0; i < dist.length; i++) {
      sb.append(' ').append(dist[i]);
    }
    return sb.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
}
