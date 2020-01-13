package weka.classifiers.bayes;

import java.util.Enumeration;
import weka.classifiers.Classifier;
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









































































public class NaiveBayesSimple
  extends Classifier
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -1478242251770381214L;
  protected double[][][] m_Counts;
  protected double[][] m_Means;
  protected double[][] m_Devs;
  protected double[] m_Priors;
  protected Instances m_Instances;
  protected static double NORM_CONST = Math.sqrt(6.283185307179586D);
  

  public NaiveBayesSimple() {}
  

  public String globalInfo()
  {
    return "Class for building and using a simple Naive Bayes classifier.Numeric attributes are modelled by a normal distribution.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Richard Duda and Peter Hart");
    result.setValue(TechnicalInformation.Field.YEAR, "1973");
    result.setValue(TechnicalInformation.Field.TITLE, "Pattern Classification and Scene Analysis");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Wiley");
    result.setValue(TechnicalInformation.Field.ADDRESS, "New York");
    
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
    
    return result;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    int attIndex = 0;
    


    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_Instances = new Instances(instances, 0);
    

    m_Counts = new double[instances.numClasses()][instances.numAttributes() - 1][0];
    
    m_Means = new double[instances.numClasses()][instances.numAttributes() - 1];
    
    m_Devs = new double[instances.numClasses()][instances.numAttributes() - 1];
    
    m_Priors = new double[instances.numClasses()];
    Enumeration enu = instances.enumerateAttributes();
    while (enu.hasMoreElements()) {
      Attribute attribute = (Attribute)enu.nextElement();
      if (attribute.isNominal()) {
        for (int j = 0; j < instances.numClasses(); j++) {
          m_Counts[j][attIndex] = new double[attribute.numValues()];
        }
      } else {
        for (int j = 0; j < instances.numClasses(); j++) {
          m_Counts[j][attIndex] = new double[1];
        }
      }
      attIndex++;
    }
    

    Enumeration enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      if (!instance.classIsMissing()) {
        Enumeration enumAtts = instances.enumerateAttributes();
        attIndex = 0;
        while (enumAtts.hasMoreElements()) {
          Attribute attribute = (Attribute)enumAtts.nextElement();
          if (!instance.isMissing(attribute)) {
            if (attribute.isNominal()) {
              m_Counts[((int)instance.classValue())][attIndex][((int)instance.value(attribute))] += 1.0D;
            }
            else {
              m_Means[((int)instance.classValue())][attIndex] += instance.value(attribute);
              
              m_Counts[((int)instance.classValue())][attIndex][0] += 1.0D;
            }
          }
          attIndex++;
        }
        m_Priors[((int)instance.classValue())] += 1.0D;
      }
    }
    

    Enumeration enumAtts = instances.enumerateAttributes();
    attIndex = 0;
    while (enumAtts.hasMoreElements()) {
      Attribute attribute = (Attribute)enumAtts.nextElement();
      if (attribute.isNumeric()) {
        for (int j = 0; j < instances.numClasses(); j++) {
          if (m_Counts[j][attIndex][0] < 2.0D) {
            throw new Exception("attribute " + attribute.name() + ": less than two values for class " + instances.classAttribute().value(j));
          }
          

          m_Means[j][attIndex] /= m_Counts[j][attIndex][0];
        }
      }
      attIndex++;
    }
    

    enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      
      if (!instance.classIsMissing()) {
        enumAtts = instances.enumerateAttributes();
        attIndex = 0;
        while (enumAtts.hasMoreElements()) {
          Attribute attribute = (Attribute)enumAtts.nextElement();
          if ((!instance.isMissing(attribute)) && 
            (attribute.isNumeric())) {
            m_Devs[((int)instance.classValue())][attIndex] += (m_Means[((int)instance.classValue())][attIndex] - instance.value(attribute)) * (m_Means[((int)instance.classValue())][attIndex] - instance.value(attribute));
          }
          




          attIndex++;
        }
      }
    }
    enumAtts = instances.enumerateAttributes();
    attIndex = 0;
    while (enumAtts.hasMoreElements()) {
      Attribute attribute = (Attribute)enumAtts.nextElement();
      if (attribute.isNumeric()) {
        for (int j = 0; j < instances.numClasses(); j++) {
          if (m_Devs[j][attIndex] <= 0.0D) {
            throw new Exception("attribute " + attribute.name() + ": standard deviation is 0 for class " + instances.classAttribute().value(j));
          }
          


          m_Devs[j][attIndex] /= (m_Counts[j][attIndex][0] - 1.0D);
          m_Devs[j][attIndex] = Math.sqrt(m_Devs[j][attIndex]);
        }
      }
      
      attIndex++;
    }
    

    enumAtts = instances.enumerateAttributes();
    attIndex = 0;
    while (enumAtts.hasMoreElements()) {
      Attribute attribute = (Attribute)enumAtts.nextElement();
      if (attribute.isNominal()) {
        for (int j = 0; j < instances.numClasses(); j++) {
          double sum = Utils.sum(m_Counts[j][attIndex]);
          for (int i = 0; i < attribute.numValues(); i++) {
            m_Counts[j][attIndex][i] = ((m_Counts[j][attIndex][i] + 1.0D) / (sum + attribute.numValues()));
          }
        }
      }
      

      attIndex++;
    }
    

    double sum = Utils.sum(m_Priors);
    for (int j = 0; j < instances.numClasses(); j++) {
      m_Priors[j] = ((m_Priors[j] + 1.0D) / (sum + instances.numClasses()));
    }
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs = new double[instance.numClasses()];
    

    for (int j = 0; j < instance.numClasses(); j++) {
      probs[j] = 1.0D;
      Enumeration enumAtts = instance.enumerateAttributes();
      int attIndex = 0;
      while (enumAtts.hasMoreElements()) {
        Attribute attribute = (Attribute)enumAtts.nextElement();
        if (!instance.isMissing(attribute)) {
          if (attribute.isNominal()) {
            probs[j] *= m_Counts[j][attIndex][((int)instance.value(attribute))];
          } else {
            probs[j] *= normalDens(instance.value(attribute), m_Means[j][attIndex], m_Devs[j][attIndex]);
          }
        }
        
        attIndex++;
      }
      probs[j] *= m_Priors[j];
    }
    

    Utils.normalize(probs);
    
    return probs;
  }
  





  public String toString()
  {
    if (m_Instances == null) {
      return "Naive Bayes (simple): No model built yet.";
    }
    try {
      StringBuffer text = new StringBuffer("Naive Bayes (simple)");
      

      for (int i = 0; i < m_Instances.numClasses(); i++) {
        text.append("\n\nClass " + m_Instances.classAttribute().value(i) + ": P(C) = " + Utils.doubleToString(m_Priors[i], 10, 8) + "\n\n");
        


        Enumeration enumAtts = m_Instances.enumerateAttributes();
        int attIndex = 0;
        while (enumAtts.hasMoreElements()) {
          Attribute attribute = (Attribute)enumAtts.nextElement();
          text.append("Attribute " + attribute.name() + "\n");
          if (attribute.isNominal()) {
            for (int j = 0; j < attribute.numValues(); j++) {
              text.append(attribute.value(j) + "\t");
            }
            text.append("\n");
            for (int j = 0; j < attribute.numValues(); j++) {
              text.append(Utils.doubleToString(m_Counts[i][attIndex][j], 10, 8) + "\t");
            }
          }
          else {
            text.append("Mean: " + Utils.doubleToString(m_Means[i][attIndex], 10, 8) + "\t");
            
            text.append("Standard Deviation: " + Utils.doubleToString(m_Devs[i][attIndex], 10, 8));
          }
          
          text.append("\n\n");
          attIndex++;
        }
      }
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print Naive Bayes classifier!";
  }
  









  protected double normalDens(double x, double mean, double stdDev)
  {
    double diff = x - mean;
    
    return 1.0D / (NORM_CONST * stdDev) * Math.exp(-(diff * diff / (2.0D * stdDev * stdDev)));
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new NaiveBayesSimple(), argv);
  }
}
