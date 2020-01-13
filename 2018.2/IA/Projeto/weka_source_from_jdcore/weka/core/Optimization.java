package weka.core;

import java.io.PrintStream;




















































































































































public abstract class Optimization
  implements TechnicalInformationHandler, RevisionHandler
{
  protected double m_ALF;
  protected double m_BETA;
  protected double m_TOLX;
  protected double m_STPMX;
  protected int m_MAXITS;
  protected static boolean m_Debug = false;
  protected double m_f;
  private double m_Slope;
  private boolean m_IsZeroStep;
  private double[] m_X;
  
  public Optimization()
  {
    m_ALF = 1.0E-4D;
    
    m_BETA = 0.9D;
    
    m_TOLX = 1.0E-6D;
    
    m_STPMX = 100.0D;
    
    m_MAXITS = 200;
    









    m_IsZeroStep = false;
  }
  





  protected static double m_Epsilon = 1.0D;
  static { while (1.0D + m_Epsilon > 1.0D) {
      m_Epsilon /= 2.0D;
    }
    m_Epsilon *= 2.0D;
    m_Zero = Math.sqrt(m_Epsilon);
    if (m_Debug) {
      System.err.print("Machine precision is " + m_Epsilon + " and zero set to " + m_Zero);
    }
  }
  









  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MASTERSTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Xin Xu");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.TITLE, "Statistical learning in multiple instance problem");
    result.setValue(TechnicalInformation.Field.SCHOOL, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, NZ");
    result.setValue(TechnicalInformation.Field.NOTE, "0657.594");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.BOOK);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "P. E. Gill and W. Murray and M. H. Wright");
    additional.setValue(TechnicalInformation.Field.YEAR, "1981");
    additional.setValue(TechnicalInformation.Field.TITLE, "Practical Optimization");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Academic Press");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "London and New York");
    
    additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "P. E. Gill and W. Murray");
    additional.setValue(TechnicalInformation.Field.YEAR, "1976");
    additional.setValue(TechnicalInformation.Field.TITLE, "Minimization subject to bounds on the variables");
    additional.setValue(TechnicalInformation.Field.INSTITUTION, "National Physical Laboratory");
    additional.setValue(TechnicalInformation.Field.NUMBER, "NAC 72");
    
    additional = result.add(TechnicalInformation.Type.BOOK);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "E. K. P. Chong and S. H. Zak");
    additional.setValue(TechnicalInformation.Field.YEAR, "1996");
    additional.setValue(TechnicalInformation.Field.TITLE, "An Introduction to Optimization");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "John Wiley and Sons");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "New York");
    
    additional = result.add(TechnicalInformation.Type.BOOK);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "J. E. Dennis and R. B. Schnabel");
    additional.setValue(TechnicalInformation.Field.YEAR, "1983");
    additional.setValue(TechnicalInformation.Field.TITLE, "Numerical Methods for Unconstrained Optimization and Nonlinear Equations");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Prentice-Hall");
    
    additional = result.add(TechnicalInformation.Type.BOOK);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "W. H. Press and B. P. Flannery and S. A. Teukolsky and W. T. Vetterling");
    additional.setValue(TechnicalInformation.Field.YEAR, "1992");
    additional.setValue(TechnicalInformation.Field.TITLE, "Numerical Recipes in C");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Cambridge University Press");
    additional.setValue(TechnicalInformation.Field.EDITION, "Second");
    
    additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "P. E. Gill and G. H. Golub and W. Murray and M. A. Saunders");
    additional.setValue(TechnicalInformation.Field.YEAR, "1974");
    additional.setValue(TechnicalInformation.Field.TITLE, "Methods for modifying matrix factorizations");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Mathematics of Computation");
    additional.setValue(TechnicalInformation.Field.VOLUME, "28");
    additional.setValue(TechnicalInformation.Field.NUMBER, "126");
    additional.setValue(TechnicalInformation.Field.PAGES, "505-535");
    
    return result;
  }
  














  protected static double m_Zero;
  












  protected double[] evaluateHessian(double[] x, int index)
    throws Exception
  {
    return null;
  }
  




  public double getMinFunction()
  {
    return m_f;
  }
  




  public void setMaxIteration(int it)
  {
    m_MAXITS = it;
  }
  




  public void setDebug(boolean db)
  {
    m_Debug = db;
  }
  





  public double[] getVarbValues()
  {
    return m_X;
  }
  
























  public double[] lnsrch(double[] xold, double[] gradient, double[] direct, double stpmax, boolean[] isFixed, double[][] nwsBounds, DynamicIntArray wsBdsIndx)
    throws Exception
  {
    int len = xold.length;
    int fixedOne = -1;
    


    double alpha = Double.POSITIVE_INFINITY;double fold = m_f;
    

    double alam2 = 0.0D;double disc = 0.0D;double maxalam = 1.0D;
    
    double[] x = new double[len];
    

    double sum = 0.0D; for (int i = 0; i < len; i++) {
      if (isFixed[i] == 0)
        sum += direct[i] * direct[i];
    }
    sum = Math.sqrt(sum);
    
    if (m_Debug) {
      System.err.println("fold:  " + Utils.doubleToString(fold, 10, 7) + "\n" + "sum:  " + Utils.doubleToString(sum, 10, 7) + "\n" + "stpmax:  " + Utils.doubleToString(stpmax, 10, 7));
    }
    
    if (sum > stpmax) {
      for (i = 0; i < len; i++) {
        if (isFixed[i] == 0)
          direct[i] *= stpmax / sum;
      }
    }
    maxalam = stpmax / sum;
    

    m_Slope = 0.0D;
    for (i = 0; i < len; i++) {
      x[i] = xold[i];
      if (isFixed[i] == 0) {
        m_Slope += gradient[i] * direct[i];
      }
    }
    if (m_Debug) {
      System.err.print("slope:  " + Utils.doubleToString(m_Slope, 10, 7) + "\n");
    }
    
    if (Math.abs(m_Slope) <= m_Zero) {
      if (m_Debug) {
        System.err.println("Gradient and direction orthogonal -- Min. found with current fixed variables (or all variables fixed). Try to release some variables now.");
      }
      

      return x;
    }
    

    if (m_Slope > m_Zero) {
      if (m_Debug) {
        for (int h = 0; h < x.length; h++) {
          System.err.println(h + ": isFixed=" + isFixed[h] + ", x=" + x[h] + ", grad=" + gradient[h] + ", direct=" + direct[h]);
        }
      }
      throw new Exception("g'*p positive! -- Try to debug from here: line 327.");
    }
    

    double test = 0.0D;
    for (i = 0; i < len; i++) {
      if (isFixed[i] == 0) {
        double temp = Math.abs(direct[i]) / Math.max(Math.abs(x[i]), 1.0D);
        if (temp > test) test = temp;
      }
    }
    double alamin;
    if (test > m_Zero) {
      alamin = m_TOLX / test;
    } else {
      if (m_Debug) {
        System.err.println("Zero directions for all free variables -- Min. found with current fixed variables (or all variables fixed). Try to release some variables now.");
      }
      

      return x;
    }
    
    double alamin;
    for (i = 0; i < len; i++) {
      if (isFixed[i] == 0)
      {
        if ((direct[i] < -m_Epsilon) && (!Double.isNaN(nwsBounds[0][i]))) {
          double alpi = (nwsBounds[0][i] - xold[i]) / direct[i];
          if (alpi <= m_Zero) {
            if (m_Debug) {
              System.err.println("Fix variable " + i + " to lower bound " + nwsBounds[0][i] + " from value " + xold[i]);
            }
            
            x[i] = nwsBounds[0][i];
            isFixed[i] = true;
            alpha = 0.0D;
            nwsBounds[0][i] = NaN.0D;
            wsBdsIndx.addElement(i);
          }
          else if (alpha > alpi) {
            alpha = alpi;
            fixedOne = i;
          }
        }
        else if ((direct[i] > m_Epsilon) && (!Double.isNaN(nwsBounds[1][i]))) {
          double alpi = (nwsBounds[1][i] - xold[i]) / direct[i];
          if (alpi <= m_Zero) {
            if (m_Debug) {
              System.err.println("Fix variable " + i + " to upper bound " + nwsBounds[1][i] + " from value " + xold[i]);
            }
            
            x[i] = nwsBounds[1][i];
            isFixed[i] = true;
            alpha = 0.0D;
            nwsBounds[1][i] = NaN.0D;
            wsBdsIndx.addElement(i);
          }
          else if (alpha > alpi) {
            alpha = alpi;
            fixedOne = i;
          }
        }
      }
    }
    
    if (m_Debug) {
      System.err.println("alamin: " + Utils.doubleToString(alamin, 10, 7));
      System.err.println("alpha: " + Utils.doubleToString(alpha, 10, 7));
    }
    
    if (alpha <= m_Zero) {
      m_IsZeroStep = true;
      if (m_Debug)
        System.err.println("Alpha too small, try again");
      return x;
    }
    
    double alam = alpha;
    if (alam > 1.0D) {
      alam = 1.0D;
    }
    
    double initF = fold;
    double hi = alam;double lo = alam;double newSlope = 0.0D;double fhi = m_f;double flo = m_f;
    


    for (int k = 0;; k++) {
      if (m_Debug) {
        System.err.println("\nLine search iteration: " + k);
      }
      for (i = 0; i < len; i++) {
        if (isFixed[i] == 0) {
          xold[i] += alam * direct[i];
          if ((!Double.isNaN(nwsBounds[0][i])) && (x[i] < nwsBounds[0][i])) {
            x[i] = nwsBounds[0][i];
          }
          else if ((!Double.isNaN(nwsBounds[1][i])) && (x[i] > nwsBounds[1][i])) {
            x[i] = nwsBounds[1][i];
          }
        }
      }
      
      m_f = objectiveFunction(x);
      if (Double.isNaN(m_f)) {
        throw new Exception("Objective function value is NaN!");
      }
      while (Double.isInfinite(m_f)) {
        if (m_Debug)
          System.err.println("Too large m_f.  Shrink step by half.");
        alam *= 0.5D;
        if (alam <= m_Epsilon) {
          if (m_Debug)
            System.err.println("Wrong starting points, change them!");
          return x;
        }
        
        for (i = 0; i < len; i++) {
          if (isFixed[i] == 0)
            xold[i] += alam * direct[i];
        }
        m_f = objectiveFunction(x);
        if (Double.isNaN(m_f)) {
          throw new Exception("Objective function value is NaN!");
        }
        initF = Double.POSITIVE_INFINITY;
      }
      
      if (m_Debug) {
        System.err.println("obj. function: " + Utils.doubleToString(m_f, 10, 7));
        
        System.err.println("threshold: " + Utils.doubleToString(fold + m_ALF * alam * m_Slope, 10, 7));
      }
      

      if (m_f <= fold + m_ALF * alam * m_Slope) {
        if (m_Debug)
          System.err.println("Sufficient function decrease (alpha condition): ");
        double[] newGrad = evaluateGradient(x);
        newSlope = 0.0D; for (i = 0; i < len; i++) {
          if (isFixed[i] == 0)
            newSlope += newGrad[i] * direct[i];
        }
        if (newSlope >= m_BETA * m_Slope) {
          if (m_Debug) {
            System.err.println("Increasing derivatives (beta condition): ");
          }
          if ((fixedOne != -1) && (alam >= alpha)) {
            if (direct[fixedOne] > 0.0D) {
              x[fixedOne] = nwsBounds[1][fixedOne];
              nwsBounds[1][fixedOne] = NaN.0D;
            }
            else {
              x[fixedOne] = nwsBounds[0][fixedOne];
              nwsBounds[0][fixedOne] = NaN.0D;
            }
            
            if (m_Debug) {
              System.err.println("Fix variable " + fixedOne + " to bound " + x[fixedOne] + " from value " + xold[fixedOne]);
            }
            
            isFixed[fixedOne] = true;
            wsBdsIndx.addElement(fixedOne);
          }
          return x;
        }
        if (k == 0)
        {
          double upper = Math.min(alpha, maxalam);
          if (m_Debug)
            System.err.println("Alpha condition holds, increase alpha... ");
          while ((alam < upper) && (m_f <= fold + m_ALF * alam * m_Slope)) {
            lo = alam;
            flo = m_f;
            alam *= 2.0D;
            if (alam >= upper) {
              alam = upper;
            }
            for (i = 0; i < len; i++)
              if (isFixed[i] == 0)
                xold[i] += alam * direct[i];
            m_f = objectiveFunction(x);
            if (Double.isNaN(m_f)) {
              throw new Exception("Objective function value is NaN!");
            }
            newGrad = evaluateGradient(x);
            newSlope = 0.0D; for (i = 0; i < len; i++) {
              if (isFixed[i] == 0)
                newSlope += newGrad[i] * direct[i];
            }
            if (newSlope >= m_BETA * m_Slope) {
              if (m_Debug) {
                System.err.println("Increasing derivatives (beta condition): \nnewSlope = " + Utils.doubleToString(newSlope, 10, 7));
              }
              
              if ((fixedOne != -1) && (alam >= alpha)) {
                if (direct[fixedOne] > 0.0D) {
                  x[fixedOne] = nwsBounds[1][fixedOne];
                  nwsBounds[1][fixedOne] = NaN.0D;
                }
                else {
                  x[fixedOne] = nwsBounds[0][fixedOne];
                  nwsBounds[0][fixedOne] = NaN.0D;
                }
                
                if (m_Debug) {
                  System.err.println("Fix variable " + fixedOne + " to bound " + x[fixedOne] + " from value " + xold[fixedOne]);
                }
                
                isFixed[fixedOne] = true;
                wsBdsIndx.addElement(fixedOne);
              }
              return x;
            }
          }
          hi = alam;
          fhi = m_f;
          break;
        }
        
        if (m_Debug)
          System.err.println("Alpha condition holds.");
        hi = alam2;lo = alam;flo = m_f;
        break;
      }
      
      if (alam < alamin) {
        if (initF < fold) {
          alam = Math.min(1.0D, alpha);
          for (i = 0; i < len; i++) {
            if (isFixed[i] == 0)
              xold[i] += alam * direct[i];
          }
          if (m_Debug) {
            System.err.println("No feasible lambda: still take alpha=" + alam);
          }
          
          if ((fixedOne != -1) && (alam >= alpha)) {
            if (direct[fixedOne] > 0.0D) {
              x[fixedOne] = nwsBounds[1][fixedOne];
              nwsBounds[1][fixedOne] = NaN.0D;
            }
            else {
              x[fixedOne] = nwsBounds[0][fixedOne];
              nwsBounds[0][fixedOne] = NaN.0D;
            }
            
            if (m_Debug) {
              System.err.println("Fix variable " + fixedOne + " to bound " + x[fixedOne] + " from value " + xold[fixedOne]);
            }
            
            isFixed[fixedOne] = true;
            wsBdsIndx.addElement(fixedOne);
          }
        }
        else {
          for (i = 0; i < len; i++)
            x[i] = xold[i];
          m_f = fold;
          if (m_Debug) {
            System.err.println("Cannot find feasible lambda");
          }
        }
        return x; }
      double tmplam;
      double tmplam;
      if (k == 0) {
        if (!Double.isInfinite(initF)) {
          initF = m_f;
        }
        tmplam = -0.5D * alam * m_Slope / ((m_f - fold) / alam - m_Slope);
      }
      else {
        double rhs1 = m_f - fold - alam * m_Slope;
        double rhs2 = fhi - fold - alam2 * m_Slope;
        double a = (rhs1 / (alam * alam) - rhs2 / (alam2 * alam2)) / (alam - alam2);
        double b = (-alam2 * rhs1 / (alam * alam) + alam * rhs2 / (alam2 * alam2)) / (alam - alam2);
        double tmplam; if (a == 0.0D) { tmplam = -m_Slope / (2.0D * b);
        } else {
          disc = b * b - 3.0D * a * m_Slope;
          if (disc < 0.0D) disc = 0.0D;
          double numerator = -b + Math.sqrt(disc);
          if (numerator >= Double.MAX_VALUE) {
            numerator = Double.MAX_VALUE;
            if (m_Debug)
              System.err.print("-b+sqrt(disc) too large! Set it to MAX_VALUE.");
          }
          tmplam = numerator / (3.0D * a);
        }
        if (m_Debug) {
          System.err.print("Cubic interpolation: \na:   " + Utils.doubleToString(a, 10, 7) + "\n" + "b:   " + Utils.doubleToString(b, 10, 7) + "\n" + "disc:   " + Utils.doubleToString(disc, 10, 7) + "\n" + "tmplam:   " + tmplam + "\n" + "alam:   " + Utils.doubleToString(alam, 10, 7) + "\n");
        }
        



        if (tmplam > 0.5D * alam) {
          tmplam = 0.5D * alam;
        }
      }
      alam2 = alam;
      fhi = m_f;
      alam = Math.max(tmplam, 0.1D * alam);
      
      if (alam > alpha) {
        throw new Exception("Sth. wrong in lnsrch:Lambda infeasible!(lambda=" + alam + ", alpha=" + alpha + ", upper=" + tmplam + "|" + -alpha * m_Slope / (2.0D * ((m_f - fold) / alpha - m_Slope)) + ", m_f=" + m_f + ", fold=" + fold + ", slope=" + m_Slope);
      }
    }
    



    double[] newGrad;
    


    double ldiff = hi - lo;
    if (m_Debug) {
      System.err.println("Last stage of searching for beta condition (alam between " + Utils.doubleToString(lo, 10, 7) + " and " + Utils.doubleToString(hi, 10, 7) + ")...\n" + "Quadratic Interpolation(QI):\n" + "Last newSlope = " + Utils.doubleToString(newSlope, 10, 7));
    }
    



    while ((newSlope < m_BETA * m_Slope) && (ldiff >= alamin)) {
      double lincr = -0.5D * newSlope * ldiff * ldiff / (fhi - flo - newSlope * ldiff);
      
      if (m_Debug) {
        System.err.println("fhi = " + fhi + "\n" + "flo = " + flo + "\n" + "ldiff = " + ldiff + "\n" + "lincr (using QI) = " + lincr + "\n");
      }
      


      if (lincr < 0.2D * ldiff) lincr = 0.2D * ldiff;
      alam = lo + lincr;
      if (alam >= hi) {
        alam = hi;
        lincr = ldiff;
      }
      for (i = 0; i < len; i++)
        if (isFixed[i] == 0)
          xold[i] += alam * direct[i];
      m_f = objectiveFunction(x);
      if (Double.isNaN(m_f)) {
        throw new Exception("Objective function value is NaN!");
      }
      if (m_f > fold + m_ALF * alam * m_Slope)
      {
        ldiff = lincr;
        fhi = m_f;
      }
      else {
        newGrad = evaluateGradient(x);
        newSlope = 0.0D; for (i = 0; i < len; i++) {
          if (isFixed[i] == 0)
            newSlope += newGrad[i] * direct[i];
        }
        if (newSlope < m_BETA * m_Slope)
        {
          lo = alam;
          ldiff -= lincr;
          flo = m_f;
        }
      }
    }
    
    if (newSlope < m_BETA * m_Slope) {
      if (m_Debug)
        System.err.println("Beta condition cannot be satisfied, take alpha condition");
      alam = lo;
      for (i = 0; i < len; i++)
        if (isFixed[i] == 0)
          xold[i] += alam * direct[i];
      m_f = flo;
    }
    else if (m_Debug) {
      System.err.println("Both alpha and beta conditions are satisfied. alam=" + Utils.doubleToString(alam, 10, 7));
    }
    
    if ((fixedOne != -1) && (alam >= alpha)) {
      if (direct[fixedOne] > 0.0D) {
        x[fixedOne] = nwsBounds[1][fixedOne];
        nwsBounds[1][fixedOne] = NaN.0D;
      }
      else {
        x[fixedOne] = nwsBounds[0][fixedOne];
        nwsBounds[0][fixedOne] = NaN.0D;
      }
      
      if (m_Debug) {
        System.err.println("Fix variable " + fixedOne + " to bound " + x[fixedOne] + " from value " + xold[fixedOne]);
      }
      
      isFixed[fixedOne] = true;
      wsBdsIndx.addElement(fixedOne);
    }
    
    return x;
  }
  









  public double[] findArgmin(double[] initX, double[][] constraints)
    throws Exception
  {
    int l = initX.length;
    


    boolean[] isFixed = new boolean[l];
    double[][] nwsBounds = new double[2][l];
    
    DynamicIntArray wsBdsIndx = new DynamicIntArray(constraints.length);
    
    DynamicIntArray toFree = null;DynamicIntArray oldToFree = null;
    

    m_f = objectiveFunction(initX);
    if (Double.isNaN(m_f)) {
      throw new Exception("Objective function value is NaN!");
    }
    double sum = 0.0D;
    double[] grad = evaluateGradient(initX);
    double[] deltaGrad = new double[l];double[] deltaX = new double[l];
    double[] direct = new double[l];double[] x = new double[l];
    Matrix L = new Matrix(l, l);
    double[] D = new double[l];
    for (int i = 0; i < l; i++) {
      L.setRow(i, new double[l]);
      L.setElement(i, i, 1.0D);
      D[i] = 1.0D;
      direct[i] = (-grad[i]);
      sum += grad[i] * grad[i];
      x[i] = initX[i];
      nwsBounds[0][i] = constraints[0][i];
      nwsBounds[1][i] = constraints[1][i];
      isFixed[i] = false;
    }
    double stpmax = m_STPMX * Math.max(Math.sqrt(sum), l);
    

    for (int step = 0; step < m_MAXITS; step++) {
      if (m_Debug) {
        System.err.println("\nIteration # " + step + ":");
      }
      
      double[] oldX = x;
      double[] oldGrad = grad;
      

      if (m_Debug)
        System.err.println("Line search ... ");
      m_IsZeroStep = false;
      x = lnsrch(x, grad, direct, stpmax, isFixed, nwsBounds, wsBdsIndx);
      
      if (m_Debug) {
        System.err.println("Line search finished.");
      }
      if (m_IsZeroStep) {
        for (int f = 0; f < wsBdsIndx.size(); f++) {
          int idx = wsBdsIndx.elementAt(f);
          L.setRow(idx, new double[l]);
          L.setColumn(idx, new double[l]);
          D[idx] = 0.0D;
        }
        grad = evaluateGradient(x);
        step--;
      }
      else
      {
        boolean finish = false;
        double test = 0.0D;
        for (int h = 0; h < l; h++) {
          x[h] -= oldX[h];
          double tmp = Math.abs(deltaX[h]) / Math.max(Math.abs(x[h]), 1.0D);
          
          if (tmp > test) test = tmp;
        }
        if (test < m_Zero) {
          if (m_Debug)
            System.err.println("\nDeltaX converge: " + test);
          finish = true;
        }
        

        grad = evaluateGradient(x);
        test = 0.0D;
        double denom = 0.0D;double dxSq = 0.0D;double dgSq = 0.0D;double newlyBounded = 0.0D;
        for (int g = 0; g < l; g++) {
          if (isFixed[g] == 0) {
            grad[g] -= oldGrad[g];
            
            denom += deltaX[g] * deltaGrad[g];
            dxSq += deltaX[g] * deltaX[g];
            dgSq += deltaGrad[g] * deltaGrad[g];
          }
          else {
            newlyBounded += deltaX[g] * (grad[g] - oldGrad[g]);
          }
          

          double tmp = Math.abs(grad[g]) * Math.max(Math.abs(direct[g]), 1.0D) / Math.max(Math.abs(m_f), 1.0D);
          

          if (tmp > test) { test = tmp;
          }
        }
        if (test < m_Zero) {
          if (m_Debug)
            System.err.println("Gradient converge: " + test);
          finish = true;
        }
        

        if (m_Debug) {
          System.err.println("dg'*dx=" + (denom + newlyBounded));
        }
        if (Math.abs(denom + newlyBounded) < m_Zero) {
          finish = true;
        }
        int size = wsBdsIndx.size();
        boolean isUpdate = true;
        
        if (finish) {
          if (m_Debug) {
            System.err.println("Test any release possible ...");
          }
          if (toFree != null)
            oldToFree = (DynamicIntArray)toFree.copy();
          toFree = new DynamicIntArray(wsBdsIndx.size());
          
          for (int m = size - 1; m >= 0; m--) {
            int index = wsBdsIndx.elementAt(m);
            double[] hessian = evaluateHessian(x, index);
            double deltaL = 0.0D;
            if (hessian != null) {
              for (int mm = 0; mm < hessian.length; mm++) {
                if (isFixed[mm] == 0) {
                  deltaL += hessian[mm] * direct[mm];
                }
              }
            }
            
            double L1;
            if (x[index] >= constraints[1][index]) {
              L1 = -grad[index]; } else { double L1;
              if (x[index] <= constraints[0][index]) {
                L1 = grad[index];
              } else {
                throw new Exception("x[" + index + "] not fixed on the" + " bounds where it should have been!");
              }
            }
            double L1;
            double L2 = L1 + deltaL;
            if (m_Debug) {
              System.err.println("Variable " + index + ": Lagrangian=" + L1 + "|" + L2);
            }
            

            boolean isConverge = 2.0D * Math.abs(deltaL) < Math.min(Math.abs(L1), Math.abs(L2));
            

            if ((L1 * L2 > 0.0D) && (isConverge) && 
              (L2 < 0.0D)) {
              toFree.addElement(index);
              wsBdsIndx.removeElementAt(m);
              finish = false;
            }
            




            if ((hessian == null) && (toFree != null) && (toFree.equal(oldToFree))) {
              finish = true;
            }
          }
          if (finish) {
            if (m_Debug)
              System.err.println("Minimum found.");
            m_f = objectiveFunction(x);
            if (Double.isNaN(m_f))
              throw new Exception("Objective function value is NaN!");
            return x;
          }
          

          for (int mmm = 0; mmm < toFree.size(); mmm++) {
            int freeIndx = toFree.elementAt(mmm);
            isFixed[freeIndx] = false;
            if (x[freeIndx] <= constraints[0][freeIndx]) {
              nwsBounds[0][freeIndx] = constraints[0][freeIndx];
              if (m_Debug) {
                System.err.println("Free variable " + freeIndx + " from bound " + nwsBounds[0][freeIndx]);
              }
            }
            else
            {
              nwsBounds[1][freeIndx] = constraints[1][freeIndx];
              if (m_Debug) {
                System.err.println("Free variable " + freeIndx + " from bound " + nwsBounds[1][freeIndx]);
              }
            }
            
            L.setElement(freeIndx, freeIndx, 1.0D);
            D[freeIndx] = 1.0D;
            isUpdate = false;
          }
        }
        
        if (denom < Math.max(m_Zero * Math.sqrt(dxSq) * Math.sqrt(dgSq), m_Zero)) {
          if (m_Debug)
            System.err.println("dg'*dx negative!");
          isUpdate = false;
        }
        
        if (isUpdate)
        {

          double coeff = 1.0D / denom;
          updateCholeskyFactor(L, D, deltaGrad, coeff, isFixed);
          

          coeff = 1.0D / m_Slope;
          updateCholeskyFactor(L, D, oldGrad, coeff, isFixed);
        }
      }
      

      Matrix LD = new Matrix(l, l);
      double[] b = new double[l];
      
      for (int k = 0; k < l; k++) {
        if (isFixed[k] == 0) b[k] = (-grad[k]); else {
          b[k] = 0.0D;
        }
        for (int j = k; j < l; j++) {
          if ((isFixed[j] == 0) && (isFixed[k] == 0)) {
            LD.setElement(j, k, L.getElement(j, k) * D[k]);
          }
        }
      }
      
      double[] LDIR = solveTriangle(LD, b, true, isFixed);
      LD = null;
      
      for (int m = 0; m < LDIR.length; m++) {
        if (Double.isNaN(LDIR[m])) {
          throw new Exception("L*direct[" + m + "] is NaN!" + "|-g=" + b[m] + "|" + isFixed[m] + "|diag=" + D[m]);
        }
      }
      


      direct = solveTriangle(L, LDIR, false, isFixed);
      for (int m = 0; m < direct.length; m++) {
        if (Double.isNaN(direct[m])) {
          throw new Exception("direct is NaN!");
        }
      }
    }
    

    if (m_Debug) {
      System.err.println("Cannot find minimum -- too many interations!");
    }
    m_X = x;
    return null;
  }
  











  public static double[] solveTriangle(Matrix t, double[] b, boolean isLower, boolean[] isZero)
  {
    int n = b.length;
    double[] result = new double[n];
    if (isZero == null) {
      isZero = new boolean[n];
    }
    if (isLower) {
      for (int j = 0; 
          (j < n) && (isZero[j] != 0); j++) { result[j] = 0.0D;
      }
      if (j < n) {
        b[j] /= t.getElement(j, j);
        for (; 
            j < n; j++) {
          if (isZero[j] == 0) {
            double numerator = b[j];
            for (int k = 0; k < j; k++)
              numerator -= t.getElement(j, k) * result[k];
            result[j] = (numerator / t.getElement(j, j));
          }
          else {
            result[j] = 0.0D;
          }
        }
      }
    } else {
      for (int j = n - 1; 
          (j >= 0) && (isZero[j] != 0); j--) { result[j] = 0.0D;
      }
      if (j >= 0) {
        b[j] /= t.getElement(j, j);
        for (; 
            j >= 0; j--) {
          if (isZero[j] == 0) {
            double numerator = b[j];
            for (int k = j + 1; k < n; k++)
              numerator -= t.getElement(k, j) * result[k];
            result[j] = (numerator / t.getElement(j, j));
          }
          else {
            result[j] = 0.0D;
          }
        }
      }
    }
    return result;
  }
  















  protected void updateCholeskyFactor(Matrix L, double[] D, double[] v, double coeff, boolean[] isFixed)
    throws Exception
  {
    int n = v.length;
    double[] vp = new double[n];
    for (int i = 0; i < v.length; i++) {
      if (isFixed[i] == 0) {
        vp[i] = v[i];
      } else
        vp[i] = 0.0D;
    }
    if (coeff > 0.0D) {
      double t = coeff;
      for (int j = 0; j < n; j++) {
        if (isFixed[j] == 0)
        {
          double p = vp[j];
          double d = D[j];double dbarj = d + t * p * p;
          D[j] = dbarj;
          
          double b = p * t / dbarj;
          t *= d / dbarj;
          for (int r = j + 1; r < n; r++)
            if (isFixed[r] == 0) {
              double l = L.getElement(r, j);
              vp[r] -= p * l;
              L.setElement(r, j, l + b * vp[r]);
            }
            else {
              L.setElement(r, j, 0.0D);
            }
        }
      }
    } else {
      double[] P = solveTriangle(L, v, true, isFixed);
      double t = 0.0D;
      for (int i = 0; i < n; i++) {
        if (isFixed[i] == 0)
          t += P[i] * P[i] / D[i];
      }
      double sqrt = 1.0D + coeff * t;
      sqrt = sqrt < 0.0D ? 0.0D : Math.sqrt(sqrt);
      
      double alpha = coeff;double sigma = coeff / (1.0D + sqrt);
      
      for (int j = 0; j < n; j++) {
        if (isFixed[j] == 0)
        {
          double d = D[j];
          double p = P[j] * P[j] / d;
          double theta = 1.0D + sigma * p;
          t -= p;
          if (t < 0.0D) { t = 0.0D;
          }
          double plus = sigma * sigma * p * t;
          if ((j < n - 1) && (plus <= m_Zero))
            plus = m_Zero;
          double rho = theta * theta + plus;
          D[j] = (rho * d);
          
          if (Double.isNaN(D[j])) {
            throw new Exception("d[" + j + "] NaN! P=" + P[j] + ",d=" + d + ",t=" + t + ",p=" + p + ",sigma=" + sigma + ",sclar=" + coeff);
          }
          


          double b = alpha * P[j] / (rho * d);
          alpha /= rho;
          rho = Math.sqrt(rho);
          double sigmaOld = sigma;
          sigma *= (1.0D + rho) / (rho * (theta + rho));
          if ((j < n - 1) && ((Double.isNaN(sigma)) || (Double.isInfinite(sigma))))
          {
            throw new Exception("sigma NaN/Inf! rho=" + rho + ",theta=" + theta + ",P[" + j + "]=" + P[j] + ",p=" + p + ",d=" + d + ",t=" + t + ",oldsigma=" + sigmaOld);
          }
          


          for (int r = j + 1; r < n; r++) {
            if (isFixed[r] == 0) {
              double l = L.getElement(r, j);
              vp[r] -= P[j] * l;
              L.setElement(r, j, l + b * vp[r]);
            }
            else {
              L.setElement(r, j, 0.0D);
            }
          }
        }
      }
    }
  }
  
  protected abstract double objectiveFunction(double[] paramArrayOfDouble)
    throws Exception;
  
  protected abstract double[] evaluateGradient(double[] paramArrayOfDouble) throws Exception;
  
  private class DynamicIntArray implements RevisionHandler
  {
    private int[] m_Objects;
    private int m_Size = 0;
    

    private int m_CapacityIncrement = 1;
    

    private int m_CapacityMultiplier = 2;
    





    public DynamicIntArray(int capacity)
    {
      m_Objects = new int[capacity];
    }
    






    public final void addElement(int element)
    {
      if (m_Size == m_Objects.length)
      {
        int[] newObjects = new int[m_CapacityMultiplier * (m_Objects.length + m_CapacityIncrement)];
        

        System.arraycopy(m_Objects, 0, newObjects, 0, m_Size);
        m_Objects = newObjects;
      }
      m_Objects[m_Size] = element;
      m_Size += 1;
    }
    






    public final Object copy()
    {
      DynamicIntArray copy = new DynamicIntArray(Optimization.this, m_Objects.length);
      
      m_Size = m_Size;
      m_CapacityIncrement = m_CapacityIncrement;
      m_CapacityMultiplier = m_CapacityMultiplier;
      System.arraycopy(m_Objects, 0, m_Objects, 0, m_Size);
      return copy;
    }
    






    public final int elementAt(int index)
    {
      return m_Objects[index];
    }
    







    private boolean equal(DynamicIntArray b)
    {
      if ((b == null) || (size() != b.size())) {
        return false;
      }
      int size = size();
      

      int[] sorta = Utils.sort(m_Objects);int[] sortb = Utils.sort(m_Objects);
      for (int j = 0; j < size; j++) {
        if (m_Objects[sorta[j]] != m_Objects[sortb[j]])
          return false;
      }
      return true;
    }
    





    public final void removeElementAt(int index)
    {
      System.arraycopy(m_Objects, index + 1, m_Objects, index, m_Size - index - 1);
      
      m_Size -= 1;
    }
    




    public final void removeAllElements()
    {
      m_Objects = new int[m_Objects.length];
      m_Size = 0;
    }
    





    public final int size()
    {
      return m_Size;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.9 $");
    }
  }
}
