package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;



















































public class MergeTwoValues
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 2925048980504034018L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  private SingleIndex m_FirstIndex = new SingleIndex("first");
  

  private SingleIndex m_SecondIndex = new SingleIndex("last");
  


  public MergeTwoValues() {}
  


  public String globalInfo()
  {
    return "Merges two values of a nominal attribute into one value.";
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    m_AttIndex.setUpper(instanceInfo.numAttributes() - 1);
    m_FirstIndex.setUpper(instanceInfo.attribute(m_AttIndex.getIndex()).numValues() - 1);
    
    m_SecondIndex.setUpper(instanceInfo.attribute(m_AttIndex.getIndex()).numValues() - 1);
    
    if ((instanceInfo.classIndex() > -1) && (instanceInfo.classIndex() == m_AttIndex.getIndex())) {
      throw new Exception("Cannot process class attribute.");
    }
    if (!instanceInfo.attribute(m_AttIndex.getIndex()).isNominal()) {
      throw new UnsupportedAttributeTypeException("Chosen attribute not nominal.");
    }
    if (instanceInfo.attribute(m_AttIndex.getIndex()).numValues() < 2) {
      throw new UnsupportedAttributeTypeException("Chosen attribute has less than two values.");
    }
    
    if (m_SecondIndex.getIndex() <= m_FirstIndex.getIndex())
    {
      throw new Exception("The second index has to be greater than the first.");
    }
    
    setOutputFormat();
    return true;
  }
  









  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    Instance newInstance = (Instance)instance.copy();
    if ((int)newInstance.value(m_AttIndex.getIndex()) == m_SecondIndex.getIndex()) {
      newInstance.setValue(m_AttIndex.getIndex(), m_FirstIndex.getIndex());
    }
    else if ((int)newInstance.value(m_AttIndex.getIndex()) > m_SecondIndex.getIndex()) {
      newInstance.setValue(m_AttIndex.getIndex(), newInstance.value(m_AttIndex.getIndex()) - 1.0D);
    }
    
    push(newInstance);
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSets the attribute index (default last).", "C", 1, "-C <col>"));
    


    newVector.addElement(new Option("\tSets the first value's index (default first).", "F", 1, "-F <value index>"));
    


    newVector.addElement(new Option("\tSets the second value's index (default last).", "S", 1, "-S <value index>"));
    


    return newVector.elements();
  }
  




















  public void setOptions(String[] options)
    throws Exception
  {
    String attIndex = Utils.getOption('C', options);
    if (attIndex.length() != 0) {
      setAttributeIndex(attIndex);
    } else {
      setAttributeIndex("last");
    }
    
    String firstValIndex = Utils.getOption('F', options);
    if (firstValIndex.length() != 0) {
      setFirstValueIndex(firstValIndex);
    } else {
      setFirstValueIndex("first");
    }
    
    String secondValIndex = Utils.getOption('S', options);
    if (secondValIndex.length() != 0) {
      setSecondValueIndex(secondValIndex);
    } else {
      setSecondValueIndex("last");
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + getAttributeIndex());
    options[(current++)] = "-F";
    options[(current++)] = ("" + getFirstValueIndex());
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSecondValueIndex());
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String attributeIndexTipText()
  {
    return "Sets which attribute to process. This attribute must be nominal (\"first\" and \"last\" are valid values)";
  }
  






  public String getAttributeIndex()
  {
    return m_AttIndex.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_AttIndex.setSingleIndex(attIndex);
  }
  




  public String firstValueIndexTipText()
  {
    return "Sets the first value to be merged. (\"first\" and \"last\" are valid values)";
  }
  






  public String getFirstValueIndex()
  {
    return m_FirstIndex.getSingleIndex();
  }
  





  public void setFirstValueIndex(String firstIndex)
  {
    m_FirstIndex.setSingleIndex(firstIndex);
  }
  




  public String secondValueIndexTipText()
  {
    return "Sets the second value to be merged. (\"first\" and \"last\" are valid values)";
  }
  






  public String getSecondValueIndex()
  {
    return m_SecondIndex.getSingleIndex();
  }
  





  public void setSecondValueIndex(String secondIndex)
  {
    m_SecondIndex.setSingleIndex(secondIndex);
  }
  







  private void setOutputFormat()
  {
    boolean firstEndsWithPrime = false;
    boolean secondEndsWithPrime = false;
    StringBuffer text = new StringBuffer();
    


    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if (j != m_AttIndex.getIndex()) {
        newAtts.addElement(att.copy());

      }
      else
      {
        if (att.value(m_FirstIndex.getIndex()).endsWith("'")) {
          firstEndsWithPrime = true;
        }
        if (att.value(m_SecondIndex.getIndex()).endsWith("'")) {
          secondEndsWithPrime = true;
        }
        if ((firstEndsWithPrime) || (secondEndsWithPrime)) {
          text.append("'");
        }
        if (firstEndsWithPrime) {
          text.append(att.value(m_FirstIndex.getIndex()).substring(1, att.value(m_FirstIndex.getIndex()).length() - 1));
        }
        else
        {
          text.append(att.value(m_FirstIndex.getIndex()));
        }
        text.append('_');
        if (secondEndsWithPrime) {
          text.append(att.value(m_SecondIndex.getIndex()).substring(1, att.value(m_SecondIndex.getIndex()).length() - 1));
        }
        else
        {
          text.append(att.value(m_SecondIndex.getIndex()));
        }
        if ((firstEndsWithPrime) || (secondEndsWithPrime)) {
          text.append("'");
        }
        


        FastVector newVals = new FastVector(att.numValues() - 1);
        for (int i = 0; i < att.numValues(); i++) {
          if (i == m_FirstIndex.getIndex()) {
            newVals.addElement(text.toString());
          } else if (i != m_SecondIndex.getIndex()) {
            newVals.addElement(att.value(i));
          }
        }
        
        Attribute newAtt = new Attribute(att.name(), newVals);
        newAtt.setWeight(getInputFormat().attribute(j).weight());
        newAtts.addElement(newAtt);
      }
    }
    


    Instances newData = new Instances(getInputFormat().relationName(), newAtts, 0);
    
    newData.setClassIndex(getInputFormat().classIndex());
    setOutputFormat(newData);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8289 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new MergeTwoValues(), argv);
  }
}
