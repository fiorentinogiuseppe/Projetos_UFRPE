package weka.classifiers.trees.j48;

import java.io.Serializable;
import java.util.Enumeration;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;












































public class Distribution
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = 8526859638230806576L;
  private double[][] m_perClassPerBag;
  private double[] m_perBag;
  private double[] m_perClass;
  private double totaL;
  
  public Distribution(int numBags, int numClasses)
  {
    m_perClassPerBag = new double[numBags][0];
    m_perBag = new double[numBags];
    m_perClass = new double[numClasses];
    for (int i = 0; i < numBags; i++)
      m_perClassPerBag[i] = new double[numClasses];
    totaL = 0.0D;
  }
  






  public Distribution(double[][] table)
  {
    m_perClassPerBag = table;
    m_perBag = new double[table.length];
    m_perClass = new double[table[0].length];
    for (int i = 0; i < table.length; i++) {
      for (int j = 0; j < table[i].length; j++) {
        m_perBag[i] += table[i][j];
        m_perClass[j] += table[i][j];
        totaL += table[i][j];
      }
    }
  }
  




  public Distribution(Instances source)
    throws Exception
  {
    m_perClassPerBag = new double[1][0];
    m_perBag = new double[1];
    totaL = 0.0D;
    m_perClass = new double[source.numClasses()];
    m_perClassPerBag[0] = new double[source.numClasses()];
    Enumeration enu = source.enumerateInstances();
    while (enu.hasMoreElements()) {
      add(0, (Instance)enu.nextElement());
    }
  }
  











  public Distribution(Instances source, ClassifierSplitModel modelToUse)
    throws Exception
  {
    m_perClassPerBag = new double[modelToUse.numSubsets()][0];
    m_perBag = new double[modelToUse.numSubsets()];
    totaL = 0.0D;
    m_perClass = new double[source.numClasses()];
    for (int i = 0; i < modelToUse.numSubsets(); i++)
      m_perClassPerBag[i] = new double[source.numClasses()];
    Enumeration enu = source.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      int index = modelToUse.whichSubset(instance);
      if (index != -1) {
        add(index, instance);
      } else {
        double[] weights = modelToUse.weights(instance);
        addWeights(instance, weights);
      }
    }
  }
  




  public Distribution(Distribution toMerge)
  {
    totaL = totaL;
    m_perClass = new double[toMerge.numClasses()];
    System.arraycopy(m_perClass, 0, m_perClass, 0, toMerge.numClasses());
    m_perClassPerBag = new double[1][0];
    m_perClassPerBag[0] = new double[toMerge.numClasses()];
    System.arraycopy(m_perClass, 0, m_perClassPerBag[0], 0, toMerge.numClasses());
    
    m_perBag = new double[1];
    m_perBag[0] = totaL;
  }
  






  public Distribution(Distribution toMerge, int index)
  {
    totaL = totaL;
    m_perClass = new double[toMerge.numClasses()];
    System.arraycopy(m_perClass, 0, m_perClass, 0, toMerge.numClasses());
    m_perClassPerBag = new double[2][0];
    m_perClassPerBag[0] = new double[toMerge.numClasses()];
    System.arraycopy(m_perClassPerBag[index], 0, m_perClassPerBag[0], 0, toMerge.numClasses());
    
    m_perClassPerBag[1] = new double[toMerge.numClasses()];
    for (int i = 0; i < toMerge.numClasses(); i++)
      m_perClassPerBag[1][i] = (m_perClass[i] - m_perClassPerBag[0][i]);
    m_perBag = new double[2];
    m_perBag[0] = m_perBag[index];
    m_perBag[1] = (totaL - m_perBag[0]);
  }
  



  public final int actualNumBags()
  {
    int returnValue = 0;
    

    for (int i = 0; i < m_perBag.length; i++) {
      if (Utils.gr(m_perBag[i], 0.0D))
        returnValue++;
    }
    return returnValue;
  }
  



  public final int actualNumClasses()
  {
    int returnValue = 0;
    

    for (int i = 0; i < m_perClass.length; i++) {
      if (Utils.gr(m_perClass[i], 0.0D))
        returnValue++;
    }
    return returnValue;
  }
  



  public final int actualNumClasses(int bagIndex)
  {
    int returnValue = 0;
    

    for (int i = 0; i < m_perClass.length; i++) {
      if (Utils.gr(m_perClassPerBag[bagIndex][i], 0.0D))
        returnValue++;
    }
    return returnValue;
  }
  








  public final void add(int bagIndex, Instance instance)
    throws Exception
  {
    int classIndex = (int)instance.classValue();
    double weight = instance.weight();
    m_perClassPerBag[bagIndex][classIndex] += weight;
    
    m_perBag[bagIndex] += weight;
    m_perClass[classIndex] += weight;
    totaL += weight;
  }
  








  public final void sub(int bagIndex, Instance instance)
    throws Exception
  {
    int classIndex = (int)instance.classValue();
    double weight = instance.weight();
    m_perClassPerBag[bagIndex][classIndex] -= weight;
    
    m_perBag[bagIndex] -= weight;
    m_perClass[classIndex] -= weight;
    totaL -= weight;
  }
  



  public final void add(int bagIndex, double[] counts)
  {
    double sum = Utils.sum(counts);
    
    for (int i = 0; i < counts.length; i++)
      m_perClassPerBag[bagIndex][i] += counts[i];
    m_perBag[bagIndex] += sum;
    for (int i = 0; i < counts.length; i++)
      m_perClass[i] += counts[i];
    totaL += sum;
  }
  













  public final void addInstWithUnknown(Instances source, int attIndex)
    throws Exception
  {
    double[] probs = new double[m_perBag.length];
    for (int j = 0; j < m_perBag.length; j++) {
      if (Utils.eq(totaL, 0.0D)) {
        probs[j] = (1.0D / probs.length);
      } else {
        probs[j] = (m_perBag[j] / totaL);
      }
    }
    Enumeration enu = source.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      if (instance.isMissing(attIndex)) {
        int classIndex = (int)instance.classValue();
        double weight = instance.weight();
        m_perClass[classIndex] += weight;
        totaL += weight;
        for (j = 0; j < m_perBag.length; j++) {
          double newWeight = probs[j] * weight;
          m_perClassPerBag[j][classIndex] += newWeight;
          
          m_perBag[j] += newWeight;
        }
      }
    }
  }
  






  public final void addRange(int bagIndex, Instances source, int startIndex, int lastPlusOne)
    throws Exception
  {
    double sumOfWeights = 0.0D;
    



    for (int i = startIndex; i < lastPlusOne; i++) {
      Instance instance = source.instance(i);
      int classIndex = (int)instance.classValue();
      sumOfWeights += instance.weight();
      m_perClassPerBag[bagIndex][classIndex] += instance.weight();
      m_perClass[classIndex] += instance.weight();
    }
    m_perBag[bagIndex] += sumOfWeights;
    totaL += sumOfWeights;
  }
  









  public final void addWeights(Instance instance, double[] weights)
    throws Exception
  {
    int classIndex = (int)instance.classValue();
    for (int i = 0; i < m_perBag.length; i++) {
      double weight = instance.weight() * weights[i];
      m_perClassPerBag[i][classIndex] += weight;
      m_perBag[i] += weight;
      m_perClass[classIndex] += weight;
      totaL += weight;
    }
  }
  



  public final boolean check(double minNoObj)
  {
    int counter = 0;
    

    for (int i = 0; i < m_perBag.length; i++)
      if (Utils.grOrEq(m_perBag[i], minNoObj))
        counter++;
    if (counter > 1) {
      return true;
    }
    return false;
  }
  





  public final Object clone()
  {
    Distribution newDistribution = new Distribution(m_perBag.length, m_perClass.length);
    
    for (int i = 0; i < m_perBag.length; i++) {
      m_perBag[i] = m_perBag[i];
      for (int j = 0; j < m_perClass.length; j++)
        m_perClassPerBag[i][j] = m_perClassPerBag[i][j];
    }
    for (int j = 0; j < m_perClass.length; j++)
      m_perClass[j] = m_perClass[j];
    totaL = totaL;
    
    return newDistribution;
  }
  








  public final void del(int bagIndex, Instance instance)
    throws Exception
  {
    int classIndex = (int)instance.classValue();
    double weight = instance.weight();
    m_perClassPerBag[bagIndex][classIndex] -= weight;
    
    m_perBag[bagIndex] -= weight;
    m_perClass[classIndex] -= weight;
    totaL -= weight;
  }
  






  public final void delRange(int bagIndex, Instances source, int startIndex, int lastPlusOne)
    throws Exception
  {
    double sumOfWeights = 0.0D;
    



    for (int i = startIndex; i < lastPlusOne; i++) {
      Instance instance = source.instance(i);
      int classIndex = (int)instance.classValue();
      sumOfWeights += instance.weight();
      m_perClassPerBag[bagIndex][classIndex] -= instance.weight();
      m_perClass[classIndex] -= instance.weight();
    }
    m_perBag[bagIndex] -= sumOfWeights;
    totaL -= sumOfWeights;
  }
  







  public final String dumpDistribution()
  {
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < m_perBag.length; i++) {
      text.append("Bag num " + i + "\n");
      for (int j = 0; j < m_perClass.length; j++)
        text.append("Class num " + j + " " + m_perClassPerBag[i][j] + "\n");
    }
    return text.toString();
  }
  



  public final void initialize()
  {
    for (int i = 0; i < m_perClass.length; i++)
      m_perClass[i] = 0.0D;
    for (int i = 0; i < m_perBag.length; i++)
      m_perBag[i] = 0.0D;
    for (int i = 0; i < m_perBag.length; i++)
      for (int j = 0; j < m_perClass.length; j++)
        m_perClassPerBag[i][j] = 0.0D;
    totaL = 0.0D;
  }
  



  public final double[][] matrix()
  {
    return m_perClassPerBag;
  }
  







  public final int maxBag()
  {
    double max = 0.0D;
    int maxIndex = -1;
    for (int i = 0; i < m_perBag.length; i++)
      if (Utils.grOrEq(m_perBag[i], max)) {
        max = m_perBag[i];
        maxIndex = i;
      }
    return maxIndex;
  }
  



  public final int maxClass()
  {
    double maxCount = 0.0D;
    int maxIndex = 0;
    

    for (int i = 0; i < m_perClass.length; i++) {
      if (Utils.gr(m_perClass[i], maxCount)) {
        maxCount = m_perClass[i];
        maxIndex = i;
      }
    }
    return maxIndex;
  }
  



  public final int maxClass(int index)
  {
    double maxCount = 0.0D;
    int maxIndex = 0;
    

    if (Utils.gr(m_perBag[index], 0.0D)) {
      for (int i = 0; i < m_perClass.length; i++)
        if (Utils.gr(m_perClassPerBag[index][i], maxCount)) {
          maxCount = m_perClassPerBag[index][i];
          maxIndex = i;
        }
      return maxIndex;
    }
    return maxClass();
  }
  



  public final int numBags()
  {
    return m_perBag.length;
  }
  



  public final int numClasses()
  {
    return m_perClass.length;
  }
  



  public final double numCorrect()
  {
    return m_perClass[maxClass()];
  }
  



  public final double numCorrect(int index)
  {
    return m_perClassPerBag[index][maxClass(index)];
  }
  



  public final double numIncorrect()
  {
    return totaL - numCorrect();
  }
  



  public final double numIncorrect(int index)
  {
    return m_perBag[index] - numCorrect(index);
  }
  




  public final double perClassPerBag(int bagIndex, int classIndex)
  {
    return m_perClassPerBag[bagIndex][classIndex];
  }
  



  public final double perBag(int bagIndex)
  {
    return m_perBag[bagIndex];
  }
  



  public final double perClass(int classIndex)
  {
    return m_perClass[classIndex];
  }
  




  public final double laplaceProb(int classIndex)
  {
    return (m_perClass[classIndex] + 1.0D) / (totaL + m_perClass.length);
  }
  




  public final double laplaceProb(int classIndex, int intIndex)
  {
    if (Utils.gr(m_perBag[intIndex], 0.0D)) {
      return (m_perClassPerBag[intIndex][classIndex] + 1.0D) / (m_perBag[intIndex] + m_perClass.length);
    }
    
    return laplaceProb(classIndex);
  }
  




  public final double prob(int classIndex)
  {
    if (!Utils.eq(totaL, 0.0D)) {
      return m_perClass[classIndex] / totaL;
    }
    return 0.0D;
  }
  




  public final double prob(int classIndex, int intIndex)
  {
    if (Utils.gr(m_perBag[intIndex], 0.0D)) {
      return m_perClassPerBag[intIndex][classIndex] / m_perBag[intIndex];
    }
    return prob(classIndex);
  }
  




  public final Distribution subtract(Distribution toSubstract)
  {
    Distribution newDist = new Distribution(1, m_perClass.length);
    
    m_perBag[0] = (totaL - totaL);
    totaL = m_perBag[0];
    for (int i = 0; i < m_perClass.length; i++) {
      m_perClassPerBag[0][i] = (m_perClass[i] - m_perClass[i]);
      m_perClass[i] = m_perClassPerBag[0][i];
    }
    return newDist;
  }
  



  public final double total()
  {
    return totaL;
  }
  








  public final void shift(int from, int to, Instance instance)
    throws Exception
  {
    int classIndex = (int)instance.classValue();
    double weight = instance.weight();
    m_perClassPerBag[from][classIndex] -= weight;
    m_perClassPerBag[to][classIndex] += weight;
    m_perBag[from] -= weight;
    m_perBag[to] += weight;
  }
  











  public final void shiftRange(int from, int to, Instances source, int startIndex, int lastPlusOne)
    throws Exception
  {
    for (int i = startIndex; i < lastPlusOne; i++) {
      Instance instance = source.instance(i);
      int classIndex = (int)instance.classValue();
      double weight = instance.weight();
      m_perClassPerBag[from][classIndex] -= weight;
      m_perClassPerBag[to][classIndex] += weight;
      m_perBag[from] -= weight;
      m_perBag[to] += weight;
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
}
