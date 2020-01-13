package weka.associations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Enumeration;
import weka.core.Drawable;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

































public class AssociatorEvaluation
  implements RevisionHandler
{
  protected StringBuffer m_Result;
  
  public AssociatorEvaluation()
  {
    m_Result = new StringBuffer();
  }
  







  protected static String makeOptionString(Associator associator)
  {
    StringBuffer text = new StringBuffer();
    

    text.append("\nGeneral options:\n\n");
    text.append("-t <training file>\n");
    text.append("\tThe name of the training file.\n");
    text.append("-g <name of graph file>\n");
    text.append("\tOutputs the graph representation (if supported) of the associator to a file.\n");
    

    if ((associator instanceof OptionHandler)) {
      text.append("\nOptions specific to " + associator.getClass().getName().replaceAll(".*\\.", "") + ":\n\n");
      


      Enumeration enm = ((OptionHandler)associator).listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        text.append(option.synopsis() + "\n");
        text.append(option.description() + "\n");
      }
    }
    
    return text.toString();
  }
  




  public static String evaluate(String associatorString, String[] options)
    throws Exception
  {
    Associator associator;
    


    try
    {
      associator = (Associator)Class.forName(associatorString).newInstance();
    }
    catch (Exception e) {
      throw new Exception("Can't find class with name " + associatorString + '.');
    }
    
    return evaluate(associator, options);
  }
  









  public static String evaluate(Associator associator, String[] options)
    throws Exception
  {
    String trainFileString = "";
    String graphFileName = "";
    



    if (Utils.getFlag('h', options)) {
      throw new Exception("\nHelp requested.\n" + makeOptionString(associator));
    }
    ConverterUtils.DataSource loader;
    try {
      trainFileString = Utils.getOption('t', options);
      if (trainFileString.length() == 0)
        throw new Exception("No training file given!");
      loader = new ConverterUtils.DataSource(trainFileString);
      
      graphFileName = Utils.getOption('g', options);
      

      if ((associator instanceof OptionHandler)) {
        ((OptionHandler)associator).setOptions(options);
      }
      

      Utils.checkForRemainingOptions(options);
    }
    catch (Exception e) {
      throw new Exception("\nWeka exception: " + e.getMessage() + "\n" + makeOptionString(associator));
    }
    




    AssociatorEvaluation eval = new AssociatorEvaluation();
    String results = eval.evaluate(associator, new Instances(loader.getDataSet()));
    

    if (((associator instanceof Drawable)) && (graphFileName.length() != 0)) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(graphFileName));
      writer.write(((Drawable)associator).graph());
      writer.newLine();
      writer.flush();
      writer.close();
    }
    
    return results;
  }
  













  public String evaluate(Associator associator, Instances data)
    throws Exception
  {
    long startTime = System.currentTimeMillis();
    associator.buildAssociations(data);
    long endTime = System.currentTimeMillis();
    
    m_Result = new StringBuffer(associator.toString());
    m_Result.append("\n=== Evaluation ===\n\n");
    m_Result.append("Elapsed time: " + (endTime - startTime) / 1000.0D + "s");
    m_Result.append("\n");
    
    return m_Result.toString();
  }
  






  public boolean equals(Object obj)
  {
    if ((obj == null) || (!obj.getClass().equals(getClass()))) {
      return false;
    }
    AssociatorEvaluation cmp = (AssociatorEvaluation)obj;
    

    String associatingResults1 = m_Result.toString().replaceAll("Elapsed time.*", "");
    String associatingResults2 = m_Result.toString().replaceAll("Elapsed time.*", "");
    if (!associatingResults1.equals(associatingResults2)) {
      return false;
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
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  





  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("The first argument must be the class name of a kernel");
      }
      
      String associator = args[0];
      args[0] = "";
      System.out.println(evaluate(associator, args));
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
