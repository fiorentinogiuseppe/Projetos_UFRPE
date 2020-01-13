package weka.experiment;

import java.io.ObjectStreamClass;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.core.AdditionalMeasureProducer;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;






































public class DensityBasedClustererSplitEvaluator
  implements SplitEvaluator, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  protected boolean m_removeClassColumn = true;
  

  protected DensityBasedClusterer m_clusterer = new EM();
  

  protected String[] m_additionalMeasures = null;
  





  protected boolean[] m_doesProduce = null;
  





  protected int m_numberAdditionalMeasures = 0;
  

  protected String m_result = null;
  

  protected String m_clustererOptions = "";
  

  protected String m_clustererVersion = "";
  
  private static final int KEY_SIZE = 3;
  
  private static final int RESULT_SIZE = 6;
  

  public DensityBasedClustererSplitEvaluator()
  {
    updateOptions();
  }
  





  public String globalInfo()
  {
    return " A SplitEvaluator that produces results for a density based clusterer. ";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tThe full class name of the density based clusterer.\n\teg: weka.clusterers.EM", "W", 1, "-W <class name>"));
    




    if ((m_clusterer != null) && ((m_clusterer instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to clusterer " + m_clusterer.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_clusterer).listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String cName = Utils.getOption('W', options);
    if (cName.length() > 0)
    {



      setClusterer((DensityBasedClusterer)AbstractClusterer.forName(cName, null));
    }
    
    if ((getClusterer() instanceof OptionHandler)) {
      ((OptionHandler)getClusterer()).setOptions(Utils.partitionOptions(options));
      
      updateOptions();
    }
  }
  






  public String[] getOptions()
  {
    String[] clustererOptions = new String[0];
    if ((m_clusterer != null) && ((m_clusterer instanceof OptionHandler)))
    {
      clustererOptions = ((OptionHandler)m_clusterer).getOptions();
    }
    
    String[] options = new String[clustererOptions.length + 3];
    int current = 0;
    
    if (getClusterer() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getClusterer().getClass().getName();
    }
    
    options[(current++)] = "--";
    
    System.arraycopy(clustererOptions, 0, options, current, clustererOptions.length);
    
    current += clustererOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  









  public void setAdditionalMeasures(String[] additionalMeasures)
  {
    m_additionalMeasures = additionalMeasures;
    


    if ((m_additionalMeasures != null) && (m_additionalMeasures.length > 0)) {
      m_doesProduce = new boolean[m_additionalMeasures.length];
      
      if ((m_clusterer instanceof AdditionalMeasureProducer)) {
        Enumeration en = ((AdditionalMeasureProducer)m_clusterer).enumerateMeasures();
        
        while (en.hasMoreElements()) {
          String mname = (String)en.nextElement();
          for (int j = 0; j < m_additionalMeasures.length; j++) {
            if (mname.compareToIgnoreCase(m_additionalMeasures[j]) == 0) {
              m_doesProduce[j] = true;
            }
          }
        }
      }
    } else {
      m_doesProduce = null;
    }
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    if ((m_clusterer instanceof AdditionalMeasureProducer)) {
      Enumeration en = ((AdditionalMeasureProducer)m_clusterer).enumerateMeasures();
      
      while (en.hasMoreElements()) {
        String mname = (String)en.nextElement();
        newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if ((m_clusterer instanceof AdditionalMeasureProducer)) {
      return ((AdditionalMeasureProducer)m_clusterer).getMeasure(additionalMeasureName);
    }
    
    throw new IllegalArgumentException("DensityBasedClustererSplitEvaluator: Can't return value for : " + additionalMeasureName + ". " + m_clusterer.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  













  public Object[] getKeyTypes()
  {
    Object[] keyTypes = new Object[3];
    keyTypes[0] = "";
    keyTypes[1] = "";
    keyTypes[2] = "";
    return keyTypes;
  }
  







  public String[] getKeyNames()
  {
    String[] keyNames = new String[3];
    keyNames[0] = "Scheme";
    keyNames[1] = "Scheme_options";
    keyNames[2] = "Scheme_version_ID";
    return keyNames;
  }
  









  public Object[] getKey()
  {
    Object[] key = new Object[3];
    key[0] = m_clusterer.getClass().getName();
    key[1] = m_clustererOptions;
    key[2] = m_clustererVersion;
    return key;
  }
  








  public Object[] getResultTypes()
  {
    int addm = m_additionalMeasures != null ? m_additionalMeasures.length : 0;
    

    int overall_length = 6 + addm;
    
    Object[] resultTypes = new Object[overall_length];
    Double doub = new Double(0.0D);
    int current = 0;
    

    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    

    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = doub;
    

    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    



    for (int i = 0; i < addm; i++) {
      resultTypes[(current++)] = doub;
    }
    if (current != overall_length) {
      throw new Error("ResultTypes didn't fit RESULT_SIZE");
    }
    return resultTypes;
  }
  






  public String[] getResultNames()
  {
    int addm = m_additionalMeasures != null ? m_additionalMeasures.length : 0;
    

    int overall_length = 6 + addm;
    
    String[] resultNames = new String[overall_length];
    int current = 0;
    resultNames[(current++)] = "Number_of_training_instances";
    resultNames[(current++)] = "Number_of_testing_instances";
    

    resultNames[(current++)] = "Log_likelihood";
    resultNames[(current++)] = "Number_of_clusters";
    

    resultNames[(current++)] = "Time_training";
    resultNames[(current++)] = "Time_testing";
    



    for (int i = 0; i < addm; i++) {
      resultNames[(current++)] = m_additionalMeasures[i];
    }
    if (current != overall_length) {
      throw new Error("ResultNames didn't fit RESULT_SIZE");
    }
    return resultNames;
  }
  










  public Object[] getResult(Instances train, Instances test)
    throws Exception
  {
    if (m_clusterer == null) {
      throw new Exception("No clusterer has been specified");
    }
    int addm = m_additionalMeasures != null ? m_additionalMeasures.length : 0;
    

    int overall_length = 6 + addm;
    
    if ((m_removeClassColumn) && (train.classIndex() != -1))
    {
      Remove r = new Remove();
      r.setAttributeIndicesArray(new int[] { train.classIndex() });
      r.setInvertSelection(false);
      r.setInputFormat(train);
      train = Filter.useFilter(train, r);
      
      test = Filter.useFilter(test, r);
    }
    train.setClassIndex(-1);
    test.setClassIndex(-1);
    
    ClusterEvaluation eval = new ClusterEvaluation();
    
    Object[] result = new Object[overall_length];
    long trainTimeStart = System.currentTimeMillis();
    m_clusterer.buildClusterer(train);
    double numClusters = m_clusterer.numberOfClusters();
    eval.setClusterer(m_clusterer);
    long trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
    long testTimeStart = System.currentTimeMillis();
    eval.evaluateClusterer(test);
    long testTimeElapsed = System.currentTimeMillis() - testTimeStart;
    



    int current = 0;
    result[(current++)] = new Double(train.numInstances());
    result[(current++)] = new Double(test.numInstances());
    
    result[(current++)] = new Double(eval.getLogLikelihood());
    result[(current++)] = new Double(numClusters);
    

    result[(current++)] = new Double(trainTimeElapsed / 1000.0D);
    result[(current++)] = new Double(testTimeElapsed / 1000.0D);
    
    for (int i = 0; i < addm; i++) {
      if (m_doesProduce[i] != 0) {
        try {
          double dv = ((AdditionalMeasureProducer)m_clusterer).getMeasure(m_additionalMeasures[i]);
          
          Double value = new Double(dv);
          
          result[(current++)] = value;
        } catch (Exception ex) {
          System.err.println(ex);
        }
      } else {
        result[(current++)] = null;
      }
    }
    
    if (current != overall_length) {
      throw new Error("Results didn't fit RESULT_SIZE");
    }
    return result;
  }
  





  public String removeClassColumnTipText()
  {
    return "Remove the class column (if set) from the data.";
  }
  




  public void setRemoveClassColumn(boolean r)
  {
    m_removeClassColumn = r;
  }
  




  public boolean getRemoveClassColumn()
  {
    return m_removeClassColumn;
  }
  





  public String clustererTipText()
  {
    return "The density based clusterer to use.";
  }
  





  public DensityBasedClusterer getClusterer()
  {
    return m_clusterer;
  }
  





  public void setClusterer(DensityBasedClusterer newClusterer)
  {
    m_clusterer = newClusterer;
    updateOptions();
  }
  
  protected void updateOptions()
  {
    if ((m_clusterer instanceof OptionHandler)) {
      m_clustererOptions = Utils.joinOptions(((OptionHandler)m_clusterer).getOptions());
    }
    else {
      m_clustererOptions = "";
    }
    if ((m_clusterer instanceof Serializable)) {
      ObjectStreamClass obs = ObjectStreamClass.lookup(m_clusterer.getClass());
      
      m_clustererVersion = ("" + obs.getSerialVersionUID());
    } else {
      m_clustererVersion = "";
    }
  }
  





  public void setClustererName(String newClustererName)
    throws Exception
  {
    try
    {
      setClusterer((DensityBasedClusterer)Class.forName(newClustererName).newInstance());
    }
    catch (Exception ex) {
      throw new Exception("Can't find Clusterer with class name: " + newClustererName);
    }
  }
  






  public String getRawResultOutput()
  {
    StringBuffer result = new StringBuffer();
    
    if (m_clusterer == null) {
      return "<null> clusterer";
    }
    result.append(toString());
    result.append("Clustering model: \n" + m_clusterer.toString() + '\n');
    

    if (m_result != null)
    {

      if (m_doesProduce != null) {
        for (int i = 0; i < m_doesProduce.length; i++) {
          if (m_doesProduce[i] != 0) {
            try {
              double dv = ((AdditionalMeasureProducer)m_clusterer).getMeasure(m_additionalMeasures[i]);
              
              Double value = new Double(dv);
              
              result.append(m_additionalMeasures[i] + " : " + value + '\n');
            } catch (Exception ex) {
              System.err.println(ex);
            }
          }
        }
      }
    }
    return result.toString();
  }
  






  public String toString()
  {
    String result = "DensityBasedClustererSplitEvaluator: ";
    if (m_clusterer == null) {
      return result + "<null> clusterer";
    }
    return result + m_clusterer.getClass().getName() + " " + m_clustererOptions + "(version " + m_clustererVersion + ")";
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11198 $");
  }
}
