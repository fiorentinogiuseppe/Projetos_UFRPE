package weka.classifiers.mi;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.SMOset;
import weka.classifiers.mi.supportVector.MIPolyKernel;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SerializedObject;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MultiInstanceToPropositional;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PropositionalToMultiInstance;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;





















































































































































public class MISMO
  extends Classifier
  implements WeightedInstancesHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -5834036950143719712L;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  
  public MISMO() {}
  
  public String globalInfo()
  {
    return "Implements John Platt's sequential minimal optimization algorithm for training a support vector classifier.\n\nThis implementation globally replaces all missing values and transforms nominal attributes into binary ones. It also normalizes all attributes by default. (In that case the coefficients in the output are based on the normalized data, not the original data --- this is important for interpreting the classifier.)\n\nMulti-class problems are solved using pairwise classification.\n\nTo obtain proper probability estimates, use the option that fits logistic regression models to the outputs of the support vector machine. In the multi-class case the predicted probabilities are coupled using Hastie and Tibshirani's pairwise coupling method.\n\nNote: for improved speed normalization should be turned off when operating on SparseInstances.\n\nFor more information on the SMO algorithm, see\n\n" + getTechnicalInformation().toString();
  }
  

























  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INCOLLECTION);
    result.setValue(TechnicalInformation.Field.AUTHOR, "J. Platt");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Machines using Sequential Minimal Optimization");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Kernel Methods - Support Vector Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "B. Schoelkopf and C. Burges and A. Smola");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "MIT Press");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "S.S. Keerthi and S.K. Shevade and C. Bhattacharyya and K.R.K. Murthy");
    additional.setValue(TechnicalInformation.Field.YEAR, "2001");
    additional.setValue(TechnicalInformation.Field.TITLE, "Improvements to Platt's SMO Algorithm for SVM Classifier Design");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Neural Computation");
    additional.setValue(TechnicalInformation.Field.VOLUME, "13");
    additional.setValue(TechnicalInformation.Field.NUMBER, "3");
    additional.setValue(TechnicalInformation.Field.PAGES, "637-649");
    
    return result;
  }
  


  protected class BinaryMISMO
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -7107082483475433531L;
    

    protected double[] m_alpha;
    

    protected double m_b;
    

    protected double m_bLow;
    

    protected double m_bUp;
    

    protected int m_iLow;
    

    protected int m_iUp;
    

    protected Instances m_data;
    

    protected double[] m_weights;
    

    protected double[] m_sparseWeights;
    
    protected int[] m_sparseIndices;
    
    protected Kernel m_kernel;
    
    protected double[] m_class;
    
    protected double[] m_errors;
    
    protected SMOset m_I0;
    
    protected SMOset m_I1;
    
    protected SMOset m_I2;
    
    protected SMOset m_I3;
    
    protected SMOset m_I4;
    
    protected SMOset m_supportVectors;
    
    protected Logistic m_logistic = null;
    

    protected double m_sumOfWeights = 0.0D;
    






    protected BinaryMISMO() {}
    





    protected void fitLogistic(Instances insts, int cl1, int cl2, int numFolds, Random random)
      throws Exception
    {
      FastVector atts = new FastVector(2);
      atts.addElement(new Attribute("pred"));
      FastVector attVals = new FastVector(2);
      attVals.addElement(insts.classAttribute().value(cl1));
      attVals.addElement(insts.classAttribute().value(cl2));
      atts.addElement(new Attribute("class", attVals));
      Instances data = new Instances("data", atts, insts.numInstances());
      data.setClassIndex(1);
      

      if (numFolds <= 0)
      {

        for (int j = 0; j < insts.numInstances(); j++) {
          Instance inst = insts.instance(j);
          double[] vals = new double[2];
          vals[0] = SVMOutput(-1, inst);
          if (inst.classValue() == cl2) {
            vals[1] = 1.0D;
          }
          data.add(new Instance(inst.weight(), vals));
        }
      }
      else
      {
        if (numFolds > insts.numInstances()) {
          numFolds = insts.numInstances();
        }
        

        insts = new Instances(insts);
        


        insts.randomize(random);
        insts.stratify(numFolds);
        for (int i = 0; i < numFolds; i++) {
          Instances train = insts.trainCV(numFolds, i, random);
          SerializedObject so = new SerializedObject(this);
          BinaryMISMO smo = (BinaryMISMO)so.getObject();
          smo.buildClassifier(train, cl1, cl2, false, -1, -1);
          Instances test = insts.testCV(numFolds, i);
          for (int j = 0; j < test.numInstances(); j++) {
            double[] vals = new double[2];
            vals[0] = smo.SVMOutput(-1, test.instance(j));
            if (test.instance(j).classValue() == cl2) {
              vals[1] = 1.0D;
            }
            data.add(new Instance(test.instance(j).weight(), vals));
          }
        }
      }
      

      m_logistic = new Logistic();
      m_logistic.buildClassifier(data);
    }
    




    public void setKernel(Kernel value)
    {
      m_kernel = value;
    }
    




    public Kernel getKernel()
    {
      return m_kernel;
    }
    













    protected void buildClassifier(Instances insts, int cl1, int cl2, boolean fitLogistic, int numFolds, int randomSeed)
      throws Exception
    {
      m_bUp = -1.0D;m_bLow = 1.0D;m_b = 0.0D;
      m_alpha = null;m_data = null;m_weights = null;m_errors = null;
      m_logistic = null;m_I0 = null;m_I1 = null;m_I2 = null;
      m_I3 = null;m_I4 = null;m_sparseWeights = null;m_sparseIndices = null;
      

      m_sumOfWeights = insts.sumOfWeights();
      

      m_class = new double[insts.numInstances()];
      m_iUp = -1;m_iLow = -1;
      for (int i = 0; i < m_class.length; i++) {
        if ((int)insts.instance(i).classValue() == cl1) {
          m_class[i] = -1.0D;m_iLow = i;
        } else if ((int)insts.instance(i).classValue() == cl2) {
          m_class[i] = 1.0D;m_iUp = i;
        } else {
          throw new Exception("This should never happen!");
        }
      }
      

      if ((m_iUp == -1) || (m_iLow == -1)) {
        if (m_iUp != -1) {
          m_b = -1.0D;
        } else if (m_iLow != -1) {
          m_b = 1.0D;
        } else {
          m_class = null;
          return;
        }
        m_supportVectors = new SMOset(0);
        m_alpha = new double[0];
        m_class = new double[0];
        

        if (fitLogistic) {
          fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
        }
        return;
      }
      

      m_data = insts;
      m_weights = null;
      

      m_alpha = new double[m_data.numInstances()];
      

      m_supportVectors = new SMOset(m_data.numInstances());
      m_I0 = new SMOset(m_data.numInstances());
      m_I1 = new SMOset(m_data.numInstances());
      m_I2 = new SMOset(m_data.numInstances());
      m_I3 = new SMOset(m_data.numInstances());
      m_I4 = new SMOset(m_data.numInstances());
      

      m_sparseWeights = null;
      m_sparseIndices = null;
      

      m_errors = new double[m_data.numInstances()];
      m_errors[m_iLow] = 1.0D;m_errors[m_iUp] = -1.0D;
      

      m_kernel.buildKernel(m_data);
      

      for (int i = 0; i < m_class.length; i++) {
        if (m_class[i] == 1.0D) {
          m_I1.insert(i);
        } else {
          m_I4.insert(i);
        }
      }
      

      int numChanged = 0;
      boolean examineAll = true;
      while ((numChanged > 0) || (examineAll)) {
        numChanged = 0;
        if (examineAll) {
          for (int i = 0; i < m_alpha.length; i++) {
            if (examineExample(i)) {
              numChanged++;
            }
            
          }
          
        } else {
          for (int i = 0; i < m_alpha.length; i++) {
            if ((m_alpha[i] > 0.0D) && (m_alpha[i] < m_C * m_data.instance(i).weight()))
            {
              if (examineExample(i)) {
                numChanged++;
              }
              

              if (m_bUp > m_bLow - 2.0D * m_tol) {
                numChanged = 0;
                break;
              }
            }
          }
        }
        







        if (examineAll) {
          examineAll = false;
        } else if (numChanged == 0) {
          examineAll = true;
        }
      }
      

      m_b = ((m_bLow + m_bUp) / 2.0D);
      

      m_kernel.clean();
      
      m_errors = null;
      m_I0 = (this.m_I1 = this.m_I2 = this.m_I3 = this.m_I4 = null);
      

      if (fitLogistic) {
        fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
      }
    }
    








    protected double SVMOutput(int index, Instance inst)
      throws Exception
    {
      double result = 0.0D;
      
      for (int i = m_supportVectors.getNext(-1); i != -1; 
          i = m_supportVectors.getNext(i)) {
        result += m_class[i] * m_alpha[i] * m_kernel.eval(index, i, inst);
      }
      result -= m_b;
      
      return result;
    }
    





    public String toString()
    {
      StringBuffer text = new StringBuffer();
      int printed = 0;
      
      if ((m_alpha == null) && (m_sparseWeights == null)) {
        return "BinaryMISMO: No model built yet.\n";
      }
      try {
        text.append("BinaryMISMO\n\n");
        
        for (int i = 0; i < m_alpha.length; i++) {
          if (m_supportVectors.contains(i)) {
            double val = m_alpha[i];
            if (m_class[i] == 1.0D) {
              if (printed > 0) {
                text.append(" + ");
              }
            } else {
              text.append(" - ");
            }
            text.append(Utils.doubleToString(val, 12, 4) + " * <");
            
            for (int j = 0; j < m_data.numAttributes(); j++) {
              if (j != m_data.classIndex()) {
                text.append(m_data.instance(i).toString(j));
              }
              if (j != m_data.numAttributes() - 1) {
                text.append(" ");
              }
            }
            text.append("> * X]\n");
            printed++;
          }
        }
        
        if (m_b > 0.0D) {
          text.append(" - " + Utils.doubleToString(m_b, 12, 4));
        } else {
          text.append(" + " + Utils.doubleToString(-m_b, 12, 4));
        }
        
        text.append("\n\nNumber of support vectors: " + m_supportVectors.numElements());
        
        int numEval = 0;
        int numCacheHits = -1;
        if (m_kernel != null)
        {
          numEval = m_kernel.numEvals();
          numCacheHits = m_kernel.numCacheHits();
        }
        text.append("\n\nNumber of kernel evaluations: " + numEval);
        if ((numCacheHits >= 0) && (numEval > 0))
        {
          double hitRatio = 1.0D - numEval * 1.0D / (numCacheHits + numEval);
          text.append(" (" + Utils.doubleToString(hitRatio * 100.0D, 7, 3).trim() + "% cached)");
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        
        return "Can't print BinaryMISMO classifier.";
      }
      
      return text.toString();
    }
    







    protected boolean examineExample(int i2)
      throws Exception
    {
      int i1 = -1;
      
      double y2 = m_class[i2];
      double F2; double F2; if (m_I0.contains(i2)) {
        F2 = m_errors[i2];
      } else {
        F2 = SVMOutput(i2, m_data.instance(i2)) + m_b - y2;
        m_errors[i2] = F2;
        

        if (((m_I1.contains(i2)) || (m_I2.contains(i2))) && (F2 < m_bUp)) {
          m_bUp = F2;m_iUp = i2;
        } else if (((m_I3.contains(i2)) || (m_I4.contains(i2))) && (F2 > m_bLow)) {
          m_bLow = F2;m_iLow = i2;
        }
      }
      



      boolean optimal = true;
      if (((m_I0.contains(i2)) || (m_I1.contains(i2)) || (m_I2.contains(i2))) && 
        (m_bLow - F2 > 2.0D * m_tol)) {
        optimal = false;i1 = m_iLow;
      }
      
      if (((m_I0.contains(i2)) || (m_I3.contains(i2)) || (m_I4.contains(i2))) && 
        (F2 - m_bUp > 2.0D * m_tol)) {
        optimal = false;i1 = m_iUp;
      }
      
      if (optimal) {
        return false;
      }
      

      if (m_I0.contains(i2)) {
        if (m_bLow - F2 > F2 - m_bUp) {
          i1 = m_iLow;
        } else {
          i1 = m_iUp;
        }
      }
      if (i1 == -1) {
        throw new Exception("This should never happen!");
      }
      return takeStep(i1, i2, F2);
    }
    











    protected boolean takeStep(int i1, int i2, double F2)
      throws Exception
    {
      double C1 = m_C * m_data.instance(i1).weight();
      double C2 = m_C * m_data.instance(i2).weight();
      

      if (i1 == i2) {
        return false;
      }
      

      double alph1 = m_alpha[i1];double alph2 = m_alpha[i2];
      double y1 = m_class[i1];double y2 = m_class[i2];
      double F1 = m_errors[i1];
      double s = y1 * y2;
      double H;
      double L;
      double H; if (y1 != y2) {
        double L = Math.max(0.0D, alph2 - alph1);
        H = Math.min(C2, C1 + alph2 - alph1);
      } else {
        L = Math.max(0.0D, alph1 + alph2 - C1);
        H = Math.min(C2, alph1 + alph2);
      }
      if (L >= H) {
        return false;
      }
      

      double k11 = m_kernel.eval(i1, i1, m_data.instance(i1));
      double k12 = m_kernel.eval(i1, i2, m_data.instance(i1));
      double k22 = m_kernel.eval(i2, i2, m_data.instance(i2));
      double eta = 2.0D * k12 - k11 - k22;
      
      double a2;
      if (eta < 0.0D)
      {

        double a2 = alph2 - y2 * (F1 - F2) / eta;
        

        if (a2 < L) {
          a2 = L;
        } else if (a2 > H) {
          a2 = H;
        }
      }
      else
      {
        double f1 = SVMOutput(i1, m_data.instance(i1));
        double f2 = SVMOutput(i2, m_data.instance(i2));
        double v1 = f1 + m_b - y1 * alph1 * k11 - y2 * alph2 * k12;
        double v2 = f2 + m_b - y1 * alph1 * k12 - y2 * alph2 * k22;
        double gamma = alph1 + s * alph2;
        double Lobj = gamma - s * L + L - 0.5D * k11 * (gamma - s * L) * (gamma - s * L) - 0.5D * k22 * L * L - s * k12 * (gamma - s * L) * L - y1 * (gamma - s * L) * v1 - y2 * L * v2;
        

        double Hobj = gamma - s * H + H - 0.5D * k11 * (gamma - s * H) * (gamma - s * H) - 0.5D * k22 * H * H - s * k12 * (gamma - s * H) * H - y1 * (gamma - s * H) * v1 - y2 * H * v2;
        
        double a2;
        if (Lobj > Hobj + m_eps) {
          a2 = L; } else { double a2;
          if (Lobj < Hobj - m_eps) {
            a2 = H;
          } else
            a2 = alph2;
        }
      }
      if (Math.abs(a2 - alph2) < m_eps * (a2 + alph2 + m_eps)) {
        return false;
      }
      

      if (a2 > C2 - MISMO.m_Del * C2) {
        a2 = C2;
      } else if (a2 <= MISMO.m_Del * C2) {
        a2 = 0.0D;
      }
      

      double a1 = alph1 + s * (alph2 - a2);
      

      if (a1 > C1 - MISMO.m_Del * C1) {
        a1 = C1;
      } else if (a1 <= MISMO.m_Del * C1) {
        a1 = 0.0D;
      }
      

      if (a1 > 0.0D) {
        m_supportVectors.insert(i1);
      } else {
        m_supportVectors.delete(i1);
      }
      if ((a1 > 0.0D) && (a1 < C1)) {
        m_I0.insert(i1);
      } else {
        m_I0.delete(i1);
      }
      if ((y1 == 1.0D) && (a1 == 0.0D)) {
        m_I1.insert(i1);
      } else {
        m_I1.delete(i1);
      }
      if ((y1 == -1.0D) && (a1 == C1)) {
        m_I2.insert(i1);
      } else {
        m_I2.delete(i1);
      }
      if ((y1 == 1.0D) && (a1 == C1)) {
        m_I3.insert(i1);
      } else {
        m_I3.delete(i1);
      }
      if ((y1 == -1.0D) && (a1 == 0.0D)) {
        m_I4.insert(i1);
      } else {
        m_I4.delete(i1);
      }
      if (a2 > 0.0D) {
        m_supportVectors.insert(i2);
      } else {
        m_supportVectors.delete(i2);
      }
      if ((a2 > 0.0D) && (a2 < C2)) {
        m_I0.insert(i2);
      } else {
        m_I0.delete(i2);
      }
      if ((y2 == 1.0D) && (a2 == 0.0D)) {
        m_I1.insert(i2);
      } else {
        m_I1.delete(i2);
      }
      if ((y2 == -1.0D) && (a2 == C2)) {
        m_I2.insert(i2);
      } else {
        m_I2.delete(i2);
      }
      if ((y2 == 1.0D) && (a2 == C2)) {
        m_I3.insert(i2);
      } else {
        m_I3.delete(i2);
      }
      if ((y2 == -1.0D) && (a2 == 0.0D)) {
        m_I4.insert(i2);
      } else {
        m_I4.delete(i2);
      }
      

      for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
        if ((j != i1) && (j != i2)) {
          m_errors[j] += y1 * (a1 - alph1) * m_kernel.eval(i1, j, m_data.instance(i1)) + y2 * (a2 - alph2) * m_kernel.eval(i2, j, m_data.instance(i2));
        }
      }
      



      m_errors[i1] += y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12;
      m_errors[i2] += y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22;
      

      m_alpha[i1] = a1;
      m_alpha[i2] = a2;
      

      m_bLow = -1.7976931348623157E308D;m_bUp = Double.MAX_VALUE;
      m_iLow = -1;m_iUp = -1;
      for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
        if (m_errors[j] < m_bUp) {
          m_bUp = m_errors[j];m_iUp = j;
        }
        if (m_errors[j] > m_bLow) {
          m_bLow = m_errors[j];m_iLow = j;
        }
      }
      if (!m_I0.contains(i1)) {
        if ((m_I3.contains(i1)) || (m_I4.contains(i1))) {
          if (m_errors[i1] > m_bLow) {
            m_bLow = m_errors[i1];m_iLow = i1;
          }
        }
        else if (m_errors[i1] < m_bUp) {
          m_bUp = m_errors[i1];m_iUp = i1;
        }
      }
      
      if (!m_I0.contains(i2)) {
        if ((m_I3.contains(i2)) || (m_I4.contains(i2))) {
          if (m_errors[i2] > m_bLow) {
            m_bLow = m_errors[i2];m_iLow = i2;
          }
        }
        else if (m_errors[i2] < m_bUp) {
          m_bUp = m_errors[i2];m_iUp = i2;
        }
      }
      
      if ((m_iLow == -1) || (m_iUp == -1)) {
        throw new Exception("This should never happen!");
      }
      

      return true;
    }
    




    protected void checkClassifier()
      throws Exception
    {
      double sum = 0.0D;
      for (int i = 0; i < m_alpha.length; i++) {
        if (m_alpha[i] > 0.0D) {
          sum += m_class[i] * m_alpha[i];
        }
      }
      System.err.println("Sum of y(i) * alpha(i): " + sum);
      
      for (int i = 0; i < m_alpha.length; i++) {
        double output = SVMOutput(i, m_data.instance(i));
        if ((Utils.eq(m_alpha[i], 0.0D)) && 
          (Utils.sm(m_class[i] * output, 1.0D))) {
          System.err.println("KKT condition 1 violated: " + m_class[i] * output);
        }
        
        if ((Utils.gr(m_alpha[i], 0.0D)) && (Utils.sm(m_alpha[i], m_C * m_data.instance(i).weight())))
        {
          if (!Utils.eq(m_class[i] * output, 1.0D)) {
            System.err.println("KKT condition 2 violated: " + m_class[i] * output);
          }
        }
        if ((Utils.eq(m_alpha[i], m_C * m_data.instance(i).weight())) && 
          (Utils.gr(m_class[i] * output, 1.0D))) {
          System.err.println("KKT condition 3 violated: " + m_class[i] * output);
        }
      }
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9144 $");
    }
  }
  







  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  





  protected BinaryMISMO[][] m_classifiers = (BinaryMISMO[][])null;
  

  protected double m_C = 1.0D;
  

  protected double m_eps = 1.0E-12D;
  

  protected double m_tol = 0.001D;
  

  protected int m_filterType = 0;
  

  protected boolean m_minimax = false;
  

  protected NominalToBinary m_NominalToBinary;
  

  protected Filter m_Filter = null;
  

  protected ReplaceMissingValues m_Missing;
  

  protected int m_classIndex = -1;
  

  protected Attribute m_classAttribute;
  

  protected Kernel m_kernel = new MIPolyKernel();
  



  protected boolean m_checksTurnedOff;
  



  protected static double m_Del = 4.94E-321D;
  

  protected boolean m_fitLogisticModels = false;
  

  protected int m_numFolds = -1;
  

  protected int m_randomSeed = 1;
  



  public void turnChecksOff()
  {
    m_checksTurnedOff = true;
  }
  



  public void turnChecksOn()
  {
    m_checksTurnedOff = false;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = getKernel().getCapabilities();
    result.setOwner(this);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = ((MultiInstanceCapabilitiesHandler)getKernel()).getMultiInstanceCapabilities();
    result.setOwner(this);
    

    result.enableAllAttributeDependencies();
    

    if (result.handles(Capabilities.Capability.NUMERIC_ATTRIBUTES))
      result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  





  public void buildClassifier(Instances insts)
    throws Exception
  {
    if (!m_checksTurnedOff)
    {
      getCapabilities().testWithFail(insts);
      

      insts = new Instances(insts);
      insts.deleteWithMissingClass();
      



      Instances data = new Instances(insts, insts.numInstances());
      for (int i = 0; i < insts.numInstances(); i++) {
        if (insts.instance(i).weight() > 0.0D)
          data.add(insts.instance(i));
      }
      if (data.numInstances() == 0) {
        throw new Exception("No training instances left after removing instance with either a weight null or a missing class!");
      }
      
      insts = data;
    }
    

    if (!m_checksTurnedOff) {
      m_Missing = new ReplaceMissingValues();
    } else {
      m_Missing = null;
    }
    if (getCapabilities().handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
      boolean onlyNumeric = true;
      if (!m_checksTurnedOff) {
        for (int i = 0; i < insts.numAttributes(); i++) {
          if ((i != insts.classIndex()) && 
            (!insts.attribute(i).isNumeric())) {
            onlyNumeric = false;
            break;
          }
        }
      }
      

      if (!onlyNumeric) {
        m_NominalToBinary = new NominalToBinary();
        
        m_NominalToBinary.setAttributeIndices("2-last");
      }
      else {
        m_NominalToBinary = null;
      }
    }
    else {
      m_NominalToBinary = null;
    }
    
    if (m_filterType == 1) {
      m_Filter = new Standardize();
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
    } else {
      m_Filter = null;
    }
    

    Filter convertToProp = new MultiInstanceToPropositional();
    Filter convertToMI = new PropositionalToMultiInstance();
    Instances transformedInsts;
    Instances transformedInsts;
    if (m_minimax)
    {

      SimpleMI transMinimax = new SimpleMI();
      transMinimax.setTransformMethod(new SelectedTag(3, SimpleMI.TAGS_TRANSFORMMETHOD));
      

      transformedInsts = transMinimax.transform(insts);
    }
    else {
      convertToProp.setInputFormat(insts);
      transformedInsts = Filter.useFilter(insts, convertToProp);
    }
    
    if (m_Missing != null) {
      m_Missing.setInputFormat(transformedInsts);
      transformedInsts = Filter.useFilter(transformedInsts, m_Missing);
    }
    
    if (m_NominalToBinary != null) {
      m_NominalToBinary.setInputFormat(transformedInsts);
      transformedInsts = Filter.useFilter(transformedInsts, m_NominalToBinary);
    }
    
    if (m_Filter != null) {
      m_Filter.setInputFormat(transformedInsts);
      transformedInsts = Filter.useFilter(transformedInsts, m_Filter);
    }
    

    convertToMI.setInputFormat(transformedInsts);
    insts = Filter.useFilter(transformedInsts, convertToMI);
    
    m_classIndex = insts.classIndex();
    m_classAttribute = insts.classAttribute();
    

    Instances[] subsets = new Instances[insts.numClasses()];
    for (int i = 0; i < insts.numClasses(); i++) {
      subsets[i] = new Instances(insts, insts.numInstances());
    }
    for (int j = 0; j < insts.numInstances(); j++) {
      Instance inst = insts.instance(j);
      subsets[((int)inst.classValue())].add(inst);
    }
    for (int i = 0; i < insts.numClasses(); i++) {
      subsets[i].compactify();
    }
    

    Random rand = new Random(m_randomSeed);
    m_classifiers = new BinaryMISMO[insts.numClasses()][insts.numClasses()];
    for (int i = 0; i < insts.numClasses(); i++) {
      for (int j = i + 1; j < insts.numClasses(); j++) {
        m_classifiers[i][j] = new BinaryMISMO();
        m_classifiers[i][j].setKernel(Kernel.makeCopy(getKernel()));
        Instances data = new Instances(insts, insts.numInstances());
        for (int k = 0; k < subsets[i].numInstances(); k++) {
          data.add(subsets[i].instance(k));
        }
        for (int k = 0; k < subsets[j].numInstances(); k++) {
          data.add(subsets[j].instance(k));
        }
        data.compactify();
        data.randomize(rand);
        m_classifiers[i][j].buildClassifier(data, i, j, m_fitLogisticModels, m_numFolds, m_randomSeed);
      }
    }
  }
  










  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    Instances insts = new Instances(inst.dataset(), 0);
    insts.add(inst);
    

    Filter convertToProp = new MultiInstanceToPropositional();
    Filter convertToMI = new PropositionalToMultiInstance();
    
    if (m_minimax) {
      SimpleMI transMinimax = new SimpleMI();
      transMinimax.setTransformMethod(new SelectedTag(3, SimpleMI.TAGS_TRANSFORMMETHOD));
      

      insts = transMinimax.transform(insts);
    }
    else {
      convertToProp.setInputFormat(insts);
      insts = Filter.useFilter(insts, convertToProp);
    }
    

    if (m_Missing != null) {
      insts = Filter.useFilter(insts, m_Missing);
    }
    if (m_NominalToBinary != null) {
      insts = Filter.useFilter(insts, m_NominalToBinary);
    }
    
    if (m_Filter != null) {
      insts = Filter.useFilter(insts, m_Filter);
    }
    
    convertToMI.setInputFormat(insts);
    insts = Filter.useFilter(insts, convertToMI);
    
    inst = insts.instance(0);
    
    if (!m_fitLogisticModels) {
      double[] result = new double[inst.numClasses()];
      for (int i = 0; i < inst.numClasses(); i++) {
        for (int j = i + 1; j < inst.numClasses(); j++) {
          if ((m_classifiers[i][j].m_alpha != null) || (m_classifiers[i][j].m_sparseWeights != null))
          {
            double output = m_classifiers[i][j].SVMOutput(-1, inst);
            if (output > 0.0D) {
              result[j] += 1.0D;
            } else {
              result[i] += 1.0D;
            }
          }
        }
      }
      Utils.normalize(result);
      return result;
    }
    


    if (inst.numClasses() == 2) {
      double[] newInst = new double[2];
      newInst[0] = m_classifiers[0][1].SVMOutput(-1, inst);
      newInst[1] = Instance.missingValue();
      return m_classifiers[0][1].m_logistic.distributionForInstance(new Instance(1.0D, newInst));
    }
    
    double[][] r = new double[inst.numClasses()][inst.numClasses()];
    double[][] n = new double[inst.numClasses()][inst.numClasses()];
    for (int i = 0; i < inst.numClasses(); i++) {
      for (int j = i + 1; j < inst.numClasses(); j++) {
        if ((m_classifiers[i][j].m_alpha != null) || (m_classifiers[i][j].m_sparseWeights != null))
        {
          double[] newInst = new double[2];
          newInst[0] = m_classifiers[i][j].SVMOutput(-1, inst);
          newInst[1] = Instance.missingValue();
          r[i][j] = m_classifiers[i][j].m_logistic.distributionForInstance(new Instance(1.0D, newInst))[0];
          
          n[i][j] = m_classifiers[i][j].m_sumOfWeights;
        }
      }
    }
    return pairwiseCoupling(n, r);
  }
  









  public double[] pairwiseCoupling(double[][] n, double[][] r)
  {
    double[] p = new double[r.length];
    for (int i = 0; i < p.length; i++) {
      p[i] = (1.0D / p.length);
    }
    double[][] u = new double[r.length][r.length];
    for (int i = 0; i < r.length; i++) {
      for (int j = i + 1; j < r.length; j++) {
        u[i][j] = 0.5D;
      }
    }
    

    double[] firstSum = new double[p.length];
    for (int i = 0; i < p.length; i++) {
      for (int j = i + 1; j < p.length; j++) {
        firstSum[i] += n[i][j] * r[i][j];
        firstSum[j] += n[i][j] * (1.0D - r[i][j]);
      }
    }
    
    boolean changed;
    do
    {
      changed = false;
      double[] secondSum = new double[p.length];
      for (int i = 0; i < p.length; i++) {
        for (int j = i + 1; j < p.length; j++) {
          secondSum[i] += n[i][j] * u[i][j];
          secondSum[j] += n[i][j] * (1.0D - u[i][j]);
        }
      }
      for (int i = 0; i < p.length; i++) {
        if ((firstSum[i] == 0.0D) || (secondSum[i] == 0.0D)) {
          if (p[i] > 0.0D) {
            changed = true;
          }
          p[i] = 0.0D;
        } else {
          double factor = firstSum[i] / secondSum[i];
          double pOld = p[i];
          p[i] *= factor;
          if (Math.abs(pOld - p[i]) > 0.001D) {
            changed = true;
          }
        }
      }
      Utils.normalize(p);
      for (int i = 0; i < r.length; i++) {
        for (int j = i + 1; j < r.length; j++) {
          u[i][j] = (p[i] / (p[i] + p[j]));
        }
      }
    } while (changed);
    return p;
  }
  





  public double[][][] sparseWeights()
  {
    int numValues = m_classAttribute.numValues();
    double[][][] sparseWeights = new double[numValues][numValues][];
    
    for (int i = 0; i < numValues; i++) {
      for (int j = i + 1; j < numValues; j++) {
        sparseWeights[i][j] = m_classifiers[i][j].m_sparseWeights;
      }
    }
    
    return sparseWeights;
  }
  





  public int[][][] sparseIndices()
  {
    int numValues = m_classAttribute.numValues();
    int[][][] sparseIndices = new int[numValues][numValues][];
    
    for (int i = 0; i < numValues; i++) {
      for (int j = i + 1; j < numValues; j++) {
        sparseIndices[i][j] = m_classifiers[i][j].m_sparseIndices;
      }
    }
    
    return sparseIndices;
  }
  





  public double[][] bias()
  {
    int numValues = m_classAttribute.numValues();
    double[][] bias = new double[numValues][numValues];
    
    for (int i = 0; i < numValues; i++) {
      for (int j = i + 1; j < numValues; j++) {
        bias[i][j] = m_classifiers[i][j].m_b;
      }
    }
    
    return bias;
  }
  





  public int numClassAttributeValues()
  {
    return m_classAttribute.numValues();
  }
  





  public String[] classAttributeNames()
  {
    int numValues = m_classAttribute.numValues();
    
    String[] classAttributeNames = new String[numValues];
    
    for (int i = 0; i < numValues; i++) {
      classAttributeNames[i] = m_classAttribute.value(i);
    }
    
    return classAttributeNames;
  }
  





  public String[][][] attributeNames()
  {
    int numValues = m_classAttribute.numValues();
    String[][][] attributeNames = new String[numValues][numValues][];
    
    for (int i = 0; i < numValues; i++) {
      for (int j = i + 1; j < numValues; j++) {
        int numAttributes = m_classifiers[i][j].m_data.numAttributes();
        String[] attrNames = new String[numAttributes];
        for (int k = 0; k < numAttributes; k++) {
          attrNames[k] = m_classifiers[i][j].m_data.attribute(k).name();
        }
        attributeNames[i][j] = attrNames;
      }
    }
    return attributeNames;
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tTurns off all checks - use with caution!\n\tTurning them off assumes that data is purely numeric, doesn't\n\tcontain any missing values, and has a nominal class. Turning them\n\toff also means that no header information will be stored if the\n\tmachine is linear. Finally, it also assumes that no instance has\n\ta weight equal to 0.\n\t(default: checks on)", "no-checks", 0, "-no-checks"));
    








    result.addElement(new Option("\tThe complexity constant C. (default 1)", "C", 1, "-C <double>"));
    


    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default 0=normalize)", "N", 1, "-N"));
    



    result.addElement(new Option("\tUse MIminimax feature space. ", "I", 0, "-I"));
    


    result.addElement(new Option("\tThe tolerance parameter. (default 1.0e-3)", "L", 1, "-L <double>"));
    


    result.addElement(new Option("\tThe epsilon for round-off error. (default 1.0e-12)", "P", 1, "-P <double>"));
    


    result.addElement(new Option("\tFit logistic models to SVM outputs. ", "M", 0, "-M"));
    


    result.addElement(new Option("\tThe number of folds for the internal cross-validation. \n\t(default -1, use training data)", "V", 1, "-V <double>"));
    



    result.addElement(new Option("\tThe random number seed. (default 1)", "W", 1, "-W <double>"));
    


    result.addElement(new Option("\tThe Kernel to use.\n\t(default: weka.classifiers.functions.supportVector.PolyKernel)", "K", 1, "-K <classname and parameters>"));
    



    result.addElement(new Option("", "", 0, "\nOptions specific to kernel " + getKernel().getClass().getName() + ":"));
    



    enm = getKernel().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  
















































































  public void setOptions(String[] options)
    throws Exception
  {
    setChecksTurnedOff(Utils.getFlag("no-checks", options));
    
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setC(Double.parseDouble(tmpStr));
    } else {
      setC(1.0D);
    }
    tmpStr = Utils.getOption('L', options);
    if (tmpStr.length() != 0) {
      setToleranceParameter(Double.parseDouble(tmpStr));
    } else {
      setToleranceParameter(0.001D);
    }
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setEpsilon(new Double(tmpStr).doubleValue());
    } else {
      setEpsilon(1.0E-12D);
    }
    setMinimax(Utils.getFlag('I', options));
    
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, TAGS_FILTER));
    }
    setBuildLogisticModels(Utils.getFlag('M', options));
    
    tmpStr = Utils.getOption('V', options);
    if (tmpStr.length() != 0) {
      m_numFolds = Integer.parseInt(tmpStr);
    } else {
      m_numFolds = -1;
    }
    tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() != 0) {
      setRandomSeed(Integer.parseInt(tmpStr));
    } else {
      setRandomSeed(1);
    }
    tmpStr = Utils.getOption('K', options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setKernel(Kernel.forName(tmpStr, tmpOptions));
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
    if (getChecksTurnedOff()) {
      result.add("-no-checks");
    }
    result.add("-C");
    result.add("" + getC());
    
    result.add("-L");
    result.add("" + getToleranceParameter());
    
    result.add("-P");
    result.add("" + getEpsilon());
    
    result.add("-N");
    result.add("" + m_filterType);
    
    if (getMinimax()) {
      result.add("-I");
    }
    if (getBuildLogisticModels()) {
      result.add("-M");
    }
    result.add("-V");
    result.add("" + getNumFolds());
    
    result.add("-W");
    result.add("" + getRandomSeed());
    
    result.add("-K");
    result.add("" + getKernel().getClass().getName() + " " + Utils.joinOptions(getKernel().getOptions()));
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public void setChecksTurnedOff(boolean value)
  {
    if (value) {
      turnChecksOff();
    } else {
      turnChecksOn();
    }
  }
  



  public boolean getChecksTurnedOff()
  {
    return m_checksTurnedOff;
  }
  





  public String checksTurnedOffTipText()
  {
    return "Turns time-consuming checks off - use with caution.";
  }
  





  public String kernelTipText()
  {
    return "The kernel to use.";
  }
  




  public Kernel getKernel()
  {
    return m_kernel;
  }
  




  public void setKernel(Kernel value)
  {
    if (!(value instanceof MultiInstanceCapabilitiesHandler)) {
      throw new IllegalArgumentException("Kernel must be able to handle multi-instance data!\n(This one does not implement " + MultiInstanceCapabilitiesHandler.class.getName() + ")");
    }
    

    m_kernel = value;
  }
  




  public String cTipText()
  {
    return "The complexity parameter C.";
  }
  





  public double getC()
  {
    return m_C;
  }
  





  public void setC(double v)
  {
    m_C = v;
  }
  




  public String toleranceParameterTipText()
  {
    return "The tolerance parameter (shouldn't be changed).";
  }
  




  public double getToleranceParameter()
  {
    return m_tol;
  }
  




  public void setToleranceParameter(double v)
  {
    m_tol = v;
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
  




  public String filterTypeTipText()
  {
    return "Determines how/if the data will be transformed.";
  }
  






  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  






  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  





  public String minimaxTipText()
  {
    return "Whether the MIMinimax feature space is to be used.";
  }
  




  public boolean getMinimax()
  {
    return m_minimax;
  }
  



  public void setMinimax(boolean v)
  {
    m_minimax = v;
  }
  




  public String buildLogisticModelsTipText()
  {
    return "Whether to fit logistic models to the outputs (for proper probability estimates).";
  }
  






  public boolean getBuildLogisticModels()
  {
    return m_fitLogisticModels;
  }
  





  public void setBuildLogisticModels(boolean newbuildLogisticModels)
  {
    m_fitLogisticModels = newbuildLogisticModels;
  }
  




  public String numFoldsTipText()
  {
    return "The number of folds for cross-validation used to generate training data for logistic models (-1 means use training data).";
  }
  






  public int getNumFolds()
  {
    return m_numFolds;
  }
  





  public void setNumFolds(int newnumFolds)
  {
    m_numFolds = newnumFolds;
  }
  




  public String randomSeedTipText()
  {
    return "Random number seed for the cross-validation.";
  }
  





  public int getRandomSeed()
  {
    return m_randomSeed;
  }
  





  public void setRandomSeed(int newrandomSeed)
  {
    m_randomSeed = newrandomSeed;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_classAttribute == null) {
      return "SMO: No model built yet.";
    }
    try {
      text.append("SMO\n\n");
      for (int i = 0; i < m_classAttribute.numValues(); i++) {
        for (int j = i + 1; j < m_classAttribute.numValues(); j++) {
          text.append("Classifier for classes: " + m_classAttribute.value(i) + ", " + m_classAttribute.value(j) + "\n\n");
          

          text.append(m_classifiers[i][j]);
          if (m_fitLogisticModels) {
            text.append("\n\n");
            if (m_classifiers[i][j].m_logistic == null) {
              text.append("No logistic model has been fit.\n");
            } else {
              text.append(m_classifiers[i][j].m_logistic);
            }
          }
          text.append("\n\n");
        }
      }
    } catch (Exception e) {
      return "Can't print SMO classifier.";
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new MISMO(), argv);
  }
}
