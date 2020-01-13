package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;



























































public class RemoveFolds
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 8220373305559055700L;
  private boolean m_Inverse = false;
  

  private int m_NumFolds = 10;
  

  private int m_Fold = 1;
  

  private long m_Seed = 0L;
  


  public RemoveFolds() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tSpecifies if inverse of selection is to be output.\n", "V", 0, "-V"));
    


    newVector.addElement(new Option("\tSpecifies number of folds dataset is split into. \n\t(default 10)\n", "N", 1, "-N <number of folds>"));
    



    newVector.addElement(new Option("\tSpecifies which fold is selected. (default 1)\n", "F", 1, "-F <fold>"));
    


    newVector.addElement(new Option("\tSpecifies random number seed. (default 0, no randomizing)\n", "S", 1, "-S <seed>"));
    


    return newVector.elements();
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    setInvertSelection(Utils.getFlag('V', options));
    String numFolds = Utils.getOption('N', options);
    if (numFolds.length() != 0) {
      setNumFolds(Integer.parseInt(numFolds));
    } else {
      setNumFolds(10);
    }
    String fold = Utils.getOption('F', options);
    if (fold.length() != 0) {
      setFold(Integer.parseInt(fold));
    } else {
      setFold(1);
    }
    String seed = Utils.getOption('S', options);
    if (seed.length() != 0) {
      setSeed(Integer.parseInt(seed));
    } else {
      setSeed(0L);
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[8];
    int current = 0;
    
    options[(current++)] = "-S";options[(current++)] = ("" + getSeed());
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    options[(current++)] = "-N";options[(current++)] = ("" + getNumFolds());
    options[(current++)] = "-F";options[(current++)] = ("" + getFold());
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String globalInfo()
  {
    return "This filter takes a dataset and outputs a specified fold for cross validation. If you want the folds to be stratified use the supervised version.";
  }
  









  public String invertSelectionTipText()
  {
    return "Whether to invert the selection.";
  }
  





  public boolean getInvertSelection()
  {
    return m_Inverse;
  }
  





  public void setInvertSelection(boolean inverse)
  {
    m_Inverse = inverse;
  }
  






  public String numFoldsTipText()
  {
    return "The number of folds to split the dataset into.";
  }
  





  public int getNumFolds()
  {
    return m_NumFolds;
  }
  







  public void setNumFolds(int numFolds)
  {
    if (numFolds < 0) {
      throw new IllegalArgumentException("Number of folds has to be positive or zero.");
    }
    m_NumFolds = numFolds;
  }
  






  public String foldTipText()
  {
    return "The fold which is selected.";
  }
  





  public int getFold()
  {
    return m_Fold;
  }
  






  public void setFold(int fold)
  {
    if (fold < 1) {
      throw new IllegalArgumentException("Fold's index has to be greater than 0.");
    }
    m_Fold = fold;
  }
  






  public String seedTipText()
  {
    return "the random number seed for shuffling the dataset. If seed is negative, shuffling will not be performed.";
  }
  





  public long getSeed()
  {
    return m_Seed;
  }
  






  public void setSeed(long seed)
  {
    m_Seed = seed;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    if ((m_NumFolds > 0) && (m_NumFolds < m_Fold)) {
      throw new IllegalArgumentException("Fold has to be smaller or equal to number of folds.");
    }
    
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    return true;
  }
  









  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (isFirstBatchDone()) {
      push(instance);
      return true;
    }
    bufferInput(instance);
    return false;
  }
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    Instances instances;
    Instances instances;
    if (!isFirstBatchDone()) {
      if (m_Seed > 0L)
      {
        getInputFormat().randomize(new Random(m_Seed));
      }
      
      Instances instances;
      
      if (!m_Inverse) {
        instances = getInputFormat().testCV(m_NumFolds, m_Fold - 1);
      } else {
        instances = getInputFormat().trainCV(m_NumFolds, m_Fold - 1);
      }
    }
    else {
      instances = getInputFormat();
    }
    
    flushInput();
    
    for (int i = 0; i < instances.numInstances(); i++) {
      push(instances.instance(i));
    }
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RemoveFolds(), argv);
  }
}
