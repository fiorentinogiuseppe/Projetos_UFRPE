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

























































public class NominalToBinary
  extends Filter
  implements UnsupervisedFilter, OptionHandler, StreamableFilter
{
  static final long serialVersionUID = -1130642825710549138L;
  protected Range m_Columns = new Range();
  

  private boolean m_Numeric = true;
  

  private boolean m_TransformAll = false;
  

  private boolean m_needToTransform = false;
  

  public NominalToBinary()
  {
    setAttributeIndices("first-last");
  }
  






  public String globalInfo()
  {
    return "Converts all nominal attributes into binary numeric attributes. An attribute with k values is transformed into k binary attributes if the class is nominal (using the one-attribute-per-value approach). Binary attributes are left binary, if option '-A' is not given.If the class is numeric, you might want to use the supervised version of this filter.";
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
    
    m_Columns.setUpper(instanceInfo.numAttributes() - 1);
    
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
    
    convertInstance(instance);
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSets if binary attributes are to be coded as nominal ones.", "N", 0, "-N"));
    


    newVector.addElement(new Option("\tFor each nominal value a new attribute is created, \n\tnot only if there are more than 2 values.", "A", 0, "-A"));
    



    newVector.addElement(new Option("\tSpecifies list of columns to act on. First and last are \n\tvalid indexes.\n\t(default: first-last)", "R", 1, "-R <col1,col2-col4,...>"));
    




    newVector.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    setBinaryAttributesNominal(Utils.getFlag('N', options));
    
    setTransformAllValues(Utils.getFlag('A', options));
    
    String convertList = Utils.getOption('R', options);
    if (convertList.length() != 0) {
      setAttributeIndices(convertList);
    } else {
      setAttributeIndices("first-last");
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[4];
    int current = 0;
    
    if (getBinaryAttributesNominal()) {
      options[(current++)] = "-N";
    }
    
    if (getTransformAllValues()) {
      options[(current++)] = "-A";
    }
    
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";options[(current++)] = getAttributeIndices();
    }
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String binaryAttributesNominalTipText()
  {
    return "Whether resulting binary attributes will be nominal.";
  }
  





  public boolean getBinaryAttributesNominal()
  {
    return !m_Numeric;
  }
  





  public void setBinaryAttributesNominal(boolean bool)
  {
    m_Numeric = (!bool);
  }
  





  public String transformAllValuesTipText()
  {
    return "Whether all nominal values are turned into new attributes, not only if there are more than 2.";
  }
  






  public boolean getTransformAllValues()
  {
    return m_TransformAll;
  }
  






  public void setTransformAllValues(boolean bool)
  {
    m_TransformAll = bool;
  }
  






  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected (numeric) attributes in the range will be discretized; if true, only non-selected attributes will be discretized.";
  }
  







  public boolean getInvertSelection()
  {
    return m_Columns.getInvert();
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_Columns.setInvert(invert);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_Columns.getRanges();
  }
  









  public void setAttributeIndices(String rangeList)
  {
    m_Columns.setRanges(rangeList);
  }
  











  private void setOutputFormat()
  {
    m_needToTransform = false;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      Attribute att = getInputFormat().attribute(i);
      if ((att.isNominal()) && (i != getInputFormat().classIndex()) && ((att.numValues() > 2) || (m_TransformAll) || (m_Numeric)))
      {
        m_needToTransform = true;
        break;
      }
    }
    
    if (!m_needToTransform) {
      setOutputFormat(getInputFormat());
      return;
    }
    
    int newClassIndex = getInputFormat().classIndex();
    FastVector newAtts = new FastVector();
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex()) || (!m_Columns.isInRange(j)))
      {
        newAtts.addElement(att.copy());
      }
      else if ((att.numValues() <= 2) && (!m_TransformAll)) {
        if (m_Numeric) {
          newAtts.addElement(new Attribute(att.name()));
        } else {
          newAtts.addElement(att.copy());
        }
      }
      else {
        if ((newClassIndex >= 0) && (j < getInputFormat().classIndex())) {
          newClassIndex += att.numValues() - 1;
        }
        

        for (int k = 0; k < att.numValues(); k++) {
          StringBuffer attributeName = new StringBuffer(att.name() + "=");
          
          attributeName.append(att.value(k));
          if (m_Numeric) {
            newAtts.addElement(new Attribute(attributeName.toString()));
          }
          else {
            FastVector vals = new FastVector(2);
            vals.addElement("f");vals.addElement("t");
            newAtts.addElement(new Attribute(attributeName.toString(), vals));
          }
        }
      }
    }
    

    Instances outputFormat = new Instances(getInputFormat().relationName(), newAtts, 0);
    
    outputFormat.setClassIndex(newClassIndex);
    setOutputFormat(outputFormat);
  }
  






  private void convertInstance(Instance instance)
  {
    if (!m_needToTransform) {
      push(instance);
      return;
    }
    
    double[] vals = new double[outputFormatPeek().numAttributes()];
    int attSoFar = 0;
    
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex()) || (!m_Columns.isInRange(j)))
      {
        vals[attSoFar] = instance.value(j);
        attSoFar++;
      }
      else if ((att.numValues() <= 2) && (!m_TransformAll)) {
        vals[attSoFar] = instance.value(j);
        attSoFar++;
      } else {
        if (instance.isMissing(j)) {
          for (int k = 0; k < att.numValues(); k++) {
            vals[(attSoFar + k)] = instance.value(j);
          }
        } else {
          for (int k = 0; k < att.numValues(); k++) {
            if (k == (int)instance.value(j)) {
              vals[(attSoFar + k)] = 1.0D;
            } else {
              vals[(attSoFar + k)] = 0.0D;
            }
          }
        }
        attSoFar += att.numValues();
      }
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
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9819 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new NominalToBinary(), argv);
  }
}
