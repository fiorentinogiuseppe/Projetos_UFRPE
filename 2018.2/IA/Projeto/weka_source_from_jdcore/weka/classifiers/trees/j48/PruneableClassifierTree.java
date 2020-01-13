package weka.classifiers.trees.j48;

import java.util.Random;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;


































public class PruneableClassifierTree
  extends ClassifierTree
{
  static final long serialVersionUID = -555775736857600201L;
  private boolean pruneTheTree = false;
  

  private int numSets = 3;
  

  private boolean m_cleanup = true;
  

  private int m_seed = 1;
  













  public PruneableClassifierTree(ModelSelection toSelectLocModel, boolean pruneTree, int num, boolean cleanup, int seed)
    throws Exception
  {
    super(toSelectLocModel);
    
    pruneTheTree = pruneTree;
    numSets = num;
    m_cleanup = cleanup;
    m_seed = seed;
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
    
    Random random = new Random(m_seed);
    data.stratify(numSets);
    buildTree(data.trainCV(numSets, numSets - 1, random), data.testCV(numSets, numSets - 1), !m_cleanup);
    
    if (pruneTheTree) {
      prune();
    }
    if (m_cleanup) {
      cleanup(new Instances(data, 0));
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
      
      if (Utils.smOrEq(errorsForLeaf(), errorsForTree()))
      {

        m_sons = null;
        m_isLeaf = true;
        

        m_localModel = new NoSplit(localModel().distribution());
      }
    }
  }
  








  protected ClassifierTree getNewTree(Instances train, Instances test)
    throws Exception
  {
    PruneableClassifierTree newTree = new PruneableClassifierTree(m_toSelectModel, pruneTheTree, numSets, m_cleanup, m_seed);
    

    newTree.buildTree(train, test, !m_cleanup);
    return newTree;
  }
  





  private double errorsForTree()
    throws Exception
  {
    double errors = 0.0D;
    
    if (m_isLeaf) {
      return errorsForLeaf();
    }
    for (int i = 0; i < m_sons.length; i++) {
      if (Utils.eq(localModel().distribution().perBag(i), 0.0D)) {
        errors += m_test.perBag(i) - m_test.perClassPerBag(i, localModel().distribution().maxClass());
      }
      else
      {
        errors += son(i).errorsForTree(); }
    }
    return errors;
  }
  






  private double errorsForLeaf()
    throws Exception
  {
    return m_test.total() - m_test.perClass(localModel().distribution().maxClass());
  }
  




  private ClassifierSplitModel localModel()
  {
    return m_localModel;
  }
  



  private PruneableClassifierTree son(int index)
  {
    return (PruneableClassifierTree)m_sons[index];
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8985 $");
  }
}
