package weka.classifiers.rules;

import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;



















































public class ZeroR
  extends Classifier
  implements WeightedInstancesHandler, Sourcable
{
  static final long serialVersionUID = 48055541465867954L;
  private double m_ClassValue;
  private double[] m_Counts;
  private Attribute m_Class;
  
  public ZeroR() {}
  
  public String globalInfo()
  {
    return "Class for building and using a 0-R classifier. Predicts the mean (for a numeric class) or the mode (for a nominal class).";
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    double sumOfWeights = 0.0D;
    
    m_Class = instances.classAttribute();
    m_ClassValue = 0.0D;
    switch (instances.classAttribute().type()) {
    case 0: 
      m_Counts = null;
      break;
    case 1: 
      m_Counts = new double[instances.numClasses()];
      for (int i = 0; i < m_Counts.length; i++) {
        m_Counts[i] = 1.0D;
      }
      sumOfWeights = instances.numClasses();
    }
    
    Enumeration enu = instances.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      if (!instance.classIsMissing()) {
        if (instances.classAttribute().isNominal()) {
          m_Counts[((int)instance.classValue())] += instance.weight();
        } else {
          m_ClassValue += instance.weight() * instance.classValue();
        }
        sumOfWeights += instance.weight();
      }
    }
    if (instances.classAttribute().isNumeric()) {
      if (Utils.gr(sumOfWeights, 0.0D)) {
        m_ClassValue /= sumOfWeights;
      }
    } else {
      m_ClassValue = Utils.maxIndex(m_Counts);
      Utils.normalize(m_Counts, sumOfWeights);
    }
  }
  






  public double classifyInstance(Instance instance)
  {
    return m_ClassValue;
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_Counts == null) {
      double[] result = new double[1];
      result[0] = m_ClassValue;
      return result;
    }
    return (double[])m_Counts.clone();
  }
  

















  public String toSource(String className)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    
    result.append("class " + className + " {\n");
    result.append("  public static double classify(Object[] i) {\n");
    if (m_Counts != null)
      result.append("    // always predicts label '" + m_Class.value((int)m_ClassValue) + "'\n");
    result.append("    return " + m_ClassValue + ";\n");
    result.append("  }\n");
    result.append("}\n");
    
    return result.toString();
  }
  





  public String toString()
  {
    if (m_Class == null) {
      return "ZeroR: No model built yet.";
    }
    if (m_Counts == null) {
      return "ZeroR predicts class value: " + m_ClassValue;
    }
    return "ZeroR predicts class value: " + m_Class.value((int)m_ClassValue);
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5529 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new ZeroR(), argv);
  }
}
