package weka.classifiers.bayes.net.search.local;

import java.util.Enumeration;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;









































































public class TAN
  extends LocalScoreSearchAlgorithm
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 965182127977228690L;
  
  public TAN() {}
  
  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "N. Friedman and D. Geiger and M. Goldszmidt");
    result.setValue(TechnicalInformation.Field.YEAR, "1997");
    result.setValue(TechnicalInformation.Field.TITLE, "Bayesian network classifiers");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "29");
    result.setValue(TechnicalInformation.Field.NUMBER, "2-3");
    result.setValue(TechnicalInformation.Field.PAGES, "131-163");
    
    return result;
  }
  







  public void buildStructure(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_bInitAsNaiveBayes = true;
    m_nMaxNrOfParents = 2;
    super.buildStructure(bayesNet, instances);
    int nNrOfAtts = instances.numAttributes();
    
    if (nNrOfAtts <= 2) {
      return;
    }
    

    double[] fBaseScores = new double[instances.numAttributes()];
    
    for (int iAttribute = 0; iAttribute < nNrOfAtts; iAttribute++) {
      fBaseScores[iAttribute] = calcNodeScore(iAttribute);
    }
    

    double[][] fScore = new double[nNrOfAtts][nNrOfAtts];
    
    for (int iAttributeHead = 0; iAttributeHead < nNrOfAtts; iAttributeHead++) {
      for (int iAttributeTail = 0; iAttributeTail < nNrOfAtts; iAttributeTail++) {
        if (iAttributeHead != iAttributeTail) {
          fScore[iAttributeHead][iAttributeTail] = calcScoreWithExtraParent(iAttributeHead, iAttributeTail);
        }
      }
    }
    





    int nClassNode = instances.classIndex();
    int[] link1 = new int[nNrOfAtts - 1];
    int[] link2 = new int[nNrOfAtts - 1];
    boolean[] linked = new boolean[nNrOfAtts];
    

    int nBestLinkNode1 = -1;
    int nBestLinkNode2 = -1;
    double fBestDeltaScore = 0.0D;
    
    for (int iLinkNode1 = 0; iLinkNode1 < nNrOfAtts; iLinkNode1++) {
      if (iLinkNode1 != nClassNode) {
        for (int iLinkNode2 = 0; iLinkNode2 < nNrOfAtts; iLinkNode2++) {
          if ((iLinkNode1 != iLinkNode2) && (iLinkNode2 != nClassNode) && ((nBestLinkNode1 == -1) || (fScore[iLinkNode1][iLinkNode2] - fBaseScores[iLinkNode1] > fBestDeltaScore)))
          {


            fBestDeltaScore = fScore[iLinkNode1][iLinkNode2] - fBaseScores[iLinkNode1];
            nBestLinkNode1 = iLinkNode2;
            nBestLinkNode2 = iLinkNode1;
          }
        }
      }
    }
    link1[0] = nBestLinkNode1;
    link2[0] = nBestLinkNode2;
    linked[nBestLinkNode1] = true;
    linked[nBestLinkNode2] = true;
    


    for (int iLink = 1; iLink < nNrOfAtts - 2; iLink++) {
      nBestLinkNode1 = -1;
      for (iLinkNode1 = 0; iLinkNode1 < nNrOfAtts; iLinkNode1++) {
        if (iLinkNode1 != nClassNode) {
          for (int iLinkNode2 = 0; iLinkNode2 < nNrOfAtts; iLinkNode2++) {
            if ((iLinkNode1 != iLinkNode2) && (iLinkNode2 != nClassNode) && ((linked[iLinkNode1] != 0) || (linked[iLinkNode2] != 0)) && ((linked[iLinkNode1] == 0) || (linked[iLinkNode2] == 0)) && ((nBestLinkNode1 == -1) || (fScore[iLinkNode1][iLinkNode2] - fBaseScores[iLinkNode1] > fBestDeltaScore)))
            {





              fBestDeltaScore = fScore[iLinkNode1][iLinkNode2] - fBaseScores[iLinkNode1];
              nBestLinkNode1 = iLinkNode2;
              nBestLinkNode2 = iLinkNode1;
            }
          }
        }
      }
      
      link1[iLink] = nBestLinkNode1;
      link2[iLink] = nBestLinkNode2;
      linked[nBestLinkNode1] = true;
      linked[nBestLinkNode2] = true;
    }
    

    boolean[] hasParent = new boolean[nNrOfAtts];
    for (int iLink = 0; iLink < nNrOfAtts - 2; iLink++) {
      if (hasParent[link1[iLink]] == 0) {
        bayesNet.getParentSet(link1[iLink]).addParent(link2[iLink], instances);
        hasParent[link1[iLink]] = true;
      } else {
        if (hasParent[link2[iLink]] != 0) {
          throw new Exception("Bug condition found: too many arrows");
        }
        bayesNet.getParentSet(link2[iLink]).addParent(link1[iLink], instances);
        hasParent[link2[iLink]] = true;
      }
    }
  }
  






  public Enumeration listOptions()
  {
    return super.listOptions();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    return super.getOptions();
  }
  



  public String globalInfo()
  {
    return "This Bayes Network learning algorithm determines the maximum weight spanning tree  and returns a Naive Bayes network augmented with a tree.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  








  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6235 $");
  }
}
