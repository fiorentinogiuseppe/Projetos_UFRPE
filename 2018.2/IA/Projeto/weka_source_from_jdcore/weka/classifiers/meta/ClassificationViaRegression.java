package weka.classifiers.meta;

import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.trees.M5P;
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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;




























































































public class ClassificationViaRegression
  extends SingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 4500023123618669859L;
  private Classifier[] m_Classifiers;
  private MakeIndicator[] m_ClassFilters;
  
  public ClassificationViaRegression()
  {
    m_Classifier = new M5P();
  }
  





  public String globalInfo()
  {
    return "Class for doing classification using regression methods. Class is binarized and one regression model is built for each class value. For more information, see, for example\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "E. Frank and Y. Wang and S. Inglis and G. Holmes and I.H. Witten");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Using model trees for classification");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "32");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "63-76");
    
    return result;
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.M5P";
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  








  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    m_Classifiers = Classifier.makeCopies(m_Classifier, insts.numClasses());
    m_ClassFilters = new MakeIndicator[insts.numClasses()];
    for (int i = 0; i < insts.numClasses(); i++) {
      m_ClassFilters[i] = new MakeIndicator();
      m_ClassFilters[i].setAttributeIndex("" + (insts.classIndex() + 1));
      m_ClassFilters[i].setValueIndex(i);
      m_ClassFilters[i].setNumeric(true);
      m_ClassFilters[i].setInputFormat(insts);
      Instances newInsts = Filter.useFilter(insts, m_ClassFilters[i]);
      m_Classifiers[i].buildClassifier(newInsts);
    }
  }
  






  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    double[] probs = new double[inst.numClasses()];
    
    double sum = 0.0D;
    
    for (int i = 0; i < inst.numClasses(); i++) {
      m_ClassFilters[i].input(inst);
      m_ClassFilters[i].batchFinished();
      Instance newInst = m_ClassFilters[i].output();
      probs[i] = m_Classifiers[i].classifyInstance(newInst);
      if (probs[i] > 1.0D) {
        probs[i] = 1.0D;
      }
      if (probs[i] < 0.0D) {
        probs[i] = 0.0D;
      }
      sum += probs[i];
    }
    if (sum != 0.0D) {
      Utils.normalize(probs, sum);
    }
    return probs;
  }
  





  public String toString()
  {
    if (m_Classifiers == null) {
      return "Classification via Regression: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("Classification via Regression\n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append("Classifier for class with index " + i + ":\n\n");
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.27 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new ClassificationViaRegression(), argv);
  }
}
