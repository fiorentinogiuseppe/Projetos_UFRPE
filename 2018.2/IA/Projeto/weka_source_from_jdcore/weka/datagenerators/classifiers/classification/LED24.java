package weka.datagenerators.classifiers.classification;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.datagenerators.ClassificationGenerator;


























































































public class LED24
  extends ClassificationGenerator
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -7880209100415868737L;
  protected double m_NoisePercent;
  protected static final int[][] m_originalInstances = { { 1, 1, 1, 0, 1, 1, 1 }, { 0, 0, 1, 0, 0, 1, 0 }, { 1, 0, 1, 1, 1, 0, 1 }, { 1, 0, 1, 1, 0, 1, 1 }, { 0, 1, 1, 1, 0, 1, 0 }, { 1, 1, 0, 1, 0, 1, 1 }, { 1, 1, 0, 1, 1, 1, 1 }, { 1, 0, 1, 0, 0, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 0, 1, 1 } };
  






  protected int m_numIrrelevantAttributes = 17;
  




  public LED24()
  {
    setNoisePercent(defaultNoisePercent());
  }
  





  public String globalInfo()
  {
    return "This generator produces data for a display with 7 LEDs. The original output consists of 10 concepts and 7 boolean attributes. Here, in addition to the 7 necessary boolean attributes, 17 other, irrelevant boolean attributes with random values are added to make it harder. By default 10 percent of noise are added to the data.\n\nMore information can be found here:\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INBOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "L. Breiman J.H. Friedman R.A. Olshen and C.J. Stone");
    result.setValue(TechnicalInformation.Field.YEAR, "1984");
    result.setValue(TechnicalInformation.Field.TITLE, "Classification and Regression Trees");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Wadsworth International Group");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Belmont, California");
    result.setValue(TechnicalInformation.Field.PAGES, "43-49");
    result.setValue(TechnicalInformation.Field.ISBN, "0412048418");
    result.setValue(TechnicalInformation.Field.URL, "http://www.ics.uci.edu/~mlearn/databases/led-display-creator/");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.add(new Option("\tThe noise percentage. (default " + defaultNoisePercent() + ")", "N", 1, "-N <num>"));
    



    return result.elements();
  }
  

































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNoisePercent(Double.parseDouble(tmpStr));
    } else {
      setNoisePercent(defaultNoisePercent());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-N");
    result.add("" + getNoisePercent());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected double defaultNoisePercent()
  {
    return 10.0D;
  }
  




  public double getNoisePercent()
  {
    return m_NoisePercent;
  }
  




  public void setNoisePercent(double value)
  {
    if ((value >= 0.0D) && (value <= 100.0D)) {
      m_NoisePercent = value;
    } else {
      throw new IllegalArgumentException("Noise percent must be in [0,100] (provided: " + value + ")!");
    }
  }
  





  public String noisePercentTipText()
  {
    return "The noise percent: 0 <= perc <= 100.";
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
    

    setNumExamplesAct(getNumExamples());
    

    FastVector atts = new FastVector();
    
    for (int n = 1; n <= 24; n++) {
      FastVector attValues = new FastVector();
      for (int i = 0; i < 2; i++)
        attValues.addElement("" + i);
      atts.addElement(new Attribute("att" + n, attValues));
    }
    
    FastVector attValues = new FastVector();
    for (int i = 0; i < 10; i++)
      attValues.addElement("" + i);
    atts.addElement(new Attribute("class", attValues));
    

    m_DatasetFormat = new Instances(getRelationNameToUse(), atts, 0);
    
    return m_DatasetFormat;
  }
  












  public Instance generateExample()
    throws Exception
  {
    Instance result = null;
    Random random = getRandom();
    
    if (m_DatasetFormat == null) {
      throw new Exception("Dataset format not defined.");
    }
    double[] atts = new double[m_DatasetFormat.numAttributes()];
    int selected = random.nextInt(10);
    for (int i = 0; i < 7; i++) {
      if (1 + random.nextInt(100) <= getNoisePercent()) {
        atts[i] = (m_originalInstances[selected][i] == 0 ? 1.0D : 0.0D);
      } else {
        atts[i] = m_originalInstances[selected][i];
      }
    }
    for (i = 0; i < m_numIrrelevantAttributes; i++) {
      atts[(i + 7)] = random.nextInt(2);
    }
    atts[(atts.length - 1)] = selected;
    

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
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new LED24(), args);
  }
}
