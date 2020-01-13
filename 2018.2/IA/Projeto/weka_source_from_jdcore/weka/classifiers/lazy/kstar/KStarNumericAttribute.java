package weka.classifiers.lazy.kstar;

import java.io.PrintStream;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































public class KStarNumericAttribute
  implements KStarConstants, RevisionHandler
{
  protected Instances m_TrainSet;
  protected Instance m_Test;
  protected Instance m_Train;
  protected int m_AttrIndex;
  protected double m_Scale = 1.0D;
  


  protected double m_MissingProb = 1.0D;
  


  protected double m_AverageProb = 1.0D;
  


  protected double m_SmallestProb = 1.0D;
  


  protected double[] m_Distances;
  


  protected int[][] m_RandClassCols;
  

  protected int m_ActualCount = 0;
  


  protected KStarCache m_Cache;
  

  protected int m_NumInstances;
  

  protected int m_NumClasses;
  

  protected int m_NumAttributes;
  

  protected int m_ClassType;
  

  protected int m_MissingMode = 4;
  

  protected int m_BlendMethod = 1;
  

  protected int m_BlendFactor = 20;
  






  public KStarNumericAttribute(Instance test, Instance train, int attrIndex, Instances trainSet, int[][] randClassCols, KStarCache cache)
  {
    m_Test = test;
    m_Train = train;
    m_AttrIndex = attrIndex;
    m_TrainSet = trainSet;
    m_RandClassCols = randClassCols;
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
    String debug = "(KStarNumericAttribute.transProb) ";
    


    if (m_Cache.containsKey(m_Test.value(m_AttrIndex))) {
      KStarCache.TableEntry te = m_Cache.getCacheValues(m_Test.value(m_AttrIndex));
      
      m_Scale = value;
      m_MissingProb = pmiss;
    }
    else {
      if (m_BlendMethod == 2) {
        m_Scale = scaleFactorUsingEntropy();
      }
      else {
        m_Scale = scaleFactorUsingBlend();
      }
      m_Cache.store(m_Test.value(m_AttrIndex), m_Scale, m_MissingProb); }
    double transProb;
    double transProb;
    if (m_Train.isMissing(m_AttrIndex)) {
      transProb = m_MissingProb;
    }
    else {
      double distance = Math.abs(m_Test.value(m_AttrIndex) - m_Train.value(m_AttrIndex));
      
      transProb = PStar(distance, m_Scale);
    }
    return transProb;
  }
  






  private double scaleFactorUsingBlend()
  {
    String debug = "(KStarNumericAttribute.scaleFactorUsingBlend)";
    int lowestcount = 0;int count = 0;
    double lowest = -1.0D;double nextlowest = -1.0D;
    
    double min_val = 9.0E300D;double scale = 1.0D;
    double avgprob = 0.0D;double minprob = 0.0D;double min_pos = 0.0D;
    
    KStarWrapper botvals = new KStarWrapper();
    KStarWrapper upvals = new KStarWrapper();
    KStarWrapper vals = new KStarWrapper();
    
    m_Distances = new double[m_NumInstances];
    
    for (int j = 0; j < m_NumInstances; j++) {
      if (m_TrainSet.instance(j).isMissing(m_AttrIndex))
      {

        m_Distances[j] = -1.0D;
      }
      else {
        m_Distances[j] = Math.abs(m_TrainSet.instance(j).value(m_AttrIndex) - m_Test.value(m_AttrIndex));
        
        if ((m_Distances[j] + 1.0E-5D < nextlowest) || (nextlowest == -1.0D)) {
          if ((m_Distances[j] + 1.0E-5D < lowest) || (lowest == -1.0D)) {
            nextlowest = lowest;
            lowest = m_Distances[j];
            lowestcount = 1;
          }
          else if (Math.abs(m_Distances[j] - lowest) < 1.0E-5D)
          {

            lowestcount++;
          }
          else {
            nextlowest = m_Distances[j];
          }
        }
        
        m_ActualCount += 1;
      }
    }
    
    if ((nextlowest == -1.0D) || (lowest == -1.0D)) {
      scale = 1.0D;
      m_SmallestProb = (this.m_AverageProb = 1.0D);
      return scale;
    }
    

    double root = 1.0D / (nextlowest - lowest);
    int i = 0;
    


    double aimfor = (m_ActualCount - lowestcount) * m_BlendFactor / 100.0D + lowestcount;
    
    if (m_BlendFactor == 0) {
      aimfor += 1.0D;
    }
    
    double bot = 0.005D;
    double up = root * 16.0D;
    
    calculateSphereSize(bot, botvals);
    sphere -= aimfor;
    
    calculateSphereSize(up, upvals);
    sphere -= aimfor;
    
    if (sphere < 0.0D)
    {
      min_pos = bot;
      avgprob = avgProb;
      minprob = minProb;
    }
    else if (sphere > 0.0D)
    {
      min_pos = up;
      avgprob = avgProb;
      minprob = minProb;
    }
    else
    {
      do {
        calculateSphereSize(root, vals);
        sphere -= aimfor;
        if (Math.abs(sphere) < min_val) {
          min_val = Math.abs(sphere);
          min_pos = root;
          avgprob = avgProb;
          minprob = minProb;
        }
        if (Math.abs(sphere) <= 0.01D) {
          break;
        }
        if (sphere > 0.0D) {
          double broot = (root + up) / 2.0D;
          bot = root;
          root = broot;
        }
        else {
          double broot = (root + bot) / 2.0D;
          up = root;
          root = broot;
        }
        i++;
      } while (i <= 40);
      

      root = min_pos;
    }
    



    m_SmallestProb = minprob;
    m_AverageProb = avgprob;
    
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
    
    
    scale = min_pos;
    return scale;
  }
  






  private void calculateSphereSize(double scale, KStarWrapper params)
  {
    String debug = "(KStarNumericAttribute.calculateSphereSize)";
    
    double minprob = 1.0D;
    
    double pstarSum = 0.0D;
    double pstarSquareSum = 0.0D;
    
    for (int i = 0; i < m_NumInstances; i++) {
      if (m_Distances[i] >= 0.0D)
      {



        double pstar = PStar(m_Distances[i], scale);
        if (minprob > pstar) {
          minprob = pstar;
        }
        double inc = pstar / m_ActualCount;
        pstarSum += inc;
        pstarSquareSum += inc * inc;
      }
    }
    double sphereSize = pstarSquareSum == 0.0D ? 0.0D : pstarSum * pstarSum / pstarSquareSum;
    

    sphere = sphereSize;
    avgProb = pstarSum;
    minProb = minprob;
  }
  




  private double scaleFactorUsingEntropy()
  {
    String debug = "(KStarNumericAttribute.scaleFactorUsingEntropy)";
    if (m_ClassType != 1) {
      System.err.println("Error: " + debug + " attribute class must be nominal!");
      System.exit(1);
    }
    int lowestcount = 0;
    double lowest = -1.0D;double nextlowest = -1.0D;
    
    double actentropy = 0.0D;double randentropy = 0.0D;
    double minrand = 0.0D;double minact = 0.0D;double maxrand = 0.0D;double maxact = 0.0D;
    
    double scale = 1.0D;
    
    KStarWrapper botvals = new KStarWrapper();
    KStarWrapper upvals = new KStarWrapper();
    KStarWrapper vals = new KStarWrapper();
    
    m_Distances = new double[m_NumInstances];
    
    for (int j = 0; j < m_NumInstances; j++) {
      if (m_TrainSet.instance(j).isMissing(m_AttrIndex))
      {

        m_Distances[j] = -1.0D;
      }
      else {
        m_Distances[j] = Math.abs(m_TrainSet.instance(j).value(m_AttrIndex) - m_Test.value(m_AttrIndex));
        

        if ((m_Distances[j] + 1.0E-5D < nextlowest) || (nextlowest == -1.0D)) {
          if ((m_Distances[j] + 1.0E-5D < lowest) || (lowest == -1.0D)) {
            nextlowest = lowest;
            lowest = m_Distances[j];
            lowestcount = 1;
          }
          else if (Math.abs(m_Distances[j] - lowest) < 1.0E-5D)
          {

            lowestcount++;
          }
          else {
            nextlowest = m_Distances[j];
          }
        }
        
        m_ActualCount += 1;
      }
    }
    
    if ((nextlowest == -1.0D) || (lowest == -1.0D)) {
      scale = 1.0D;
      m_SmallestProb = (this.m_AverageProb = 1.0D);
      return scale;
    }
    

    double root = 1.0D / (nextlowest - lowest);
    
    double bot = 0.005D;
    double up = root * 8.0D;
    
    calculateEntropy(up, upvals);
    calculateEntropy(bot, botvals);
    double actscale = actEntropy - actEntropy;
    double randscale = randEntropy - randEntropy;
    
    double bestroot = root = bot;
    double currentdiff; double bestdiff = currentdiff = 0.1D;
    double bestpsum = avgProb;
    double bestminprob = minProb;
    double stepsize = (up - bot) / 20.0D;
    int itcount = 0;
    
    for (;;)
    {
      itcount++;
      double lastdiff = currentdiff;
      root += Math.log(root + 1.0D) * stepsize;
      double delta; double delta; if (root <= bot) {
        root = bot;
        currentdiff = 0.0D;
        delta = -1.0D;
      } else { double delta;
        if (root >= up) {
          root = up;
          currentdiff = 0.0D;
          delta = -1.0D;
        }
        else {
          calculateEntropy(root, vals);
          
          randEntropy = ((randEntropy - randEntropy) / randscale);
          
          actEntropy = ((actEntropy - actEntropy) / randscale);
          
          currentdiff = randEntropy - actEntropy;
          
          if (currentdiff < 0.1D) {
            currentdiff = 0.1D;
            if (stepsize < 0.0D)
            {

              bestdiff = currentdiff;
              bestroot = bot;
              bestpsum = avgProb;
              bestminprob = minProb;
              break;
            }
          }
          delta = currentdiff - lastdiff;
        } }
      if (currentdiff > bestdiff) {
        bestdiff = currentdiff;
        bestroot = root;
        bestminprob = minProb;
        bestpsum = avgProb;
      }
      if (delta < 0.0D) {
        if (Math.abs(stepsize) >= 0.01D)
        {


          stepsize /= -4.0D;
        }
      }
      else if (itcount > 40) {
        break;
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
    
    
    scale = bestroot;
    
    return scale;
  }
  





  private void calculateEntropy(double scale, KStarWrapper params)
  {
    String debug = "(KStarNumericAttribute.calculateEntropy)";
    
    double actent = 0.0D;double randent = 0.0D;
    double avgprob = 0.0D;double minprob = 1.0D;
    
    double[][] pseudoClassProbs = new double[6][m_NumClasses];
    
    for (int j = 0; j <= 5; j++) {
      for (int i = 0; i < m_NumClasses; i++) {
        pseudoClassProbs[j][i] = 0.0D;
      }
    }
    for (int i = 0; i < m_NumInstances; i++) {
      if (m_Distances[i] >= 0.0D)
      {



        double pstar = PStar(m_Distances[i], scale);
        double tprob = pstar / m_ActualCount;
        avgprob += tprob;
        if (pstar < minprob) {
          minprob = pstar;
        }
        
        for (int k = 0; k <= 5; k++)
        {


          pseudoClassProbs[k][m_RandClassCols[k][i]] += tprob;
        }
      }
    }
    

    for (j = m_NumClasses - 1; j >= 0; j--) {
      double actClassProb = pseudoClassProbs[5][j] / avgprob;
      if (actClassProb > 0.0D) {
        actent -= actClassProb * Math.log(actClassProb) / 0.693147181D;
      }
    }
    

    for (int k = 0; k < 5; k++) {
      for (i = m_NumClasses - 1; i >= 0; i--) {
        double randClassProb = pseudoClassProbs[k][i] / avgprob;
        if (randClassProb > 0.0D) {
          randent -= randClassProb * Math.log(randClassProb) / 0.693147181D;
        }
      }
    }
    randent /= 5.0D;
    
    actEntropy = actent;
    randEntropy = randent;
    avgProb = avgprob;
    minProb = minprob;
  }
  







  private double PStar(double x, double scale)
  {
    return scale * Math.exp(-2.0D * x * scale);
  }
  





  public void setOptions(int missingmode, int blendmethod, int blendfactor)
  {
    m_MissingMode = missingmode;
    m_BlendMethod = blendmethod;
    m_BlendFactor = blendfactor;
  }
  



  public void setMissingMode(int mode)
  {
    m_MissingMode = mode;
  }
  



  public void setBlendMethod(int method)
  {
    m_BlendMethod = method;
  }
  



  public void setBlendFactor(int factor)
  {
    m_BlendFactor = factor;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
