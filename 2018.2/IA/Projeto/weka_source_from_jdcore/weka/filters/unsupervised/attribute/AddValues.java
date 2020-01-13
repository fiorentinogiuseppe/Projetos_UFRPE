package weka.filters.unsupervised.attribute;

import java.util.Collections;
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























































public class AddValues
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  private static final long serialVersionUID = -8100622241742393656L;
  protected SingleIndex m_AttIndex = new SingleIndex("last");
  

  protected Vector m_Labels = new Vector();
  

  protected boolean m_Sort = false;
  

  protected int[] m_SortedIndices;
  


  public AddValues() {}
  

  public String globalInfo()
  {
    return "Adds the labels from the given list to an attribute if they are missing. The labels can also be sorted in an ascending manner. If no labels are provided then only the (optional) sorting applies.";
  }
  









  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSets the attribute index\n\t(default last).", "C", 1, "-C <col>"));
    



    result.addElement(new Option("\tComma-separated list of labels to add.\n\t(default: none)", "L", 1, "-L <label1,label2,...>"));
    



    result.addElement(new Option("\tTurns on the sorting of the labels.", "S", 0, "-S"));
    


    return result.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setAttributeIndex(tmpStr);
    } else {
      setAttributeIndex("last");
    }
    tmpStr = Utils.getOption('L', options);
    if (tmpStr.length() != 0) {
      setLabels(tmpStr);
    } else {
      setLabels("");
    }
    setSort(Utils.getFlag('S', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-C");
    result.add("" + getAttributeIndex());
    
    result.add("-L");
    result.add("" + getLabels());
    
    if (getSort()) {
      result.add("-S");
    }
    return (String[])result.toArray(new String[result.size()]);
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
    Attribute att = instanceInfo.attribute(m_AttIndex.getIndex());
    if (!att.isNominal()) {
      throw new UnsupportedAttributeTypeException("Chosen attribute not nominal.");
    }
    
    Vector allLabels = new Vector();
    Enumeration enm = att.enumerateValues();
    while (enm.hasMoreElements())
      allLabels.add(enm.nextElement());
    for (int i = 0; i < m_Labels.size(); i++) {
      if (!allLabels.contains(m_Labels.get(i))) {
        allLabels.add(m_Labels.get(i));
      }
    }
    
    if (getSort())
      Collections.sort(allLabels);
    m_SortedIndices = new int[att.numValues()];
    enm = att.enumerateValues();
    i = 0;
    while (enm.hasMoreElements()) {
      m_SortedIndices[i] = allLabels.indexOf(enm.nextElement());
      i++;
    }
    

    FastVector values = new FastVector();
    for (i = 0; i < allLabels.size(); i++)
      values.addElement(allLabels.get(i));
    Attribute attNew = new Attribute(att.name(), values);
    
    FastVector atts = new FastVector();
    for (i = 0; i < instanceInfo.numAttributes(); i++) {
      if (i == m_AttIndex.getIndex()) {
        atts.addElement(attNew);
      } else {
        atts.addElement(instanceInfo.attribute(i));
      }
    }
    Instances instNew = new Instances(instanceInfo.relationName(), atts, 0);
    instNew.setClassIndex(instanceInfo.classIndex());
    

    setOutputFormat(instNew);
    
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
    

    double[] values = instance.toDoubleArray();
    if (!instance.isMissing(m_AttIndex.getIndex()))
      values[m_AttIndex.getIndex()] = m_SortedIndices[((int)values[m_AttIndex.getIndex()])];
    Instance newInstance = new Instance(instance.weight(), values);
    

    copyValues(instance, false, instance.dataset(), getOutputFormat());
    
    push(newInstance);
    
    return true;
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
  





  public String labelsTipText()
  {
    return "Comma-separated list of lables to add.";
  }
  







  public String getLabels()
  {
    String result = "";
    for (int i = 0; i < m_Labels.size(); i++) {
      if (i > 0)
        result = result + ",";
      result = result + Utils.quote((String)m_Labels.get(i));
    }
    
    return result;
  }
  









  public void setLabels(String value)
  {
    m_Labels.clear();
    
    String label = "";
    boolean quoted = false;
    boolean add = false;
    
    for (int i = 0; i < value.length(); i++)
    {
      if (value.charAt(i) == '"') {
        quoted = !quoted;
        if (!quoted) {
          add = true;
        }
      }
      else if ((value.charAt(i) == ',') && (!quoted)) {
        add = true;
      }
      else
      {
        label = label + value.charAt(i);
        
        if (i == value.length() - 1) {
          add = true;
        }
      }
      if (add) {
        if (label.length() != 0)
          m_Labels.add(label);
        label = "";
        add = false;
      }
    }
  }
  





  public String sortTipText()
  {
    return "Whether to sort the labels alphabetically.";
  }
  




  public boolean getSort()
  {
    return m_Sort;
  }
  




  public void setSort(boolean value)
  {
    m_Sort = value;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6897 $");
  }
  





  public static void main(String[] args)
  {
    runFilter(new AddValues(), args);
  }
}
