package weka.filters.unsupervised.attribute;

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

















































public class Copy
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -8543707493627441566L;
  protected Range m_CopyCols = new Range();
  


  protected int[] m_SelectedAttributes;
  



  public Copy() {}
  


  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tSpecify list of columns to copy. First and last are valid\n\tindexes. (default none)", "R", 1, "-R <index1,index2-index4,...>"));
    


    newVector.addElement(new Option("\tInvert matching sense (i.e. copy all non-specified columns)", "V", 0, "-V"));
    


    return newVector.elements();
  }
  

















  public void setOptions(String[] options)
    throws Exception
  {
    String copyList = Utils.getOption('R', options);
    if (copyList.length() != 0) {
      setAttributeIndices(copyList);
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
    
    m_CopyCols.setUpper(instanceInfo.numAttributes() - 1);
    

    Instances outputFormat = new Instances(instanceInfo, 0);
    m_SelectedAttributes = m_CopyCols.getSelection();
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      
      Attribute origAttribute = instanceInfo.attribute(current);
      outputFormat.insertAttributeAt(origAttribute.copy("Copy of " + origAttribute.name()), outputFormat.numAttributes());
    }
    


    int[] newIndices = new int[instanceInfo.numAttributes() + m_SelectedAttributes.length];
    for (int i = 0; i < instanceInfo.numAttributes(); i++)
      newIndices[i] = i;
    for (int i = 0; i < m_SelectedAttributes.length; i++)
      newIndices[(instanceInfo.numAttributes() + i)] = m_SelectedAttributes[i];
    initInputLocators(instanceInfo, newIndices);
    
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
    
    double[] vals = new double[outputFormatPeek().numAttributes()];
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      vals[i] = instance.value(i);
    }
    int j = getInputFormat().numAttributes();
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      vals[(i + j)] = instance.value(current);
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
    return "An instance filter that copies a range of attributes in the dataset. This is used in conjunction with other filters that overwrite attribute values during the course of their operation -- this filter allows the original attributes to be kept as well as the new attributes.";
  }
  









  public String invertSelectionTipText()
  {
    return "Sets copy selected vs unselected action. If set to false, only the specified attributes will be copied; If set to true, non-specified attributes will be copied.";
  }
  







  public boolean getInvertSelection()
  {
    return m_CopyCols.getInvert();
  }
  










  public void setInvertSelection(boolean invert)
  {
    m_CopyCols.setInvert(invert);
  }
  





  public String getAttributeIndices()
  {
    return m_CopyCols.getRanges();
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  














  public void setAttributeIndices(String rangeList)
    throws Exception
  {
    m_CopyCols.setRanges(rangeList);
  }
  










  public void setAttributeIndicesArray(int[] attributes)
    throws Exception
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6996 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Copy(), argv);
  }
}
