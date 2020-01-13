package weka.classifiers.trees;

import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NoSupportForMissingValuesException;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;









































































public class Id3
  extends Classifier
  implements TechnicalInformationHandler, Sourcable
{
  static final long serialVersionUID = -2693678647096322561L;
  private Id3[] m_Successors;
  private Attribute m_Attribute;
  private double m_ClassValue;
  private double[] m_Distribution;
  private Attribute m_ClassAttribute;
  
  public Id3() {}
  
  public String globalInfo()
  {
    return "Class for constructing an unpruned decision tree based on the ID3 algorithm. Can only deal with nominal attributes. No missing values allowed. Empty leaves may result in unclassified instances. For more information see: \n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R. Quinlan");
    result.setValue(TechnicalInformation.Field.YEAR, "1986");
    result.setValue(TechnicalInformation.Field.TITLE, "Induction of decision trees");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "1");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "81-106");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    makeTree(data);
  }
  






  private void makeTree(Instances data)
    throws Exception
  {
    if (data.numInstances() == 0) {
      m_Attribute = null;
      m_ClassValue = Instance.missingValue();
      m_Distribution = new double[data.numClasses()];
      return;
    }
    

    double[] infoGains = new double[data.numAttributes()];
    Enumeration attEnum = data.enumerateAttributes();
    while (attEnum.hasMoreElements()) {
      Attribute att = (Attribute)attEnum.nextElement();
      infoGains[att.index()] = computeInfoGain(data, att);
    }
    m_Attribute = data.attribute(Utils.maxIndex(infoGains));
    


    if (Utils.eq(infoGains[m_Attribute.index()], 0.0D)) {
      m_Attribute = null;
      m_Distribution = new double[data.numClasses()];
      Enumeration instEnum = data.enumerateInstances();
      while (instEnum.hasMoreElements()) {
        Instance inst = (Instance)instEnum.nextElement();
        m_Distribution[((int)inst.classValue())] += 1.0D;
      }
      Utils.normalize(m_Distribution);
      m_ClassValue = Utils.maxIndex(m_Distribution);
      m_ClassAttribute = data.classAttribute();
    } else {
      Instances[] splitData = splitData(data, m_Attribute);
      m_Successors = new Id3[m_Attribute.numValues()];
      for (int j = 0; j < m_Attribute.numValues(); j++) {
        m_Successors[j] = new Id3();
        m_Successors[j].makeTree(splitData[j]);
      }
    }
  }
  







  public double classifyInstance(Instance instance)
    throws NoSupportForMissingValuesException
  {
    if (instance.hasMissingValue()) {
      throw new NoSupportForMissingValuesException("Id3: no missing values, please.");
    }
    
    if (m_Attribute == null) {
      return m_ClassValue;
    }
    return m_Successors[((int)instance.value(m_Attribute))].classifyInstance(instance);
  }
  









  public double[] distributionForInstance(Instance instance)
    throws NoSupportForMissingValuesException
  {
    if (instance.hasMissingValue()) {
      throw new NoSupportForMissingValuesException("Id3: no missing values, please.");
    }
    
    if (m_Attribute == null) {
      return m_Distribution;
    }
    return m_Successors[((int)instance.value(m_Attribute))].distributionForInstance(instance);
  }
  







  public String toString()
  {
    if ((m_Distribution == null) && (m_Successors == null)) {
      return "Id3: No model built yet.";
    }
    return "Id3\n\n" + toString(0);
  }
  








  private double computeInfoGain(Instances data, Attribute att)
    throws Exception
  {
    double infoGain = computeEntropy(data);
    Instances[] splitData = splitData(data, att);
    for (int j = 0; j < att.numValues(); j++) {
      if (splitData[j].numInstances() > 0) {
        infoGain -= splitData[j].numInstances() / data.numInstances() * computeEntropy(splitData[j]);
      }
    }
    

    return infoGain;
  }
  






  private double computeEntropy(Instances data)
    throws Exception
  {
    double[] classCounts = new double[data.numClasses()];
    Enumeration instEnum = data.enumerateInstances();
    while (instEnum.hasMoreElements()) {
      Instance inst = (Instance)instEnum.nextElement();
      classCounts[((int)inst.classValue())] += 1.0D;
    }
    double entropy = 0.0D;
    for (int j = 0; j < data.numClasses(); j++) {
      if (classCounts[j] > 0.0D) {
        entropy -= classCounts[j] * Utils.log2(classCounts[j]);
      }
    }
    entropy /= data.numInstances();
    return entropy + Utils.log2(data.numInstances());
  }
  







  private Instances[] splitData(Instances data, Attribute att)
  {
    Instances[] splitData = new Instances[att.numValues()];
    for (int j = 0; j < att.numValues(); j++) {
      splitData[j] = new Instances(data, data.numInstances());
    }
    Enumeration instEnum = data.enumerateInstances();
    while (instEnum.hasMoreElements()) {
      Instance inst = (Instance)instEnum.nextElement();
      splitData[((int)inst.value(att))].add(inst);
    }
    for (int i = 0; i < splitData.length; i++) {
      splitData[i].compactify();
    }
    return splitData;
  }
  






  private String toString(int level)
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Attribute == null) {
      if (Instance.isMissingValue(m_ClassValue)) {
        text.append(": null");
      } else {
        text.append(": " + m_ClassAttribute.value((int)m_ClassValue));
      }
    } else {
      for (int j = 0; j < m_Attribute.numValues(); j++) {
        text.append("\n");
        for (int i = 0; i < level; i++) {
          text.append("|  ");
        }
        text.append(m_Attribute.name() + " = " + m_Attribute.value(j));
        text.append(m_Successors[j].toString(level + 1));
      }
    }
    return text.toString();
  }
  











  protected int toSource(int id, StringBuffer buffer)
    throws Exception
  {
    buffer.append("\n");
    buffer.append("  protected static double node" + id + "(Object[] i) {\n");
    
    int result;
    if (m_Attribute == null) {
      int result = id;
      if (Double.isNaN(m_ClassValue)) {
        buffer.append("    return Double.NaN;");
      } else {
        buffer.append("    return " + m_ClassValue + ";");
      }
      if (m_ClassAttribute != null) {
        buffer.append(" // " + m_ClassAttribute.value((int)m_ClassValue));
      }
      buffer.append("\n");
      buffer.append("  }\n");
    } else {
      buffer.append("    checkMissing(i, " + m_Attribute.index() + ");\n\n");
      buffer.append("    // " + m_Attribute.name() + "\n");
      

      StringBuffer[] subBuffers = new StringBuffer[m_Attribute.numValues()];
      int newID = id;
      for (int i = 0; i < m_Attribute.numValues(); i++) {
        newID++;
        
        buffer.append("    ");
        if (i > 0) {
          buffer.append("else ");
        }
        buffer.append("if (((String) i[" + m_Attribute.index() + "]).equals(\"" + m_Attribute.value(i) + "\"))\n");
        
        buffer.append("      return node" + newID + "(i);\n");
        
        subBuffers[i] = new StringBuffer();
        newID = m_Successors[i].toSource(newID, subBuffers[i]);
      }
      buffer.append("    else\n");
      buffer.append("      throw new IllegalArgumentException(\"Value '\" + i[" + m_Attribute.index() + "] + \"' is not allowed!\");\n");
      
      buffer.append("  }\n");
      

      for (i = 0; i < m_Attribute.numValues(); i++) {
        buffer.append(subBuffers[i].toString());
      }
      subBuffers = null;
      
      result = newID;
    }
    
    return result;
  }
  


















  public String toSource(String className)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    
    result.append("class " + className + " {\n");
    result.append("  private static void checkMissing(Object[] i, int index) {\n");
    result.append("    if (i[index] == null)\n");
    result.append("      throw new IllegalArgumentException(\"Null values are not allowed!\");\n");
    
    result.append("  }\n\n");
    result.append("  public static double classify(Object[] i) {\n");
    int id = 0;
    result.append("    return node" + id + "(i);\n");
    result.append("  }\n");
    toSource(id, result);
    result.append("}\n");
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6404 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new Id3(), args);
  }
}
