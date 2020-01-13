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
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;

















































public class RemoveType
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -3563999462782486279L;
  protected Remove m_attributeFilter = new Remove();
  

  protected int m_attTypeToDelete = 2;
  

  protected boolean m_invert = false;
  

  public static final Tag[] TAGS_ATTRIBUTETYPE = { new Tag(1, "Delete nominal attributes"), new Tag(0, "Delete numeric attributes"), new Tag(2, "Delete string attributes"), new Tag(3, "Delete date attributes"), new Tag(4, "Delete relational attributes") };
  





  public RemoveType() {}
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
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
    
    int[] attsToDelete = new int[instanceInfo.numAttributes()];
    int numToDelete = 0;
    for (int i = 0; i < instanceInfo.numAttributes(); i++) {
      if (i == instanceInfo.classIndex()) {
        if (m_invert)
        {

          attsToDelete[(numToDelete++)] = i;
        }
      }
      else if (instanceInfo.attribute(i).type() == m_attTypeToDelete) {
        attsToDelete[(numToDelete++)] = i;
      }
    }
    int[] finalAttsToDelete = new int[numToDelete];
    System.arraycopy(attsToDelete, 0, finalAttsToDelete, 0, numToDelete);
    
    m_attributeFilter.setAttributeIndicesArray(finalAttsToDelete);
    m_attributeFilter.setInvertSelection(m_invert);
    
    boolean result = m_attributeFilter.setInputFormat(instanceInfo);
    Instances afOutputFormat = m_attributeFilter.getOutputFormat();
    

    afOutputFormat.setRelationName(instanceInfo.relationName());
    
    setOutputFormat(afOutputFormat);
    return result;
  }
  







  public boolean input(Instance instance)
  {
    return m_attributeFilter.input(instance);
  }
  





  public boolean batchFinished()
    throws Exception
  {
    return m_attributeFilter.batchFinished();
  }
  






  public Instance output()
  {
    return m_attributeFilter.output();
  }
  







  public Instance outputPeek()
  {
    return m_attributeFilter.outputPeek();
  }
  





  public int numPendingOutput()
  {
    return m_attributeFilter.numPendingOutput();
  }
  





  public boolean isOutputFormatDefined()
  {
    return m_attributeFilter.isOutputFormatDefined();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tAttribute type to delete. Valid options are \"nominal\", \n\t\"numeric\", \"string\", \"date\" and \"relational\".\n\t(default \"string\")", "T", 1, "-T <nominal|numeric|string|date|relational>"));
    




    newVector.addElement(new Option("\tInvert matching sense (i.e. only keep specified columns)", "V", 0, "-V"));
    


    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    String tString = Utils.getOption('T', options);
    if (tString.length() != 0) setAttributeTypeString(tString);
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
    options[(current++)] = "-T";
    options[(current++)] = getAttributeTypeString();
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String globalInfo()
  {
    return "Removes attributes of a given type.";
  }
  






  public String attributeTypeTipText()
  {
    return "The type of attribute to remove.";
  }
  





  public void setAttributeType(SelectedTag type)
  {
    if (type.getTags() == TAGS_ATTRIBUTETYPE) {
      m_attTypeToDelete = type.getSelectedTag().getID();
    }
  }
  





  public SelectedTag getAttributeType()
  {
    return new SelectedTag(m_attTypeToDelete, TAGS_ATTRIBUTETYPE);
  }
  






  public String invertSelectionTipText()
  {
    return "Determines whether action is to select or delete. If set to true, only the specified attributes will be kept; If set to false, specified attributes will be deleted.";
  }
  







  public boolean getInvertSelection()
  {
    return m_invert;
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_invert = invert;
  }
  





  protected String getAttributeTypeString()
  {
    if (m_attTypeToDelete == 1) return "nominal";
    if (m_attTypeToDelete == 0) return "numeric";
    if (m_attTypeToDelete == 2) return "string";
    if (m_attTypeToDelete == 3) return "date";
    if (m_attTypeToDelete == 4) return "relational";
    return "unknown";
  }
  





  protected void setAttributeTypeString(String typeString)
  {
    typeString = typeString.toLowerCase();
    if (typeString.equals("nominal")) { m_attTypeToDelete = 1;
    } else if (typeString.equals("numeric")) { m_attTypeToDelete = 0;
    } else if (typeString.equals("string")) { m_attTypeToDelete = 2;
    } else if (typeString.equals("date")) { m_attTypeToDelete = 3;
    } else if (typeString.equals("relational")) { m_attTypeToDelete = 4;
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9814 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RemoveType(), argv);
  }
}
