package weka.classifiers.functions;

import java.io.PrintStream;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;


















































public class SimpleLinearRegression
  extends Classifier
  implements WeightedInstancesHandler
{
  static final long serialVersionUID = 1679336022895414137L;
  private Attribute m_attribute;
  private int m_attributeIndex;
  private double m_slope;
  private double m_intercept;
  private boolean m_suppressErrorMessage = false;
  

  public SimpleLinearRegression() {}
  

  public String globalInfo()
  {
    return "Learns a simple linear regression model. Picks the attribute that results in the lowest squared error. Missing values are not allowed. Can only deal with numeric attributes.";
  }
  








  public double classifyInstance(Instance inst)
    throws Exception
  {
    if (m_attribute == null) {
      return m_intercept;
    }
    if (inst.isMissing(m_attribute.index())) {
      throw new Exception("SimpleLinearRegression: No missing values!");
    }
    return m_intercept + m_slope * inst.value(m_attribute.index());
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    

    double yMean = insts.meanOrMode(insts.classIndex());
    

    double minMsq = Double.MAX_VALUE;
    m_attribute = null;
    int chosen = -1;
    double chosenSlope = NaN.0D;
    double chosenIntercept = NaN.0D;
    for (int i = 0; i < insts.numAttributes(); i++) {
      if (i != insts.classIndex()) {
        m_attribute = insts.attribute(i);
        

        double xMean = insts.meanOrMode(i);
        double sumWeightedXDiffSquared = 0.0D;
        double sumWeightedYDiffSquared = 0.0D;
        m_slope = 0.0D;
        for (int j = 0; j < insts.numInstances(); j++) {
          Instance inst = insts.instance(j);
          if ((!inst.isMissing(i)) && (!inst.classIsMissing())) {
            double xDiff = inst.value(i) - xMean;
            double yDiff = inst.classValue() - yMean;
            double weightedXDiff = inst.weight() * xDiff;
            double weightedYDiff = inst.weight() * yDiff;
            m_slope += weightedXDiff * yDiff;
            sumWeightedXDiffSquared += weightedXDiff * xDiff;
            sumWeightedYDiffSquared += weightedYDiff * yDiff;
          }
        }
        

        if (sumWeightedXDiffSquared != 0.0D)
        {

          double numerator = m_slope;
          m_slope /= sumWeightedXDiffSquared;
          m_intercept = (yMean - m_slope * xMean);
          

          double msq = sumWeightedYDiffSquared - m_slope * numerator;
          

          if (msq < minMsq) {
            minMsq = msq;
            chosen = i;
            chosenSlope = m_slope;
            chosenIntercept = m_intercept;
          }
        }
      }
    }
    
    if (chosen == -1) {
      if (!m_suppressErrorMessage) System.err.println("----- no useful attribute found");
      m_attribute = null;
      m_attributeIndex = 0;
      m_slope = 0.0D;
      m_intercept = yMean;
    } else {
      m_attribute = insts.attribute(chosen);
      m_attributeIndex = chosen;
      m_slope = chosenSlope;
      m_intercept = chosenIntercept;
    }
  }
  




  public boolean foundUsefulAttribute()
  {
    return m_attribute != null;
  }
  




  public int getAttributeIndex()
  {
    return m_attributeIndex;
  }
  




  public double getSlope()
  {
    return m_slope;
  }
  




  public double getIntercept()
  {
    return m_intercept;
  }
  




  public void setSuppressErrorMessage(boolean s)
  {
    m_suppressErrorMessage = s;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    if (m_attribute == null) {
      text.append("Predicting constant " + m_intercept);
    } else {
      text.append("Linear regression on " + m_attribute.name() + "\n\n");
      text.append(Utils.doubleToString(m_slope, 2) + " * " + m_attribute.name());
      
      if (m_intercept > 0.0D) {
        text.append(" + " + Utils.doubleToString(m_intercept, 2));
      } else {
        text.append(" - " + Utils.doubleToString(-m_intercept, 2));
      }
    }
    text.append("\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new SimpleLinearRegression(), argv);
  }
}
