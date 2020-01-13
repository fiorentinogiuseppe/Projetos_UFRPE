package weka.attributeSelection;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

















































































































































































public class ReliefFAttributeEval
  extends ASEvaluation
  implements AttributeEvaluator, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -8422186665795839379L;
  private Instances m_trainInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private boolean m_numericClass;
  private int m_numClasses;
  private double m_ndc;
  private double[] m_nda;
  private double[] m_ndcda;
  private double[] m_weights;
  private double[] m_classProbs;
  private int m_sampleM;
  private int m_Knn;
  private double[][][] m_karray;
  private double[] m_maxArray;
  private double[] m_minArray;
  private double[] m_worst;
  private int[] m_index;
  private int[] m_stored;
  private int m_seed;
  private double[] m_weightsByRank;
  private int m_sigma;
  private boolean m_weightByDistance;
  
  public ReliefFAttributeEval()
  {
    resetOptions();
  }
  




  public String globalInfo()
  {
    return "ReliefFAttributeEval :\n\nEvaluates the worth of an attribute by repeatedly sampling an instance and considering the value of the given attribute for the nearest instance of the same and different class. Can operate on both discrete and continuous class data.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Kenji Kira and Larry A. Rendell");
    result.setValue(TechnicalInformation.Field.TITLE, "A Practical Approach to Feature Selection");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Ninth International Workshop on Machine Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "Derek H. Sleeman and Peter Edwards");
    result.setValue(TechnicalInformation.Field.YEAR, "1992");
    result.setValue(TechnicalInformation.Field.PAGES, "249-256");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Igor Kononenko");
    additional.setValue(TechnicalInformation.Field.TITLE, "Estimating Attributes: Analysis and Extensions of RELIEF");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "European Conference on Machine Learning");
    additional.setValue(TechnicalInformation.Field.EDITOR, "Francesco Bergadano and Luc De Raedt");
    additional.setValue(TechnicalInformation.Field.YEAR, "1994");
    additional.setValue(TechnicalInformation.Field.PAGES, "171-182");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Marko Robnik-Sikonja and Igor Kononenko");
    additional.setValue(TechnicalInformation.Field.TITLE, "An adaptation of Relief for attribute estimation in regression");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Fourteenth International Conference on Machine Learning");
    additional.setValue(TechnicalInformation.Field.EDITOR, "Douglas H. Fisher");
    additional.setValue(TechnicalInformation.Field.YEAR, "1997");
    additional.setValue(TechnicalInformation.Field.PAGES, "296-304");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    
    return result;
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    newVector.addElement(new Option("\tSpecify the number of instances to\n\tsample when estimating attributes.\n\tIf not specified, then all instances\n\twill be used.", "M", 1, "-M <num instances>"));
    




    newVector.addElement(new Option("\tSeed for randomly sampling instances.\n\t(Default = 1)", "D", 1, "-D <seed>"));
    


    newVector.addElement(new Option("\tNumber of nearest neighbours (k) used\n\tto estimate attribute relevances\n\t(Default = 10).", "K", 1, "-K <number of neighbours>"));
    



    newVector.addElement(new Option("\tWeight nearest neighbours by distance", "W", 0, "-W"));
    

    newVector.addElement(new Option("\tSpecify sigma value (used in an exp\n\tfunction to control how quickly\n\tweights for more distant instances\n\tdecrease. Use in conjunction with -W.\n\tSensible value=1/5 to 1/10 of the\n\tnumber of nearest neighbours.\n\t(Default = 2)", "A", 1, "-A <num>"));
    






    return newVector.elements();
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    setWeightByDistance(Utils.getFlag('W', options));
    String optionString = Utils.getOption('M', options);
    
    if (optionString.length() != 0) {
      setSampleSize(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('D', options);
    
    if (optionString.length() != 0) {
      setSeed(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('K', options);
    
    if (optionString.length() != 0) {
      setNumNeighbours(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('A', options);
    
    if (optionString.length() != 0) {
      setWeightByDistance(true);
      setSigma(Integer.parseInt(optionString));
    }
  }
  




  public String sigmaTipText()
  {
    return "Set influence of nearest neighbours. Used in an exp function to control how quickly weights decrease for more distant instances. Use in conjunction with weightByDistance. Sensible values = 1/5 to 1/10 the number of nearest neighbours.";
  }
  








  public void setSigma(int s)
    throws Exception
  {
    if (s <= 0) {
      throw new Exception("value of sigma must be > 0!");
    }
    
    m_sigma = s;
  }
  





  public int getSigma()
  {
    return m_sigma;
  }
  




  public String numNeighboursTipText()
  {
    return "Number of nearest neighbours for attribute estimation.";
  }
  




  public void setNumNeighbours(int n)
  {
    m_Knn = n;
  }
  





  public int getNumNeighbours()
  {
    return m_Knn;
  }
  




  public String seedTipText()
  {
    return "Random seed for sampling instances.";
  }
  




  public void setSeed(int s)
  {
    m_seed = s;
  }
  





  public int getSeed()
  {
    return m_seed;
  }
  




  public String sampleSizeTipText()
  {
    return "Number of instances to sample. Default (-1) indicates that all instances will be used for attribute estimation.";
  }
  





  public void setSampleSize(int s)
  {
    m_sampleM = s;
  }
  





  public int getSampleSize()
  {
    return m_sampleM;
  }
  




  public String weightByDistanceTipText()
  {
    return "Weight nearest neighbours by their distance.";
  }
  




  public void setWeightByDistance(boolean b)
  {
    m_weightByDistance = b;
  }
  





  public boolean getWeightByDistance()
  {
    return m_weightByDistance;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[9];
    int current = 0;
    
    if (getWeightByDistance()) {
      options[(current++)] = "-W";
    }
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getSampleSize());
    options[(current++)] = "-D";
    options[(current++)] = ("" + getSeed());
    options[(current++)] = "-K";
    options[(current++)] = ("" + getNumNeighbours());
    
    if (getWeightByDistance()) {
      options[(current++)] = "-A";
      options[(current++)] = ("" + getSigma());
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("ReliefF feature evaluator has not been built yet\n");
    }
    else {
      text.append("\tReliefF Ranking Filter");
      text.append("\n\tInstances sampled: ");
      
      if (m_sampleM == -1) {
        text.append("all\n");
      }
      else {
        text.append(m_sampleM + "\n");
      }
      
      text.append("\tNumber of nearest neighbours (k): " + m_Knn + "\n");
      
      if (m_weightByDistance) {
        text.append("\tExponentially decreasing (with distance) influence for\n\tnearest neighbours. Sigma: " + m_sigma + "\n");

      }
      else
      {

        text.append("\tEqual influence nearest neighbours\n");
      }
    }
    
    return text.toString();
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
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  








  public void buildEvaluator(Instances data)
    throws Exception
  {
    Random r = new Random(m_seed);
    

    getCapabilities().testWithFail(data);
    
    m_trainInstances = data;
    m_classIndex = m_trainInstances.classIndex();
    m_numAttribs = m_trainInstances.numAttributes();
    m_numInstances = m_trainInstances.numInstances();
    
    if (m_trainInstances.attribute(m_classIndex).isNumeric()) {
      m_numericClass = true;
    }
    else {
      m_numericClass = false;
    }
    
    if (!m_numericClass) {
      m_numClasses = m_trainInstances.attribute(m_classIndex).numValues();
    }
    else {
      m_ndc = 0.0D;
      m_numClasses = 1;
      m_nda = new double[m_numAttribs];
      m_ndcda = new double[m_numAttribs];
    }
    
    if (m_weightByDistance)
    {
      m_weightsByRank = new double[m_Knn];
      
      for (int i = 0; i < m_Knn; i++) {
        m_weightsByRank[i] = Math.exp(-(i / m_sigma * (i / m_sigma)));
      }
    }
    


    m_weights = new double[m_numAttribs];
    

    m_karray = new double[m_numClasses][m_Knn][2];
    
    if (!m_numericClass) {
      m_classProbs = new double[m_numClasses];
      
      for (int i = 0; i < m_numInstances; i++) {
        m_classProbs[((int)m_trainInstances.instance(i).value(m_classIndex))] += 1.0D;
      }
      
      for (int i = 0; i < m_numClasses; i++) {
        m_classProbs[i] /= m_numInstances;
      }
    }
    
    m_worst = new double[m_numClasses];
    m_index = new int[m_numClasses];
    m_stored = new int[m_numClasses];
    m_minArray = new double[m_numAttribs];
    m_maxArray = new double[m_numAttribs];
    
    for (int i = 0; i < m_numAttribs; i++) {
      double tmp411_408 = NaN.0D;m_maxArray[i] = tmp411_408;m_minArray[i] = tmp411_408;
    }
    
    for (int i = 0; i < m_numInstances; i++)
      updateMinMax(m_trainInstances.instance(i));
    int totalInstances;
    int totalInstances;
    if ((m_sampleM > m_numInstances) || (m_sampleM < 0)) {
      totalInstances = m_numInstances;
    }
    else {
      totalInstances = m_sampleM;
    }
    

    for (int i = 0; i < totalInstances; i++) { int z;
      int z; if (totalInstances == m_numInstances) {
        z = i;
      }
      else {
        z = r.nextInt() % m_numInstances;
      }
      
      if (z < 0) {
        z *= -1;
      }
      
      if (!m_trainInstances.instance(z).isMissing(m_classIndex))
      {
        for (int j = 0; j < m_numClasses; j++) {
          int tmp567_566 = 0;m_stored[j] = tmp567_566;m_index[j] = tmp567_566;
          
          for (int k = 0; k < m_Knn; k++) {
            double tmp605_604 = 0.0D;m_karray[j][k][1] = tmp605_604;m_karray[j][k][0] = tmp605_604;
          }
        }
        
        findKHitMiss(z);
        
        if (m_numericClass) {
          updateWeightsNumericClass(z);
        }
        else {
          updateWeightsDiscreteClass(z);
        }
      }
    }
    



    for (int i = 0; i < m_numAttribs; i++) { if (i != m_classIndex) {
        if (m_numericClass) {
          m_weights[i] = (m_ndcda[i] / m_ndc - (m_nda[i] - m_ndcda[i]) / (totalInstances - m_ndc));
        }
        else
        {
          m_weights[i] *= 1.0D / totalInstances;
        }
      }
    }
  }
  









  public double evaluateAttribute(int attribute)
    throws Exception
  {
    return m_weights[attribute];
  }
  



  protected void resetOptions()
  {
    m_trainInstances = null;
    m_sampleM = -1;
    m_Knn = 10;
    m_sigma = 2;
    m_weightByDistance = false;
    m_seed = 1;
  }
  







  private double norm(double x, int i)
  {
    if ((Double.isNaN(m_minArray[i])) || (Utils.eq(m_maxArray[i], m_minArray[i])))
    {
      return 0.0D;
    }
    
    return (x - m_minArray[i]) / (m_maxArray[i] - m_minArray[i]);
  }
  







  private void updateMinMax(Instance instance)
  {
    try
    {
      for (int j = 0; j < instance.numValues(); j++) {
        if ((instance.attributeSparse(j).isNumeric()) && (!instance.isMissingSparse(j)))
        {
          if (Double.isNaN(m_minArray[instance.index(j)])) {
            m_minArray[instance.index(j)] = instance.valueSparse(j);
            m_maxArray[instance.index(j)] = instance.valueSparse(j);

          }
          else if (instance.valueSparse(j) < m_minArray[instance.index(j)]) {
            m_minArray[instance.index(j)] = instance.valueSparse(j);

          }
          else if (instance.valueSparse(j) > m_maxArray[instance.index(j)]) {
            m_maxArray[instance.index(j)] = instance.valueSparse(j);
          }
        }
      }
    }
    catch (Exception ex)
    {
      System.err.println(ex);
      ex.printStackTrace();
    }
  }
  




  private double difference(int index, double val1, double val2)
  {
    switch (m_trainInstances.attribute(index).type())
    {

    case 1: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2)))
      {
        return 1.0D - 1.0D / m_trainInstances.attribute(index).numValues();
      }
      if ((int)val1 != (int)val2) {
        return 1.0D;
      }
      return 0.0D;
    


    case 0: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2)))
      {
        if ((Instance.isMissingValue(val1)) && (Instance.isMissingValue(val2)))
        {
          return 1.0D; }
        double diff;
        double diff;
        if (Instance.isMissingValue(val2)) {
          diff = norm(val1, index);
        } else {
          diff = norm(val2, index);
        }
        if (diff < 0.5D) {
          diff = 1.0D - diff;
        }
        return diff;
      }
      
      return Math.abs(norm(val1, index) - norm(val2, index));
    }
    
    return 0.0D;
  }
  








  private double distance(Instance first, Instance second)
  {
    double distance = 0.0D;
    

    int p1 = 0;int p2 = 0;
    while ((p1 < first.numValues()) || (p2 < second.numValues())) { int firstI;
      int firstI; if (p1 >= first.numValues()) {
        firstI = m_trainInstances.numAttributes();
      } else
        firstI = first.index(p1);
      int secondI;
      int secondI; if (p2 >= second.numValues()) {
        secondI = m_trainInstances.numAttributes();
      } else {
        secondI = second.index(p2);
      }
      if (firstI == m_trainInstances.classIndex()) {
        p1++;
      }
      else if (secondI == m_trainInstances.classIndex()) {
        p2++;
      } else {
        double diff;
        if (firstI == secondI) {
          double diff = difference(firstI, first.valueSparse(p1), second.valueSparse(p2));
          

          p1++;p2++;
        } else if (firstI > secondI) {
          double diff = difference(secondI, 0.0D, second.valueSparse(p2));
          
          p2++;
        } else {
          diff = difference(firstI, first.valueSparse(p1), 0.0D);
          
          p1++;
        }
        
        distance += diff;
      }
    }
    
    return distance;
  }
  







  private void updateWeightsNumericClass(int instNum)
  {
    int[] tempSorted = null;
    double[] tempDist = null;
    double distNorm = 1.0D;
    

    Instance inst = m_trainInstances.instance(instNum);
    

    if (m_weightByDistance) {
      tempDist = new double[m_stored[0]];
      
      int j = 0; for (distNorm = 0.0D; j < m_stored[0]; j++)
      {
        tempDist[j] = m_karray[0][j][0];
        
        distNorm += m_weightsByRank[j];
      }
      
      tempSorted = Utils.sort(tempDist);
    }
    
    for (int i = 0; i < m_stored[0]; i++) {
      double temp;
      if (m_weightByDistance) {
        double temp = difference(m_classIndex, inst.value(m_classIndex), m_trainInstances.instance((int)m_karray[0][tempSorted[i]][1]).value(m_classIndex));
        



        temp *= m_weightsByRank[i] / distNorm;
      }
      else {
        temp = difference(m_classIndex, inst.value(m_classIndex), m_trainInstances.instance((int)m_karray[0][i][1]).value(m_classIndex));
        



        temp *= 1.0D / m_stored[0];
      }
      
      m_ndc += temp;
      

      Instance cmp = m_weightByDistance ? m_trainInstances.instance((int)m_karray[0][tempSorted[i]][1]) : m_trainInstances.instance((int)m_karray[0][i][1]);
      


      double temp_diffP_diffA_givNearest = difference(m_classIndex, inst.value(m_classIndex), cmp.value(m_classIndex));
      


      int p1 = 0;int p2 = 0;
      while ((p1 < inst.numValues()) || (p2 < cmp.numValues())) { int firstI;
        int firstI; if (p1 >= inst.numValues()) {
          firstI = m_trainInstances.numAttributes();
        } else
          firstI = inst.index(p1);
        int secondI;
        int secondI; if (p2 >= cmp.numValues()) {
          secondI = m_trainInstances.numAttributes();
        } else {
          secondI = cmp.index(p2);
        }
        if (firstI == m_trainInstances.classIndex()) {
          p1++;
        }
        else if (secondI == m_trainInstances.classIndex()) {
          p2++;
        } else {
          temp = 0.0D;
          double temp2 = 0.0D;
          int j;
          if (firstI == secondI) {
            int j = firstI;
            temp = difference(j, inst.valueSparse(p1), cmp.valueSparse(p2));
            p1++;p2++;
          } else if (firstI > secondI) {
            int j = secondI;
            temp = difference(j, 0.0D, cmp.valueSparse(p2));
            p2++;
          } else {
            j = firstI;
            temp = difference(j, inst.valueSparse(p1), 0.0D);
            p1++;
          }
          
          temp2 = temp_diffP_diffA_givNearest * temp;
          

          if (m_weightByDistance) {
            temp2 *= m_weightsByRank[i] / distNorm;
          }
          else {
            temp2 *= 1.0D / m_stored[0];
          }
          
          m_ndcda[j] += temp2;
          

          if (m_weightByDistance) {
            temp *= m_weightsByRank[i] / distNorm;
          }
          else {
            temp *= 1.0D / m_stored[0];
          }
          
          m_nda[j] += temp;
        }
      }
    }
  }
  






  private void updateWeightsDiscreteClass(int instNum)
  {
    double w_norm = 1.0D;
    
    int[] tempSortedClass = null;
    double distNormClass = 1.0D;
    
    int[][] tempSortedAtt = (int[][])null;
    double[] distNormAtt = null;
    


    Instance inst = m_trainInstances.instance(instNum);
    

    int cl = (int)m_trainInstances.instance(instNum).value(m_classIndex);
    

    if (m_weightByDistance)
    {

      double[] tempDistClass = new double[m_stored[cl]];
      
      int j = 0; for (distNormClass = 0.0D; j < m_stored[cl]; j++)
      {
        tempDistClass[j] = m_karray[cl][j][0];
        
        distNormClass += m_weightsByRank[j];
      }
      
      tempSortedClass = Utils.sort(tempDistClass);
      
      tempSortedAtt = new int[m_numClasses][1];
      distNormAtt = new double[m_numClasses];
      
      for (int k = 0; k < m_numClasses; k++) {
        if (k != cl)
        {

          double[] tempDistAtt = new double[m_stored[k]];
          
          j = 0; for (distNormAtt[k] = 0.0D; j < m_stored[k]; j++)
          {
            tempDistAtt[j] = m_karray[k][j][0];
            
            distNormAtt[k] += m_weightsByRank[j];
          }
          
          tempSortedAtt[k] = Utils.sort(tempDistAtt);
        }
      }
    }
    
    if (m_numClasses > 2)
    {

      w_norm = 1.0D - m_classProbs[cl];
    }
    

    int j = 0; for (double temp_diff = 0.0D; j < m_stored[cl]; j++)
    {
      Instance cmp = m_weightByDistance ? m_trainInstances.instance((int)m_karray[cl][tempSortedClass[j]][1]) : m_trainInstances.instance((int)m_karray[cl][j][1]);
      



      int p1 = 0;int p2 = 0;
      while ((p1 < inst.numValues()) || (p2 < cmp.numValues())) { int firstI;
        int firstI; if (p1 >= inst.numValues()) {
          firstI = m_trainInstances.numAttributes();
        } else
          firstI = inst.index(p1);
        int secondI;
        int secondI; if (p2 >= cmp.numValues()) {
          secondI = m_trainInstances.numAttributes();
        } else {
          secondI = cmp.index(p2);
        }
        if (firstI == m_trainInstances.classIndex()) {
          p1++;
        }
        else if (secondI == m_trainInstances.classIndex()) {
          p2++;
        } else { int i;
          if (firstI == secondI) {
            int i = firstI;
            temp_diff = difference(i, inst.valueSparse(p1), cmp.valueSparse(p2));
            
            p1++;p2++;
          } else if (firstI > secondI) {
            int i = secondI;
            temp_diff = difference(i, 0.0D, cmp.valueSparse(p2));
            p2++;
          } else {
            i = firstI;
            temp_diff = difference(i, inst.valueSparse(p1), 0.0D);
            p1++;
          }
          
          if (m_weightByDistance) {
            temp_diff *= m_weightsByRank[j] / distNormClass;

          }
          else if (m_stored[cl] > 0) {
            temp_diff /= m_stored[cl];
          }
          
          m_weights[i] -= temp_diff;
        }
      }
    }
    


    temp_diff = 0.0D;
    
    for (int k = 0; k < m_numClasses; k++) {
      if (k != cl)
      {
        for (j = 0; j < m_stored[k]; j++)
        {
          Instance cmp = m_weightByDistance ? m_trainInstances.instance((int)m_karray[k][tempSortedAtt[k][j]][1]) : m_trainInstances.instance((int)m_karray[k][j][1]);
          



          int p1 = 0;int p2 = 0;
          while ((p1 < inst.numValues()) || (p2 < cmp.numValues())) { int firstI;
            int firstI; if (p1 >= inst.numValues()) {
              firstI = m_trainInstances.numAttributes();
            } else
              firstI = inst.index(p1);
            int secondI;
            int secondI; if (p2 >= cmp.numValues()) {
              secondI = m_trainInstances.numAttributes();
            } else {
              secondI = cmp.index(p2);
            }
            if (firstI == m_trainInstances.classIndex()) {
              p1++;
            }
            else if (secondI == m_trainInstances.classIndex()) {
              p2++;
            } else { int i;
              if (firstI == secondI) {
                int i = firstI;
                temp_diff = difference(i, inst.valueSparse(p1), cmp.valueSparse(p2));
                
                p1++;p2++;
              } else if (firstI > secondI) {
                int i = secondI;
                temp_diff = difference(i, 0.0D, cmp.valueSparse(p2));
                p2++;
              } else {
                i = firstI;
                temp_diff = difference(i, inst.valueSparse(p1), 0.0D);
                p1++;
              }
              
              if (m_weightByDistance) {
                temp_diff *= m_weightsByRank[j] / distNormAtt[k];


              }
              else if (m_stored[k] > 0) {
                temp_diff /= m_stored[k];
              }
              
              if (m_numClasses > 2) {
                m_weights[i] += m_classProbs[k] / w_norm * temp_diff;
              } else {
                m_weights[i] += temp_diff;
              }
            }
          }
        }
      }
    }
  }
  









  private void findKHitMiss(int instNum)
  {
    double temp_diff = 0.0D;
    Instance thisInst = m_trainInstances.instance(instNum);
    
    for (int i = 0; i < m_numInstances; i++) {
      if (i != instNum) {
        Instance cmpInst = m_trainInstances.instance(i);
        temp_diff = distance(cmpInst, thisInst);
        int cl;
        int cl;
        if (m_numericClass) {
          cl = 0;
        }
        else {
          cl = (int)m_trainInstances.instance(i).value(m_classIndex);
        }
        

        if (m_stored[cl] < m_Knn) {
          m_karray[cl][m_stored[cl]][0] = temp_diff;
          m_karray[cl][m_stored[cl]][1] = i;
          m_stored[cl] += 1;
          

          int j = 0; for (double ww = -1.0D; j < m_stored[cl]; j++) {
            if (m_karray[cl][j][0] > ww) {
              ww = m_karray[cl][j][0];
              m_index[cl] = j;
            }
          }
          
          m_worst[cl] = ww;




        }
        else if (temp_diff < m_karray[cl][m_index[cl]][0]) {
          m_karray[cl][m_index[cl]][0] = temp_diff;
          m_karray[cl][m_index[cl]][1] = i;
          
          int j = 0; for (double ww = -1.0D; j < m_stored[cl]; j++) {
            if (m_karray[cl][j][0] > ww) {
              ww = m_karray[cl][j][0];
              m_index[cl] = j;
            }
          }
          
          m_worst[cl] = ww;
        }
      }
    }
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11219 $");
  }
  


  public int[] postProcess(int[] attributeSet)
  {
    m_trainInstances = new Instances(m_trainInstances, 0);
    
    return attributeSet;
  }
  








  public static void main(String[] args)
  {
    runEvaluator(new ReliefFAttributeEval(), args);
  }
}
