package weka.classifiers.bayes.net.search.global;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;






























































































public class K2
  extends GlobalScoreSearchAlgorithm
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -6626871067466338256L;
  boolean m_bRandomOrder = false;
  




  public K2() {}
  



  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "G.F. Cooper and E. Herskovits");
    result.setValue(TechnicalInformation.Field.YEAR, "1990");
    result.setValue(TechnicalInformation.Field.TITLE, "A Bayesian method for constructing Bayesian belief networks from databases");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the Conference on Uncertainty in AI");
    result.setValue(TechnicalInformation.Field.PAGES, "86-94");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "G. Cooper and E. Herskovits");
    additional.setValue(TechnicalInformation.Field.YEAR, "1992");
    additional.setValue(TechnicalInformation.Field.TITLE, "A Bayesian method for the induction of probabilistic networks from data");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    additional.setValue(TechnicalInformation.Field.VOLUME, "9");
    additional.setValue(TechnicalInformation.Field.NUMBER, "4");
    additional.setValue(TechnicalInformation.Field.PAGES, "309-347");
    
    return result;
  }
  







  public void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    int[] nOrder = new int[instances.numAttributes()];
    nOrder[0] = instances.classIndex();
    
    int nAttribute = 0;
    
    for (int iOrder = 1; iOrder < instances.numAttributes(); iOrder++) {
      if (nAttribute == instances.classIndex()) {
        nAttribute++;
      }
      nOrder[iOrder] = (nAttribute++);
    }
    
    if (m_bRandomOrder)
    {
      Random random = new Random();
      int iClass;
      int iClass; if (getInitAsNaiveBayes()) {
        iClass = 0;
      } else {
        iClass = -1;
      }
      for (int iOrder = 0; iOrder < instances.numAttributes(); iOrder++) {
        int iOrder2 = Math.abs(random.nextInt()) % instances.numAttributes();
        if ((iOrder != iClass) && (iOrder2 != iClass)) {
          int nTmp = nOrder[iOrder];
          nOrder[iOrder] = nOrder[iOrder2];
          nOrder[iOrder2] = nTmp;
        }
      }
    }
    

    double fBaseScore = calcScore(bayesNet);
    

    for (int iOrder = 1; iOrder < instances.numAttributes(); iOrder++) {
      int iAttribute = nOrder[iOrder];
      double fBestScore = fBaseScore;
      
      boolean bProgress = bayesNet.getParentSet(iAttribute).getNrOfParents() < getMaxNrOfParents();
      while ((bProgress) && (bayesNet.getParentSet(iAttribute).getNrOfParents() < getMaxNrOfParents())) {
        int nBestAttribute = -1;
        for (int iOrder2 = 0; iOrder2 < iOrder; iOrder2++) {
          int iAttribute2 = nOrder[iOrder2];
          double fScore = calcScoreWithExtraParent(iAttribute, iAttribute2);
          if (fScore > fBestScore) {
            fBestScore = fScore;
            nBestAttribute = iAttribute2;
          }
        }
        if (nBestAttribute != -1) {
          bayesNet.getParentSet(iAttribute).addParent(nBestAttribute, instances);
          fBaseScore = fBestScore;
          bProgress = true;
        } else {
          bProgress = false;
        }
      }
    }
  }
  




  public void setMaxNrOfParents(int nMaxNrOfParents)
  {
    m_nMaxNrOfParents = nMaxNrOfParents;
  }
  




  public int getMaxNrOfParents()
  {
    return m_nMaxNrOfParents;
  }
  




  public void setInitAsNaiveBayes(boolean bInitAsNaiveBayes)
  {
    m_bInitAsNaiveBayes = bInitAsNaiveBayes;
  }
  




  public boolean getInitAsNaiveBayes()
  {
    return m_bInitAsNaiveBayes;
  }
  




  public void setRandomOrder(boolean bRandomOrder)
  {
    m_bRandomOrder = bRandomOrder;
  }
  




  public boolean getRandomOrder()
  {
    return m_bRandomOrder;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(0);
    
    newVector.addElement(new Option("\tInitial structure is empty (instead of Naive Bayes)", "N", 0, "-N"));
    

    newVector.addElement(new Option("\tMaximum number of parents", "P", 1, "-P <nr of parents>"));
    

    newVector.addElement(new Option("\tRandom order.\n\t(default false)", "R", 0, "-R"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  

































  public void setOptions(String[] options)
    throws Exception
  {
    setRandomOrder(Utils.getFlag('R', options));
    
    m_bInitAsNaiveBayes = (!Utils.getFlag('N', options));
    
    String sMaxNrOfParents = Utils.getOption('P', options);
    
    if (sMaxNrOfParents.length() != 0) {
      setMaxNrOfParents(Integer.parseInt(sMaxNrOfParents));
    } else {
      setMaxNrOfParents(100000);
    }
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[4 + superOptions.length];
    int current = 0;
    options[(current++)] = "-P";
    options[(current++)] = ("" + m_nMaxNrOfParents);
    if (!m_bInitAsNaiveBayes) {
      options[(current++)] = "-N";
    }
    if (getRandomOrder()) {
      options[(current++)] = "-R";
    }
    
    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  


  public String randomOrderTipText()
  {
    return "When set to true, the order of the nodes in the network is random. Default random order is false and the order of the nodes in the dataset is used. In any case, when the network was initialized as Naive Bayes Network, the class variable is first in the ordering though.";
  }
  







  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses a hill climbing algorithm restricted by an order on the variables.\n\nFor more information see:\n\n" + getTechnicalInformation().toString() + "\n\n" + "Works with nominal variables and no missing values only.";
  }
  









  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
