package weka.classifiers.functions.supportVector;

import java.util.Enumeration;
import java.util.Random;
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


































































public class RegSMO
  extends RegOptimizer
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -7504070793279598638L;
  protected double m_eps = 1.0E-12D;
  


  protected static final double m_Del = 1.0E-10D;
  


  double[] m_error;
  


  protected double m_alpha1;
  


  protected double m_alpha1Star;
  


  protected double m_alpha2;
  


  protected double m_alpha2Star;
  



  public RegSMO() {}
  



  public String globalInfo()
  {
    return "Implementation of SMO for support vector regression as described in :\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "A.J. Smola and B. Schoelkopf");
    result.setValue(TechnicalInformation.Field.TITLE, "A tutorial on support vector regression");
    result.setValue(TechnicalInformation.Field.NOTE, "NeuroCOLT2 Technical Report NC2-TR-1998-030");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe epsilon for round-off error.\n\t(default 1.0e-12)", "P", 1, "-P <double>"));
    



    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    
    return result.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setEpsilon(Double.parseDouble(tmpStr));
    } else {
      setEpsilon(1.0E-12D);
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-P");
    result.add("" + getEpsilon());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String epsilonTipText()
  {
    return "The epsilon for round-off error (shouldn't be changed).";
  }
  




  public double getEpsilon()
  {
    return m_eps;
  }
  




  public void setEpsilon(double v)
  {
    m_eps = v;
  }
  



  protected void init(Instances data)
    throws Exception
  {
    super.init(data);
    

    m_error = new double[m_nInstances];
    for (int i = 0; i < m_nInstances; i++) {
      m_error[i] = (-m_target[i]);
    }
  }
  




  protected void wrapUp()
    throws Exception
  {
    m_error = null;
    super.wrapUp();
  }
  


















  protected boolean findOptimalPointOnLine(int i1, double alpha1, double alpha1Star, double C1, int i2, double alpha2, double alpha2Star, double C2, double gamma, double eta, double deltaPhi)
  {
    if (eta <= 0.0D)
    {

      return false;
    }
    
    boolean case1 = false;
    boolean case2 = false;
    boolean case3 = false;
    boolean case4 = false;
    boolean finished = false;
    



    while (!finished)
    {














      if ((!case1) && ((alpha1 > 0.0D) || ((alpha1Star == 0.0D) && (deltaPhi > 0.0D))) && ((alpha2 > 0.0D) || ((alpha2Star == 0.0D) && (deltaPhi < 0.0D))))
      {


        double L = Math.max(0.0D, gamma - C1);
        double H = Math.min(C2, gamma);
        if (L < H) {
          double a2 = alpha2 - deltaPhi / eta;
          a2 = Math.min(a2, H);
          a2 = Math.max(L, a2);
          
          if (a2 > C2 - 1.0E-10D * C2) {
            a2 = C2;
          } else if (a2 <= 1.0E-10D * C2) {
            a2 = 0.0D;
          }
          double a1 = alpha1 - (a2 - alpha2);
          if (a1 > C1 - 1.0E-10D * C1) {
            a1 = C1;
          } else if (a1 <= 1.0E-10D * C1) {
            a1 = 0.0D;
          }
          
          if (Math.abs(alpha1 - a1) > m_eps) {
            deltaPhi += eta * (a2 - alpha2);
            alpha1 = a1;
            alpha2 = a2;
          }
        } else {
          finished = true;
        }
        case1 = true;
















      }
      else if ((!case2) && ((alpha1 > 0.0D) || ((alpha1Star == 0.0D) && (deltaPhi > 2.0D * m_epsilon))) && ((alpha2Star > 0.0D) || ((alpha2 == 0.0D) && (deltaPhi > 2.0D * m_epsilon))))
      {



        double L = Math.max(0.0D, -gamma);
        double H = Math.min(C2, -gamma + C1);
        if (L < H) {
          double a2 = alpha2Star + (deltaPhi - 2.0D * m_epsilon) / eta;
          a2 = Math.min(a2, H);
          a2 = Math.max(L, a2);
          
          if (a2 > C2 - 1.0E-10D * C2) {
            a2 = C2;
          } else if (a2 <= 1.0E-10D * C2) {
            a2 = 0.0D;
          }
          double a1 = alpha1 + (a2 - alpha2Star);
          if (a1 > C1 - 1.0E-10D * C1) {
            a1 = C1;
          } else if (a1 <= 1.0E-10D * C1) {
            a1 = 0.0D;
          }
          
          if (Math.abs(alpha1 - a1) > m_eps) {
            deltaPhi += eta * (-a2 + alpha2Star);
            alpha1 = a1;
            alpha2Star = a2;
          }
        } else {
          finished = true;
        }
        case2 = true;
















      }
      else if ((!case3) && ((alpha1Star > 0.0D) || ((alpha1 == 0.0D) && (deltaPhi < -2.0D * m_epsilon))) && ((alpha2 > 0.0D) || ((alpha2Star == 0.0D) && (deltaPhi < -2.0D * m_epsilon))))
      {



        double L = Math.max(0.0D, gamma);
        double H = Math.min(C2, C1 + gamma);
        if (L < H)
        {
          double a2 = alpha2 - (deltaPhi + 2.0D * m_epsilon) / eta;
          a2 = Math.min(a2, H);
          a2 = Math.max(L, a2);
          
          if (a2 > C2 - 1.0E-10D * C2) {
            a2 = C2;
          } else if (a2 <= 1.0E-10D * C2) {
            a2 = 0.0D;
          }
          double a1 = alpha1Star + (a2 - alpha2);
          if (a1 > C1 - 1.0E-10D * C1) {
            a1 = C1;
          } else if (a1 <= 1.0E-10D * C1) {
            a1 = 0.0D;
          }
          
          if (Math.abs(alpha1Star - a1) > m_eps) {
            deltaPhi += eta * (a2 - alpha2);
            alpha1Star = a1;
            alpha2 = a2;
          }
        } else {
          finished = true;
        }
        case3 = true;



















      }
      else if ((!case4) && ((alpha1Star > 0.0D) || ((alpha1 == 0.0D) && (deltaPhi < 0.0D))) && ((alpha2Star > 0.0D) || ((alpha2 == 0.0D) && (deltaPhi > 0.0D))))
      {


        double L = Math.max(0.0D, -gamma - C1);
        double H = Math.min(C2, -gamma);
        if (L < H) {
          double a2 = alpha2Star + deltaPhi / eta;
          a2 = Math.min(a2, H);
          a2 = Math.max(L, a2);
          
          if (a2 > C2 - 1.0E-10D * C2) {
            a2 = C2;
          } else if (a2 <= 1.0E-10D * C2) {
            a2 = 0.0D;
          }
          double a1 = alpha1Star - (a2 - alpha2Star);
          if (a1 > C1 - 1.0E-10D * C1) {
            a1 = C1;
          } else if (a1 <= 1.0E-10D * C1) {
            a1 = 0.0D;
          }
          
          if (Math.abs(alpha1Star - a1) > m_eps) {
            deltaPhi += eta * (-a2 + alpha2Star);
            
            alpha1Star = a1;
            alpha2Star = a2;
          }
        } else {
          finished = true;
        }
        case4 = true;
      } else {
        finished = true;
      }
    }
    










    if ((Math.abs(alpha1 - m_alpha[i1]) > m_eps) || (Math.abs(alpha1Star - m_alphaStar[i1]) > m_eps) || (Math.abs(alpha2 - m_alpha[i2]) > m_eps) || (Math.abs(alpha2Star - m_alphaStar[i2]) > m_eps))
    {



      if (alpha1 > C1 - 1.0E-10D * C1) {
        alpha1 = C1;
      } else if (alpha1 <= 1.0E-10D * C1) {
        alpha1 = 0.0D;
      }
      if (alpha1Star > C1 - 1.0E-10D * C1) {
        alpha1Star = C1;
      } else if (alpha1Star <= 1.0E-10D * C1) {
        alpha1Star = 0.0D;
      }
      if (alpha2 > C2 - 1.0E-10D * C2) {
        alpha2 = C2;
      } else if (alpha2 <= 1.0E-10D * C2) {
        alpha2 = 0.0D;
      }
      if (alpha2Star > C2 - 1.0E-10D * C2) {
        alpha2Star = C2;
      } else if (alpha2Star <= 1.0E-10D * C2) {
        alpha2Star = 0.0D;
      }
      

      m_alpha[i1] = alpha1;
      m_alphaStar[i1] = alpha1Star;
      m_alpha[i2] = alpha2;
      m_alphaStar[i2] = alpha2Star;
      

      if ((alpha1 != 0.0D) || (alpha1Star != 0.0D)) {
        if (!m_supportVectors.contains(i1)) {
          m_supportVectors.insert(i1);
        }
      } else {
        m_supportVectors.delete(i1);
      }
      if ((alpha2 != 0.0D) || (alpha2Star != 0.0D)) {
        if (!m_supportVectors.contains(i2)) {
          m_supportVectors.insert(i2);
        }
      } else {
        m_supportVectors.delete(i2);
      }
      return true;
    }
    
    return false;
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
    double y1 = m_target[i1];
    double phi1 = m_error[i1];
    






    double k11 = m_kernel.eval(i1, i1, m_data.instance(i1));
    double k12 = m_kernel.eval(i1, i2, m_data.instance(i1));
    double k22 = m_kernel.eval(i2, i2, m_data.instance(i2));
    double eta = -2.0D * k12 + k11 + k22;
    if (eta < 0.0D)
    {

      return 0;
    }
    double gamma = alpha1 - alpha1Star + alpha2 - alpha2Star;
    








    double alpha1old = alpha1;
    double alpha1Starold = alpha1Star;
    double alpha2old = alpha2;
    double alpha2Starold = alpha2Star;
    double deltaPhi = phi2 - phi1;
    
    if (findOptimalPointOnLine(i1, alpha1, alpha1Star, C1, i2, alpha2, alpha2Star, C2, gamma, eta, deltaPhi)) {
      alpha1 = m_alpha[i1];
      alpha1Star = m_alphaStar[i1];
      alpha2 = m_alpha[i2];
      alpha2Star = m_alphaStar[i2];
      


      double dAlpha1 = alpha1 - alpha1old - (alpha1Star - alpha1Starold);
      double dAlpha2 = alpha2 - alpha2old - (alpha2Star - alpha2Starold);
      for (int j = 0; j < m_nInstances; j++) {
        if ((j != i1) && (j != i2)) {
          m_error[j] += dAlpha1 * m_kernel.eval(i1, j, m_data.instance(i1)) + dAlpha2 * m_kernel.eval(i2, j, m_data.instance(i2));
        }
      }
      m_error[i1] += dAlpha1 * k11 + dAlpha2 * k12;
      m_error[i2] += dAlpha1 * k12 + dAlpha2 * k22;
      

      double b1 = Double.MAX_VALUE;
      double b2 = Double.MAX_VALUE;
      if (((0.0D < alpha1) && (alpha1 < C1)) || ((0.0D < alpha1Star) && (alpha1Star < C1)) || ((0.0D < alpha2) && (alpha2 < C2)) || ((0.0D < alpha2Star) && (alpha2Star < C2))) {
        if ((0.0D < alpha1) && (alpha1 < C1)) {
          b1 = m_error[i1] - m_epsilon;
        } else if ((0.0D < alpha1Star) && (alpha1Star < C1)) {
          b1 = m_error[i1] + m_epsilon;
        }
        if ((0.0D < alpha2) && (alpha2 < C2)) {
          b2 = m_error[i2] - m_epsilon;
        } else if ((0.0D < alpha2Star) && (alpha2Star < C2)) {
          b2 = m_error[i2] + m_epsilon;
        }
        if (b1 < Double.MAX_VALUE) {
          m_b = b1;
          if (b2 < Double.MAX_VALUE) {
            m_b = ((b1 + b2) / 2.0D);
          }
        } else if (b2 < Double.MAX_VALUE) {
          m_b = b2;
        }
      } else if (m_b == 0.0D)
      {
        m_b = ((m_error[i1] + m_error[i2]) / 2.0D);
      }
      





      return 1;
    }
    return 0;
  }
  










  protected int examineExample(int i2)
    throws Exception
  {
    double y2 = m_target[i2];
    
    double alpha2 = m_alpha[i2];
    double alpha2Star = m_alphaStar[i2];
    
    double C2 = m_C;
    double C2Star = m_C;
    
    double phi2 = m_error[i2];
    
    double phi2b = phi2 - m_b;
    



    if (((phi2b > m_epsilon) && (alpha2Star < C2Star)) || ((phi2b < m_epsilon) && (alpha2Star > 0.0D)) || ((-phi2b > m_epsilon) && (alpha2 < C2)) || ((-phi2b > m_epsilon) && (alpha2 > 0.0D)))
    {







      int i1 = secondChoiceHeuristic(i2);
      if ((i1 >= 0) && (takeStep(i1, i2, alpha2, alpha2Star, phi2) > 0)) {
        return 1;
      }
      



      for (i1 = 0; i1 < m_target.length; i1++) {
        if (((m_alpha[i1] > 0.0D) && (m_alpha[i1] < m_C)) || ((m_alphaStar[i1] > 0.0D) && (m_alphaStar[i1] < m_C) && 
          (takeStep(i1, i2, alpha2, alpha2Star, phi2) > 0))) {
          return 1;
        }
      }
      




      for (i1 = 0; i1 < m_target.length; i1++) {
        if (takeStep(i1, i2, alpha2, alpha2Star, phi2) > 0) {
          return 1;
        }
      }
    }
    

    return 0;
  }
  








  protected int secondChoiceHeuristic(int i2)
  {
    for (int i = 0; i < 59; i++) {
      int i1 = m_random.nextInt(m_nInstances);
      if (((i1 != i2) && (m_alpha[i1] > 0.0D) && (m_alpha[i1] < m_C)) || ((m_alphaStar[i1] > 0.0D) && (m_alphaStar[i1] < m_C))) {
        return i1;
      }
    }
    return -1;
  }
  










  public void optimize()
    throws Exception
  {
    int numChanged = 0;
    int examineAll = 1;
    int sigFig = -100;
    int loopCounter = 0;
    
    while ((((numChanged > 0) || (examineAll > 0) ? 1 : 0) | (sigFig < 3 ? 1 : 0)) != 0)
    {

      loopCounter++;
      numChanged = 0;
      






      int numSamples = 0;
      if (examineAll > 0) {
        for (int i = 0; i < m_nInstances; i++) {
          numChanged += examineExample(i);
        }
      } else {
        for (int i = 0; i < m_target.length; i++) {
          if (((m_alpha[i] > 0.0D) && (m_alpha[i] < m_C * m_data.instance(i).weight())) || ((m_alphaStar[i] > 0.0D) && (m_alphaStar[i] < m_C * m_data.instance(i).weight())))
          {
            numSamples++;
            numChanged += examineExample(i);
          }
        }
      }
      





      int minimumNumChanged = 1;
      if (loopCounter % 2 == 0) {
        minimumNumChanged = (int)Math.max(1.0D, 0.1D * numSamples);
      }
      





      if (examineAll == 1) {
        examineAll = 0;
      } else if (numChanged < minimumNumChanged) {
        examineAll = 1;
      }
      

      if (loopCounter == 2500) {
        break;
      }
    }
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    init(instances);
    
    optimize();
    
    wrapUp();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
