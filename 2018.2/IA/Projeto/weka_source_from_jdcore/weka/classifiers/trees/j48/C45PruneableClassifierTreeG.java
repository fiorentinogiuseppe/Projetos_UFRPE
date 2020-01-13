package weka.classifiers.trees.j48;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
































public class C45PruneableClassifierTreeG
  extends ClassifierTree
{
  static final long serialVersionUID = 66981207374331964L;
  boolean m_pruneTheTree = false;
  

  float m_CF = 0.25F;
  

  boolean m_subtreeRaising = true;
  

  boolean m_cleanup = true;
  

  boolean m_relabel = false;
  

  double m_BiProbCrit = 1.64D;
  
  boolean m_Debug = false;
  














  public C45PruneableClassifierTreeG(ModelSelection toSelectLocModel, boolean pruneTree, float cf, boolean raiseTree, boolean relabel, boolean cleanup)
    throws Exception
  {
    super(toSelectLocModel);
    
    m_pruneTheTree = pruneTree;
    m_CF = cf;
    m_subtreeRaising = raiseTree;
    m_cleanup = cleanup;
    m_relabel = relabel;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  



















  public C45PruneableClassifierTreeG(ModelSelection toSelectLocModel, Instances data, ClassifierSplitModel gs, boolean prune, float cf, boolean raise, boolean isLeaf, boolean relabel, boolean cleanup)
  {
    super(toSelectLocModel);
    m_relabel = relabel;
    m_cleanup = cleanup;
    m_localModel = gs;
    m_train = data;
    m_test = null;
    m_isLeaf = isLeaf;
    if (gs.distribution().total() > 0.0D) {
      m_isEmpty = false;
    } else {
      m_isEmpty = true;
    }
    m_pruneTheTree = prune;
    m_CF = cf;
    m_subtreeRaising = raise;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    buildTree(data, m_subtreeRaising);
    collapse();
    if (m_pruneTheTree) {
      prune();
    }
    doGrafting(data);
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
        C45PruneableClassifierTreeG largestBranch = son(indexOfLargestBranch);
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
    C45PruneableClassifierTreeG newTree = new C45PruneableClassifierTreeG(m_toSelectModel, m_pruneTheTree, m_CF, m_subtreeRaising, m_relabel, m_cleanup);
    



    newTree.buildTree(data, m_subtreeRaising);
    
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
  



  private C45PruneableClassifierTreeG son(int index)
  {
    return (C45PruneableClassifierTreeG)m_sons[index];
  }
  









  public void doGrafting(Instances data)
    throws Exception
  {
    double[][] limits = new double[data.numAttributes()][2];
    

    for (int i = 0; i < data.numAttributes(); i++) {
      limits[i][0] = Double.NEGATIVE_INFINITY;
      limits[i][1] = Double.POSITIVE_INFINITY;
    }
    



    double[][] instanceIndex = new double[2][data.numInstances()];
    
    for (int x = 0; x < data.numInstances(); x++) {
      instanceIndex[0][x] = 1.0D;
      instanceIndex[1][x] = 1.0D;
    }
    

    traverseTree(data, instanceIndex, limits, this, 0.0D, -1);
  }
  

















  private void traverseTree(Instances fulldata, double[][] iindex, double[][] limits, C45PruneableClassifierTreeG parent, double pL, int nodeClass)
    throws Exception
  {
    if (m_isLeaf)
    {
      findGraft(fulldata, iindex, limits, parent, pL, nodeClass);

    }
    else
    {

      for (int i = 0; i < localModel().numSubsets(); i++)
      {
        double[][] newiindex = new double[2][fulldata.numInstances()];
        for (int x = 0; x < 2; x++)
          System.arraycopy(iindex[x], 0, newiindex[x], 0, iindex[x].length);
        sortInstances(fulldata, newiindex, limits, i);
      }
    }
  }
  










  private void sortInstances(Instances fulldata, double[][] iindex, double[][] limits, int subset)
    throws Exception
  {
    C45Split test = (C45Split)localModel();
    

    double knownCases = 0.0D;
    double thisSubsetCount = 0.0D;
    for (int x = 0; x < iindex[0].length; x++) {
      if ((iindex[0][x] != 0.0D) || (iindex[1][x] != 0.0D))
      {
        if (!fulldata.instance(x).isMissing(test.attIndex())) {
          knownCases += iindex[0][x];
          if (test.whichSubset(fulldata.instance(x)) != subset) {
            if (iindex[0][x] > 0.0D)
            {
              iindex[1][x] = iindex[0][x];
              iindex[0][x] = 0.0D;
            }
            else if (iindex[1][x] > 0.0D)
            {
              iindex[1][x] = 0.0D;
            }
          }
          else {
            thisSubsetCount += iindex[0][x];
          }
        }
      }
    }
    
    double lprop = knownCases == 0.0D ? 1.0D / test.numSubsets() : thisSubsetCount / knownCases;
    


    for (int x = 0; x < iindex[0].length; x++) {
      if ((iindex[0][x] != 0.0D) || (iindex[1][x] != 0.0D))
      {
        if (fulldata.instance(x).isMissing(test.attIndex())) {
          iindex[1][x] -= (iindex[1][x] - iindex[0][x]) * (1.0D - lprop);
          iindex[0][x] *= lprop;
        }
      }
    }
    int nodeClass = localModel().distribution().maxClass(subset);
    double pL = (localModel().distribution().perClass(nodeClass) + 1.0D) / (localModel().distribution().total() + 2.0D);
    


    son(subset).traverseTree(fulldata, iindex, test.minsAndMaxs(fulldata, limits, subset), this, pL, nodeClass);
  }
  













  private void findGraft(Instances fulldata, double[][] iindex, double[][] limits, ClassifierTree parent, double pLaplace, int pLeafClass)
    throws Exception
  {
    int leafClass = m_isEmpty ? pLeafClass : localModel().distribution().maxClass();
    



    double leafLaplace = m_isEmpty ? pLaplace : laplaceLeaf(leafClass);
    



    Instances l = new Instances(fulldata, fulldata.numInstances());
    Instances n = new Instances(fulldata, fulldata.numInstances());
    int lcount = 0;
    int acount = 0;
    for (int x = 0; x < fulldata.numInstances(); x++) {
      if ((iindex[0][x] > 0.0D) || (iindex[1][x] > 0.0D))
      {
        if (iindex[0][x] != 0.0D) {
          l.add(fulldata.instance(x));
          l.instance(lcount).setWeight(iindex[0][x]);
          
          iindex[0][(lcount++)] = iindex[0][x];
        }
        if (iindex[1][x] > 0.0D) {
          n.add(fulldata.instance(x));
          n.instance(acount).setWeight(iindex[1][x]);
          
          iindex[1][(acount++)] = iindex[1][x];
        }
      }
    }
    boolean graftPossible = false;
    double[] classDist = new double[n.numClasses()];
    for (int x = 0; x < n.numInstances(); x++) {
      if ((iindex[1][x] > 0.0D) && (!n.instance(x).classIsMissing())) {
        classDist[((int)n.instance(x).classValue())] += iindex[1][x];
      }
    }
    for (int cVal = 0; cVal < n.numClasses(); cVal++) {
      double theLaplace = (classDist[cVal] + 1.0D) / (classDist[cVal] + 2.0D);
      if ((cVal != leafClass) && (theLaplace > leafLaplace) && (biprob(classDist[cVal], classDist[cVal], leafLaplace) > m_BiProbCrit))
      {

        graftPossible = true;
        break;
      }
    }
    
    if (!graftPossible) {
      return;
    }
    

    ArrayList t = new ArrayList();
    

    for (int a = 0; a < n.numAttributes(); a++) {
      if (a != n.classIndex())
      {


        int[] sorted = sortByAttribute(n, a);
        

        if (n.attribute(a).isNumeric())
        {

          boolean prohibited = false;
          double minLeaf = Double.POSITIVE_INFINITY;
          double maxLeaf = Double.NEGATIVE_INFINITY;
          for (int i = 0; i < l.numInstances(); i++) {
            if ((l.instance(i).isMissing(a)) && 
              (l.instance(i).classValue() == leafClass)) {
              prohibited = true;
              break;
            }
            
            double value = l.instance(i).value(a);
            if ((!m_relabel) || (l.instance(i).classValue() == leafClass)) {
              if (value < minLeaf)
                minLeaf = value;
              if (value > maxLeaf)
                maxLeaf = value;
            }
          }
          if (!prohibited)
          {













            double minBestClass = NaN.0D;
            double minBestLaplace = leafLaplace;
            double minBestVal = NaN.0D;
            double minBestPos = NaN.0D;
            double minBestTotal = NaN.0D;
            double[][] minBestCounts = (double[][])null;
            double[][] counts = new double[2][n.numClasses()];
            for (int x = 0; x < n.numInstances(); x++) {
              if (n.instance(sorted[x]).isMissing(a)) {
                break;
              }
              double theval = n.instance(sorted[x]).value(a);
              if (m_Debug) {
                System.out.println("\t " + theval);
              }
              if (theval <= limits[a][0]) {
                if (m_Debug) {
                  System.out.println("\t  <= lowerlim: continuing...");
                }
              }
              else {
                if (theval >= minLeaf) {
                  if (!m_Debug) break;
                  System.out.println("\t  >= minLeaf; breaking..."); break;
                }
                
                counts[0][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
                

                if (x != n.numInstances() - 1) {
                  int z = x + 1;
                  
                  while ((z < n.numInstances()) && (n.instance(sorted[z]).value(a) == theval)) {
                    z++;x++;
                    counts[0][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
                  }
                }
                


                double total = Utils.sum(counts[0]);
                for (int c = 0; c < n.numClasses(); c++) {
                  double temp = (counts[0][c] + 1.0D) / (total + 2.0D);
                  if (temp > minBestLaplace) {
                    minBestPos = counts[0][c];
                    minBestTotal = total;
                    minBestLaplace = temp;
                    minBestClass = c;
                    minBestCounts = copyCounts(counts);
                    
                    minBestVal = x == n.numInstances() - 1 ? theval : (theval + n.instance(sorted[(x + 1)]).value(a)) / 2.0D;
                  }
                }
              }
            }
            


            if ((!Double.isNaN(minBestVal)) && (biprob(minBestPos, minBestTotal, leafLaplace) > m_BiProbCrit))
            {
              GraftSplit gsplit = null;
              try {
                gsplit = new GraftSplit(a, minBestVal, 0, leafClass, minBestCounts);
              }
              catch (Exception e) {
                System.err.println("graftsplit error: " + e.getMessage());
                System.exit(1);
              }
              t.add(gsplit);
            }
            
            minBestCounts = (double[][])null;
            









            double maxBestClass = -1.0D;
            double maxBestLaplace = leafLaplace;
            double maxBestVal = NaN.0D;
            double maxBestPos = NaN.0D;
            double maxBestTotal = NaN.0D;
            double[][] maxBestCounts = (double[][])null;
            for (int c = 0; c < n.numClasses(); c++) {
              counts[0][c] = 0.0D;
              counts[1][c] = 0.0D;
            }
            

            if ((n.numInstances() >= 1) && (n.instance(sorted[0]).value(a) < limits[a][1]))
            {
              for (int x = n.numInstances() - 1; x >= 0; x--) {
                if (!n.instance(sorted[x]).isMissing(a))
                {

                  double theval = n.instance(sorted[x]).value(a);
                  if (m_Debug) {
                    System.out.println("\t " + theval);
                  }
                  if (theval > limits[a][1]) {
                    if (m_Debug) {
                      System.out.println("\t  >= upperlim; continuing...");
                    }
                  } else {
                    if (theval <= maxLeaf) {
                      if (!m_Debug) break;
                      System.out.println("\t  < maxLeaf; breaking..."); break;
                    }
                    


                    counts[1][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
                    

                    if ((x != 0) && (!n.instance(sorted[(x - 1)]).isMissing(a))) {
                      int z = x - 1;
                      while ((z >= 0) && (n.instance(sorted[z]).value(a) == theval)) {
                        z--;x--;
                        counts[1][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
                      }
                    }
                    


                    double total = Utils.sum(counts[1]);
                    for (int c = 0; c < n.numClasses(); c++) {
                      double temp = (counts[1][c] + 1.0D) / (total + 2.0D);
                      if (temp > maxBestLaplace) {
                        maxBestPos = counts[1][c];
                        maxBestTotal = total;
                        maxBestLaplace = temp;
                        maxBestClass = c;
                        maxBestCounts = copyCounts(counts);
                        maxBestVal = x == 0 ? theval : (theval + n.instance(sorted[(x - 1)]).value(a)) / 2.0D;
                      }
                    }
                  }
                }
              }
              

              if ((!Double.isNaN(maxBestVal)) && (biprob(maxBestPos, maxBestTotal, leafLaplace) > m_BiProbCrit))
              {
                GraftSplit gsplit = null;
                try {
                  gsplit = new GraftSplit(a, maxBestVal, 1, leafClass, maxBestCounts);
                }
                catch (Exception e) {
                  System.err.println("graftsplit error:" + e.getMessage());
                  System.exit(1);
                }
                t.add(gsplit);
              }
              
            }
            
          }
          

        }
        else if (limits[a][1] != 1.0D)
        {


          boolean[] prohibit = new boolean[l.attribute(a).numValues()];
          for (int aval = 0; aval < n.attribute(a).numValues(); aval++) {
            for (int x = 0; x < l.numInstances(); x++) {
              if (((l.instance(x).isMissing(a)) || (l.instance(x).value(a) == aval)) && ((!m_relabel) || (l.instance(x).classValue() == leafClass)))
              {

                prohibit[aval] = true;
                break;
              }
            }
          }
          






          double bestVal = NaN.0D;
          double bestClass = NaN.0D;
          double bestLaplace = leafLaplace;
          double[][] bestCounts = (double[][])null;
          double[][] counts = new double[2][n.numClasses()];
          
          for (int x = 0; x < n.numInstances(); x++) {
            if (!n.instance(sorted[x]).isMissing(a))
            {


              for (int c = 0; c < n.numClasses(); c++) {
                counts[0][c] = 0.0D;
              }
              double theval = n.instance(sorted[x]).value(a);
              counts[0][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
              

              if (x != n.numInstances() - 1) {
                int z = x + 1;
                
                while ((z < n.numInstances()) && (n.instance(sorted[z]).value(a) == theval)) {
                  z++;x++;
                  counts[0][((int)n.instance(sorted[x]).classValue())] += iindex[1][sorted[x]];
                }
              }
              

              if (prohibit[((int)theval)] == 0)
              {
                double total = Utils.sum(counts[0]);
                bestLaplace = leafLaplace;
                bestClass = NaN.0D;
                for (int c = 0; c < n.numClasses(); c++) {
                  double temp = (counts[0][c] + 1.0D) / (total + 2.0D);
                  if ((temp > bestLaplace) && (biprob(counts[0][c], total, leafLaplace) > m_BiProbCrit))
                  {
                    bestLaplace = temp;
                    bestClass = c;
                    bestVal = theval;
                    bestCounts = copyCounts(counts);
                  }
                }
                
                if (!Double.isNaN(bestClass)) {
                  GraftSplit gsplit = null;
                  try {
                    gsplit = new GraftSplit(a, bestVal, 2, leafClass, bestCounts);
                  }
                  catch (Exception e) {
                    System.err.println("graftsplit error: " + e.getMessage());
                    System.exit(1);
                  }
                  t.add(gsplit);
                }
              }
            }
          }
        }
      }
    }
    








    Collections.sort(t);
    


    for (int x = 0; x < t.size(); x++) {
      GraftSplit gs = (GraftSplit)t.get(x);
      if (gs.maxClassForSubsetOfInterest() != leafClass) {
        break;
      }
      t.remove(x);
      x--;
    }
    


    if (t.size() < 1) {
      return;
    }
    

    for (int x = t.size() - 1; x >= 0; x--) {
      GraftSplit gs = (GraftSplit)t.get(x);
      try {
        gs.buildClassifier(l);
        gs.deleteGraftedCases(l);
      } catch (Exception e) {
        System.err.println("graftsplit build error: " + e.getMessage());
      }
    }
    

    ((C45PruneableClassifierTreeG)parent).setDescendents(t, this);
  }
  







  private int[] sortByAttribute(Instances data, int a)
  {
    double[] attList = data.attributeToDoubleArray(a);
    int[] temp = Utils.sort(attList);
    return temp;
  }
  






  private double[][] copyCounts(double[][] src)
  {
    double[][] newArr = new double[src.length][0];
    for (int x = 0; x < src.length; x++) {
      newArr[x] = new double[src[x].length];
      for (int y = 0; y < src[x].length; y++) {
        newArr[x][y] = src[x][y];
      }
    }
    return newArr;
  }
  








  private double getProbsLaplace(int classIndex, Instance instance, double weight)
    throws Exception
  {
    double prob = 0.0D;
    


    if (m_isLeaf) {
      return weight * localModel().classProbLaplace(classIndex, instance, -1);
    }
    int treeIndex = localModel().whichSubset(instance);
    
    if (treeIndex == -1) {
      double[] weights = localModel().weights(instance);
      for (int i = 0; i < m_sons.length; i++) {
        if (!sonm_isEmpty) {
          if (!sonm_isLeaf) {
            prob += son(i).getProbsLaplace(classIndex, instance, weights[i] * weight);
          }
          else {
            prob += weight * weights[i] * localModel().classProbLaplace(classIndex, instance, i);
          }
        }
      }
      
      return prob;
    }
    
    if (sonm_isLeaf) {
      return weight * localModel().classProbLaplace(classIndex, instance, treeIndex);
    }
    
    return son(treeIndex).getProbsLaplace(classIndex, instance, weight);
  }
  











  private double getProbs(int classIndex, Instance instance, double weight)
    throws Exception
  {
    double prob = 0.0D;
    


    if (m_isLeaf) {
      return weight * localModel().classProb(classIndex, instance, -1);
    }
    int treeIndex = localModel().whichSubset(instance);
    if (treeIndex == -1) {
      double[] weights = localModel().weights(instance);
      for (int i = 0; i < m_sons.length; i++) {
        if (!sonm_isEmpty) {
          prob += son(i).getProbs(classIndex, instance, weights[i] * weight);
        }
      }
      
      return prob;
    }
    
    if (sonm_isEmpty) {
      return weight * localModel().classProb(classIndex, instance, treeIndex);
    }
    
    return son(treeIndex).getProbs(classIndex, instance, weight);
  }
  













  public void setDescendents(ArrayList t, C45PruneableClassifierTreeG originalLeaf)
  {
    Instances headerInfo = new Instances(m_train, 0);
    
    boolean end = false;
    ClassifierSplitModel splitmod = null;
    C45PruneableClassifierTreeG newNode;
    C45PruneableClassifierTreeG newNode; if (t.size() > 0) {
      splitmod = (ClassifierSplitModel)t.remove(t.size() - 1);
      newNode = new C45PruneableClassifierTreeG(m_toSelectModel, headerInfo, splitmod, m_pruneTheTree, m_CF, m_subtreeRaising, false, m_relabel, m_cleanup);

    }
    else
    {
      NoSplit kLeaf = ((GraftSplit)localModel()).getOtherLeaf();
      newNode = new C45PruneableClassifierTreeG(m_toSelectModel, headerInfo, kLeaf, m_pruneTheTree, m_CF, m_subtreeRaising, true, m_relabel, m_cleanup);
      


      end = true;
    }
    


    if (m_sons != null) {
      for (int x = 0; x < m_sons.length; x++) {
        if (son(x).equals(originalLeaf)) {
          m_sons[x] = newNode;
        }
      }
    }
    else
    {
      m_sons = new C45PruneableClassifierTreeG[localModel().numSubsets()];
      

      NoSplit kLeaf = ((GraftSplit)localModel()).getLeaf();
      C45PruneableClassifierTreeG kNode = new C45PruneableClassifierTreeG(m_toSelectModel, headerInfo, kLeaf, m_pruneTheTree, m_CF, m_subtreeRaising, true, m_relabel, m_cleanup);
      




      if (((GraftSplit)localModel()).subsetOfInterest() == 0) {
        m_sons[0] = kNode;
        m_sons[1] = newNode;
      } else {
        m_sons[0] = newNode;
        m_sons[1] = kNode;
      }
    }
    if (!end) {
      newNode.setDescendents(t, originalLeaf);
    }
  }
  



  private double laplaceLeaf(double classIndex)
  {
    double l = (localModel().distribution().perClass((int)classIndex) + 1.0D) / (localModel().distribution().total() + 2.0D);
    
    return l;
  }
  









  public double biprob(double x, double n, double r)
    throws Exception
  {
    return (x - 0.5D - n * r) / Math.sqrt(n * r * (1.0D - r));
  }
  


  public String toString()
  {
    try
    {
      StringBuffer text = new StringBuffer();
      
      if (m_isLeaf) {
        text.append(": ");
        if ((m_localModel instanceof GraftSplit)) {
          text.append(((GraftSplit)m_localModel).dumpLabelG(0, m_train));
        } else
          text.append(m_localModel.dumpLabel(0, m_train));
      } else {
        dumpTree(0, text); }
      text.append("\n\nNumber of Leaves  : \t" + numLeaves() + "\n");
      text.append("\nSize of the tree : \t" + numNodes() + "\n");
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print classification tree.";
  }
  







  protected void dumpTree(int depth, StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("\n");
      for (int j = 0; j < depth; j++)
        text.append("|   ");
      text.append(m_localModel.leftSide(m_train));
      text.append(m_localModel.rightSide(i, m_train));
      if (m_sons[i].m_isLeaf) {
        text.append(": ");
        if ((m_localModel instanceof GraftSplit)) {
          text.append(((GraftSplit)m_localModel).dumpLabelG(i, m_train));
        } else
          text.append(m_localModel.dumpLabel(i, m_train));
      } else {
        ((C45PruneableClassifierTreeG)m_sons[i]).dumpTree(depth + 1, text);
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5535 $");
  }
}
