package weka.classifiers.rules.part;

import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;





































public class PruneableDecList
  extends ClassifierDecList
{
  private static final long serialVersionUID = -7228103346297172921L;
  
  public PruneableDecList(ModelSelection toSelectLocModel, int minNum)
  {
    super(toSelectLocModel, minNum);
  }
  





  public void buildRule(Instances train, Instances test)
    throws Exception
  {
    buildDecList(train, test, false);
    
    cleanup(new Instances(train, 0));
  }
  











  public void buildDecList(Instances train, Instances test, boolean leaf)
    throws Exception
  {
    m_train = null;
    m_isLeaf = false;
    m_isEmpty = false;
    m_sons = null;
    indeX = 0;
    double sumOfWeights = train.sumOfWeights();
    NoSplit noSplit = new NoSplit(new Distribution(train));
    if (leaf) {
      m_localModel = noSplit;
    } else
      m_localModel = m_toSelectModel.selectModel(train, test);
    m_test = new Distribution(test, m_localModel);
    if (m_localModel.numSubsets() > 1) {
      Instances[] localTrain = m_localModel.split(train);
      Instances[] localTest = m_localModel.split(test);
      train = null;
      test = null;
      m_sons = new ClassifierDecList[m_localModel.numSubsets()];
      int i = 0;
      int ind;
      do { i++;
        ind = chooseIndex();
        if (ind == -1) {
          for (int j = 0; j < m_sons.length; j++)
            if (m_sons[j] == null)
              m_sons[j] = getNewDecList(localTrain[j], localTest[j], true);
          if (i < 2) {
            m_localModel = noSplit;
            m_isLeaf = true;
            m_sons = null;
            if (Utils.eq(sumOfWeights, 0.0D))
              m_isEmpty = true;
            return;
          }
          ind = 0;
          break;
        }
        m_sons[ind] = getNewDecList(localTrain[ind], localTest[ind], false);
      } while ((i < m_sons.length) && (m_sons[ind].m_isLeaf));
      

      for (int j = 0; j < m_sons.length; j++)
        if ((m_sons[j] == null) || (!m_sons[j].m_isLeaf))
          break;
      if (j == m_sons.length) {
        pruneEnd();
        if (!m_isLeaf)
          indeX = chooseLastIndex();
      } else {
        indeX = chooseLastIndex();
      }
    } else { m_isLeaf = true;
      if (Utils.eq(sumOfWeights, 0.0D)) {
        m_isEmpty = true;
      }
    }
  }
  







  protected ClassifierDecList getNewDecList(Instances train, Instances test, boolean leaf)
    throws Exception
  {
    PruneableDecList newDecList = new PruneableDecList(m_toSelectModel, m_minNumObj);
    

    newDecList.buildDecList(train, test, leaf);
    
    return newDecList;
  }
  




  protected void pruneEnd()
    throws Exception
  {
    double errorsTree = errorsForTree();
    double errorsLeaf = errorsForLeaf();
    if (Utils.smOrEq(errorsLeaf, errorsTree)) {
      m_isLeaf = true;
      m_sons = null;
      m_localModel = new NoSplit(localModel().distribution());
    }
  }
  




  private double errorsForTree()
    throws Exception
  {
    if (m_isLeaf) {
      return errorsForLeaf();
    }
    double error = 0.0D;
    for (int i = 0; i < m_sons.length; i++) {
      if (Utils.eq(son(i).localModel().distribution().total(), 0.0D)) {
        error += m_test.perBag(i) - m_test.perClassPerBag(i, localModel().distribution().maxClass());
      }
      else
      {
        error += ((PruneableDecList)son(i)).errorsForTree(); }
    }
    return error;
  }
  



  private double errorsForLeaf()
    throws Exception
  {
    return m_test.total() - m_test.perClass(localModel().distribution().maxClass());
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
}
