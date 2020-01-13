package weka.classifiers.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;










































































































public class MetaCost
  extends RandomizableSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 1205317833344726855L;
  public static final int MATRIX_ON_DEMAND = 1;
  public static final int MATRIX_SUPPLIED = 2;
  public static final Tag[] TAGS_MATRIX_SOURCE = { new Tag(1, "Load cost matrix on demand"), new Tag(2, "Use explicit cost matrix") };
  




  protected int m_MatrixSource = 1;
  




  protected File m_OnDemandDirectory = new File(System.getProperty("user.dir"));
  

  protected String m_CostFile;
  

  protected CostMatrix m_CostMatrix = new CostMatrix(1);
  

  protected int m_NumIterations = 10;
  

  protected int m_BagSizePercent = 100;
  


  public MetaCost() {}
  

  public String globalInfo()
  {
    return "This metaclassifier makes its base classifier cost-sensitive using the method specified in\n\n" + getTechnicalInformation().toString() + "\n\n" + "This classifier should produce similar results to one created by " + "passing the base learner to Bagging, which is in turn passed to a " + "CostSensitiveClassifier operating on minimum expected cost. The difference " + "is that MetaCost produces a single cost-sensitive classifier of the " + "base learner, giving the benefits of fast classification and interpretable " + "output (if the base learner itself is interpretable). This implementation  " + "uses all bagging iterations when reclassifying training data (the MetaCost " + "paper reports a marginal improvement when only those iterations containing " + "each training instance are used in reclassifying that instance).";
  }
  




















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Pedro Domingos");
    result.setValue(TechnicalInformation.Field.TITLE, "MetaCost: A general method for making classifiers cost-sensitive");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Fifth International Conference on Knowledge Discovery and Data Mining");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.PAGES, "155-164");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tNumber of bagging iterations.\n\t(default 10)", "I", 1, "-I <num>"));
    


    newVector.addElement(new Option("\tFile name of a cost matrix to use. If this is not supplied,\n\ta cost matrix will be loaded on demand. The name of the\n\ton-demand file is the relation name of the training data\n\tplus \".cost\", and the path to the on-demand file is\n\tspecified with the -N option.", "C", 1, "-C <cost file name>"));
    





    newVector.addElement(new Option("\tName of a directory to search for cost files when loading\n\tcosts on demand (default current directory).", "N", 1, "-N <directory>"));
    


    newVector.addElement(new Option("\tThe cost matrix in Matlab single line format.", "cost-matrix", 1, "-cost-matrix <matrix>"));
    

    newVector.addElement(new Option("\tSize of each bag, as a percentage of the\n\ttraining set size. (default 100)", "P", 1, "-P"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  






















































  public void setOptions(String[] options)
    throws Exception
  {
    String bagIterations = Utils.getOption('I', options);
    if (bagIterations.length() != 0) {
      setNumIterations(Integer.parseInt(bagIterations));
    } else {
      setNumIterations(10);
    }
    
    String bagSize = Utils.getOption('P', options);
    if (bagSize.length() != 0) {
      setBagSizePercent(Integer.parseInt(bagSize));
    } else {
      setBagSizePercent(100);
    }
    
    String costFile = Utils.getOption('C', options);
    if (costFile.length() != 0) {
      setCostMatrix(new CostMatrix(new BufferedReader(new FileReader(costFile))));
      
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
    

    String[] options = new String[superOptions.length + 6];
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
    options[(current++)] = "-I";options[(current++)] = ("" + getNumIterations());
    options[(current++)] = "-P";options[(current++)] = ("" + getBagSizePercent());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    return options;
  }
  




  public String costMatrixSourceTipText()
  {
    return "Gets the source location method of the cost matrix. Will be one of MATRIX_ON_DEMAND or MATRIX_SUPPLIED.";
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
    return "Name of directory to search for cost files when loading costs on demand.";
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
  




  public String bagSizePercentTipText()
  {
    return "The size of each bag, as a percentage of the training set size.";
  }
  






  public int getBagSizePercent()
  {
    return m_BagSizePercent;
  }
  





  public void setBagSizePercent(int newBagSizePercent)
  {
    m_BagSizePercent = newBagSizePercent;
  }
  




  public String numIterationsTipText()
  {
    return "The number of bagging iterations.";
  }
  





  public void setNumIterations(int numIterations)
  {
    m_NumIterations = numIterations;
  }
  





  public int getNumIterations()
  {
    return m_NumIterations;
  }
  




  public String costMatrixTipText()
  {
    return "A misclassification cost matrix.";
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
    
    if (m_MatrixSource == 1) {
      String costName = data.relationName() + CostMatrix.FILE_EXTENSION;
      File costFile = new File(getOnDemandDirectory(), costName);
      if (!costFile.exists()) {
        throw new Exception("On-demand cost file doesn't exist: " + costFile);
      }
      setCostMatrix(new CostMatrix(new BufferedReader(new FileReader(costFile))));
    }
    


    Bagging bagger = new Bagging();
    bagger.setClassifier(getClassifier());
    bagger.setSeed(getSeed());
    bagger.setNumIterations(getNumIterations());
    bagger.setBagSizePercent(getBagSizePercent());
    bagger.buildClassifier(data);
    


    Instances newData = new Instances(data);
    for (int i = 0; i < newData.numInstances(); i++) {
      Instance current = newData.instance(i);
      double[] pred = bagger.distributionForInstance(current);
      int minCostPred = Utils.minIndex(m_CostMatrix.expectedCosts(pred));
      current.setClassValue(minCostPred);
    }
    

    m_Classifier.buildClassifier(newData);
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_Classifier.distributionForInstance(instance);
  }
  






  protected String getClassifierSpec()
  {
    Classifier c = getClassifier();
    return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
  }
  






  public String toString()
  {
    if (m_Classifier == null) {
      return "MetaCost: No model built yet.";
    }
    
    String result = "MetaCost cost sensitive classifier induction";
    result = result + "\nOptions: " + Utils.joinOptions(getOptions());
    result = result + "\nBase learner: " + getClassifierSpec() + "\n\nClassifier Model\n" + m_Classifier.toString() + "\n\nCost Matrix\n" + m_CostMatrix.toString();
    




    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.24 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MetaCost(), argv);
  }
}
