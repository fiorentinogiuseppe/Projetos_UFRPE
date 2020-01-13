package weka.datagenerators.clusterers;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.datagenerators.ClusterDefinition;
import weka.datagenerators.ClusterGenerator;


















































































































public class SubspaceCluster
  extends ClusterGenerator
{
  static final long serialVersionUID = -3454999858505621128L;
  protected double m_NoiseRate;
  protected ClusterDefinition[] m_Clusters;
  protected int[] m_numValues;
  protected double[] m_globalMinValue;
  protected double[] m_globalMaxValue;
  public static final int UNIFORM_RANDOM = 0;
  public static final int TOTAL_UNIFORM = 1;
  public static final int GAUSSIAN = 2;
  public static final Tag[] TAGS_CLUSTERTYPE = { new Tag(0, "uniform/random"), new Tag(1, "total uniform"), new Tag(2, "gaussian") };
  


  public static final int CONTINUOUS = 0;
  


  public static final int INTEGER = 1;
  

  public static final Tag[] TAGS_CLUSTERSUBTYPE = { new Tag(0, "continuous"), new Tag(1, "integer") };
  








  public SubspaceCluster()
  {
    setNoiseRate(defaultNoiseRate());
  }
  





  public String globalInfo()
  {
    return "A data generator that produces data points in hyperrectangular subspace clusters.";
  }
  





  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe noise rate in percent (default " + defaultNoiseRate() + ").\n" + "\tCan be between 0% and 30%. (Remark: The original \n" + "\talgorithm only allows noise up to 10%.)", "P", 1, "-P <num>"));
    





    result.addElement(new Option("\tA cluster definition of class '" + SubspaceClusterDefinition.class.getName().replaceAll(".*\\.", "") + "'\n" + "\t(definition needs to be quoted to be recognized as \n" + "\ta single argument).", "C", 1, "-C <cluster-definition>"));
    





    result.addElement(new Option("", "", 0, "\nOptions specific to " + SubspaceClusterDefinition.class.getName() + ":"));
    



    result.addAll(enumToVector(new SubspaceClusterDefinition(this).listOptions()));
    

    return result.elements();
  }
  











































































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    m_numValues = new int[getNumAttributes()];
    

    for (int i = 0; i < getNumAttributes(); i++) {
      m_numValues[i] = 1;
    }
    String tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setNoiseRate(Double.parseDouble(tmpStr));
    } else {
      setNoiseRate(defaultNoiseRate());
    }
    
    Vector list = new Vector();
    
    int clCount = 0;
    do {
      tmpStr = Utils.getOption('C', options);
      if (tmpStr.length() != 0) {
        clCount++;
        SubspaceClusterDefinition cl = new SubspaceClusterDefinition(this);
        cl.setOptions(Utils.splitOptions(tmpStr));
        list.add(cl);
      }
      
    } while (tmpStr.length() != 0);
    
    m_Clusters = ((ClusterDefinition[])list.toArray(new ClusterDefinition[list.size()]));
    


    getClusters();
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-P");
    result.add("" + getNoiseRate());
    
    for (i = 0; i < getClusters().length; i++) {
      result.add("-C");
      result.add(Utils.joinOptions(getClusters()[i].getOptions()));
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected ClusterDefinition[] getClusters()
  {
    if ((m_Clusters == null) || (m_Clusters.length == 0)) {
      if (m_Clusters != null) {
        System.out.println("NOTE: at least 1 cluster definition is necessary, created default one.");
      }
      m_Clusters = new ClusterDefinition[] { new SubspaceClusterDefinition(this) };
    }
    
    return m_Clusters;
  }
  




  protected int defaultNumAttributes()
  {
    return 1;
  }
  



  public void setNumAttributes(int numAttributes)
  {
    super.setNumAttributes(numAttributes);
    m_numValues = new int[getNumAttributes()];
  }
  





  public String numAttributesTipText()
  {
    return "The number of attributes the generated data will contain (Note: they must be covered by the cluster definitions!)";
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
  




  public ClusterDefinition[] getClusterDefinitions()
  {
    return getClusters();
  }
  








  public void setClusterDefinitions(ClusterDefinition[] value)
    throws Exception
  {
    String indexStr = "";
    m_Clusters = value;
    for (int i = 0; i < getClusters().length; i++) {
      if (!(getClusters()[i] instanceof SubspaceClusterDefinition)) {
        if (indexStr.length() != 0)
          indexStr = indexStr + ",";
        indexStr = indexStr + "" + (i + 1);
      }
      getClusters()[i].setParent(this);
      getClusters()[i].setOptions(getClusters()[i].getOptions());
    }
    

    if (indexStr.length() != 0) {
      throw new Exception("These cluster definitions are not '" + SubspaceClusterDefinition.class.getName() + "': " + indexStr);
    }
  }
  





  public String clusterDefinitionsTipText()
  {
    return "The clusters to use.";
  }
  













  protected boolean checkCoverage()
  {
    int[] count = new int[getNumAttributes()];
    for (int i = 0; i < getNumAttributes(); i++) {
      for (int n = 0; n < getClusters().length; n++) {
        SubspaceClusterDefinition cl = (SubspaceClusterDefinition)getClusters()[n];
        Range r = new Range(cl.getAttrIndexRange());
        r.setUpper(getNumAttributes());
        if (r.isInRange(i)) {
          count[i] += 1;
        }
      }
    }
    
    String attrIndex = "";
    for (i = 0; i < count.length; i++) {
      if (count[i] == 0) {
        if (attrIndex.length() != 0)
          attrIndex = attrIndex + ",";
        attrIndex = attrIndex + (i + 1);
      }
    }
    
    if (attrIndex.length() != 0) {
      throw new IllegalArgumentException("The following attributes are not covered by a cluster definition: " + attrIndex + "\n");
    }
    

    return true;
  }
  




  public boolean getSingleModeFlag()
  {
    return false;
  }
  







  public Instances defineDataFormat()
    throws Exception
  {
    setOptions(getOptions());
    
    checkCoverage();
    
    Random random = new Random(getSeed());
    setRandom(random);
    
    FastVector attributes = new FastVector(3);
    
    boolean classFlag = getClassFlag();
    
    FastVector classValues = null;
    if (classFlag)
      classValues = new FastVector(getClusters().length);
    FastVector boolValues = new FastVector(2);
    boolValues.addElement("false");
    boolValues.addElement("true");
    FastVector nomValues = null;
    

    for (int i = 0; i < getNumAttributes(); i++) { Attribute attribute;
      Attribute attribute;
      if (m_booleanCols.isInRange(i)) {
        attribute = new Attribute("B" + i, boolValues);
      } else { Attribute attribute;
        if (m_nominalCols.isInRange(i))
        {
          nomValues = new FastVector(m_numValues[i]);
          for (int j = 0; j < m_numValues[i]; j++)
            nomValues.addElement("value-" + j);
          attribute = new Attribute("N" + i, nomValues);
        }
        else
        {
          attribute = new Attribute("X" + i);
        } }
      attributes.addElement(attribute);
    }
    
    if (classFlag) {
      for (int i = 0; i < getClusters().length; i++)
        classValues.addElement("c" + i);
      Attribute attribute = new Attribute("class", classValues);
      attributes.addElement(attribute);
    }
    
    Instances dataset = new Instances(getRelationNameToUse(), attributes, 0);
    if (classFlag) {
      dataset.setClassIndex(m_NumAttributes);
    }
    
    Instances format = new Instances(dataset, 0);
    setDatasetFormat(format);
    
    for (int i = 0; i < getClusters().length; i++) {
      SubspaceClusterDefinition cl = (SubspaceClusterDefinition)getClusters()[i];
      cl.setNumInstances(random);
      cl.setParent(this);
    }
    
    return dataset;
  }
  




  public boolean isBoolean(int index)
  {
    return m_booleanCols.isInRange(index);
  }
  




  public boolean isNominal(int index)
  {
    return m_nominalCols.isInRange(index);
  }
  




  public int[] getNumValues()
  {
    return m_numValues;
  }
  





  public Instance generateExample()
    throws Exception
  {
    throw new Exception("Examples cannot be generated one by one.");
  }
  




  public Instances generateExamples()
    throws Exception
  {
    Instances format = getDatasetFormat();
    Instance example = null;
    
    if (format == null) {
      throw new Exception("Dataset format not defined.");
    }
    
    for (int cNum = 0; cNum < getClusters().length; cNum++) {
      SubspaceClusterDefinition cl = (SubspaceClusterDefinition)getClusters()[cNum];
      

      int instNum = cl.getNumInstances();
      

      String cName = "c" + cNum;
      
      switch (cl.getClusterType().getSelectedTag().getID()) {
      case 0: 
        for (int i = 0; i < instNum; i++)
        {
          example = generateExample(format, getRandom(), cl, cName);
          if (example != null)
            format.add(example);
        }
        break;
      
      case 1: 
        if (!cl.isInteger()) {
          generateUniformExamples(format, instNum, cl, cName);
        } else
          generateUniformIntegerExamples(format, instNum, cl, cName);
        break;
      
      case 2: 
        generateGaussianExamples(format, instNum, getRandom(), cl, cName);
      }
      
    }
    
    return format;
  }
  











  private Instance generateExample(Instances format, Random randomG, SubspaceClusterDefinition cl, String cName)
  {
    boolean makeInteger = cl.isInteger();
    int num = -1;
    Instance example = null;
    int numAtts = m_NumAttributes;
    if (getClassFlag()) { numAtts++;
    }
    example = new Instance(numAtts);
    example.setDataset(format);
    boolean[] attributes = cl.getAttributes();
    double[] minValue = cl.getMinValue();
    double[] maxValue = cl.getMaxValue();
    

    int clusterI = -1;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (attributes[i] != 0) {
        clusterI++;
        num++;
        double value;
        if ((isBoolean(i)) || (isNominal(i))) {
          double value;
          if (minValue[clusterI] == maxValue[clusterI]) {
            value = minValue[clusterI];
          }
          else {
            int numValues = (int)(maxValue[clusterI] - minValue[clusterI] + 1.0D);
            double value = randomG.nextInt(numValues);
            value += minValue[clusterI];
          }
        }
        else
        {
          value = randomG.nextDouble() * (maxValue[num] - minValue[num]) + minValue[num];
          
          if (makeInteger)
            value = Math.round(value);
        }
        example.setValue(i, value);
      }
      else {
        example.setMissing(i);
      }
    }
    
    if (getClassFlag()) {
      example.setClassValue(cName);
    }
    return example;
  }
  










  private void generateUniformExamples(Instances format, int numInstances, SubspaceClusterDefinition cl, String cName)
  {
    Instance example = null;
    int numAtts = m_NumAttributes;
    if (getClassFlag()) { numAtts++;
    }
    example = new Instance(numAtts);
    example.setDataset(format);
    boolean[] attributes = cl.getAttributes();
    double[] minValue = cl.getMinValue();
    double[] maxValue = cl.getMaxValue();
    double[] diff = new double[minValue.length];
    
    for (int i = 0; i < minValue.length; i++) {
      maxValue[i] -= minValue[i];
    }
    for (int j = 0; j < numInstances; j++) {
      int num = -1;
      for (int i = 0; i < m_NumAttributes; i++) {
        if (attributes[i] != 0) {
          num++;
          double value = minValue[num] + diff[num] * (j / (numInstances - 1));
          example.setValue(i, value);
        }
        else {
          example.setMissing(i);
        }
      }
      if (getClassFlag())
        example.setClassValue(cName);
      format.add(example);
    }
  }
  










  private void generateUniformIntegerExamples(Instances format, int numInstances, SubspaceClusterDefinition cl, String cName)
  {
    Instance example = null;
    int numAtts = m_NumAttributes;
    if (getClassFlag()) { numAtts++;
    }
    example = new Instance(numAtts);
    example.setDataset(format);
    boolean[] attributes = cl.getAttributes();
    double[] minValue = cl.getMinValue();
    double[] maxValue = cl.getMaxValue();
    int[] minInt = new int[minValue.length];
    int[] maxInt = new int[maxValue.length];
    int[] intValue = new int[maxValue.length];
    int[] numInt = new int[minValue.length];
    
    int num = 1;
    for (int i = 0; i < minValue.length; i++) {
      minInt[i] = ((int)Math.ceil(minValue[i]));
      maxInt[i] = ((int)Math.floor(maxValue[i]));
      numInt[i] = (maxInt[i] - minInt[i] + 1);
      num *= numInt[i];
    }
    int numEach = numInstances / num;
    int rest = numInstances - numEach * num;
    

    for (int i = 0; i < m_NumAttributes; i++) {
      if (attributes[i] != 0) {
        example.setValue(i, minInt[i]);
        intValue[i] = minInt[i];
      }
      else {
        example.setMissing(i);
      }
    }
    if (getClassFlag())
      example.setClassValue(cName);
    int added = 0;
    int attr = 0;
    
    do
    {
      for (int k = 0; k < numEach; k++) {
        format.add(example);
        example = (Instance)example.copy();
        added++;
      }
      if (rest > 0) {
        format.add(example);
        example = (Instance)example.copy();
        added++;
        rest--;
      }
      
      if (added >= numInstances)
        break;
      boolean done = false;
      do {
        if ((attributes[attr] != 0) && (intValue[attr] + 1 <= maxInt[attr])) {
          intValue[attr] += 1;
          done = true;
        }
        else {
          attr++;
        }
      } while (!done);
      
      example.setValue(attr, intValue[attr]);
    } while (added < numInstances);
  }
  











  private void generateGaussianExamples(Instances format, int numInstances, Random random, SubspaceClusterDefinition cl, String cName)
  {
    boolean makeInteger = cl.isInteger();
    Instance example = null;
    int numAtts = m_NumAttributes;
    if (getClassFlag()) { numAtts++;
    }
    example = new Instance(numAtts);
    example.setDataset(format);
    boolean[] attributes = cl.getAttributes();
    double[] meanValue = cl.getMeanValue();
    double[] stddevValue = cl.getStddevValue();
    
    for (int j = 0; j < numInstances; j++) {
      int num = -1;
      for (int i = 0; i < m_NumAttributes; i++) {
        if (attributes[i] != 0) {
          num++;
          double value = meanValue[num] + random.nextGaussian() * stddevValue[num];
          if (makeInteger)
            value = Math.round(value);
          example.setValue(i, value);
        }
        else {
          example.setMissing(i);
        }
      }
      if (getClassFlag())
        example.setClassValue(cName);
      format.add(example);
    }
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
    for (int cNum = 0; cNum < getClusters().length; cNum++) {
      SubspaceClusterDefinition cl = (SubspaceClusterDefinition)getClusters()[cNum];
      docu.append("%\n");
      docu.append("% Cluster: c" + cNum + "   ");
      switch (cl.getClusterType().getSelectedTag().getID()) {
      case 0: 
        docu.append("Uniform Random");
        break;
      case 1: 
        docu.append("Total Random");
        break;
      case 2: 
        docu.append("Gaussian");
      }
      
      if (cl.isInteger()) {
        docu.append(" / INTEGER");
      }
      
      docu.append("\n% ----------------------------------------------\n");
      docu.append("%" + cl.attributesToString());
      
      docu.append("\n% Number of Instances:            " + cl.getInstNums() + "\n");
      docu.append("% Generated Number of Instances:  " + cl.getNumInstances() + "\n");
      sumInst += cl.getNumInstances();
    }
    docu.append("%\n% ----------------------------------------------\n");
    docu.append("% Total Number of Instances: " + sumInst + "\n");
    docu.append("%                            in " + getClusters().length + " Cluster(s)\n%");
    
    return docu.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new SubspaceCluster(), args);
  }
}
