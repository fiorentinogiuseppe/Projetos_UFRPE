package weka.classifiers.lazy.kstar;

import java.io.PrintStream;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;










































public class KStarNominalAttribute
  implements KStarConstants, RevisionHandler
{
  protected Instances m_TrainSet;
  protected Instance m_Test;
  protected Instance m_Train;
  protected int m_AttrIndex;
  protected double m_Stop = 1.0D;
  


  protected double m_MissingProb = 1.0D;
  


  protected double m_AverageProb = 1.0D;
  


  protected double m_SmallestProb = 1.0D;
  


  protected int m_TotalCount;
  


  protected int[] m_Distribution;
  


  protected int[][] m_RandClassCols;
  


  protected KStarCache m_Cache;
  

  protected int m_NumInstances;
  

  protected int m_NumClasses;
  

  protected int m_NumAttributes;
  

  protected int m_ClassType;
  

  protected int m_MissingMode = 4;
  

  protected int m_BlendMethod = 1;
  

  protected int m_BlendFactor = 20;
  





  public KStarNominalAttribute(Instance test, Instance train, int attrIndex, Instances trainSet, int[][] randClassCol, KStarCache cache)
  {
    m_Test = test;
    m_Train = train;
    m_AttrIndex = attrIndex;
    m_TrainSet = trainSet;
    m_RandClassCols = randClassCol;
    m_Cache = cache;
    init();
  }
  

  private void init()
  {
    try
    {
      m_NumInstances = m_TrainSet.numInstances();
      m_NumClasses = m_TrainSet.numClasses();
      m_NumAttributes = m_TrainSet.numAttributes();
      m_ClassType = m_TrainSet.classAttribute().type();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  






  public double transProb()
  {
    String debug = "(KStarNominalAttribute.transProb) ";
    double transProb = 0.0D;
    

    if (m_Cache.containsKey(m_Test.value(m_AttrIndex))) {
      KStarCache.TableEntry te = m_Cache.getCacheValues(m_Test.value(m_AttrIndex));
      
      m_Stop = value;
      m_MissingProb = pmiss;
    }
    else {
      generateAttrDistribution();
      
      if (m_BlendMethod == 2) {
        m_Stop = stopProbUsingEntropy();
      }
      else {
        m_Stop = stopProbUsingBlend();
      }
      
      m_Cache.store(m_Test.value(m_AttrIndex), m_Stop, m_MissingProb);
    }
    
    if (m_Train.isMissing(m_AttrIndex)) {
      transProb = m_MissingProb;
    } else {
      try
      {
        transProb = (1.0D - m_Stop) / m_Test.attribute(m_AttrIndex).numValues();
        if ((int)m_Test.value(m_AttrIndex) == (int)m_Train.value(m_AttrIndex))
        {

          transProb += m_Stop;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return transProb;
  }
  










  private double stopProbUsingEntropy()
  {
    String debug = "(KStarNominalAttribute.stopProbUsingEntropy)";
    if (m_ClassType != 1) {
      System.err.println("Error: " + debug + " attribute class must be nominal!");
      System.exit(1);
    }
    int itcount = 0;
    

    double bestminprob = 0.0D;double bestpsum = 0.0D;
    double bestdiff = 0.0D;double bestpstop = 0.0D;
    

    KStarWrapper botvals = new KStarWrapper();
    KStarWrapper upvals = new KStarWrapper();
    KStarWrapper vals = new KStarWrapper();
    

    double lower = 0.005D;
    double upper = 0.995D;
    

    calculateEntropy(upper, upvals);
    calculateEntropy(lower, botvals);
    
    if (avgProb == 0.0D)
    {


      calculateEntropy(lower, vals);
    }
    else {
      double pstop;
      double stepsize;
      if ((randEntropy - actEntropy < randEntropy - actEntropy) && (randEntropy - actEntropy > 0.0D))
      {
        double pstop;
        
        bestpstop = pstop = lower;
        double stepsize = 0.05D;
        bestminprob = minProb;
        bestpsum = avgProb;
      }
      else {
        bestpstop = pstop = upper;
        stepsize = -0.05D;
        bestminprob = minProb;
        bestpsum = avgProb; }
      double currentdiff;
      bestdiff = currentdiff = 0.0D;
      itcount = 0;
      
      for (;;)
      {
        itcount++;
        double lastdiff = currentdiff;
        pstop += stepsize;
        double delta; double delta; if (pstop <= lower) {
          pstop = lower;
          currentdiff = 0.0D;
          delta = -1.0D;
        } else { double delta;
          if (pstop >= upper) {
            pstop = upper;
            currentdiff = 0.0D;
            delta = -1.0D;
          }
          else {
            calculateEntropy(pstop, vals);
            currentdiff = randEntropy - actEntropy;
            
            if (currentdiff < 0.0D) {
              currentdiff = 0.0D;
              if ((Math.abs(stepsize) < 0.05D) && (bestdiff == 0.0D))
              {
                bestpstop = lower;
                bestminprob = minProb;
                bestpsum = avgProb;
                break;
              }
            }
            delta = currentdiff - lastdiff;
          } }
        if (currentdiff > bestdiff) {
          bestdiff = currentdiff;
          bestpstop = pstop;
          bestminprob = minProb;
          bestpsum = avgProb;
        }
        if (delta < 0.0D) {
          if (Math.abs(stepsize) >= 0.01D)
          {


            stepsize /= -2.0D;
          }
        }
        else if (itcount > 40) {
          break;
        }
      }
    }
    
    m_SmallestProb = bestminprob;
    m_AverageProb = bestpsum;
    
    switch (m_MissingMode)
    {
    case 1: 
      m_MissingProb = 0.0D;
      break;
    case 3: 
      m_MissingProb = 1.0D;
      break;
    case 2: 
      m_MissingProb = m_SmallestProb;
      break;
    case 4: 
      m_MissingProb = m_AverageProb;
    }
    double stopProb;
    double stopProb;
    if (Math.abs(bestpsum - m_TotalCount) < 1.0E-5D)
    {
      stopProb = 1.0D;
    }
    else {
      stopProb = bestpstop;
    }
    return stopProb;
  }
  











  private void calculateEntropy(double stop, KStarWrapper params)
  {
    String debug = "(KStarNominalAttribute.calculateEntropy)";
    

    double actent = 0.0D;double randent = 0.0D;
    double psum = 0.0D;double minprob = 1.0D;
    
    double[][] pseudoClassProb = new double[6][m_NumClasses];
    
    for (int j = 0; j <= 5; j++) {
      for (int i = 0; i < m_NumClasses; i++) {
        pseudoClassProb[j][i] = 0.0D;
      }
    }
    for (int i = 0; i < m_NumInstances; i++) {
      Instance train = m_TrainSet.instance(i);
      if (!train.isMissing(m_AttrIndex)) {
        double pstar = PStar(m_Test, train, m_AttrIndex, stop);
        double tprob = pstar / m_TotalCount;
        if (pstar < minprob) {
          minprob = pstar;
        }
        psum += tprob;
        
        for (int k = 0; k <= 5; k++)
        {


          pseudoClassProb[k][m_RandClassCols[k][i]] += tprob;
        }
      }
    }
    

    for (j = m_NumClasses - 1; j >= 0; j--) {
      double actClassProb = pseudoClassProb[5][j] / psum;
      if (actClassProb > 0.0D) {
        actent -= actClassProb * Math.log(actClassProb) / 0.693147181D;
      }
    }
    

    for (int k = 0; k < 5; k++) {
      for (i = m_NumClasses - 1; i >= 0; i--) {
        double randClassProb = pseudoClassProb[k][i] / psum;
        if (randClassProb > 0.0D) {
          randent -= randClassProb * Math.log(randClassProb) / 0.693147181D;
        }
      }
    }
    randent /= 5.0D;
    
    actEntropy = actent;
    randEntropy = randent;
    avgProb = psum;
    minProb = minprob;
  }
  










  private double stopProbUsingBlend()
  {
    String debug = "(KStarNominalAttribute.stopProbUsingBlend) ";
    int itcount = 0;
    


    KStarWrapper botvals = new KStarWrapper();
    KStarWrapper upvals = new KStarWrapper();
    KStarWrapper vals = new KStarWrapper();
    
    int testvalue = (int)m_Test.value(m_AttrIndex);
    double aimfor = (m_TotalCount - m_Distribution[testvalue]) * m_BlendFactor / 100.0D + m_Distribution[testvalue];
    


    double tstop = 1.0D - m_BlendFactor / 100.0D;
    double lower = 0.005D;
    double upper = 0.995D;
    

    calculateSphereSize(testvalue, lower, botvals);
    sphere -= aimfor;
    calculateSphereSize(testvalue, upper, upvals);
    sphere -= aimfor;
    
    if (avgProb == 0.0D)
    {


      calculateSphereSize(testvalue, tstop, vals);
    }
    else if (sphere > 0.0D)
    {
      tstop = upper;
      avgProb = avgProb;
    }
    else
    {
      for (;;) {
        itcount++;
        calculateSphereSize(testvalue, tstop, vals);
        sphere -= aimfor;
        if ((Math.abs(sphere) <= 0.01D) || (itcount >= 40)) {
          break;
        }
        

        if (sphere > 0.0D) {
          lower = tstop;
          tstop = (upper + lower) / 2.0D;
        }
        else {
          upper = tstop;
          tstop = (upper + lower) / 2.0D;
        }
      }
    }
    
    m_SmallestProb = minProb;
    m_AverageProb = avgProb;
    
    switch (m_MissingMode)
    {
    case 1: 
      m_MissingProb = 0.0D;
      break;
    case 3: 
      m_MissingProb = 1.0D;
      break;
    case 2: 
      m_MissingProb = m_SmallestProb;
      break;
    case 4: 
      m_MissingProb = m_AverageProb;
    }
    double stopProb;
    double stopProb;
    if (Math.abs(avgProb - m_TotalCount) < 1.0E-5D)
    {
      stopProb = 1.0D;
    }
    else {
      stopProb = tstop;
    }
    return stopProb;
  }
  

















  private void calculateSphereSize(int testvalue, double stop, KStarWrapper params)
  {
    String debug = "(KStarNominalAttribute.calculateSphereSize) ";
    
    double tval = 0.0D;double t1 = 0.0D;
    double minprob = 1.0D;double transprob = 0.0D;
    
    for (int i = 0; i < m_Distribution.length; i++) {
      int thiscount = m_Distribution[i];
      if (thiscount != 0) { double tprob;
        if (testvalue == i) {
          double tprob = (stop + (1.0D - stop) / m_Distribution.length) / m_TotalCount;
          tval += tprob * thiscount;
          t1 += tprob * tprob * thiscount;
        }
        else {
          tprob = (1.0D - stop) / m_Distribution.length / m_TotalCount;
          tval += tprob * thiscount;
          t1 += tprob * tprob * thiscount;
        }
        if (minprob > tprob * m_TotalCount) {
          minprob = tprob * m_TotalCount;
        }
      }
    }
    transprob = tval;
    double sphere = t1 == 0.0D ? 0.0D : tval * tval / t1;
    
    sphere = sphere;
    avgProb = transprob;
    minProb = minprob;
  }
  











  private double PStar(Instance test, Instance train, int col, double stop)
  {
    String debug = "(KStarNominalAttribute.PStar) ";
    
    int numvalues = 0;
    try {
      numvalues = test.attribute(col).numValues();
    } catch (Exception ex) {
      ex.printStackTrace(); }
    double pstar;
    double pstar; if ((int)test.value(col) == (int)train.value(col)) {
      pstar = stop + (1.0D - stop) / numvalues;
    }
    else {
      pstar = (1.0D - stop) / numvalues;
    }
    return pstar;
  }
  





  private void generateAttrDistribution()
  {
    String debug = "(KStarNominalAttribute.generateAttrDistribution)";
    m_Distribution = new int[m_TrainSet.attribute(m_AttrIndex).numValues()];
    

    for (int i = 0; i < m_NumInstances; i++) {
      Instance train = m_TrainSet.instance(i);
      if (!train.isMissing(m_AttrIndex)) {
        m_TotalCount += 1;
        m_Distribution[((int)train.value(m_AttrIndex))] += 1;
      }
    }
  }
  



  public void setOptions(int missingmode, int blendmethod, int blendfactor)
  {
    m_MissingMode = missingmode;
    m_BlendMethod = blendmethod;
    m_BlendFactor = blendfactor;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
