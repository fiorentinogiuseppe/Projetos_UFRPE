package weka.classifiers.bayes;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




















































































public class HNB
  extends Classifier
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -4503874444306113214L;
  private double[] m_ClassCounts;
  private double[][][] m_ClassAttAttCounts;
  private int[] m_NumAttValues;
  private int m_TotalAttValues;
  private int m_NumClasses;
  private int m_NumAttributes;
  private int m_NumInstances;
  private int m_ClassIndex;
  private int[] m_StartAttIndex;
  private double[][] m_condiMutualInfo;
  
  public HNB() {}
  
  public String globalInfo()
  {
    return "Contructs Hidden Naive Bayes classification model with high classification accuracy and AUC.\n\nFor more information refer to:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "H. Zhang and L. Jiang and J. Su");
    result.setValue(TechnicalInformation.Field.TITLE, "Hidden Naive Bayes");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Twentieth National Conference on Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.PAGES, "919-924");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "AAAI Press");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    m_NumClasses = instances.numClasses();
    m_ClassIndex = instances.classIndex();
    m_NumAttributes = instances.numAttributes();
    m_NumInstances = instances.numInstances();
    m_TotalAttValues = 0;
    

    m_StartAttIndex = new int[m_NumAttributes];
    m_NumAttValues = new int[m_NumAttributes];
    


    for (int i = 0; i < m_NumAttributes; i++) {
      if (i != m_ClassIndex) {
        m_StartAttIndex[i] = m_TotalAttValues;
        m_NumAttValues[i] = instances.attribute(i).numValues();
        m_TotalAttValues += m_NumAttValues[i];
      }
      else {
        m_StartAttIndex[i] = -1;
        m_NumAttValues[i] = m_NumClasses;
      }
    }
    

    m_ClassCounts = new double[m_NumClasses];
    m_ClassAttAttCounts = new double[m_NumClasses][m_TotalAttValues][m_TotalAttValues];
    

    for (int k = 0; k < m_NumInstances; k++) {
      int classVal = (int)instances.instance(k).classValue();
      m_ClassCounts[classVal] += 1.0D;
      int[] attIndex = new int[m_NumAttributes];
      for (int i = 0; i < m_NumAttributes; i++) {
        if (i == m_ClassIndex) {
          attIndex[i] = -1;
        } else
          attIndex[i] = (m_StartAttIndex[i] + (int)instances.instance(k).value(i));
      }
      for (int Att1 = 0; Att1 < m_NumAttributes; Att1++) {
        if (attIndex[Att1] != -1) {
          for (int Att2 = 0; Att2 < m_NumAttributes; Att2++) {
            if (attIndex[Att2] != -1) {
              m_ClassAttAttCounts[classVal][attIndex[Att1]][attIndex[Att2]] += 1.0D;
            }
          }
        }
      }
    }
    
    m_condiMutualInfo = new double[m_NumAttributes][m_NumAttributes];
    for (int son = 0; son < m_NumAttributes; son++) {
      if (son != m_ClassIndex) {
        for (int parent = 0; parent < m_NumAttributes; parent++) {
          if ((parent != m_ClassIndex) && (son != parent)) {
            m_condiMutualInfo[son][parent] = conditionalMutualInfo(son, parent);
          }
        }
      }
    }
  }
  





  private double conditionalMutualInfo(int son, int parent)
    throws Exception
  {
    double CondiMutualInfo = 0.0D;
    int sIndex = m_StartAttIndex[son];
    int pIndex = m_StartAttIndex[parent];
    double[] PriorsClass = new double[m_NumClasses];
    double[][] PriorsClassSon = new double[m_NumClasses][m_NumAttValues[son]];
    double[][] PriorsClassParent = new double[m_NumClasses][m_NumAttValues[parent]];
    double[][][] PriorsClassParentSon = new double[m_NumClasses][m_NumAttValues[parent]][m_NumAttValues[son]];
    
    for (int i = 0; i < m_NumClasses; i++) {
      PriorsClass[i] = (m_ClassCounts[i] / m_NumInstances);
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[son]; j++) {
        PriorsClassSon[i][j] = (m_ClassAttAttCounts[i][(sIndex + j)][(sIndex + j)] / m_NumInstances);
      }
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[parent]; j++) {
        PriorsClassParent[i][j] = (m_ClassAttAttCounts[i][(pIndex + j)][(pIndex + j)] / m_NumInstances);
      }
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[parent]; j++) {
        for (int k = 0; k < m_NumAttValues[son]; k++) {
          PriorsClassParentSon[i][j][k] = (m_ClassAttAttCounts[i][(pIndex + j)][(sIndex + k)] / m_NumInstances);
        }
      }
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[parent]; j++) {
        for (int k = 0; k < m_NumAttValues[son]; k++) {
          CondiMutualInfo += PriorsClassParentSon[i][j][k] * log2(PriorsClassParentSon[i][j][k] * PriorsClass[i], PriorsClassParent[i][j] * PriorsClassSon[i][k]);
        }
      }
    }
    return CondiMutualInfo;
  }
  







  private double log2(double x, double y)
  {
    if ((x < 1.0E-6D) || (y < 1.0E-6D)) {
      return 0.0D;
    }
    return Math.log(x / y) / Math.log(2.0D);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs = new double[m_NumClasses];
    




    int[] attIndex = new int[m_NumAttributes];
    for (int att = 0; att < m_NumAttributes; att++) {
      if (att == m_ClassIndex) {
        attIndex[att] = -1;
      } else {
        attIndex[att] = (m_StartAttIndex[att] + (int)instance.value(att));
      }
    }
    
    for (int classVal = 0; classVal < m_NumClasses; classVal++) {
      probs[classVal] = ((m_ClassCounts[classVal] + 1.0D / m_NumClasses) / (m_NumInstances + 1.0D));
      for (int son = 0; son < m_NumAttributes; son++)
        if (attIndex[son] != -1) {
          int sIndex = attIndex[son];
          attIndex[son] = -1;
          double prob = 0.0D;
          double condiMutualInfoSum = 0.0D;
          for (int parent = 0; parent < m_NumAttributes; parent++)
            if (attIndex[parent] != -1) {
              condiMutualInfoSum += m_condiMutualInfo[son][parent];
              prob += m_condiMutualInfo[son][parent] * (m_ClassAttAttCounts[classVal][attIndex[parent]][sIndex] + 1.0D / m_NumAttValues[son]) / (m_ClassAttAttCounts[classVal][attIndex[parent]][attIndex[parent]] + 1.0D);
            }
          if (condiMutualInfoSum > 0.0D) {
            prob /= condiMutualInfoSum;
            probs[classVal] *= prob;
          }
          else {
            prob = (m_ClassAttAttCounts[classVal][sIndex][sIndex] + 1.0D / m_NumAttValues[son]) / (m_ClassCounts[classVal] + 1.0D);
            probs[classVal] *= prob;
          }
          attIndex[son] = sIndex;
        }
    }
    Utils.normalize(probs);
    return probs;
  }
  





  public String toString()
  {
    return "HNB (Hidden Naive Bayes)";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new HNB(), args);
  }
}
