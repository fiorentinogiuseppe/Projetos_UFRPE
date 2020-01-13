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
import weka.core.SingleIndex;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;



















































public class MakeIndicator
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 766001176862773163L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  private Range m_ValIndex;
  

  private boolean m_Numeric = true;
  



  public MakeIndicator()
  {
    m_ValIndex = new Range("last");
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
    m_ValIndex.setUpper(instanceInfo.attribute(m_AttIndex.getIndex()).numValues() - 1);
    
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
      if (m_ValIndex.isInRange((int)newInstance.value(m_AttIndex.getIndex()))) {
        newInstance.setValue(m_AttIndex.getIndex(), 1.0D);
      } else {
        newInstance.setValue(m_AttIndex.getIndex(), 0.0D);
      }
    }
    push(newInstance);
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSets the attribute index.", "C", 1, "-C <col>"));
    


    newVector.addElement(new Option("\tSpecify the list of values to indicate. First and last are\n\tvalid indexes (default last)", "V", 1, "-V <index1,index2-index4,...>"));
    


    newVector.addElement(new Option("\tSet if new boolean attribute nominal.", "N", 0, "-N <index>"));
    


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
    
    String valIndex = Utils.getOption('V', options);
    if (valIndex.length() != 0) {
      setValueIndices(valIndex);
    } else {
      setValueIndices("last");
    }
    
    setNumeric(!Utils.getFlag('N', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + getAttributeIndex());
    options[(current++)] = "-V";
    options[(current++)] = getValueIndices();
    if (!getNumeric()) {
      options[(current++)] = "-N";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String globalInfo()
  {
    return "A filter that creates a new dataset with a boolean attribute replacing a nominal attribute.  In the new dataset, a value of 1 is assigned to an instance that exhibits a particular range of attribute values, a 0 to an instance that doesn't. The boolean attribute is coded as numeric by default.";
  }
  








  public String attributeIndexTipText()
  {
    return "Sets which attribute should be replaced by the indicator. This attribute must be nominal.";
  }
  






  public String getAttributeIndex()
  {
    return m_AttIndex.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_AttIndex.setSingleIndex(attIndex);
  }
  





  public Range getValueRange()
  {
    return m_ValIndex;
  }
  




  public String valueIndicesTipText()
  {
    return "Specify range of nominal values to act on. This is a comma separated list of attribute indices (numbered from 1), with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getValueIndices()
  {
    return m_ValIndex.getRanges();
  }
  






  public void setValueIndices(String range)
  {
    m_ValIndex.setRanges(range);
  }
  





  public void setValueIndex(int index)
  {
    setValueIndices("" + (index + 1));
  }
  








  public void setValueIndicesArray(int[] indices)
  {
    setValueIndices(Range.indicesToRangeList(indices));
  }
  




  public String numericTipText()
  {
    return "Determines whether the output indicator attribute is numeric. If this is set to false, the output attribute will be nominal.";
  }
  






  public void setNumeric(boolean bool)
  {
    m_Numeric = bool;
  }
  





  public boolean getNumeric()
  {
    return m_Numeric;
  }
  








  private void setOutputFormat()
  {
    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if (j != m_AttIndex.getIndex())
      {


        newAtts.addElement(att);
      }
      else if (m_Numeric) {
        newAtts.addElement(new Attribute(att.name()));
      }
      else {
        int[] sel = m_ValIndex.getSelection();
        String vals; String vals; if (sel.length == 1) {
          vals = att.value(sel[0]);
        } else {
          vals = m_ValIndex.getRanges().replace(',', '_');
        }
        FastVector newVals = new FastVector(2);
        newVals.addElement("neg_" + vals);
        newVals.addElement("pos_" + vals);
        newAtts.addElement(new Attribute(att.name(), newVals));
      }
    }
    


    Instances newData = new Instances(getInputFormat().relationName(), newAtts, 0);
    newData.setClassIndex(getInputFormat().classIndex());
    setOutputFormat(newData);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new MakeIndicator(), argv);
  }
}
