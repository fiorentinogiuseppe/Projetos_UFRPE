package weka.filters;

import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

























































public class CheckSource
  implements OptionHandler, RevisionHandler
{
  protected Filter m_Filter = null;
  

  protected Filter m_SourceCode = null;
  

  protected File m_Dataset = null;
  

  protected int m_ClassIndex = -1;
  

  public CheckSource() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe filter (incl. options) that was used to generate\n\tthe source code.", "W", 1, "-W <classname and options>"));
    



    result.addElement(new Option("\tThe classname of the generated source code.", "S", 1, "-S <classname>"));
    


    result.addElement(new Option("\tThe training set with which the source code was generated.", "t", 1, "-t <file>"));
    


    result.addElement(new Option("\tThe class index of the training set. 'first' and 'last' are\n\tvalid indices.\n\t(default: none)", "c", 1, "-c <index>"));
    




    return result.elements();
  }
  






























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0) {
      String[] spec = Utils.splitOptions(tmpStr);
      if (spec.length == 0)
        throw new IllegalArgumentException("Invalid filter specification string");
      String classname = spec[0];
      spec[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, classname, spec));
    }
    else {
      throw new Exception("No filter (classname + options) provided!"); }
    String classname;
    String[] spec;
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() > 0) {
      spec = Utils.splitOptions(tmpStr);
      if (spec.length != 1)
        throw new IllegalArgumentException("Invalid source code specification string");
      classname = spec[0];
      spec[0] = "";
      setSourceCode((Filter)Utils.forName(Filter.class, classname, spec));
    }
    else {
      throw new Exception("No source code (classname) provided!");
    }
    
    tmpStr = Utils.getOption('t', options);
    if (tmpStr.length() != 0) {
      setDataset(new File(tmpStr));
    } else {
      throw new Exception("No dataset provided!");
    }
    tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() != 0) {
      if (tmpStr.equals("first")) {
        setClassIndex(0);
      } else if (tmpStr.equals("last")) {
        setClassIndex(-2);
      } else {
        setClassIndex(Integer.parseInt(tmpStr) - 1);
      }
    } else {
      setClassIndex(-1);
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getFilter() != null) {
      result.add("-W");
      result.add(getFilter().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getFilter()).getOptions()));
    }
    

    if (getSourceCode() != null) {
      result.add("-S");
      result.add(getSourceCode().getClass().getName());
    }
    
    if (getDataset() != null) {
      result.add("-t");
      result.add(m_Dataset.getAbsolutePath());
    }
    
    if (getClassIndex() != -1) {
      result.add("-c");
      if (getClassIndex() == -2) {
        result.add("last");
      } else if (getClassIndex() == 0) {
        result.add("first");
      } else {
        result.add("" + (getClassIndex() + 1));
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setFilter(Filter value)
  {
    m_Filter = value;
  }
  




  public Filter getFilter()
  {
    return m_Filter;
  }
  




  public void setSourceCode(Filter value)
  {
    m_SourceCode = value;
  }
  




  public Filter getSourceCode()
  {
    return m_SourceCode;
  }
  




  public void setDataset(File value)
  {
    if (!value.exists()) {
      throw new IllegalArgumentException("Dataset '" + value.getAbsolutePath() + "' does not exist!");
    }
    
    m_Dataset = value;
  }
  




  public File getDataset()
  {
    return m_Dataset;
  }
  




  public void setClassIndex(int value)
  {
    m_ClassIndex = value;
  }
  




  public int getClassIndex()
  {
    return m_ClassIndex;
  }
  










  protected boolean compare(Instance inst1, Instance inst2)
  {
    boolean result = inst1.numAttributes() == inst2.numAttributes();
    

    if (result) {
      for (int i = 0; i < inst1.numAttributes(); i++) {
        if ((!Double.isNaN(inst1.value(i))) || (!Double.isNaN(inst2.value(i))))
        {

          if (inst1.value(i) != inst2.value(i)) {
            result = false;
            System.out.println("Values at position " + (i + 1) + " differ (Filter/Source code): " + inst1.value(i) + " != " + inst2.value(i));
            

            break;
          }
        }
      }
    }
    return result;
  }
  










  protected boolean compare(Instances inst1, Instances inst2)
  {
    boolean result = inst1.numInstances() == inst2.numInstances();
    

    if (result) {
      for (int i = 0; i < inst1.numInstances(); i++) {
        result = compare(inst1.instance(i), inst2.instance(i));
        if (!result) {
          System.out.println("Values in line " + (i + 1) + " differ!");
          
          break;
        }
      }
    }
    
    return result;
  }
  















  public boolean execute()
    throws Exception
  {
    boolean result = true;
    

    if (getFilter() == null)
      throw new Exception("No filter set!");
    if (getSourceCode() == null)
      throw new Exception("No source code set!");
    if (getDataset() == null)
      throw new Exception("No dataset set!");
    if (!getDataset().exists()) {
      throw new Exception("Dataset '" + getDataset().getAbsolutePath() + "' does not exist!");
    }
    

    ConverterUtils.DataSource source = new ConverterUtils.DataSource(getDataset().getAbsolutePath());
    Instances data = source.getDataSet();
    if (getClassIndex() == -2) {
      data.setClassIndex(data.numAttributes() - 1);
    } else {
      data.setClassIndex(getClassIndex());
    }
    

    Filter filter = Filter.makeCopy(getFilter());
    filter.setInputFormat(data);
    Instances filteredInstances = Filter.useFilter(data, filter);
    
    Filter filterSource = Filter.makeCopy(getSourceCode());
    filterSource.setInputFormat(data);
    Instances filteredInstancesSource = Filter.useFilter(data, filterSource);
    
    result = compare(filteredInstances, filteredInstancesSource);
    

    if (result) {
      filter = Filter.makeCopy(getFilter());
      filter.setInputFormat(data);
      Filter.useFilter(data, filter);
      
      filterSource = Filter.makeCopy(getSourceCode());
      filterSource.setInputFormat(data);
      
      for (int i = 0; i < data.numInstances(); i++) {
        filter.input(data.instance(i));
        filter.batchFinished();
        Instance filteredInstance = filter.output();
        
        filterSource.input(data.instance(i));
        filterSource.batchFinished();
        Instance filteredInstanceSource = filterSource.output();
        
        if (!compare(filteredInstance, filteredInstanceSource)) {
          System.out.println(i + 1 + ". instance (Filter/Source code): " + filteredInstance + " != " + filteredInstanceSource);
        }
      }
    }
    

    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  








  public static void main(String[] args)
    throws Exception
  {
    CheckSource check = new CheckSource();
    if (Utils.getFlag('h', args)) {
      StringBuffer text = new StringBuffer();
      text.append("\nHelp requested:\n\n");
      Enumeration enm = check.listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        text.append(option.synopsis() + "\n");
        text.append(option.description() + "\n");
      }
      System.out.println("\n" + text + "\n");
    }
    else {
      check.setOptions(args);
      if (check.execute()) {
        System.out.println("Tests OK!");
      } else {
        System.out.println("Tests failed!");
      }
    }
  }
}
