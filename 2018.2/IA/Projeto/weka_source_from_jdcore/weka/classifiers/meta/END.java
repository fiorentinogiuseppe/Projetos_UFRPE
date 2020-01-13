package weka.classifiers.meta;

import java.util.Hashtable;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.meta.nestedDichotomies.ClassBalancedND;
import weka.classifiers.meta.nestedDichotomies.DataNearBalancedND;
import weka.classifiers.meta.nestedDichotomies.ND;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;













































































































































public class END
  extends RandomizableIteratedSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -4143242362912214956L;
  protected Hashtable m_hashtable = null;
  



  public END()
  {
    m_Classifier = new ND();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.meta.nestedDichotomies.ND";
  }
  





  public String globalInfo()
  {
    return "A meta classifier for handling multi-class datasets with 2-class classifiers by building an ensemble of nested dichotomies.\n\nFor more info, check\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Lin Dong and Eibe Frank and Stefan Kramer");
    result.setValue(TechnicalInformation.Field.TITLE, "Ensembles of Balanced Nested Dichotomies for Multi-class Problems");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "PKDD");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.PAGES, "84-95");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Stefan Kramer");
    additional.setValue(TechnicalInformation.Field.TITLE, "Ensembles of nested dichotomies for multi-class problems");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Twenty-first International Conference on Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "2004");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "ACM");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.setMinimumNumberInstances(1);
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if ((!(m_Classifier instanceof ND)) && (!(m_Classifier instanceof ClassBalancedND)) && (!(m_Classifier instanceof DataNearBalancedND)))
    {

      throw new IllegalArgumentException("END only works with ND, ClassBalancedND or DataNearBalancedND classifier");
    }
    

    m_hashtable = new Hashtable();
    
    m_Classifiers = Classifier.makeCopies(m_Classifier, m_NumIterations);
    
    Random random = data.getRandomNumberGenerator(m_Seed);
    for (int j = 0; j < m_Classifiers.length; j++)
    {

      ((Randomizable)m_Classifiers[j]).setSeed(random.nextInt());
      

      if ((m_Classifier instanceof ND)) {
        ((ND)m_Classifiers[j]).setHashtable(m_hashtable);
      } else if ((m_Classifier instanceof ClassBalancedND)) {
        ((ClassBalancedND)m_Classifiers[j]).setHashtable(m_hashtable);
      } else if ((m_Classifier instanceof DataNearBalancedND)) {
        ((DataNearBalancedND)m_Classifiers[j]).setHashtable(m_hashtable);
      }
      

      m_Classifiers[j].buildClassifier(data);
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] sums = new double[instance.numClasses()];
    
    for (int i = 0; i < m_NumIterations; i++) {
      if (instance.classAttribute().isNumeric() == true) {
        sums[0] += m_Classifiers[i].classifyInstance(instance);
      } else {
        double[] newProbs = m_Classifiers[i].distributionForInstance(instance);
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
  






  public String toString()
  {
    if (m_Classifiers == null) {
      return "END: No model built yet.";
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
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new END(), argv);
  }
}
