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
import weka.datagenerators.RegressionGenerator;





















































































public class MexicanHat
  extends RegressionGenerator
{
  static final long serialVersionUID = 4577016375261512975L;
  protected double m_Amplitude;
  protected double m_MinRange;
  protected double m_MaxRange;
  protected double m_NoiseRate;
  protected double m_NoiseVariance;
  protected Random m_NoiseRandom = null;
  




  public MexicanHat()
  {
    setAmplitude(defaultAmplitude());
    setMinRange(defaultMinRange());
    setMaxRange(defaultMaxRange());
    setNoiseRate(defaultNoiseRate());
    setNoiseVariance(defaultNoiseVariance());
  }
  





  public String globalInfo()
  {
    return "A data generator for the simple 'Mexian Hat' function:\n   y = sin|x| / |x|\nIn addition to this simple function, the amplitude can be changed and gaussian noise can be added.";
  }
  








  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe amplitude multiplier (default " + defaultAmplitude() + ").", "A", 1, "-A <num>"));
    



    result.addElement(new Option("\tThe range x is randomly drawn from (default " + defaultMinRange() + ".." + defaultMaxRange() + ").", "R", 1, "-R <num>..<num>"));
    



    result.addElement(new Option("\tThe noise rate (default " + defaultNoiseRate() + ").", "N", 1, "-N <num>"));
    



    result.addElement(new Option("\tThe noise variance (default " + defaultNoiseVariance() + ").", "V", 1, "-V <num>"));
    



    return result.elements();
  }
  










































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0) {
      setAmplitude(Double.parseDouble(tmpStr));
    } else {
      setAmplitude(defaultAmplitude());
    }
    tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setRange(tmpStr);
    } else {
      setRange(defaultMinRange() + ".." + defaultMaxRange());
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNoiseRate(Double.parseDouble(tmpStr));
    } else {
      setNoiseRate(defaultNoiseRate());
    }
    tmpStr = Utils.getOption('V', options);
    if (tmpStr.length() != 0) {
      setNoiseVariance(Double.parseDouble(tmpStr));
    } else {
      setNoiseVariance(defaultNoiseVariance());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = removeBlacklist(super.getOptions());
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-A");
    result.add("" + getAmplitude());
    
    result.add("-R");
    result.add("" + getRange());
    
    result.add("-N");
    result.add("" + getNoiseRate());
    
    result.add("-V");
    result.add("" + getNoiseVariance());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected double defaultAmplitude()
  {
    return 1.0D;
  }
  




  public double getAmplitude()
  {
    return m_Amplitude;
  }
  




  public void setAmplitude(double value)
  {
    m_Amplitude = value;
  }
  





  public String amplitudeTipText()
  {
    return "The amplitude of the mexican hat.";
  }
  





  protected void setRange(String fromTo)
  {
    int i = fromTo.indexOf("..");
    String from = fromTo.substring(0, i);
    setMinRange(Double.valueOf(from).doubleValue());
    String to = fromTo.substring(i + 2, fromTo.length());
    setMaxRange(Double.valueOf(to).doubleValue());
  }
  





  protected String getRange()
  {
    String fromTo = "" + Utils.doubleToString(getMinRange(), 2) + ".." + Utils.doubleToString(getMaxRange(), 2);
    

    return fromTo;
  }
  





  protected String rangeTipText()
  {
    return "The upper and lower boundary for the range x is drawn from randomly.";
  }
  




  protected double defaultMinRange()
  {
    return -10.0D;
  }
  




  public void setMinRange(double value)
  {
    m_MinRange = value;
  }
  




  public double getMinRange()
  {
    return m_MinRange;
  }
  





  public String minRangeTipText()
  {
    return "The lower boundary for the range x is drawn from randomly.";
  }
  




  protected double defaultMaxRange()
  {
    return 10.0D;
  }
  




  public void setMaxRange(double value)
  {
    m_MaxRange = value;
  }
  




  public double getMaxRange()
  {
    return m_MaxRange;
  }
  





  public String maxRangeTipText()
  {
    return "The upper boundary for the range x is drawn from randomly.";
  }
  




  protected double defaultNoiseRate()
  {
    return 0.0D;
  }
  




  public double getNoiseRate()
  {
    return m_NoiseRate;
  }
  




  public void setNoiseRate(double value)
  {
    m_NoiseRate = value;
  }
  





  public String noiseRateTipText()
  {
    return "The gaussian noise rate to use.";
  }
  




  protected double defaultNoiseVariance()
  {
    return 1.0D;
  }
  




  public double getNoiseVariance()
  {
    return m_NoiseVariance;
  }
  




  public void setNoiseVariance(double value)
  {
    if (value > 0.0D) {
      m_NoiseVariance = value;
    } else {
      throw new IllegalArgumentException("Noise variance needs to be > 0 (provided: " + value + ")!");
    }
  }
  





  public String noiseVarianceTipText()
  {
    return "The noise variance to use.";
  }
  





  public boolean getSingleModeFlag()
    throws Exception
  {
    return true;
  }
  










  public Instances defineDataFormat()
    throws Exception
  {
    m_Random = new Random(getSeed());
    m_NoiseRandom = new Random(getSeed());
    

    setNumExamplesAct(getNumExamples());
    

    FastVector atts = new FastVector();
    atts.addElement(new Attribute("x"));
    atts.addElement(new Attribute("y"));
    
    m_DatasetFormat = new Instances(getRelationNameToUse(), atts, 0);
    
    return m_DatasetFormat;
  }
  












  public Instance generateExample()
    throws Exception
  {
    Instance result = null;
    Random rand = getRandom();
    
    if (m_DatasetFormat == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    double[] atts = new double[m_DatasetFormat.numAttributes()];
    

    double x = rand.nextDouble();
    
    x = x * (getMaxRange() - getMinRange()) + getMinRange();
    double y;
    double y;
    if (Utils.eq(x, 0.0D)) {
      y = getAmplitude();
    } else {
      y = getAmplitude() * StrictMath.sin(StrictMath.abs(x)) / StrictMath.abs(x);
    }
    
    y += getAmplitude() * m_NoiseRandom.nextGaussian() * getNoiseRate() * getNoiseVariance();
    


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
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new MexicanHat(), args);
  }
}
