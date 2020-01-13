package weka.attributeSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.CostMatrix;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




































public abstract class CostSensitiveASEvaluation
  extends ASEvaluation
  implements OptionHandler, Serializable
{
  static final long serialVersionUID = -7045833833363396977L;
  public static final int MATRIX_ON_DEMAND = 1;
  public static final int MATRIX_SUPPLIED = 2;
  public static final Tag[] TAGS_MATRIX_SOURCE = { new Tag(1, "Load cost matrix on demand"), new Tag(2, "Use explicit cost matrix") };
  




  protected int m_MatrixSource = 1;
  




  protected File m_OnDemandDirectory = new File(System.getProperty("user.dir"));
  

  protected String m_CostFile;
  

  protected CostMatrix m_CostMatrix = new CostMatrix(1);
  

  protected ASEvaluation m_evaluator;
  

  protected int m_seed = 1;
  


  public CostSensitiveASEvaluation() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tFile name of a cost matrix to use. If this is not supplied,\n\ta cost matrix will be loaded on demand. The name of the\n\ton-demand file is the relation name of the training data\n\tplus \".cost\", and the path to the on-demand file is\n\tspecified with the -N option.", "C", 1, "-C <cost file name>"));
    





    newVector.addElement(new Option("\tName of a directory to search for cost files when loading\n\tcosts on demand (default current directory).", "N", 1, "-N <directory>"));
    


    newVector.addElement(new Option("\tThe cost matrix in Matlab single line format.", "cost-matrix", 1, "-cost-matrix <matrix>"));
    

    newVector.addElement(new Option("\tThe seed to use for random number generation.", "S", 1, "-S <integer>"));
    


    newVector.addElement(new Option("\tFull name of base evaluator. Options after -- are passed to the evaluator.\n\t(default: " + defaultEvaluatorString() + ")", "W", 1, "-W"));
    




    if ((m_evaluator instanceof OptionHandler)) {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to evaluator " + m_evaluator.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_evaluator).listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    

    return newVector.elements();
  }
  





























  public void setOptions(String[] options)
    throws Exception
  {
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
    

    String seed = Utils.getOption('S', options);
    if (seed.length() != 0) {
      setSeed(Integer.parseInt(seed));
    } else {
      setSeed(1);
    }
    
    String evaluatorName = Utils.getOption('W', options);
    
    if (evaluatorName.length() > 0)
    {


      setEvaluator(ASEvaluation.forName(evaluatorName, null));
      setEvaluator(ASEvaluation.forName(evaluatorName, Utils.partitionOptions(options)));

    }
    else
    {

      setEvaluator(ASEvaluation.forName(defaultEvaluatorString(), null));
      setEvaluator(ASEvaluation.forName(defaultEvaluatorString(), Utils.partitionOptions(options)));
    }
  }
  





  public String[] getOptions()
  {
    ArrayList<String> options = new ArrayList();
    
    if (m_MatrixSource == 2) {
      if (m_CostFile != null) {
        options.add("-C");
        options.add("" + m_CostFile);
      }
      else {
        options.add("-cost-matrix");
        options.add(getCostMatrix().toMatlab());
      }
    } else {
      options.add("-N");
      options.add("" + getOnDemandDirectory());
    }
    
    options.add("-S");
    options.add("" + getSeed());
    
    options.add("-W");
    options.add(m_evaluator.getClass().getName());
    
    if ((m_evaluator instanceof OptionHandler)) {
      String[] evaluatorOptions = ((OptionHandler)m_evaluator).getOptions();
      if (evaluatorOptions.length > 0) {
        options.add("--");
        for (int i = 0; i < evaluatorOptions.length; i++) {
          options.add(evaluatorOptions[i]);
        }
      }
    }
    
    return (String[])options.toArray(new String[0]);
  }
  




  public String globalInfo()
  {
    return "A meta subset evaluator that makes its base subset evaluator cost-sensitive. ";
  }
  




  public String defaultEvaluatorString()
  {
    return "weka.attributeSelection.CfsSubsetEval";
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
  






  protected String getEvaluatorSpec()
  {
    ASEvaluation ase = getEvaluator();
    if ((ase instanceof OptionHandler)) {
      return ase.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)ase).getOptions());
    }
    
    return ase.getClass().getName();
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
  




  public String seedTipText()
  {
    return "The random number seed to be used.";
  }
  





  public void setSeed(int seed)
  {
    m_seed = seed;
  }
  





  public int getSeed()
  {
    return m_seed;
  }
  




  public String evaluatorTipText()
  {
    return "The base evaluator to be used.";
  }
  





  public void setEvaluator(ASEvaluation newEvaluator)
    throws IllegalArgumentException
  {
    m_evaluator = newEvaluator;
  }
  





  public ASEvaluation getEvaluator()
  {
    return m_evaluator;
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (getEvaluator() != null) {
      result = getEvaluator().getCapabilities();
    } else {
      result = new Capabilities(this);
      result.disableAll();
    }
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  







  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if (m_evaluator == null) {
      throw new Exception("No base evaluator has been set!");
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
    

    Random random = null;
    if (!(m_evaluator instanceof WeightedInstancesHandler)) {
      random = new Random(m_seed);
    }
    data = m_CostMatrix.applyCostMatrix(data, random);
    m_evaluator.buildEvaluator(data);
  }
  







  public int[] postProcess(int[] attributeSet)
    throws Exception
  {
    return m_evaluator.postProcess(attributeSet);
  }
  





  public String toString()
  {
    if (m_evaluator == null) {
      return "CostSensitiveASEvaluation: No model built yet.";
    }
    
    String result = (m_evaluator instanceof AttributeEvaluator) ? "CostSensitiveAttributeEval using " : "CostSensitiveSubsetEval using ";
    


    result = result + "\n\n" + getEvaluatorSpec() + "\n\nEvaluator\n" + m_evaluator.toString() + "\n\nCost Matrix\n" + m_CostMatrix.toString();
    




    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
}
