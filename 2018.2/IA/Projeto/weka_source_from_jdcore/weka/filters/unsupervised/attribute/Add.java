package weka.filters.unsupervised.attribute;

import java.text.SimpleDateFormat;
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
import weka.core.SelectedTag;
import weka.core.SingleIndex;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;


































































public class Add
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 761386447332932389L;
  public static final Tag[] TAGS_TYPE = { new Tag(0, "NUM", "Numeric attribute"), new Tag(1, "NOM", "Nominal attribute"), new Tag(2, "STR", "String attribute"), new Tag(3, "DAT", "Date attribute") };
  






  protected int m_AttributeType = 0;
  

  protected String m_Name = "unnamed";
  

  private SingleIndex m_Insert = new SingleIndex("last");
  

  protected FastVector m_Labels = new FastVector();
  

  protected String m_DateFormat = "yyyy-MM-dd'T'HH:mm:ss";
  


  public Add() {}
  


  public String globalInfo()
  {
    return "An instance filter that adds a new attribute to the dataset. The new attribute will contain all missing values.";
  }
  










  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    String desc = "";
    for (int i = 0; i < TAGS_TYPE.length; i++) {
      SelectedTag tag = new SelectedTag(TAGS_TYPE[i].getID(), TAGS_TYPE);
      desc = desc + "\t" + tag.getSelectedTag().getIDStr() + " = " + tag.getSelectedTag().getReadable() + "\n";
    }
    

    newVector.addElement(new Option("\tThe type of attribute to create:\n" + desc + "\t(default: " + new SelectedTag(0, TAGS_TYPE) + ")", "T", 1, "-T " + Tag.toOptionList(TAGS_TYPE)));
    




    newVector.addElement(new Option("\tSpecify where to insert the column. First and last\n\tare valid indexes.(default: last)", "C", 1, "-C <index>"));
    



    newVector.addElement(new Option("\tName of the new attribute.\n\t(default: 'Unnamed')", "N", 1, "-N <name>"));
    



    newVector.addElement(new Option("\tCreate nominal attribute with given labels\n\t(default: numeric attribute)", "L", 1, "-L <label1,label2,...>"));
    



    newVector.addElement(new Option("\tThe format of the date values (see ISO-8601)\n\t(default: yyyy-MM-dd'T'HH:mm:ss)", "F", 1, "-F <format>"));
    



    return newVector.elements();
  }
  




































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('T', options);
    if (tmpStr.length() != 0) {
      setAttributeType(new SelectedTag(tmpStr, TAGS_TYPE));
    } else {
      setAttributeType(new SelectedTag(0, TAGS_TYPE));
    }
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() == 0)
      tmpStr = "last";
    setAttributeIndex(tmpStr);
    
    setAttributeName(Utils.unbackQuoteChars(Utils.getOption('N', options)));
    
    if (m_AttributeType == 1) {
      tmpStr = Utils.getOption('L', options);
      if (tmpStr.length() != 0) {
        setNominalLabels(tmpStr);
      }
    } else if (m_AttributeType == 3) {
      tmpStr = Utils.getOption('F', options);
      if (tmpStr.length() != 0) {
        setDateFormat(tmpStr);
      }
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (m_AttributeType != 0) {
      result.add("-T");
      result.add("" + getAttributeType());
    }
    
    result.add("-N");
    result.add(Utils.backQuoteChars(getAttributeName()));
    
    if (m_AttributeType == 1) {
      result.add("-L");
      result.add(getNominalLabels());
    }
    else if (m_AttributeType == 1) {
      result.add("-F");
      result.add(getDateFormat());
    }
    
    result.add("-C");
    result.add("" + getAttributeIndex());
    
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
    
    m_Insert.setUpper(instanceInfo.numAttributes());
    Instances outputFormat = new Instances(instanceInfo, 0);
    Attribute newAttribute = null;
    switch (m_AttributeType) {
    case 0: 
      newAttribute = new Attribute(m_Name);
      break;
    case 1: 
      newAttribute = new Attribute(m_Name, m_Labels);
      break;
    case 2: 
      newAttribute = new Attribute(m_Name, (FastVector)null);
      break;
    case 3: 
      newAttribute = new Attribute(m_Name, m_DateFormat);
      break;
    default: 
      throw new IllegalArgumentException("Unknown attribute type in Add");
    }
    
    if ((m_Insert.getIndex() < 0) || (m_Insert.getIndex() > getInputFormat().numAttributes()))
    {
      throw new IllegalArgumentException("Index out of range");
    }
    outputFormat.insertAttributeAt(newAttribute, m_Insert.getIndex());
    setOutputFormat(outputFormat);
    


    Range atts = new Range(m_Insert.getSingleIndex());
    atts.setInvert(true);
    atts.setUpper(outputFormat.numAttributes() - 1);
    initOutputLocators(outputFormat, atts.getSelection());
    
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
    
    Instance inst = (Instance)instance.copy();
    

    copyValues(inst, true, inst.dataset(), getOutputFormat());
    

    inst.setDataset(null);
    inst.insertAttributeAt(m_Insert.getIndex());
    inst.setDataset(getOutputFormat());
    push(inst);
    return true;
  }
  






  public String attributeNameTipText()
  {
    return "Set the new attribute's name.";
  }
  





  public String getAttributeName()
  {
    return m_Name;
  }
  




  public void setAttributeName(String name)
  {
    if (name.trim().equals("")) {
      m_Name = "unnamed";
    } else {
      m_Name = name;
    }
  }
  





  public String attributeIndexTipText()
  {
    return "The position (starting from 1) where the attribute will be inserted (first and last are valid indices).";
  }
  






  public String getAttributeIndex()
  {
    return m_Insert.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_Insert.setSingleIndex(attIndex);
  }
  





  public String nominalLabelsTipText()
  {
    return "The list of value labels (nominal attribute creation only).  The list must be comma-separated, eg: \"red,green,blue\". If this is empty, the created attribute will be numeric.";
  }
  







  public String getNominalLabels()
  {
    String labelList = "";
    for (int i = 0; i < m_Labels.size(); i++) {
      if (i == 0) {
        labelList = (String)m_Labels.elementAt(i);
      } else {
        labelList = labelList + "," + (String)m_Labels.elementAt(i);
      }
    }
    return labelList;
  }
  






  public void setNominalLabels(String labelList)
  {
    FastVector labels = new FastVector(10);
    
    int commaLoc;
    
    while ((commaLoc = labelList.indexOf(',')) >= 0) {
      String label = labelList.substring(0, commaLoc).trim();
      if (!label.equals("")) {
        labels.addElement(label);
      } else {
        throw new IllegalArgumentException("Invalid label list at " + labelList.substring(commaLoc));
      }
      
      labelList = labelList.substring(commaLoc + 1);
    }
    String label = labelList.trim();
    if (!label.equals("")) {
      labels.addElement(label);
    }
    

    m_Labels = labels;
    if (labels.size() == 0) {
      m_AttributeType = 0;
    } else {
      m_AttributeType = 1;
    }
  }
  





  public String attributeTypeTipText()
  {
    return "Defines the type of the attribute to generate.";
  }
  




  public void setAttributeType(SelectedTag value)
  {
    if (value.getTags() == TAGS_TYPE) {
      m_AttributeType = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getAttributeType()
  {
    return new SelectedTag(m_AttributeType, TAGS_TYPE);
  }
  





  public String dateFormatTipText()
  {
    return "The format of the date values (see ISO-8601).";
  }
  




  public String getDateFormat()
  {
    return m_DateFormat;
  }
  



  public void setDateFormat(String value)
  {
    try
    {
      new SimpleDateFormat(value);
      m_DateFormat = value;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Add(), argv);
  }
}
