package weka.classifiers.lazy;

import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;








































































public class IB1
  extends Classifier
  implements UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = -6152184127304895851L;
  private Instances m_Train;
  private double[] m_MinArray;
  private double[] m_MaxArray;
  
  public IB1() {}
  
  public String globalInfo()
  {
    return "Nearest-neighbour classifier. Uses normalized Euclidean distance to find the training instance closest to the given test instance, and predicts the same class as this training instance. If multiple instances have the same (smallest) distance to the test instance, the first one found is used.\n\nFor more information, see \n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "D. Aha and D. Kibler");
    result.setValue(TechnicalInformation.Field.YEAR, "1991");
    result.setValue(TechnicalInformation.Field.TITLE, "Instance-based learning algorithms");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "6");
    result.setValue(TechnicalInformation.Field.PAGES, "37-66");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
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
    
    m_Train = new Instances(instances, 0, instances.numInstances());
    
    m_MinArray = new double[m_Train.numAttributes()];
    m_MaxArray = new double[m_Train.numAttributes()];
    for (int i = 0; i < m_Train.numAttributes(); i++) {
      double tmp90_87 = NaN.0D;m_MaxArray[i] = tmp90_87;m_MinArray[i] = tmp90_87;
    }
    Enumeration enu = m_Train.enumerateInstances();
    while (enu.hasMoreElements()) {
      updateMinMax((Instance)enu.nextElement());
    }
  }
  





  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (!m_Train.equalHeaders(instance.dataset())) {
      throw new Exception("Incompatible instance types");
    }
    if (instance.classIsMissing()) {
      return;
    }
    m_Train.add(instance);
    updateMinMax(instance);
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    if (m_Train.numInstances() == 0) {
      throw new Exception("No training instances!");
    }
    
    double minDistance = Double.MAX_VALUE;double classValue = 0.0D;
    updateMinMax(instance);
    Enumeration enu = m_Train.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance trainInstance = (Instance)enu.nextElement();
      if (!trainInstance.classIsMissing()) {
        double distance = distance(instance, trainInstance);
        if (distance < minDistance) {
          minDistance = distance;
          classValue = trainInstance.classValue();
        }
      }
    }
    
    return classValue;
  }
  





  public String toString()
  {
    return "IB1 classifier";
  }
  







  private double distance(Instance first, Instance second)
  {
    double distance = 0.0D;
    
    for (int i = 0; i < m_Train.numAttributes(); i++) {
      if (i != m_Train.classIndex())
      {

        if (m_Train.attribute(i).isNominal())
        {

          if ((first.isMissing(i)) || (second.isMissing(i)) || ((int)first.value(i) != (int)second.value(i)))
          {
            distance += 1.0D;
          }
        }
        else {
          double diff;
          if ((first.isMissing(i)) || (second.isMissing(i))) { double diff;
            if ((first.isMissing(i)) && (second.isMissing(i))) {
              diff = 1.0D; } else { double diff;
              double diff;
              if (second.isMissing(i)) {
                diff = norm(first.value(i), i);
              } else {
                diff = norm(second.value(i), i);
              }
              if (diff < 0.5D) {
                diff = 1.0D - diff;
              }
            }
          } else {
            diff = norm(first.value(i), i) - norm(second.value(i), i);
          }
          distance += diff * diff;
        }
      }
    }
    return distance;
  }
  







  private double norm(double x, int i)
  {
    if ((Double.isNaN(m_MinArray[i])) || (Utils.eq(m_MaxArray[i], m_MinArray[i])))
    {
      return 0.0D;
    }
    return (x - m_MinArray[i]) / (m_MaxArray[i] - m_MinArray[i]);
  }
  







  private void updateMinMax(Instance instance)
  {
    for (int j = 0; j < m_Train.numAttributes(); j++) {
      if ((m_Train.attribute(j).isNumeric()) && (!instance.isMissing(j))) {
        if (Double.isNaN(m_MinArray[j])) {
          m_MinArray[j] = instance.value(j);
          m_MaxArray[j] = instance.value(j);
        }
        else if (instance.value(j) < m_MinArray[j]) {
          m_MinArray[j] = instance.value(j);
        }
        else if (instance.value(j) > m_MaxArray[j]) {
          m_MaxArray[j] = instance.value(j);
        }
      }
    }
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5525 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new IB1(), argv);
  }
}
