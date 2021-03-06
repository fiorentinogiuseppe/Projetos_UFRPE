package weka.datagenerators.classifiers.classification;

import java.io.PrintStream;
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
import weka.datagenerators.ClassificationGenerator;























































































public class RandomRBF
  extends ClassificationGenerator
{
  static final long serialVersionUID = 6069033710635728720L;
  protected int m_NumAttributes;
  protected int m_NumClasses;
  protected int m_NumCentroids;
  protected double[][] m_centroids;
  protected int[] m_centroidClasses;
  protected double[] m_centroidWeights;
  protected double[] m_centroidStdDevs;
  
  public RandomRBF()
  {
    setNumAttributes(defaultNumAttributes());
    setNumClasses(defaultNumClasses());
    setNumCentroids(defaultNumCentroids());
  }
  





  public String globalInfo()
  {
    return "RandomRBF data is generated by first creating a random set of centers for each class. Each center is randomly assigned a weight, a central point per attribute, and a standard deviation. To generate new instances, a center is chosen at random taking the weights of each center into consideration. Attribute values are randomly generated and offset from the center, where the overall vector has been scaled so that its length equals a value sampled randomly from the Gaussian distribution of the center. The particular center chosen determines the class of the instance.\n RandomRBF data contains only numeric attributes as it is non-trivial to include nominal values.";
  }
  















  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe number of attributes (default " + defaultNumAttributes() + ").", "a", 1, "-a <num>"));
    



    result.addElement(new Option("\tThe number of classes (default " + defaultNumClasses() + ")", "c", 1, "-c <num>"));
    


    result.add(new Option("\tThe number of centroids to use. (default " + defaultNumCentroids() + ")", "C", 1, "-C <num>"));
    



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
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setNumCentroids(Integer.parseInt(tmpStr));
    } else {
      setNumCentroids(defaultNumCentroids());
    }
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
    
    result.add("-C");
    result.add("" + getNumCentroids());
    
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
  




  protected int defaultNumCentroids()
  {
    return 50;
  }
  




  public int getNumCentroids()
  {
    return m_NumCentroids;
  }
  




  public void setNumCentroids(int value)
  {
    if (value > 0) {
      m_NumCentroids = value;
    } else {
      System.out.println("At least 1 centroid is necessary (provided: " + value + ")!");
    }
  }
  





  public String numCentroidsTipText()
  {
    return "The number of centroids to use.";
  }
  





  public boolean getSingleModeFlag()
    throws Exception
  {
    return true;
  }
  













  protected int chooseRandomIndexBasedOnProportions(double[] proportionArray, Random random)
  {
    double probSum = Utils.sum(proportionArray);
    double val = random.nextDouble() * probSum;
    int index = 0;
    double sum = 0.0D;
    
    while ((sum <= val) && (index < proportionArray.length)) {
      sum += proportionArray[(index++)];
    }
    return index - 1;
  }
  














  public Instances defineDataFormat()
    throws Exception
  {
    m_Random = new Random(getSeed());
    Random rand = getRandom();
    

    setNumExamplesAct(getNumExamples());
    

    m_centroids = new double[getNumCentroids()][getNumAttributes()];
    m_centroidClasses = new int[getNumCentroids()];
    m_centroidWeights = new double[getNumCentroids()];
    m_centroidStdDevs = new double[getNumCentroids()];
    
    for (int i = 0; i < getNumCentroids(); i++) {
      for (int j = 0; j < getNumAttributes(); j++)
        m_centroids[i][j] = rand.nextDouble();
      m_centroidClasses[i] = rand.nextInt(getNumClasses());
      m_centroidWeights[i] = rand.nextDouble();
      m_centroidStdDevs[i] = rand.nextDouble();
    }
    

    FastVector atts = new FastVector();
    for (i = 0; i < getNumAttributes(); i++) {
      atts.addElement(new Attribute("a" + i));
    }
    FastVector clsValues = new FastVector();
    for (i = 0; i < getNumClasses(); i++)
      clsValues.addElement("c" + i);
    atts.addElement(new Attribute("class", clsValues));
    
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
    
    int centroid = chooseRandomIndexBasedOnProportions(m_centroidWeights, rand);
    double label = m_centroidClasses[centroid];
    

    double[] atts = new double[getNumAttributes() + 1];
    for (int i = 0; i < getNumAttributes(); i++)
      atts[i] = (rand.nextDouble() * 2.0D - 1.0D);
    atts[(atts.length - 1)] = label;
    
    double magnitude = 0.0D;
    for (i = 0; i < getNumAttributes(); i++) {
      magnitude += atts[i] * atts[i];
    }
    magnitude = Math.sqrt(magnitude);
    double desiredMag = rand.nextGaussian() * m_centroidStdDevs[centroid];
    double scale = desiredMag / magnitude;
    for (i = 0; i < getNumAttributes(); i++) {
      atts[i] *= scale;
      atts[i] += m_centroids[centroid][i];
      result = new Instance(1.0D, atts);
    }
    

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
    StringBuffer result = new StringBuffer();
    
    result.append("%\n");
    result.append("% centroids:\n");
    for (int i = 0; i < getNumCentroids(); i++) {
      result.append("% " + i + ".: " + Utils.arrayToString(m_centroids[i]) + "\n");
    }
    result.append("%\n");
    result.append("% centroidClasses: " + Utils.arrayToString(m_centroidClasses) + "\n");
    
    result.append("%\n");
    result.append("% centroidWeights: " + Utils.arrayToString(m_centroidWeights) + "\n");
    
    result.append("%\n");
    result.append("% centroidStdDevs: " + Utils.arrayToString(m_centroidStdDevs) + "\n");
    
    result.append("%\n");
    
    return result.toString();
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
    runDataGenerator(new RandomRBF(), args);
  }
}
