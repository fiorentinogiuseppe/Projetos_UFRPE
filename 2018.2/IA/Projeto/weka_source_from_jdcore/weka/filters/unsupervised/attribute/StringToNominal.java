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
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;


















































public class StringToNominal
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  private static final long serialVersionUID = 4864084427902797605L;
  private final Range m_AttIndices = new Range("last");
  


  public StringToNominal() {}
  


  public String globalInfo()
  {
    return "Converts a range of string attributes (unspecified number of values) to nominal (set number of values). You should ensure that all string values that will appear are represented in the first batch of the data.";
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
    m_AttIndices.setUpper(instanceInfo.numAttributes() - 1);
    return false;
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
    
    if (isOutputFormatDefined()) {
      Instance newInstance = (Instance)instance.copy();
      


      for (int i = 0; i < newInstance.numAttributes(); i++) {
        if ((newInstance.attribute(i).isString()) && (!newInstance.isMissing(i)) && (m_AttIndices.isInRange(i)))
        {
          Attribute outAtt = getOutputFormat().attribute(newInstance.attribute(i).name());
          
          String inVal = newInstance.stringValue(i);
          int outIndex = outAtt.indexOfValue(inVal);
          if (outIndex < 0) {
            newInstance.setMissing(i);
          } else {
            newInstance.setValue(i, outIndex);
          }
        }
      }
      push(newInstance);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (!isOutputFormatDefined())
    {
      setOutputFormat();
      

      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        Instance temp = (Instance)getInputFormat().instance(i).copy();
        for (int j = 0; j < temp.numAttributes(); j++) {
          if ((temp.attribute(j).isString()) && (!temp.isMissing(j)) && (m_AttIndices.isInRange(j)))
          {


            temp.setValue(j, temp.value(j) - 1.0D);
          }
        }
        push(temp);
      }
    }
    
    flushInput();
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  





  public Enumeration<Option> listOptions()
  {
    Vector<Option> newVector = new Vector(1);
    
    newVector.addElement(new Option("\tSets the range of attribute indices (default last).", "R", 1, "-R <col>"));
    


    newVector.addElement(new Option("\tInvert the range specified by -R.", "V", 1, "-V <col>"));
    

    return newVector.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String attIndices = Utils.getOption('R', options);
    if (attIndices.length() != 0) {
      setAttributeRange(attIndices);
    } else {
      setAttributeRange("last");
    }
    
    String invertSelection = Utils.getOption('V', options);
    if (invertSelection.length() != 0) {
      m_AttIndices.setInvert(true);
    } else {
      m_AttIndices.setInvert(false);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[m_AttIndices.getInvert() ? 7 : 6];
    int current = 0;
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + getAttributeRange());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    if (m_AttIndices.getInvert()) {
      options[(current++)] = "-V";
    }
    
    return options;
  }
  




  public String attributeRangeTipText()
  {
    return "Sets which attributes to process. This attributes must be string attributes (\"first\" and \"last\" are valid values as well as ranges and lists)";
  }
  







  public String getAttributeRange()
  {
    return m_AttIndices.getRanges();
  }
  





  public void setAttributeRange(String rangeList)
  {
    m_AttIndices.setRanges(rangeList);
  }
  









  private void setOutputFormat()
  {
    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!m_AttIndices.isInRange(j)) || (!att.isString()))
      {


        newAtts.addElement(att);
      }
      else
      {
        FastVector newVals = new FastVector(att.numValues());
        for (int i = 1; i < att.numValues(); i++) {
          newVals.addElement(att.value(i));
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
    return RevisionUtils.extract("$Revision: 9273 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new StringToNominal(), argv);
  }
}
