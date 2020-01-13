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
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;




















































public class Remove
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 5011337331921522847L;
  protected Range m_SelectCols = new Range();
  



  protected int[] m_SelectedAttributes;
  




  public Remove()
  {
    m_SelectCols.setInvert(true);
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tSpecify list of columns to delete. First and last are valid\n\tindexes. (default none)", "R", 1, "-R <index1,index2-index4,...>"));
    


    newVector.addElement(new Option("\tInvert matching sense (i.e. only keep specified columns)", "V", 0, "-V"));
    


    return newVector.elements();
  }
  

















  public void setOptions(String[] options)
    throws Exception
  {
    String deleteList = Utils.getOption('R', options);
    if (deleteList.length() != 0) {
      setAttributeIndices(deleteList);
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[3];
    int current = 0;
    
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";options[(current++)] = getAttributeIndices();
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
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
    
    m_SelectCols.setUpper(instanceInfo.numAttributes() - 1);
    

    FastVector attributes = new FastVector();
    int outputClass = -1;
    m_SelectedAttributes = m_SelectCols.getSelection();
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      if (instanceInfo.classIndex() == current) {
        outputClass = attributes.size();
      }
      Attribute keep = (Attribute)instanceInfo.attribute(current).copy();
      attributes.addElement(keep);
    }
    
    initInputLocators(getInputFormat(), m_SelectedAttributes);
    Instances outputFormat = new Instances(instanceInfo.relationName(), attributes, 0);
    
    outputFormat.setClassIndex(outputClass);
    setOutputFormat(outputFormat);
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
    
    if (getOutputFormat().numAttributes() == 0) {
      return false;
    }
    double[] vals = new double[getOutputFormat().numAttributes()];
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      vals[i] = instance.value(current);
    }
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(getOutputFormat());
    copyValues(inst, false, instance.dataset(), getOutputFormat());
    inst.setDataset(getOutputFormat());
    push(inst);
    return true;
  }
  






  public String globalInfo()
  {
    return "A filter that removes a range of attributes from the dataset. Will re-order the remaining attributes if invert matching sense is turned on and the attribute column indices are not specified in ascending order.";
  }
  











  public String invertSelectionTipText()
  {
    return "Determines whether action is to select or delete. If set to true, only the specified attributes will be kept; If set to false, specified attributes will be deleted.";
  }
  







  public boolean getInvertSelection()
  {
    return !m_SelectCols.getInvert();
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_SelectCols.setInvert(!invert);
  }
  






  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_SelectCols.getRanges();
  }
  








  public void setAttributeIndices(String rangeList)
  {
    m_SelectCols.setRanges(rangeList);
  }
  







  public void setAttributeIndicesArray(int[] attributes)
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7107 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Remove(), argv);
  }
}
