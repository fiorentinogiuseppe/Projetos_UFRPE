package weka.classifiers.trees.m5;

import java.io.Serializable;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;















































public class PreConstructedLinearModel
  extends Classifier
  implements Serializable
{
  static final long serialVersionUID = 2030974097051713247L;
  private double[] m_coefficients;
  private double m_intercept;
  private Instances m_instancesHeader;
  private int m_numParameters;
  
  public PreConstructedLinearModel(double[] coeffs, double intercept)
  {
    m_coefficients = coeffs;
    m_intercept = intercept;
    int count = 0;
    for (int i = 0; i < coeffs.length; i++) {
      if (coeffs[i] != 0.0D) {
        count++;
      }
    }
    m_numParameters = count;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    m_instancesHeader = new Instances(instances, 0);
  }
  





  public double classifyInstance(Instance inst)
    throws Exception
  {
    double result = 0.0D;
    

    for (int i = 0; i < m_coefficients.length; i++) {
      if ((i != inst.classIndex()) && (!inst.isMissing(i)))
      {
        result += m_coefficients[i] * inst.value(i);
      }
    }
    
    result += m_intercept;
    return result;
  }
  




  public int numParameters()
  {
    return m_numParameters;
  }
  




  public double[] coefficients()
  {
    return m_coefficients;
  }
  




  public double intercept()
  {
    return m_intercept;
  }
  




  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("\n" + m_instancesHeader.classAttribute().name() + " = ");
    boolean first = true;
    for (int i = 0; i < m_coefficients.length; i++) {
      if (m_coefficients[i] != 0.0D) {
        double c = m_coefficients[i];
        if (first) {
          b.append("\n\t" + Utils.doubleToString(c, 12, 4).trim() + " * " + m_instancesHeader.attribute(i).name() + " ");
          
          first = false;
        } else {
          b.append("\n\t" + (m_coefficients[i] < 0.0D ? "- " + Utils.doubleToString(Math.abs(c), 12, 4).trim() : new StringBuilder().append("+ ").append(Utils.doubleToString(Math.abs(c), 12, 4).trim()).toString()) + " * " + m_instancesHeader.attribute(i).name() + " ");
        }
      }
    }
    



    b.append("\n\t" + (m_intercept < 0.0D ? "- " : "+ ") + Utils.doubleToString(Math.abs(m_intercept), 12, 4).trim());
    
    return b.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
