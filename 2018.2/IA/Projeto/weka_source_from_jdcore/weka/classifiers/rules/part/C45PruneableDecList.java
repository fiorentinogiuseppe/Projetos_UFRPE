package weka.classifiers.rules.part;

import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.NoSplit;
import weka.classifiers.trees.j48.Stats;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
































public class C45PruneableDecList
  extends ClassifierDecList
{
  private static final long serialVersionUID = -2757684345218324559L;
  private double CF = 0.25D;
  










  public C45PruneableDecList(ModelSelection toSelectLocModel, double cf, int minNum)
    throws Exception
  {
    super(toSelectLocModel, minNum);
    
    CF = cf;
  }
  










  public void buildDecList(Instances data, boolean leaf)
    throws Exception
  {
    m_train = null;
    m_test = null;
    m_isLeaf = false;
    m_isEmpty = false;
    m_sons = null;
    indeX = 0;
    double sumOfWeights = data.sumOfWeights();
    NoSplit noSplit = new NoSplit(new Distribution(data));
    if (leaf) {
      m_localModel = noSplit;
    } else
      m_localModel = m_toSelectModel.selectModel(data);
    if (m_localModel.numSubsets() > 1) {
      Instances[] localInstances = m_localModel.split(data);
      data = null;
      m_sons = new ClassifierDecList[m_localModel.numSubsets()];
      int i = 0;
      int ind;
      do { i++;
        ind = chooseIndex();
        if (ind == -1) {
          for (int j = 0; j < m_sons.length; j++)
            if (m_sons[j] == null)
              m_sons[j] = getNewDecList(localInstances[j], true);
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
        m_sons[ind] = getNewDecList(localInstances[ind], false);
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
  




  protected ClassifierDecList getNewDecList(Instances data, boolean leaf)
    throws Exception
  {
    C45PruneableDecList newDecList = new C45PruneableDecList(m_toSelectModel, CF, m_minNumObj);
    

    newDecList.buildDecList(data, leaf);
    
    return newDecList;
  }
  





  protected void pruneEnd()
  {
    double errorsTree = getEstimatedErrorsForTree();
    double errorsLeaf = getEstimatedErrorsForLeaf();
    if (Utils.smOrEq(errorsLeaf, errorsTree + 0.1D)) {
      m_isLeaf = true;
      m_sons = null;
      m_localModel = new NoSplit(localModel().distribution());
    }
  }
  



  private double getEstimatedErrorsForTree()
  {
    if (m_isLeaf) {
      return getEstimatedErrorsForLeaf();
    }
    double error = 0.0D;
    for (int i = 0; i < m_sons.length; i++)
      if (!Utils.eq(son(i).localModel().distribution().total(), 0.0D))
        error += ((C45PruneableDecList)son(i)).getEstimatedErrorsForTree();
    return error;
  }
  




  public double getEstimatedErrorsForLeaf()
  {
    double errors = localModel().distribution().numIncorrect();
    
    return errors + Stats.addErrs(localModel().distribution().total(), errors, (float)CF);
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}
