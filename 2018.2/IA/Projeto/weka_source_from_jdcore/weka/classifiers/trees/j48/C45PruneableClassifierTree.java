package weka.classifiers.trees.j48;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;


































public class C45PruneableClassifierTree
  extends ClassifierTree
{
  static final long serialVersionUID = -4813820170260388194L;
  boolean m_pruneTheTree = false;
  

  float m_CF = 0.25F;
  

  boolean m_subtreeRaising = true;
  

  boolean m_cleanup = true;
  














  public C45PruneableClassifierTree(ModelSelection toSelectLocModel, boolean pruneTree, float cf, boolean raiseTree, boolean cleanup)
    throws Exception
  {
    super(toSelectLocModel);
    
    m_pruneTheTree = pruneTree;
    m_CF = cf;
    m_subtreeRaising = raiseTree;
    m_cleanup = cleanup;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    buildTree(data, (m_subtreeRaising) || (!m_cleanup));
    collapse();
    if (m_pruneTheTree) {
      prune();
    }
    if (m_cleanup) {
      cleanup(new Instances(data, 0));
    }
  }
  







  public final void collapse()
  {
    if (!m_isLeaf) {
      double errorsOfSubtree = getTrainingErrors();
      double errorsOfTree = localModel().distribution().numIncorrect();
      if (errorsOfSubtree >= errorsOfTree - 0.001D)
      {

        m_sons = null;
        m_isLeaf = true;
        

        m_localModel = new NoSplit(localModel().distribution());
      } else {
        for (int i = 0; i < m_sons.length; i++) {
          son(i).collapse();
        }
      }
    }
  }
  









  public void prune()
    throws Exception
  {
    if (!m_isLeaf)
    {

      for (int i = 0; i < m_sons.length; i++) {
        son(i).prune();
      }
      
      int indexOfLargestBranch = localModel().distribution().maxBag();
      double errorsLargestBranch; double errorsLargestBranch; if (m_subtreeRaising) {
        errorsLargestBranch = son(indexOfLargestBranch).getEstimatedErrorsForBranch(m_train);
      }
      else {
        errorsLargestBranch = Double.MAX_VALUE;
      }
      

      double errorsLeaf = getEstimatedErrorsForDistribution(localModel().distribution());
      


      double errorsTree = getEstimatedErrors();
      

      if ((Utils.smOrEq(errorsLeaf, errorsTree + 0.1D)) && (Utils.smOrEq(errorsLeaf, errorsLargestBranch + 0.1D)))
      {


        m_sons = null;
        m_isLeaf = true;
        

        m_localModel = new NoSplit(localModel().distribution());
        return;
      }
      


      if (Utils.smOrEq(errorsLargestBranch, errorsTree + 0.1D)) {
        C45PruneableClassifierTree largestBranch = son(indexOfLargestBranch);
        m_sons = m_sons;
        m_localModel = largestBranch.localModel();
        m_isLeaf = m_isLeaf;
        newDistribution(m_train);
        prune();
      }
    }
  }
  






  protected ClassifierTree getNewTree(Instances data)
    throws Exception
  {
    C45PruneableClassifierTree newTree = new C45PruneableClassifierTree(m_toSelectModel, m_pruneTheTree, m_CF, m_subtreeRaising, m_cleanup);
    

    newTree.buildTree(data, (m_subtreeRaising) || (!m_cleanup));
    
    return newTree;
  }
  





  private double getEstimatedErrors()
  {
    double errors = 0.0D;
    

    if (m_isLeaf) {
      return getEstimatedErrorsForDistribution(localModel().distribution());
    }
    for (int i = 0; i < m_sons.length; i++)
      errors += son(i).getEstimatedErrors();
    return errors;
  }
  









  private double getEstimatedErrorsForBranch(Instances data)
    throws Exception
  {
    double errors = 0.0D;
    

    if (m_isLeaf) {
      return getEstimatedErrorsForDistribution(new Distribution(data));
    }
    Distribution savedDist = localModelm_distribution;
    localModel().resetDistribution(data);
    Instances[] localInstances = (Instances[])localModel().split(data);
    localModelm_distribution = savedDist;
    for (int i = 0; i < m_sons.length; i++) {
      errors += son(i).getEstimatedErrorsForBranch(localInstances[i]);
    }
    return errors;
  }
  








  private double getEstimatedErrorsForDistribution(Distribution theDistribution)
  {
    if (Utils.eq(theDistribution.total(), 0.0D)) {
      return 0.0D;
    }
    return theDistribution.numIncorrect() + Stats.addErrs(theDistribution.total(), theDistribution.numIncorrect(), m_CF);
  }
  







  private double getTrainingErrors()
  {
    double errors = 0.0D;
    

    if (m_isLeaf) {
      return localModel().distribution().numIncorrect();
    }
    for (int i = 0; i < m_sons.length; i++)
      errors += son(i).getTrainingErrors();
    return errors;
  }
  






  private ClassifierSplitModel localModel()
  {
    return m_localModel;
  }
  








  private void newDistribution(Instances data)
    throws Exception
  {
    localModel().resetDistribution(data);
    m_train = data;
    if (!m_isLeaf) {
      Instances[] localInstances = (Instances[])localModel().split(data);
      
      for (int i = 0; i < m_sons.length; i++) {
        son(i).newDistribution(localInstances[i]);
      }
      
    }
    else if (!Utils.eq(data.sumOfWeights(), 0.0D)) {
      m_isEmpty = false;
    }
  }
  




  private C45PruneableClassifierTree son(int index)
  {
    return (C45PruneableClassifierTree)m_sons[index];
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8986 $");
  }
}
