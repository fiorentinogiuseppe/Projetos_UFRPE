package weka.filters.unsupervised.attribute;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;
























































public class NumericTransform
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -8561413333351366934L;
  private Range m_Cols = new Range();
  



  private String m_Class;
  


  private String m_Method;
  



  public String globalInfo()
  {
    return "Transforms numeric attributes using a given transformation method.";
  }
  




  public NumericTransform()
  {
    m_Class = "java.lang.Math";
    m_Method = "abs";
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
    if (m_Class == null) {
      throw new IllegalStateException("No class has been set.");
    }
    if (m_Method == null) {
      throw new IllegalStateException("No method has been set.");
    }
    super.setInputFormat(instanceInfo);
    m_Cols.setUpper(instanceInfo.numAttributes() - 1);
    setOutputFormat(instanceInfo);
    return true;
  }
  










  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    Method m = Class.forName(m_Class).getMethod(m_Method, new Class[] { Double.TYPE });
    
    double[] vals = new double[instance.numAttributes()];
    Double[] params = new Double[1];
    
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (instance.isMissing(i)) {
        vals[i] = Instance.missingValue();
      }
      else if ((m_Cols.isInRange(i)) && (instance.attribute(i).isNumeric()))
      {
        params[0] = new Double(instance.value(i));
        Double newVal = (Double)m.invoke(null, (Object[])params);
        if ((newVal.isNaN()) || (newVal.isInfinite())) {
          vals[i] = Instance.missingValue();
        } else {
          vals[i] = newVal.doubleValue();
        }
      } else {
        vals[i] = instance.value(i);
      }
    }
    
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(instance.dataset());
    push(inst);
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify list of columns to transform. First and last are\n\tvalid indexes (default none). Non-numeric columns are \n\tskipped.", "R", 1, "-R <index1,index2-index4,...>"));
    




    newVector.addElement(new Option("\tInvert matching sense.", "V", 0, "-V"));
    


    newVector.addElement(new Option("\tSets the class containing transformation method.\n\t(default java.lang.Math)", "C", 1, "-C <string>"));
    



    newVector.addElement(new Option("\tSets the method. (default abs)", "M", 1, "-M <string>"));
    


    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    setAttributeIndices(Utils.getOption('R', options));
    setInvertSelection(Utils.getFlag('V', options));
    String classString = Utils.getOption('C', options);
    if (classString.length() != 0) {
      setClassName(classString);
    }
    String methodString = Utils.getOption('M', options);
    if (methodString.length() != 0) {
      setMethodName(methodString);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";options[(current++)] = getAttributeIndices();
    }
    if (m_Class != null) {
      options[(current++)] = "-C";options[(current++)] = getClassName();
    }
    if (m_Method != null) {
      options[(current++)] = "-M";options[(current++)] = getMethodName();
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String classNameTipText()
  {
    return "Name of the class containing the method used for the transformation.";
  }
  





  public String getClassName()
  {
    return m_Class;
  }
  





  public void setClassName(String name)
    throws ClassNotFoundException
  {
    m_Class = name;
  }
  





  public String methodNameTipText()
  {
    return "Name of the method used for the transformation.";
  }
  





  public String getMethodName()
  {
    return m_Method;
  }
  





  public void setMethodName(String name)
    throws NoSuchMethodException
  {
    m_Method = name;
  }
  





  public String invertSelectionTipText()
  {
    return "Whether to process the inverse of the given attribute ranges.";
  }
  





  public boolean getInvertSelection()
  {
    return m_Cols.getInvert();
  }
  





  public void setInvertSelection(boolean invert)
  {
    m_Cols.setInvert(invert);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_Cols.getRanges();
  }
  










  public void setAttributeIndices(String rangeList)
  {
    m_Cols.setRanges(rangeList);
  }
  








  public void setAttributeIndicesArray(int[] attributes)
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new NumericTransform(), argv);
  }
}
