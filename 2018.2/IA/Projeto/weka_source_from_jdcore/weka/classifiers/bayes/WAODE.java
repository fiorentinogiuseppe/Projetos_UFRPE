package weka.classifiers.bayes;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;


























































































public class WAODE
  extends Classifier
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 2170978824284697882L;
  private double[] m_ClassCounts;
  private double[] m_AttCounts;
  private double[][] m_AttAttCounts;
  private double[][][] m_ClassAttAttCounts;
  private int[] m_NumAttValues;
  private int m_TotalAttValues;
  private int m_NumClasses;
  private int m_NumAttributes;
  private int m_NumInstances;
  private int m_ClassIndex;
  private int[] m_StartAttIndex;
  private double[] m_mutualInformation;
  private Instances m_Header = null;
  


  private boolean m_Internals = false;
  

  private Classifier m_ZeroR;
  


  public WAODE() {}
  

  public String globalInfo()
  {
    return "WAODE contructs the model called Weightily Averaged One-Dependence Estimators.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tWhether to print some more internals.\n\t(default: no)", "I", 0, "-I"));
    



    return result.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setInternals(Utils.getFlag('I', options));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getInternals()) {
      result.add("-I");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String internalsTipText()
  {
    return "Prints more internals of the classifier.";
  }
  





  public void setInternals(boolean value)
  {
    m_Internals = value;
  }
  




  public boolean getInternals()
  {
    return m_Internals;
  }
  








  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "L. Jiang and H. Zhang");
    result.setValue(TechnicalInformation.Field.TITLE, "Weightily Averaged One-Dependence Estimators");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 9th Biennial Pacific Rim International Conference on Artificial Intelligence, PRICAI 2006");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.PAGES, "970-974");
    result.setValue(TechnicalInformation.Field.SERIES, "LNAI");
    result.setValue(TechnicalInformation.Field.VOLUME, "4099");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    if (instances.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(instances);
      return;
    }
    
    m_ZeroR = null;
    


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
    m_AttCounts = new double[m_TotalAttValues];
    m_AttAttCounts = new double[m_TotalAttValues][m_TotalAttValues];
    m_ClassAttAttCounts = new double[m_NumClasses][m_TotalAttValues][m_TotalAttValues];
    m_Header = new Instances(instances, 0);
    

    for (int k = 0; k < m_NumInstances; k++) {
      int classVal = (int)instances.instance(k).classValue();
      m_ClassCounts[classVal] += 1.0D;
      int[] attIndex = new int[m_NumAttributes];
      for (int i = 0; i < m_NumAttributes; i++) {
        if (i == m_ClassIndex) {
          attIndex[i] = -1;
        }
        else {
          attIndex[i] = (m_StartAttIndex[i] + (int)instances.instance(k).value(i));
          m_AttCounts[attIndex[i]] += 1.0D;
        }
      }
      for (int Att1 = 0; Att1 < m_NumAttributes; Att1++) {
        if (attIndex[Att1] != -1) {
          for (int Att2 = 0; Att2 < m_NumAttributes; Att2++) {
            if (attIndex[Att2] != -1) {
              m_AttAttCounts[attIndex[Att1]][attIndex[Att2]] += 1.0D;
              m_ClassAttAttCounts[classVal][attIndex[Att1]][attIndex[Att2]] += 1.0D;
            }
          }
        }
      }
    }
    
    m_mutualInformation = new double[m_NumAttributes];
    for (int att = 0; att < m_NumAttributes; att++) {
      if (att != m_ClassIndex) {
        m_mutualInformation[att] = mutualInfo(att);
      }
    }
  }
  





  private double mutualInfo(int att)
  {
    double mutualInfo = 0.0D;
    int attIndex = m_StartAttIndex[att];
    double[] PriorsClass = new double[m_NumClasses];
    double[] PriorsAttribute = new double[m_NumAttValues[att]];
    double[][] PriorsClassAttribute = new double[m_NumClasses][m_NumAttValues[att]];
    
    for (int i = 0; i < m_NumClasses; i++) {
      PriorsClass[i] = (m_ClassCounts[i] / m_NumInstances);
    }
    
    for (int j = 0; j < m_NumAttValues[att]; j++) {
      PriorsAttribute[j] = (m_AttCounts[(attIndex + j)] / m_NumInstances);
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[att]; j++) {
        PriorsClassAttribute[i][j] = (m_ClassAttAttCounts[i][(attIndex + j)][(attIndex + j)] / m_NumInstances);
      }
    }
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumAttValues[att]; j++) {
        mutualInfo += PriorsClassAttribute[i][j] * log2(PriorsClassAttribute[i][j], PriorsClass[i] * PriorsAttribute[j]);
      }
    }
    return mutualInfo;
  }
  







  private double log2(double x, double y)
  {
    if ((x < Utils.SMALL) || (y < Utils.SMALL)) {
      return 0.0D;
    }
    return Math.log(x / y) / Math.log(2.0D);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    

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
      probs[classVal] = 0.0D;
      double prob = 1.0D;
      double mutualInfoSum = 0.0D;
      for (int parent = 0; parent < m_NumAttributes; parent++)
        if (attIndex[parent] != -1) {
          prob = (m_ClassAttAttCounts[classVal][attIndex[parent]][attIndex[parent]] + 1.0D / (m_NumClasses * m_NumAttValues[parent])) / (m_NumInstances + 1.0D);
          for (int son = 0; son < m_NumAttributes; son++) {
            if ((attIndex[son] != -1) && (son != parent))
              prob *= (m_ClassAttAttCounts[classVal][attIndex[parent]][attIndex[son]] + 1.0D / m_NumAttValues[son]) / (m_ClassAttAttCounts[classVal][attIndex[parent]][attIndex[parent]] + 1.0D);
          }
          mutualInfoSum += m_mutualInformation[parent];
          probs[classVal] += m_mutualInformation[parent] * prob;
        }
      probs[classVal] /= mutualInfoSum;
    }
    if (!Double.isNaN(Utils.sum(probs)))
      Utils.normalize(probs);
    return probs;
  }
  




  public String toString()
  {
    StringBuffer result;
    



    if (m_ZeroR != null) {
      StringBuffer result = new StringBuffer();
      result.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      result.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      result.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      result.append(m_ZeroR.toString());
    }
    else {
      String classname = getClass().getName().replaceAll(".*\\.", "");
      result = new StringBuffer();
      result.append(classname + "\n");
      result.append(classname.replaceAll(".", "=") + "\n\n");
      
      if (m_Header == null) {
        result.append("No Model built yet.\n");
      }
      else {
        if (getInternals()) {
          result.append("Mutual information of attributes with class attribute:\n");
          for (int i = 0; i < m_Header.numAttributes(); i++)
          {
            if (i != m_Header.classIndex())
            {

              result.append(i + 1 + ". " + m_Header.attribute(i).name() + ": " + Utils.doubleToString(m_mutualInformation[i], 6) + "\n");
            }
          }
        }
        

        result.append("Model built successfully.\n");
      }
    }
    

    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new WAODE(), argv);
  }
}
