package weka.classifiers.trees.j48;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;



























































public class GraftSplit
  extends ClassifierSplitModel
  implements Comparable
{
  private static final long serialVersionUID = 722773260393182051L;
  private Distribution m_graftdistro;
  private int m_attIndex;
  private double m_splitPoint;
  private int m_maxClass;
  private int m_otherLeafMaxClass;
  private double m_laplace;
  private Distribution m_leafdistro;
  private int m_testType;
  
  public GraftSplit(int a, double v, int t, double c, double l)
  {
    m_attIndex = a;
    m_splitPoint = v;
    m_testType = t;
    m_maxClass = ((int)c);
    m_laplace = l;
  }
  









  public GraftSplit(int a, double v, int t, double oC, double[][] counts)
    throws Exception
  {
    m_attIndex = a;
    m_splitPoint = v;
    m_testType = t;
    m_otherLeafMaxClass = ((int)oC);
    

    m_numSubsets = 2;
    

    int subset = subsetOfInterest();
    

    m_distribution = new Distribution(counts);
    

    double[][] lcounts = new double[1][m_distribution.numClasses()];
    for (int c = 0; c < lcounts[0].length; c++) {
      lcounts[0][c] = counts[subset][c];
    }
    m_leafdistro = new Distribution(lcounts);
    

    m_maxClass = m_distribution.maxClass(subset);
    

    m_laplace = ((m_distribution.perClassPerBag(subset, m_maxClass) + 1.0D) / (m_distribution.perBag(subset) + 2.0D));
  }
  










  public void deleteGraftedCases(Instances data)
  {
    int subOfInterest = subsetOfInterest();
    for (int x = 0; x < data.numInstances(); x++) {
      if (whichSubset(data.instance(x)) == subOfInterest) {
        data.delete(x--);
      }
    }
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    m_graftdistro = new Distribution(2, data.numClasses());
    

    int subset = subsetOfInterest();
    
    double thisNodeCount = 0.0D;
    double knownCases = 0.0D;
    boolean allKnown = true;
    
    for (int x = 0; x < data.numInstances(); x++) {
      Instance instance = data.instance(x);
      if (instance.isMissing(m_attIndex)) {
        allKnown = false;
      }
      else {
        knownCases += instance.weight();
        int subst = whichSubset(instance);
        if (subst != -1)
        {
          m_graftdistro.add(subst, instance);
          if (subst == subset)
            thisNodeCount += instance.weight();
        }
      } }
    double factor = knownCases == 0.0D ? 0.5D : thisNodeCount / knownCases;
    
    if (!allKnown) {
      for (int x = 0; x < data.numInstances(); x++) {
        if (data.instance(x).isMissing(m_attIndex)) {
          Instance instance = data.instance(x);
          int subst = whichSubset(instance);
          if (subst != -1)
          {
            instance.setWeight(instance.weight() * factor);
            m_graftdistro.add(subst, instance);
          }
        }
      }
    }
    

    if (m_graftdistro.perBag(subset) == 0.0D) {
      double[] counts = new double[data.numClasses()];
      counts[m_maxClass] = 0.01D;
      m_graftdistro.add(subset, counts);
    }
    if (m_graftdistro.perBag(subset == 0 ? 1 : 0) == 0.0D) {
      double[] counts = new double[data.numClasses()];
      counts[m_otherLeafMaxClass] = 0.01D;
      m_graftdistro.add(subset == 0 ? 1 : 0, counts);
    }
  }
  



  public NoSplit getLeaf()
  {
    return new NoSplit(m_leafdistro);
  }
  





  public NoSplit getOtherLeaf()
  {
    int bag = subsetOfInterest() == 0 ? 1 : 0;
    
    double[][] counts = new double[1][m_graftdistro.numClasses()];
    double totals = 0.0D;
    for (int c = 0; c < counts[0].length; c++) {
      counts[0][c] = m_graftdistro.perClassPerBag(bag, c);
      totals += counts[0][c];
    }
    
    if (totals == 0.0D) {
      counts[0][m_otherLeafMaxClass] += 0.01D;
    }
    return new NoSplit(new Distribution(counts));
  }
  










  public final String dumpLabelG(int index, Instances data)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    text.append(data.classAttribute().value(index == subsetOfInterest() ? m_maxClass : m_otherLeafMaxClass));
    
    text.append(" (" + Utils.roundDouble(m_graftdistro.perBag(index), 1));
    if (Utils.gr(m_graftdistro.numIncorrect(index), 0.0D)) {
      text.append("/" + Utils.roundDouble(m_graftdistro.numIncorrect(index), 2));
    }
    

    if (index == subsetOfInterest()) {
      text.append("|" + Utils.roundDouble(m_distribution.perBag(index), 2));
      if (Utils.gr(m_distribution.numIncorrect(index), 0.0D)) {
        text.append("/" + Utils.roundDouble(m_distribution.numIncorrect(index), 2));
      }
    }
    text.append(")");
    return text.toString();
  }
  



  public int subsetOfInterest()
  {
    if (m_testType == 2)
      return 0;
    if (m_testType == 3)
      return 1;
    return m_testType;
  }
  



  public double positivesForSubsetOfInterest()
  {
    return m_distribution.perClassPerBag(subsetOfInterest(), m_maxClass);
  }
  




  public double positives(int subset)
  {
    return m_distribution.perClassPerBag(subset, m_distribution.maxClass(subset));
  }
  




  public double totalForSubsetOfInterest()
  {
    return m_distribution.perBag(subsetOfInterest());
  }
  




  public double totalForSubset(int subset)
  {
    return m_distribution.perBag(subset);
  }
  





  public String leftSide(Instances data)
  {
    return data.attribute(m_attIndex).name();
  }
  



  public int attribute()
  {
    return m_attIndex;
  }
  






  public final String rightSide(int index, Instances data)
  {
    StringBuffer text = new StringBuffer();
    if (data.attribute(m_attIndex).isNominal()) {
      if (index == 0) {
        text.append(" = " + data.attribute(m_attIndex).value((int)m_splitPoint));
      }
      else {
        text.append(" != " + data.attribute(m_attIndex).value((int)m_splitPoint));
      }
    }
    else if (index == 0) {
      text.append(" <= " + Utils.doubleToString(m_splitPoint, 6));
    }
    else {
      text.append(" > " + Utils.doubleToString(m_splitPoint, 6));
    }
    return text.toString();
  }
  









  public final String sourceExpression(int index, Instances data)
  {
    StringBuffer expr = null;
    if (index < 0) {
      return "i[" + m_attIndex + "] == null";
    }
    if (data.attribute(m_attIndex).isNominal()) {
      if (index == 0) {
        expr = new StringBuffer("i[");
      } else
        expr = new StringBuffer("!i[");
      expr.append(m_attIndex).append("]");
      expr.append(".equals(\"").append(data.attribute(m_attIndex).value((int)m_splitPoint)).append("\")");
    }
    else {
      expr = new StringBuffer("((Double) i[");
      expr.append(m_attIndex).append("])");
      if (index == 0) {
        expr.append(".doubleValue() <= ").append(m_splitPoint);
      } else {
        expr.append(".doubleValue() > ").append(m_splitPoint);
      }
    }
    return expr.toString();
  }
  








  public double[] weights(Instance instance)
  {
    if (instance.isMissing(m_attIndex)) {
      double[] weights = new double[m_numSubsets];
      for (int i = 0; i < m_numSubsets; i++) {
        weights[i] = (m_graftdistro.perBag(i) / m_graftdistro.total());
      }
      return weights;
    }
    return null;
  }
  






  public int whichSubset(Instance instance)
  {
    if (instance.isMissing(m_attIndex)) {
      return -1;
    }
    if (instance.attribute(m_attIndex).isNominal())
    {
      if (instance.value(m_attIndex) == m_splitPoint) {
        return 0;
      }
      return 1;
    }
    if (Utils.smOrEq(instance.value(m_attIndex), m_splitPoint)) {
      return 0;
    }
    return 1;
  }
  




  public double splitPoint()
  {
    return m_splitPoint;
  }
  


  public int maxClassForSubsetOfInterest()
  {
    return m_maxClass;
  }
  


  public double laplaceForSubsetOfInterest()
  {
    return m_laplace;
  }
  



  public int testType()
  {
    return m_testType;
  }
  





  public int compareTo(Object g)
  {
    if (m_laplace > ((GraftSplit)g).laplaceForSubsetOfInterest())
      return 1;
    if (m_laplace < ((GraftSplit)g).laplaceForSubsetOfInterest())
      return -1;
    return 0;
  }
  






  public final double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    if (theSubset <= -1) {
      double[] weights = weights(instance);
      if (weights == null) {
        return m_distribution.prob(classIndex);
      }
      double prob = 0.0D;
      for (int i = 0; i < weights.length; i++) {
        prob += weights[i] * m_distribution.prob(classIndex, i);
      }
      return prob;
    }
    
    if (Utils.gr(m_distribution.perBag(theSubset), 0.0D)) {
      return m_distribution.prob(classIndex, theSubset);
    }
    return m_distribution.prob(classIndex);
  }
  



  public String toString(Instances data)
  {
    String theTest;
    

    String theTest;
    

    if (m_testType == 0) {
      theTest = " <= "; } else { String theTest;
      if (m_testType == 1) {
        theTest = " > "; } else { String theTest;
        if (m_testType == 2) {
          theTest = " = ";
        } else
          theTest = " != ";
      } }
    if (data.attribute(m_attIndex).isNominal()) {
      theTest = theTest + data.attribute(m_attIndex).value((int)m_splitPoint);
    } else {
      theTest = theTest + Double.toString(m_splitPoint);
    }
    return data.attribute(m_attIndex).name() + theTest + " (" + Double.toString(m_laplace) + ") --> " + data.attribute(data.classIndex()).value(m_maxClass);
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
