package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleStreamFilter;




















































public class RandomSubset
  extends SimpleStreamFilter
{
  private static final long serialVersionUID = 2911221724251628050L;
  protected double m_NumAttributes = 0.5D;
  

  protected int m_Seed = 1;
  

  protected int[] m_Indices = null;
  


  public RandomSubset() {}
  

  public String globalInfo()
  {
    return "Chooses a random subset of attributes, either an absolute number or a percentage. The class is always included in the output (as the last attribute).";
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe number of attributes to randomly select.\n\tIf < 1 then percentage, >= 1 absolute number.\n\t(default: 0.5)", "N", 1, "-N <double>"));
    




    result.addElement(new Option("\tThe seed value.\n\t(default: 1)", "S", 1, "-S <int>"));
    



    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-N");
    result.add("" + m_NumAttributes);
    
    result.add("-S");
    result.add("" + m_Seed);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption("N", options);
    if (tmpStr.length() != 0) {
      setNumAttributes(Double.parseDouble(tmpStr));
    } else {
      setNumAttributes(0.5D);
    }
    tmpStr = Utils.getOption("S", options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }
    super.setOptions(options);
  }
  





  public String numAttributesTipText()
  {
    return "The number of attributes to choose: < 1 percentage, >= 1 absolute number.";
  }
  




  public double getNumAttributes()
  {
    return m_NumAttributes;
  }
  




  public void setNumAttributes(double value)
  {
    m_NumAttributes = value;
  }
  





  public String seedTipText()
  {
    return "The seed value for the random number generator.";
  }
  




  public int getSeed()
  {
    return m_Seed;
  }
  




  public void setSeed(int value)
  {
    m_Seed = value;
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
  



















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    int numAtts = inputFormat.numAttributes();
    if (inputFormat.classIndex() > -1) {
      numAtts--;
    }
    if (m_NumAttributes < 1.0D) {
      numAtts = (int)Math.round(numAtts * m_NumAttributes);

    }
    else if (m_NumAttributes < numAtts) {
      numAtts = (int)m_NumAttributes;
    }
    if (getDebug()) {
      System.out.println("# of atts: " + numAtts);
    }
    
    Vector<Integer> indices = new Vector();
    for (int i = 0; i < inputFormat.numAttributes(); i++) {
      if (i != inputFormat.classIndex())
      {
        indices.add(Integer.valueOf(i));
      }
    }
    Vector<Integer> subset = new Vector();
    Random rand = new Random(m_Seed);
    for (i = 0; i < numAtts; i++) {
      int index = rand.nextInt(indices.size());
      subset.add(indices.get(index));
      indices.remove(index);
    }
    Collections.sort(subset);
    if (inputFormat.classIndex() > -1)
      subset.add(Integer.valueOf(inputFormat.classIndex()));
    if (getDebug()) {
      System.out.println("indices: " + subset);
    }
    
    FastVector atts = new FastVector();
    m_Indices = new int[subset.size()];
    for (i = 0; i < subset.size(); i++) {
      atts.addElement(inputFormat.attribute(((Integer)subset.get(i)).intValue()));
      m_Indices[i] = ((Integer)subset.get(i)).intValue();
    }
    Instances result = new Instances(inputFormat.relationName(), atts, 0);
    if (inputFormat.classIndex() > -1) {
      result.setClassIndex(result.numAttributes() - 1);
    }
    return result;
  }
  










  protected Instance process(Instance instance)
    throws Exception
  {
    double[] values = new double[m_Indices.length];
    for (int i = 0; i < m_Indices.length; i++) {
      values[i] = instance.value(m_Indices[i]);
    }
    Instance result = new Instance(instance.weight(), values);
    result.setDataset(getOutputFormat());
    
    copyValues(result, false, instance.dataset(), getOutputFormat());
    result.setDataset(getOutputFormat());
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5547 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new RandomSubset(), args);
  }
}
