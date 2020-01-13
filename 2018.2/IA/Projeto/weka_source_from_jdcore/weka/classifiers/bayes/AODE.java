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































































































































public class AODE
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = 9197439980415113523L;
  private double[][][] m_CondiCounts;
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
  

  private boolean m_MEstimates = false;
  

  private int m_Weight = 1;
  


  public AODE() {}
  


  public String globalInfo()
  {
    return "AODE achieves highly accurate classification by averaging over all of a small space of alternative naive-Bayes-like models that have weaker (and hence less detrimental) independence assumptions than naive Bayes. The resulting algorithm is computationally efficient while delivering highly accurate classification on many learning  tasks.\n\nFor more information, see\n\n" + getTechnicalInformation().toString() + "\n\n" + "Further papers are available at\n" + "  http://www.csse.monash.edu.au/~webb/.\n\n" + "Can use an m-estimate for smoothing base probability estimates " + "in place of the Laplace correction (via option -M).\n" + "Default frequency limit set to 1.";
  }
  




















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "G. Webb and J. Boughton and Z. Wang");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.TITLE, "Not So Naive Bayes: Aggregating One-Dependence Estimators");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "58");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "5-24");
    
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
    m_NumAttributes = m_Instances.numAttributes();
    m_NumClasses = m_Instances.numClasses();
    

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
        
        for (int Att2 = 0; Att2 < m_NumAttributes; Att2++) {
          if (attIndex[Att2] != -1) {
            countsPointer[attIndex[Att2]] += weight;
          }
        }
      }
    }
  }
  








  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs = new double[m_NumClasses];
    











    int[] attIndex = new int[m_NumAttributes];
    for (int att = 0; att < m_NumAttributes; att++) {
      if ((instance.isMissing(att)) || (att == m_ClassIndex)) {
        attIndex[att] = -1;
      } else {
        attIndex[att] = (m_StartAttIndex[att] + (int)instance.value(att));
      }
    }
    
    for (int classVal = 0; classVal < m_NumClasses; classVal++)
    {
      probs[classVal] = 0.0D;
      double spodeP = 0.0D;
      int parentCount = 0;
      
      double[][] countsForClass = m_CondiCounts[classVal];
      

      for (int parent = 0; parent < m_NumAttributes; parent++) {
        if (attIndex[parent] != -1)
        {


          int pIndex = attIndex[parent];
          

          if (m_Frequencies[pIndex] >= m_Limit)
          {

            double[] countsForClassParent = countsForClass[pIndex];
            

            attIndex[parent] = -1;
            
            parentCount++;
            

            double classparentfreq = countsForClassParent[pIndex];
            

            double missing4ParentAtt = m_Frequencies[(m_StartAttIndex[parent] + m_NumAttValues[parent])];
            


            if (!m_MEstimates) {
              spodeP = (classparentfreq + 1.0D) / (m_SumInstances - missing4ParentAtt + m_NumClasses * m_NumAttValues[parent]);
            }
            else
            {
              spodeP = (classparentfreq + m_Weight / (m_NumClasses * m_NumAttValues[parent])) / (m_SumInstances - missing4ParentAtt + m_Weight);
            }
            



            for (int att = 0; att < m_NumAttributes; att++) {
              if (attIndex[att] != -1)
              {

                double missingForParentandChildAtt = countsForClassParent[(m_StartAttIndex[att] + m_NumAttValues[att])];
                

                if (!m_MEstimates) {
                  spodeP *= (countsForClassParent[attIndex[att]] + 1.0D) / (classparentfreq - missingForParentandChildAtt + m_NumAttValues[att]);
                }
                else
                {
                  spodeP *= (countsForClassParent[attIndex[att]] + m_Weight / m_NumAttValues[att]) / (classparentfreq - missingForParentandChildAtt + m_Weight);
                }
              }
            }
            



            probs[classVal] += spodeP;
            

            attIndex[parent] = pIndex;
          }
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
  {
    double prob;
    



    double prob;
    


    if (!m_MEstimates) {
      prob = (m_ClassCounts[classVal] + 1.0D) / (m_SumInstances + m_NumClasses);
    } else {
      prob = (m_ClassCounts[classVal] + m_Weight / m_NumClasses) / (m_SumInstances + m_Weight);
    }
    

    double[][] pointer = m_CondiCounts[classVal];
    

    for (int att = 0; att < m_NumAttributes; att++) {
      if ((att != m_ClassIndex) && (!instance.isMissing(att)))
      {


        int aIndex = m_StartAttIndex[att] + (int)instance.value(att);
        
        if (!m_MEstimates) {
          prob *= (pointer[aIndex][aIndex] + 1.0D) / (m_SumForCounts[classVal][att] + m_NumAttValues[att]);
        }
        else {
          prob *= (pointer[aIndex][aIndex] + m_Weight / m_NumAttValues[att]) / (m_SumForCounts[classVal][att] + m_Weight);
        }
      }
    }
    
    return prob;
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tOutput debugging information\n", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tImpose a frequency limit for superParents\n\t(default is 1)", "F", 1, "-F <int>"));
    

    newVector.addElement(new Option("\tUse m-estimate instead of laplace correction\n", "M", 0, "-M"));
    

    newVector.addElement(new Option("\tSpecify a weight to use with m-estimate\n\t(default is 1)", "W", 1, "-W <int>"));
    

    return newVector.elements();
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    m_Debug = Utils.getFlag('D', options);
    
    String Freq = Utils.getOption('F', options);
    if (Freq.length() != 0) {
      m_Limit = Integer.parseInt(Freq);
    } else {
      m_Limit = 1;
    }
    m_MEstimates = Utils.getFlag('M', options);
    String weight = Utils.getOption('W', options);
    if (weight.length() != 0) {
      if (!m_MEstimates)
        throw new Exception("Can't use Laplace AND m-estimate weight. Choose one.");
      m_Weight = Integer.parseInt(weight);

    }
    else if (m_MEstimates) {
      m_Weight = 1;
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
    
    if (m_MEstimates) {
      result.add("-M");
      result.add("-W");
      result.add("" + m_Weight);
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String weightTipText()
  {
    return "Set the weight for m-estimate.";
  }
  




  public void setWeight(int w)
  {
    if (!getUseMEstimates()) {
      System.out.println("Weight is only used in conjunction with m-estimate - ignored!");


    }
    else if (w > 0) {
      m_Weight = w;
    } else {
      System.out.println("Weight must be greater than 0!");
    }
  }
  




  public int getWeight()
  {
    return m_Weight;
  }
  




  public String useMEstimatesTipText()
  {
    return "Use m-estimate instead of laplace correction.";
  }
  




  public boolean getUseMEstimates()
  {
    return m_MEstimates;
  }
  




  public void setUseMEstimates(boolean value)
  {
    m_MEstimates = value;
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
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("The AODE Classifier");
    if (m_Instances == null) {
      text.append(": No model built yet.");
    } else {
      try {
        for (int i = 0; i < m_NumClasses; i++)
        {
          text.append("\nClass " + m_Instances.classAttribute().value(i) + ": Prior probability = " + Utils.doubleToString((m_ClassCounts[i] + 1.0D) / (m_SumInstances + m_NumClasses), 4, 2) + "\n\n");
        }
        



        text.append("Dataset: " + m_Instances.relationName() + "\n" + "Instances: " + m_NumInstances + "\n" + "Attributes: " + m_NumAttributes + "\n" + "Frequency limit for superParents: " + m_Limit + "\n");
        


        text.append("Correction: ");
        if (!m_MEstimates) {
          text.append("laplace\n");
        } else {
          text.append("m-estimate (m=" + m_Weight + ")\n");
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
    runClassifier(new AODE(), argv);
  }
}
