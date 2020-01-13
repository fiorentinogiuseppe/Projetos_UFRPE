package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Optimization;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;





























































































public class Logistic
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 3932117032546553727L;
  protected double[][] m_Par;
  protected double[][] m_Data;
  protected int m_NumPredictors;
  protected int m_ClassIndex;
  protected int m_NumClasses;
  protected double m_Ridge;
  private RemoveUseless m_AttFilter;
  private NominalToBinary m_NominalToBinary;
  private ReplaceMissingValues m_ReplaceMissingValues;
  protected boolean m_Debug;
  protected double m_LL;
  private int m_MaxIts;
  private Instances m_structure;
  
  public Logistic()
  {
    m_Ridge = 1.0E-8D;
    
















    m_MaxIts = -1;
  }
  





  public String globalInfo()
  {
    return "Class for building and using a multinomial logistic regression model with a ridge estimator.\n\nThere are some modifications, however, compared to the paper of leCessie and van Houwelingen(1992): \n\nIf there are k classes for n instances with m attributes, the parameter matrix B to be calculated will be an m*(k-1) matrix.\n\nThe probability for class j with the exception of the last class is\n\nPj(Xi) = exp(XiBj)/((sum[j=1..(k-1)]exp(Xi*Bj))+1) \n\nThe last class has probability\n\n1-(sum[j=1..(k-1)]Pj(Xi)) \n\t= 1/((sum[j=1..(k-1)]exp(Xi*Bj))+1)\n\nThe (negative) multinomial log-likelihood is thus: \n\nL = -sum[i=1..n]{\n\tsum[j=1..(k-1)](Yij * ln(Pj(Xi)))\n\t+(1 - (sum[j=1..(k-1)]Yij)) \n\t* ln(1 - sum[j=1..(k-1)]Pj(Xi))\n\t} + ridge * (B^2)\n\nIn order to find the matrix B for which L is minimised, a Quasi-Newton Method is used to search for the optimized values of the m*(k-1) variables.  Note that before we use the optimization procedure, we 'squeeze' the matrix B into a m*(k-1) vector.  For details of the optimization procedure, please check weka.core.Optimization class.\n\nAlthough original Logistic Regression does not deal with instance weights, we modify the algorithm a little bit to handle the instance weights.\n\nFor more information see:\n\n" + getTechnicalInformation().toString() + "\n\n" + "Note: Missing values are replaced using a ReplaceMissingValuesFilter, and " + "nominal attributes are transformed into numeric attributes using a " + "NominalToBinaryFilter.";
  }
  



































  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "le Cessie, S. and van Houwelingen, J.C.");
    result.setValue(TechnicalInformation.Field.YEAR, "1992");
    result.setValue(TechnicalInformation.Field.TITLE, "Ridge Estimators in Logistic Regression");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Applied Statistics");
    result.setValue(TechnicalInformation.Field.VOLUME, "41");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "191-201");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    newVector.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    
    newVector.addElement(new Option("\tSet the ridge in the log-likelihood.", "R", 1, "-R <ridge>"));
    
    newVector.addElement(new Option("\tSet the maximum number of iterations (default -1, until convergence).", "M", 1, "-M <number>"));
    

    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String ridgeString = Utils.getOption('R', options);
    if (ridgeString.length() != 0) {
      m_Ridge = Double.parseDouble(ridgeString);
    } else {
      m_Ridge = 1.0E-8D;
    }
    String maxItsString = Utils.getOption('M', options);
    if (maxItsString.length() != 0) {
      m_MaxIts = Integer.parseInt(maxItsString);
    } else {
      m_MaxIts = -1;
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    if (getDebug())
      options[(current++)] = "-D";
    options[(current++)] = "-R";
    options[(current++)] = ("" + m_Ridge);
    options[(current++)] = "-M";
    options[(current++)] = ("" + m_MaxIts);
    while (current < options.length)
      options[(current++)] = "";
    return options;
  }
  




  public String debugTipText()
  {
    return "Output debug information to the console.";
  }
  




  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  




  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public String ridgeTipText()
  {
    return "Set the Ridge value in the log-likelihood.";
  }
  




  public void setRidge(double ridge)
  {
    m_Ridge = ridge;
  }
  




  public double getRidge()
  {
    return m_Ridge;
  }
  




  public String maxItsTipText()
  {
    return "Maximum number of iterations to perform.";
  }
  





  public int getMaxIts()
  {
    return m_MaxIts;
  }
  





  public void setMaxIts(int newMaxIts)
  {
    m_MaxIts = newMaxIts;
  }
  

  private class OptEng
    extends Optimization
  {
    private double[] weights;
    
    private int[] cls;
    
    private OptEng() {}
    
    public void setWeights(double[] w)
    {
      weights = w;
    }
    



    public void setClassLabels(int[] c)
    {
      cls = c;
    }
    




    protected double objectiveFunction(double[] x)
    {
      double nll = 0.0D;
      int dim = m_NumPredictors + 1;
      
      for (int i = 0; i < cls.length; i++)
      {
        double[] exp = new double[m_NumClasses - 1];
        
        for (int offset = 0; offset < m_NumClasses - 1; offset++) {
          int index = offset * dim;
          for (int j = 0; j < dim; j++)
            exp[offset] += m_Data[i][j] * x[(index + j)];
        }
        double max = exp[Utils.maxIndex(exp)];
        double denom = Math.exp(-max);
        double num;
        double num; if (cls[i] == m_NumClasses - 1) {
          num = -max;
        } else {
          num = exp[cls[i]] - max;
        }
        for (int offset = 0; offset < m_NumClasses - 1; offset++) {
          denom += Math.exp(exp[offset] - max);
        }
        
        nll -= weights[i] * (num - Math.log(denom));
      }
      

      for (int offset = 0; offset < m_NumClasses - 1; offset++) {
        for (int r = 1; r < dim; r++) {
          nll += m_Ridge * x[(offset * dim + r)] * x[(offset * dim + r)];
        }
      }
      return nll;
    }
    




    protected double[] evaluateGradient(double[] x)
    {
      double[] grad = new double[x.length];
      int dim = m_NumPredictors + 1;
      
      for (int i = 0; i < cls.length; i++) {
        double[] num = new double[m_NumClasses - 1];
        
        for (int offset = 0; offset < m_NumClasses - 1; offset++) {
          double exp = 0.0D;
          int index = offset * dim;
          for (int j = 0; j < dim; j++)
            exp += m_Data[i][j] * x[(index + j)];
          num[offset] = exp;
        }
        
        double max = num[Utils.maxIndex(num)];
        double denom = Math.exp(-max);
        for (int offset = 0; offset < m_NumClasses - 1; offset++) {
          num[offset] = Math.exp(num[offset] - max);
          denom += num[offset];
        }
        Utils.normalize(num, denom);
        


        for (int offset = 0; offset < m_NumClasses - 1; offset++) {
          int index = offset * dim;
          double firstTerm = weights[i] * num[offset];
          for (int q = 0; q < dim; q++) {
            grad[(index + q)] += firstTerm * m_Data[i][q];
          }
        }
        
        if (cls[i] != m_NumClasses - 1) {
          for (int p = 0; p < dim; p++) {
            grad[(cls[i] * dim + p)] -= weights[i] * m_Data[i][p];
          }
        }
      }
      

      for (int offset = 0; offset < m_NumClasses - 1; offset++) {
        for (int r = 1; r < dim; r++) {
          grad[(offset * dim + r)] += 2.0D * m_Ridge * x[(offset * dim + r)];
        }
      }
      return grad;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5523 $");
    }
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
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances train)
    throws Exception
  {
    getCapabilities().testWithFail(train);
    

    train = new Instances(train);
    train.deleteWithMissingClass();
    

    m_ReplaceMissingValues = new ReplaceMissingValues();
    m_ReplaceMissingValues.setInputFormat(train);
    train = Filter.useFilter(train, m_ReplaceMissingValues);
    

    m_AttFilter = new RemoveUseless();
    m_AttFilter.setInputFormat(train);
    train = Filter.useFilter(train, m_AttFilter);
    

    m_NominalToBinary = new NominalToBinary();
    m_NominalToBinary.setInputFormat(train);
    train = Filter.useFilter(train, m_NominalToBinary);
    

    m_structure = new Instances(train, 0);
    

    m_ClassIndex = train.classIndex();
    m_NumClasses = train.numClasses();
    
    int nK = m_NumClasses - 1;
    int nR = this.m_NumPredictors = train.numAttributes() - 1;
    int nC = train.numInstances();
    
    m_Data = new double[nC][nR + 1];
    int[] Y = new int[nC];
    double[] xMean = new double[nR + 1];
    double[] xSD = new double[nR + 1];
    double[] sY = new double[nK + 1];
    double[] weights = new double[nC];
    double totWeights = 0.0D;
    m_Par = new double[nR + 1][nK];
    
    if (m_Debug) {
      System.out.println("Extracting data...");
    }
    
    for (int i = 0; i < nC; i++)
    {
      Instance current = train.instance(i);
      Y[i] = ((int)current.classValue());
      weights[i] = current.weight();
      totWeights += weights[i];
      
      m_Data[i][0] = 1.0D;
      int j = 1;
      for (int k = 0; k <= nR; k++) {
        if (k != m_ClassIndex) {
          double x = current.value(k);
          m_Data[i][j] = x;
          xMean[j] += weights[i] * x;
          xSD[j] += weights[i] * x * x;
          j++;
        }
      }
      

      sY[Y[i]] += 1.0D;
    }
    
    if ((totWeights <= 1.0D) && (nC > 1)) {
      throw new Exception("Sum of weights of instances less than 1, please reweight!");
    }
    xMean[0] = 0.0D;xSD[0] = 1.0D;
    for (int j = 1; j <= nR; j++) {
      xMean[j] /= totWeights;
      if (totWeights > 1.0D) {
        xSD[j] = Math.sqrt(Math.abs(xSD[j] - totWeights * xMean[j] * xMean[j]) / (totWeights - 1.0D));
      } else {
        xSD[j] = 0.0D;
      }
    }
    if (m_Debug)
    {
      System.out.println("Descriptives...");
      for (int m = 0; m <= nK; m++)
        System.out.println(sY[m] + " cases have class " + m);
      System.out.println("\n Variable     Avg       SD    ");
      for (int j = 1; j <= nR; j++) {
        System.out.println(Utils.doubleToString(j, 8, 4) + Utils.doubleToString(xMean[j], 10, 4) + Utils.doubleToString(xSD[j], 10, 4));
      }
    }
    



    for (int i = 0; i < nC; i++) {
      for (int j = 0; j <= nR; j++) {
        if (xSD[j] != 0.0D) {
          m_Data[i][j] = ((m_Data[i][j] - xMean[j]) / xSD[j]);
        }
      }
    }
    
    if (m_Debug) {
      System.out.println("\nIteration History...");
    }
    
    double[] x = new double[(nR + 1) * nK];
    double[][] b = new double[2][x.length];
    

    for (int p = 0; p < nK; p++) {
      int offset = p * (nR + 1);
      x[offset] = (Math.log(sY[p] + 1.0D) - Math.log(sY[nK] + 1.0D));
      b[0][offset] = NaN.0D;
      b[1][offset] = NaN.0D;
      for (int q = 1; q <= nR; q++) {
        x[(offset + q)] = 0.0D;
        b[0][(offset + q)] = NaN.0D;
        b[1][(offset + q)] = NaN.0D;
      }
    }
    
    OptEng opt = new OptEng(null);
    opt.setDebug(m_Debug);
    opt.setWeights(weights);
    opt.setClassLabels(Y);
    
    if (m_MaxIts == -1) {
      x = opt.findArgmin(x, b);
      while (x == null) {
        x = opt.getVarbValues();
        if (m_Debug)
          System.out.println("200 iterations finished, not enough!");
        x = opt.findArgmin(x, b);
      }
      if (m_Debug) {
        System.out.println(" -------------<Converged>--------------");
      }
    } else {
      opt.setMaxIteration(m_MaxIts);
      x = opt.findArgmin(x, b);
      if (x == null) {
        x = opt.getVarbValues();
      }
    }
    m_LL = (-opt.getMinFunction());
    

    m_Data = ((double[][])null);
    

    for (int i = 0; i < nK; i++) {
      m_Par[0][i] = x[(i * (nR + 1))];
      for (int j = 1; j <= nR; j++) {
        m_Par[j][i] = x[(i * (nR + 1) + j)];
        if (xSD[j] != 0.0D) {
          m_Par[j][i] /= xSD[j];
          m_Par[0][i] -= m_Par[j][i] * xMean[j];
        }
      }
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_ReplaceMissingValues.input(instance);
    instance = m_ReplaceMissingValues.output();
    m_AttFilter.input(instance);
    instance = m_AttFilter.output();
    m_NominalToBinary.input(instance);
    instance = m_NominalToBinary.output();
    

    double[] instDat = new double[m_NumPredictors + 1];
    int j = 1;
    instDat[0] = 1.0D;
    for (int k = 0; k <= m_NumPredictors; k++) {
      if (k != m_ClassIndex) {
        instDat[(j++)] = instance.value(k);
      }
    }
    
    double[] distribution = evaluateProbability(instDat);
    return distribution;
  }
  





  private double[] evaluateProbability(double[] data)
  {
    double[] prob = new double[m_NumClasses];
    double[] v = new double[m_NumClasses];
    

    for (int j = 0; j < m_NumClasses - 1; j++) {
      for (int k = 0; k <= m_NumPredictors; k++) {
        v[j] += m_Par[k][j] * data[k];
      }
    }
    v[(m_NumClasses - 1)] = 0.0D;
    

    for (int m = 0; m < m_NumClasses; m++) {
      double sum = 0.0D;
      for (int n = 0; n < m_NumClasses - 1; n++)
        sum += Math.exp(v[n] - v[m]);
      prob[m] = (1.0D / (sum + Math.exp(-v[m])));
    }
    
    return prob;
  }
  






  public double[][] coefficients()
  {
    return m_Par;
  }
  




  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    
    String result = "";
    temp.append("Logistic Regression with ridge parameter of " + m_Ridge);
    if (m_Par == null) {
      return result + ": No model built yet.";
    }
    

    int attLength = 0;
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      if ((i != m_structure.classIndex()) && (m_structure.attribute(i).name().length() > attLength))
      {
        attLength = m_structure.attribute(i).name().length();
      }
    }
    
    if ("Intercept".length() > attLength) {
      attLength = "Intercept".length();
    }
    
    if ("Variable".length() > attLength) {
      attLength = "Variable".length();
    }
    attLength += 2;
    
    int colWidth = 0;
    
    for (int i = 0; i < m_structure.classAttribute().numValues() - 1; i++) {
      if (m_structure.classAttribute().value(i).length() > colWidth) {
        colWidth = m_structure.classAttribute().value(i).length();
      }
    }
    

    for (int j = 1; j <= m_NumPredictors; j++) {
      for (int k = 0; k < m_NumClasses - 1; k++) {
        if (Utils.doubleToString(m_Par[j][k], 12, 4).trim().length() > colWidth) {
          colWidth = Utils.doubleToString(m_Par[j][k], 12, 4).trim().length();
        }
        double ORc = Math.exp(m_Par[j][k]);
        String t = " " + (ORc > 1.0E10D ? "" + ORc : Utils.doubleToString(ORc, 12, 4));
        if (t.trim().length() > colWidth) {
          colWidth = t.trim().length();
        }
      }
    }
    
    if ("Class".length() > colWidth) {
      colWidth = "Class".length();
    }
    colWidth += 2;
    

    temp.append("\nCoefficients...\n");
    temp.append(Utils.padLeft(" ", attLength) + Utils.padLeft("Class", colWidth) + "\n");
    temp.append(Utils.padRight("Variable", attLength));
    
    for (int i = 0; i < m_NumClasses - 1; i++) {
      String className = m_structure.classAttribute().value(i);
      temp.append(Utils.padLeft(className, colWidth));
    }
    temp.append("\n");
    int separatorL = attLength + (m_NumClasses - 1) * colWidth;
    for (int i = 0; i < separatorL; i++) {
      temp.append("=");
    }
    temp.append("\n");
    
    int j = 1;
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      if (i != m_structure.classIndex()) {
        temp.append(Utils.padRight(m_structure.attribute(i).name(), attLength));
        for (int k = 0; k < m_NumClasses - 1; k++) {
          temp.append(Utils.padLeft(Utils.doubleToString(m_Par[j][k], 12, 4).trim(), colWidth));
        }
        temp.append("\n");
        j++;
      }
    }
    
    temp.append(Utils.padRight("Intercept", attLength));
    for (int k = 0; k < m_NumClasses - 1; k++) {
      temp.append(Utils.padLeft(Utils.doubleToString(m_Par[0][k], 10, 4).trim(), colWidth));
    }
    temp.append("\n");
    
    temp.append("\n\nOdds Ratios...\n");
    temp.append(Utils.padLeft(" ", attLength) + Utils.padLeft("Class", colWidth) + "\n");
    temp.append(Utils.padRight("Variable", attLength));
    
    for (int i = 0; i < m_NumClasses - 1; i++) {
      String className = m_structure.classAttribute().value(i);
      temp.append(Utils.padLeft(className, colWidth));
    }
    temp.append("\n");
    for (int i = 0; i < separatorL; i++) {
      temp.append("=");
    }
    temp.append("\n");
    
    j = 1;
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      if (i != m_structure.classIndex()) {
        temp.append(Utils.padRight(m_structure.attribute(i).name(), attLength));
        for (int k = 0; k < m_NumClasses - 1; k++) {
          double ORc = Math.exp(m_Par[j][k]);
          String ORs = " " + (ORc > 1.0E10D ? "" + ORc : Utils.doubleToString(ORc, 12, 4));
          temp.append(Utils.padLeft(ORs.trim(), colWidth));
        }
        temp.append("\n");
        j++;
      }
    }
    
    return temp.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new Logistic(), argv);
  }
}
