package weka.classifiers.bayes.net.search.local;

import java.io.PrintStream;
import java.util.Enumeration;
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

























































































public class TabuSearch
  extends HillClimber
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 1457344073228786447L;
  int m_nRuns = 10;
  

  int m_nTabuList = 5;
  

  HillClimber.Operation[] m_oTabuList = null;
  



  public TabuSearch() {}
  



  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R.R. Bouckaert");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.TITLE, "Bayesian Belief Networks: from Construction to Inference");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "University of Utrecht");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Utrecht, Netherlands");
    
    return result;
  }
  






  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_oTabuList = new HillClimber.Operation[m_nTabuList];
    int iCurrentTabuList = 0;
    initCache(bayesNet, instances);
    


    double fCurrentScore = 0.0D;
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      fCurrentScore += calcNodeScore(iAttribute);
    }
    




    double fBestScore = fCurrentScore;
    BayesNet bestBayesNet = new BayesNet();
    m_Instances = instances;
    bestBayesNet.initStructure();
    copyParentSets(bestBayesNet, bayesNet);
    


    for (int iRun = 0; iRun < m_nRuns; iRun++) {
      HillClimber.Operation oOperation = getOptimalOperation(bayesNet, instances);
      performOperation(bayesNet, instances, oOperation);
      
      if (oOperation == null) {
        throw new Exception("Panic: could not find any step to make. Tabu list too long?");
      }
      
      m_oTabuList[iCurrentTabuList] = oOperation;
      iCurrentTabuList = (iCurrentTabuList + 1) % m_nTabuList;
      
      fCurrentScore += m_fDeltaScore;
      
      if (fCurrentScore > fBestScore) {
        fBestScore = fCurrentScore;
        copyParentSets(bestBayesNet, bayesNet);
      }
      
      if (bayesNet.getDebug()) {
        printTabuList();
      }
    }
    

    copyParentSets(bayesNet, bestBayesNet);
    

    bestBayesNet = null;
    m_Cache = null;
  }
  






  void copyParentSets(BayesNet dest, BayesNet source)
  {
    int nNodes = source.getNrOfNodes();
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      dest.getParentSet(iNode).copy(source.getParentSet(iNode));
    }
  }
  





  boolean isNotTabu(HillClimber.Operation oOperation)
  {
    for (int iTabu = 0; iTabu < m_nTabuList; iTabu++) {
      if (oOperation.equals(m_oTabuList[iTabu])) {
        return false;
      }
    }
    return true;
  }
  

  void printTabuList()
  {
    for (int i = 0; i < m_nTabuList; i++) {
      HillClimber.Operation o = m_oTabuList[i];
      if (o != null) {
        if (m_nOperation == 0) System.out.print(" +("); else System.out.print(" -(");
        System.out.print(m_nTail + "->" + m_nHead + ")");
      }
    }
    System.out.println();
  }
  


  public int getRuns()
  {
    return m_nRuns;
  }
  



  public void setRuns(int nRuns)
  {
    m_nRuns = nRuns;
  }
  


  public int getTabuList()
  {
    return m_nTabuList;
  }
  



  public void setTabuList(int nTabuList)
  {
    m_nTabuList = nTabuList;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tTabu list length", "L", 1, "-L <integer>"));
    newVector.addElement(new Option("\tNumber of runs", "U", 1, "-U <integer>"));
    newVector.addElement(new Option("\tMaximum number of parents", "P", 1, "-P <nr of parents>"));
    newVector.addElement(new Option("\tUse arc reversal operation.\n\t(default false)", "R", 0, "-R"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  









































  public void setOptions(String[] options)
    throws Exception
  {
    String sTabuList = Utils.getOption('L', options);
    if (sTabuList.length() != 0) {
      setTabuList(Integer.parseInt(sTabuList));
    }
    String sRuns = Utils.getOption('U', options);
    if (sRuns.length() != 0) {
      setRuns(Integer.parseInt(sRuns));
    }
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[7 + superOptions.length];
    int current = 0;
    
    options[(current++)] = "-L";
    options[(current++)] = ("" + getTabuList());
    
    options[(current++)] = "-U";
    options[(current++)] = ("" + getRuns());
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  



  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses tabu search for finding a well scoring Bayes network structure. Tabu search is hill climbing till an optimum is reached. The following step is the least worst possible step. The last X steps are kept in a list and none of the steps in this so called tabu list is considered in taking the next step. The best network found in this traversal is returned.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  








  public String runsTipText()
  {
    return "Sets the number of steps to be performed.";
  }
  


  public String tabuListTipText()
  {
    return "Sets the length of the tabu list.";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}
