package weka.classifiers.functions.supportVector;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

































































































public class RegSMOImproved
  extends RegSMO
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 471692841446029784L;
  public static final int I0 = 3;
  public static final int I0a = 1;
  public static final int I0b = 2;
  public static final int I1 = 4;
  public static final int I2 = 8;
  public static final int I3 = 16;
  protected SMOset m_I0;
  protected int[] m_iSet;
  protected double m_bUp;
  protected double m_bLow;
  protected int m_iUp;
  protected int m_iLow;
  double m_fTolerance = 0.001D;
  

  boolean m_bUseVariant1 = true;
  


  public RegSMOImproved() {}
  

  public String globalInfo()
  {
    return "Learn SVM for regression using SMO with Shevade, Keerthi, et al. adaption of the stopping criterion.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "S.K. Shevade and S.S. Keerthi and C. Bhattacharyya and K.R.K. Murthy");
    result.setValue(TechnicalInformation.Field.TITLE, "Improvements to the SMO Algorithm for SVM Regression");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "IEEE Transactions on Neural Networks");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.PS, "http://guppy.mpe.nus.edu.sg/~mpessk/svm/ieee_smo_reg.ps.gz");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "S.K. Shevade and S.S. Keerthi and C. Bhattacharyya and K.R.K. Murthy");
    additional.setValue(TechnicalInformation.Field.TITLE, "Improvements to the SMO Algorithm for SVM Regression");
    additional.setValue(TechnicalInformation.Field.INSTITUTION, "National University of Singapore");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Control Division, Dept. of Mechanical Engineering");
    additional.setValue(TechnicalInformation.Field.NUMBER, "CD-99-16");
    additional.setValue(TechnicalInformation.Field.YEAR, "1999");
    additional.setValue(TechnicalInformation.Field.PS, "http://guppy.mpe.nus.edu.sg/~mpessk/svm/smoreg_mod.ps.gz");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe tolerance parameter for checking the stopping criterion.\n\t(default 0.001)", "T", 1, "-T <double>"));
    



    result.addElement(new Option("\tUse variant 1 of the algorithm when true, otherwise use variant 2.\n\t(default true)", "V", 0, "-V"));
    



    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    
    return result.elements();
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('T', options);
    if (tmpStr.length() != 0) {
      setTolerance(Double.parseDouble(tmpStr));
    } else {
      setTolerance(0.001D);
    }
    
    setUseVariant1(Utils.getFlag('V', options));
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-T");
    result.add("" + getTolerance());
    
    if (m_bUseVariant1) {
      result.add("-V");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String toleranceTipText()
  {
    return "tolerance parameter used for checking stopping criterion b.up < b.low + 2 tol";
  }
  




  public double getTolerance()
  {
    return m_fTolerance;
  }
  




  public void setTolerance(double d)
  {
    m_fTolerance = d;
  }
  





  public String useVariant1TipText()
  {
    return "set true to use variant 1 of the paper, otherwise use variant 2.";
  }
  




  public boolean isUseVariant1()
  {
    return m_bUseVariant1;
  }
  




  public void setUseVariant1(boolean b)
  {
    m_bUseVariant1 = b;
  }
  














  protected int takeStep(int i1, int i2, double alpha2, double alpha2Star, double phi2)
    throws Exception
  {
    if (i1 == i2) {
      return 0;
    }
    double C1 = m_C * m_data.instance(i1).weight();
    double C2 = m_C * m_data.instance(i2).weight();
    
    double alpha1 = m_alpha[i1];
    double alpha1Star = m_alphaStar[i1];
    


    double phi1 = m_error[i1];
    









    double k11 = m_kernel.eval(i1, i1, m_data.instance(i1));
    double k12 = m_kernel.eval(i1, i2, m_data.instance(i1));
    double k22 = m_kernel.eval(i2, i2, m_data.instance(i2));
    double eta = -2.0D * k12 + k11 + k22;
    double gamma = alpha1 - alpha1Star + alpha2 - alpha2Star;
    






































































    double alpha1old = alpha1;
    double alpha1Starold = alpha1Star;
    double alpha2old = alpha2;
    double alpha2Starold = alpha2Star;
    double deltaPhi = phi1 - phi2;
    
    if (findOptimalPointOnLine(i1, alpha1, alpha1Star, C1, i2, alpha2, alpha2Star, C2, gamma, eta, deltaPhi))
    {
      alpha1 = m_alpha[i1];
      alpha1Star = m_alphaStar[i1];
      alpha2 = m_alpha[i2];
      alpha2Star = m_alphaStar[i2];
      











      double dAlpha1 = alpha1 - alpha1old - (alpha1Star - alpha1Starold);
      double dAlpha2 = alpha2 - alpha2old - (alpha2Star - alpha2Starold);
      for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
        if ((j != i1) && (j != i2)) {
          m_error[j] -= dAlpha1 * m_kernel.eval(i1, j, m_data.instance(i1)) + dAlpha2 * m_kernel.eval(i2, j, m_data.instance(i2));
        }
      }
      
      m_error[i1] -= dAlpha1 * k11 + dAlpha2 * k12;
      m_error[i2] -= dAlpha1 * k12 + dAlpha2 * k22;
      
      updateIndexSetFor(i1, C1);
      updateIndexSetFor(i2, C2);
      

      m_bUp = Double.MAX_VALUE;
      m_bLow = -1.7976931348623157E308D;
      for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
        updateBoundaries(j, m_error[j]);
      }
      if (!m_I0.contains(i1)) {
        updateBoundaries(i1, m_error[i1]);
      }
      if (!m_I0.contains(i2)) {
        updateBoundaries(i2, m_error[i2]);
      }
      
      return 1;
    }
    
    return 0;
  }
  













  protected void updateIndexSetFor(int i, double C)
    throws Exception
  {
    if ((m_alpha[i] == 0.0D) && (m_alphaStar[i] == 0.0D))
    {
      m_iSet[i] = 4;
      m_I0.delete(i);
    } else if (m_alpha[i] > 0.0D) {
      if (m_alpha[i] < C) {
        if ((m_iSet[i] & 0x3) == 0)
        {
          m_I0.insert(i);
        }
        
        m_iSet[i] = 1;
      }
      else {
        m_iSet[i] = 16;
        m_I0.delete(i);
      }
    }
    else if (m_alphaStar[i] < C) {
      if ((m_iSet[i] & 0x3) == 0)
      {
        m_I0.insert(i);
      }
      
      m_iSet[i] = 2;
    }
    else {
      m_iSet[i] = 8;
      m_I0.delete(i);
    }
  }
  






  protected void updateBoundaries(int i2, double F2)
  {
    int iSet = m_iSet[i2];
    
    double FLow = m_bLow;
    if ((iSet & 0xA) > 0) {
      FLow = F2 + m_epsilon;
    } else if ((iSet & 0x5) > 0) {
      FLow = F2 - m_epsilon;
    }
    if (m_bLow < FLow) {
      m_bLow = FLow;
      m_iLow = i2;
    }
    double FUp = m_bUp;
    if ((iSet & 0x11) > 0) {
      FUp = F2 - m_epsilon;
    } else if ((iSet & 0x6) > 0) {
      FUp = F2 + m_epsilon;
    }
    if (m_bUp > FUp) {
      m_bUp = FUp;
      m_iUp = i2;
    }
  }
  








  protected int examineExample(int i2)
    throws Exception
  {
    double alpha2 = m_alpha[i2];
    double alpha2Star = m_alphaStar[i2];
    






















    int iSet = m_iSet[i2];
    double F2 = m_error[i2];
    if (!m_I0.contains(i2)) {
      F2 = -SVMOutput(i2) - m_b + m_target[i2];
      m_error[i2] = F2;
      if (iSet == 4) {
        if (F2 + m_epsilon < m_bUp) {
          m_bUp = (F2 + m_epsilon);
          m_iUp = i2;
        } else if (F2 - m_epsilon > m_bLow) {
          m_bLow = (F2 - m_epsilon);
          m_iLow = i2;
        }
      } else if ((iSet == 8) && (F2 + m_epsilon > m_bLow)) {
        m_bLow = (F2 + m_epsilon);
        m_iLow = i2;
      } else if ((iSet == 16) && (F2 - m_epsilon < m_bUp)) {
        m_bUp = (F2 - m_epsilon);
        m_iUp = i2;
      }
    }
    





























































    int i1 = i2;
    boolean bOptimality = true;
    
    if (iSet == 1) {
      if (m_bLow - (F2 - m_epsilon) > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iLow;
        
        if (F2 - m_epsilon - m_bUp > m_bLow - (F2 - m_epsilon)) {
          i1 = m_iUp;
        }
      } else if (F2 - m_epsilon - m_bUp > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iUp;
        
        if (m_bLow - (F2 - m_epsilon) > F2 - m_epsilon - m_bUp) {
          i1 = m_iLow;
        }
      }
    }
    else if (iSet == 2) {
      if (m_bLow - (F2 + m_epsilon) > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iLow;
        if (F2 + m_epsilon - m_bUp > m_bLow - (F2 + m_epsilon)) {
          i1 = m_iUp;
        }
      } else if (F2 + m_epsilon - m_bUp > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iUp;
        if (m_bLow - (F2 + m_epsilon) > F2 + m_epsilon - m_bUp) {
          i1 = m_iLow;
        }
      }
    }
    else if (iSet == 4) {
      if (m_bLow - (F2 + m_epsilon) > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iLow;
        
        if (F2 + m_epsilon - m_bUp > m_bLow - (F2 + m_epsilon)) {
          i1 = m_iUp;
        }
      } else if (F2 - m_epsilon - m_bUp > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iUp;
        if (m_bLow - (F2 - m_epsilon) > F2 - m_epsilon - m_bUp) {
          i1 = m_iLow;
        }
      }
    }
    else if (iSet == 8) {
      if (F2 + m_epsilon - m_bUp > 2.0D * m_fTolerance) {
        bOptimality = false;
        i1 = m_iUp;
      }
    }
    else if ((iSet == 16) && 
      (m_bLow - (F2 - m_epsilon) > 2.0D * m_fTolerance)) {
      bOptimality = false;
      i1 = m_iLow;
    }
    








    if (bOptimality) {
      return 0;
    }
    return takeStep(i1, i2, m_alpha[i2], m_alphaStar[i2], F2);
  }
  




  protected void init(Instances data)
    throws Exception
  {
    super.init(data);
    






    m_I0 = new SMOset(m_data.numInstances());
    m_iSet = new int[m_data.numInstances()];
    for (int i = 0; i < m_nInstances; i++) {
      m_iSet[i] = 4;
    }
    
    m_iUp = 0;
    m_bUp = (m_target[m_iUp] + m_epsilon);
    m_iLow = m_iUp;
    m_bLow = (m_target[m_iLow] - m_epsilon);
    
    m_error = new double[m_nInstances];
    for (int i = 0; i < m_nInstances; i++) {
      m_error[i] = m_target[i];
    }
  }
  






  protected void optimize1()
    throws Exception
  {
    int nNumChanged = 0;
    boolean bExamineAll = true;
    

    while ((nNumChanged > 0) || (bExamineAll)) {
      nNumChanged = 0;
      









      if (bExamineAll) {
        for (int i = 0; i < m_nInstances; i++) {
          nNumChanged += examineExample(i);
        }
      } else {
        for (int i = m_I0.getNext(-1); i != -1; i = m_I0.getNext(i))
        {
          nNumChanged += examineExample(i);
          if (m_bLow - m_bUp < 2.0D * m_fTolerance) {
            nNumChanged = 0;
            break;
          }
        }
      }
      





      if (bExamineAll) {
        bExamineAll = false;
      } else if (nNumChanged == 0) {
        bExamineAll = true;
      }
    }
  }
  




  protected void optimize2()
    throws Exception
  {
    int nNumChanged = 0;
    boolean bExamineAll = true;
    

    while ((nNumChanged > 0) || (bExamineAll)) {
      nNumChanged = 0;
      




















      if (bExamineAll) {
        for (int i = 0; i < m_nInstances; i++) {
          nNumChanged += examineExample(i);
        }
      } else {
        boolean bInnerLoopSuccess = true;
        do {
          if (takeStep(m_iUp, m_iLow, m_alpha[m_iLow], m_alphaStar[m_iLow], m_error[m_iLow]) > 0) {
            bInnerLoopSuccess = true;
            nNumChanged++;
          } else {
            bInnerLoopSuccess = false;
          }
        } while ((m_bUp <= m_bLow - 2.0D * m_fTolerance) && (bInnerLoopSuccess));
        nNumChanged = 0;
      }
      







      if (bExamineAll) {
        bExamineAll = false;
      } else if (nNumChanged == 0) {
        bExamineAll = true;
      }
    }
  }
  




  protected void wrapUp()
    throws Exception
  {
    m_b = (-(m_bLow + m_bUp) / 2.0D);
    m_target = null;
    m_error = null;
    super.wrapUp();
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    init(instances);
    

    if (m_bUseVariant1) {
      optimize1();
    } else {
      optimize2();
    }
    

    wrapUp();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
