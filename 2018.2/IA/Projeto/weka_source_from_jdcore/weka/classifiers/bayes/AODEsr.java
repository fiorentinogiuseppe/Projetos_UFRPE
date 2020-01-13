package weka.classifiers.bayes;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;































































































































public class AODEsr
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = 5602143019183068848L;
  private double[][][] m_CondiCounts;
  private double[][] m_CondiCountsNoClass;
  private double[] m_ClassCounts;
  private double[][] m_SumForCounts;
  private int m_NumClasses;
  private int m_NumAttributes;
  private int m_NumInstances;
  private int m_ClassIndex;
  private Instances m_Instances;
  private int m_TotalAttValues;
  private int[] m_StartAttIndex;
  private int[] m_NumAttValues;
  private double[] m_Frequencies;
  private double m_SumInstances;
  private int m_Limit = 1;
  

  private boolean m_Debug = false;
  

  protected double m_MWeight = 1.0D;
  

  private boolean m_Laplace = false;
  

  private int m_Critical = 50;
  


  public AODEsr() {}
  


  public String globalInfo()
  {
    return "AODEsr augments AODE with Subsumption Resolution.AODEsr detects specializations between two attribute values at classification time and deletes the generalization attribute value.\nFor more information, see:\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Fei Zheng and Geoffrey I. Webb");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.TITLE, "Efficient Lazy Elimination for Averaged-One Dependence Estimators");
    result.setValue(TechnicalInformation.Field.PAGES, "1113-1120");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the Twenty-third International Conference on Machine  Learning (ICML 2006)");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    result.setValue(TechnicalInformation.Field.ISBN, "1-59593-383-2");
    
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
    getCapabilities().testWithFail(instances);
    

    m_Instances = new Instances(instances);
    m_Instances.deleteWithMissingClass();
    

    m_SumInstances = 0.0D;
    m_ClassIndex = instances.classIndex();
    m_NumInstances = m_Instances.numInstances();
    m_NumAttributes = instances.numAttributes();
    m_NumClasses = instances.numClasses();
    

    m_StartAttIndex = new int[m_NumAttributes];
    m_NumAttValues = new int[m_NumAttributes];
    
    m_TotalAttValues = 0;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (i != m_ClassIndex) {
        m_StartAttIndex[i] = m_TotalAttValues;
        m_NumAttValues[i] = m_Instances.attribute(i).numValues();
        m_TotalAttValues += m_NumAttValues[i] + 1;
      }
      else
      {
        m_NumAttValues[i] = m_NumClasses;
      }
    }
    

    m_CondiCounts = new double[m_NumClasses][m_TotalAttValues][m_TotalAttValues];
    m_ClassCounts = new double[m_NumClasses];
    m_SumForCounts = new double[m_NumClasses][m_NumAttributes];
    m_Frequencies = new double[m_TotalAttValues];
    m_CondiCountsNoClass = new double[m_TotalAttValues][m_TotalAttValues];
    

    for (int k = 0; k < m_NumInstances; k++) {
      addToCounts(m_Instances.instance(k));
    }
    

    m_Instances = new Instances(m_Instances, 0);
  }
  







  public void updateClassifier(Instance instance)
  {
    addToCounts(instance);
  }
  










  private void addToCounts(Instance instance)
  {
    if (instance.classIsMissing()) {
      return;
    }
    int classVal = (int)instance.classValue();
    double weight = instance.weight();
    
    m_ClassCounts[classVal] += weight;
    m_SumInstances += weight;
    


    int[] attIndex = new int[m_NumAttributes];
    for (int i = 0; i < m_NumAttributes; i++) {
      if (i == m_ClassIndex) {
        attIndex[i] = -1;
      }
      else if (instance.isMissing(i)) {
        attIndex[i] = (m_StartAttIndex[i] + m_NumAttValues[i]);
      } else {
        attIndex[i] = (m_StartAttIndex[i] + (int)instance.value(i));
      }
    }
    
    for (int Att1 = 0; Att1 < m_NumAttributes; Att1++) {
      if (attIndex[Att1] != -1)
      {

        m_Frequencies[attIndex[Att1]] += weight;
        

        if (!instance.isMissing(Att1)) {
          m_SumForCounts[classVal][Att1] += weight;
        }
        
        double[] countsPointer = m_CondiCounts[classVal][attIndex[Att1]];
        double[] countsNoClassPointer = m_CondiCountsNoClass[attIndex[Att1]];
        
        for (int Att2 = 0; Att2 < m_NumAttributes; Att2++) {
          if (attIndex[Att2] != -1) {
            countsPointer[attIndex[Att2]] += weight;
            countsNoClassPointer[attIndex[Att2]] += weight;
          }
        }
      }
    }
  }
  








  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs = new double[m_NumClasses];
    



    int[] SpecialGeneralArray = new int[m_NumAttributes];
    








    int[] attIndex = new int[m_NumAttributes];
    for (int att = 0; att < m_NumAttributes; att++) {
      if ((instance.isMissing(att)) || (att == m_ClassIndex)) {
        attIndex[att] = -1;
      } else {
        attIndex[att] = (m_StartAttIndex[att] + (int)instance.value(att));
      }
    }
    for (int i = 0; i < m_NumAttributes; i++) {
      SpecialGeneralArray[i] = -1;
    }
    

    for (int i = 0; i < m_NumAttributes; i++)
    {
      if (attIndex[i] != -1) {
        double[] countsForAtti = m_CondiCountsNoClass[attIndex[i]];
        
        for (int j = 0; j < m_NumAttributes; j++)
        {
          if ((attIndex[j] != -1) && (i != j) && (SpecialGeneralArray[j] != i))
          {

            double[] countsForAttj = m_CondiCountsNoClass[attIndex[j]];
            

            if (countsForAttj[attIndex[j]] > m_Critical)
            {


              if (countsForAttj[attIndex[j]] == countsForAtti[attIndex[j]])
              {


                if ((countsForAttj[attIndex[j]] != countsForAtti[attIndex[i]]) || (i >= j))
                {



                  SpecialGeneralArray[i] = j;
                  break;
                }
              }
            }
          }
        }
      }
    }
    for (int classVal = 0; classVal < m_NumClasses; classVal++)
    {
      probs[classVal] = 0.0D;
      double x = 0.0D;
      int parentCount = 0;
      
      double[][] countsForClass = m_CondiCounts[classVal];
      

      for (int parent = 0; parent < m_NumAttributes; parent++) {
        if (attIndex[parent] != -1)
        {


          int pIndex = attIndex[parent];
          

          if (m_Frequencies[pIndex] >= m_Limit)
          {


            if (SpecialGeneralArray[parent] == -1)
            {

              double[] countsForClassParent = countsForClass[pIndex];
              

              attIndex[parent] = -1;
              
              parentCount++;
              
              double classparentfreq = countsForClassParent[pIndex];
              

              double missing4ParentAtt = m_Frequencies[(m_StartAttIndex[parent] + m_NumAttValues[parent])];
              


              if (m_Laplace) {
                x = LaplaceEstimate(classparentfreq, m_SumInstances - missing4ParentAtt, m_NumClasses * m_NumAttValues[parent]);
              }
              else
              {
                x = MEstimate(classparentfreq, m_SumInstances - missing4ParentAtt, m_NumClasses * m_NumAttValues[parent]);
              }
              




              for (int att = 0; att < m_NumAttributes; att++) {
                if (attIndex[att] != -1)
                {

                  if (SpecialGeneralArray[att] == -1)
                  {


                    double missingForParentandChildAtt = countsForClassParent[(m_StartAttIndex[att] + m_NumAttValues[att])];
                    

                    if (m_Laplace) {
                      x *= LaplaceEstimate(countsForClassParent[attIndex[att]], classparentfreq - missingForParentandChildAtt, m_NumAttValues[att]);
                    }
                    else {
                      x *= MEstimate(countsForClassParent[attIndex[att]], classparentfreq - missingForParentandChildAtt, m_NumAttValues[att]);
                    }
                  }
                }
              }
              
              probs[classVal] += x;
              

              attIndex[parent] = pIndex;
            } }
        }
      }
      if (parentCount < 1)
      {

        probs[classVal] = NBconditionalProb(instance, classVal);

      }
      else
      {

        probs[classVal] /= parentCount;
      }
    }
    Utils.normalize(probs);
    return probs;
  }
  




  public double NBconditionalProb(Instance instance, int classVal)
    throws Exception
  {
    double prob;
    



    double prob;
    



    if (m_Laplace) {
      prob = LaplaceEstimate(m_ClassCounts[classVal], m_SumInstances, m_NumClasses);
    } else {
      prob = MEstimate(m_ClassCounts[classVal], m_SumInstances, m_NumClasses);
    }
    double[][] pointer = m_CondiCounts[classVal];
    

    for (int att = 0; att < m_NumAttributes; att++) {
      if ((att != m_ClassIndex) && (!instance.isMissing(att)))
      {


        int attIndex = m_StartAttIndex[att] + (int)instance.value(att);
        if (m_Laplace) {
          prob *= LaplaceEstimate(pointer[attIndex][attIndex], m_SumForCounts[classVal][att], m_NumAttValues[att]);
        }
        else {
          prob *= MEstimate(pointer[attIndex][attIndex], m_SumForCounts[classVal][att], m_NumAttValues[att]);
        }
      }
    }
    return prob;
  }
  










  public double MEstimate(double frequency, double total, double numValues)
  {
    return (frequency + m_MWeight / numValues) / (total + m_MWeight);
  }
  









  public double LaplaceEstimate(double frequency, double total, double numValues)
  {
    return (frequency + 1.0D) / (total + numValues);
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tOutput debugging information\n", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tImpose a critcal value for specialization-generalization relationship\n\t(default is 50)", "C", 1, "-C"));
    

    newVector.addElement(new Option("\tImpose a frequency limit for superParents\n\t(default is 1)", "F", 2, "-F"));
    

    newVector.addElement(new Option("\tUsing Laplace estimation\n\t(default is m-esimation (m=1))", "L", 3, "-L"));
    


    newVector.addElement(new Option("\tWeight value for m-estimation\n\t(default is 1.0)", "M", 4, "-M"));
    


    return newVector.elements();
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    m_Debug = Utils.getFlag('D', options);
    
    String Critical = Utils.getOption('C', options);
    if (Critical.length() != 0) {
      m_Critical = Integer.parseInt(Critical);
    } else {
      m_Critical = 50;
    }
    String Freq = Utils.getOption('F', options);
    if (Freq.length() != 0) {
      m_Limit = Integer.parseInt(Freq);
    } else {
      m_Limit = 1;
    }
    m_Laplace = Utils.getFlag('L', options);
    String MWeight = Utils.getOption('M', options);
    if (MWeight.length() != 0) {
      if (m_Laplace)
        throw new Exception("weight for m-estimate is pointless if using laplace estimation!");
      m_MWeight = Double.parseDouble(MWeight);
    } else {
      m_MWeight = 1.0D;
    }
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (m_Debug) {
      result.add("-D");
    }
    result.add("-F");
    result.add("" + m_Limit);
    
    if (m_Laplace) {
      result.add("-L");
    } else {
      result.add("-M");
      result.add("" + m_MWeight);
    }
    
    result.add("-C");
    result.add("" + m_Critical);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String mestWeightTipText()
  {
    return "Set the weight for m-estimate.";
  }
  




  public void setMestWeight(double w)
  {
    if (getUseLaplace()) {
      System.out.println("Weight is only used in conjunction with m-estimate - ignored!");

    }
    else if (w > 0.0D) {
      m_MWeight = w;
    } else {
      System.out.println("M-Estimate Weight must be greater than 0!");
    }
  }
  




  public double getMestWeight()
  {
    return m_MWeight;
  }
  




  public String useLaplaceTipText()
  {
    return "Use Laplace correction instead of m-estimation.";
  }
  




  public boolean getUseLaplace()
  {
    return m_Laplace;
  }
  




  public void setUseLaplace(boolean value)
  {
    m_Laplace = value;
  }
  




  public String frequencyLimitTipText()
  {
    return "Attributes with a frequency in the train set below this value aren't used as parents.";
  }
  





  public void setFrequencyLimit(int f)
  {
    m_Limit = f;
  }
  




  public int getFrequencyLimit()
  {
    return m_Limit;
  }
  




  public String criticalValueTipText()
  {
    return "Specify critical value for specialization-generalization relationship (default 50).";
  }
  





  public void setCriticalValue(int c)
  {
    m_Critical = c;
  }
  




  public int getCriticalValue()
  {
    return m_Critical;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("The AODEsr Classifier");
    if (m_Instances == null) {
      text.append(": No model built yet.");
    } else {
      try {
        for (int i = 0; i < m_NumClasses; i++)
        {
          text.append("\nClass " + m_Instances.classAttribute().value(i) + ": Prior probability = " + Utils.doubleToString((m_ClassCounts[i] + 1.0D) / (m_SumInstances + m_NumClasses), 4, 2) + "\n\n");
        }
        



        text.append("Dataset: " + m_Instances.relationName() + "\n" + "Instances: " + m_NumInstances + "\n" + "Attributes: " + m_NumAttributes + "\n" + "Frequency limit for superParents: " + m_Limit + "\n" + "Critical value for the specializtion-generalization " + "relationship: " + m_Critical + "\n");
        




        if (m_Laplace) {
          text.append("Using LapLace estimation.");
        } else {
          text.append("Using m-estimation, m = " + m_MWeight);
        }
      } catch (Exception ex) {
        text.append(ex.getMessage());
      }
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new AODEsr(), argv);
  }
}
