package weka.datagenerators.classifiers.regression;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.AddExpression;


























































































public class Expression
  extends MexicanHat
{
  static final long serialVersionUID = -4237047357682277211L;
  protected String m_Expression;
  protected AddExpression m_Filter;
  protected Instances m_RawData;
  
  public Expression()
  {
    setExpression(defaultExpression());
  }
  





  public String globalInfo()
  {
    return "A data generator for generating y according to a given expression out of randomly generated x.\nE.g., the mexican hat can be generated like this:\n   sin(abs(a1)) / abs(a1)\nIn addition to this function, the amplitude can be changed and gaussian noise can be added.";
  }
  










  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe expression to use for generating y out of x \n\t(default " + defaultExpression() + ").", "E", 1, "-E <expression>"));
    



    return result.elements();
  }
  














































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0) {
      setExpression(tmpStr);
    } else {
      setExpression(defaultExpression());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-E");
    result.add("" + getExpression());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String amplitudeTipText()
  {
    return "The amplitude to multiply the y value with.";
  }
  




  protected String defaultExpression()
  {
    return "sin(abs(a1)) / abs(a1)";
  }
  




  public String getExpression()
  {
    return m_Expression;
  }
  




  public void setExpression(String value)
  {
    if (value.length() != 0) {
      m_Expression = value;
    } else {
      throw new IllegalArgumentException("An expression has to be provided!");
    }
  }
  





  public String expressionTipText()
  {
    return "The expression for generating y out of x.";
  }
  





  public boolean getSingleModeFlag()
    throws Exception
  {
    return true;
  }
  











  public Instances defineDataFormat()
    throws Exception
  {
    FastVector atts = new FastVector();
    atts.addElement(new Attribute("x"));
    
    m_RawData = new Instances(getRelationNameToUse(), atts, 0);
    
    m_Filter = new AddExpression();
    m_Filter.setName("y");
    m_Filter.setExpression(getExpression());
    m_Filter.setInputFormat(m_RawData);
    
    return super.defineDataFormat();
  }
  













  public Instance generateExample()
    throws Exception
  {
    Instance result = null;
    Random rand = getRandom();
    
    if (m_DatasetFormat == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    double x = rand.nextDouble();
    
    x = x * (getMaxRange() - getMinRange()) + getMinRange();
    

    double[] atts = new double[1];
    atts[0] = x;
    Instance inst = new Instance(1.0D, atts);
    m_Filter.input(inst);
    m_Filter.batchFinished();
    inst = m_Filter.output();
    

    double y = inst.value(1) + getAmplitude() * m_NoiseRandom.nextGaussian() * getNoiseRate() * getNoiseVariance();
    



    atts = new double[m_DatasetFormat.numAttributes()];
    
    atts[0] = x;
    atts[1] = y;
    result = new Instance(1.0D, atts);
    

    result.setDataset(m_DatasetFormat);
    
    return result;
  }
  











  public Instances generateExamples()
    throws Exception
  {
    Instances result = new Instances(m_DatasetFormat, 0);
    m_Random = new Random(getSeed());
    
    for (int i = 0; i < getNumExamplesAct(); i++) {
      result.add(generateExample());
    }
    return result;
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
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new Expression(), args);
  }
}
