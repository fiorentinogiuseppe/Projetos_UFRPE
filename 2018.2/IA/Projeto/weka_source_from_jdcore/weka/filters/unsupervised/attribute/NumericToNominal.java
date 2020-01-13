package weka.filters.unsupervised.attribute;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;
























































public class NumericToNominal
  extends SimpleBatchFilter
{
  private static final long serialVersionUID = -6614630932899796239L;
  protected static final int MAX_DECIMALS = 6;
  protected Range m_Cols = new Range("first-last");
  

  protected String m_DefaultCols = "first-last";
  


  public NumericToNominal() {}
  


  public String globalInfo()
  {
    return "A filter for turning numeric attributes into nominal ones. Unlike discretization, it just takes all numeric values and adds them to the list of nominal values of that attribute. Useful after CSV imports, to enforce certain attributes to become nominal, e.g., the class attribute, containing values from 1 to 5.";
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSpecifies list of columns to Discretize. First and last are valid indexes.\n\t(default: first-last)", "R", 1, "-R <col1,col2-col4,...>"));
    




    result.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    return result.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setInvertSelection(Utils.getFlag('V', options));
    
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setAttributeIndices(tmpStr);
    } else {
      setAttributeIndices(m_DefaultCols);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    
    if (!getAttributeIndices().equals("")) {
      result.add("-R");
      result.add(getAttributeIndices());
    }
    
    if (getInvertSelection()) {
      result.add("-V");
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected (numeric) attributes in the range will be 'nominalized'; if true, only non-selected attributes will be 'nominalized'.";
  }
  







  public boolean getInvertSelection()
  {
    return m_Cols.getInvert();
  }
  






  public void setInvertSelection(boolean value)
  {
    m_Cols.setInvert(value);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  







  public String getAttributeIndices()
  {
    return m_Cols.getRanges();
  }
  








  public void setAttributeIndices(String value)
  {
    m_Cols.setRanges(value);
  }
  








  public void setAttributeIndicesArray(int[] value)
  {
    setAttributeIndices(Range.indicesToRangeList(value));
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
  
























  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    m_Cols.setUpper(inputFormat.numAttributes() - 1);
    Instances data = new Instances(inputFormat);
    FastVector atts = new FastVector();
    for (int i = 0; i < data.numAttributes(); i++) {
      if ((!m_Cols.isInRange(i)) || (!data.attribute(i).isNumeric())) {
        atts.addElement(data.attribute(i));

      }
      else
      {
        boolean isDate = data.attribute(i).type() == 3;
        

        HashSet hash = new HashSet();
        for (int n = 0; n < data.numInstances(); n++) {
          Instance inst = data.instance(n);
          if (!inst.isMissing(i))
          {


            if (isDate) {
              hash.add(inst.stringValue(i));
            } else {
              hash.add(new Double(inst.value(i)));
            }
          }
        }
        
        Vector sorted = new Vector();
        for (Object o : hash) {
          sorted.add(o);
        }
        Collections.sort(sorted);
        

        FastVector values = new FastVector();
        for (Object o : sorted) {
          if (isDate) {
            values.addElement(o.toString());
          }
          else {
            values.addElement(Utils.doubleToString(((Double)o).doubleValue(), 6));
          }
        }
        
        Attribute newAtt = new Attribute(data.attribute(i).name(), values);
        newAtt.setWeight(data.attribute(i).weight());
        atts.addElement(newAtt);
      }
    }
    Instances result = new Instances(inputFormat.relationName(), atts, 0);
    result.setClassIndex(inputFormat.classIndex());
    
    return result;
  }
  

















  protected Instances process(Instances instances)
    throws Exception
  {
    if (!isFirstBatchDone()) {
      setOutputFormat(determineOutputFormat(getInputFormat()));
    }
    
    Instances result = new Instances(getOutputFormat());
    
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance inst = instances.instance(i);
      double[] values = inst.toDoubleArray();
      
      for (int n = 0; n < values.length; n++) {
        if ((m_Cols.isInRange(n)) && (instances.attribute(n).isNumeric()) && (!inst.isMissing(n)))
        {
          String value;
          

          String value;
          
          if (instances.attribute(n).type() == 3) {
            value = inst.stringValue(n);
          } else {
            value = Utils.doubleToString(inst.value(n), 6);
          }
          
          int index = result.attribute(n).indexOfValue(value);
          if (index == -1) {
            values[n] = Instance.missingValue();
          } else
            values[n] = index;
        }
      }
      Instance newInst;
      Instance newInst;
      if ((inst instanceof SparseInstance)) {
        newInst = new SparseInstance(inst.weight(), values);
      } else {
        newInst = new Instance(inst.weight(), values);
      }
      

      newInst.setDataset(getOutputFormat());
      copyValues(newInst, false, inst.dataset(), getOutputFormat());
      
      result.add(newInst);
    }
    
    return result;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10988 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new NumericToNominal(), args);
  }
}
