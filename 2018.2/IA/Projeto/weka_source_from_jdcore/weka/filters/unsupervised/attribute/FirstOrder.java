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
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;

























































public class FirstOrder
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -7500464545400454179L;
  protected Range m_DeltaCols = new Range();
  


  public FirstOrder() {}
  


  public String globalInfo()
  {
    return "This instance filter takes a range of N numeric attributes and replaces them with N-1 numeric attributes, the values of which are the difference between consecutive attribute values from the original instance. eg: \n\nOriginal attribute values\n\n   0.1, 0.2, 0.3, 0.1, 0.3\n\nNew attribute values\n\n   0.1, 0.1, -0.2, 0.2\n\nThe range of attributes used is taken in numeric order. That is, a range spec of 7-11,3-5 will use the attribute ordering 3,4,5,7,8,9,10,11 for the differences, NOT 7,8,9,10,11,3,4,5.";
  }
  














  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tSpecify list of columns to take the differences between.\n\tFirst and last are valid indexes.\n\t(default none)", "R", 1, "-R <index1,index2-index4,...>"));
    




    return newVector.elements();
  }
  
















  public void setOptions(String[] options)
    throws Exception
  {
    String deltaList = Utils.getOption('R', options);
    if (deltaList.length() != 0) {
      setAttributeIndices(deltaList);
    } else {
      setAttributeIndices("");
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
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
    
    m_DeltaCols.setUpper(getInputFormat().numAttributes() - 1);
    int selectedCount = 0;
    for (int i = getInputFormat().numAttributes() - 1; i >= 0; i--) {
      if (m_DeltaCols.isInRange(i)) {
        selectedCount++;
        if (!getInputFormat().attribute(i).isNumeric()) {
          throw new UnsupportedAttributeTypeException("Selected attributes must be all numeric");
        }
      }
    }
    if (selectedCount == 1) {
      throw new Exception("Cannot select only one attribute.");
    }
    

    FastVector newAtts = new FastVector();
    boolean inRange = false;
    String foName = null;
    int clsIndex = -1;
    for (int i = 0; i < instanceInfo.numAttributes(); i++) {
      if ((m_DeltaCols.isInRange(i)) && (i != instanceInfo.classIndex())) {
        if (inRange) {
          Attribute newAttrib = new Attribute(foName);
          newAtts.addElement(newAttrib);
        }
        foName = instanceInfo.attribute(i).name();
        foName = "'FO " + foName.replace('\'', ' ').trim() + '\'';
        inRange = true;
      } else {
        newAtts.addElement((Attribute)instanceInfo.attribute(i).copy());
        if (i == instanceInfo.classIndex())
          clsIndex = newAtts.size() - 1;
      }
    }
    Instances data = new Instances(instanceInfo.relationName(), newAtts, 0);
    data.setClassIndex(clsIndex);
    setOutputFormat(data);
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
    
    Instances outputFormat = outputFormatPeek();
    double[] vals = new double[outputFormat.numAttributes()];
    boolean inRange = false;
    double lastVal = Instance.missingValue();
    
    int i = 0; for (int j = 0; j < outputFormat.numAttributes(); i++) {
      if ((m_DeltaCols.isInRange(i)) && (i != instance.classIndex())) {
        if (inRange) {
          if ((Instance.isMissingValue(lastVal)) || (instance.isMissing(i))) {
            vals[(j++)] = Instance.missingValue();
          } else {
            vals[(j++)] = (instance.value(i) - lastVal);
          }
        } else {
          inRange = true;
        }
        lastVal = instance.value(i);
      } else {
        vals[(j++)] = instance.value(i);
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
    return true;
  }
  






  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_DeltaCols.getRanges();
  }
  








  public void setAttributeIndices(String rangeList)
    throws Exception
  {
    m_DeltaCols.setRanges(rangeList);
  }
  







  public void setAttributeIndicesArray(int[] attributes)
    throws Exception
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new FirstOrder(), argv);
  }
}
