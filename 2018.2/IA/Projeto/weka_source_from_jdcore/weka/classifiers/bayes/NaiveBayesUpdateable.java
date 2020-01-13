package weka.classifiers.bayes;

import weka.classifiers.UpdateableClassifier;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;









































































public class NaiveBayesUpdateable
  extends NaiveBayes
  implements UpdateableClassifier
{
  static final long serialVersionUID = -5354015843807192221L;
  
  public NaiveBayesUpdateable() {}
  
  public String globalInfo()
  {
    return "Class for a Naive Bayes classifier using estimator classes. This is the updateable version of NaiveBayes.\nThis classifier will use a default precision of 0.1 for numeric attributes when buildClassifier is called with zero training instances.\n\nFor more information on Naive Bayes classifiers, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    return super.getTechnicalInformation();
  }
  





  public void setUseSupervisedDiscretization(boolean newblah)
  {
    if (newblah) {
      throw new IllegalArgumentException("Can't use discretization in NaiveBayesUpdateable!");
    }
    
    m_UseDiscretization = false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new NaiveBayesUpdateable(), argv);
  }
}
