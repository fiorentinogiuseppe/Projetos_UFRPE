package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
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
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.MultiInstanceToPropositional;
















































































public class MIBoost
  extends SingleClassifierEnhancer
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -3808427225599279539L;
  protected Classifier[] m_Models;
  protected int m_NumClasses;
  protected int[] m_Classes;
  protected Instances m_Attributes;
  private int m_NumIterations;
  protected double[] m_Beta;
  protected int m_MaxIterations;
  protected int m_DiscretizeBin;
  protected Discretize m_Filter;
  protected MultiInstanceToPropositional m_ConvertToSI;
  
  public MIBoost()
  {
    m_NumIterations = 100;
    




    m_MaxIterations = 10;
    

    m_DiscretizeBin = 0;
    

    m_Filter = null;
    

    m_ConvertToSI = new MultiInstanceToPropositional();
  }
  




  public String globalInfo()
  {
    return "MI AdaBoost method, considers the geometric mean of posterior of instances inside a bag (arithmatic mean of log-posterior) and the expectation for a bag is taken inside the loss function.\n\nFor more information about Adaboost, see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Yoav Freund and Robert E. Schapire");
    result.setValue(TechnicalInformation.Field.TITLE, "Experiments with a new boosting algorithm");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Thirteenth International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "148-156");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    


    result.addElement(new Option("\tThe number of bins in discretization\n\t(default 0, no discretization)", "B", 1, "-B <num>"));
    



    result.addElement(new Option("\tMaximum number of boost iterations.\n\t(default 10)", "R", 1, "-R <num>"));
    



    result.addElement(new Option("\tFull name of classifier to boost.\n\teg: weka.classifiers.bayes.NaiveBayes", "W", 1, "-W <class name>"));
    



    Enumeration enu = m_Classifier.listOptions();
    while (enu.hasMoreElements()) {
      result.addElement(enu.nextElement());
    }
    
    return result.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String bin = Utils.getOption('B', options);
    if (bin.length() != 0) {
      setDiscretizeBin(Integer.parseInt(bin));
    } else {
      setDiscretizeBin(0);
    }
    
    String boostIterations = Utils.getOption('R', options);
    if (boostIterations.length() != 0) {
      setMaxIterations(Integer.parseInt(boostIterations));
    } else {
      setMaxIterations(10);
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-R");
    result.add("" + getMaxIterations());
    
    result.add("-B");
    result.add("" + getDiscretizeBin());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String maxIterationsTipText()
  {
    return "The maximum number of boost iterations.";
  }
  




  public void setMaxIterations(int maxIterations)
  {
    m_MaxIterations = maxIterations;
  }
  





  public int getMaxIterations()
  {
    return m_MaxIterations;
  }
  





  public String discretizeBinTipText()
  {
    return "The number of bins in discretization.";
  }
  




  public void setDiscretizeBin(int bin)
  {
    m_DiscretizeBin = bin;
  }
  




  public int getDiscretizeBin()
  {
    return m_DiscretizeBin;
  }
  
  private class OptEng extends Optimization {
    private double[] weights;
    private double[] errs;
    
    private OptEng() {}
    
    public void setWeights(double[] w) { weights = w; }
    
    public void setErrs(double[] e)
    {
      errs = e;
    }
    




    protected double objectiveFunction(double[] x)
      throws Exception
    {
      double obj = 0.0D;
      for (int i = 0; i < weights.length; i++) {
        obj += weights[i] * Math.exp(x[0] * (2.0D * errs[i] - 1.0D));
        if (Double.isNaN(obj)) {
          throw new Exception("Objective function value is NaN!");
        }
      }
      return obj;
    }
    




    protected double[] evaluateGradient(double[] x)
      throws Exception
    {
      double[] grad = new double[1];
      for (int i = 0; i < weights.length; i++) {
        grad[0] += weights[i] * (2.0D * errs[i] - 1.0D) * Math.exp(x[0] * (2.0D * errs[i] - 1.0D));
        if (Double.isNaN(grad[0])) {
          throw new Exception("Gradient is NaN!");
        }
      }
      return grad;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9144 $");
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.disable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    if (super.getCapabilities().handles(Capabilities.Capability.BINARY_CLASS))
      result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  







  public void buildClassifier(Instances exps)
    throws Exception
  {
    getCapabilities().testWithFail(exps);
    

    Instances train = new Instances(exps);
    train.deleteWithMissingClass();
    
    m_NumClasses = train.numClasses();
    m_NumIterations = m_MaxIterations;
    
    if (m_Classifier == null)
      throw new Exception("A base classifier has not been specified!");
    if (!(m_Classifier instanceof WeightedInstancesHandler)) {
      throw new Exception("Base classifier cannot handle weighted instances!");
    }
    m_Models = Classifier.makeCopies(m_Classifier, getMaxIterations());
    if (m_Debug) {
      System.err.println("Base classifier: " + m_Classifier.getClass().getName());
    }
    m_Beta = new double[m_NumIterations];
    



    double N = train.numInstances();double sumNi = 0.0D;
    for (int i = 0; i < N; i++)
      sumNi += train.instance(i).relationalValue(1).numInstances();
    for (int i = 0; i < N; i++) {
      train.instance(i).setWeight(sumNi / N);
    }
    

    m_ConvertToSI.setInputFormat(train);
    Instances data = Filter.useFilter(train, m_ConvertToSI);
    data.deleteAttributeAt(0);
    


    if (m_DiscretizeBin > 0) {
      m_Filter = new Discretize();
      m_Filter.setInputFormat(new Instances(data, 0));
      m_Filter.setBins(m_DiscretizeBin);
      data = Filter.useFilter(data, m_Filter);
    }
    



    for (int m = 0; m < m_MaxIterations; m++) {
      if (m_Debug) {
        System.err.println("\nIteration " + m);
      }
      

      m_Models[m].buildClassifier(data);
      

      double[] err = new double[(int)N];double[] weights = new double[(int)N];
      boolean perfect = true;boolean tooWrong = true;
      int dataIdx = 0;
      for (int n = 0; n < N; n++) {
        Instance exn = train.instance(n);
        

        double nn = exn.relationalValue(1).numInstances();
        for (int p = 0; p < nn; p++) {
          Instance testIns = data.instance(dataIdx++);
          if ((int)m_Models[m].classifyInstance(testIns) != (int)exn.classValue())
          {
            err[n] += 1.0D; }
        }
        weights[n] = exn.weight();
        err[n] /= nn;
        if (err[n] > 0.5D)
          perfect = false;
        if (err[n] < 0.5D) {
          tooWrong = false;
        }
      }
      if ((perfect) || (tooWrong)) {
        if (m == 0) {
          m_Beta[m] = 1.0D;
        } else
          m_Beta[m] = 0.0D;
        m_NumIterations = (m + 1);
        if (!m_Debug) break; System.err.println("No errors"); break;
      }
      

      double[] x = new double[1];
      x[0] = 0.0D;
      double[][] b = new double[2][x.length];
      b[0][0] = NaN.0D;
      b[1][0] = NaN.0D;
      
      OptEng opt = new OptEng(null);
      opt.setWeights(weights);
      opt.setErrs(err);
      
      if (m_Debug)
        System.out.println("Start searching for c... ");
      x = opt.findArgmin(x, b);
      while (x == null) {
        x = opt.getVarbValues();
        if (m_Debug)
          System.out.println("200 iterations finished, not enough!");
        x = opt.findArgmin(x, b);
      }
      if (m_Debug)
        System.out.println("Finished.");
      m_Beta[m] = x[0];
      
      if (m_Debug) {
        System.err.println("c = " + m_Beta[m]);
      }
      
      if ((Double.isInfinite(m_Beta[m])) || (Utils.smOrEq(m_Beta[m], 0.0D)))
      {

        if (m == 0) {
          m_Beta[m] = 1.0D;
        } else
          m_Beta[m] = 0.0D;
        m_NumIterations = (m + 1);
        if (!m_Debug) break;
        System.err.println("Errors out of range!"); break;
      }
      


      dataIdx = 0;
      double totWeights = 0.0D;
      for (int r = 0; r < N; r++) {
        Instance exr = train.instance(r);
        exr.setWeight(weights[r] * Math.exp(m_Beta[m] * (2.0D * err[r] - 1.0D)));
        totWeights += exr.weight();
      }
      
      if (m_Debug) {
        System.err.println("Total weights = " + totWeights);
      }
      for (int r = 0; r < N; r++) {
        Instance exr = train.instance(r);
        double num = exr.relationalValue(1).numInstances();
        exr.setWeight(sumNi * exr.weight() / totWeights);
        

        for (int s = 0; s < num; s++) {
          Instance inss = data.instance(dataIdx);
          inss.setWeight(exr.weight() / num);
          


          if (Double.isNaN(inss.weight()))
            throw new Exception("instance " + s + " in bag " + r + " has weight NaN!");
          dataIdx++;
        }
      }
    }
  }
  









  public double[] distributionForInstance(Instance exmp)
    throws Exception
  {
    double[] rt = new double[m_NumClasses];
    
    Instances insts = new Instances(exmp.dataset(), 0);
    insts.add(exmp);
    

    insts = Filter.useFilter(insts, m_ConvertToSI);
    insts.deleteAttributeAt(0);
    
    double n = insts.numInstances();
    
    if (m_DiscretizeBin > 0) {
      insts = Filter.useFilter(insts, m_Filter);
    }
    for (int y = 0; y < n; y++) {
      Instance ins = insts.instance(y);
      for (int x = 0; x < m_NumIterations; x++) {
        rt[((int)m_Models[x].classifyInstance(ins))] += m_Beta[x] / n;
      }
    }
    
    for (int i = 0; i < rt.length; i++) {
      rt[i] = Math.exp(rt[i]);
    }
    Utils.normalize(rt);
    return rt;
  }
  





  public String toString()
  {
    if (m_Models == null) {
      return "No model built yet!";
    }
    StringBuffer text = new StringBuffer();
    text.append("MIBoost: number of bins in discretization = " + m_DiscretizeBin + "\n");
    if (m_NumIterations == 0) {
      text.append("No model built yet.\n");
    } else if (m_NumIterations == 1) {
      text.append("No boosting possible, one classifier used: Weight = " + Utils.roundDouble(m_Beta[0], 2) + "\n");
      
      text.append("Base classifiers:\n" + m_Models[0].toString());
    } else {
      text.append("Base classifiers and their weights: \n");
      for (int i = 0; i < m_NumIterations; i++) {
        text.append("\n\n" + i + ": Weight = " + Utils.roundDouble(m_Beta[i], 2) + "\nBase classifier:\n" + m_Models[i].toString());
      }
    }
    

    text.append("\n\nNumber of performed Iterations: " + m_NumIterations + "\n");
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MIBoost(), argv);
  }
}
