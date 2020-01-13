package weka.datagenerators.classifiers.classification;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.net.BayesNetGenerator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.datagenerators.ClassificationGenerator;









































































public class BayesNet
  extends ClassificationGenerator
{
  static final long serialVersionUID = -796118162379901512L;
  protected BayesNetGenerator m_Generator;
  
  public BayesNet()
  {
    setNumAttributes(defaultNumAttributes());
    setNumArcs(defaultNumArcs());
    setCardinality(defaultCardinality());
  }
  





  public String globalInfo()
  {
    return "Generates random instances based on a Bayes network.";
  }
  





  public Enumeration<Option> listOptions()
  {
    Vector<Option> result = enumToVector(super.listOptions());
    
    result.add(new Option("\tThe number of arcs to use. (default " + defaultNumArcs() + ")", "A", 1, "-A <num>"));
    

    result.add(new Option("\tThe number of attributes to generate. (default " + defaultNumAttributes() + ")", "N", 1, "-N <num>"));
    

    result.add(new Option("\tThe cardinality of the attributes and the class. (default " + defaultCardinality() + ")", "C", 1, "-C <num>"));
    


    return result.elements();
  }
  










































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    Vector<String> list = new Vector();
    
    list.add("-N");
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      list.add(tmpStr);
    } else {
      list.add("" + defaultNumAttributes());
    }
    

    list.add("-M");
    list.add("" + getNumExamples());
    
    list.add("-S");
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      list.add(tmpStr);
    } else {
      list.add("" + defaultSeed());
    }
    
    list.add("-A");
    tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0) {
      list.add(tmpStr);
    } else {
      list.add("" + defaultNumArcs());
    }
    
    list.add("-C");
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      list.add(tmpStr);
    } else {
      list.add("" + defaultCardinality());
    }
    
    setGeneratorOptions(list);
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = removeBlacklist(super.getOptions());
    Collections.addAll(result, options);
    

    options = getGenerator().getOptions();
    
    result.add("-N");
    result.add("" + getNumAttributes());
    
    result.add("-S");
    result.add("" + getSeed());
    try
    {
      result.add("-A");
      result.add(Utils.getOption('A', options));
    } catch (Exception e) {
      e.printStackTrace();
    }
    try
    {
      result.add("-C");
      result.add(Utils.getOption('C', options));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  protected void setGeneratorOptions(BayesNetGenerator generator, Vector<String> options)
  {
    try
    {
      generator.setOptions((String[])options.toArray(new String[options.size()]));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  protected BayesNetGenerator getGenerator()
  {
    if (m_Generator == null) {
      m_Generator = new BayesNetGenerator();
    }
    
    return m_Generator;
  }
  




  protected void setGeneratorOptions(Vector<String> options)
  {
    setGeneratorOptions(getGenerator(), options);
  }
  












  protected void setGeneratorOption(BayesNetGenerator generator, String option, String value)
  {
    try
    {
      String[] options = generator.getOptions();
      Utils.getOption(option, options);
      

      Vector<String> list = new Vector();
      for (int i = 0; i < options.length; i++) {
        if (options[i].length() != 0) {
          list.add(options[i]);
        }
      }
      list.add("-" + option);
      list.add(value);
      setGeneratorOptions(generator, list);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  





  protected void setGeneratorOption(String option, String value)
  {
    setGeneratorOption(getGenerator(), option, value);
  }
  




  protected int defaultNumAttributes()
  {
    return 10;
  }
  




  public void setNumAttributes(int numAttributes)
  {
    setGeneratorOption("N", "" + numAttributes);
  }
  






  public int getNumAttributes()
  {
    int result = -1;
    try {
      result = Integer.parseInt(Utils.getOption('N', getGenerator().getOptions()));
    }
    catch (Exception e) {
      e.printStackTrace();
      result = -1;
    }
    
    return result;
  }
  





  public String numAttributesTipText()
  {
    return "The number of attributes the generated data will contain (including class attribute), ie the number of nodes in the bayesian net.";
  }
  




  protected int defaultCardinality()
  {
    return 2;
  }
  




  public void setCardinality(int value)
  {
    setGeneratorOption("C", "" + value);
  }
  






  public int getCardinality()
  {
    int result = -1;
    try {
      result = Integer.parseInt(Utils.getOption('C', getGenerator().getOptions()));
    }
    catch (Exception e) {
      e.printStackTrace();
      result = -1;
    }
    
    return result;
  }
  





  public String cardinalityTipText()
  {
    return "The cardinality of the attributes, incl the class attribute.";
  }
  




  protected int defaultNumArcs()
  {
    return 20;
  }
  








  public void setNumArcs(int value)
  {
    int nodes = getNumAttributes();
    int minArcs = nodes - 1;
    int maxArcs = nodes * (nodes - 1) / 2;
    
    if (value > maxArcs) {
      throw new IllegalArgumentException("Number of arcs should be at most nodes * (nodes - 1) / 2 = " + maxArcs + " instead of " + value + " (nodes = numAttributes)!");
    }
    
    if (value < minArcs) {
      throw new IllegalArgumentException("Number of arcs should be at least (nodes - 1) = " + minArcs + " instead of " + value + " (nodes = numAttributes)!");
    }
    

    setGeneratorOption("A", "" + value);
  }
  







  public int getNumArcs()
  {
    int result = -1;
    try {
      result = Integer.parseInt(Utils.getOption('A', getGenerator().getOptions()));
    }
    catch (Exception e) {
      e.printStackTrace();
      result = -1;
    }
    
    return result;
  }
  





  public String numArcsTipText()
  {
    return "The number of arcs in the bayesian net, at most: n * (n - 1) / 2 and at least: (n - 1); with n = numAttributes";
  }
  





  public void setNumExamples(int numExamples)
  {
    super.setNumExamples(numExamples);
    setGeneratorOption("M", "" + numExamples);
  }
  







  public int getNumExamples()
  {
    int result = -1;
    try {
      result = Integer.parseInt(Utils.getOption('M', getGenerator().getOptions()));
    }
    catch (Exception e) {
      e.printStackTrace();
      result = -1;
    }
    
    return result;
  }
  







  public int getSeed()
  {
    int result = -1;
    try {
      result = Integer.parseInt(Utils.getOption('S', getGenerator().getOptions()));
    }
    catch (Exception e) {
      e.printStackTrace();
      result = -1;
    }
    
    return result;
  }
  





  public void setSeed(int newSeed)
  {
    super.setSeed(newSeed);
    setGeneratorOption("S", "" + newSeed);
  }
  






  public boolean getSingleModeFlag()
    throws Exception
  {
    return false;
  }
  










  public Instances defineDataFormat()
    throws Exception
  {
    BayesNetGenerator bng = new BayesNetGenerator();
    bng.setOptions(getGenerator().getOptions());
    setGeneratorOption(bng, "M", "1");
    bng.generateRandomNetwork();
    bng.generateInstances();
    m_Instances.renameAttribute(0, "class");
    m_Instances.setRelationName(getRelationNameToUse());
    
    return m_Instances;
  }
  







  public Instance generateExample()
    throws Exception
  {
    throw new Exception("Cannot generate examples one-by-one!");
  }
  









  public Instances generateExamples()
    throws Exception
  {
    getGenerator().setOptions(getGenerator().getOptions());
    getGenerator().generateRandomNetwork();
    getGenerator().generateInstances();
    getGeneratorm_Instances.renameAttribute(0, "class");
    getGeneratorm_Instances.setRelationName(getRelationNameToUse());
    
    return getGeneratorm_Instances;
  }
  







  public String generateStart()
  {
    return "";
  }
  






  public String generateFinished()
    throws Exception
  {
    return "";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11753 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new BayesNet(), args);
  }
}
