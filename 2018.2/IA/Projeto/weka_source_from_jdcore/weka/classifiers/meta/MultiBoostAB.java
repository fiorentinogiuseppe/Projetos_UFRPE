package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




























































































































public class MultiBoostAB
  extends AdaBoostM1
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -6681619178187935148L;
  protected int m_NumSubCmtys = 3;
  

  protected Random m_Random = null;
  


  public MultiBoostAB() {}
  

  public String globalInfo()
  {
    return "Class for boosting a classifier using the MultiBoosting method.\n\nMultiBoosting is an extension to the highly successful AdaBoost technique for forming decision committees. MultiBoosting can be viewed as combining AdaBoost with wagging. It is able to harness both AdaBoost's high bias and variance reduction with wagging's superior variance reduction. Using C4.5 as the base learning algorithm, Multi-boosting is demonstrated to produce decision committees with lower error than either AdaBoost or wagging significantly more often than the reverse over a large representative cross-section of UCI data sets. It offers the further advantage over AdaBoost of suiting parallel execution.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  




















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Geoffrey I. Webb");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.TITLE, "MultiBoosting: A Technique for Combining Boosting and Wagging");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "Vol.40");
    result.setValue(TechnicalInformation.Field.NUMBER, "No.2");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Kluwer Academic Publishers");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Boston");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Enumeration enu = super.listOptions();
    Vector vec = new Vector(1);
    
    vec.addElement(new Option("\tNumber of sub-committees. (Default 3)", "C", 1, "-C <num>"));
    

    while (enu.hasMoreElements()) {
      vec.addElement(enu.nextElement());
    }
    return vec.elements();
  }
  














































  public void setOptions(String[] options)
    throws Exception
  {
    String subcmtyString = Utils.getOption('C', options);
    if (subcmtyString.length() != 0) {
      setNumSubCmtys(Integer.parseInt(subcmtyString));
    } else {
      setNumSubCmtys(3);
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] ops = super.getOptions();
    String[] options = new String[ops.length + 2];
    options[0] = "-C";options[1] = ("" + getNumSubCmtys());
    System.arraycopy(ops, 0, options, 2, ops.length);
    return options;
  }
  




  public String numSubCmtysTipText()
  {
    return "Sets the (approximate) number of subcommittees.";
  }
  






  public void setNumSubCmtys(int subc)
  {
    m_NumSubCmtys = subc;
  }
  





  public int getNumSubCmtys()
  {
    return m_NumSubCmtys;
  }
  





  public void buildClassifier(Instances training)
    throws Exception
  {
    m_Random = new Random(m_Seed);
    
    super.buildClassifier(training);
    
    m_Random = null;
  }
  







  protected void setWeights(Instances training, double reweight)
    throws Exception
  {
    int subCmtySize = m_Classifiers.length / m_NumSubCmtys;
    
    if ((m_NumIterationsPerformed + 1) % subCmtySize == 0)
    {
      if (getDebug()) {
        System.err.println(m_NumIterationsPerformed + " " + subCmtySize);
      }
      double oldSumOfWeights = training.sumOfWeights();
      

      for (int i = 0; i < training.numInstances(); i++) {
        training.instance(i).setWeight(-Math.log(m_Random.nextDouble() * 9999.0D / 10000.0D));
      }
      

      double sumProbs = training.sumOfWeights();
      for (int i = 0; i < training.numInstances(); i++) {
        training.instance(i).setWeight(training.instance(i).weight() * oldSumOfWeights / sumProbs);
      }
    } else {
      super.setWeights(training, reweight);
    }
  }
  






  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    StringBuffer text = new StringBuffer();
    
    if (m_NumIterations == 0) {
      text.append("MultiBoostAB: No model built yet.\n");
    } else if (m_NumIterations == 1) {
      text.append("MultiBoostAB: No boosting possible, one classifier used!\n");
      text.append(m_Classifiers[0].toString() + "\n");
    } else {
      text.append("MultiBoostAB: Base classifiers and their weights: \n\n");
      for (int i = 0; i < m_NumIterations; i++) {
        if ((m_Classifiers != null) && (m_Classifiers[i] != null)) {
          text.append(m_Classifiers[i].toString() + "\n\n");
          text.append("Weight: " + Utils.roundDouble(m_Betas[i], 2) + "\n\n");
        }
        else {
          text.append("not yet initialized!\n\n");
        }
      }
      text.append("Number of performed Iterations: " + m_NumIterations + "\n");
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new MultiBoostAB(), argv);
  }
}
