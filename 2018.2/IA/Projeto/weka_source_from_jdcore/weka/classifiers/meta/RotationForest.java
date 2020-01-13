package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PrincipalComponents;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.instance.RemovePercentage;

















































































































































public class RotationForest
  extends RandomizableIteratedSingleClassifierEnhancer
  implements WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -3255631880798499936L;
  protected int m_MinGroup = 3;
  

  protected int m_MaxGroup = 3;
  



  protected boolean m_NumberOfGroups = false;
  

  protected int m_RemovedPercentage = 50;
  

  protected int[][][] m_Groups = (int[][][])null;
  

  protected Filter m_ProjectionFilter = null;
  

  protected Filter[][] m_ProjectionFilters = (Filter[][])null;
  

  protected Instances[] m_Headers = null;
  

  protected Instances[][] m_ReducedHeaders = (Instances[][])null;
  

  protected RemoveUseless m_RemoveUseless = null;
  

  protected Normalize m_Normalize = null;
  



  public RotationForest()
  {
    m_Classifier = new J48();
    m_ProjectionFilter = defaultFilter();
  }
  


  protected Filter defaultFilter()
  {
    PrincipalComponents filter = new PrincipalComponents();
    
    filter.setVarianceCovered(1.0D);
    return filter;
  }
  





  public String globalInfo()
  {
    return "Class for construction a Rotation Forest. Can do classification and regression depending on the base learner. \n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Juan J. Rodriguez and Ludmila I. Kuncheva and Carlos J. Alonso");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.TITLE, "Rotation Forest: A new classifier ensemble method");
    result.setValue(TechnicalInformation.Field.JOURNAL, "IEEE Transactions on Pattern Analysis and Machine Intelligence");
    result.setValue(TechnicalInformation.Field.VOLUME, "28");
    result.setValue(TechnicalInformation.Field.NUMBER, "10");
    result.setValue(TechnicalInformation.Field.PAGES, "1619-1630");
    result.setValue(TechnicalInformation.Field.ISSN, "0162-8828");
    result.setValue(TechnicalInformation.Field.URL, "http://doi.ieeecomputersociety.org/10.1109/TPAMI.2006.211");
    
    return result;
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tWhether minGroup (-G) and maxGroup (-H) refer to\n\tthe number of groups or their size.\n\t(default: false)", "N", 0, "-N"));
    




    newVector.addElement(new Option("\tMinimum size of a group of attributes:\n\t\tif numberOfGroups is true, the minimum number\n\t\tof groups.\n\t\t(default: 3)", "G", 1, "-G <num>"));
    





    newVector.addElement(new Option("\tMaximum size of a group of attributes:\n\t\tif numberOfGroups is true, the maximum number\n\t\tof groups.\n\t\t(default: 3)", "H", 1, "-H <num>"));
    





    newVector.addElement(new Option("\tPercentage of instances to be removed.\n\t\t(default: 50)", "P", 1, "-P <num>"));
    



    newVector.addElement(new Option("\tFull class name of filter to use, followed\n\tby filter options.\n\teg: \"weka.filters.unsupervised.attribute.PrincipalComponents-R 1.0\"", "F", 1, "-F <filter specification>"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  



























































































  public void setOptions(String[] options)
    throws Exception
  {
    String filterString = Utils.getOption('F', options);
    if (filterString.length() > 0) {
      String[] filterSpec = Utils.splitOptions(filterString);
      if (filterSpec.length == 0) {
        throw new IllegalArgumentException("Invalid filter specification string");
      }
      String filterName = filterSpec[0];
      filterSpec[0] = "";
      setProjectionFilter((Filter)Utils.forName(Filter.class, filterName, filterSpec));
    } else {
      setProjectionFilter(defaultFilter());
    }
    


    String tmpStr = Utils.getOption('G', options);
    if (tmpStr.length() != 0) {
      setMinGroup(Integer.parseInt(tmpStr));
    } else {
      setMinGroup(3);
    }
    tmpStr = Utils.getOption('H', options);
    if (tmpStr.length() != 0) {
      setMaxGroup(Integer.parseInt(tmpStr));
    } else {
      setMaxGroup(3);
    }
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setRemovedPercentage(Integer.parseInt(tmpStr));
    } else {
      setRemovedPercentage(50);
    }
    setNumberOfGroups(Utils.getFlag('N', options));
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 9];
    
    int current = 0;
    
    if (getNumberOfGroups()) {
      options[(current++)] = "-N";
    }
    
    options[(current++)] = "-G";
    options[(current++)] = ("" + getMinGroup());
    
    options[(current++)] = "-H";
    options[(current++)] = ("" + getMaxGroup());
    
    options[(current++)] = "-P";
    options[(current++)] = ("" + getRemovedPercentage());
    
    options[(current++)] = "-F";
    options[(current++)] = getProjectionFilterSpec();
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String numberOfGroupsTipText()
  {
    return "Whether minGroup and maxGroup refer to the number of groups or their size.";
  }
  







  public void setNumberOfGroups(boolean numberOfGroups)
  {
    m_NumberOfGroups = numberOfGroups;
  }
  







  public boolean getNumberOfGroups()
  {
    return m_NumberOfGroups;
  }
  




  public String minGroupTipText()
  {
    return "Minimum size of a group (if numberOfGrups is true, the minimum number of groups.";
  }
  





  public void setMinGroup(int minGroup)
    throws IllegalArgumentException
  {
    if (minGroup <= 0)
      throw new IllegalArgumentException("MinGroup has to be positive.");
    m_MinGroup = minGroup;
  }
  




  public int getMinGroup()
  {
    return m_MinGroup;
  }
  




  public String maxGroupTipText()
  {
    return "Maximum size of a group (if numberOfGrups is true, the maximum number of groups.";
  }
  





  public void setMaxGroup(int maxGroup)
    throws IllegalArgumentException
  {
    if (maxGroup <= 0)
      throw new IllegalArgumentException("MaxGroup has to be positive.");
    m_MaxGroup = maxGroup;
  }
  




  public int getMaxGroup()
  {
    return m_MaxGroup;
  }
  




  public String removedPercentageTipText()
  {
    return "The percentage of instances to be removed.";
  }
  




  public void setRemovedPercentage(int removedPercentage)
    throws IllegalArgumentException
  {
    if (removedPercentage < 0)
      throw new IllegalArgumentException("RemovedPercentage has to be >=0.");
    if (removedPercentage >= 100) {
      throw new IllegalArgumentException("RemovedPercentage has to be <100.");
    }
    m_RemovedPercentage = removedPercentage;
  }
  




  public int getRemovedPercentage()
  {
    return m_RemovedPercentage;
  }
  




  public String projectionFilterTipText()
  {
    return "The filter used to project the data (e.g., PrincipalComponents).";
  }
  





  public void setProjectionFilter(Filter projectionFilter)
  {
    m_ProjectionFilter = projectionFilter;
  }
  




  public Filter getProjectionFilter()
  {
    return m_ProjectionFilter;
  }
  







  protected String getProjectionFilterSpec()
  {
    Filter c = getProjectionFilter();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    
    return c.getClass().getName();
  }
  





  public String toString()
  {
    if (m_Classifiers == null) {
      return "RotationForest: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("All the base classifiers: \n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7012 $");
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    data = new Instances(data);
    super.buildClassifier(data);
    
    checkMinMax(data);
    Random random;
    Random random;
    if (data.numInstances() > 0)
    {
      random = data.getRandomNumberGenerator(m_Seed);
    }
    else {
      random = new Random(m_Seed);
    }
    
    m_RemoveUseless = new RemoveUseless();
    m_RemoveUseless.setInputFormat(data);
    data = Filter.useFilter(data, m_RemoveUseless);
    
    m_Normalize = new Normalize();
    m_Normalize.setInputFormat(data);
    data = Filter.useFilter(data, m_Normalize);
    
    if (m_NumberOfGroups) {
      generateGroupsFromNumbers(data, random);
    }
    else {
      generateGroupsFromSizes(data, random);
    }
    
    m_ProjectionFilters = new Filter[m_Groups.length][];
    for (int i = 0; i < m_ProjectionFilters.length; i++) {
      m_ProjectionFilters[i] = Filter.makeCopies(m_ProjectionFilter, m_Groups[i].length);
    }
    

    int numClasses = data.numClasses();
    

    Instances[] instancesOfClass = new Instances[numClasses + 1];
    if (data.classAttribute().isNumeric()) {
      instancesOfClass = new Instances[numClasses];
      instancesOfClass[0] = data;
    }
    else {
      instancesOfClass = new Instances[numClasses + 1];
      for (int i = 0; i < instancesOfClass.length; i++) {
        instancesOfClass[i] = new Instances(data, 0);
      }
      Enumeration enu = data.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance instance = (Instance)enu.nextElement();
        if (instance.classIsMissing()) {
          instancesOfClass[numClasses].add(instance);
        }
        else {
          int c = (int)instance.classValue();
          instancesOfClass[c].add(instance);
        }
      }
      

      if (instancesOfClass[numClasses].numInstances() == 0) {
        Instances[] tmp = instancesOfClass;
        instancesOfClass = new Instances[numClasses];
        System.arraycopy(tmp, 0, instancesOfClass, 0, numClasses);
      }
    }
    

    m_Headers = new Instances[m_Classifiers.length];
    m_ReducedHeaders = new Instances[m_Classifiers.length][];
    

    for (int i = 0; i < m_Classifiers.length; i++) {
      m_ReducedHeaders[i] = new Instances[m_Groups[i].length];
      FastVector transformedAttributes = new FastVector(data.numAttributes());
      

      for (int j = 0; j < m_Groups[i].length; j++) {
        FastVector fv = new FastVector(m_Groups[i][j].length + 1);
        for (int k = 0; k < m_Groups[i][j].length; k++) {
          String newName = data.attribute(m_Groups[i][j][k]).name() + "_" + k;
          
          fv.addElement(data.attribute(m_Groups[i][j][k]).copy(newName));
        }
        fv.addElement(data.classAttribute().copy());
        Instances dataSubSet = new Instances("rotated-" + i + "-" + j + "-", fv, 0);
        
        dataSubSet.setClassIndex(dataSubSet.numAttributes() - 1);
        

        m_ReducedHeaders[i][j] = new Instances(dataSubSet, 0);
        boolean[] selectedClasses = selectClasses(instancesOfClass.length, random);
        
        for (int c = 0; c < selectedClasses.length; c++) {
          if (selectedClasses[c] != 0)
          {
            Enumeration enu = instancesOfClass[c].enumerateInstances();
            while (enu.hasMoreElements()) {
              Instance instance = (Instance)enu.nextElement();
              Instance newInstance = new Instance(dataSubSet.numAttributes());
              newInstance.setDataset(dataSubSet);
              for (int k = 0; k < m_Groups[i][j].length; k++) {
                newInstance.setValue(k, instance.value(m_Groups[i][j][k]));
              }
              newInstance.setClassValue(instance.classValue());
              dataSubSet.add(newInstance);
            }
          }
        }
        dataSubSet.randomize(random);
        
        Instances originalDataSubSet = dataSubSet;
        dataSubSet.randomize(random);
        RemovePercentage rp = new RemovePercentage();
        rp.setPercentage(m_RemovedPercentage);
        rp.setInputFormat(dataSubSet);
        dataSubSet = Filter.useFilter(dataSubSet, rp);
        if (dataSubSet.numInstances() < 2) {
          dataSubSet = originalDataSubSet;
        }
        

        m_ProjectionFilters[i][j].setInputFormat(dataSubSet);
        Instances projectedData = null;
        do {
          try {
            projectedData = Filter.useFilter(dataSubSet, m_ProjectionFilters[i][j]);
          }
          catch (Exception e)
          {
            addRandomInstances(dataSubSet, 10, random);
          }
        } while (projectedData == null);
        


        for (int a = 0; a < projectedData.numAttributes() - 1; a++) {
          String newName = projectedData.attribute(a).name() + "_" + j;
          transformedAttributes.addElement(projectedData.attribute(a).copy(newName));
        }
      }
      
      transformedAttributes.addElement(data.classAttribute().copy());
      Instances transformedData = new Instances("rotated-" + i + "-", transformedAttributes, 0);
      
      transformedData.setClassIndex(transformedData.numAttributes() - 1);
      m_Headers[i] = new Instances(transformedData, 0);
      

      Enumeration enu = data.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance instance = (Instance)enu.nextElement();
        Instance newInstance = convertInstance(instance, i);
        transformedData.add(newInstance);
      }
      

      if ((m_Classifier instanceof Randomizable)) {
        ((Randomizable)m_Classifiers[i]).setSeed(random.nextInt());
      }
      m_Classifiers[i].buildClassifier(transformedData);
    }
    
    if (m_Debug) {
      printGroups();
    }
  }
  







  protected void addRandomInstances(Instances dataset, int numInstances, Random random)
  {
    int n = dataset.numAttributes();
    double[] v = new double[n];
    for (int i = 0; i < numInstances; i++) {
      for (int j = 0; j < n; j++) {
        Attribute att = dataset.attribute(j);
        if (att.isNumeric()) {
          v[j] = random.nextDouble();
        }
        else if (att.isNominal()) {
          v[j] = random.nextInt(att.numValues());
        }
      }
      dataset.add(new Instance(1.0D, v));
    }
  }
  




  protected void checkMinMax(Instances data)
  {
    if (m_MinGroup > m_MaxGroup) {
      int tmp = m_MaxGroup;
      m_MaxGroup = m_MinGroup;
      m_MinGroup = tmp;
    }
    
    int n = data.numAttributes();
    if (m_MaxGroup >= n)
      m_MaxGroup = (n - 1);
    if (m_MinGroup >= n) {
      m_MinGroup = (n - 1);
    }
  }
  






  protected boolean[] selectClasses(int numClasses, Random random)
  {
    int numSelected = 0;
    boolean[] selected = new boolean[numClasses];
    
    for (int i = 0; i < selected.length; i++) {
      if (random.nextBoolean()) {
        selected[i] = true;
        numSelected++;
      }
    }
    if (numSelected == 0) {
      selected[random.nextInt(selected.length)] = true;
    }
    return selected;
  }
  







  protected void generateGroupsFromSizes(Instances data, Random random)
  {
    m_Groups = new int[m_Classifiers.length][][];
    for (int i = 0; i < m_Classifiers.length; i++) {
      int[] permutation = attributesPermutation(data.numAttributes(), data.classIndex(), random);
      


      int[] numGroupsOfSize = new int[m_MaxGroup - m_MinGroup + 1];
      
      int numAttributes = 0;
      


      for (int numGroups = 0; numAttributes < permutation.length; numGroups++) {
        int n = random.nextInt(numGroupsOfSize.length);
        numGroupsOfSize[n] += 1;
        numAttributes += m_MinGroup + n;
      }
      
      m_Groups[i] = new int[numGroups][];
      int currentAttribute = 0;
      int currentSize = 0;
      for (int j = 0; j < numGroups; j++) {
        while (numGroupsOfSize[currentSize] == 0)
          currentSize++;
        numGroupsOfSize[currentSize] -= 1;
        int n = m_MinGroup + currentSize;
        m_Groups[i][j] = new int[n];
        for (int k = 0; k < n; k++) {
          if (currentAttribute < permutation.length) {
            m_Groups[i][j][k] = permutation[currentAttribute];
          }
          else {
            m_Groups[i][j][k] = permutation[random.nextInt(permutation.length)];
          }
          currentAttribute++;
        }
      }
    }
  }
  







  protected void generateGroupsFromNumbers(Instances data, Random random)
  {
    m_Groups = new int[m_Classifiers.length][][];
    for (int i = 0; i < m_Classifiers.length; i++) {
      int[] permutation = attributesPermutation(data.numAttributes(), data.classIndex(), random);
      
      int numGroups = m_MinGroup + random.nextInt(m_MaxGroup - m_MinGroup + 1);
      m_Groups[i] = new int[numGroups][];
      int groupSize = permutation.length / numGroups;
      

      int numBiggerGroups = permutation.length % numGroups;
      

      int currentAttribute = 0;
      for (int j = 0; j < numGroups; j++) {
        if (j < numBiggerGroups) {
          m_Groups[i][j] = new int[groupSize + 1];
        }
        else {
          m_Groups[i][j] = new int[groupSize];
        }
        for (int k = 0; k < m_Groups[i][j].length; k++) {
          m_Groups[i][j][k] = permutation[(currentAttribute++)];
        }
      }
    }
  }
  








  protected int[] attributesPermutation(int numAttributes, int classAttribute, Random random)
  {
    int[] permutation = new int[numAttributes - 1];
    for (int i = 0; 
        i < classAttribute; i++) {
      permutation[i] = i;
    }
    for (; i < permutation.length; i++) {
      permutation[i] = (i + 1);
    }
    
    permute(permutation, random);
    
    return permutation;
  }
  






  protected void permute(int[] v, Random random)
  {
    for (int i = v.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      if (i != j) {
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
      }
    }
  }
  


  protected void printGroups()
  {
    for (int i = 0; i < m_Groups.length; i++) {
      for (int j = 0; j < m_Groups[i].length; j++) {
        System.err.print("( ");
        for (int k = 0; k < m_Groups[i][j].length; k++) {
          System.err.print(m_Groups[i][j][k]);
          System.err.print(" ");
        }
        System.err.print(") ");
      }
      System.err.println();
    }
  }
  







  protected Instance convertInstance(Instance instance, int i)
    throws Exception
  {
    Instance newInstance = new Instance(m_Headers[i].numAttributes());
    newInstance.setWeight(instance.weight());
    newInstance.setDataset(m_Headers[i]);
    int currentAttribute = 0;
    

    for (int j = 0; j < m_Groups[i].length; j++) {
      Instance auxInstance = new Instance(m_Groups[i][j].length + 1);
      
      for (int k = 0; k < m_Groups[i][j].length; k++) {
        auxInstance.setValue(k, instance.value(m_Groups[i][j][k]));
      }
      auxInstance.setValue(k, instance.classValue());
      auxInstance.setDataset(m_ReducedHeaders[i][j]);
      m_ProjectionFilters[i][j].input(auxInstance);
      auxInstance = m_ProjectionFilters[i][j].output();
      m_ProjectionFilters[i][j].batchFinished();
      for (int a = 0; a < auxInstance.numAttributes() - 1; a++) {
        newInstance.setValue(currentAttribute++, auxInstance.value(a));
      }
    }
    
    newInstance.setClassValue(instance.classValue());
    return newInstance;
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_RemoveUseless.input(instance);
    instance = m_RemoveUseless.output();
    m_RemoveUseless.batchFinished();
    
    m_Normalize.input(instance);
    instance = m_Normalize.output();
    m_Normalize.batchFinished();
    
    double[] sums = new double[instance.numClasses()];
    
    for (int i = 0; i < m_Classifiers.length; i++) {
      Instance convertedInstance = convertInstance(instance, i);
      if (instance.classAttribute().isNumeric() == true) {
        sums[0] += m_Classifiers[i].classifyInstance(convertedInstance);
      } else {
        double[] newProbs = m_Classifiers[i].distributionForInstance(convertedInstance);
        for (int j = 0; j < newProbs.length; j++)
          sums[j] += newProbs[j];
      }
    }
    if (instance.classAttribute().isNumeric() == true) {
      sums[0] /= m_NumIterations;
      return sums; }
    if (Utils.eq(Utils.sum(sums), 0.0D)) {
      return sums;
    }
    Utils.normalize(sums);
    return sums;
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new RotationForest(), argv);
  }
}
