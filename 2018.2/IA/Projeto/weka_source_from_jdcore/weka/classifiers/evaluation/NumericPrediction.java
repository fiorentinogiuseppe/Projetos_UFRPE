package weka.classifiers.evaluation;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;


































public class NumericPrediction
  implements Prediction, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -4880216423674233887L;
  private double m_Actual = MISSING_VALUE;
  

  private double m_Predicted = MISSING_VALUE;
  

  private double m_Weight = 1.0D;
  






  public NumericPrediction(double actual, double predicted)
  {
    this(actual, predicted, 1.0D);
  }
  







  public NumericPrediction(double actual, double predicted, double weight)
  {
    m_Actual = actual;
    m_Predicted = predicted;
    m_Weight = weight;
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
  








  public double error()
  {
    if ((m_Actual == MISSING_VALUE) || (m_Predicted == MISSING_VALUE))
    {
      return MISSING_VALUE;
    }
    return m_Predicted - m_Actual;
  }
  





  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("NUM: ").append(actual()).append(' ').append(predicted());
    sb.append(' ').append(weight());
    return sb.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}
