package weka.datagenerators.clusterers;

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
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.datagenerators.ClusterGenerator;

















































































































































public class BIRCHCluster
  extends ClusterGenerator
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -334820527230755027L;
  protected int m_NumClusters;
  private int m_MinInstNum;
  private int m_MaxInstNum;
  private double m_MinRadius;
  private double m_MaxRadius;
  public static final int GRID = 0;
  public static final int SINE = 1;
  public static final int RANDOM = 2;
  public static final Tag[] TAGS_PATTERN = { new Tag(0, "Grid"), new Tag(1, "Sine"), new Tag(2, "Random") };
  


  private int m_Pattern;
  


  private double m_DistMult;
  

  private int m_NumCycles;
  

  public static final int ORDERED = 0;
  

  public static final int RANDOMIZED = 1;
  

  public static final Tag[] TAGS_INPUTORDER = { new Tag(0, "ordered"), new Tag(1, "randomized") };
  



  private int m_InputOrder;
  


  private double m_NoiseRate;
  


  private FastVector m_ClusterList;
  


  private int m_GridSize;
  


  private double m_GridWidth;
  



  private class Cluster
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -8336901069823498140L;
    


    private int m_InstNum;
    


    private double m_Radius;
    


    private double[] m_Center;
    



    private Cluster(int instNum, double radius, Random random)
    {
      m_InstNum = instNum;
      m_Radius = radius;
      m_Center = new double[getNumAttributes()];
      for (int i = 0; i < getNumAttributes(); i++) {
        m_Center[i] = (random.nextDouble() * m_NumClusters);
      }
    }
    











    private Cluster(int instNum, double radius, int[] gridVector, double gridWidth)
    {
      m_InstNum = instNum;
      m_Radius = radius;
      m_Center = new double[getNumAttributes()];
      for (int i = 0; i < getNumAttributes(); i++) {
        m_Center[i] = ((gridVector[i] + 1.0D) * gridWidth);
      }
    }
    





    private int getInstNum()
    {
      return m_InstNum;
    }
    




    private double getRadius()
    {
      return m_Radius;
    }
    




    private double getVariance()
    {
      return Math.pow(m_Radius, 2.0D) / 2.0D;
    }
    




    private double getStdDev()
    {
      return m_Radius / Math.pow(2.0D, 0.5D);
    }
    




    private double[] getCenter()
    {
      return m_Center;
    }
    





    private double getCenterValue(int dimension)
      throws Exception
    {
      if (dimension >= m_Center.length) {
        throw new Exception("Current system has only " + m_Center.length + " dimensions.");
      }
      return m_Center[dimension];
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.8 $");
    }
  }
  




  private class GridVector
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -1900309948991039522L;
    



    private int[] m_GridVector;
    


    private int m_Base;
    


    private int m_Size;
    



    private GridVector(int numDim, int base)
    {
      m_Size = numDim;
      m_Base = base;
      m_GridVector = new int[numDim];
      for (int i = 0; i < numDim; i++) {
        m_GridVector[i] = 0;
      }
    }
    



    private int[] getGridVector()
    {
      return m_GridVector;
    }
    





    private boolean overflow(int digit)
    {
      return digit == 0;
    }
    






    private int addOne(int digit)
    {
      int value = digit + 1;
      if (value >= m_Base) value = 0;
      return value;
    }
    


    private void addOne()
    {
      m_GridVector[0] = addOne(m_GridVector[0]);
      int i = 1;
      while ((overflow(m_GridVector[(i - 1)])) && (i < m_Size)) {
        m_GridVector[i] = addOne(m_GridVector[i]);
        i++;
      }
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.8 $");
    }
  }
  




  public BIRCHCluster()
  {
    setNumClusters(defaultNumClusters());
    setMinInstNum(defaultMinInstNum());
    setMaxInstNum(defaultMaxInstNum());
    setMinRadius(defaultMinRadius());
    setMaxRadius(defaultMaxRadius());
    setPattern(defaultPattern());
    setDistMult(defaultDistMult());
    setNumCycles(defaultNumCycles());
    setInputOrder(defaultInputOrder());
    setNoiseRate(defaultNoiseRate());
  }
  





  public String globalInfo()
  {
    return "Cluster data generator designed for the BIRCH System\n\nDataset is generated with instances in K clusters.\nInstances are 2-d data points.\nEach cluster is characterized by the number of data points in itits radius and its center. The location of the cluster centers isdetermined by the pattern parameter. Three patterns are currentlysupported grid, sine and random.\n\nFor more information refer to:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Tian Zhang and Raghu Ramakrishnan and Miron Livny");
    result.setValue(TechnicalInformation.Field.TITLE, "BIRCH: An Efficient Data Clustering Method for Very Large Databases");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ACM SIGMOD International Conference on Management of Data");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "103-114");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe number of clusters (default " + defaultNumClusters() + ")", "k", 1, "-k <num>"));
    



    result.addElement(new Option("\tSet pattern to grid (default is random).\n\tThis flag cannot be used at the same time as flag I.\n\tThe pattern is random, if neither flag G nor flag I is set.", "G", 0, "-G"));
    




    result.addElement(new Option("\tSet pattern to sine (default is random).\n\tThis flag cannot be used at the same time as flag I.\n\tThe pattern is random, if neither flag G nor flag I is set.", "I", 0, "-I"));
    




    result.addElement(new Option("\tThe range of number of instances per cluster (default " + defaultMinInstNum() + ".." + defaultMaxInstNum() + ").\n" + "\tLower number must be between 0 and 2500,\n" + "\tupper number must be between 50 and 2500.", "N", 1, "-N <num>..<num>"));
    





    result.addElement(new Option("\tThe range of radius per cluster (default " + defaultMinRadius() + ".." + defaultMaxRadius() + ").\n" + "\tLower number must be between 0 and SQRT(2), \n" + "\tupper number must be between SQRT(2) and SQRT(32).", "R", 1, "-R <num>..<num>"));
    





    result.addElement(new Option("\tThe distance multiplier (default " + defaultDistMult() + ").", "M", 1, "-M <num>"));
    



    result.addElement(new Option("\tThe number of cycles (default " + defaultNumCycles() + ").", "C", 1, "-C <num>"));
    



    result.addElement(new Option("\tFlag for input order is ORDERED. If flag is not set then \n\tinput order is RANDOMIZED. RANDOMIZED is currently not \n\timplemented, therefore is the input order always ORDERED.", "O", 0, "-O"));
    




    result.addElement(new Option("\tThe noise rate in percent (default " + defaultNoiseRate() + ").\n" + "\tCan be between 0% and 30%. (Remark: The original \n" + "\talgorithm only allows noise up to 10%.)", "P", 1, "-P <num>"));
    





    return result.elements();
  }
  














































































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('k', options);
    if (tmpStr.length() != 0) {
      setNumClusters(Integer.parseInt(tmpStr));
    } else {
      setNumClusters(defaultNumClusters());
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setInstNums(tmpStr);
    } else {
      setInstNums(defaultMinInstNum() + ".." + defaultMaxInstNum());
    }
    tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setRadiuses(tmpStr);
    } else {
      setRadiuses(defaultMinRadius() + ".." + defaultMaxRadius());
    }
    boolean grid = Utils.getFlag('G', options);
    boolean sine = Utils.getFlag('I', options);
    
    if ((grid) && (sine)) {
      throw new Exception("Flags -G and -I can only be set mutually exclusiv.");
    }
    setPattern(new SelectedTag(2, TAGS_PATTERN));
    if (grid)
      setPattern(new SelectedTag(0, TAGS_PATTERN));
    if (sine) {
      setPattern(new SelectedTag(1, TAGS_PATTERN));
    }
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      if (!grid)
        throw new Exception("Option M can only be used with GRID pattern.");
      setDistMult(Double.parseDouble(tmpStr));
    }
    else {
      setDistMult(defaultDistMult());
    }
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      if (!sine)
        throw new Exception("Option C can only be used with SINE pattern.");
      setNumCycles(Integer.parseInt(tmpStr));
    }
    else {
      setNumCycles(defaultNumCycles());
    }
    
    if (Utils.getFlag('O', options)) {
      setInputOrder(new SelectedTag(0, TAGS_INPUTORDER));
    } else {
      setInputOrder(defaultInputOrder());
    }
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setNoiseRate(Double.parseDouble(tmpStr));
    } else {
      setNoiseRate(defaultNoiseRate());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-k");
    result.add("" + getNumClusters());
    
    result.add("-N");
    result.add("" + getInstNums());
    
    result.add("-R");
    result.add("" + getRadiuses());
    
    if (m_Pattern == 0) {
      result.add("-G");
      
      result.add("-M");
      result.add("" + getDistMult());
    }
    
    if (m_Pattern == 1) {
      result.add("-I");
      
      result.add("-C");
      result.add("" + getNumCycles());
    }
    
    if (getOrderedFlag()) {
      result.add("-O");
    }
    result.add("-P");
    result.add("" + getNoiseRate());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected int defaultNumClusters()
  {
    return 4;
  }
  



  public void setNumClusters(int numClusters)
  {
    m_NumClusters = numClusters;
  }
  



  public int getNumClusters()
  {
    return m_NumClusters;
  }
  





  public String numClustersTipText()
  {
    return "The number of clusters to generate.";
  }
  





  protected void setInstNums(String fromTo)
  {
    int i = fromTo.indexOf("..");
    String from = fromTo.substring(0, i);
    setMinInstNum(Integer.parseInt(from));
    String to = fromTo.substring(i + 2, fromTo.length());
    setMaxInstNum(Integer.parseInt(to));
  }
  





  protected String getInstNums()
  {
    String fromTo = "" + getMinInstNum() + ".." + getMaxInstNum();
    

    return fromTo;
  }
  





  protected String instNumsTipText()
  {
    return "The upper and lowet boundary for instances per cluster.";
  }
  




  protected int defaultMinInstNum()
  {
    return 1;
  }
  




  public int getMinInstNum()
  {
    return m_MinInstNum;
  }
  




  public void setMinInstNum(int newMinInstNum)
  {
    m_MinInstNum = newMinInstNum;
  }
  





  public String minInstNumTipText()
  {
    return "The lower boundary for instances per cluster.";
  }
  




  protected int defaultMaxInstNum()
  {
    return 50;
  }
  




  public int getMaxInstNum()
  {
    return m_MaxInstNum;
  }
  




  public void setMaxInstNum(int newMaxInstNum)
  {
    m_MaxInstNum = newMaxInstNum;
  }
  





  public String maxInstNumTipText()
  {
    return "The upper boundary for instances per cluster.";
  }
  





  protected void setRadiuses(String fromTo)
  {
    int i = fromTo.indexOf("..");
    String from = fromTo.substring(0, i);
    setMinRadius(Double.valueOf(from).doubleValue());
    String to = fromTo.substring(i + 2, fromTo.length());
    setMaxRadius(Double.valueOf(to).doubleValue());
  }
  





  protected String getRadiuses()
  {
    String fromTo = "" + Utils.doubleToString(getMinRadius(), 2) + ".." + Utils.doubleToString(getMaxRadius(), 2);
    

    return fromTo;
  }
  





  protected String radiusesTipText()
  {
    return "The upper and lower boundary for the radius of the clusters.";
  }
  




  protected double defaultMinRadius()
  {
    return 0.1D;
  }
  




  public double getMinRadius()
  {
    return m_MinRadius;
  }
  




  public void setMinRadius(double newMinRadius)
  {
    m_MinRadius = newMinRadius;
  }
  





  public String minRadiusTipText()
  {
    return "The lower boundary for the radius of the clusters.";
  }
  




  protected double defaultMaxRadius()
  {
    return Math.sqrt(2.0D);
  }
  




  public double getMaxRadius()
  {
    return m_MaxRadius;
  }
  




  public void setMaxRadius(double newMaxRadius)
  {
    m_MaxRadius = newMaxRadius;
  }
  





  public String maxRadiusTipText()
  {
    return "The upper boundary for the radius of the clusters.";
  }
  




  protected SelectedTag defaultPattern()
  {
    return new SelectedTag(2, TAGS_PATTERN);
  }
  




  public SelectedTag getPattern()
  {
    return new SelectedTag(m_Pattern, TAGS_PATTERN);
  }
  




  public void setPattern(SelectedTag value)
  {
    if (value.getTags() == TAGS_PATTERN) {
      m_Pattern = value.getSelectedTag().getID();
    }
  }
  




  public String patternTipText()
  {
    return "The pattern for generating the data.";
  }
  




  protected double defaultDistMult()
  {
    return 4.0D;
  }
  




  public double getDistMult()
  {
    return m_DistMult;
  }
  




  public void setDistMult(double newDistMult)
  {
    m_DistMult = newDistMult;
  }
  





  public String distMultTipText()
  {
    return "The distance multiplier (in combination with the 'Grid' pattern).";
  }
  




  protected int defaultNumCycles()
  {
    return 4;
  }
  




  public int getNumCycles()
  {
    return m_NumCycles;
  }
  




  public void setNumCycles(int newNumCycles)
  {
    m_NumCycles = newNumCycles;
  }
  





  public String numCyclesTipText()
  {
    return "The number of cycles to use (in combination with the 'Sine' pattern).";
  }
  




  protected SelectedTag defaultInputOrder()
  {
    return new SelectedTag(0, TAGS_INPUTORDER);
  }
  




  public SelectedTag getInputOrder()
  {
    return new SelectedTag(m_InputOrder, TAGS_INPUTORDER);
  }
  




  public void setInputOrder(SelectedTag value)
  {
    if (value.getTags() == TAGS_INPUTORDER) {
      m_InputOrder = value.getSelectedTag().getID();
    }
  }
  




  public String inputOrderTipText()
  {
    return "The input order to use.";
  }
  




  public boolean getOrderedFlag()
  {
    return m_InputOrder == 0;
  }
  




  protected double defaultNoiseRate()
  {
    return 0.0D;
  }
  




  public double getNoiseRate()
  {
    return m_NoiseRate;
  }
  




  public void setNoiseRate(double newNoiseRate)
  {
    m_NoiseRate = newNoiseRate;
  }
  





  public String noiseRateTipText()
  {
    return "The noise rate to use.";
  }
  




  public boolean getSingleModeFlag()
  {
    return false;
  }
  





  public Instances defineDataFormat()
    throws Exception
  {
    Random random = new Random(getSeed());
    setRandom(random);
    
    FastVector attributes = new FastVector(3);
    
    boolean classFlag = getClassFlag();
    
    FastVector classValues = null;
    if (classFlag) { classValues = new FastVector(m_NumClusters);
    }
    
    for (int i = 0; i < getNumAttributes(); i++) {
      Attribute attribute = new Attribute("X" + i);
      attributes.addElement(attribute);
    }
    
    if (classFlag) {
      for (int i = 0; i < m_NumClusters; i++)
        classValues.addElement("c" + i);
      Attribute attribute = new Attribute("class", classValues);
      attributes.addElement(attribute);
    }
    
    Instances dataset = new Instances(getRelationNameToUse(), attributes, 0);
    if (classFlag) {
      dataset.setClassIndex(getNumAttributes());
    }
    
    Instances format = new Instances(dataset, 0);
    setDatasetFormat(format);
    
    m_ClusterList = defineClusters(random);
    

    return dataset;
  }
  





  public Instance generateExample()
    throws Exception
  {
    throw new Exception("Examples cannot be generated one by one.");
  }
  





  public Instances generateExamples()
    throws Exception
  {
    Random random = getRandom();
    Instances data = getDatasetFormat();
    if (data == null) { throw new Exception("Dataset format not defined.");
    }
    
    if (getOrderedFlag()) {
      data = generateExamples(random, data);
    } else {
      throw new Exception("RANDOMIZED is not yet implemented.");
    }
    return data;
  }
  







  public Instances generateExamples(Random random, Instances format)
    throws Exception
  {
    Instance example = null;
    
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    int cNum = 0;
    Enumeration enm = m_ClusterList.elements();
    for (; enm.hasMoreElements(); cNum++) {
      Cluster cl = (Cluster)enm.nextElement();
      double stdDev = cl.getStdDev();
      int instNum = cl.getInstNum();
      double[] center = cl.getCenter();
      String cName = "c" + cNum;
      
      for (int i = 0; i < instNum; i++)
      {
        example = generateInstance(format, random, stdDev, center, cName);
        

        if (example != null)
          example.setDataset(format);
        format.add(example);
      }
    }
    
    return format;
  }
  















  private Instance generateInstance(Instances format, Random randomG, double stdDev, double[] center, String cName)
  {
    int numAtts = getNumAttributes();
    if (getClassFlag()) {
      numAtts++;
    }
    Instance example = new Instance(numAtts);
    example.setDataset(format);
    
    for (int i = 0; i < getNumAttributes(); i++) {
      example.setValue(i, randomG.nextGaussian() * stdDev + center[i]);
    }
    if (getClassFlag()) {
      example.setClassValue(cName);
    }
    return example;
  }
  







  private FastVector defineClusters(Random random)
    throws Exception
  {
    if (m_Pattern == 0) {
      return defineClustersGRID(random);
    }
    return defineClustersRANDOM(random);
  }
  







  private FastVector defineClustersGRID(Random random)
    throws Exception
  {
    FastVector clusters = new FastVector(m_NumClusters);
    double diffInstNum = m_MaxInstNum - m_MinInstNum;
    double minInstNum = m_MinInstNum;
    double diffRadius = m_MaxRadius - m_MinRadius;
    


    double gs = Math.pow(m_NumClusters, 1.0D / getNumAttributes());
    
    if (gs - (int)gs > 0.0D)
      m_GridSize = ((int)(gs + 1.0D)); else {
      m_GridSize = ((int)gs);
    }
    
    m_GridWidth = ((m_MaxRadius + m_MinRadius) / 2.0D * m_DistMult);
    




    GridVector gv = new GridVector(getNumAttributes(), m_GridSize, null);
    
    for (int i = 0; i < m_NumClusters; i++) {
      int instNum = (int)(random.nextDouble() * diffInstNum + minInstNum);
      
      double radius = random.nextDouble() * diffRadius + m_MinRadius;
      

      Cluster cluster = new Cluster(instNum, radius, gv.getGridVector(), m_GridWidth, null);
      
      clusters.addElement(cluster);
      gv.addOne();
    }
    return clusters;
  }
  







  private FastVector defineClustersRANDOM(Random random)
    throws Exception
  {
    FastVector clusters = new FastVector(m_NumClusters);
    double diffInstNum = m_MaxInstNum - m_MinInstNum;
    double minInstNum = m_MinInstNum;
    double diffRadius = m_MaxRadius - m_MinRadius;
    

    for (int i = 0; i < m_NumClusters; i++) {
      int instNum = (int)(random.nextDouble() * diffInstNum + minInstNum);
      
      double radius = random.nextDouble() * diffRadius + m_MinRadius;
      

      Cluster cluster = new Cluster(instNum, radius, random, null);
      clusters.addElement(cluster);
    }
    return clusters;
  }
  






  public String generateFinished()
    throws Exception
  {
    return "";
  }
  





  public String generateStart()
  {
    StringBuffer docu = new StringBuffer();
    
    int sumInst = 0;
    int cNum = 0;
    Enumeration enm = m_ClusterList.elements();
    for (; enm.hasMoreElements(); cNum++) {
      Cluster cl = (Cluster)enm.nextElement();
      docu.append("%\n");
      docu.append("% Cluster: c" + cNum + "\n");
      docu.append("% ----------------------------------------------\n");
      docu.append("% StandardDeviation: " + Utils.doubleToString(cl.getStdDev(), 2) + "\n");
      
      docu.append("% Number of instances: " + cl.getInstNum() + "\n");
      
      sumInst += cl.getInstNum();
      double[] center = cl.getCenter();
      docu.append("% ");
      for (int i = 0; i < center.length - 1; i++) {
        docu.append(Utils.doubleToString(center[i], 2) + ", ");
      }
      docu.append(Utils.doubleToString(center[(center.length - 1)], 2) + "\n");
    }
    docu.append("%\n% ----------------------------------------------\n");
    docu.append("% Total number of instances: " + sumInst + "\n");
    docu.append("%                            in " + cNum + " clusters\n");
    docu.append("% Pattern chosen           : ");
    if (m_Pattern == 0) {
      docu.append("GRID, distance multiplier = " + Utils.doubleToString(m_DistMult, 2) + "\n");

    }
    else if (m_Pattern == 1) {
      docu.append("SINE\n");
    } else {
      docu.append("RANDOM\n");
    }
    return docu.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new BIRCHCluster(), args);
  }
}
