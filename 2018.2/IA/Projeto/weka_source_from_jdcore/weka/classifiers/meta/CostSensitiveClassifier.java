package weka.classifiers.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;


















































































public class CostSensitiveClassifier
  extends RandomizableSingleClassifierEnhancer
  implements OptionHandler, Drawable
{
  static final long serialVersionUID = -720658209263002404L;
  public static final int MATRIX_ON_DEMAND = 1;
  public static final int MATRIX_SUPPLIED = 2;
  public static final Tag[] TAGS_MATRIX_SOURCE = { new Tag(1, "Load cost matrix on demand"), new Tag(2, "Use explicit cost matrix") };
  




  protected int m_MatrixSource = 1;
  




  protected File m_OnDemandDirectory = new File(System.getProperty("user.dir"));
  

  protected String m_CostFile;
  

  protected CostMatrix m_CostMatrix = new CostMatrix(1);
  




  protected boolean m_MinimizeExpectedCost;
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.rules.ZeroR";
  }
  


  public CostSensitiveClassifier()
  {
    m_Classifier = new ZeroR();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tMinimize expected misclassification cost. Default is to\n\treweight training instances according to costs per class", "M", 0, "-M"));
    


    newVector.addElement(new Option("\tFile name of a cost matrix to use. If this is not supplied,\n\ta cost matrix will be loaded on demand. The name of the\n\ton-demand file is the relation name of the training data\n\tplus \".cost\", and the path to the on-demand file is\n\tspecified with the -N option.", "C", 1, "-C <cost file name>"));
    





    newVector.addElement(new Option("\tName of a directory to search for cost files when loading\n\tcosts on demand (default current directory).", "N", 1, "-N <directory>"));
    


    newVector.addElement(new Option("\tThe cost matrix in Matlab single line format.", "cost-matrix", 1, "-cost-matrix <matrix>"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  


















































  public void setOptions(String[] options)
    throws Exception
  {
    setMinimizeExpectedCost(Utils.getFlag('M', options));
    
    String costFile = Utils.getOption('C', options);
    if (costFile.length() != 0) {
      try {
        setCostMatrix(new CostMatrix(new BufferedReader(new FileReader(costFile))));

      }
      catch (Exception ex)
      {
        setCostMatrix(null);
      }
      setCostMatrixSource(new SelectedTag(2, TAGS_MATRIX_SOURCE));
      
      m_CostFile = costFile;
    } else {
      setCostMatrixSource(new SelectedTag(1, TAGS_MATRIX_SOURCE));
    }
    

    String demandDir = Utils.getOption('N', options);
    if (demandDir.length() != 0) {
      setOnDemandDirectory(new File(demandDir));
    }
    
    String cost_matrix = Utils.getOption("cost-matrix", options);
    if (cost_matrix.length() != 0) {
      StringWriter writer = new StringWriter();
      CostMatrix.parseMatlab(cost_matrix).write(writer);
      setCostMatrix(new CostMatrix(new StringReader(writer.toString())));
      setCostMatrixSource(new SelectedTag(2, TAGS_MATRIX_SOURCE));
    }
    

    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 7];
    
    int current = 0;
    
    if (m_MatrixSource == 2) {
      if (m_CostFile != null) {
        options[(current++)] = "-C";
        options[(current++)] = ("" + m_CostFile);
      }
      else {
        options[(current++)] = "-cost-matrix";
        options[(current++)] = getCostMatrix().toMatlab();
      }
    } else {
      options[(current++)] = "-N";
      options[(current++)] = ("" + getOnDemandDirectory());
    }
    
    if (getMinimizeExpectedCost()) {
      options[(current++)] = "-M";
    }
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    while (current < options.length) {
      if (options[current] == null) {
        options[current] = "";
      }
      current++;
    }
    
    return options;
  }
  




  public String globalInfo()
  {
    return "A metaclassifier that makes its base classifier cost-sensitive. Two methods can be used to introduce cost-sensitivity: reweighting training instances according to the total cost assigned to each class; or predicting the class with minimum expected misclassification cost (rather than the most likely class). Performance can often be improved by using a Bagged classifier to improve the probability estimates of the base classifier.";
  }
  











  public String costMatrixSourceTipText()
  {
    return "Sets where to get the cost matrix. The two options areto use the supplied explicit cost matrix (the setting of the costMatrix property), or to load a cost matrix from a file when required (this file will be loaded from the directory set by the onDemandDirectory property and will be named relation_name" + CostMatrix.FILE_EXTENSION + ").";
  }
  











  public SelectedTag getCostMatrixSource()
  {
    return new SelectedTag(m_MatrixSource, TAGS_MATRIX_SOURCE);
  }
  






  public void setCostMatrixSource(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_MATRIX_SOURCE) {
      m_MatrixSource = newMethod.getSelectedTag().getID();
    }
  }
  




  public String onDemandDirectoryTipText()
  {
    return "Sets the directory where cost files are loaded from. This option is used when the costMatrixSource is set to \"On Demand\".";
  }
  







  public File getOnDemandDirectory()
  {
    return m_OnDemandDirectory;
  }
  






  public void setOnDemandDirectory(File newDir)
  {
    if (newDir.isDirectory()) {
      m_OnDemandDirectory = newDir;
    } else {
      m_OnDemandDirectory = new File(newDir.getParent());
    }
    m_MatrixSource = 1;
  }
  




  public String minimizeExpectedCostTipText()
  {
    return "Sets whether the minimum expected cost criteria will be used. If this is false, the training data will be reweighted according to the costs assigned to each class. If true, the minimum expected cost criteria will be used.";
  }
  








  public boolean getMinimizeExpectedCost()
  {
    return m_MinimizeExpectedCost;
  }
  





  public void setMinimizeExpectedCost(boolean newMinimizeExpectedCost)
  {
    m_MinimizeExpectedCost = newMinimizeExpectedCost;
  }
  






  protected String getClassifierSpec()
  {
    Classifier c = getClassifier();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
    }
    
    return c.getClass().getName();
  }
  



  public String costMatrixTipText()
  {
    return "Sets the cost matrix explicitly. This matrix is used if the costMatrixSource property is set to \"Supplied\".";
  }
  






  public CostMatrix getCostMatrix()
  {
    return m_CostMatrix;
  }
  





  public void setCostMatrix(CostMatrix newCostMatrix)
  {
    m_CostMatrix = newCostMatrix;
    m_MatrixSource = 2;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }
    if (m_MatrixSource == 1) {
      String costName = data.relationName() + CostMatrix.FILE_EXTENSION;
      File costFile = new File(getOnDemandDirectory(), costName);
      if (!costFile.exists()) {
        throw new Exception("On-demand cost file doesn't exist: " + costFile);
      }
      setCostMatrix(new CostMatrix(new BufferedReader(new FileReader(costFile))));
    }
    else if (m_CostMatrix == null)
    {
      m_CostMatrix = new CostMatrix(data.numClasses());
      m_CostMatrix.readOldFormat(new BufferedReader(new FileReader(m_CostFile)));
    }
    

    if (!m_MinimizeExpectedCost) {
      Random random = null;
      if (!(m_Classifier instanceof WeightedInstancesHandler)) {
        random = new Random(m_Seed);
      }
      data = m_CostMatrix.applyCostMatrix(data, random);
    }
    m_Classifier.buildClassifier(data);
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (!m_MinimizeExpectedCost) {
      return m_Classifier.distributionForInstance(instance);
    }
    double[] pred = m_Classifier.distributionForInstance(instance);
    double[] costs = m_CostMatrix.expectedCosts(pred, instance);
    











    int classIndex = Utils.minIndex(costs);
    for (int i = 0; i < pred.length; i++) {
      if (i == classIndex) {
        pred[i] = 1.0D;
      } else {
        pred[i] = 0.0D;
      }
    }
    return pred;
  }
  






  public int graphType()
  {
    if ((m_Classifier instanceof Drawable)) {
      return ((Drawable)m_Classifier).graphType();
    }
    return 0;
  }
  





  public String graph()
    throws Exception
  {
    if ((m_Classifier instanceof Drawable))
      return ((Drawable)m_Classifier).graph();
    throw new Exception("Classifier: " + getClassifierSpec() + " cannot be graphed");
  }
  






  public String toString()
  {
    if (m_Classifier == null) {
      return "CostSensitiveClassifier: No model built yet.";
    }
    
    String result = "CostSensitiveClassifier using ";
    if (m_MinimizeExpectedCost) {
      result = result + "minimized expected misclasification cost\n";
    } else {
      result = result + "reweighted training instances\n";
    }
    result = result + "\n" + getClassifierSpec() + "\n\nClassifier Model\n" + m_Classifier.toString() + "\n\nCost Matrix\n" + m_CostMatrix.toString();
    




    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.29 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new CostSensitiveClassifier(), argv);
  }
}
