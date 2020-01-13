package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RelationalLocator;
import weka.core.RevisionUtils;
import weka.core.StringLocator;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;




















































public class PropositionalToMultiInstance
  extends Filter
  implements OptionHandler, UnsupervisedFilter
{
  private static final long serialVersionUID = 5825873573912102482L;
  protected boolean m_DoNotWeightBags = false;
  

  protected int m_Seed = 1;
  

  protected boolean m_Randomize = false;
  

  protected StringLocator m_BagStringAtts = null;
  

  protected RelationalLocator m_BagRelAtts = null;
  


  public PropositionalToMultiInstance() {}
  

  public String globalInfo()
  {
    return "Converts a propositional dataset into a multi-instance dataset (with relational attribute). When normalizing or standardizing a multi-instance dataset, the MultiInstanceToPropositional filter can be applied first to convert the multi-instance dataset into a propositional instance dataset. After normalization or standardization, we may use this PropositionalToMultiInstance filter to convert the data back to multi-instance format.\n\nNote: the first attribute of the original propositional instance dataset must be a nominal attribute which is expected to be the bagId attribute.";
  }
  















  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tDo not weight bags by number of instances they contain.\t(default off)", "no-weights", 0, "-no-weights"));
    



    result.addElement(new Option("\tThe seed for the randomization of the order of bags.\t(default 1)", "S", 1, "-S <num>"));
    



    result.addElement(new Option("\tRandomizes the order of the produced bags after the generation.\t(default off)", "R", 0, "-R"));
    



    return result.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    setDoNotWeightBags(Utils.getFlag("no-weights", options));
    
    setRandomize(Utils.getFlag('R', options));
    
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-S");
    result.add("" + getSeed());
    
    if (m_Randomize) {
      result.add("-R");
    }
    if (getDoNotWeightBags()) {
      result.add("-no-weights");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String seedTipText()
  {
    return "The seed used by the random number generator";
  }
  




  public void setSeed(int value)
  {
    m_Seed = value;
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  





  public String randomizeTipText()
  {
    return "Whether the order of the generated data is randomized.";
  }
  




  public void setRandomize(boolean value)
  {
    m_Randomize = value;
  }
  




  public boolean getRandomize()
  {
    return m_Randomize;
  }
  





  public String doNotWeightBagsTipText()
  {
    return "Whether the bags are weighted by the number of instances they contain.";
  }
  




  public void setDoNotWeightBags(boolean value)
  {
    m_DoNotWeightBags = value;
  }
  




  public boolean getDoNotWeightBags()
  {
    return m_DoNotWeightBags;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    if (instanceInfo.attribute(0).type() != 1) {
      throw new Exception("The first attribute type of the original propositional instance dataset must be Nominal!");
    }
    super.setInputFormat(instanceInfo);
    

    Instances newData = instanceInfo.stringFreeStructure();
    Attribute attBagIndex = (Attribute)newData.attribute(0).copy();
    Attribute attClass = (Attribute)newData.classAttribute().copy();
    
    newData.deleteAttributeAt(0);
    
    newData.setClassIndex(-1);
    newData.deleteAttributeAt(newData.numAttributes() - 1);
    
    FastVector attInfo = new FastVector(3);
    attInfo.addElement(attBagIndex);
    attInfo.addElement(new Attribute("bag", newData));
    attInfo.addElement(attClass);
    Instances data = new Instances("Multi-Instance-Dataset", attInfo, 0);
    data.setClassIndex(data.numAttributes() - 1);
    
    super.setOutputFormat(data.stringFreeStructure());
    
    m_BagStringAtts = new StringLocator(data.attribute(1).relation());
    m_BagRelAtts = new RelationalLocator(data.attribute(1).relation());
    
    return true;
  }
  

















  protected void addBag(Instances input, Instances output, Instances bagInsts, int bagIndex, double classValue, double bagWeight)
  {
    for (int i = 0; i < bagInsts.numInstances(); i++) {
      RelationalLocator.copyRelationalValues(bagInsts.instance(i), false, input, m_InputRelAtts, bagInsts, m_BagRelAtts);
      



      StringLocator.copyStringValues(bagInsts.instance(i), false, input, m_InputStringAtts, bagInsts, m_BagStringAtts);
    }
    



    int value = output.attribute(1).addRelation(bagInsts);
    Instance newBag = new Instance(output.numAttributes());
    newBag.setValue(0, bagIndex);
    newBag.setValue(2, classValue);
    newBag.setValue(1, value);
    if (!m_DoNotWeightBags) {
      newBag.setWeight(bagWeight);
    }
    newBag.setDataset(output);
    output.add(newBag);
  }
  





  protected void push(Instance instance)
  {
    if (instance != null) {
      super.push(instance);
    }
  }
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    Instances input = getInputFormat();
    input.sort(0);
    Instances output = getOutputFormat();
    Instances bagInsts = output.attribute(1).relation();
    Instance inst = new Instance(bagInsts.numAttributes());
    inst.setDataset(bagInsts);
    
    double bagIndex = input.instance(0).value(0);
    double classValue = input.instance(0).classValue();
    double bagWeight = 0.0D;
    

    for (int i = 0; i < input.numInstances(); i++) {
      double currentBagIndex = input.instance(i).value(0);
      

      for (int j = 0; j < input.numAttributes() - 2; j++)
        inst.setValue(j, input.instance(i).value(j + 1));
      inst.setWeight(input.instance(i).weight());
      
      if (currentBagIndex == bagIndex) {
        bagInsts.add(inst);
        bagWeight += inst.weight();
      }
      else {
        addBag(input, output, bagInsts, (int)bagIndex, classValue, bagWeight);
        
        bagInsts = bagInsts.stringFreeStructure();
        bagInsts.add(inst);
        bagIndex = currentBagIndex;
        classValue = input.instance(i).classValue();
        bagWeight = inst.weight();
      }
    }
    

    addBag(input, output, bagInsts, (int)bagIndex, classValue, bagWeight);
    
    if (getRandomize()) {
      output.randomize(new Random(getSeed()));
    }
    for (int i = 0; i < output.numInstances(); i++) {
      push(output.instance(i));
    }
    
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9666 $");
  }
  





  public static void main(String[] args)
  {
    runFilter(new PropositionalToMultiInstance(), args);
  }
}
