package weka.classifiers.functions.supportVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;









































public class KernelEvaluation
  implements RevisionHandler
{
  protected StringBuffer m_Result;
  protected double[][] m_Evaluations;
  protected int m_NumEvals;
  protected int m_NumCacheHits;
  protected String[] m_Options;
  
  public KernelEvaluation()
  {
    m_Result = new StringBuffer();
    m_Evaluations = new double[0][0];
    m_Options = new String[0];
    m_NumEvals = 0;
    m_NumCacheHits = 0;
  }
  




  public void setUserOptions(String[] options)
  {
    m_Options = ((String[])options.clone());
  }
  




  public String[] getUserOptions()
  {
    return (String[])m_Options.clone();
  }
  







  protected static String makeOptionString(Kernel Kernel)
  {
    StringBuffer text = new StringBuffer();
    

    text.append("\nGeneral options:\n\n");
    text.append("-t <training file>\n");
    text.append("\tThe name of the training file.\n");
    text.append("-c <class index>\n");
    text.append("\tSets index of class attribute (default: last).\n");
    

    if ((Kernel instanceof OptionHandler)) {
      text.append("\nOptions specific to " + Kernel.getClass().getName().replaceAll(".*\\.", "") + ":\n\n");
      


      Enumeration enm = Kernel.listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        text.append(option.synopsis() + "\n");
        text.append(option.description() + "\n");
      }
    }
    
    return text.toString();
  }
  









  public static String evaluate(Kernel Kernel, String[] options)
    throws Exception
  {
    String trainFileString = "";
    


    int classIndex = -1;
    



    if (Utils.getFlag('h', options))
      throw new Exception("\nHelp requested.\n" + makeOptionString(Kernel));
    BufferedReader reader;
    String[] userOptions;
    try {
      trainFileString = Utils.getOption('t', options);
      if (trainFileString.length() == 0)
        throw new Exception("No training file given!");
      reader = new BufferedReader(new FileReader(trainFileString));
      
      String classIndexString = Utils.getOption('c', options);
      if (classIndexString.length() != 0) {
        if (classIndexString.equals("first")) {
          classIndex = 1;
        } else if (classIndexString.equals("last")) {
          classIndex = -1;
        } else {
          classIndex = Integer.parseInt(classIndexString);
        }
      }
      
      userOptions = (String[])options.clone();
      if ((Kernel instanceof OptionHandler)) {
        Kernel.setOptions(options);
      }
      

      Utils.checkForRemainingOptions(options);
    }
    catch (Exception e) {
      throw new Exception("\nWeka exception: " + e.getMessage() + "\n" + makeOptionString(Kernel));
    }
    




    KernelEvaluation eval = new KernelEvaluation();
    eval.setUserOptions(userOptions);
    Instances train = new Instances(reader);
    if (classIndex == -1) {
      train.setClassIndex(train.numAttributes() - 1);
    } else {
      train.setClassIndex(classIndex);
    }
    return eval.evaluate(Kernel, train);
  }
  




  public static String evaluate(String kernelString, String[] options)
    throws Exception
  {
    Kernel kernel;
    


    try
    {
      kernel = (Kernel)Class.forName(kernelString).newInstance();
    }
    catch (Exception e) {
      throw new Exception("Can't find class with name " + kernelString + '.');
    }
    
    return evaluate(kernel, options);
  }
  














  public String evaluate(Kernel kernel, Instances data)
    throws Exception
  {
    m_Result = new StringBuffer();
    

    long startTime = System.currentTimeMillis();
    kernel.buildKernel(data);
    long endTime = System.currentTimeMillis();
    m_Result.append("\n=== Model ===\n\n");
    if (Utils.joinOptions(getUserOptions()).trim().length() != 0)
      m_Result.append("Options: " + Utils.joinOptions(getUserOptions()) + "\n\n");
    m_Result.append(kernel.toString() + "\n");
    

    m_Evaluations = new double[data.numInstances()][data.numInstances()];
    for (int n = 0; n < data.numInstances(); n++) {
      for (int i = n; i < data.numInstances(); i++) {
        m_Evaluations[n][i] = kernel.eval(n, i, data.instance(n));
      }
    }
    

    if ((kernel instanceof CachedKernel)) {
      for (n = 0; n < data.numInstances(); n++) {
        for (int i = n; i < data.numInstances(); i++) {
          m_Evaluations[n][i] = kernel.eval(n, i, data.instance(n));
        }
      }
    }
    
    m_NumEvals = kernel.numEvals();
    m_NumCacheHits = kernel.numCacheHits();
    

    m_Result.append("\n=== Evaluation ===\n\n");
    if ((kernel instanceof CachedKernel)) {
      m_Result.append("Cache size   : " + ((CachedKernel)kernel).getCacheSize() + "\n");
    }
    m_Result.append("# Evaluations: " + m_NumEvals + "\n");
    m_Result.append("# Cache hits : " + m_NumCacheHits + "\n");
    m_Result.append("Elapsed time : " + (endTime - startTime) / 1000.0D + "s\n");
    
    return m_Result.toString();
  }
  






  public boolean equals(Object obj)
  {
    if ((obj == null) || (!obj.getClass().equals(getClass()))) {
      return false;
    }
    KernelEvaluation cmp = (KernelEvaluation)obj;
    
    if (m_NumEvals != m_NumEvals) return false;
    if (m_NumCacheHits != m_NumCacheHits) { return false;
    }
    if (m_Evaluations.length != m_Evaluations.length)
      return false;
    for (int n = 0; n < m_Evaluations.length; n++) {
      for (int i = 0; i < m_Evaluations[n].length; i++) {
        if ((!Double.isNaN(m_Evaluations[n][i])) || (!Double.isNaN(m_Evaluations[n][i])))
        {
          if (m_Evaluations[n][i] != m_Evaluations[n][i])
            return false;
        }
      }
    }
    return true;
  }
  




  public String toSummaryString()
  {
    return toSummaryString("");
  }
  







  public String toSummaryString(String title)
  {
    StringBuffer result = new StringBuffer(title);
    if (title.length() != 0)
      result.append("\n");
    result.append(m_Result);
    
    return result.toString();
  }
  





  public String toString()
  {
    return toSummaryString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  





  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("The first argument must be the class name of a kernel");
      }
      
      String kernel = args[0];
      args[0] = "";
      System.out.println(evaluate(kernel, args));
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
