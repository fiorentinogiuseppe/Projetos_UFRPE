package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RelationalLocator;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.StringLocator;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;
























































public class MultiInstanceToPropositional
  extends Filter
  implements OptionHandler, UnsupervisedFilter, MultiInstanceCapabilitiesHandler
{
  private static final long serialVersionUID = -4102847628883002530L;
  protected int m_NumBags;
  protected StringLocator m_BagStringAtts = null;
  

  protected RelationalLocator m_BagRelAtts = null;
  

  protected int m_NumInstances;
  

  public static final int WEIGHTMETHOD_ORIGINAL = 0;
  
  public static final int WEIGHTMETHOD_1 = 1;
  
  public static final int WEIGHTMETHOD_INVERSE1 = 2;
  
  public static final int WEIGHTMETHOD_INVERSE2 = 3;
  
  public static final Tag[] TAGS_WEIGHTMETHOD = { new Tag(0, "keep the weight to be the same as the original value"), new Tag(1, "1.0"), new Tag(2, "1.0 / Total # of prop. instance in the corresp. bag"), new Tag(3, "Total # of prop. instance / (Total # of bags * Total # of prop. instance in the corresp. bag)") };
  










  protected int m_WeightMethod = 3;
  

  public MultiInstanceToPropositional() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe type of weight setting for each prop. instance:\n\t0.weight = original single bag weight /Total number of\n\tprop. instance in the corresponding bag;\n\t1.weight = 1.0;\n\t2.weight = 1.0/Total number of prop. instance in the \n\t\tcorresponding bag; \n\t3. weight = Total number of prop. instance / (Total number \n\t\tof bags * Total number of prop. instance in the \n\t\tcorresponding bag). \n\t(default:0)", "A", 1, "-A <num>"));
    











    return result.elements();
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    String weightString = Utils.getOption('A', options);
    if (weightString.length() != 0) {
      setWeightMethod(new SelectedTag(Integer.parseInt(weightString), TAGS_WEIGHTMETHOD));
    }
    else {
      setWeightMethod(new SelectedTag(3, TAGS_WEIGHTMETHOD));
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-A");
    result.add("" + m_WeightMethod);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String weightMethodTipText()
  {
    return "The method used for weighting the instances.";
  }
  




  public void setWeightMethod(SelectedTag method)
  {
    if (method.getTags() == TAGS_WEIGHTMETHOD) {
      m_WeightMethod = method.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getWeightMethod()
  {
    return new SelectedTag(m_WeightMethod, TAGS_WEIGHTMETHOD);
  }
  






  public String globalInfo()
  {
    return "Converts the multi-instance dataset into single instance dataset so that the Nominalize, Standardize and other type of filters or transformation  can be applied to these data for the further preprocessing.\nNote: the first attribute of the converted dataset is a nominal attribute and refers to the bagId.";
  }
  










  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.disableAllAttributes();
    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = new Capabilities(this);
    

    result.enableAllAttributes();
    result.disable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    if (instanceInfo.attribute(1).type() != 4) {
      throw new Exception("Can only handle relational-valued attribute!");
    }
    super.setInputFormat(instanceInfo);
    
    m_NumBags = instanceInfo.numInstances();
    m_NumInstances = 0;
    for (int i = 0; i < m_NumBags; i++) {
      if (instanceInfo.instance(i).relationalValue(1) == null) {
        m_NumInstances += 1;
      } else {
        m_NumInstances += instanceInfo.instance(i).relationalValue(1).numInstances();
      }
    }
    Attribute classAttribute = (Attribute)instanceInfo.classAttribute().copy();
    Attribute bagIndex = (Attribute)instanceInfo.attribute(0).copy();
    

    Instances newData = instanceInfo.attribute(1).relation().stringFreeStructure();
    newData.insertAttributeAt(bagIndex, 0);
    newData.insertAttributeAt(classAttribute, newData.numAttributes());
    newData.setClassIndex(newData.numAttributes() - 1);
    
    super.setOutputFormat(newData.stringFreeStructure());
    
    m_BagStringAtts = new StringLocator(instanceInfo.attribute(1).relation().stringFreeStructure());
    m_BagRelAtts = new RelationalLocator(instanceInfo.attribute(1).relation().stringFreeStructure());
    
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
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    Instances input = getInputFormat();
    

    for (int i = 0; i < input.numInstances(); i++) {
      convertInstance(input.instance(i));
    }
    

    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  






  private void convertInstance(Instance bag)
  {
    Instances data = bag.relationalValue(1);
    int bagSize = 1;
    if (data != null) {
      bagSize = data.numInstances();
    }
    double bagIndex = bag.value(0);
    double classValue = bag.classValue();
    double weight = 0.0D;
    
    if (m_WeightMethod == 1) {
      weight = 1.0D;
    } else if (m_WeightMethod == 2) {
      weight = 1.0D / bagSize;
    } else if (m_WeightMethod == 3) {
      weight = m_NumInstances / (m_NumBags * bagSize);
    } else {
      weight = bag.weight() / bagSize;
    }
    
    Instances outputFormat = getOutputFormat().stringFreeStructure();
    
    for (int i = 0; i < bagSize; i++) {
      Instance newInst = new Instance(outputFormat.numAttributes());
      newInst.setDataset(outputFormat);
      newInst.setValue(0, bagIndex);
      if (!bag.classIsMissing()) {
        newInst.setClassValue(classValue);
      }
      for (int j = 1; j < outputFormat.numAttributes() - 1; j++) {
        if (data == null) {
          newInst.setMissing(j);
        } else {
          newInst.setValue(j, data.instance(i).value(j - 1));
        }
      }
      
      newInst.setWeight(weight);
      

      StringLocator.copyStringValues(newInst, false, data, m_BagStringAtts, outputFormat, m_OutputStringAtts);
      



      RelationalLocator.copyRelationalValues(newInst, false, data, m_BagRelAtts, outputFormat, m_OutputRelAtts);
      



      push(newInst);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9141 $");
  }
  





  public static void main(String[] args)
  {
    runFilter(new MultiInstanceToPropositional(), args);
  }
}
