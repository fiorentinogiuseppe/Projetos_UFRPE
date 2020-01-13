package weka.attributeSelection;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
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
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;






























































































public class CfsSubsetEval
  extends ASEvaluation
  implements SubsetEvaluator, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 747878400813276317L;
  private Instances m_trainInstances;
  private Discretize m_disTransform;
  private int m_classIndex;
  private boolean m_isNumeric;
  private int m_numAttribs;
  private int m_numInstances;
  private boolean m_missingSeparate;
  private boolean m_locallyPredictive;
  private float[][] m_corr_matrix;
  private double[] m_std_devs;
  private double m_c_Threshold;
  
  public String globalInfo()
  {
    return "CfsSubsetEval :\n\nEvaluates the worth of a subset of attributes by considering the individual predictive ability of each feature along with the degree of redundancy between them.\n\nSubsets of features that are highly correlated with the class while having low intercorrelation are preferred.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "M. A. Hall");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Correlation-based Feature Subset Selection for Machine Learning");
    
    result.setValue(TechnicalInformation.Field.SCHOOL, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    return result;
  }
  


  public CfsSubsetEval()
  {
    resetOptions();
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    newVector.addElement(new Option("\tTreat missing values as a separate value.", "M", 0, "-M"));
    
    newVector.addElement(new Option("\tDon't include locally predictive attributes.", "L", 0, "-L"));
    

    return newVector.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    setMissingSeparate(Utils.getFlag('M', options));
    setLocallyPredictive(!Utils.getFlag('L', options));
  }
  





  public String locallyPredictiveTipText()
  {
    return "Identify locally predictive attributes. Iteratively adds attributes with the highest correlation with the class as long as there is not already an attribute in the subset that has a higher correlation with the attribute in question";
  }
  







  public void setLocallyPredictive(boolean b)
  {
    m_locallyPredictive = b;
  }
  




  public boolean getLocallyPredictive()
  {
    return m_locallyPredictive;
  }
  





  public String missingSeparateTipText()
  {
    return "Treat missing as a separate value. Otherwise, counts for missing values are distributed across other values in proportion to their frequency.";
  }
  






  public void setMissingSeparate(boolean b)
  {
    m_missingSeparate = b;
  }
  




  public boolean getMissingSeparate()
  {
    return m_missingSeparate;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    if (getMissingSeparate()) {
      options[(current++)] = "-M";
    }
    
    if (!getLocallyPredictive()) {
      options[(current++)] = "-L";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
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
    getCapabilities().testWithFail(data);
    
    m_trainInstances = new Instances(data);
    m_trainInstances.deleteWithMissingClass();
    m_classIndex = m_trainInstances.classIndex();
    m_numAttribs = m_trainInstances.numAttributes();
    m_numInstances = m_trainInstances.numInstances();
    m_isNumeric = m_trainInstances.attribute(m_classIndex).isNumeric();
    
    if (!m_isNumeric) {
      m_disTransform = new Discretize();
      m_disTransform.setUseBetterEncoding(true);
      m_disTransform.setInputFormat(m_trainInstances);
      m_trainInstances = Filter.useFilter(m_trainInstances, m_disTransform);
    }
    
    m_std_devs = new double[m_numAttribs];
    m_corr_matrix = new float[m_numAttribs][];
    for (int i = 0; i < m_numAttribs; i++) {
      m_corr_matrix[i] = new float[i + 1];
    }
    
    for (int i = 0; i < m_corr_matrix.length; i++) {
      m_corr_matrix[i][i] = 1.0F;
      m_std_devs[i] = 1.0D;
    }
    
    for (int i = 0; i < m_numAttribs; i++) {
      for (int j = 0; j < m_corr_matrix[i].length - 1; j++) {
        m_corr_matrix[i][j] = -999.0F;
      }
    }
  }
  







  public double evaluateSubset(BitSet subset)
    throws Exception
  {
    double num = 0.0D;
    double denom = 0.0D;
    


    for (int i = 0; i < m_numAttribs; i++) {
      if ((i != m_classIndex) && 
        (subset.get(i))) { int smaller;
        int smaller; int larger; if (i > m_classIndex) {
          int larger = i;
          smaller = m_classIndex;
        } else {
          smaller = i;
          larger = m_classIndex;
        }
        



        if (m_corr_matrix[larger][smaller] == -999.0F) {
          float corr = correlate(i, m_classIndex);
          m_corr_matrix[larger][smaller] = corr;
          num += m_std_devs[i] * corr;
        }
        else {
          num += m_std_devs[i] * m_corr_matrix[larger][smaller];
        }
      }
    }
    


    for (int i = 0; i < m_numAttribs; i++) {
      if ((i != m_classIndex) && 
        (subset.get(i))) {
        denom += 1.0D * m_std_devs[i] * m_std_devs[i];
        
        for (int j = 0; j < m_corr_matrix[i].length - 1; j++) {
          if (subset.get(j)) {
            if (m_corr_matrix[i][j] == -999.0F) {
              float corr = correlate(i, j);
              m_corr_matrix[i][j] = corr;
              denom += 2.0D * m_std_devs[i] * m_std_devs[j] * corr;
            }
            else {
              denom += 2.0D * m_std_devs[i] * m_std_devs[j] * m_corr_matrix[i][j];
            }
          }
        }
      }
    }
    


    if (denom < 0.0D) {
      denom *= -1.0D;
    }
    
    if (denom == 0.0D) {
      return 0.0D;
    }
    
    double merit = num / Math.sqrt(denom);
    
    if (merit < 0.0D) {
      merit *= -1.0D;
    }
    
    return merit;
  }
  
  private float correlate(int att1, int att2) {
    if (!m_isNumeric) {
      return (float)symmUncertCorr(att1, att2);
    }
    
    boolean att1_is_num = m_trainInstances.attribute(att1).isNumeric();
    boolean att2_is_num = m_trainInstances.attribute(att2).isNumeric();
    
    if ((att1_is_num) && (att2_is_num)) {
      return (float)num_num(att1, att2);
    }
    
    if (att2_is_num) {
      return (float)num_nom2(att1, att2);
    }
    
    if (att1_is_num) {
      return (float)num_nom2(att2, att1);
    }
    


    return (float)nom_nom(att1, att2);
  }
  

  private double symmUncertCorr(int att1, int att2)
  {
    double sum = 0.0D;
    



    boolean flag = false;
    double temp = 0.0D;
    
    if ((att1 == m_classIndex) || (att2 == m_classIndex)) {
      flag = true;
    }
    
    int ni = m_trainInstances.attribute(att1).numValues() + 1;
    int nj = m_trainInstances.attribute(att2).numValues() + 1;
    double[][] counts = new double[ni][nj];
    double[] sumi = new double[ni];
    double[] sumj = new double[nj];
    
    for (int i = 0; i < ni; i++) {
      sumi[i] = 0.0D;
      
      for (int j = 0; j < nj; j++) {
        sumj[j] = 0.0D;
        counts[i][j] = 0.0D;
      }
    }
    

    for (i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      int ii;
      int ii; if (inst.isMissing(att1)) {
        ii = ni - 1;
      }
      else
        ii = (int)inst.value(att1);
      int jj;
      int jj;
      if (inst.isMissing(att2)) {
        jj = nj - 1;
      }
      else {
        jj = (int)inst.value(att2);
      }
      
      counts[ii][jj] += 1.0D;
    }
    

    for (i = 0; i < ni; i++) {
      sumi[i] = 0.0D;
      
      for (int j = 0; j < nj; j++) {
        sumi[i] += counts[i][j];
        sum += counts[i][j];
      }
    }
    

    for (int j = 0; j < nj; j++) {
      sumj[j] = 0.0D;
      
      for (i = 0; i < ni; i++) {
        sumj[j] += counts[i][j];
      }
    }
    

    if ((!m_missingSeparate) && (sumi[(ni - 1)] < m_numInstances) && (sumj[(nj - 1)] < m_numInstances))
    {

      double[] i_copy = new double[sumi.length];
      double[] j_copy = new double[sumj.length];
      double[][] counts_copy = new double[sumi.length][sumj.length];
      
      for (i = 0; i < ni; i++) {
        System.arraycopy(counts[i], 0, counts_copy[i], 0, sumj.length);
      }
      
      System.arraycopy(sumi, 0, i_copy, 0, sumi.length);
      System.arraycopy(sumj, 0, j_copy, 0, sumj.length);
      double total_missing = sumi[(ni - 1)] + sumj[(nj - 1)] - counts[(ni - 1)][(nj - 1)];
      


      if (sumi[(ni - 1)] > 0.0D) {
        for (j = 0; j < nj - 1; j++) {
          if (counts[(ni - 1)][j] > 0.0D) {
            for (i = 0; i < ni - 1; i++) {
              temp = i_copy[i] / (sum - i_copy[(ni - 1)]) * counts[(ni - 1)][j];
              counts[i][j] += temp;
              sumi[i] += temp;
            }
            
            counts[(ni - 1)][j] = 0.0D;
          }
        }
      }
      
      sumi[(ni - 1)] = 0.0D;
      

      if (sumj[(nj - 1)] > 0.0D) {
        for (i = 0; i < ni - 1; i++) {
          if (counts[i][(nj - 1)] > 0.0D) {
            for (j = 0; j < nj - 1; j++) {
              temp = j_copy[j] / (sum - j_copy[(nj - 1)]) * counts[i][(nj - 1)];
              counts[i][j] += temp;
              sumj[j] += temp;
            }
            
            counts[i][(nj - 1)] = 0.0D;
          }
        }
      }
      
      sumj[(nj - 1)] = 0.0D;
      

      if ((counts[(ni - 1)][(nj - 1)] > 0.0D) && (total_missing != sum)) {
        for (i = 0; i < ni - 1; i++) {
          for (j = 0; j < nj - 1; j++) {
            temp = counts_copy[i][j] / (sum - total_missing) * counts_copy[(ni - 1)][(nj - 1)];
            

            counts[i][j] += temp;
            sumi[i] += temp;
            sumj[j] += temp;
          }
        }
        
        counts[(ni - 1)][(nj - 1)] = 0.0D;
      }
    }
    
    double corr_measure = ContingencyTables.symmetricalUncertainty(counts);
    
    if (Utils.eq(corr_measure, 0.0D)) {
      if (flag == true) {
        return 0.0D;
      }
      
      return 1.0D;
    }
    

    return corr_measure;
  }
  


  private double num_num(int att1, int att2)
  {
    double num = 0.0D;double sx = 0.0D;double sy = 0.0D;
    double mx = m_trainInstances.meanOrMode(m_trainInstances.attribute(att1));
    double my = m_trainInstances.meanOrMode(m_trainInstances.attribute(att2));
    
    for (int i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      double diff1 = inst.isMissing(att1) ? 0.0D : inst.value(att1) - mx;
      double diff2 = inst.isMissing(att2) ? 0.0D : inst.value(att2) - my;
      num += diff1 * diff2;
      sx += diff1 * diff1;
      sy += diff2 * diff2;
    }
    
    if ((sx != 0.0D) && 
      (m_std_devs[att1] == 1.0D)) {
      m_std_devs[att1] = Math.sqrt(sx / m_numInstances);
    }
    

    if ((sy != 0.0D) && 
      (m_std_devs[att2] == 1.0D)) {
      m_std_devs[att2] = Math.sqrt(sy / m_numInstances);
    }
    

    if (sx * sy > 0.0D) {
      double r = num / Math.sqrt(sx * sy);
      return r < 0.0D ? -r : r;
    }
    
    if ((att1 != m_classIndex) && (att2 != m_classIndex)) {
      return 1.0D;
    }
    
    return 0.0D;
  }
  




  private double num_nom2(int att1, int att2)
  {
    int mx = (int)m_trainInstances.meanOrMode(m_trainInstances.attribute(att1));
    
    double my = m_trainInstances.meanOrMode(m_trainInstances.attribute(att2));
    
    double stdv_num = 0.0D;
    
    double r = 0.0D;
    int nx = !m_missingSeparate ? m_trainInstances.attribute(att1).numValues() : m_trainInstances.attribute(att1).numValues() + 1;
    


    double[] prior_nom = new double[nx];
    double[] stdvs_nom = new double[nx];
    double[] covs = new double[nx];
    
    for (int i = 0; i < nx; i++) {
      double tmp115_114 = (prior_nom[i] = 0.0D);covs[i] = tmp115_114;stdvs_nom[i] = tmp115_114;
    }
    


    for (i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      int ii;
      int ii; if (inst.isMissing(att1)) { int ii;
        if (!m_missingSeparate) {
          ii = mx;
        }
        else {
          ii = nx - 1;
        }
      }
      else {
        ii = (int)inst.value(att1);
      }
      

      prior_nom[ii] += 1.0D;
    }
    
    for (int k = 0; k < m_numInstances; k++) {
      Instance inst = m_trainInstances.instance(k);
      
      double diff2 = inst.isMissing(att2) ? 0.0D : inst.value(att2) - my;
      stdv_num += diff2 * diff2;
      

      for (i = 0; i < nx; i++) { double temp;
        double temp; if (inst.isMissing(att1)) { double temp;
          if (!m_missingSeparate) {
            temp = i == mx ? 1.0D : 0.0D;
          }
          else {
            temp = i == nx - 1 ? 1.0D : 0.0D;
          }
        }
        else {
          temp = i == inst.value(att1) ? 1.0D : 0.0D;
        }
        
        double diff1 = temp - prior_nom[i] / m_numInstances;
        stdvs_nom[i] += diff1 * diff1;
        tmp115_114[i] += diff1 * diff2;
      }
    }
    

    i = 0; for (double temp = 0.0D; i < nx; i++)
    {
      temp += prior_nom[i] / m_numInstances * (stdvs_nom[i] / m_numInstances);
      

      if (stdvs_nom[i] * stdv_num > 0.0D)
      {
        double rr = tmp115_114[i] / Math.sqrt(stdvs_nom[i] * stdv_num);
        
        if (rr < 0.0D) {
          rr = -rr;
        }
        
        r += prior_nom[i] / m_numInstances * rr;







      }
      else if ((att1 != m_classIndex) && (att2 != m_classIndex)) {
        r += prior_nom[i] / m_numInstances * 1.0D;
      }
    }
    



    if ((temp != 0.0D) && 
      (m_std_devs[att1] == 1.0D)) {
      m_std_devs[att1] = Math.sqrt(temp);
    }
    

    if ((stdv_num != 0.0D) && 
      (m_std_devs[att2] == 1.0D)) {
      m_std_devs[att2] = Math.sqrt(stdv_num / m_numInstances);
    }
    

    if ((r == 0.0D) && 
      (att1 != m_classIndex) && (att2 != m_classIndex)) {
      r = 1.0D;
    }
    

    return r;
  }
  


  private double nom_nom(int att1, int att2)
  {
    int mx = (int)m_trainInstances.meanOrMode(m_trainInstances.attribute(att1));
    
    int my = (int)m_trainInstances.meanOrMode(m_trainInstances.attribute(att2));
    

    double r = 0.0D;
    int nx = !m_missingSeparate ? m_trainInstances.attribute(att1).numValues() : m_trainInstances.attribute(att1).numValues() + 1;
    


    int ny = !m_missingSeparate ? m_trainInstances.attribute(att2).numValues() : m_trainInstances.attribute(att2).numValues() + 1;
    


    double[][] prior_nom = new double[nx][ny];
    double[] sumx = new double[nx];
    double[] sumy = new double[ny];
    double[] stdvsx = new double[nx];
    double[] stdvsy = new double[ny];
    double[][] covs = new double[nx][ny];
    
    for (int i = 0; i < nx; i++) {
      double tmp170_169 = 0.0D;stdvsx[i] = tmp170_169;sumx[i] = tmp170_169;
    }
    
    for (int j = 0; j < ny; j++) {
      double tmp198_197 = 0.0D;stdvsy[j] = tmp198_197;sumy[j] = tmp198_197;
    }
    
    for (i = 0; i < nx; i++) {
      for (j = 0; j < ny; j++) {
        double tmp238_237 = 0.0D;prior_nom[i][j] = tmp238_237;covs[i][j] = tmp238_237;
      }
    }
    


    for (i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      int ii;
      int ii; if (inst.isMissing(att1)) { int ii;
        if (!m_missingSeparate) {
          ii = mx;
        }
        else {
          ii = nx - 1;
        }
      }
      else {
        ii = (int)inst.value(att1); }
      int jj;
      int jj;
      if (inst.isMissing(att2)) { int jj;
        if (!m_missingSeparate) {
          jj = my;
        }
        else {
          jj = ny - 1;
        }
      }
      else {
        jj = (int)inst.value(att2);
      }
      

      prior_nom[ii][jj] += 1.0D;
      sumx[ii] += 1.0D;
      sumy[jj] += 1.0D;
    }
    
    for (int z = 0; z < m_numInstances; z++) {
      Instance inst = m_trainInstances.instance(z);
      
      for (j = 0; j < ny; j++) { double temp2;
        double temp2; if (inst.isMissing(att2)) { double temp2;
          if (!m_missingSeparate) {
            temp2 = j == my ? 1.0D : 0.0D;
          }
          else {
            temp2 = j == ny - 1 ? 1.0D : 0.0D;
          }
        }
        else {
          temp2 = j == inst.value(att2) ? 1.0D : 0.0D;
        }
        
        double diff2 = temp2 - sumy[j] / m_numInstances;
        stdvsy[j] += diff2 * diff2;
      }
      

      for (i = 0; i < nx; i++) { double temp1;
        double temp1; if (inst.isMissing(att1)) { double temp1;
          if (!m_missingSeparate) {
            temp1 = i == mx ? 1.0D : 0.0D;
          }
          else {
            temp1 = i == nx - 1 ? 1.0D : 0.0D;
          }
        }
        else {
          temp1 = i == inst.value(att1) ? 1.0D : 0.0D;
        }
        
        double diff1 = temp1 - sumx[i] / m_numInstances;
        stdvsx[i] += diff1 * diff1;
        
        for (j = 0; j < ny; j++) { double temp2;
          double temp2; if (inst.isMissing(att2)) { double temp2;
            if (!m_missingSeparate) {
              temp2 = j == my ? 1.0D : 0.0D;
            }
            else {
              temp2 = j == ny - 1 ? 1.0D : 0.0D;
            }
          }
          else {
            temp2 = j == inst.value(att2) ? 1.0D : 0.0D;
          }
          
          double diff2 = temp2 - sumy[j] / m_numInstances;
          covs[i][j] += diff1 * diff2;
        }
      }
    }
    

    for (i = 0; i < nx; i++) {
      for (j = 0; j < ny; j++) {
        if (stdvsx[i] * stdvsy[j] > 0.0D)
        {
          double rr = covs[i][j] / Math.sqrt(stdvsx[i] * stdvsy[j]);
          
          if (rr < 0.0D) {
            rr = -rr;
          }
          
          r += prior_nom[i][j] / m_numInstances * rr;





        }
        else if ((att1 != m_classIndex) && (att2 != m_classIndex)) {
          r += prior_nom[i][j] / m_numInstances * 1.0D;
        }
      }
    }
    



    i = 0; for (double temp1 = 0.0D; i < nx; i++) {
      temp1 += sumx[i] / m_numInstances * (stdvsx[i] / m_numInstances);
    }
    
    if ((temp1 != 0.0D) && 
      (m_std_devs[att1] == 1.0D)) {
      m_std_devs[att1] = Math.sqrt(temp1);
    }
    

    j = 0; for (double temp2 = 0.0D; j < ny; j++) {
      temp2 += sumy[j] / m_numInstances * (stdvsy[j] / m_numInstances);
    }
    
    if ((temp2 != 0.0D) && 
      (m_std_devs[att2] == 1.0D)) {
      m_std_devs[att2] = Math.sqrt(temp2);
    }
    

    if ((r == 0.0D) && 
      (att1 != m_classIndex) && (att2 != m_classIndex)) {
      r = 1.0D;
    }
    

    return r;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("CFS subset evaluator has not been built yet\n");
    }
    else {
      text.append("\tCFS Subset Evaluator\n");
      
      if (m_missingSeparate) {
        text.append("\tTreating missing values as a separate value\n");
      }
      
      if (m_locallyPredictive) {
        text.append("\tIncluding locally predictive attributes\n");
      }
    }
    
    return text.toString();
  }
  
  private void addLocallyPredictive(BitSet best_group)
  {
    boolean done = false;
    boolean ok = true;
    double temp_best = -1.0D;
    
    int j = 0;
    BitSet temp_group = (BitSet)best_group.clone();
    

    while (!done) {
      temp_best = -1.0D;
      

      for (int i = 0; i < m_numAttribs; i++) { int smaller;
        int smaller; int larger; if (i > m_classIndex) {
          int larger = i;
          smaller = m_classIndex;
        } else {
          smaller = i;
          larger = m_classIndex;
        }
        



        if ((!temp_group.get(i)) && (i != m_classIndex)) {
          if (m_corr_matrix[larger][smaller] == -999.0F) {
            float corr = correlate(i, m_classIndex);
            m_corr_matrix[larger][smaller] = corr;
          }
          
          if (m_corr_matrix[larger][smaller] > temp_best) {
            temp_best = m_corr_matrix[larger][smaller];
            j = i;
          }
        }
      }
      
      if (temp_best == -1.0D) {
        done = true;
      }
      else {
        ok = true;
        temp_group.set(j);
        


        for (i = 0; i < m_numAttribs; i++) { int smaller;
          int larger; int smaller; if (i > j) {
            int larger = i;
            smaller = j;
          } else {
            larger = j;
            smaller = i;
          }
          


          if (best_group.get(i)) {
            if (m_corr_matrix[larger][smaller] == -999.0F) {
              float corr = correlate(i, j);
              m_corr_matrix[larger][smaller] = corr;
            }
            
            if (m_corr_matrix[larger][smaller] > temp_best - m_c_Threshold) {
              ok = false;
              break;
            }
          }
        }
        

        if (ok) {
          best_group.set(j);
        }
      }
    }
  }
  








  public int[] postProcess(int[] attributeSet)
    throws Exception
  {
    int j = 0;
    
    if (!m_locallyPredictive) {
      return attributeSet;
    }
    
    BitSet bestGroup = new BitSet(m_numAttribs);
    
    for (int element : attributeSet) {
      bestGroup.set(element);
    }
    
    addLocallyPredictive(bestGroup);
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (bestGroup.get(i)) {
        j++;
      }
    }
    
    int[] newSet = new int[j];
    j = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (bestGroup.get(i)) {
        newSet[(j++)] = i;
      }
    }
    
    return newSet;
  }
  
  public void clean()
  {
    if (m_trainInstances != null)
    {
      m_trainInstances = new Instances(m_trainInstances, 0);
    }
  }
  
  protected void resetOptions()
  {
    m_trainInstances = null;
    m_missingSeparate = false;
    m_locallyPredictive = true;
    m_c_Threshold = 0.0D;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11851 $");
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new CfsSubsetEval(), args);
  }
}
