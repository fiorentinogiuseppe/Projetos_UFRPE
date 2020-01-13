package weka.classifiers.rules;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
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








































































public class NNge
  extends Classifier
  implements UpdateableClassifier, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 4084742275553788972L;
  private Instances m_Train;
  private Exemplar m_Exemplars;
  private Exemplar[] m_ExemplarsByClass;
  double[] m_MinArray;
  double[] m_MaxArray;
  
  public NNge() {}
  
  public String globalInfo()
  {
    return "Nearest-neighbor-like algorithm using non-nested generalized exemplars (which are hyperrectangles that can be viewed as if-then rules). For more information, see \n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MASTERSTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Brent Martin");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.TITLE, "Instance-Based learning: Nearest Neighbor With Generalization");
    result.setValue(TechnicalInformation.Field.SCHOOL, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.UNPUBLISHED);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Sylvain Roy");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.TITLE, "Nearest Neighbor With Generalization");
    additional.setValue(TechnicalInformation.Field.SCHOOL, "University of Canterbury");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Christchurch, New Zealand");
    
    return result;
  }
  



  private class Exemplar
    extends Instances
  {
    static final long serialVersionUID = 3960180128928697216L;
    


    private Exemplar previous = null;
    private Exemplar next = null;
    

    private Exemplar previousWithClass = null;
    private Exemplar nextWithClass = null;
    

    private NNge m_NNge;
    

    private double m_ClassValue;
    

    private int m_PositiveCount = 1;
    

    private int m_NegativeCount = 0;
    

    private double[] m_MaxBorder;
    

    private double[] m_MinBorder;
    

    private boolean[][] m_Range;
    

    private double[] m_PreMaxBorder = null;
    private double[] m_PreMinBorder = null;
    private boolean[][] m_PreRange = (boolean[][])null;
    private Instance m_PreInst = null;
    









    private Exemplar(NNge nnge, Instances inst, int size, double classV)
    {
      super(size);
      m_NNge = nnge;
      m_ClassValue = classV;
      m_MinBorder = new double[numAttributes()];
      m_MaxBorder = new double[numAttributes()];
      m_Range = new boolean[numAttributes()][];
      for (int i = 0; i < numAttributes(); i++) {
        if (attribute(i).isNumeric()) {
          m_MinBorder[i] = Double.POSITIVE_INFINITY;
          m_MaxBorder[i] = Double.NEGATIVE_INFINITY;
          m_Range[i] = null;
        } else {
          m_MinBorder[i] = NaN.0D;
          m_MaxBorder[i] = NaN.0D;
          m_Range[i] = new boolean[attribute(i).numValues() + 1];
          for (int j = 0; j < attribute(i).numValues() + 1; j++) {
            m_Range[i][j] = 0;
          }
        }
      }
    }
    






    private void generalise(Instance inst)
      throws Exception
    {
      if (m_ClassValue != inst.classValue()) {
        throw new Exception("Exemplar.generalise : Incompatible instance's class.");
      }
      add(inst);
      

      for (int i = 0; i < numAttributes(); i++)
      {
        if (inst.isMissing(i)) {
          throw new Exception("Exemplar.generalise : Generalisation with missing feature impossible.");
        }
        if (i != classIndex())
        {

          if (attribute(i).isNumeric()) {
            if (m_MaxBorder[i] < inst.value(i))
              m_MaxBorder[i] = inst.value(i);
            if (inst.value(i) < m_MinBorder[i]) {
              m_MinBorder[i] = inst.value(i);
            }
          } else {
            m_Range[i][((int)inst.value(i))] = 1;
          }
        }
      }
    }
    







    private void preGeneralise(Instance inst)
      throws Exception
    {
      if (m_ClassValue != inst.classValue()) {
        throw new Exception("Exemplar.preGeneralise : Incompatible instance's class.");
      }
      m_PreInst = inst;
      

      m_PreRange = new boolean[numAttributes()][];
      m_PreMinBorder = new double[numAttributes()];
      m_PreMaxBorder = new double[numAttributes()];
      for (int i = 0; i < numAttributes(); i++) {
        if (attribute(i).isNumeric()) {
          m_PreMinBorder[i] = m_MinBorder[i];
          m_PreMaxBorder[i] = m_MaxBorder[i];
        } else {
          m_PreRange[i] = new boolean[attribute(i).numValues() + 1];
          for (int j = 0; j < attribute(i).numValues() + 1; j++) {
            m_PreRange[i][j] = m_Range[i][j];
          }
        }
      }
      

      for (int i = 0; i < numAttributes(); i++) {
        if (inst.isMissing(i))
          throw new Exception("Exemplar.preGeneralise : Generalisation with missing feature impossible.");
        if (i != classIndex())
        {
          if (attribute(i).isNumeric()) {
            if (m_MaxBorder[i] < inst.value(i))
              m_MaxBorder[i] = inst.value(i);
            if (inst.value(i) < m_MinBorder[i])
              m_MinBorder[i] = inst.value(i);
          } else {
            m_Range[i][((int)inst.value(i))] = 1;
          }
        }
      }
    }
    




    private void validateGeneralisation()
      throws Exception
    {
      if (m_PreInst == null) {
        throw new Exception("Exemplar.validateGeneralisation : validateGeneralisation called without previous call to preGeneralise!");
      }
      add(m_PreInst);
      m_PreRange = ((boolean[][])null);
      m_PreMinBorder = null;
      m_PreMaxBorder = null;
    }
    





    private void cancelGeneralisation()
      throws Exception
    {
      if (m_PreInst == null) {
        throw new Exception("Exemplar.cancelGeneralisation : cancelGeneralisation called without previous call to preGeneralise!");
      }
      m_PreInst = null;
      m_Range = m_PreRange;
      m_MinBorder = m_PreMinBorder;
      m_MaxBorder = m_PreMaxBorder;
      m_PreRange = ((boolean[][])null);
      m_PreMinBorder = null;
      m_PreMaxBorder = null;
    }
    







    private boolean holds(Instance inst)
    {
      if (numInstances() == 0) {
        return false;
      }
      for (int i = 0; i < numAttributes(); i++) {
        if ((i != classIndex()) && (!holds(i, inst.value(i))))
          return false;
      }
      return true;
    }
    








    private boolean holds(int attrIndex, double value)
    {
      if (numAttributes() == 0) {
        return false;
      }
      if (attribute(attrIndex).isNumeric()) {
        return (m_MinBorder[attrIndex] <= value) && (value <= m_MaxBorder[attrIndex]);
      }
      return m_Range[attrIndex][((int)value)];
    }
    








    private boolean overlaps(Exemplar ex)
    {
      if ((ex.isEmpty()) || (isEmpty())) {
        return false;
      }
      for (int i = 0; i < numAttributes(); i++)
      {
        if (i != classIndex())
        {

          if ((attribute(i).isNumeric()) && ((m_MaxBorder[i] < m_MinBorder[i]) || (m_MinBorder[i] > m_MaxBorder[i])))
          {
            return false;
          }
          if (attribute(i).isNominal()) {
            boolean in = false;
            for (int j = 0; j < attribute(i).numValues() + 1; j++) {
              if ((m_Range[i][j] != 0) && (m_Range[i][j] != 0)) {
                in = true;
                break;
              }
            }
            if (!in) return false;
          }
        } }
      return true;
    }
    









    private double attrDistance(Instance inst, int attrIndex)
    {
      if (inst.isMissing(attrIndex)) {
        return 0.0D;
      }
      
      if (attribute(attrIndex).isNumeric())
      {
        double norm = m_NNge.m_MaxArray[attrIndex] - m_NNge.m_MinArray[attrIndex];
        if (norm <= 0.0D) {
          norm = 1.0D;
        }
        if (m_MaxBorder[attrIndex] < inst.value(attrIndex))
          return (inst.value(attrIndex) - m_MaxBorder[attrIndex]) / norm;
        if (inst.value(attrIndex) < m_MinBorder[attrIndex]) {
          return (m_MinBorder[attrIndex] - inst.value(attrIndex)) / norm;
        }
        return 0.0D;
      }
      


      if (holds(attrIndex, inst.value(attrIndex))) {
        return 0.0D;
      }
      return 1.0D;
    }
    









    private double squaredDistance(Instance inst)
    {
      double sum = 0.0D;
      int numNotMissingAttr = 0;
      for (int i = 0; i < inst.numAttributes(); i++)
      {
        if (i != classIndex())
        {

          double term = m_NNge.attrWeight(i) * attrDistance(inst, i);
          term *= term;
          sum += term;
          
          if (!inst.isMissing(i)) {
            numNotMissingAttr++;
          }
        }
      }
      if (numNotMissingAttr == 0) {
        return 0.0D;
      }
      return sum / (numNotMissingAttr * numNotMissingAttr);
    }
    






    private double weight()
    {
      return (m_PositiveCount + m_NegativeCount) / m_PositiveCount;
    }
    





    private double classValue()
    {
      return m_ClassValue;
    }
    






    private double getMinBorder(int attrIndex)
      throws Exception
    {
      if (!attribute(attrIndex).isNumeric())
        throw new Exception("Exception.getMinBorder : not numeric attribute !");
      if (numInstances() == 0)
        throw new Exception("Exception.getMinBorder : empty Exemplar !");
      return m_MinBorder[attrIndex];
    }
    







    private double getMaxBorder(int attrIndex)
      throws Exception
    {
      if (!attribute(attrIndex).isNumeric())
        throw new Exception("Exception.getMaxBorder : not numeric attribute !");
      if (numInstances() == 0)
        throw new Exception("Exception.getMaxBorder : empty Exemplar !");
      return m_MaxBorder[attrIndex];
    }
    





    private int getPositiveCount()
    {
      return m_PositiveCount;
    }
    





    private int getNegativeCount()
    {
      return m_NegativeCount;
    }
    





    private void setPositiveCount(int value)
    {
      m_PositiveCount = value;
    }
    





    private void setNegativeCount(int value)
    {
      m_NegativeCount = value;
    }
    



    private void incrPositiveCount()
    {
      m_PositiveCount += 1;
    }
    



    private void incrNegativeCount()
    {
      m_NegativeCount += 1;
    }
    





    private boolean isEmpty()
    {
      return numInstances() == 0;
    }
    






    private String toString2()
    {
      Enumeration enu = null;
      String s = "Exemplar[";
      if (numInstances() == 0) {
        return s + "Empty]";
      }
      s = s + "{";
      enu = enumerateInstances();
      while (enu.hasMoreElements()) {
        s = s + "<" + enu.nextElement().toString() + "> ";
      }
      s = s.substring(0, s.length() - 1);
      s = s + "} {" + toRules() + "} p=" + m_PositiveCount + " n=" + m_NegativeCount + "]";
      return s;
    }
    






    private String toRules()
    {
      if (numInstances() == 0) {
        return "No Rules (Empty Exemplar)";
      }
      String s = "";String sep = "";
      
      for (int i = 0; i < numAttributes(); i++)
      {
        if (i != classIndex())
        {

          if (attribute(i).isNumeric()) {
            if (m_MaxBorder[i] != m_MinBorder[i]) {
              s = s + sep + m_MinBorder[i] + "<=" + attribute(i).name() + "<=" + m_MaxBorder[i];
            } else {
              s = s + sep + attribute(i).name() + "=" + m_MaxBorder[i];
            }
            sep = " ^ ";
          }
          else {
            s = s + sep + attribute(i).name() + " in {";
            String virg = "";
            for (int j = 0; j < attribute(i).numValues() + 1; j++) {
              if (m_Range[i][j] != 0) {
                s = s + virg;
                if (j == attribute(i).numValues()) {
                  s = s + "?";
                } else
                  s = s + attribute(i).value(j);
                virg = ",";
              }
            }
            s = s + "}";
            sep = " ^ ";
          } }
      }
      s = s + "  (" + numInstances() + ")";
      return s;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  


















  private int m_NumAttemptsOfGene = 5;
  

  private int m_NumFoldersMI = 5;
  

  private double[] m_MissingVector;
  

  private int[][][] m_MI_NumAttrClassInter;
  

  private int[][] m_MI_NumAttrInter;
  

  private double[] m_MI_MaxArray;
  

  private double[] m_MI_MinArray;
  
  private int[][][] m_MI_NumAttrClassValue;
  
  private int[][] m_MI_NumAttrValue;
  
  private int[] m_MI_NumClass;
  
  private int m_MI_NumInst;
  
  private double[] m_MI;
  

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
    


    m_Train = new Instances(data, 0);
    m_Exemplars = null;
    m_ExemplarsByClass = new Exemplar[m_Train.numClasses()];
    for (int i = 0; i < m_Train.numClasses(); i++) {
      m_ExemplarsByClass[i] = null;
    }
    m_MaxArray = new double[m_Train.numAttributes()];
    m_MinArray = new double[m_Train.numAttributes()];
    for (int i = 0; i < m_Train.numAttributes(); i++) {
      m_MinArray[i] = Double.POSITIVE_INFINITY;
      m_MaxArray[i] = Double.NEGATIVE_INFINITY;
    }
    
    m_MI_MinArray = new double[data.numAttributes()];
    m_MI_MaxArray = new double[data.numAttributes()];
    m_MI_NumAttrClassInter = new int[data.numAttributes()][][];
    m_MI_NumAttrInter = new int[data.numAttributes()][];
    m_MI_NumAttrClassValue = new int[data.numAttributes()][][];
    m_MI_NumAttrValue = new int[data.numAttributes()][];
    m_MI_NumClass = new int[data.numClasses()];
    m_MI = new double[data.numAttributes()];
    m_MI_NumInst = 0;
    for (int cclass = 0; cclass < data.numClasses(); cclass++)
      m_MI_NumClass[cclass] = 0;
    for (int attrIndex = 0; attrIndex < data.numAttributes(); attrIndex++)
    {
      if (attrIndex != data.classIndex())
      {

        double tmp288_285 = NaN.0D;m_MI_MinArray[attrIndex] = tmp288_285;m_MI_MaxArray[attrIndex] = tmp288_285;
        m_MI[attrIndex] = NaN.0D;
        
        if (data.attribute(attrIndex).isNumeric()) {
          m_MI_NumAttrInter[attrIndex] = new int[m_NumFoldersMI];
          for (int inter = 0; inter < m_NumFoldersMI; inter++) {
            m_MI_NumAttrInter[attrIndex][inter] = 0;
          }
        } else {
          m_MI_NumAttrValue[attrIndex] = new int[data.attribute(attrIndex).numValues() + 1];
          for (int attrValue = 0; attrValue < data.attribute(attrIndex).numValues() + 1; attrValue++) {
            m_MI_NumAttrValue[attrIndex][attrValue] = 0;
          }
        }
        
        m_MI_NumAttrClassInter[attrIndex] = new int[data.numClasses()][];
        m_MI_NumAttrClassValue[attrIndex] = new int[data.numClasses()][];
        
        for (int cclass = 0; cclass < data.numClasses(); cclass++) {
          if (data.attribute(attrIndex).isNumeric()) {
            m_MI_NumAttrClassInter[attrIndex][cclass] = new int[m_NumFoldersMI];
            for (int inter = 0; inter < m_NumFoldersMI; inter++) {
              m_MI_NumAttrClassInter[attrIndex][cclass][inter] = 0;
            }
          } else if (data.attribute(attrIndex).isNominal()) {
            m_MI_NumAttrClassValue[attrIndex][cclass] = new int[data.attribute(attrIndex).numValues() + 1];
            for (int attrValue = 0; attrValue < data.attribute(attrIndex).numValues() + 1; attrValue++)
              m_MI_NumAttrClassValue[attrIndex][cclass][attrValue] = 0;
          }
        }
      }
    }
    m_MissingVector = new double[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i == data.classIndex()) {
        m_MissingVector[i] = NaN.0D;
      } else {
        m_MissingVector[i] = data.attribute(i).numValues();
      }
    }
    

    Enumeration enu = data.enumerateInstances();
    while (enu.hasMoreElements()) {
      update((Instance)enu.nextElement());
    }
  }
  









  public double classifyInstance(Instance instance)
    throws Exception
  {
    if (!m_Train.equalHeaders(instance.dataset())) {
      throw new Exception("NNge.classifyInstance : Incompatible instance types !");
    }
    
    Exemplar matched = nearestExemplar(instance);
    if (matched == null) {
      throw new Exception("NNge.classifyInstance : NNge hasn't been trained !");
    }
    return matched.classValue();
  }
  







  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (!m_Train.equalHeaders(instance.dataset())) {
      throw new Exception("Incompatible instance types");
    }
    update(instance);
  }
  











  private void update(Instance instance)
    throws Exception
  {
    if (instance.classIsMissing()) {
      return;
    }
    
    instance.replaceMissingValues(m_MissingVector);
    m_Train.add(instance);
    

    updateMinMax(instance);
    

    updateMI(instance);
    

    Exemplar nearest = nearestExemplar(instance);
    

    if (nearest == null) {
      Exemplar newEx = new Exemplar(this, m_Train, 10, instance.classValue(), null);
      newEx.generalise(instance);
      initWeight(newEx);
      addExemplar(newEx);
      return;
    }
    adjust(instance, nearest);
    

    generalise(instance);
  }
  







  private Exemplar nearestExemplar(Instance inst)
  {
    if (m_Exemplars == null)
      return null;
    Exemplar cur = m_Exemplars;Exemplar nearest = m_Exemplars;
    double smallestDist = cur.squaredDistance(inst);
    while (next != null) {
      cur = next;
      double dist = cur.squaredDistance(inst);
      if (dist < smallestDist) {
        smallestDist = dist;
        nearest = cur;
      }
    }
    return nearest;
  }
  








  private Exemplar nearestExemplar(Instance inst, double c)
  {
    if (m_ExemplarsByClass[((int)c)] == null)
      return null;
    Exemplar cur = m_ExemplarsByClass[((int)c)];Exemplar nearest = m_ExemplarsByClass[((int)c)];
    double smallestDist = cur.squaredDistance(inst);
    while (nextWithClass != null) {
      cur = nextWithClass;
      double dist = cur.squaredDistance(inst);
      if (dist < smallestDist) {
        smallestDist = dist;
        nearest = cur;
      }
    }
    return nearest;
  }
  







  private void generalise(Instance newInst)
    throws Exception
  {
    Exemplar first = m_ExemplarsByClass[((int)newInst.classValue())];
    int n = 0;
    

    while ((n < m_NumAttemptsOfGene) && (first != null))
    {

      Exemplar closest = first;Exemplar cur = first;
      double smallestDist = first.squaredDistance(newInst);
      while (nextWithClass != null) {
        cur = nextWithClass;
        double dist = cur.squaredDistance(newInst);
        if (dist < smallestDist) {
          smallestDist = dist;
          closest = cur;
        }
      }
      

      if (closest == first)
        first = nextWithClass;
      removeExemplar(closest);
      

      closest.preGeneralise(newInst);
      if (!detectOverlapping(closest)) {
        closest.validateGeneralisation();
        addExemplar(closest);
        return;
      }
      

      closest.cancelGeneralisation();
      addExemplar(closest);
      
      n++;
    }
    

    Exemplar newEx = new Exemplar(this, m_Train, 5, newInst.classValue(), null);
    newEx.generalise(newInst);
    initWeight(newEx);
    addExemplar(newEx);
  }
  








  private void adjust(Instance newInst, Exemplar predictedExemplar)
    throws Exception
  {
    if (newInst.classValue() == predictedExemplar.classValue()) {
      predictedExemplar.incrPositiveCount();
    }
    else {
      predictedExemplar.incrNegativeCount();
      

      if (predictedExemplar.holds(newInst)) {
        prune(predictedExemplar, newInst);
      }
    }
  }
  








  private void prune(Exemplar predictedExemplar, Instance newInst)
    throws Exception
  {
    removeExemplar(predictedExemplar);
    

    int numAttr = -1;int nomAttr = -1;
    double smallestDelta = Double.POSITIVE_INFINITY;
    int biggest_N_Nom = -1;int biggest_N_Num = -1;
    for (int i = 0; i < m_Train.numAttributes(); i++)
    {
      if (i != m_Train.classIndex())
      {


        if (m_Train.attribute(i).isNumeric())
        {

          double norm = m_MaxArray[i] - m_MinArray[i];
          double delta; double delta; if (norm != 0.0D) {
            delta = Math.min(predictedExemplar.getMaxBorder(i) - newInst.value(i), newInst.value(i) - predictedExemplar.getMinBorder(i)) / norm;
          }
          else {
            delta = Double.POSITIVE_INFINITY;
          }
          
          int m;
          int n = m = 0;
          Enumeration enu = predictedExemplar.enumerateInstances();
          while (enu.hasMoreElements()) {
            Instance ins = (Instance)enu.nextElement();
            if (ins.value(i) < newInst.value(i)) {
              n++;
            } else if (ins.value(i) > newInst.value(i))
              m++;
          }
          n = Math.max(n, m);
          
          if (delta < smallestDelta) {
            smallestDelta = delta;
            biggest_N_Num = n;
            numAttr = i;
          } else if ((delta == smallestDelta) && (n > biggest_N_Num)) {
            biggest_N_Num = n;
            numAttr = i;
          }
          

        }
        else
        {
          Enumeration enu = predictedExemplar.enumerateInstances();
          int n = 0;
          while (enu.hasMoreElements()) {
            if (((Instance)enu.nextElement()).value(i) != newInst.value(i))
              n++;
          }
          if (n > biggest_N_Nom) {
            biggest_N_Nom = n;
            nomAttr = i;
          }
        }
      }
    }
    int attrToCut;
    int attrToCut;
    if ((numAttr == -1) && (nomAttr == -1)) {
      attrToCut = 0; } else { int attrToCut;
      if (numAttr == -1) {
        attrToCut = nomAttr; } else { int attrToCut;
        if (nomAttr == -1) {
          attrToCut = numAttr;
        } else { int attrToCut;
          if (biggest_N_Nom > biggest_N_Num) {
            attrToCut = nomAttr;
          } else {
            attrToCut = numAttr;
          }
        }
      }
    }
    
    Exemplar a = new Exemplar(this, m_Train, 10, predictedExemplar.classValue(), null);
    Exemplar b = new Exemplar(this, m_Train, 10, predictedExemplar.classValue(), null);
    LinkedList leftAlone = new LinkedList();
    Enumeration enu = predictedExemplar.enumerateInstances();
    if (m_Train.attribute(attrToCut).isNumeric()) {
      while (enu.hasMoreElements()) {
        Instance curInst = (Instance)enu.nextElement();
        if (curInst.value(attrToCut) > newInst.value(attrToCut)) {
          a.generalise(curInst);
        } else if (curInst.value(attrToCut) < newInst.value(attrToCut)) {
          b.generalise(curInst);
        } else if (notEqualFeatures(curInst, newInst)) {
          leftAlone.add(curInst);
        }
      }
    }
    while (enu.hasMoreElements()) {
      Instance curInst = (Instance)enu.nextElement();
      if (curInst.value(attrToCut) != newInst.value(attrToCut)) {
        a.generalise(curInst);
      } else if (notEqualFeatures(curInst, newInst)) {
        leftAlone.add(curInst);
      }
    }
    


    while (leftAlone.size() != 0)
    {
      Instance alone = (Instance)leftAlone.removeFirst();
      a.preGeneralise(alone);
      if (!a.holds(newInst)) {
        a.validateGeneralisation();
      }
      else {
        a.cancelGeneralisation();
        b.preGeneralise(alone);
        if (!b.holds(newInst)) {
          b.validateGeneralisation();
        }
        else {
          b.cancelGeneralisation();
          Exemplar exem = new Exemplar(this, m_Train, 3, alone.classValue(), null);
          exem.generalise(alone);
          initWeight(exem);
          addExemplar(exem);
        }
      }
    }
    if (a.numInstances() != 0) {
      initWeight(a);
      addExemplar(a);
    }
    if (b.numInstances() != 0) {
      initWeight(b);
      addExemplar(b);
    }
  }
  








  private boolean notEqualFeatures(Instance inst1, Instance inst2)
  {
    for (int i = 0; i < m_Train.numAttributes(); i++) {
      if (i != m_Train.classIndex())
      {
        if (inst1.value(i) != inst2.value(i))
          return true; }
    }
    return false;
  }
  






  private boolean detectOverlapping(Exemplar ex)
  {
    Exemplar cur = m_Exemplars;
    while (cur != null) {
      if (ex.overlaps(cur)) {
        return true;
      }
      cur = next;
    }
    return false;
  }
  






  private void updateMinMax(Instance instance)
  {
    for (int j = 0; j < m_Train.numAttributes(); j++) {
      if ((m_Train.classIndex() != j) && (!m_Train.attribute(j).isNominal()))
      {
        if (instance.value(j) < m_MinArray[j])
          m_MinArray[j] = instance.value(j);
        if (instance.value(j) > m_MaxArray[j]) {
          m_MaxArray[j] = instance.value(j);
        }
      }
    }
  }
  






  private void updateMI(Instance inst)
    throws Exception
  {
    if (m_NumFoldersMI < 1) {
      throw new Exception("NNge.updateMI : incorrect number of folders ! Option I must be greater than 1.");
    }
    
    m_MI_NumClass[((int)inst.classValue())] += 1;
    m_MI_NumInst += 1;
    

    for (int attrIndex = 0; attrIndex < m_Train.numAttributes(); attrIndex++)
    {

      if (m_Train.classIndex() != attrIndex)
      {


        if (m_Train.attribute(attrIndex).isNumeric())
        {

          if ((Double.isNaN(m_MI_MaxArray[attrIndex])) || (Double.isNaN(m_MI_MinArray[attrIndex])) || (m_MI_MaxArray[attrIndex] < inst.value(attrIndex)) || (inst.value(attrIndex) < m_MI_MinArray[attrIndex]))
          {




            if (Double.isNaN(m_MI_MaxArray[attrIndex])) m_MI_MaxArray[attrIndex] = inst.value(attrIndex);
            if (Double.isNaN(m_MI_MinArray[attrIndex])) m_MI_MinArray[attrIndex] = inst.value(attrIndex);
            if (m_MI_MaxArray[attrIndex] < inst.value(attrIndex)) m_MI_MaxArray[attrIndex] = inst.value(attrIndex);
            if (m_MI_MinArray[attrIndex] > inst.value(attrIndex)) { m_MI_MinArray[attrIndex] = inst.value(attrIndex);
            }
            
            double delta = (m_MI_MaxArray[attrIndex] - m_MI_MinArray[attrIndex]) / m_NumFoldersMI;
            

            for (int inter = 0; inter < m_NumFoldersMI; inter++)
            {
              m_MI_NumAttrInter[attrIndex][inter] = 0;
              

              for (int cclass = 0; cclass < m_Train.numClasses(); cclass++)
              {
                m_MI_NumAttrClassInter[attrIndex][cclass][inter] = 0;
                

                Enumeration enu = m_Train.enumerateInstances();
                while (enu.hasMoreElements()) {
                  Instance cur = (Instance)enu.nextElement();
                  if ((m_MI_MinArray[attrIndex] + inter * delta <= cur.value(attrIndex)) && (cur.value(attrIndex) <= m_MI_MinArray[attrIndex] + (inter + 1) * delta) && (cur.classValue() == cclass))
                  {

                    m_MI_NumAttrInter[attrIndex][inter] += 1;
                    m_MI_NumAttrClassInter[attrIndex][cclass][inter] += 1;
                  }
                  
                }
                
              }
            }
          }
          else
          {
            double delta = (m_MI_MaxArray[attrIndex] - m_MI_MinArray[attrIndex]) / m_NumFoldersMI;
            

            for (int inter = 0; inter < m_NumFoldersMI; inter++)
            {
              if ((m_MI_MinArray[attrIndex] + inter * delta <= inst.value(attrIndex)) && (inst.value(attrIndex) <= m_MI_MinArray[attrIndex] + (inter + 1) * delta))
              {
                m_MI_NumAttrInter[attrIndex][inter] += 1;
                m_MI_NumAttrClassInter[attrIndex][((int)inst.classValue())][inter] += 1;
              }
            }
          }
          

          m_MI[attrIndex] = 0.0D;
          

          for (int inter = 0; inter < m_NumFoldersMI; inter++) {
            for (int cclass = 0; cclass < m_Train.numClasses(); cclass++) {
              double pXY = m_MI_NumAttrClassInter[attrIndex][cclass][inter] / m_MI_NumInst;
              double pX = m_MI_NumClass[cclass] / m_MI_NumInst;
              double pY = m_MI_NumAttrInter[attrIndex][inter] / m_MI_NumInst;
              
              if (pXY != 0.0D) {
                m_MI[attrIndex] += pXY * Utils.log2(pXY / (pX * pY));
              }
            }
          }
        }
        else if (m_Train.attribute(attrIndex).isNominal())
        {

          m_MI_NumAttrValue[attrIndex][((int)inst.value(attrIndex))] += 1;
          m_MI_NumAttrClassValue[attrIndex][((int)inst.classValue())][((int)inst.value(attrIndex))] += 1;
          

          m_MI[attrIndex] = 0.0D;
          

          for (int attrValue = 0; attrValue < m_Train.attribute(attrIndex).numValues() + 1; attrValue++) {
            for (int cclass = 0; cclass < m_Train.numClasses(); cclass++) {
              double pXY = m_MI_NumAttrClassValue[attrIndex][cclass][attrValue] / m_MI_NumInst;
              double pX = m_MI_NumClass[cclass] / m_MI_NumInst;
              double pY = m_MI_NumAttrValue[attrIndex][attrValue] / m_MI_NumInst;
              if (pXY != 0.0D) {
                m_MI[attrIndex] += pXY * Utils.log2(pXY / (pX * pY));
              }
            }
          }
        }
        else {
          throw new Exception("NNge.updateMI : Cannot deal with 'string attribute'.");
        }
      }
    }
  }
  





  private void initWeight(Exemplar ex)
  {
    int pos = 0;int neg = 0;int n = 0;
    Exemplar cur = m_Exemplars;
    if (cur == null) {
      ex.setPositiveCount(1);
      ex.setNegativeCount(0);
      return;
    }
    while (cur != null) {
      pos += cur.getPositiveCount();
      neg += cur.getNegativeCount();
      n++;
      cur = next;
    }
    ex.setPositiveCount(pos / n);
    ex.setNegativeCount(neg / n);
  }
  








  private void addExemplar(Exemplar ex)
  {
    next = m_Exemplars;
    if (m_Exemplars != null)
      m_Exemplars.previous = ex;
    previous = null;
    m_Exemplars = ex;
    

    nextWithClass = m_ExemplarsByClass[((int)ex.classValue())];
    if (m_ExemplarsByClass[((int)ex.classValue())] != null)
      m_ExemplarsByClass[((int)ex.classValue())].previousWithClass = ex;
    previousWithClass = null;
    m_ExemplarsByClass[((int)ex.classValue())] = ex;
  }
  











  private void removeExemplar(Exemplar ex)
  {
    if (m_Exemplars == ex) {
      m_Exemplars = next;
      if (m_Exemplars != null) {
        m_Exemplars.previous = null;
      }
    } else {
      previous.next = next;
      if (next != null) {
        next.previous = previous;
      }
    }
    next = Exemplar.access$2002(ex, null);
    

    if (m_ExemplarsByClass[((int)ex.classValue())] == ex) {
      m_ExemplarsByClass[((int)ex.classValue())] = nextWithClass;
      if (m_ExemplarsByClass[((int)ex.classValue())] != null) {
        m_ExemplarsByClass[((int)ex.classValue())].previousWithClass = null;
      }
    } else {
      previousWithClass.nextWithClass = nextWithClass;
      if (nextWithClass != null) {
        nextWithClass.previousWithClass = previousWithClass;
      }
    }
    nextWithClass = Exemplar.access$2102(ex, null);
  }
  






  private double attrWeight(int index)
  {
    return m_MI[index];
  }
  







  public String toString()
  {
    Exemplar cur = m_Exemplars;
    

    if (m_MinArray == null) {
      return "No classifier built";
    }
    int[] nbHypClass = new int[m_Train.numClasses()];
    int[] nbSingleClass = new int[m_Train.numClasses()];
    for (int i = 0; i < nbHypClass.length; i++) {
      nbHypClass[i] = 0;
      nbSingleClass[i] = 0;
    }
    int nbHyp = 0;int nbSingle = 0;
    
    String s = "\nNNGE classifier\n\nRules generated :\n";
    
    while (cur != null) {
      s = s + "\tclass " + m_Train.attribute(m_Train.classIndex()).value((int)cur.classValue()) + " IF : ";
      s = s + cur.toRules() + "\n";
      nbHyp++;
      nbHypClass[((int)cur.classValue())] += 1;
      if (cur.numInstances() == 1) {
        nbSingle++;
        nbSingleClass[((int)cur.classValue())] += 1;
      }
      cur = next;
    }
    s = s + "\nStat :\n";
    for (i = 0; i < nbHypClass.length; i++) {
      s = s + "\tclass " + m_Train.attribute(m_Train.classIndex()).value(i) + " : " + Integer.toString(nbHypClass[i]) + " exemplar(s) including " + Integer.toString(nbHypClass[i] - nbSingleClass[i]) + " Hyperrectangle(s) and " + Integer.toString(nbSingleClass[i]) + " Single(s).\n";
    }
    


    s = s + "\n\tTotal : " + Integer.toString(nbHyp) + " exemplars(s) including " + Integer.toString(nbHyp - nbSingle) + " Hyperrectangle(s) and " + Integer.toString(nbSingle) + " Single(s).\n";
    


    s = s + "\n";
    
    s = s + "\tFeature weights : ";
    
    String space = "[";
    for (int ii = 0; ii < m_Train.numAttributes(); ii++) {
      if (ii != m_Train.classIndex()) {
        s = s + space + Double.toString(attrWeight(ii));
        space = " ";
      }
    }
    s = s + "]";
    s = s + "\n\n";
    return s;
  }
  










  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tNumber of attempts of generalisation.\n", "G", 1, "-G <value>"));
    



    newVector.addElement(new Option("\tNumber of folder for computing the mutual information.\n", "I", 1, "-I <value>"));
    




    return newVector.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    String str = Utils.getOption('G', options);
    if (str.length() != 0) {
      m_NumAttemptsOfGene = Integer.parseInt(str);
      if (m_NumAttemptsOfGene < 1)
        throw new Exception("NNge.setOptions : G option's value must be greater than 1.");
    } else {
      m_NumAttemptsOfGene = 5;
    }
    

    str = Utils.getOption('I', options);
    if (str.length() != 0) {
      m_NumFoldersMI = Integer.parseInt(str);
      if (m_NumFoldersMI < 1)
        throw new Exception("NNge.setOptions : I option's value must be greater than 1.");
    } else {
      m_NumFoldersMI = 5;
    }
  }
  






  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    options[(current++)] = "-G";options[(current++)] = ("" + m_NumAttemptsOfGene);
    options[(current++)] = "-I";options[(current++)] = ("" + m_NumFoldersMI);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String numAttemptsOfGeneOptionTipText()
  {
    return "Sets the number of attempts for generalization.";
  }
  




  public int getNumAttemptsOfGeneOption()
  {
    return m_NumAttemptsOfGene;
  }
  





  public void setNumAttemptsOfGeneOption(int newIntParameter)
  {
    m_NumAttemptsOfGene = newIntParameter;
  }
  




  public String numFoldersMIOptionTipText()
  {
    return "Sets the number of folder for mutual information.";
  }
  




  public int getNumFoldersMIOption()
  {
    return m_NumFoldersMI;
  }
  




  public void setNumFoldersMIOption(int newIntParameter)
  {
    m_NumFoldersMI = newIntParameter;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5529 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new NNge(), argv);
  }
}
