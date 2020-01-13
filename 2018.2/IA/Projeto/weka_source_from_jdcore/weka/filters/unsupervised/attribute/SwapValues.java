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




















































public class SwapValues
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 6155834679414275855L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  private SingleIndex m_FirstIndex = new SingleIndex("first");
  

  private SingleIndex m_SecondIndex = new SingleIndex("last");
  


  public SwapValues() {}
  


  public String globalInfo()
  {
    return "Swaps two values of a nominal attribute.";
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
    
    if (!instanceInfo.attribute(m_AttIndex.getIndex()).isNominal()) {
      throw new UnsupportedAttributeTypeException("Chosen attribute not nominal.");
    }
    if (instanceInfo.attribute(m_AttIndex.getIndex()).numValues() < 2) {
      throw new UnsupportedAttributeTypeException("Chosen attribute has less than two values.");
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
    if (!newInstance.isMissing(m_AttIndex.getIndex())) {
      if ((int)newInstance.value(m_AttIndex.getIndex()) == m_SecondIndex.getIndex()) {
        newInstance.setValue(m_AttIndex.getIndex(), m_FirstIndex.getIndex());
      } else if ((int)newInstance.value(m_AttIndex.getIndex()) == m_FirstIndex.getIndex())
      {
        newInstance.setValue(m_AttIndex.getIndex(), m_SecondIndex.getIndex());
      }
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
    return "The index of the first value.(\"first\" and \"last\" are valid values)";
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
    return "The index of the second value.(\"first\" and \"last\" are valid values)";
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
    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if (j != m_AttIndex.getIndex()) {
        newAtts.addElement(att.copy());

      }
      else
      {
        FastVector newVals = new FastVector(att.numValues());
        for (int i = 0; i < att.numValues(); i++) {
          if (i == m_FirstIndex.getIndex()) {
            newVals.addElement(att.value(m_SecondIndex.getIndex()));
          } else if (i == m_SecondIndex.getIndex()) {
            newVals.addElement(att.value(m_FirstIndex.getIndex()));
          } else {
            newVals.addElement(att.value(i));
          }
        }
        Attribute newAtt = new Attribute(att.name(), newVals);
        newAtt.setWeight(att.weight());
        newAtts.addElement(newAtt);
      }
    }
    


    Instances newData = new Instances(getInputFormat().relationName(), newAtts, 0);
    newData.setClassIndex(getInputFormat().classIndex());
    setOutputFormat(newData);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8585 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new SwapValues(), argv);
  }
}
