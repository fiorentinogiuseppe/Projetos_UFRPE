package weka.datagenerators.classifiers.classification;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.datagenerators.ClassificationGenerator;
import weka.datagenerators.Test;















































































































public class RDG1
  extends ClassificationGenerator
{
  static final long serialVersionUID = 7751005204635320414L;
  protected int m_NumAttributes;
  protected int m_NumClasses;
  private int m_MaxRuleSize;
  private int m_MinRuleSize;
  private int m_NumIrrelevant;
  private int m_NumNumeric;
  
  private class RuleList
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 2830125413361938177L;
    private FastVector m_RuleList = null;
    

    double m_ClassValue = 0.0D;
    

    private RuleList() {}
    

    public double getClassValue()
    {
      return m_ClassValue;
    }
    




    public void setClassValue(double newClassValue)
    {
      m_ClassValue = newClassValue;
    }
    




    private void addTest(Test newTest)
    {
      if (m_RuleList == null) {
        m_RuleList = new FastVector();
      }
      m_RuleList.addElement(newTest);
    }
    





    private double classifyInstance(Instance example)
      throws Exception
    {
      boolean passedAllTests = true;
      Enumeration e = m_RuleList.elements();
      while ((passedAllTests) && (e.hasMoreElements())) {
        Test test = (Test)e.nextElement();
        passedAllTests = test.passesTest(example);
      }
      if (passedAllTests) return m_ClassValue;
      return -1.0D;
    }
    




    public String toString()
    {
      StringBuffer str = new StringBuffer();
      str = str.append("  c" + (int)m_ClassValue + " := ");
      Enumeration e = m_RuleList.elements();
      if (e.hasMoreElements()) {
        Test test = (Test)e.nextElement();
        str = str.append(test.toPrologString());
      }
      while (e.hasMoreElements()) {
        Test test = (Test)e.nextElement();
        str = str.append(", " + test.toPrologString());
      }
      return str.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5674 $");
    }
  }
  



















  private boolean m_VoteFlag = false;
  

  private FastVector m_DecisionList = null;
  



  boolean[] m_AttList_Irr;
  



  public RDG1()
  {
    setNumAttributes(defaultNumAttributes());
    setNumClasses(defaultNumClasses());
    setMaxRuleSize(defaultMaxRuleSize());
    setMinRuleSize(defaultMinRuleSize());
    setNumIrrelevant(defaultNumIrrelevant());
    setNumNumeric(defaultNumNumeric());
  }
  





  public String globalInfo()
  {
    return "A data generator that produces data randomly by producing a decision list.\nThe decision list consists of rules.\nInstances are generated randomly one by one. If decision list fails to classify the current instance, a new rule according to this current instance is generated and added to the decision list.\n\nThe option -V switches on voting, which means that at the end of the generation all instances are reclassified to the class value that is supported by the most rules.\n\nThis data generator can generate 'boolean' attributes (= nominal with the values {true, false}) and numeric attributes. The rules can be 'A' or 'NOT A' for boolean values and 'B < random_value' or 'B >= random_value' for numeric values.";
  }
  
















  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe number of attributes (default " + defaultNumAttributes() + ").", "a", 1, "-a <num>"));
    



    result.addElement(new Option("\tThe number of classes (default " + defaultNumClasses() + ")", "c", 1, "-c <num>"));
    


    result.addElement(new Option("\tmaximum size for rules (default " + defaultMaxRuleSize() + ") ", "R", 1, "-R <num>"));
    



    result.addElement(new Option("\tminimum size for rules (default " + defaultMinRuleSize() + ") ", "M", 1, "-M <num>"));
    



    result.addElement(new Option("\tnumber of irrelevant attributes (default " + defaultNumIrrelevant() + ")", "I", 1, "-I <num>"));
    



    result.addElement(new Option("\tnumber of numeric attributes (default " + defaultNumNumeric() + ")", "N", 1, "-N"));
    



    result.addElement(new Option("\tswitch on voting (default is no voting)", "V", 1, "-V"));
    


    return result.elements();
  }
  



















































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('a', options);
    if (tmpStr.length() != 0) {
      setNumAttributes(Integer.parseInt(tmpStr));
    } else {
      setNumAttributes(defaultNumAttributes());
    }
    tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() != 0) {
      setNumClasses(Integer.parseInt(tmpStr));
    } else {
      setNumClasses(defaultNumClasses());
    }
    tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setMaxRuleSize(Integer.parseInt(tmpStr));
    } else {
      setMaxRuleSize(defaultMaxRuleSize());
    }
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setMinRuleSize(Integer.parseInt(tmpStr));
    } else {
      setMinRuleSize(defaultMinRuleSize());
    }
    tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      setNumIrrelevant(Integer.parseInt(tmpStr));
    } else {
      setNumIrrelevant(defaultNumIrrelevant());
    }
    if (getNumAttributes() - getNumIrrelevant() < getMinRuleSize()) {
      throw new Exception("Possible rule size is below minimal rule size.");
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNumNumeric(Integer.parseInt(tmpStr));
    } else {
      setNumNumeric(defaultNumNumeric());
    }
    setVoteFlag(Utils.getFlag('V', options));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-a");
    result.add("" + getNumAttributes());
    
    result.add("-c");
    result.add("" + getNumClasses());
    
    result.add("-N");
    result.add("" + getNumNumeric());
    
    result.add("-I");
    result.add("" + getNumIrrelevant());
    
    result.add("-M");
    result.add("" + getMinRuleSize());
    
    result.add("-R");
    result.add("" + getMaxRuleSize());
    
    if (getVoteFlag()) {
      result.add("-V");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected int defaultNumAttributes()
  {
    return 10;
  }
  



  public void setNumAttributes(int numAttributes)
  {
    m_NumAttributes = numAttributes;
  }
  



  public int getNumAttributes()
  {
    return m_NumAttributes;
  }
  





  public String numAttributesTipText()
  {
    return "The number of attributes the generated data will contain.";
  }
  




  protected int defaultNumClasses()
  {
    return 2;
  }
  



  public void setNumClasses(int numClasses)
  {
    m_NumClasses = numClasses;
  }
  



  public int getNumClasses()
  {
    return m_NumClasses;
  }
  





  public String numClassesTipText()
  {
    return "The number of classes to generate.";
  }
  




  protected int defaultMaxRuleSize()
  {
    return 10;
  }
  




  public int getMaxRuleSize()
  {
    return m_MaxRuleSize;
  }
  




  public void setMaxRuleSize(int newMaxRuleSize)
  {
    m_MaxRuleSize = newMaxRuleSize;
  }
  





  public String maxRuleSizeTipText()
  {
    return "The maximum number of tests in rules.";
  }
  




  protected int defaultMinRuleSize()
  {
    return 1;
  }
  




  public int getMinRuleSize()
  {
    return m_MinRuleSize;
  }
  




  public void setMinRuleSize(int newMinRuleSize)
  {
    m_MinRuleSize = newMinRuleSize;
  }
  





  public String minRuleSizeTipText()
  {
    return "The minimum number of tests in rules.";
  }
  




  protected int defaultNumIrrelevant()
  {
    return 0;
  }
  




  public int getNumIrrelevant()
  {
    return m_NumIrrelevant;
  }
  




  public void setNumIrrelevant(int newNumIrrelevant)
  {
    m_NumIrrelevant = newNumIrrelevant;
  }
  





  public String numIrrelevantTipText()
  {
    return "The number of irrelevant attributes.";
  }
  




  protected int defaultNumNumeric()
  {
    return 0;
  }
  




  public int getNumNumeric()
  {
    return m_NumNumeric;
  }
  




  public void setNumNumeric(int newNumNumeric)
  {
    m_NumNumeric = newNumNumeric;
  }
  





  public String numNumericTipText()
  {
    return "The number of numerical attributes.";
  }
  




  public boolean getVoteFlag()
  {
    return m_VoteFlag;
  }
  




  public void setVoteFlag(boolean newVoteFlag)
  {
    m_VoteFlag = newVoteFlag;
  }
  





  public String voteFlagTipText()
  {
    return "Whether to use voting or not.";
  }
  




  public boolean getSingleModeFlag()
  {
    return !getVoteFlag();
  }
  





  public boolean[] getAttList_Irr()
  {
    return m_AttList_Irr;
  }
  





  public void setAttList_Irr(boolean[] newAttList_Irr)
  {
    m_AttList_Irr = newAttList_Irr;
  }
  





  public String attList_IrrTipText()
  {
    return "The array with the indices of the irrelevant attributes.";
  }
  





  public Instances defineDataFormat()
    throws Exception
  {
    Random random = new Random(getSeed());
    setRandom(random);
    
    m_DecisionList = new FastVector();
    

    setNumExamplesAct(getNumExamples());
    

    Instances dataset = defineDataset(random);
    return dataset;
  }
  




  public Instance generateExample()
    throws Exception
  {
    Random random = getRandom();
    Instances format = getDatasetFormat();
    
    if (format == null)
      throw new Exception("Dataset format not defined.");
    if (getVoteFlag()) {
      throw new Exception("Examples cannot be generated one by one.");
    }
    
    format = generateExamples(1, random, format);
    
    return format.lastInstance();
  }
  




  public Instances generateExamples()
    throws Exception
  {
    Random random = getRandom();
    Instances format = getDatasetFormat();
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    format = generateExamples(getNumExamplesAct(), random, format);
    

    if (getVoteFlag()) {
      format = voteDataset(format);
    }
    return format;
  }
  










  public Instances generateExamples(int num, Random random, Instances format)
    throws Exception
  {
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    for (int i = 0; i < num; i++)
    {
      Instance example = generateExample(random, format);
      

      boolean classDefined = classifyExample(example);
      if (!classDefined)
      {
        example = updateDecisionList(random, example);
      }
      example.setDataset(format);
      format.add(example);
    }
    
    return format;
  }
  









  private Instance updateDecisionList(Random random, Instance example)
    throws Exception
  {
    Instances format = getDatasetFormat();
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    FastVector TestList = generateTestList(random, example);
    
    int maxSize = getMaxRuleSize() < TestList.size() ? getMaxRuleSize() : TestList.size();
    
    int ruleSize = (int)(random.nextDouble() * (maxSize - getMinRuleSize())) + getMinRuleSize();
    


    RuleList newRule = new RuleList(null);
    for (int i = 0; i < ruleSize; i++) {
      int testIndex = (int)(random.nextDouble() * TestList.size());
      Test test = (Test)TestList.elementAt(testIndex);
      
      newRule.addTest(test);
      TestList.removeElementAt(testIndex);
    }
    double newClassValue = 0.0D;
    if (m_DecisionList.size() > 0) {
      RuleList r = (RuleList)m_DecisionList.lastElement();
      double oldClassValue = r.getClassValue();
      
      newClassValue = ((int)oldClassValue + 1) % getNumClasses();
    }
    
    newRule.setClassValue(newClassValue);
    m_DecisionList.addElement(newRule);
    example = (Instance)example.copy();
    example.setDataset(format);
    example.setClassValue(newClassValue);
    return example;
  }
  









  private FastVector generateTestList(Random random, Instance example)
    throws Exception
  {
    Instances format = getDatasetFormat();
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    int numTests = getNumAttributes() - getNumIrrelevant();
    FastVector TestList = new FastVector(numTests);
    boolean[] irrelevant = getAttList_Irr();
    
    for (int i = 0; i < getNumAttributes(); i++) {
      if (irrelevant[i] == 0) {
        Test newTest = null;
        Attribute att = example.attribute(i);
        if (att.isNumeric()) {
          double newSplit = random.nextDouble();
          boolean newNot = newSplit < example.value(i);
          newTest = new Test(i, newSplit, format, newNot);
        } else {
          newTest = new Test(i, example.value(i), format, false);
        }
        TestList.addElement(newTest);
      }
    }
    
    return TestList;
  }
  











  private Instance generateExample(Random random, Instances format)
    throws Exception
  {
    double[] attributes = new double[getNumAttributes() + 1];
    for (int i = 0; i < getNumAttributes(); i++) {
      double value = random.nextDouble();
      if (format.attribute(i).isNumeric()) {
        attributes[i] = value;
      }
      else if (format.attribute(i).isNominal()) {
        attributes[i] = (value > 0.5D ? 1.0D : 0.0D);
      } else {
        throw new Exception("Attribute type is not supported.");
      }
    }
    Instance example = new Instance(1.0D, attributes);
    example.setDataset(format);
    example.setClassMissing();
    
    return example;
  }
  





  private boolean classifyExample(Instance example)
    throws Exception
  {
    double classValue = -1.0D;
    
    Enumeration e = m_DecisionList.elements();
    while ((e.hasMoreElements()) && (classValue < 0.0D)) {
      RuleList rl = (RuleList)e.nextElement();
      classValue = rl.classifyInstance(example);
    }
    if (classValue >= 0.0D) {
      example.setClassValue(classValue);
      return true;
    }
    
    return false;
  }
  










  private Instance votedReclassifyExample(Instance example)
    throws Exception
  {
    int[] classVotes = new int[getNumClasses()];
    for (int i = 0; i < classVotes.length; i++) { classVotes[i] = 0;
    }
    Enumeration e = m_DecisionList.elements();
    while (e.hasMoreElements()) {
      RuleList rl = (RuleList)e.nextElement();
      int classValue = (int)rl.classifyInstance(example);
      if (classValue >= 0) classVotes[classValue] += 1;
    }
    int maxVote = 0;
    int vote = -1;
    for (int i = 0; i < classVotes.length; i++) {
      if (classVotes[i] > maxVote) {
        maxVote = classVotes[i];
        vote = i;
      }
    }
    if (vote >= 0) {
      example.setClassValue(vote);
    } else {
      throw new Exception("Error in instance classification.");
    }
    return example;
  }
  







  private Instances defineDataset(Random random)
    throws Exception
  {
    FastVector attributes = new FastVector();
    
    FastVector nominalValues = new FastVector(2);
    nominalValues.addElement("false");
    nominalValues.addElement("true");
    FastVector classValues = new FastVector(getNumClasses());
    


    boolean[] attList_Irr = defineIrrelevant(random);
    setAttList_Irr(attList_Irr);
    

    int[] attList_Num = defineNumeric(random);
    

    for (int i = 0; i < getNumAttributes(); i++) { Attribute attribute;
      Attribute attribute; if (attList_Num[i] == 0) {
        attribute = new Attribute("a" + i);
      } else
        attribute = new Attribute("a" + i, nominalValues);
      attributes.addElement(attribute);
    }
    for (int i = 0; i < classValues.capacity(); i++)
      classValues.addElement("c" + i);
    Attribute attribute = new Attribute("class", classValues);
    attributes.addElement(attribute);
    
    Instances dataset = new Instances(getRelationNameToUse(), attributes, getNumExamplesAct());
    
    dataset.setClassIndex(getNumAttributes());
    

    Instances format = new Instances(dataset, 0);
    setDatasetFormat(format);
    
    return dataset;
  }
  










  private boolean[] defineIrrelevant(Random random)
  {
    boolean[] irr = new boolean[getNumAttributes()];
    

    for (int i = 0; i < irr.length; i++) {
      irr[i] = false;
    }
    
    int numIrr = 0;
    for (int i = 0; 
        (numIrr < getNumIrrelevant()) && (i < getNumAttributes() * 5); 
        i++) {
      int maybeNext = (int)(random.nextDouble() * irr.length);
      if (irr[maybeNext] == 0) {
        irr[maybeNext] = true;
        numIrr++;
      }
    }
    
    return irr;
  }
  






  private int[] defineNumeric(Random random)
  {
    int[] num = new int[getNumAttributes()];
    

    for (int i = 0; i < num.length; i++) {
      num[i] = 1;
    }
    int numNum = 0;
    for (int i = 0; 
        (numNum < getNumNumeric()) && (i < getNumAttributes() * 5); i++) {
      int maybeNext = (int)(random.nextDouble() * num.length);
      if (num[maybeNext] != 0) {
        num[maybeNext] = 0;
        numNum++;
      }
    }
    
    return num;
  }
  






  public String generateStart()
  {
    return "";
  }
  









  public String generateFinished()
    throws Exception
  {
    StringBuffer dLString = new StringBuffer();
    

    boolean[] attList_Irr = getAttList_Irr();
    Instances format = getDatasetFormat();
    dLString.append("%\n% Number of attributes chosen as irrelevant = " + getNumIrrelevant() + "\n");
    
    for (int i = 0; i < attList_Irr.length; i++) {
      if (attList_Irr[i] != 0) {
        dLString.append("% " + format.attribute(i).name() + "\n");
      }
    }
    dLString.append("%\n% DECISIONLIST (number of rules = " + m_DecisionList.size() + "):\n");
    

    for (int i = 0; i < m_DecisionList.size(); i++) {
      RuleList rl = (RuleList)m_DecisionList.elementAt(i);
      dLString.append("% RULE " + i + ": " + rl.toString() + "\n");
    }
    
    return dLString.toString();
  }
  







  private Instances voteDataset(Instances dataset)
    throws Exception
  {
    for (int i = 0; i < dataset.numInstances(); i++) {
      Instance inst = dataset.firstInstance();
      inst = votedReclassifyExample(inst);
      dataset.add(inst);
      dataset.delete(0);
    }
    
    return dataset;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5674 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new RDG1(), args);
  }
}
