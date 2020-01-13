package weka.classifiers.lazy;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

































































































public class LBR
  extends Classifier
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 5648559277738985156L;
  protected int[][][] m_Counts;
  protected int[][][] m_tCounts;
  protected int[] m_Priors;
  protected int[] m_tPriors;
  protected int m_numAtts;
  protected int m_numClasses;
  protected int m_numInsts;
  public LBR() {}
  
  public class Indexes
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -2771490019751421307L;
    public boolean[] m_InstIndexes;
    public boolean[] m_AttIndexes;
    private int m_NumInstances;
    private int m_NumAtts;
    public int[] m_SequentialInstIndexes;
    public int[] m_SequentialAttIndexes;
    private boolean m_SequentialInstanceIndex_valid = false;
    

    private boolean m_SequentialAttIndex_valid = false;
    



    public int m_NumInstsSet;
    



    public int m_NumAttsSet;
    



    public int m_NumSeqInstsSet;
    


    public int m_NumSeqAttsSet;
    


    public int m_ClassIndex;
    



    public Indexes(int numInstances, int numAtts, boolean value, int classIndex)
    {
      m_NumInstsSet = (this.m_NumInstances = numInstances);
      m_NumAttsSet = (this.m_NumAtts = numAtts);
      
      m_InstIndexes = new boolean[numInstances];
      

      int i = 0;
      while (i < numInstances) {
        m_InstIndexes[i] = value;
        i++;
      }
      
      m_AttIndexes = new boolean[numAtts];
      

      i = 0;
      while (i < numAtts) {
        m_AttIndexes[i] = true;
        i++;
      }
      
      if (!value) {
        m_NumInstsSet = 0;
      }
      m_SequentialInstanceIndex_valid = false;
      m_SequentialAttIndex_valid = false;
      

      if (classIndex != -1)
        setAttIndex(classIndex, false);
      m_ClassIndex = classIndex;
    }
    




    public Indexes(Indexes FromIndexes)
    {
      m_NumInstances = FromIndexes.getNumInstances();
      m_NumInstsSet = m_NumInstsSet;
      m_NumAtts = m_NumAtts;
      m_NumAttsSet = m_NumAttsSet;
      m_InstIndexes = new boolean[m_NumInstances];
      
      System.arraycopy(m_InstIndexes, 0, m_InstIndexes, 0, m_NumInstances);
      
      m_AttIndexes = new boolean[m_NumAtts];
      
      System.arraycopy(m_AttIndexes, 0, m_AttIndexes, 0, m_NumAtts);
      m_ClassIndex = m_ClassIndex;
      m_SequentialInstanceIndex_valid = false;
      m_SequentialAttIndex_valid = false;
    }
    







    public void setInstanceIndex(int index, boolean value)
    {
      if ((index < 0) || (index >= m_NumInstances)) {
        throw new IllegalArgumentException("Invalid Instance Index value");
      }
      if (m_InstIndexes[index] != value)
      {

        m_InstIndexes[index] = value;
        

        m_SequentialInstanceIndex_valid = false;
        

        if (!value) {
          m_NumInstsSet -= 1;
        } else {
          m_NumInstsSet += 1;
        }
      }
    }
    






    public void setAtts(int[] Attributes, boolean value)
    {
      for (int i = 0; i < m_NumAtts; i++) {
        m_AttIndexes[i] = (!value ? 1 : false);
      }
      for (int i = 0; i < Attributes.length; i++) {
        m_AttIndexes[Attributes[i]] = value;
      }
      m_NumAttsSet = Attributes.length;
      m_SequentialAttIndex_valid = false;
    }
    







    public void setInsts(int[] Instances, boolean value)
    {
      resetInstanceIndex(!value);
      for (int i = 0; i < Instances.length; i++) {
        m_InstIndexes[Instances[i]] = value;
      }
      m_NumInstsSet = Instances.length;
      m_SequentialInstanceIndex_valid = false;
    }
    








    public void setAttIndex(int index, boolean value)
    {
      if ((index < 0) || (index >= m_NumAtts)) {
        throw new IllegalArgumentException("Invalid Attribute Index value");
      }
      if (m_AttIndexes[index] != value)
      {

        m_AttIndexes[index] = value;
        

        m_SequentialAttIndex_valid = false;
        

        if (!value) {
          m_NumAttsSet -= 1;
        } else {
          m_NumAttsSet += 1;
        }
      }
    }
    






    public boolean getInstanceIndex(int index)
    {
      if ((index < 0) || (index >= m_NumInstances)) {
        throw new IllegalArgumentException("Invalid index value");
      }
      return m_InstIndexes[index];
    }
    







    public int getSequentialInstanceIndex(int index)
    {
      if ((index < 0) || (index >= m_NumInstances)) {
        throw new IllegalArgumentException("Invalid index value");
      }
      return m_SequentialInstIndexes[index];
    }
    






    public void resetInstanceIndex(boolean value)
    {
      m_NumInstsSet = m_NumInstances;
      for (int i = 0; i < m_NumInstances; i++) {
        m_InstIndexes[i] = value;
      }
      if (!value)
        m_NumInstsSet = 0;
      m_SequentialInstanceIndex_valid = false;
    }
    






    public void resetDatasetBasedOn(Indexes FromIndexes)
    {
      resetInstanceIndex(false);
      resetAttIndexTo(FromIndexes);
    }
    






    public void resetAttIndex(boolean value)
    {
      m_NumAttsSet = m_NumAtts;
      for (int i = 0; i < m_NumAtts; i++) {
        m_AttIndexes[i] = value;
      }
      if (m_ClassIndex != -1)
        setAttIndex(m_ClassIndex, false);
      if (!value)
        m_NumAttsSet = 0;
      m_SequentialAttIndex_valid = false;
    }
    






    public void resetAttIndexTo(Indexes FromIndexes)
    {
      System.arraycopy(m_AttIndexes, 0, m_AttIndexes, 0, m_NumAtts);
      m_NumAttsSet = FromIndexes.getNumAttributesSet();
      m_ClassIndex = m_ClassIndex;
      m_SequentialAttIndex_valid = false;
    }
    







    public boolean getAttIndex(int index)
    {
      if ((index < 0) || (index >= m_NumAtts)) {
        throw new IllegalArgumentException("Invalid index value");
      }
      return m_AttIndexes[index];
    }
    







    public int getSequentialAttIndex(int index)
    {
      if ((index < 0) || (index >= m_NumAtts)) {
        throw new IllegalArgumentException("Invalid index value");
      }
      return m_SequentialAttIndexes[index];
    }
    






    public int getNumInstancesSet()
    {
      return m_NumInstsSet;
    }
    






    public int getNumInstances()
    {
      return m_NumInstances;
    }
    






    public int getSequentialNumInstances()
    {
      return m_NumSeqInstsSet;
    }
    






    public int getNumAttributes()
    {
      return m_NumAtts;
    }
    






    public int getNumAttributesSet()
    {
      return m_NumAttsSet;
    }
    






    public int getSequentialNumAttributes()
    {
      return m_NumSeqAttsSet;
    }
    






    public boolean isSequentialInstanceIndexValid()
    {
      return m_SequentialInstanceIndex_valid;
    }
    






    public boolean isSequentialAttIndexValid()
    {
      return m_SequentialAttIndex_valid;
    }
    





    public void setSequentialDataset(boolean value)
    {
      setSequentialInstanceIndex(value);
      setSequentialAttIndex(value);
    }
    







    public void setSequentialInstanceIndex(boolean value)
    {
      if (m_SequentialInstanceIndex_valid == true) {
        return;
      }
      

      int size = m_NumInstsSet;
      
      m_SequentialInstIndexes = new int[size];
      
      int j = 0;
      for (int i = 0; i < m_NumInstances; i++) {
        if (m_InstIndexes[i] == value) {
          m_SequentialInstIndexes[j] = i;
          j++;
        }
      }
      
      m_SequentialInstanceIndex_valid = true;
      m_NumSeqInstsSet = j;
    }
    







    public void setSequentialAttIndex(boolean value)
    {
      if (m_SequentialAttIndex_valid == true) {
        return;
      }
      

      int size = m_NumAttsSet;
      
      m_SequentialAttIndexes = new int[size];
      
      int j = 0;
      for (int i = 0; i < m_NumAtts; i++) {
        if (m_AttIndexes[i] == value) {
          m_SequentialAttIndexes[j] = i;
          j++;
        }
      }
      
      m_SequentialAttIndex_valid = true;
      m_NumSeqAttsSet = j;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5525 $");
    }
  }
  




















  protected Instances m_Instances = null;
  

  protected int m_Errors;
  

  protected boolean[] m_ErrorFlags;
  

  protected ArrayList leftHand = new ArrayList();
  


  protected static final double SIGNLOWER = 0.05D;
  


  protected boolean[] m_subOldErrorFlags;
  


  protected int m_RemainderErrors = 0;
  

  protected int m_Number = 0;
  

  protected int m_NumberOfInstances = 0;
  

  protected boolean m_NCV = false;
  

  protected Indexes m_subInstances;
  

  protected Indexes tempSubInstances;
  
  protected double[] posteriorsArray;
  
  protected int bestCnt;
  
  protected int tempCnt;
  
  protected int forCnt;
  
  protected int whileCnt;
  

  public String globalInfo()
  {
    return "Lazy Bayesian Rules Classifier. The naive Bayesian classifier provides a simple and effective approach to classifier learning, but its attribute independence assumption is often violated in the real world. Lazy Bayesian Rules selectively relaxes the independence assumption, achieving lower error rates over a range of learning tasks. LBR defers processing to classification time, making it a highly efficient and accurate classification algorithm when small numbers of objects are to be classified.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Zijian Zheng and G. Webb");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.TITLE, "Lazy Learning of Bayesian Rules");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "4");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "53-84");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    bestCnt = 0;
    tempCnt = 0;
    forCnt = 0;
    whileCnt = 0;
    

    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_numAtts = instances.numAttributes();
    m_numClasses = instances.numClasses();
    m_numInsts = instances.numInstances();
    

    m_Counts = new int[m_numClasses][m_numAtts][0];
    m_Priors = new int[m_numClasses];
    m_tCounts = new int[m_numClasses][m_numAtts][0];
    m_tPriors = new int[m_numClasses];
    m_subOldErrorFlags = new boolean[m_numInsts + 1];
    
    m_Instances = instances;
    
    m_subInstances = new Indexes(m_numInsts, m_numAtts, true, m_Instances.classIndex());
    tempSubInstances = new Indexes(m_numInsts, m_numAtts, true, m_Instances.classIndex());
    

    posteriorsArray = new double[m_numClasses];
    

    for (int attIndex = 0; attIndex < m_numAtts; attIndex++) {
      Attribute attribute = instances.attribute(attIndex);
      for (int j = 0; j < m_numClasses; j++) {
        m_Counts[j][attIndex] = new int[attribute.numValues()];
        m_tCounts[j][attIndex] = new int[attribute.numValues()];
      }
    }
    

    for (int i = 0; i < m_numInsts; i++) {
      Instance instance = instances.instance(i);
      int classValue = (int)instance.classValue();
      
      int[][] countsPointer = m_tCounts[classValue];
      for (attIndex = 0; attIndex < m_numAtts; attIndex++) {
        countsPointer[attIndex][((int)instance.value(attIndex))] += 1;
      }
      m_tPriors[classValue] += 1;
    }
    


    m_ErrorFlags = new boolean[m_numInsts];
    
    m_Errors = leaveOneOut(m_subInstances, m_tCounts, m_tPriors, m_ErrorFlags);
    
    if (m_Number == 0) {
      m_NumberOfInstances = m_Instances.numInstances();
    } else {
      System.out.println(" ");
      System.out.println("N-Fold Cross Validation: ");
      m_NCV = true;
    }
  }
  










  public double[] distributionForInstance(Instance testInstance)
    throws Exception
  {
    int subAttrIndex = 0;
    int subInstIndex = 0;
    int tempInstIndex = 0;
    
    int subLocalErrors = 0;
    int tempErrorsBest = 0;
    boolean[] tempErrorFlagBest = null;
    int[] tempD_subsetBestInsts = null;
    int[] tempD_subsetBestAtts = null;
    Indexes subInstances = new Indexes(m_numInsts, m_numAtts, true, m_Instances.classIndex());
    
    boolean[] subLocalErrorFlags = new boolean[subInstances.getNumInstances() + 1];
    
    int localErrors = m_Errors;
    boolean[] localErrorFlags = (boolean[])m_ErrorFlags.clone();
    

    int errorsNewNotOld = 0;
    
    int errorsOldNotNew = 0;
    

    leftHand.clear();
    


    while (localErrors >= 5) {
      int attributeBest = -1;
      whileCnt += 1;
      
      tempErrorsBest = subInstances.getNumInstancesSet() + 1;
      subInstances.setSequentialDataset(true);
      
      for (int attr = 0; attr < m_NumSeqAttsSet; attr++) {
        forCnt += 1;
        subAttrIndex = m_SequentialAttIndexes[attr];
        

        m_RemainderErrors = 0;
        

        for (int i = 0; i < m_numInsts; i++) {
          m_subOldErrorFlags[i] = true;
        }
        
        tempSubInstances.resetDatasetBasedOn(subInstances);
        
        for (int inst = 0; inst < m_NumSeqInstsSet; inst++) {
          subInstIndex = m_SequentialInstIndexes[inst];
          if (m_Instances.instance(subInstIndex).value(subAttrIndex) == testInstance.value(subAttrIndex))
          {
            tempSubInstances.setInstanceIndex(subInstIndex, true);
            if (localErrorFlags[subInstIndex] == 0) {
              m_subOldErrorFlags[subInstIndex] = false;
            }
            
          }
          else if (localErrorFlags[subInstIndex] == 0) {
            m_RemainderErrors += 1;
          }
        }
        


        if (tempSubInstances.m_NumInstsSet < m_NumInstsSet)
        {
          tempSubInstances.setAttIndex(subAttrIndex, false);
          



          localNaiveBayes(tempSubInstances);
          
          subLocalErrors = leaveOneOut(tempSubInstances, m_Counts, m_Priors, subLocalErrorFlags);
          
          errorsNewNotOld = 0;
          errorsOldNotNew = 0;
          
          tempSubInstances.setSequentialDataset(true);
          
          for (int t_inst = 0; t_inst < tempSubInstances.m_NumSeqInstsSet; t_inst++) {
            tempInstIndex = tempSubInstances.m_SequentialInstIndexes[t_inst];
            if (subLocalErrorFlags[tempInstIndex] == 0)
            {
              if (m_subOldErrorFlags[tempInstIndex] == 1) {
                errorsNewNotOld++;
              }
              
            }
            else if (m_subOldErrorFlags[tempInstIndex] == 0) {
              errorsOldNotNew++;
            }
          }
          


          int tempErrors = subLocalErrors + m_RemainderErrors;
          

          if ((tempErrors < tempErrorsBest) && (binomP(errorsNewNotOld, errorsNewNotOld + errorsOldNotNew, 0.5D) < 0.05D))
          {
            tempCnt += 1;
            



            tempSubInstances.setSequentialDataset(true);
            tempD_subsetBestInsts = (int[])tempSubInstances.m_SequentialInstIndexes.clone();
            tempD_subsetBestAtts = (int[])tempSubInstances.m_SequentialAttIndexes.clone();
            

            tempErrorsBest = tempErrors;
            
            tempErrorFlagBest = (boolean[])subLocalErrorFlags.clone();
            

            attributeBest = subAttrIndex;
          }
        }
      }
      

      if (attributeBest == -1) break;
      bestCnt += 1;
      
      leftHand.add(testInstance.attribute(attributeBest));
      




      subInstances.setInsts(tempD_subsetBestInsts, true);
      subInstances.setAtts(tempD_subsetBestAtts, true);
      subInstances.setAttIndex(attributeBest, false);
      

      localErrors = tempErrorsBest;
      localErrorFlags = tempErrorFlagBest;
    }
    





    localNaiveBayes(subInstances);
    return localDistributionForInstance(testInstance, subInstances);
  }
  





  public String toString()
  {
    if (m_Instances == null) {
      return "Lazy Bayesian Rule: No model built yet.";
    }
    try
    {
      StringBuffer text = new StringBuffer("=== LBR Run information ===\n\n");
      

      text.append("Scheme:       weka.classifiers.LBR\n");
      
      text.append("Relation:     " + m_Instances.attribute(m_Instances.classIndex()).name() + "\n");
      


      text.append("Instances:    " + m_Instances.numInstances() + "\n");
      
      text.append("Attributes:   " + m_Instances.numAttributes() + "\n");
      

      return text.toString();
    } catch (Exception e) {
      e.printStackTrace(); }
    return "Can't Print Lazy Bayes Rule Classifier!";
  }
  






















  public int leaveOneOut(Indexes instanceIndex, int[][][] counts, int[] priors, boolean[] errorFlags)
    throws Exception
  {
    double max = 0.0D;
    int maxIndex = 0;
    

    int errors = 0;
    

    instanceIndex.setSequentialDataset(true);
    
    int[] tempAttributeValues = new int[m_NumSeqAttsSet + 1];
    
    for (int inst = 0; inst < m_NumSeqInstsSet; inst++) {
      int instIndex = m_SequentialInstIndexes[inst];
      
      Instance tempInstance = m_Instances.instance(instIndex);
      if (!tempInstance.classIsMissing()) {
        int tempInstanceClassValue = (int)tempInstance.classValue();
        
        int[][] countsPointer = counts[tempInstanceClassValue];
        
        for (int attIndex = 0; attIndex < m_NumSeqAttsSet; attIndex++) {
          int AIndex = m_SequentialAttIndexes[attIndex];
          tempAttributeValues[attIndex] = ((int)tempInstance.value(AIndex));
          countsPointer[AIndex][tempAttributeValues[attIndex]] -= 1;
        }
        
        priors[tempInstanceClassValue] -= 1;
        max = 0.0D;
        maxIndex = 0;
        
        double sumForPriors = Utils.sum(priors);
        for (int clss = 0; clss < m_numClasses; clss++) {
          double posteriors = 0.0D;
          posteriors = (priors[clss] + 1) / (sumForPriors + m_numClasses);
          
          countsPointer = counts[clss];
          for (attIndex = 0; attIndex < m_NumSeqAttsSet; attIndex++) {
            int AIndex = m_SequentialAttIndexes[attIndex];
            if (!tempInstance.isMissing(AIndex)) {
              double sumForCounts = Utils.sum(countsPointer[AIndex]);
              posteriors *= (countsPointer[AIndex][tempAttributeValues[attIndex]] + 1) / (sumForCounts + tempInstance.attribute(AIndex).numValues());
            }
          }
          
          if (posteriors > max) {
            maxIndex = clss;
            max = posteriors;
          } }
        int tempClassValue;
        int tempClassValue;
        if (max > 0.0D) {
          tempClassValue = maxIndex;
        } else {
          tempClassValue = (int)Instance.missingValue();
        }
        



        if (tempClassValue == tempInstanceClassValue) {
          errorFlags[instIndex] = true;
        } else {
          errorFlags[instIndex] = false;
          errors++;
        }
        
        countsPointer = counts[tempInstanceClassValue];
        for (attIndex = 0; attIndex < m_NumSeqAttsSet; attIndex++) {
          int AIndex = m_SequentialAttIndexes[attIndex];
          counts[tempInstanceClassValue][AIndex][tempAttributeValues[attIndex]] += 1;
        }
        
        priors[tempInstanceClassValue] += 1;
      }
    }
    
    return errors;
  }
  











  public void localNaiveBayes(Indexes instanceIndex)
    throws Exception
  {
    int attIndex = 0;
    
    int attVal = 0;
    int classVal = 0;
    

    instanceIndex.setSequentialDataset(true);
    

    for (classVal = 0; classVal < m_numClasses; classVal++)
    {
      int[][] countsPointer1 = m_Counts[classVal];
      for (attIndex = 0; attIndex < m_numAtts; attIndex++) {
        Attribute attribute = m_Instances.attribute(attIndex);
        
        int[] countsPointer2 = countsPointer1[attIndex];
        for (attVal = 0; attVal < attribute.numValues(); attVal++) {
          countsPointer2[attVal] = 0;
        }
      }
      m_Priors[classVal] = 0;
    }
    
    for (int i = 0; i < m_NumSeqInstsSet; i++) {
      Instance instance = m_Instances.instance(m_SequentialInstIndexes[i]);
      for (attIndex = 0; attIndex < m_NumSeqAttsSet; attIndex++) {
        int AIndex = m_SequentialAttIndexes[attIndex];
        m_Counts[((int)instance.classValue())][AIndex][((int)instance.value(AIndex))] += 1;
      }
      m_Priors[((int)instance.classValue())] += 1;
    }
  }
  









  public double[] localDistributionForInstance(Instance instance, Indexes instanceIndex)
    throws Exception
  {
    double sumForPriors = 0.0D;
    double sumForCounts = 0.0D;
    
    int numClassesOfInstance = instance.numClasses();
    
    sumForPriors = 0.0D;
    sumForCounts = 0.0D;
    instanceIndex.setSequentialDataset(true);
    
    sumForPriors = Utils.sum(m_Priors) + numClassesOfInstance;
    for (int j = 0; j < numClassesOfInstance; j++)
    {
      int[][] countsPointer = m_Counts[j];
      posteriorsArray[j] = ((m_Priors[j] + 1) / sumForPriors);
      for (int attIndex = 0; attIndex < m_NumSeqAttsSet; attIndex++) {
        int AIndex = m_SequentialAttIndexes[attIndex];
        sumForCounts = Utils.sum(countsPointer[AIndex]);
        if (!instance.isMissing(AIndex)) {
          posteriorsArray[j] *= (countsPointer[AIndex][((int)instance.value(AIndex))] + 1) / (sumForCounts + instance.attribute(AIndex).numValues());
        }
      }
    }
    

    Utils.normalize(posteriorsArray);
    
    return posteriorsArray;
  }
  










  public double binomP(double r, double n, double p)
    throws Exception
  {
    if (n == r) return 1.0D;
    return Statistics.incompleteBeta(n - r, r + 1.0D, 1.0D - p);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5525 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LBR(), argv);
  }
}
